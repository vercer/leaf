package com.vercer.leaf.engine;

import com.google.appengine.tools.appstats.AppstatsFilter;
import com.google.appengine.tools.appstats.AppstatsServlet;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;

public class AppStatsModule extends ServletModule
{
	private String path;
	private boolean cost;
	private String viewer;

	public AppStatsModule(String path, String viewer, boolean cost)
	{
		this.path = path;
		this.viewer = viewer;
		this.cost = cost;
	}
	
	@Override
	protected void configureServlets()
	{
		bind(AppstatsServlet.class).in(Scopes.SINGLETON);
		filter(path).through(AppstatsFilter.class, ImmutableMap.of("calculateRpcCosts", Boolean.toString(cost)));
		serve(viewer).with(AppstatsServlet.class);
	}
}
