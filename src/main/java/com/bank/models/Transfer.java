package com.bank.models;

import java.time.LocalDateTime;

public class Transfer
{
	private int transfer_id;
	private int amount;
	private String sender_id;
	private String sender_name;
	private String receiver_id;
	private String receiver_name;
	private LocalDateTime transfer_date;

	public Transfer(int id, int money, String sender, String senderName, String receiver, String receiverName, LocalDateTime transfer)
	{
		this.transfer_id = id;
		this.amount = money;
		this.sender_id = sender;
		this.receiver_id = receiver;
		this.transfer_date = transfer;
		this.sender_name = receiverName;
		this.receiver_name = receiverName;
	}

	//setters
	public void set_amount(int money){ this.amount = money;}

	//getters
	public int get_id() { return this.transfer_id; }
	public int get_amount() { return this.amount; }
	public String get_sender() { return this.sender_id; }
	public String get_sender_name() { return this.receiver_name; }
	public String get_receiver_name() { return this.receiver_name; }
	public String get_receiver() {return this.receiver_id; }
	public LocalDateTime get_date() { return this.transfer_date; }
}