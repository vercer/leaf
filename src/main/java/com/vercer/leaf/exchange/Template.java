package com.vercer.leaf.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import lombok.extern.java.Log;

import com.google.common.collect.MapMaker;
import com.google.common.io.CharStreams;
import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Parser;

@Log
public abstract class Template extends Container<Void> implements Markup.Source
{
	private static final Map<Class<?>, Markup> typeToTemplate =
		new MapMaker()
			.concurrencyLevel(4)
			.makeMap();

	private static final Markup NO_MARKUP = Markup.builder().build();

	private Parser parser;

	public final Markup getMarkup()
	{
		return exchange(null);
	}
	
	public Template()
	{
	}

	@SuppressWarnings("unchecked")
	protected Markup getTemplate()
	{
		Class<? extends Template> type = getClass();
		do
		{
			Markup template = template(type);
			if (template != null) return template;
			type = (Class<? extends Template>) type.getSuperclass();
		}
		while (type != Template.class);

		throw new IllegalStateException("Could not find html template for " + getClass().getName());
	}

	@Override
	protected final Markup transformComponent(Markup markup)
	{
		Markup template = getTemplate();
		Markup transformed = transformTemplate(template);

		if (markup == null)
		{
			return transformed;
		}
		else
		{
			return Markup.builder(transformed).tag(markup.getTag()).prelude(markup.getPrelude()).attributes(markup.getAttributes()).build();
		}
	}

	protected Markup transformTemplate(Markup markup)
	{
		return super.transformComponent(markup);
	}

	protected Markup template(Class<? extends Template> type)
	{
		Markup template = typeToTemplate.get(type);

		boolean reload = Leaf.get().getSettings().isReload();

		if (template == NO_MARKUP)
		{
			return null;
		}
		else if (template != null && !reload)
		{
			return template;
		}

		// do not all load the template at once
		synchronized (type)
		{
			// just check in case another loaded it already while we blocked
			template = typeToTemplate.get(type);
			if (template == NO_MARKUP)
			{
				return null;
			}
			else if (template != null && !reload)
			{
				log.warning("Another request loaded template " + type);
				return template;
			}

			log.info("Loading template " + type + ". Reload " + reload);

			String text = load(type);
			if (text == null)
			{
				typeToTemplate.put(type, NO_MARKUP);
				return null;
			}

			Parser parser = getParser();
			template = parser.parse(text);

			typeToTemplate.put(type, template);
		}

		return template;
	}

	public static String load(Class<? extends Template> type)
	{
		com.vercer.leaf.annotation.Template annotation = type.getAnnotation(com.vercer.leaf.annotation.Template.class);
		String name;
		if (annotation != null)
		{
			name = annotation.value();
		}
		else
		{
			name = type.getSimpleName() + ".html";
		}
		
		InputStream stream = type.getResourceAsStream(name);
		if (stream == null)
		{
			return null;
		}

		String text;
		try
		{
			text = CharStreams.toString(new InputStreamReader(stream));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		try
		{
			stream.close();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		// remove windows carriage returns
		text = text.replaceAll("\r", "");

		return text;
	}

	private final Parser getParser()
	{
		if (parser == null)
		{
			parser = createParser();
		}
		return parser;
	}

	protected Parser createParser()
	{
		return new Parser();
	}

}
