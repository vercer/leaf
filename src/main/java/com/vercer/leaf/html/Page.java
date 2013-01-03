package com.vercer.leaf.html;

import java.util.List;

import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;
import com.vercer.leaf.Parser;
import com.vercer.leaf.exchange.Decorator;
import com.vercer.leaf.exchange.Exchanger;

public abstract class Page extends Decorator
{
	// the current top level page - other pages may be contained
	private static final ThreadLocal<Page> outer = new ThreadLocal<Page>();

	private final static ThreadLocal<Page> current = new ThreadLocal<Page>();
	
	// keep reference to the head so we can write to it later
	private Head head;

	private Builder bodyBuilder;

	/**
	 * @return The top level page
	 */
	public static Page getOuterPage()
	{
		return outer.get();
	}

	/**
	 * @return The current page being exchanged
	 */
	public static Page getCurrentPage()
	{
		return current.get();
	}
	
	private long id;
	public String createUniqueId()
	{
		return Long.toString(id++, Character.MAX_RADIX);
	}

	/**
	 * We need to treat head and body tags as special markup
	 */
	@Override
	protected Parser createParser()
	{
		return new Parser()
		{
			@Override
			protected boolean isSpecialTag(Markup markup)
			{
				return super.isSpecialTag(markup) ||
					markup.getTag().equals("body") ||
					markup.getTag().equals("head");
			}
		};
	}

	@Override
	protected final void onBeforeComponent()
	{
		// see if there is another top level page that contains us
		Page top = outer.get();
		if (top == null)
		{
			// this is the top level page so remember it
			outer.set(this);
		}
	}

	@Override
	protected final Markup exchangeTemplate(Markup markup)
	{
		// must be decorated markup and we have already transformed the decorator
		if (!"html".equals(markup.getTag()))
		{
			throw new ExchangeException("Outer decorator page markup must be html", markup);
		}

		try
		{
			current.set(this);

			Markup transformed = exchangePage(markup);
			
			onAfterPage();
			
			// see if we are processing the outer page
			if (getOuterTemplateClass() == getCurrentTemplateClass() && getOuterPage() == this)
			{
				onAfterOuterPage();

				// outer page returns all html
				return transformed;
			}
			else
			{
				// contained page returns only the body
				return bodyBuilder.tag(Leaf.get().getSettings().getPrefix() + ":body").build();
			}
		}
		finally
		{
			current.set(null);
		}
	}

	/**
	 * Called after out page and contained pages exchanged.
	 */
	protected void onAfterPage()
	{
	}
	
	/**
	 * Called only once after the outer page is exchanged. 
	 */
	protected void onAfterOuterPage()
	{
	}

	protected Markup exchangePage(Markup markup)
	{
		return exchangeContainer(markup);
	}

	@Override
	protected final void onAfterComponent()
	{
		if (outer.get() == this)
		{
			// clean up after ourselves
			outer.set(null);
		}
		afterExchangePage();
	}

	protected void afterExchangePage()
	{
	}

	public Head getHead()
	{
		return head;
	}

	public Builder getBodyBuilder()
	{
		return this.bodyBuilder;
	}

	@Override
	protected final Markup exchangeChild(Markup child)
	{
		if ("head".equals(child.getTag()))
		{
			if (head == null)
			{
				Page top = outer.get();
				if (top == this)
				{
					head = new Head();
				}
				else
				{
					head = top.head;
				}
			}

			// process the head markup against the page
			Markup transformed = exchangeContainer(child);

			return head.exchange(transformed);
		}
		else if ("body".equals(child.getTag()))
		{
			// TODO like head, allow contained pages to contribute body attributes
			Markup bodyMarkup = exchangeBody(child);

			// copy any body tag attributes to the new body tag
			Markup existingBodyMarkup = bodyBuilder == null ? null : bodyBuilder.build();

			bodyBuilder = Markup.builder(bodyMarkup);

			if (existingBodyMarkup != null && !existingBodyMarkup.getAttributes().isEmpty())
			{
				bodyBuilder.attributes(existingBodyMarkup.getAttributes());
			}

			return bodyBuilder.build();
		}
		else 
		{
			// this method is also called for children of head and body
			return exchangePageChild(child);
		}
	}

	protected Markup exchangePageChild(Markup child)
	{
		return super.exchangeChild(child);
	}

	protected Markup exchangeBody(Markup child)
	{
		// use this page to get fields
		return this.exchangeContainer(child);
	}

	public class Head implements Exchanger
	{
		private Builder builder;

		@Override
		public Markup exchange(Markup markup)
		{
			// do we already have some head content
			if (builder == null)
			{
				// create a builder so we can change markup later
				builder = Markup.builder(markup);

				// return the markup but we can still add to it
				return builder.build();
			}
			else
			{
				// add the contained head content after the current head content
				builder.content(builder.build().getContent() + markup.getContent());

				// add all the contained pages head markup to the current page head
				List<Markup> children = markup.getChildren();
				for (Markup child : children)
				{
					// we can still modify the head markup because we have its builder
					builder.child(child);
				}

				// the contained head tag will be ignored
				return null;
			}
		}

		public Builder getBuilder()
		{
			return this.builder;
		}
	}
}
