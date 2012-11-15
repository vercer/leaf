package com.vercer.leaf;

import junit.framework.Assert;

import org.junit.Test;


public class DecoratorTest
{
	@Test
	public void decorator()
	{
		Leaf.set(new TestJet());
		
		MyDecorated decorator = new MyDecorated();
		Markup markup = decorator.getMarkup();
		Renderer renderer = new Renderer(true, true);
		String rendered = renderer.render(markup);
		
		String expected = "<html xmlns:jet=\"com.vercer.jet\">\n" + 
				"	<head></head>\n" + 
				"	<body>\n" + 
				"		Hi <span>there!</span>\n" + 
				"		<div xmlns:jet=\"com.vercer.jet\">\n" + 
				"\n" + 
				"	This is in the other bit\n" + 
				"\n" + 
				"	<div>with this</div>\n" + 
				"\n" + 
				"</div>\n" + 
				"		Bye\n" + 
				"	</body>\n" + 
				"</html>";
		
		Assert.assertEquals(expected, rendered);
	}
}
