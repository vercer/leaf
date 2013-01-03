package com.vercer.leaf.exchange;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.vercer.convert.TypeConverter;
import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;

public abstract class Container extends Component
{
	private static final String PROPERTY_ATTRIBUTE = ":x";
	
	public interface NeedsParent
	{
		void setParent(Container parent);
	}
	
	@Inject	private static Set<Exchanger> globals = new HashSet<Exchanger>();
	
	// call during single threaded initialization
	public static void addGlobalTransformer(Exchanger transformer)
	{
		globals.add(transformer);
	}

	public Container()
	{
	}
	
	@Override
	protected Markup exchangeComponent(Markup markup)
	{
		return exchangeContainer(markup);
	}

	protected final Markup exchangeContainer(Markup markup)
	{
		Builder builder = Markup.builder(markup);

		// clear all children and add the modified ones we want
		builder.abandon();

		// add all transformed children
		for (Markup child : markup.getChildren())
		{
			for (Exchanger global : globals)
			{
				child = global.exchange(child);
			}

			child = exchangeChild(child);

			// null results are not added (removed)
			if (child != null)
			{
				builder.child(child);
			}
		}

		return builder.build();
	}

	protected Markup exchangeChild(Markup child)
	{
		// find child transformers by id and remove attribute
		String childMarkupId = child.getAttributes().get(Leaf.get().getSettings().getPrefix() + PROPERTY_ATTRIBUTE);

		// we cannot process this markup but another transformer may
		if (childMarkupId == null) return child;

		try
		{
			Exchanger transformer = exchanger(childMarkupId, child);
			if (transformer instanceof NeedsParent)
			{
				((NeedsParent) transformer).setParent(this);
			}
			return transformer.exchange(child);
		}
		catch (Throwable t)
		{
			throw new ExchangeException("Problem transforming " + childMarkupId, child, t);
		}
	}

	private Exchanger exchanger(String id, Markup markup)
	{
		Map<String, Method> nameToMethod = getAccessibleReadMethods(getBean().getClass());
		Method getter = nameToMethod.get(id);

		if (getter == null)
		{
			throw new ExchangeException("No getter for " + id + " in " + getBean().getClass(), markup);
		}

		Object object;
		try
		{
			object = getter.invoke(getBean());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
		// handle all nulls the same - remove the markup
		if (object == null)
		{
			return Exchanger.REMOVE;
		}
		
		TypeConverter converter = Leaf.get().getConverter();
		Exchanger transformer = converter.convert(object, getter.getReturnType(), Exchanger.class);
		if (transformer == null)
		{
			throw new ExchangeException("No exchanger found for " + object, markup);
		}
		return transformer;
	}

	private static final Map<Class<?>, Map<String, Method>> classToNameToMethod = Maps.newConcurrentMap();
	private static Map<String, Method> getAccessibleReadMethods(Class<?> clazz)
	{
		Map<String, Method> nameToMethod = classToNameToMethod.get(clazz);

		if (nameToMethod == null)
		{
			BeanInfo info;
			try
			{
				info = Introspector.getBeanInfo(clazz);
			}
			catch (IntrospectionException e)
			{
				throw new RuntimeException(e);
			}

			PropertyDescriptor[] descriptors = info.getPropertyDescriptors();

			nameToMethod = new HashMap<String, Method>(descriptors.length);
			for (PropertyDescriptor descriptor : descriptors)
			{
				Method readMethod = descriptor.getReadMethod();
				if (readMethod != null)
				{
					readMethod.setAccessible(true);
				}
				nameToMethod.put(descriptor.getName(), readMethod);
			}

			classToNameToMethod.put(clazz, nameToMethod);
		}
		return nameToMethod;
	}

	protected Object getBean()
	{
		return this;
	}
}
