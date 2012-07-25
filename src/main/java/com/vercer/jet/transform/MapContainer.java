package com.vercer.jet.transform;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import lombok.Getter;

public class MapContainer extends Repeat<Map.Entry<?, ?>>
{
	@Getter
	String name;

	@Getter
	String value;

	@Override
	protected void populate(Map.Entry<?, ?> entry)
	{
		name = entry.getKey().toString();
		value = entry.getValue().toString();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MapContainer(Map<?, ?> map)
	{
		super(((Map) map).entrySet());
	}
	
	public MapContainer(Collection<Entry<?, ?>> entries)
	{
		super(entries);
	}
}
