package com.vercer.leaf.html;

import java.net.URI;
import java.util.Collection;

import com.vercer.leaf.Markup;
import com.vercer.leaf.exchange.Exchanger;


public abstract class ChangePageSelect<T> implements Exchanger
{
	private SelectOptions<T> options;

	public ChangePageSelect(Collection<T> items, T current)
	{
		options = new SelectOptions<T>(items, current)
		{
			@Override
			protected String value(T item)
			{
				return ChangePageSelect.this.link(item).toASCIIString();
			}

			@Override
			protected String text(T item)
			{
				return ChangePageSelect.this.text(item);
			}
		};
	}
	protected abstract URI link(T item);
	
	protected abstract String text(T item);
	
	@Override
	public Markup exchange(Markup markup)
	{
		Markup option = Markup.builder().tag("option").build();
		return Markup.builder(markup)
				.tag("select")
				.attribute("onchange", "location.assign(this.value)")
				.abandon()
				.child(options.exchange(option)).build();
	}
}
