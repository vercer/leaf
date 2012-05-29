package com.vercer.jet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.java.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton @Log
public class JetFilter implements Filter
{
	@Inject private
	Dispatcher dispatcher;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
	throws IOException, ServletException
	{
		// if we did not handle this request pass it on down the chain
		try
		{
			if (!dispatcher.dispatch((HttpServletRequest) request, (HttpServletResponse) response))
			{
				// should avoid passing static content requests through filter
				log.fine("Jet did not handle " + ((HttpServletRequest)request).getRequestURI());

				chain.doFilter(request, response);
			}
		}
		catch (Throwable e)
		{
			throw new ServletException(e);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	}

	@Override
	public void destroy()
	{
	}
}
