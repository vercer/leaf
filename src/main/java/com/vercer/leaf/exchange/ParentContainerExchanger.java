package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;
import com.vercer.leaf.exchange.Container.NeedsParent;

/**
 * @author John Patterson (john@vercer.com)
 */
public class ParentContainerExchanger extends Container<Void> implements NeedsParent
{
	private Container<?> parent;

	@Override
	public void setParent(Container<?> parent)
	{
		this.parent = parent;
	}
	
	@Override
	protected Markup exchangeChild(Markup child)
	{
		return parent.exchangeChild(child);
	}
}
