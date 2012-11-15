package com.vercer.leaf;

import org.junit.Test;

import junit.framework.Assert;

public class PageTest
{
	@Test
	public void decorated()
	{
		Leaf.set(new TestJet());
		
		TestPage page = new TestPage();
		Markup markup = page.getMarkup();
		Renderer renderer = new Renderer(true, true);
		String rendered = renderer.render(markup);
		
		String expected = "\n" + 
				"<html>\n" + 
				"	<head>\n" + 
				"		<hello>hi</hello>\n" + 
				"	\n" + 
				"		<there></there>\n" + 
				"	</head>\n" + 
				"	<body>\n" + 
				"		In the base page\n" + 
				"		<div>cow</div>\n" + 
				"		\n" + 
				"	\n" + 
				"		and how:\n" + 
				"		<div>moo</div>\n" + 
				"		not quite it...\n" + 
				"	\n" + 
				"		\n" + 
				"		and that is it\n" + 
				"	</body>\n" + 
				"</html>";
		
		Assert.assertEquals(expected, rendered);
	}
	
	@Test
	public void twiceDecorated()
	{
		Leaf.set(new TestJet());
		
		ThreeDeepTestPage page = new ThreeDeepTestPage();
		Markup markup = page.getMarkup();
		Renderer renderer = new Renderer(true, true);
		String rendered = renderer.render(markup);
		
		String expected = "\n" + 
				"<html>\n" + 
				"	<head>\n" + 
				"		<hello>hi</hello>\n" + 
				"	\n" + 
				"		<there></there>\n" + 
				"	\n" + 
				"		<jolly></jolly>\n" + 
				"	</head>\n" + 
				"	<body>\n" + 
				"		In the base page\n" + 
				"		<div>cow</div>\n" + 
				"		\n" + 
				"	\n" + 
				"		and how:\n" + 
				"		<div>bark</div>\n" + 
				"		not quite it...\n" + 
				"		\n" + 
				"	\n" + 
				"		this should be top of all\n" + 
				"		and this bottom at <span>noon</span>\n" + 
				"	\n" + 
				"	\n" + 
				"		\n" + 
				"		and that is it\n" + 
				"	</body>\n" + 
				"</html>";
		
		Assert.assertEquals(expected, rendered);
	}
}
