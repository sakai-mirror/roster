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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.model.SelectItem;

import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.section.api.coursemanagement.CourseSection;

public class FilteredStatusListingBean extends FilteredProfileListingBean implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Overrides the behavior in FilteredParticipantListingBean.  Here, if the status filter
	 * matches the participant's enrollment status, the participant is a match.
	 */
	protected boolean participantMatchesStatusFilter(Participant participant, String statusFilter, Set<String> studentRoles) {
		// TODO Implement this method
		return true;
	}

	public List<SelectItem> getSectionSelectItems() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		// Get the available sections
		List<CourseSection> sections = services.rosterManager.getViewableSectionsForCurrentUser();
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection sakaiSection = iter.next();
			if(sakaiSection.getEid() != null) {
				// This is an official section.  Does it have an enrollment set?
				Section cmSection = services.cmService.getSection(sakaiSection.getEid());
				if(cmSection.getEnrollmentSet() != null) {
					list.add(new SelectItem(sakaiSection.getUuid(), sakaiSection.getTitle()));
				}
			}
		}
		return list;
	}
	
}
