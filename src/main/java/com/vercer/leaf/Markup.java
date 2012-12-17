package com.vercer.leaf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Almost immutable. You can continue to mutate using its Builder
 * even after build() is called. This is handy to check the state
 * of the Markup because the builder does not have getters.
 *
 * @author John Patterson (john@vercer.com)
 *
 */
@EqualsAndHashCode @Getter
public class Markup implements Cloneable
{
	private String prelude = "";
	private String content = "";
	private String tag;
	private Map<String, String> attributes;
	private List<Markup> children;
	private boolean open;
	private boolean special;

	protected Markup()
	{
	}

	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		Markup clone = (Markup) super.clone();
		if (clone.children != null)
		{
			// set an immutable copy
			clone.children = clone.getChildren();
		}
		if (clone.attributes != null)
		{
			// set an immutable copy
			clone.attributes = clone.getAttributes();
		}
		return clone;
	}

	/**
	 * @author John Patterson (john@vercer.com)
	 *
	 */
	public static class Builder
	{
		private final Markup markup;

		private Builder(Markup markup)
		{
			try
			{
				this.markup = (Markup) markup.clone();
			}
			catch (CloneNotSupportedException e)
			{
				throw new AssertionError();
			}
		}

		public Markup build()
		{
			return markup;
		}

		public Builder tag(String tag)
		{
			markup.tag = tag.toLowerCase();
			return this;
		}

		public Builder prelude(String prelude)
		{
			markup.prelude = prelude;
			return this;
		}

		public Builder content(String content)
		{
			markup.open = true;
			markup.content = content;
			return this;
		}

		public Builder open(boolean open)
		{
			markup.open = open;
			return this;
		}

		public Builder attribute(String name, String value)
		{
			ensureAttributes();

			if (value == null)
			{
				markup.attributes.remove(name.toLowerCase());
			}
			else
			{
				markup.attributes.put(name.toLowerCase(), value);
			}

			return this;
		}

		private void ensureAttributes()
		{
			if (markup.attributes == null)
			{
				markup.attributes = new LinkedHashMap<String, String>(4);
			}
			else if (markup.attributes instanceof LinkedHashMap == false)
			{
				// replace immutable map from clone
				markup.attributes = new LinkedHashMap<String, String>(markup.attributes);
			}
		}

		public Builder attributes(Map<String, String> attributes)
		{
			if (markup.attributes == null || markup.attributes.isEmpty())
			{
				markup.attributes = attributes;
			}
			else
			{
				ensureAttributes();
				markup.attributes.putAll(attributes);
			}
			return this;
		}

		public Builder removeAttribute(String name)
		{
			ensureAttributes();
			markup.attributes.remove(name);
			return this;
		}

		public Builder child(Markup child)
		{
			// automatically open closed tags
			markup.open = true;

			if (markup.children == null)
			{
				markup.children = new ArrayList<Markup>(4);
			}
			else if (markup.children instanceof ArrayList == false)
			{
				// replace immutable list from clone
				markup.children = new ArrayList<Markup>(markup.children);
			}
			markup.children.add(child);
			return this;
		}

		public Builder remomveChild(Markup child)
		{
			markup.children.remove(child);
			return this;
		}

		public Builder abandon()
		{
			if (markup.children != null)
			{
				if (markup.children instanceof ArrayList)
				{
					markup.children.clear();
				}
				else
				{
					markup.children = new ArrayList<Markup>(4);
				}
			}
			return this;
		}

		public Builder special(boolean special)
		{
			markup.special = special;
			return this;
		}

		public Builder appendAttribute(String name, String seperator, String value)
		{
			String existing = markup.getAttribute(name);
			if (existing == null)
			{
				return attribute(name, value);
			}
			else
			{
				return attribute(name, existing + seperator + value);
			}
		}

	}

	public static Builder builder()
	{
		return new Builder(new Markup());
	}

	public static Builder builder(Markup markup)
	{
		return new Builder(markup);
	}

	public List<Markup> getChildren()
	{
		if (children == null)
		{
			return Collections.emptyList();
		}
		else
		{
			return Collections.unmodifiableList(this.children);
		}
	}

	public Map<String, String> getAttributes()
	{
		if (attributes == null)
		{
			return Collections.emptyMap();
		}
		else
		{
			return Collections.unmodifiableMap(this.attributes);
		}
	}
	
	public String getAttribute(String name)
	{
		return attributes == null ? null : attributes.get(name);
	}
	
	public String getSpecialAttribute(String name)
	{
		return getAttribute(Leaf.get().getSettings().getPrefix() + ":" + name);
	}

	public interface Source
	{
		Markup getMarkup();
	}

	public String toString()
	{
		return new Renderer(false, false).render(this);
	}
}
