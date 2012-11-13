package com.vercer.leaf;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Predicate;
import com.vercer.leaf.annotation.Handle.Http;

public class HttpMethodPredicate implements Predicate<HttpServletRequest>
{
	private final Http http;

	public HttpMethodPredicate(Http http)
	{
		this.http = http;
	}

	@Override
	public boolean apply(HttpServletRequest arg0)
	{
		return arg0.getMethod().toUpperCase().equals(http.name().toUpperCase());
	}
}
