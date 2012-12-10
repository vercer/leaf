package com.vercer.leaf.convert;

import javax.servlet.http.Cookie;

import com.vercer.convert.Converter;

public class CookieToString implements Converter<Cookie, String>
{
	@Override
	public String convert(Cookie source)
	{
		return source.getValue();
	}
}
