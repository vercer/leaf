package com.vercer.leaf.exchange;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import lombok.extern.java.Log;

import com.google.inject.Provider;

@Log
public class FutureProvider<T> implements Provider<T>
{
	private final Future<? extends T> future;
	private final long timeout;

	public FutureProvider(Future<? extends T> future, long timeout)
	{
		this.future = future;
		this.timeout = timeout;
	}

	@Override
	public T get()
	{
		try
		{
			long start = System.currentTimeMillis();
			T result = future.get(timeout, TimeUnit.MILLISECONDS);
			log.info(System.currentTimeMillis() - start + "ms");
			return result;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
