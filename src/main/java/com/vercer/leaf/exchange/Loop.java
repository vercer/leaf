package com.vercer.leaf.exchange;

import lombok.Getter;

import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;

public abstract class Loop<T> extends Container
{
	private static final String LOOP_TAG = Leaf.get().getSettings().getPrefix() + ":loop";
	@Getter
	private int iteration;

	public Loop()
	{
		super();
	}

	@Override
	protected final Markup exchangeComponentAndChained(Markup markup)
	{
		// put the prelude before the new container so it only shows once
		Builder parent = Markup.builder().tag(LOOP_TAG).prelude(markup.getPrelude());

		// remove the prelude from the markup to stop it repeating
		markup = Markup.builder(markup).prelude("").build();

		// repeat the markup
		for(iteration = 0; loop(); iteration++)
		{
			// make a copy of markup so loops do not interfere
			Markup transformed = exchangeLoop(Markup.builder(markup).build());

//			if (chained != null)
//			{
//				transformed = chained.exchange(transformed);
//			}

			parent.child(transformed);
		}

		return parent.build();
	}

	protected Markup exchangeLoop(Markup markup)
	{
		return super.exchangeComponentAndChained(markup);
	}
	
	protected abstract boolean loop();
}
