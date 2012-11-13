package com.vercer.leaf.engine;

import com.google.inject.Binder;
import com.vercer.engine.guice.EngineModule;
import com.vercer.leaf.Settings;

public class EngineConfiguration extends EngineModule
{
	@Override
	public void configure(Settings.SettingsBuilder settings, Binder binder)
	{
		super.configure(settings, binder);
		settings.development(isDevelopment());
	}
}
