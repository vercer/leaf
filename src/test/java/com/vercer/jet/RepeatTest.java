package com.vercer.jet;

import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;
import lombok.Getter;

import org.junit.Test;

import com.vercer.jet.transform.Label;
import com.vercer.jet.transform.Repeat;
import com.vercer.jet.transform.Template;
import com.vercer.jet.transform.Transformer;

public class RepeatTest extends Template
{
	private List<String> dayNames;

	@Test
	public void render()
	{
		dayNames = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday");

		Jet.set(new TestJet());

		Markup markup = getMarkup();

		Renderer renderer = new Renderer(true, true);
		String rendered = renderer.render(markup);

		Assert.assertEquals(rendered, "<html>\n" +
				"	<body>\n" +
				"		<div class=\"asdf asdff asdfdd\">\n" +
				"			&&& these should just go straight through &&&\n" +
				"		</div>\n" +
				"		\n" +
				"		<div>\n" +
				"			A day name is <span>Monday</span>\n" +
				"		</div><div>\n" +
				"			A day name is <span>Tuesday</span>\n" +
				"		</div><div>\n" +
				"			A day name is <span>Wednesday</span>\n" +
				"		</div><div>\n" +
				"			A day name is <span>Thursday</span>\n" +
				"		</div>\n" +
				"		\n" +
				"		This is the replacement\n" +
				"	</body>\n" +
				"</html>");
	}

	public Transformer getDays()
	{
		return new Repeat<String>(dayNames)
		{
			@Getter
			Label day = new Label(this);

			@Override
			protected void populate(String item)
			{
			}
		};
	}

	public Label getHello()
	{
		return new Label("This is the replacement");
	}
}
