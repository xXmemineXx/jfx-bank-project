package com.bank.controller;

import com.bank.dao.*;
import com.bank.models.*;

import java.util.ArrayList;
import java.util.List;

public class HomeController
{
	private HistoryDAO Hdao = new HistoryDAO();
	private List<History> clientList = Hdao.gethistory();
}