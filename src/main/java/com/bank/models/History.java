package com.bank.models;

import java.time.LocalDateTime;

public class History
{
	private String from_;
	private String operation_;
	private String subject_;
	private String target_;
	private LocalDateTime date_;

	public History(String table, String action, String on, String to, LocalDateTime at)
	{
		this.from_ = table;
		this.operation_ = action;
		this.subject_ = on;
		this.target_ = to;
		this.date_ = at;
	}

	//getters
	public String get_table() { return this.from_; }
	public String get_action() { return this.operation_; }
	public String get_subject() { return this.subject_; }
	public String get_target() { return this.target_; }
	public LocalDateTime get_h_date() { return this.date_; }
}