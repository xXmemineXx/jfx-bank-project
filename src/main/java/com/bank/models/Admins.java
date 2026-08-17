package com.bank.models;

public class Admins
{
	private int id;
	private String admin_last_name;
	private String admin_first_name;
	private String email;
	private String password;

	public Admins(int id, String l_name, String f_name, String mail, String pswd)
	{
		this.id = id;
		this.admin_last_name = l_name;
		this.admin_first_name = f_name;
		this.email = mail;
		this.password = pswd;
	}
	
	//setters
	public void set_id(int id) { this.id = id; }
	public void set_last_name(String l_name) { this.admin_last_name = l_name; }
	public void set_first_name(String f_name) { this.admin_first_name = f_name; }
	public void set_mail(String mail) { this.email = mail; }
	public void set_password(String pswd) { this.password = pswd; }
	
	//geters
	public int get_id() { return this.id; }
	public String get_last_name() { return this.admin_last_name; }
	public String get_first_name() { return this.admin_first_name; }
	public String get_mail() { return this.email; }
	public String get_password() { return this.password; }
}