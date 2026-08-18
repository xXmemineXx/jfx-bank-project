package com.bank.models;

import java.time.LocalDateTime;

public class Returns
{
	private int returned_amount;
	private int unpayed;
	private int loan_amount;
	private boolean fully_returned;
	private String status;
	private String return_id;
	private String loan_id;
	private String debtorName;
	private LocalDateTime return_date;

	public Returns(String status, int loan,int amount, int unpayed, String id, String loanId, String debtor, LocalDateTime r_date, boolean full)
	{
		this.status = status;
		this.returned_amount = amount;
		this.loan_amount = loan;
		this.unpayed = unpayed;
		this.return_id = id;
		this.loan_id = loanId;
		this.return_date = r_date;
		this.debtorName = debtor;
	}

	//setters
	public void set_amount(int money) { this.returned_amount = money; }
	public void set_id(String id) { this.return_id = id; }

	//getters
	public Boolean is_repayed() { return this.fully_returned; }
	public int get_amount() { return this.returned_amount; }
	public int get_unpayed() { return this.unpayed; }
	public int get_loan_amount() { return this.loan_amount; }
	public String get_debtor() { return this.debtorName; }
	public String get_id() { return this.return_id; }
	public String get_loan() { return this.loan_id; }
	public String get_status() { return this.status; }
	public LocalDateTime get_date() { return this.return_date; }
}