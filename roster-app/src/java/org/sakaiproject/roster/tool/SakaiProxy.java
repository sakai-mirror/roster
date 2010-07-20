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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.api.ComponentManager;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.UserDirectoryService;

/**
 * <code>SakaiProxy</code> acts as a proxy between Roster and Sakai components.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class SakaiProxy {

	private static final Log log = LogFactory.getLog(SakaiProxy.class);
	
	public final static String DEFAULT_SORT_COLUMN = "sortName";
	public final static Boolean DEFAULT_FIRST_NAME_LAST_NAME = false;
	public final static Boolean DEFAULT_HIDE_SINGLE_GROUP_FILTER = false;
	
	private ToolManager toolManager = null;
	private UserDirectoryService userDirectoryService = null;
	private ServerConfigurationService serverConfigurationService = null;
	
	public SakaiProxy() {

		ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager
				.getInstance();

		toolManager = (ToolManager) componentManager.get(ToolManager.class);
		userDirectoryService = (UserDirectoryService) componentManager
				.get(UserDirectoryService.class);
		serverConfigurationService = (ServerConfigurationService) componentManager
				.get(ServerConfigurationService.class);
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
	
	/* methods for reading sakai.properties */

	public String getDefaultSortColumn() {
		
		return serverConfigurationService
				.getString("roster.defaultSortColumn", DEFAULT_SORT_COLUMN);
	}
	
	public Boolean getDisplayFirstNameLastName() {

		return serverConfigurationService.getBoolean(
				"roster.display.firstNameLastName", DEFAULT_FIRST_NAME_LAST_NAME);

	}
	
	public Boolean getHideSingleGroupFilter() {

		return serverConfigurationService.getBoolean(
				"roster.display.hideSingleGroupFilter",
				DEFAULT_HIDE_SINGLE_GROUP_FILTER);
	}
	
}
