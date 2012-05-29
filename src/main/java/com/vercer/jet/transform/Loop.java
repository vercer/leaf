package com.vercer.jet.transform;

import lombok.Getter;

import com.google.inject.Provider;
import com.vercer.jet.Jet;
import com.vercer.jet.Markup;
import com.vercer.jet.Markup.Builder;

public abstract class Loop<T> extends Container<T>
{
	private static final String LOOP_TAG = Jet.get().getSettings().getPrefix() + ":loop";
	@Getter
	private int iteration;

	private Transformer chained;

	public Loop()
	{
		super();
	}

	public <C extends Transformer> C chainLoop(C transformer)
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
			// make a copy of markup so loops do not interfer
			Markup transformed = transformLoop(Markup.builder(markup).build());

			if (chained != null)
			{
				transformed = chained.transform(transformed);
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
