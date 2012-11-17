package com.vercer.leaf.utility;

import java.util.Iterator;

import com.google.inject.Provider;

public class IterableToIteratorProvider<T> implements Provider<Iterator<T>>
{
	private final Provider<? extends Iterable<T>> iterable;
	public IterableToIteratorProvider(Provider<? extends Iterable<T>> iterable)
	{
		this.iterable = iterable;
	}
	
	@Override
	public Iterator<T> get()
	{
		return iterable.get().iterator();
	}
}
