/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.web.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;

import com.sitewhere.SiteWhere;
import com.sitewhere.rest.model.user.Tenant;
import com.sitewhere.security.LoginManager;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.server.ISiteWhereTenantEngine;
import com.sitewhere.spi.server.lifecycle.LifecycleStatus;
import com.sitewhere.spi.user.ITenant;
import com.sitewhere.version.VersionHelper;

/**
 * Base class for SiteWhere Spring MVC controllers.
 * 
 * @author dadams
 */
public class MvcController {

	/** Static logger instance */
	private static Logger LOGGER = Logger.getLogger(MvcController.class);

	/** Session attribute for tenant */
	public static final String SESSION_TENANT = "com.sitewhere.Tenant";

	/** Version information sent in request */
	public static final String DATA_VERSION = "version";

	/** Current user information sent in request */
	public static final String DATA_CURRENT_USER = "currentUser";

	/** Granted authorities sent in request */
	public static final String DATA_AUTHORITIES = "authorities";

	/** Tenant information sent in request */
	public static final String DATA_TENANT = "tenant";

	/** Redirect URL for tenant selection page */
	public static final String DATA_REDIRECT = "redirect";

	/**
	 * Show error message for exception.
	 * 
	 * @param e
	 * @return
	 */
	protected ModelAndView showError(Exception e) {
		LOGGER.error(e);
		return showError(e.getMessage());
	}

	/**
	 * Returns a {@link ModelAndView} that will display an error message.
	 * 
	 * @param message
	 * @return
	 */
	protected ModelAndView showError(String message) {
		try {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put(DATA_VERSION, VersionHelper.getVersion());
			data.put(DATA_CURRENT_USER, LoginManager.getCurrentlyLoggedInUser());
			data.put("message", message);
			return new ModelAndView("error", data);
		} catch (SiteWhereException e) {
			Map<String, Object> data = new HashMap<String, Object>();
			data.put(DATA_VERSION, VersionHelper.getVersion());
			data.put("message", e.getMessage());
			return new ModelAndView("error", data);
		}
	}

	/**
	 * Create data structure and common objects passed to pages.
	 * 
	 * @param request
	 * @return
	 * @throws SiteWhereException
	 */
	protected Map<String, Object> createBaseData(HttpServletRequest request) throws SiteWhereException {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DATA_VERSION, VersionHelper.getVersion());
		data.put(DATA_CURRENT_USER, LoginManager.getCurrentlyLoggedInUser());
		data.put(DATA_AUTHORITIES, new AuthoritiesHelper(LoginManager.getCurrentlyLoggedInUser()));
		data.put(DATA_TENANT, assureTenant(request));
		return data;
	}

	/**
	 * Store the chosen tenant in the session.
	 * 
	 * @param tenant
	 * @param request
	 * @throws SiteWhereException
	 */
	protected void setChosenTenant(ITenant tenant, HttpServletRequest request) throws SiteWhereException {
		request.getSession().setAttribute(SESSION_TENANT, tenant);
	}

	/**
	 * Assure that a tenant is associated with the session.
	 * 
	 * @param request
	 * @return
	 * @throws NoTenantException
	 */
	protected ITenant assureTenant(HttpServletRequest request) throws NoTenantException {
		Tenant tenant = (Tenant) request.getSession().getAttribute(SESSION_TENANT);
		if (tenant == null) {
			throw new NoTenantException("Tenant not found in session.");
		}

		try {
			ISiteWhereTenantEngine engine = SiteWhere.getServer().getTenantEngine(tenant.getId());
			if (engine == null) {
				throw new NoTenantException("Engine not found for tenant.");
			}
			tenant.setEngineState(engine.getEngineState());

			if ((tenant.getEngineState() != null)
					&& (tenant.getEngineState().getLifecycleStatus() != LifecycleStatus.Started)) {
				throw new NoTenantException("Tenant engine not started.");
			}
			return tenant;
		} catch (SiteWhereException e) {
			throw new NoTenantException("Error loading tenant state.", e);
		}
	}

	/**
	 * Get URL from servlet request.
	 * 
	 * @param request
	 * @return
	 */
	protected static String getUrl(HttpServletRequest request) {
		String reqUrl = request.getRequestURL().toString();
		String queryString = request.getQueryString();
		if (queryString != null) {
			reqUrl += "?" + queryString;
		}
		return reqUrl;
	}
}