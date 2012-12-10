package com.vercer.leaf.exchange;

import java.util.ArrayDeque;
import java.util.Deque;

import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;

public abstract class Decorator extends Template
{
	private Deque<Class<? extends Decorator>> decorators = new ArrayDeque<Class<? extends Decorator>>(3);

	private Class<? extends Decorator> currentTemplateClass;
	private Class<? extends Decorator> outerTemplateClass;

	@Override
	@SuppressWarnings("unchecked")
	protected Markup getTemplate()
	{
		Class<? extends Decorator> current = getClass();
		while (current != Decorator.class)
		{
			decorators.push(current);
			current = (Class<? extends Decorator>) current.getSuperclass();
		}

		return nextTemplate();
	}

	private Markup nextTemplate()
	{
		// look for the next sub class that has a template file
		Markup template = null;
		do
		{
			currentTemplateClass = decorators.pop();
			template = template(currentTemplateClass);
			if (template == null && decorators.isEmpty())
			{
				throw new IllegalStateException("No template found for class " + currentTemplateClass);
			}
		}
		while(template == null);

		// remember the outside template so we know when we are finished
		if (outerTemplateClass == null)
		{
			outerTemplateClass = currentTemplateClass;
		}

		return template;
	}

	public Class<? extends Decorator> getOuterTemplateClass()
	{
		return this.outerTemplateClass;
	}

	public Class<? extends Decorator> getCurrentTemplateClass()
	{
		return this.currentTemplateClass;
	}

	public Deque<Class<? extends Decorator>> getDecorators()
	{
		return this.decorators;
	}

	@Override
	protected Markup exchangeChild(Markup markup)
	{
		if (markup.getTag().equals(Leaf.get().getSettings().getPrefix() + ":decorated"))
		{
			Class<? extends Decorator> existingDecoratorClass = currentTemplateClass;

			Markup decorated = nextTemplate();

			// skip the before and after intercepters
			decorated = transformTemplate(decorated);

			currentTemplateClass = existingDecoratorClass;

			return Markup.builder(markup).abandon().child(decorated).build();

			// add decorated markup as a child to the decorator
//			Builder builder = Markup.builder(markup).clear();
//			List<Markup> children = decorated.getChildren();
//			for (Markup child : children)
//			{
//				builder.child(child);
//			}

//			builder.content(decorated.getContent());

//			return builder.build();
		}
		else
		{
			return super.exchangeChild(markup);
		}
	}
}
