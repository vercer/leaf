package com.vercer.leaf.html;

import java.util.Collection;
import java.util.Iterator;

import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;
import com.vercer.leaf.exchange.Repeat;

public abstract class SelectOptions<T> extends Repeat<T>
{
	private final T selected;

	public SelectOptions(Collection<T> items, T selected)
	{
		super(items);
		this.selected = selected;
	}

	public SelectOptions(Iterator<T> items, T selected)
	{
		super(items);
		this.selected = selected;
	}

	@Override
	protected void populate(T item)
	{
	}

	@Override
	protected Markup exchangeLoop(Markup markup)
	{
		Builder builder = Markup.builder(markup)
				.content(text(getItem()))
				.attribute("value", value(getItem()));

		if (getItem().equals(selected))
		{
			builder.attribute("selected", "selected");
		}
		return builder.build();
	}

	protected abstract String value(T item);

	protected abstract String text(T item);
}
