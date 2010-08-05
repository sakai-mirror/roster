/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.roster.tool;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.util.ResourceLoader;

/**
 * <code>RosterTool</code> performs basic checks and redirects to roster.html
 * 
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class RosterTool extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(RosterTool.class);

	private SakaiProxy sakaiProxy = null;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

		if (log.isDebugEnabled()) {
			log.debug("init");
		}
		
        ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
		sakaiProxy = (SakaiProxy) componentManager.get(SakaiProxy.class);
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		if (log.isDebugEnabled()) {
			log.debug("doGet()");
		}
		
		if (sakaiProxy == null) {
			throw new ServletException("sakaiProxy MUST be initialised.");
		}

		String userId = sakaiProxy.getCurrentUserId();

		if (userId == null) {
			// We are not logged in
			throw new ServletException("getCurrentUser returned null.");
		}

		// pass siteId, language code and sakai.properties to the JQuery code
		response.sendRedirect("/sakai-roster-tool/roster.html?state=" + "pics"
				+ "&siteId=" + sakaiProxy.getCurrentSiteId() + "&language="
				+ (new ResourceLoader(userId)).getLocale().getLanguage()
				+ "&defaultSortColumn=" + sakaiProxy.getDefaultSortColumn()
				+ "&firstNameLastName="
				+ sakaiProxy.getFirstNameLastName()
				+ "&hideSingleGroupFilter="
				+ sakaiProxy.getHideSingleGroupFilter() + "&viewEmailColumn="
				+ sakaiProxy.getViewEmailColumn());
	}
}
