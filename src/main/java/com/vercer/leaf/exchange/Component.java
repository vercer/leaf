package com.vercer.leaf.exchange;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.vercer.leaf.Markup;

public abstract class Component<T> implements Exchanger, Provider<T>
{
	private final Provider<? extends T> provider;
	private Exchanger chained;

	public Component()
	{
		provider = null;
	}

	public Component(Provider<? extends T> provider)
	{
		this.provider = provider;
	}

	public Component(T value)
	{
		this(Providers.of(value));
	}
	
	@Override
	public T get()
	{
		return provider.get();
	}

	public void chain(Exchanger chained)
	{
		this.chained = chained;
	}
	
	public void chain(Exchanger...components)
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
				beforeTransformComponent();
				markup = exchangeComponentAndChained(markup);
			}
			finally
			{
				afterTransformComponent();
			}
		}

		return markup;
	}

	protected Markup exchangeComponentAndChained(Markup markup)
	{
		markup = exchangeComponent(markup);
		if (chained != null)
		{
			markup = chained.exchange(markup);
		}
		return markup;
	}

	protected void afterTransformComponent()
	{
	}

	protected void beforeTransformComponent()
	{
	}

	protected abstract Markup exchangeComponent(Markup markup);

	protected boolean isRemoved()
	{
		return false;
	}
}
