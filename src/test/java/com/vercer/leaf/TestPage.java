package com.vercer.leaf;

import com.vercer.leaf.exchange.Label;

public class TestPage extends BasePage
{
	private Label how = new Label("moo");
	
	public Label getHow()
	{
		return this.how;
	}
}
