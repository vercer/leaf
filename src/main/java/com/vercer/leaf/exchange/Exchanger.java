package com.vercer.leaf.exchange;

import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;


public interface Exchanger
{
	Exchanger NO_OP = new Exchanger()
	{
		@Override
		public Markup exchange(Markup markup)
		{
			return markup;
		}
	};
	Exchanger REMOVE = new Exchanger()
	{
		@Override
		public Markup exchange(Markup markup)
		{
			return Markup.builder().tag(Leaf.get().getSettings().getPrefix() + ":remove").prelude(markup.getPrelude()).build();
		}
	};
	Exchanger EMPTY = new Exchanger() 
	{
		@Override
		public Markup exchange(Markup markup)
		{
			return Markup.builder(markup).content("").abandon().build();
		}
	};

	Markup exchange(Markup markup);
	
	public class TransformException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		public TransformException(String message, Markup markup, Throwable cause)
		{
			super(build(message, markup), cause);
		}

		private static String build(String message, Markup markup)
		{
			return message += "\n\nMarkup:" + markup.toString();
		}

		public TransformException(String message, Markup markup)
		{
			this(message, markup, null);
		}
	}
	
	public class Chain implements Exchanger
	{
		private final Exchanger[] transformers;

		public Chain(Exchanger... transformers)
		{
			this.transformers = transformers;
		}
		
		@Override
		public Markup exchange(Markup markup)
		{
			for (Exchanger  transformer : transformers)
			{
				markup = transformer.exchange(markup);
			}
			return markup;
		}
	}
}
