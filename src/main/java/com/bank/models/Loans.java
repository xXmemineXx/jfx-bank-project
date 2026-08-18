package com.bank.models;

import java.time.LocalDateTime;

public class Loans
{
	private int amount;
	private String loan_id;
	private String debtor_id;
	private String debtor_name;
	private LocalDateTime loan_date;

	public Loans( int money, String id, String debtor, String debtorm, LocalDateTime loan)
	{
		this.amount = money;
		this.loan_id = id;
		this.debtor_id = debtor;
		this.loan_date = loan;
		this.debtor_name = debtorm;
	}

	//setters
	public void set_id(String id) { this.loan_id = id; }
	public void set_amount(int money) { this.amount = money; }

	//getters
	public int get_amount() { return this.amount; }
	public String get_id() { return this.loan_id; }
	public String get_debtor() {return this.debtor_id; }
	public String get_debtor_name() {return this.debtor_name; }
	public LocalDateTime get_date() { return this.loan_date; }
}