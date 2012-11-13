package com.vercer.leaf;

import java.util.Collection;
import java.util.Map;

/**
 * @author John Patterson (john@vercer.com)
 *
 */
public class Renderer
{
	private final boolean removeSpecialTags;
	private final boolean removeSpecialAttributes; 
	
	/**
	 * @param removeSpecialTags Remove tags starting with e.g. "jet:"
	 * @param removeSpecialAttributes Remove attributes starting with e.g. "jet:"
	 */
	public Renderer(boolean removeSpecialTags, boolean removeSpecialAttributes)
	{
		this.removeSpecialTags = removeSpecialTags;
		this.removeSpecialAttributes = removeSpecialAttributes;
	}
	
	public String render(Markup markup)
	{
		StringBuilder builder = new StringBuilder();
		render(markup, builder);
		return builder.toString();
	}

	private void render(Markup markup, StringBuilder builder)
	{
		String prefix = Leaf.get().getSettings().getPrefix();
		builder.append(markup.getPrelude());
		
		if (!removeSpecialTags || !markup.getTag().startsWith(prefix))
		{
			builder.append('<');
			builder.append(markup.getTag());
			
			Map<String, String> attributes = markup.getAttributes();
			for (String  name : attributes.keySet())
			{
				// do not render special attributes
				if (removeSpecialAttributes && 
						(name.startsWith(prefix + ":") || 
						 name.equals("xmlns:" + prefix)))
				{
					continue;
				}
				
				builder.append(' ');
				builder.append(name);
				builder.append("=\"");
				builder.append(attributes.get(name));
				builder.append('"');
			}
			
			if (!markup.isOpen())
			{
				builder.append("/>");
				return;
			}
			
			builder.append('>');
		}
		
		Collection<Markup> children = markup.getChildren();
		for (Markup child : children)
		{
			render(child, builder);
		}
		builder.append(markup.getContent());

		if (!removeSpecialTags || !markup.getTag().startsWith(prefix))
		{
			builder.append("</");
			builder.append(markup.getTag());
			builder.append('>');
		}
	}
	
	public static class RenderException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	
		public RenderException(String message, StringBuilder builder)
		{
			super(message + " at:\n\n" + builder.toString());
		}
	}
}
