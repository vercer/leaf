package com.vercer.jet;

import lombok.Getter;

import com.vercer.jet.transform.Label;

public class MyDecorated extends MyDecorator
{
	@Getter
	private Label other = new Label("with this");

	@Getter
	private Label who = new Label("there!");
	
}
