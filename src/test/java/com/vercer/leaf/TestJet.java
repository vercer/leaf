package com.vercer.leaf;

import com.vercer.convert.DirectTypeConverter;


public class TestJet extends Leaf
{
	public TestJet()
	{
		settings = Settings.builder().build();
		renderer = new Renderer(false, false);
		converter = new DirectTypeConverter();
	}
}
