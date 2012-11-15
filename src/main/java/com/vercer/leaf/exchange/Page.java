package com.vercer.leaf.exchange;

import java.util.List;

import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;
import com.vercer.leaf.Parser;

public abstract class Page extends Decorator
{
	// the current top level page - other pages may be contained
	private static final ThreadLocal<Page> pages = new ThreadLocal<Page>();

	// keep reference to the head so we can write to it later
	private Head head;

	private Builder bodyBuilder;

	/**
	 * @return The current top level page
	 */
	public static Page getOuterPage()
	{
		return pages.get();
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
	protected final void beforeTransformComponent()
	{
		// see if there is another top level page that contains us
		Page top = pages.get();
		if (top == null)
		{
			// this is the top level page so remember it
			pages.set(this);
		}
	}

	@Override
	protected final Markup transformTemplate(Markup markup)
	{
		// must be decorated markup and we have already transformed the decorator
		if (!"html".equals(markup.getTag()))
		{
			// head must exist if we are an inner page
			assert head != null : "Outer decorator page markup must be html";

			// return the whole page which has no body or head
			return super.exchangeContainer(markup);
		}

		Markup transformed = transformPage(markup);

		// see if we are processing the outer page
		if (getOuterTemplateClass() == getCurrentTemplateClass() && getOuterPage() == this)
		{
			// outer page returns all html
			return transformed;
		}
		else
		{
			// contained page returns only the body
			return bodyBuilder.tag(Leaf.get().getSettings().getPrefix() + ":body").build();
//			return bodyBuilder.build();
		}
	}

	protected Markup transformPage(Markup markup)
	{
		return super.exchangeContainer(markup);
	}

	@Override
	protected final void afterTransformComponent()
	{
		if (pages.get() == this)
		{
			// clean up after ourselves
			pages.set(null);
		}
		afterTransformPage();
	}

	protected void afterTransformPage()
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
	protected final Markup transformChild(Markup child)
	{
		if ("head".equals(child.getTag()))
		{
			if (head == null)
			{
				Page top = pages.get();
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
			Markup transformed = this.exchangeContainer(child);

			return head.exchange(transformed);
		}
		else if ("body".equals(child.getTag()))
		{
			// TODO like head, allow contained pages to contribute body attributes
			Markup bodyMarkup = transformBody(child);

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
			return super.transformChild(child);
		}
	}

	protected Markup transformBody(Markup child)
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
