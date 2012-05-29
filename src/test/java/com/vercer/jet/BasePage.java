package com.vercer.jet;

import com.vercer.jet.transform.Label;
import com.vercer.jet.transform.Page;

public class BasePage extends Page
{
	private Label what = new Label("cow");
	
	public Label getWhat()
	{
		return this.what;
	}
}
