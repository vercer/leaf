package com.vercer.leaf;

import lombok.Getter;

import com.vercer.leaf.exchange.Label;

public class MyDecorated extends MyDecorator
{
	@Getter
	private Label other = new Label("with this");

	@Getter
	private Label who = new Label("there!");
	
}
