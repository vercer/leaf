package com.vercer.jet.transform;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.vercer.jet.Markup;

public abstract class Component<T> implements Transformer, Provider<T>
{
	private final Provider<? extends T> provider;
	private Transformer chained;

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

	public Component<?> chain(Component<?> chained)
	{
		if (this.chained != null)
		{
			throw new IllegalStateException("Chained component already set");
		}
		this.chained = chained;
		return this;
	}

	@Override
	public final Markup transform(Markup markup)
	{
		if (isRemoved())
		{
			return Transformer.REMOVE.transform(markup);
		}
		else
		{
			// make sure the after hook is called even on throwing
			try
			{
				beforeTransformComponent();
				markup = transformComponent(markup);
				if (chained != null)
				{
					markup = chained.transform(markup);
				}
			}
			finally
			{
				afterTransformComponent();
			}
		}

		return markup;
	}

	protected void afterTransformComponent()
	{
	}

	protected void beforeTransformComponent()
	{
	}

	protected Markup transformComponent(Markup markup)
	{
		return markup;
	}

	protected boolean isRemoved()
	{
		return false;
	}
}
