package com.vercer.leaf.html;

import com.vercer.leaf.exchange.AttributeModifier;

public class ClassNameAppender extends AttributeModifier
{
	public ClassNameAppender(String value)
	{
		super("class", value);
	}
	
	@Override
	protected String modifyExisting(String existing, String replacement)
	{
		return existing == null ? replacement : existing + " " + replacement;
	}
}
