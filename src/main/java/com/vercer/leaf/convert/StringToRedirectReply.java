package com.vercer.leaf.convert;

import com.vercer.convert.Converter;
import com.vercer.leaf.Reply;

public class StringToRedirectReply implements Converter<String, Reply>
{
	@Override
	public Reply convert(final String input)
	{
		return Reply.withRedirect(input);
	}
}
