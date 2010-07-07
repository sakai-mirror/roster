package org.sakaiproject.roster.tool;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class RosterTool extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private static final Log log = LogFactory.getLog(RosterTool.class);

	private SakaiProxy sakaiProxy;
	
	public void init(ServletConfig config) throws ServletException
	{
		super.init(config);

		if (log.isDebugEnabled()) {
			log.debug("init");
		}
		
		sakaiProxy = new SakaiProxy();
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

		String siteId = sakaiProxy.getCurrentSiteId();

		// We need to pass the language code to the JQuery code in the pages.
		Locale locale = (new ResourceLoader(userId)).getLocale();
		String languageCode = locale.getLanguage();

		response.sendRedirect("/sakai-roster-tool/roster.html?state=" + "pics"
				+ "&siteId=" + siteId + "&language=" + languageCode);
	}
}
