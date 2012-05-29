package com.vercer.jet;

import com.vercer.convert.DirectTypeConverter;


public class TestJet extends Jet
{
	public TestJet()
	{
		settings = Settings.builder().build();
		renderer = new Renderer(false, false);
		converter = new DirectTypeConverter();
	}
}
