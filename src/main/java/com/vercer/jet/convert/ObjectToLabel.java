package com.vercer.jet.convert;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vercer.convert.Converter;
import com.vercer.convert.TypeConverter;
import com.vercer.jet.transform.Label;

@Singleton
public class ObjectToLabel implements Converter<Object, Label>
{
	@Inject private
	TypeConverter converter;
	
	@Override
	public Label convert(Object source)
	{
		return new Label(converter.<String>convert(source, String.class));
	}
}
