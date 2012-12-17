package com.vercer.leaf.exchange;

import java.util.Iterator;

import com.google.inject.Provider;

public abstract class Repeat<T> extends Loop<Iterator<T>>
{
	private T item;
	private Iterator<T> iterator;

	public Repeat(Iterable<T> iterable)
	{
		iterator = iterable.iterator();
	}

	public Repeat(Iterator<T> iterator)
	{
		this.iterator = iterator;
	}

	public T getItem()
	{
		return this.item;
	}

	public Provider<T> getItemProvider()
	{
		return new Provider<T>()
		{
			@Override
			public T get()
			{
				return item;
			}
		};
	}

	protected abstract void populate(T item);

	@Override
	protected boolean loop()
	{
		// remove all children to repopulate
		if (iterator.hasNext())
		{
			this.item = iterator.next();
			populate(item);
			return true;
		}
		return false;
	}
}
