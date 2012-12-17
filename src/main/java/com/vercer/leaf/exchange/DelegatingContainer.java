package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public abstract class DelegatingContainer extends Container
{
	private Container delegate;
	
	public DelegatingContainer(Container delegate)
	{
		this.delegate = delegate;
	}
	
	@Override
	protected Markup exchangeChild(Markup child)
	{
		return delegate.exchangeChild(child);
	}
}
