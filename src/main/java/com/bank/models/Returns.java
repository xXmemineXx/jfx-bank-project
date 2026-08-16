package com.bank.models;

import java.time.LocalDateTime;

class Returns
{
	private boolean fully_returned;
	private int returned_amount;
	private int unpayed;
	private String return_id;
	private String loan_id;
	private LocalDateTime return_date;

	public Returns(boolean full, int amount, int unpayed, String id, String loan, LocalDateTime r_date)
	{
		this.fully_returned = full;
		this.returned_amount = amount;
		this.unpayed = unpayed;
		this.return_id = id;
		this.loan_id = loan;
		this.return_date = r_date;
	}

	//setters
	public void set_amount(int money) { this.returned_amount = money; }
	public void set_id(String id) { this.return_id = id; }

	//getters
	public boolean is_repayed() { return this.fully_returned; }
	public int get_amount() { return this.returned_amount; }
	public int get_unpayed() { return this.unpayed; }
	public String get_id() { return this.return_id; }
	public String get_loan() { return this.loan_id; }
	public LocalDateTime get_date() { return this.return_date; }
}