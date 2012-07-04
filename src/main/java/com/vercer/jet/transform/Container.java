package com.vercer.jet.transform;

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
import com.google.inject.Provider;
import com.vercer.convert.TypeConverter;
import com.vercer.jet.Jet;
import com.vercer.jet.Markup;
import com.vercer.jet.Markup.Builder;

public class Container<T> extends Component<T>
{
	@Inject
	private static Set<Transformer> globals = new HashSet<Transformer>();

	public static void addGlobalTransformer(Transformer transformer)
	{
		globals.add(transformer);
	}

	public Container()
	{
	}

	public Container(Provider<? extends T> provider)
	{
		super(provider);
	}

	public Container(T value)
	{
		super(value);
	}

	@Override
	protected Markup transformComponent(Markup markup)
	{
		return transformContainer(markup);
	}

	protected final Markup transformContainer(Markup markup)
	{
		Builder builder = Markup.builder(markup);

		// clear all children and add the modified ones we want
		builder.clear();

		// add all transformed children
		for (Markup child : markup.getChildren())
		{
			for (Transformer global : globals)
			{
				child = global.transform(child);
			}

			child = transformChild(child);

			// null results are not added (removed)
			if (child != null)
			{
				builder.child(child);
			}
		}

		return builder.build();
	}

	protected Markup transformChild(Markup child)
	{
		// TODO change id to field
		// find child transformers by id and remove attribute
		String childMarkupId = child.getAttributes().get(Jet.get().getSettings().getPrefix() + ":id");

		// we cannot process this markup but another transformer may
		if (childMarkupId == null) return child;

		try
		{
			Transformer transformer = transformer(childMarkupId, child);

			return transformer.transform(child);
		}
		catch (Throwable t)
		{
			throw new TransformException("Problem transforming " + childMarkupId, child, t);
		}
	}

	private Transformer transformer(String id, Markup markup)
	{
		Map<String, Method> nameToMethod = getAccessibleReadMethods(this.getClass());
		Method getter = nameToMethod.get(id);

		// ignore markup without an id
		if (getter == null)
		{
			throw new TransformException("No transformer for " + id, markup);
		}

		try
		{
			Object object = getter.invoke(this);
			Transformer transformer;
			if (object == null)
			{
				transformer = Transformer.REMOVE;
			}
			else
			{
				TypeConverter converter = Jet.get().getConverter();
				transformer = converter.convert(object, getter.getReturnType(), Transformer.class);
				if (transformer == null)
				{
					throw new TransformException("Could not convert " + object + " to Transformer for " + id, markup);
				}
			}

			return transformer;
		}
		catch (Exception e)
		{
			if (e instanceof RuntimeException)
			{
				throw (RuntimeException) e;
			}
			else
			{
				throw new RuntimeException(e);
			}
		}

	}

	private static final Map<Class<?>, Map<String, Method>> classToNameToMethod = Maps.newConcurrentMap();
	public static Map<String, Method> getAccessibleReadMethods(Class<?> clazz)
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
				readMethod.setAccessible(true);
				nameToMethod.put(descriptor.getName(), readMethod);
			}

			classToNameToMethod.put(clazz, nameToMethod);
		}
		return nameToMethod;
	}

}
