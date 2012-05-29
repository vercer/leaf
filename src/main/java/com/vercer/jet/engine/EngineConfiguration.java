package com.vercer.jet.engine;

import com.google.inject.Binder;
import com.vercer.engine.guice.EngineModule;
import com.vercer.jet.Settings;

public class EngineConfiguration extends EngineModule
{
	@Override
	public void configure(Settings.Builder settings, Binder binder)
	{
		super.configure(settings, binder);
		settings.development(isDevelopment());
	}
}
