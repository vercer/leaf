package com.vercer.jet.utility;

import java.net.URI;
import java.util.Collection;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.vercer.jet.annotation.Handle;
import com.vercer.jet.annotation.Handle.Http;
import com.vercer.jet.transform.Anchor;

public class Search
{
	private Handle.Http method;
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
	
	public Handle.Http getMethod()
	{
		return method;
	}
}
