package com.vercer.leaf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.CharStreams;
import com.vercer.leaf.exchange.Exchanger;
import com.vercer.leaf.exchange.Label;
import com.vercer.leaf.exchange.Loop;

public class LoopTest
{
	@Test
	public void repeat() throws IOException
	{
		Leaf.set(new TestJet());
		
		InputStream is = getClass().getResourceAsStream("LoopTest.html");
		InputStreamReader isr = new InputStreamReader(is);
		String template = CharStreams.toString(isr);
		Parser parser = new Parser();
		Markup markup = parser.parse(template);
		
		Loop<Void> component = new Loop<Void>()
		{
			int count;
			
			@Override
			protected boolean loop()
			{
				return count++ < 10;
			}
			
			@SuppressWarnings("unused")
			public Exchanger getChild()
			{
				return new Label("Hello " + count);
			}
		};
		
		Markup modified = component.exchange(markup);
		
		Assert.assertEquals(modified.getChildren().size(), 10);
	}
	
}
