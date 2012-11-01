package com.vercer.jet.transform;

import com.google.inject.Provider;
import com.vercer.jet.Jet;
import com.vercer.jet.Markup;

public class Label extends Component<Object>
{
	private boolean html;

	public Label(String value)
	{
		super(value);
	}

	public Label(String value, boolean html)
	{
		super(value);
		this.html = html;
	}

	public Label(Provider<?> provider)
	{
		super(provider);
	}

	@Override
	public Markup transformComponent(Markup markup)
	{
		String string = get().toString();
		String html = markup.attribute(Jet.get().getSettings().getPrefix() + ":html");
		if (!this.html && !Boolean.valueOf(html))
		{
			string = escapeHTML(string);
		}
		return Markup.builder(markup).clear().content(string).build();
	}

	public static String escapeHTML(String string)
	{
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
