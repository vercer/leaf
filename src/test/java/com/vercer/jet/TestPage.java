package com.vercer.jet;

import com.vercer.jet.transform.Label;

public class TestPage extends BasePage
{
	private Label how = new Label("moo");
	
	public Label getHow()
	{
		return this.how;
	}
}
