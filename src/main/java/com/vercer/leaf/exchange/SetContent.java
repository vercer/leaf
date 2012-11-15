package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public class SetContent implements Exchanger
{
	private String content;
	public SetContent(String content)
	{
		this.content = content;
	}
	
	@Override
	public Markup exchange(Markup markup)
	{
		return Markup.builder(markup).content(content).build();
	}
}
