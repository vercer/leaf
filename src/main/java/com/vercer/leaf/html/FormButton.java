package com.vercer.leaf.html;

import java.net.URI;

import com.vercer.leaf.Markup;
import com.vercer.leaf.annotation.Discriminator;
import com.vercer.leaf.exchange.Component;

public class FormButton extends Component
{
	private String action;
	private URI uri;

	public FormButton(String action, URI uri)
	{
		this.action = action;
		this.uri = uri;
	}

	@Override
	protected Markup exchangeComponent(Markup markup)
	{
		Markup input = Markup.builder()
				.tag("input")
				.attribute("type", "hidden")
				.attribute("name", Discriminator.PARAMETER)
				.attribute("value", action)
				.build();
		
		Markup form = Markup.builder()
				.tag("form")
				.attribute("method", "post")
				.attribute("action", uri.toASCIIString())
				.child(input)
				.child(markup)
				.build();
		
		return form;
	}
}
