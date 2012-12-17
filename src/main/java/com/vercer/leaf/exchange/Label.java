package com.vercer.leaf.exchange;

import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;

public class Label extends Component
{
	private boolean html;
	private CharSequence value;

	public Label(CharSequence value)
	{
		this(value, false);
	}

	public Label(CharSequence value, boolean html)
	{
		this.value = value;
		this.html = html;
	}

	@Override
	public Markup exchangeComponent(Markup markup)
	{
		String html = markup.getAttribute(Leaf.get().getSettings().getPrefix() + ":html");
		String string = value.toString();
		if (!this.html && !Boolean.valueOf(html))
		{
			string = escapeHTML(string);
		}
		return Markup.builder(markup).abandon().content(string).build();
	}

	public static String escapeHTML(String string)
	{
		if (string == null) return null;
		string = string
				.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\n", "<br/>\n");
		return string;
	}

	public void setHtml(boolean html)
	{
		this.html = html;
	}

	public boolean isHtml()
	{
		return this.html;
	}
}
