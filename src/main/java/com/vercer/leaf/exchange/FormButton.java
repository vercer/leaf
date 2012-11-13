package com.vercer.leaf.exchange;

import java.net.URI;

import com.vercer.leaf.Markup;
import com.vercer.leaf.annotation.Discriminator;

public class FormButton extends Component<URI>
{
	private String action;

	public FormButton(String action, URI uri)
	{
		super(uri);
		this.action = action;
	}

	@Override
	protected Markup transformComponent(Markup markup)
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
				.attribute("action", get().toASCIIString())
				.child(input)
				.child(markup)
				.build();
		
		return form;
	}
}
