package com.vercer.jet.transform;

import com.vercer.jet.Jet;
import com.vercer.jet.Markup;


public interface Transformer
{
	Transformer NO_OP = new Transformer()
	{
		@Override
		public Markup transform(Markup markup)
		{
			return markup;
		}
	};
	Transformer REMOVE = new Transformer()
	{
		@Override
		public Markup transform(Markup markup)
		{
			return Markup.builder().tag(Jet.get().getSettings().getPrefix() + ":remove").prelude(markup.getPrelude()).build();
		}
	};
	Transformer EMPTY = new Transformer() 
	{
		@Override
		public Markup transform(Markup markup)
		{
			return Markup.builder(markup).content("").clear().build();
		}
	};

	Markup transform(Markup markup);
	
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
	
	public class Chain implements Transformer
	{
		private final Transformer[] transformers;

		public Chain(Transformer... transformers)
		{
			this.transformers = transformers;
		}
		
		@Override
		public Markup transform(Markup markup)
		{
			for (Transformer  transformer : transformers)
			{
				markup = transformer.transform(markup);
			}
			return markup;
		}
	}
}
