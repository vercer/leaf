package com.vercer.leaf.exchange;

import lombok.Getter;

import com.google.inject.Provider;
import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;

public abstract class Loop<T> extends Container<T>
{
	private static final String LOOP_TAG = Leaf.get().getSettings().getPrefix() + ":loop";
	@Getter
	private int iteration;

	private Exchanger chained;

	public Loop()
	{
		super();
	}

	public <C extends Exchanger> C chainLoop(C transformer)
	{
		this.chained = transformer;
		return transformer;
	}

	public Loop(Provider<? extends T> provider)
	{
		super(provider);
	}

	public Loop(T value)
	{
		super(value);
	}

	@Override
	protected final Markup transformComponent(Markup markup)
	{
		// put the prelude before the new container so it only shows once
		Builder parent = Markup.builder().tag(LOOP_TAG).prelude(markup.getPrelude());

		// remove the prelude from the markup to stop it repeating
		markup = Markup.builder(markup).prelude("").build();

		// repeat the markup
		for(iteration = 0; loop(); iteration++)
		{
			// make a copy of markup so loops do not interfere
			Markup transformed = transformLoop(Markup.builder(markup).build());

			if (chained != null)
			{
				transformed = chained.exchange(transformed);
			}

			parent.child(transformed);
		}

		return parent.build();
	}

	protected Markup transformLoop(Markup markup)
	{
		return transformContainer(markup);
	}
	
	protected abstract boolean loop();
}
