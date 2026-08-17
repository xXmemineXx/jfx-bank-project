package com.bank.models;

import java.time.LocalDateTime;

public class Transfer
{
	private int transfer_id;
	private int amount;
	private String sender_id;
	private String receiver_id;
	private LocalDateTime transfer_date;

	public Transfer(int id, int money, String sender, String receiver, LocalDateTime transfer)
	{
		this.transfer_id = id;
		this.amount = money;
		this.sender_id = sender;
		this.receiver_id = receiver;
		this.transfer_date = transfer;
	}

	//setters
	public void set_amount(int money){ this.amount = money;}

	//getters
	public int get_id() { return this.transfer_id; }
	public int get_amount() { return this.amount; }
	public String get_sender() { return this.sender_id; }
	public String get_receiver() {return this.receiver_id; }
	public LocalDateTime get_date() { return this.transfer_date; }
}