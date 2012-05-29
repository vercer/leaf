package com.vercer.engine.guice;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

public class GuiceContextListener implements ServletContextListener
{
	private static Injector injector;

	public void contextDestroyed(ServletContextEvent arg0)
	{
	}

	public void contextInitialized(ServletContextEvent arg0)
	{
		String stageName = arg0.getServletContext().getInitParameter("guice.stage");
		
		Stage stage = stageName == null ? Stage.PRODUCTION : Stage.valueOf(stageName);
		
		String moduleClassName = arg0.getServletContext().getInitParameter("guice.module");
		Module module;
		try
		{
			module = (Module) Class.forName(moduleClassName).newInstance();
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException(e);
		}

		Logger log = Logger.getLogger(this.getClass().getName());
		log.info("Build injector");
		injector = Guice.createInjector(stage, module);
		log.info("Build injector done");
	}

	public static Injector getInjector()
	{
		return injector;
	}
}
