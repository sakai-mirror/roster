package org.sakaiproject.roster.tool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

public class SakaiProxy {

	private static final Log log = LogFactory.getLog(SakaiProxy.class);
	
	private ToolManager toolManager = null;
	private UserDirectoryService userDirectoryService = null;
	
	public SakaiProxy() {

		ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		toolManager = (ToolManager) componentManager.get(ToolManager.class);
		userDirectoryService = (UserDirectoryService) componentManager
				.get(UserDirectoryService.class);

	}
	
	public String getCurrentUserId() {
		
		if (null == userDirectoryService.getCurrentUser()) {
			log.warn("cannot retrieve current user");
			return null;
		}
		
		return userDirectoryService.getCurrentUser().getId();
	}

	public String getCurrentSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}

}
