package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public abstract class DelegatingContainer extends Container<Void>
{
	private Container<?> delegate;
	
	public DelegatingContainer(Container<?> delegate)
	{
		this.delegate = delegate;
	}
	
	@Override
	protected Markup transformChild(Markup child)
	{
		return delegate.transformChild(child);
	}
}
