/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
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
package org.sakaiproject.tool.roster;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFilter;
import org.sakaiproject.coursemanagement.api.CourseOffering;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;

public class FilteredProfileListingBean extends InitializableBean implements Serializable {

	private static final long serialVersionUID = 1L;

	protected ServicesBean services;
	public void setServices(ServicesBean services) {
		this.services = services;
	}

	protected RosterPreferences prefs;
	public void setPrefs(RosterPreferences prefs) {
		this.prefs = prefs;
	}
	
	// A list of decorated users
	protected List<Participant> participants;

	// Our filter, used to limit the number of Participants displayed in the UI
	protected RosterFilter filter;

	public List<Participant> getParticipants() {
		if(participants == null) {
			participants = services.rosterManager.getRoster(getFilter());			
		}
		return participants;
	}

	/**
	 * Gets a Map of roles to the number of users in the site with those roles.
	 * @return
	 */
	public Map<String, Integer> getRoleCounts() {
		Map<String, Integer> map = new TreeMap<String, Integer>();
		for(Iterator<Participant> iter = getParticipants().iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			String key = participant.getRoleTitle();
			if(map.containsKey(key)) {
				Integer prevCount = map.get(key);
				map.put(key, ++prevCount);
			} else {
				map.put(key, 1);
			}
		}
		return map;
	}

	/**
	 * We display enrollment details when there is a single EnrollmentSet associated
	 * with a site, or when multiple EnrollmentSets are children of cross listed
	 * CourseOfferings.
	 * 
	 * @return
	 */
	public boolean isDisplayEnrollmentDetails() {
		Set<Section> officialSections = services.rosterManager.getOfficialSectionsInSite();
		int count = 0;
		for(Iterator<Section> iter = officialSections.iterator(); iter.hasNext();) {
			Section section = iter.next();
			EnrollmentSet es = section.getEnrollmentSet();
			if(es != null) {
				count++;
			}
		}
		if(count == 0) return false;
		if(count == 1) return true;

		// TODO Deal with cross listings.  Multiple cross listed courses should still show enrollment details
		return false;
	}
	
	public RosterFilter getFilter() {
		if(filter == null) {
			filter = services.rosterManager.newFilter();
		}
		return filter;
	}

	public void setFilter(RosterFilter filter) {
		this.filter = filter;
	}
}
