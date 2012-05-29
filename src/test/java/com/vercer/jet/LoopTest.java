package com.vercer.jet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.Assert;

import org.junit.Test;

import com.google.common.io.CharStreams;
import com.vercer.jet.transform.Label;
import com.vercer.jet.transform.Loop;
import com.vercer.jet.transform.Transformer;

public class LoopTest
{
	@Test
	public void repeat() throws IOException
	{
		Jet.set(new TestJet());
		
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
			public Transformer getChild()
			{
				return new Label("Hello " + count);
			}
		};
		
		Markup modified = component.transform(markup);
		
		Assert.assertEquals(modified.getChildren().size(), 10);
	}
	
}
