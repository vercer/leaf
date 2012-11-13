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
import com.google.inject.Provider;
import com.vercer.convert.TypeConverter;
import com.vercer.leaf.Leaf;
import com.vercer.leaf.Markup;
import com.vercer.leaf.Markup.Builder;

public class Container<T> extends Component<T>
{
	private static final String CHILD_NAME_ATTRIBUTE = ":id";
	
	@Inject	private static Set<Exchanger> globals = new HashSet<Exchanger>();

	// call during single threaded initialisation
	public static void addGlobalTransformer(Exchanger transformer)
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
		builder.abandon();

		// add all transformed children
		for (Markup child : markup.getChildren())
		{
			for (Exchanger global : globals)
			{
				child = global.exchange(child);
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
		String childMarkupId = child.getAttributes().get(Leaf.get().getSettings().getPrefix() + CHILD_NAME_ATTRIBUTE);

		// we cannot process this markup but another transformer may
		if (childMarkupId == null) return child;

		try
		{
			Exchanger transformer = transformer(childMarkupId, child);

			return transformer.exchange(child);
		}
		catch (Throwable t)
		{
			throw new TransformException("Problem transforming " + childMarkupId, child, t);
		}
	}

	private Exchanger transformer(String id, Markup markup)
	{
		Map<String, Method> nameToMethod = getAccessibleReadMethods(this.getClass());
		Method getter = nameToMethod.get(id);

		if (getter == null)
		{
			throw new TransformException("No transformer for " + id, markup);
		}

		try
		{
			Object object = getter.invoke(this);
			TypeConverter converter = Leaf.get().getConverter();
			Exchanger transformer = converter.convert(object, getter.getReturnType(), Exchanger.class);
			if (transformer == null)
			{
				return Exchanger.REMOVE;
			}
			else
			{
				return transformer;
			}
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
