/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-api/api/src/java/org/sakaiproject/presence/api/PresenceService.java $
 * $Id: PresenceService.java 7844 2006-04-17 13:06:02Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.roster;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.user.api.User;

/**
 * @author rshastri
 * 
 */
public class ParticipantImpl implements Participant, Serializable {
	private static final long serialVersionUID = 1L;

	private final static Log log = LogFactory.getLog(ParticipantImpl.class);

	protected User user;
	protected Profile profile;
	protected String roleTitle;
	protected List<CourseSection> sections;
	private Map<String, CourseSection> sectionsMap;

	/**
	 * Constructs a ParticipantImpl.
	 * 
	 * @param user
	 * @param profile
	 * @param roleTitle
	 * @param enrolledSections
	 */
	public ParticipantImpl(User user, Profile profile, String roleTitle,
			List<CourseSection> enrolledSections) {
		this.user = user;
		this.profile = profile;
		this.roleTitle = roleTitle;
		this.sections = enrolledSections;
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public String getRoleTitle() {
		return roleTitle;
	}

	public void setRoleTitle(String roleTitle) {
		this.roleTitle = roleTitle;
	}

	public Map<String, CourseSection> getSectionsMap() {
		if (sectionsMap == null) {
			sectionsMap = new HashMap<String, CourseSection>();
			if (sections != null) {
				for (Iterator<CourseSection> iter = sections.iterator(); iter
						.hasNext();) {
					CourseSection section = iter.next();
					String key = StringUtils.trimToEmpty(section.getCategory());
					sectionsMap.put(key, section);
				}
			}
		}
		return sectionsMap;
	}

	public void setSections(List<CourseSection> sections) {
		this.sections = sections;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
