package com.vercer.leaf.utility;

import java.net.URI;
import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vercer.leaf.annotation.Handle.Http;
import com.vercer.leaf.html.Anchor;

public class Search
{
	private Http method;
	private URI address;
	private Multimap<String, String> parameters;
	
	public Search(URI address, Http method)
	{
		this.address = address;
		this.method = method;
	}
	
	public Search parameter(String name, String value)
	{
		if (parameters == null)
		{
			parameters = ArrayListMultimap.create();
		}
		parameters.put(name, value);
		return this;
	}
	
	public String getParameterText()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(address.toASCIIString());
		for (String  parameter : parameters.keySet())
		{
			Collection<String> values = parameters.get(parameter);
			for (Object value : values)
			{
				Anchor.addNotNullParameter(builder, parameter, value);
			}
		}
		return builder.toString();
	}
	
	public Http getMethod()
	{
		return method;
	}
}
