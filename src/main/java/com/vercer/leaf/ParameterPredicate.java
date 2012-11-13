package com.vercer.leaf;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Predicate;

public class ParameterPredicate implements Predicate<HttpServletRequest>
{
	private final String name;
	private final String value;

	public ParameterPredicate(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	@Override
	public boolean apply(HttpServletRequest arg0)
	{
		String parameter = arg0.getParameter(name);
		return parameter != null && parameter.equals(value);
	}
}
