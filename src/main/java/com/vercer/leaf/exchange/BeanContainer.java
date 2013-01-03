package com.vercer.leaf.exchange;

public class BeanContainer extends Container
{
	private Object bean;

	public BeanContainer(Object bean)
	{
		this.bean = bean;
	}
	
	@Override
	protected Object getBean()
	{
		return bean;
	}
}
