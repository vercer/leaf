package com.vercer.leaf.exchange;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import com.google.inject.Provider;

public abstract class Repeat<T> extends Loop<Iterator<T>>
{
	private T item;
	private Iterator<T> iterator;

	public Repeat(Iterable<T> iterable)
	{
		// make nulls a little less painful
		super(iterable == null ? Iterators.<T>emptyIterator() : iterable.iterator());
	}

	public Repeat(Provider<? extends Iterable<T>> provider)
	{
		super(new IterableToIteratorProvider<T>(provider));
	}

	public Repeat(Iterator<T> iterator)
	{
		super(iterator);
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
		if (iterator == null)
		{
			iterator = get();
		}

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
