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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.user.api.User;

public class FilteredProfileListingBean implements Serializable {
	private static final String VIEW_ALL = "roster_view_all";
	private static final String VIEW_STUDENTS = "roster_view_students";
	private static final Log log = LogFactory.getLog(FilteredProfileListingBean.class);
	private static final long serialVersionUID = 1L;

	protected ServicesBean services;
	public void setServices(ServicesBean services) {
		this.services = services;
	}

	protected String viewFilter;
	protected String searchFilter = getDefaultSearchText();
	protected String sectionFilter;
	
	protected Map<String, String> sectionCategoryMap;

	// Cache the participants list so we don't have to fetch it twice (once for the list,
	// and again for its size)
	protected List<Participant> participants;
	protected Integer participantCount;
	protected SortedMap<String, Integer> roleCounts;
	protected boolean displayingParticipants = false;

	/**
	 * Initialize this bean once, so we can call our access method as often as we like
	 * without invoking unnecessary service calls.
	 */
	public void init() {
		this.participants = findParticipants();
		this.participantCount = this.participants.size();
		this.roleCounts = findRoleCounts(this.participants);
		
		// if we have entries in the roleCounts map, we have participants to display
		if( ! roleCounts.isEmpty()) {
			displayingParticipants = true;
		}
		
		// Build the section category map
		sectionCategoryMap = new HashMap<String, String>();
		for(Iterator<String> iter = services.cmService.getSectionCategories().iterator(); iter.hasNext();) {
			String category = iter.next();
			sectionCategoryMap.put(category, services.cmService.getSectionCategoryDescription(category));
		}
	}

	/**
	 * JSF hack to call init() when a filtering page is rendered.
	 * @return null;
	 */
	public String getInit() {
		init();
		return null;
	}
	
	// UI Actions
	
	public void search(ActionEvent ae) {
		// Nothing needs to be done to search
	}
	
	public void clearSearch(ActionEvent ae) {
		searchFilter = getDefaultSearchText();
	}

	protected List<Participant> findParticipants() {
		List<Participant> participants = services.rosterManager.getRoster();
		String defaultText = getDefaultSearchText();
		Set<String> studentRoles = getStudentRoles();
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			
			if( ! participantMatchesViewFilter(participant, studentRoles)) {
				iter.remove();
				continue;
			}
			
			// Remove this participant if they don't  pass the search filter
			if(searchFilter != null && ! searchFilter.equals(defaultText) && ! searchMatches(searchFilter, participant.getUser())) {
				iter.remove();
				continue;
			}
			
			// Remove this participant if they don't  pass the section filter
			if(sectionFilter != null && isDisplaySectionsFilter() && ! sectionMatches(sectionFilter, participant.getSectionsMap())) {
				iter.remove();
				continue;
			}
		}
		return participants;
	}
	
	protected boolean participantMatchesViewFilter(Participant participant, Set<String> studentRoles) {
		if(VIEW_STUDENTS.equals(viewFilter)) {
			// We are filtering on the collection of all student roles
			if( ! studentRoles.contains(participant.getRoleTitle())) {
				return false;
			}
		}
		return true;
	}
	
	protected Set<String> getStudentRoles() {
		AuthzGroup azg = null;
		try {
			azg = services.authzService.getAuthzGroup(getSiteReference());
		} catch (GroupNotDefinedException gnde) {
			log.error("Unable to find site " + getSiteReference());
			return new HashSet<String>();
		}
		Set<String> roles = azg.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
		return roles;
	}
	
	protected SortedMap<String, Integer> findRoleCounts(List<Participant> participants) {
		SortedMap<String, Integer> roleCountMap = new TreeMap<String, Integer>();
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			String role = participant.getRoleTitle();
			Integer count = roleCountMap.get(role);
			if(count == null) {
				roleCountMap.put(role, new Integer(1));
			} else {
				roleCountMap.put(role, ++count);
			}
		}
		return roleCountMap;
	}
	
	protected boolean searchMatches(String search, User user) {
		return user.getDisplayName().toLowerCase().startsWith(search.toLowerCase()) ||
				   user.getSortName().toLowerCase().startsWith(search.toLowerCase()) ||
				   user.getDisplayId().toLowerCase().startsWith(search.toLowerCase()) ||
				   user.getEmail().toLowerCase().startsWith(search.toLowerCase());
	}

	protected boolean sectionMatches(String sectionUuid, Map<String, CourseSection> sectionsMap) {
		for(Iterator<Entry<String, CourseSection>> iter = sectionsMap.entrySet().iterator(); iter.hasNext();) {
			Entry<String, CourseSection> entry = iter.next();
			CourseSection section = entry.getValue();
			if(section.getUuid().equals(sectionUuid)) return true;
		}
		return false;
	}
	
	public List<SelectItem> getSectionSelectItems() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		// Get the available sections
		List<CourseSection> sections = services.rosterManager.getViewableSectionsForCurrentUser();
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
			list.add(new SelectItem(section.getUuid(), section.getTitle()));
		}
		return list;
	}
	
	public List<SelectItem> getStatusSelectItems() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		Map<String, String> map = services.cmService.getEnrollmentStatusDescriptions(LocaleUtil.getLocale(FacesContext.getCurrentInstance()));
		
		// The UI doesn't care about status IDs... just labels
		List<String> statusLabels = new ArrayList<String>();
		for(Iterator<Entry<String, String>> iter = map.entrySet().iterator(); iter.hasNext();) {
			Entry<String, String> entry = iter.next();
			statusLabels.add(entry.getValue());
		}
		Collections.sort(statusLabels);
		for(Iterator<String> iter = statusLabels.iterator(); iter.hasNext();) {
			String statusLabel = iter.next();
			SelectItem item = new SelectItem(statusLabel, statusLabel);
			list.add(item);
		}
		return list;
	}
	
	public boolean isDisplaySectionsFilter() {
		return services.rosterManager.getViewableSectionsForCurrentUser().size() > 1;
	}

	public String getSearchFilter() {
		return searchFilter;
	}

	public void setSearchFilter(String searchFilter) {
		String trimmedArg = StringUtils.trimToNull(searchFilter);
		String defaultText = getDefaultSearchText();
		if(trimmedArg == null) {
			this.searchFilter = defaultText;
		} else {
			this.searchFilter = trimmedArg;
		}
	}

	public String getSectionFilter() {
		return sectionFilter;
	}

	public void setSectionFilter(String sectionFilter) {
		// Don't allow this value to be set to the separater line
		if(LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "roster_section_sep_line")
				.equals(sectionFilter)) {
			this.sectionFilter = null;
		} else {
			this.sectionFilter = StringUtils.trimToNull(sectionFilter);
		}
	}

	public String getViewFilter() {
		return viewFilter;
	}

	public void setViewFilter(String statusFilter) {
		this.viewFilter = StringUtils.trimToNull(statusFilter);
	}

	public String getDefaultSearchText() {
		return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "roster_search_text");
	}

	public Integer getParticipantCount() {
		return participantCount;
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public String getRoleCountMessage() {
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for(Iterator<Entry<String, Integer>> iter = roleCounts.entrySet().iterator(); iter.hasNext();) {
			Entry<String, Integer> entry = iter.next();
			int count = entry.getValue();
			sb.append(count);
			sb.append(" ");
			sb.append(entry.getKey());
			// Make the role plural if necessary
			if(count != 1) {
				sb.append("s");
			}
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public boolean isDisplayingParticipants() {
		return displayingParticipants;
	}
	
	protected String getSiteReference() {
		return "/site/" + getSiteContext();
	}
	protected String getSiteContext() {
		return services.toolManager.getCurrentPlacement().getContext();
	}

	public Map<String, String> getSectionCategoryMap() {
		return sectionCategoryMap;
	}

}
