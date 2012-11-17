package com.vercer.leaf.toolkit;

import com.google.inject.Binder;
import com.vercer.leaf.Configuration;
import com.vercer.leaf.Settings.SettingsBuilder;
import com.vercer.leaf.html.Anchor;

public class ToolkitConfiguration implements Configuration
{
	@Override
	public void configure(SettingsBuilder settings, Binder binder)
	{
		// TODO should be a setting
		Anchor.sticky("gwt.codesvr");
	}
}
