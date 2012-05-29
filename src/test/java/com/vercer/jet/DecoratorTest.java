package com.vercer.jet;

import junit.framework.Assert;

import org.junit.Test;


public class DecoratorTest
{
	@Test
	public void decorator()
	{
		Jet.set(new TestJet());
		
		MyDecorated decorator = new MyDecorated();
		Markup markup = decorator.getMarkup();
		Renderer renderer = new Renderer(true, true);
		String rendered = renderer.render(markup);
		
		String expected = "<html>\n" + 
				"	<head></head>\n" + 
				"	<body>\n" + 
				"		Hi <span>there!</span>\n" + 
				"		\n" + 
				"\n" + 
				"	This is in the other bit\n" + 
				"\n" + 
				"	<div>with this</div>\n" + 
				"\n" + 
				"\n" + 
				"		Bye\n" + 
				"	</body>\n" + 
				"</html>";
		
		Assert.assertEquals(expected, rendered);
	}
}
