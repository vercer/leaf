package com.vercer.jet.convert;

import com.vercer.convert.Converter;
import com.vercer.jet.Reply;

public class StringToReply implements Converter<String, Reply>
{
	@Override
	public Reply convert(final String input)
	{
		return Reply.withRedirect(input);
	}
}
