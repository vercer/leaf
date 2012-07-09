package com.vercer.jet;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class Server<S extends Session<?>>
{
	@Inject
	private Provider<S> sessions;
	
	public S getSession()
	{
		return sessions.get();
	}
}
