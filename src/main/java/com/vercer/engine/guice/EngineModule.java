package com.vercer.engine.guice;

import com.google.appengine.api.quota.QuotaService;
import com.google.appengine.api.quota.QuotaServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty.Environment;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.vercer.jet.Configuration;
import com.vercer.jet.Settings;

public class EngineModule implements Module, Configuration
{
	@Override
	public final void configure(Binder binder)
	{
		// only use implicit providers
	}
	
	@Provides
	public UserService getUserService()
	{
		return UserServiceFactory.getUserService();
	}

	@Provides
	public User getUser()
	{
		return getUserService().getCurrentUser();
	}

	@Provides
	public QuotaService getQuotaService()
	{
		return QuotaServiceFactory.getQuotaService();
	}
	
	public static boolean isDevelopment()
	{
		return Environment.environment.value().equals(Environment.Value.Development);
	}

	public static boolean isTest()
	{
		String version = Environment.applicationVersion.get();
		return version.startsWith("test") || version.startsWith("demo");
	}

	public static boolean isLive()
	{
		return !isDevelopment() && !isTest();
	}

	@Override
	public void configure(Settings.Builder settings, Binder binder)
	{
		binder.install(this);
	}
}
