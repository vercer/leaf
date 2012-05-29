package com.vercer.jet.toolkit;

import com.google.inject.Binder;
import com.vercer.jet.Configuration;
import com.vercer.jet.Settings.Builder;
import com.vercer.jet.transform.Anchor;

public class ToolkitConfiguration implements Configuration
{
	@Override
	public void configure(Builder settings, Binder binder)
	{
		// TODO should be a setting
		Anchor.sticky("gwt.codesvr");
	}
}
