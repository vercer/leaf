package com.vercer.leaf;

import com.vercer.leaf.exchange.Label;
import com.vercer.leaf.html.Page;

public class BasePage extends Page
{
	private Label what = new Label("cow");
	
	public Label getWhat()
	{
		return this.what;
	}
}
