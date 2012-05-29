package com.vercer.jet;

import com.google.inject.Binder;

public interface Configuration
{
	public void configure(Settings.Builder settings, Binder binder);
}
