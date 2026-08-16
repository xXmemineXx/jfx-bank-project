package com.bank.models;

public class Client
{
	private int balance;
	private String account_id;
	private String last_name;
	private String first_name;
	private String email;
	private String phone;

	public Client(String id, String l_name, String f_name, String mail, String number, int money)
	{
		this.account_id = id;
		this.last_name = l_name;
		this.first_name = f_name;
		this.email = mail;
		this.phone = number;
		this.balance = money;
	}
	
	//setters
	public void set_balance(int balance) { this.balance = balance; }
	public void set_id(String id) { this.account_id = id; }
	public void set_last_name(String l_name) { this.last_name = l_name; }
	public void set_first_name(String f_name) { this.first_name = f_name; }
	public void set_mail(String mail) { this.email = mail; }
	public void set_phone(String number) { this.phone = number; }
	
	//geters
	public int get_balance() { return this.balance; }
	public String get_id() { return this.account_id; }
	public String get_last_name() { return this.last_name; }
	public String get_first_name() { return this.first_name; }
	public String get_mail() { return this.email; }
	public String get_phone() { return this.phone; }
}