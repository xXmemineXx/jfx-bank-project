package com.bank.models;

import java.time.LocalDateTime;

public class History
{
	private String from;
	private String operation;
	private String subject;
	private String target;
	private LocalDateTime date;

	public History(String table, String action, String on, String to, LocalDateTime at)
	{
		this.from = table;
		this.operation = action;
		this.subject = on;
		this.target = to;
		this.date = at;
	}

	//getters
	public String getFrom() { return from; }
    public String getOperation() { return operation; }
    public String getSubject() { return subject; }
    public String getTarget() { return target; }
    public LocalDateTime getDate() { return date; }
}