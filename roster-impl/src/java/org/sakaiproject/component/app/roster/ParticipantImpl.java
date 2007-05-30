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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	protected String groupsForDisplay;
	
	// These are dynamically built from the full list of sections and groups (in field 'sections').
	protected Map<String, CourseSection> sectionsMap;
	protected List<CourseSection> groups;

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
		if(enrolledSections == null) {
			this.sections = new ArrayList<CourseSection>();
		} else {
			this.sections = enrolledSections;
		}

		// Build the map of categories to sections
		sectionsMap = new HashMap<String, CourseSection>();
		if (sections != null) {
			for (Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
				CourseSection section = iter.next();
				String cat = StringUtils.trimToNull(section.getCategory());
				if(cat != null) sectionsMap.put(cat, section);
			}
		}

		// Build the list of groups
		groups = new ArrayList<CourseSection>();
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection sec = iter.next();
			if(StringUtils.trimToNull(sec.getCategory()) == null) {
				groups.add(sec);
			}
		}
		Collections.sort(groups, groupComparator);
		
		// And the groups list to display in the UI
		StringBuffer sb = new StringBuffer();
		for(Iterator<CourseSection> iter = groups.iterator(); iter.hasNext();) {
			CourseSection  group = iter.next();
			sb.append(group.getTitle());
			if(iter.hasNext()) {
				sb.append(", ");
			}
		}
		groupsForDisplay = sb.toString();
	}

	protected static final Comparator<CourseSection> groupComparator = new Comparator<CourseSection>() {
		public int compare(CourseSection one, CourseSection another) {
			return one.getTitle().compareTo(another.getTitle());
		}
	};

	public String getGroupsForDisplay() {
		return groupsForDisplay;
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
	
	public List<CourseSection> getGroups() {
		return groups;
	}
}
