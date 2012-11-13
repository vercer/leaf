package com.vercer.leaf;

import com.google.inject.Binder;

public interface Configuration
{
	public void configure(Settings.SettingsBuilder settings, Binder binder);
}
