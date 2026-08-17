DROP TABLE IF EXISTS transfer;
DROP TABLE IF EXISTS returned;
DROP TABLE IF EXISTS loans;
DROP TABLE IF EXISTS clients;
DROP TABLE IF EXISTS history;

DROP TRIGGER IF EXISTS loan_log ON loans;
DROP TRIGGER IF EXISTS return_log ON returned;
DROP TRIGGER IF EXISTS transfer_sync ON transfer;
DROP TRIGGER IF EXISTS prevent_multiple_loans ON loans;
DROP TRIGGER IF EXISTS loan_sync ON loans;
DROP TRIGGER IF EXISTS return_sync ON returned;
DROP TRIGGER IF EXISTS calculate_loans ON returned;

CREATE OR REPLACE FUNCTION calculate_loan()
RETURNS TRIGGER AS $$
DECLARE
    original_loan_amount INTEGER;
BEGIN
    -- 1. Find the total amount borrowed from the loans table
    SELECT amount INTO original_loan_amount 
    FROM loans 
    WHERE loan_id = NEW.loan_id;

    -- Safety check: If the loan ID does not exist, throw an error
    IF original_loan_amount IS NULL THEN
        RAISE EXCEPTION 'Invalid Loan ID: % does not exist in the loans ledger.', NEW.loan_id;
    END IF;

    -- 2. Prevent overpaying the loan amount
    IF NEW.returned_amount > original_loan_amount THEN
        RAISE EXCEPTION 'Payment rejected: Repayment amount (%) exceeds the total loan balance (%).', 
            NEW.returned_amount, original_loan_amount;
    END IF;

    -- 3. Calculate remaining unpaid debt
    NEW.unpayed := original_loan_amount - NEW.returned_amount;

    -- 4. Automatically switch fully_returned status
    IF NEW.unpayed = 0 THEN
        NEW.fully_returned := TRUE;
    ELSE
        NEW.fully_returned := FALSE;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION check_active_loan()
RETURNS TRIGGER AS $$
DECLARE
    last_loan_id VARCHAR(10);
    is_fully_returned BOOLEAN;
BEGIN
    -- 1. Find the debtor's most recent loan ID (if any exists)
    SELECT loan_id INTO last_loan_id
    FROM loans
    WHERE debtor_id = NEW.debtor_id
    ORDER BY loan_date DESC
    LIMIT 1;

    -- 2. If they have a prior loan, check if it has been fully paid off
    IF last_loan_id IS NOT NULL THEN
        -- Look up the status in the 'returned' table
        SELECT fully_returned INTO is_fully_returned
        FROM returned
        WHERE loan_id = last_loan_id;

        -- OR if the loan exists in 'loans' but no payment attempts exist in 'returned' yet.
        IF is_fully_returned IS NULL OR is_fully_returned = FALSE THEN
            RAISE EXCEPTION 'Loan rejected: Debtor % still has an active, unreturned loan (%)', 
                NEW.debtor_id, last_loan_id;
        END IF;
    END IF;

    -- If everything is fine, allow the insert
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sync_return()
RETURNS TRIGGER AS $$
DECLARE
    loan_debtor_id VARCHAR(15);
    amount_difference INTEGER;
BEGIN
    -- Find the client ID owning the target loan record
    IF (TG_OP = 'INSERT' OR TG_OP = 'UPDATE') THEN
        SELECT debtor_id INTO loan_debtor_id FROM loans WHERE loan_id = NEW.loan_id;
    ELSE
        SELECT debtor_id INTO loan_debtor_id FROM loans WHERE loan_id = OLD.loan_id;
    END IF;

    -- CASE 1: New Return Entry (Subtract cash from client's wallet)
    IF (TG_OP = 'INSERT') THEN
        UPDATE clients 
        SET balance = balance - NEW.returned_amount 
        WHERE account_id = loan_debtor_id;
        RETURN NEW;

    -- CASE 2: Edited Return Amount
    ELSIF (TG_OP = 'UPDATE') THEN
        amount_difference := NEW.returned_amount - OLD.returned_amount;
        UPDATE clients 
        SET balance = balance - amount_difference 
        WHERE account_id = loan_debtor_id;
        RETURN NEW;

    -- CASE 3: Deleted Return Entry (Refund the money back into their wallet)
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE clients 
        SET balance = balance + OLD.returned_amount 
        WHERE account_id = loan_debtor_id;
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION sync_loan()
RETURNS TRIGGER AS $$
DECLARE
    amount_difference INTEGER;
BEGIN
    -- CASE 1: New Loan (Add money to client's wallet)
    IF (TG_OP = 'INSERT') THEN
        UPDATE clients 
        SET balance = balance + NEW.amount 
        WHERE account_id = NEW.debtor_id;
        RETURN NEW;

    -- CASE 2: Edited Loan Amount (Adjust the difference)
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Prevent changing the debtor account to maintain consistent histories
        IF (OLD.debtor_id <> NEW.debtor_id) THEN
            RAISE EXCEPTION 'Loan update failed: You cannot change the debtor profile after creation.';
        END IF;

        amount_difference := NEW.amount - OLD.amount;
        UPDATE clients 
        SET balance = balance + amount_difference 
        WHERE account_id = NEW.debtor_id;
        RETURN NEW;

    -- CASE 3: Deleted Loan (Deduct the borrowed cash out of the wallet)
    ELSIF (TG_OP = 'DELETE') THEN
        UPDATE clients 
        SET balance = balance - OLD.amount 
        WHERE account_id = OLD.debtor_id;
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION process_client_transfer()
RETURNS TRIGGER AS $$
DECLARE
    sender_balance INTEGER;
    amount_difference INTEGER;
BEGIN
    -- CASE 1: NEW TRANSFER (INSERT)
    IF (TG_OP = 'INSERT') THEN
        -- 1. Check sender's current balance
        SELECT balance INTO sender_balance FROM clients WHERE account_id = NEW.sender_id;
        
        -- 2. Validate funds
        IF sender_balance < NEW.amount THEN
            RAISE EXCEPTION 'Transfer failed: Insufficient funds. Sender balance is %', sender_balance;
        END IF;

        -- 3. Move the money
        UPDATE clients SET balance = balance - NEW.amount WHERE account_id = NEW.sender_id;
        UPDATE clients SET balance = balance + NEW.amount WHERE account_id = NEW.receiver_id;
        
        RETURN NEW;

    -- CASE 2: EDITED TRANSFER (UPDATE)
    ELSIF (TG_OP = 'UPDATE') THEN
        -- Prevent changing the accounts involved in a transfer edit to avoid major balance chaos
        IF (OLD.sender_id <> NEW.sender_id OR OLD.receiver_id <> NEW.receiver_id) THEN
            RAISE EXCEPTION 'Transfer edit failed: You cannot change the sender or receiver accounts after a transfer is created.';
        END IF;

        -- Calculate the difference between the new amount and the old amount
        -- Example: If old was 100 and new is 150, difference is +50 (sender needs 50 less, receiver needs 50 more)
        -- Example: If old was 100 and new is 40, difference is -60 (sender gets 60 back, receiver loses 60)
        amount_difference := NEW.amount - OLD.amount;

        -- Check if sender has enough money for the *extra* amount needed (if the transfer grew)
        SELECT balance INTO sender_balance FROM clients WHERE account_id = NEW.sender_id;
        IF amount_difference > sender_balance THEN
            RAISE EXCEPTION 'Transfer edit failed: Insufficient funds for the updated amount. Sender needs % more but only has %', amount_difference, sender_balance;
        END IF;

        -- Adjust both client balances according to the difference
        UPDATE clients SET balance = balance - amount_difference WHERE account_id = NEW.sender_id;
        UPDATE clients SET balance = balance + amount_difference WHERE account_id = NEW.receiver_id;

        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION client_history()
RETURNS TRIGGER AS $$
  BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO history (from_, operation_, subject_)
        VALUES ('clients', 'INSERT', NEW.account_id);
        RETURN NEW;
        
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('clients', 'UPDATE', NEW.account_id, OLD.account_id);
        RETURN NEW;
        
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO history (from_, operation_, subject_)
        VALUES ('clients', 'DELETE', OLD.account_id);
        RETURN OLD;
    END IF;
  END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION transfer_history()
RETURNS TRIGGER AS $$
  BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('transfer', 'INSERT', NEW.sender_id, new.receiver_id);
        RETURN NEW;
        
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('transfer', 'UPDATE', NEW.sender_id, NEW.receiver_id);
        RETURN NEW;
        
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('transfer', 'DELETE', OLD.sender_id, OLD.receiver_id);
        RETURN OLD;
    END IF;
  END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION loan_history()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('loan', 'INSERT', NEW.loan_id, NEW.debtor_id);
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('loan', 'UPDATE', NEW.loan_id, NEW.debtor_id);
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('loan', 'DELETE', OLD.loan_id, OLD.debtor_id);
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION return_history()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('return', 'INSERT', NEW.return_id, NEW.loan_id);
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('return', 'UPDATE', NEW.return_id, NEW.loan_id);
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO history (from_, operation_, subject_, target_)
        VALUES ('return', 'DELETE', OLD.return_id, OLD.loan_id);
        RETURN OLD;
    END IF;
END;
$$ LANGUAGE plpgsql;

Create table clients (
  account_id varchar(15) PRIMARY KEY,
  first_name varchar(20) NOT NULL,
  last_name varchar(20),
  phone varchar(13),
  email varchar(60),
  balance INTEGER NOT NULL DEFAULT 0

  constraint chk_positive_solde check (balance > 0)
);

create table transfer(
  transfer_id serial primary key,
  sender_id varchar(15) references clients(account_id),
  receiver_id varchar(15) references clients(account_id),
  amount integer not null,
  transfer_date timestamp not null default current_timestamp,

  CONSTRAINT chk_different_accounts CHECK (sender_id <> receiver_id),
  CONSTRAINT chk_positive_amount CHECK (amount > 0)
);

create table loans(
  loan_id varchar(10) primary key,
  debtor_id varchar(15) references clients(account_id),
  amount integer not null,
  loan_date timestamp not null default current_timestamp,

  constraint chk_positive_amount CHECK (amount > 0)
);

create table returned(
  return_id varchar(10) primary key,
  loan_id varchar(10) references loans(loan_id),
  fully_returned boolean not null default false,
  unpayed integer not null default 0,
  returned_amount integer not null,
  return_date timestamp not null default current_timestamp,

  constraint chk_positive_amount CHECK (returned_amount > 0)
);

create table history (
  from_ varchar(10) not null,
  operation_ varchar(10) not null,
  subject_ varchar(15) not null,
  target_ varchar(15) default null,
  date_ timestamp default current_timestamp
);

CREATE TRIGGER client_log
AFTER INSERT OR UPDATE OR DELETE ON clients
FOR EACH ROW
EXECUTE FUNCTION client_history();

CREATE TRIGGER transfer_log
AFTER INSERT OR UPDATE OR DELETE ON transfer
FOR EACH ROW
EXECUTE FUNCTION transfer_history();

CREATE TRIGGER loan_log
AFTER INSERT OR UPDATE OR DELETE ON loans
FOR EACH ROW
EXECUTE FUNCTION loan_history();

CREATE TRIGGER return_log
AFTER INSERT OR UPDATE OR DELETE ON returned
FOR EACH ROW
EXECUTE FUNCTION return_history();

CREATE TRIGGER transfer_sync
BEFORE INSERT OR UPDATE ON transfer
FOR EACH ROW
EXECUTE FUNCTION process_client_transfer();

CREATE TRIGGER loan_sync
AFTER INSERT OR UPDATE OR DELETE ON loans
FOR EACH ROW
EXECUTE FUNCTION sync_loan();

CREATE TRIGGER return_sync
AFTER INSERT OR UPDATE OR DELETE ON returned
FOR EACH ROW
EXECUTE FUNCTION sync_return();

CREATE TRIGGER prevent_multiple_loans
BEFORE INSERT ON loans
FOR EACH ROW
EXECUTE FUNCTION check_active_loan();

CREATE TRIGGER update_returned
BEFORE INSERT OR UPDATE ON returned
FOR EACH ROW
EXECUTE FUNCTION calculate_loan();