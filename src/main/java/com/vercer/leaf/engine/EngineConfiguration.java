package com.vercer.leaf.engine;

import com.google.inject.Binder;
import com.vercer.engine.guice.EngineModule;
import com.vercer.leaf.Configuration;
import com.vercer.leaf.Settings;

public class EngineConfiguration implements Configuration
{
	@Override
	public void configure(Settings.SettingsBuilder settings, Binder binder)
	{
		EngineModule module = new EngineModule();
		binder.install(module);
		settings.development(EngineModule.isDevelopment());
	}
}
