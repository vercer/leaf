package com.vercer.leaf.exchange;

import com.vercer.leaf.Markup;

public abstract class Component implements Exchanger
{
	private Exchanger chained;

	public final void chain(Exchanger chained)
	{
		this.chained = chained;
	}
	
	public final void chain(Exchanger...components)
	{
		this.chained = new CompositeExchanger(components);
	}

	@Override
	public final Markup exchange(Markup markup)
	{
		if (isRemoved())
		{
			return Exchanger.REMOVE.exchange(markup);
		}
		else
		{
			// make sure the after hook is called even on throwing
			try
			{
				onBeforeComponent();
				markup = exchangeComponentAndChained(markup);
			}
			finally
			{
				onAfterComponent();
			}
		}

		return markup;
	}

	/**
	 * Allow loop to override to call multiple times
	 */
	protected Markup exchangeComponentAndChained(Markup markup)
	{
		markup = exchangeComponent(markup);
		if (chained != null)
		{
			markup = chained.exchange(markup);
		}
		return markup;
	}

	protected void onAfterComponent()
	{
	}

	protected void onBeforeComponent()
	{
	}

	protected abstract Markup exchangeComponent(Markup markup);

	protected boolean isRemoved()
	{
		return false;
	}
}
