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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.coursemanagement.Course;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.user.api.User;

public class FilteredParticipantListingBean implements Serializable {
	private static final Log log = LogFactory.getLog(FilteredParticipantListingBean.class);
	private static final long serialVersionUID = 1L;

	protected ServicesBean services;
	public void setServices(ServicesBean services) {
		this.services = services;
	}
	protected SearchFilter searchFilter;
	public void setSearchFilter(SearchFilter searchFilter) {
		this.searchFilter = searchFilter;
	}
	
	protected String viewFilter;
	protected String defaultSearchText;
	protected String sectionFilter;


	// Cache the participants list so we don't have to fetch it twice (once for the list,
	// and again for its size)
	protected List<Participant> participants;
	protected Integer participantCount;
	protected SortedMap<String, Integer> roleCounts;

	/**
	 * Initialize this bean once, so we can call our access method as often as we like
	 * without invoking unnecessary service calls.
	 */
	public void init() {
		this.participants = findParticipants();
		this.participantCount = this.participants.size();
		this.roleCounts = findRoleCounts(this.participants);

		if(defaultSearchText == null) defaultSearchText = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "roster_search_text");
		if(getSearchFilterString() == null) searchFilter.setSearchFilter(defaultSearchText);
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
		searchFilter.setSearchFilter(defaultSearchText);
	}

	protected List<Participant> findParticipants() {
		// Only get the participants we need
		List<Participant> participants;
		if(sectionFilter != null && isDisplaySectionsFilter()) {
			participants = services.rosterManager.getRoster(sectionFilter);
		} else {
			participants = services.rosterManager.getRoster();
		}
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			if(filterParticipant(participant)) iter.remove();
		}

		return participants;
	}

	/**
	 * Remove this participant if they don't  pass the search filter
	 */
	protected boolean filterParticipant(Participant participant) {
		return getSearchFilterString() != null && ! getSearchFilterString().equals(defaultSearchText) && ! searchMatches(getSearchFilterString(), participant.getUser());
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

	public List<SelectItem> getSectionSelectItems() {
		List<SelectItem> list = new ArrayList<SelectItem>();

		FacesContext facesContext = FacesContext.getCurrentInstance();
		String sepLine = LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "roster_section_sep_line");
        String all_sections = LocaleUtil.getLocalizedString(facesContext, ServicesBean.MESSAGE_BUNDLE, "roster_sections_all");

        // Add the "all" select option and a separator line
        list.add(new SelectItem("", all_sections));
        list.add(new SelectItem(sepLine, sepLine));

		// Get the available sections
		List<CourseSection> sections = requestCache().viewableSections;
		for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
			list.add(new SelectItem(section.getUuid(), section.getTitle()));
		}
		return list;
	}

	protected List<CourseSection> getViewableEnrollableSections() {
		return services.rosterManager.getViewableEnrollmentStatusSectionsForCurrentUser();
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
		return requestCache().viewableSections.size() > 1;
	}

	public String getSearchFilterString() {
		return searchFilter.getSearchFilter();
	}

	public void setSearchFilterString(String searchFilter) {
		String trimmedArg = StringUtils.trimToNull(searchFilter);
		if(trimmedArg == null) {
			this.searchFilter.setSearchFilter(defaultSearchText);
		} else {
			this.searchFilter.setSearchFilter(trimmedArg);
		}
	}

	public String getSectionFilter() {
		return sectionFilter;
	}

    public String getSectionFilterTitle(){
        List<CourseSection> sections = requestCache().viewableSections;
        for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
            if(section.getUuid().equals(getSectionFilter())) return section.getTitle();
		}
        return null;
    }

     public String getCourseFilterTitle(){
        List<CourseSection> sections = requestCache().viewableSections;
        for(Iterator<CourseSection> iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
            Course course = section.getCourse();
            return course.getTitle();
		}
        return null;
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

	public Integer getParticipantCount() {
		return participantCount;
	}

	public List<Participant> getParticipants() {
		return participants;
	}

	public String getRoleCountMessage() {
        if(roleCounts.size() == 0) return "";
		StringBuffer sb = new StringBuffer();
		sb.append("(");
		for(Iterator<Entry<String, Integer>> iter = roleCounts.entrySet().iterator(); iter.hasNext();) {
			Entry<String, Integer> entry = iter.next();
			String[] params = new String[] {entry.getValue().toString(), entry.getKey()};			
			sb.append(getFormattedMessage("role_breakdown_fragment", params));
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	private String getFormattedMessage(String key, String[] params) {
		String rawString = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(), ServicesBean.MESSAGE_BUNDLE, key);
        MessageFormat format = new MessageFormat(rawString);
        return format.format(params);
	}
	
	public boolean isDisplayingParticipants() {
		// if we have entries in the roleCounts map, we have participants to display
		return ! roleCounts.isEmpty();
	}

    protected String getSiteReference() {
		return "/site/" + getSiteContext();
	}
	protected String getSiteContext() {
		return services.toolManager.getCurrentPlacement().getContext();
	}

	// We use this request-scoped bean to hold a reference to the sections in this site.
	// DO NOT cache the RequestCache itself.  Always obtain a reference using
	// requestCache().
	protected RequestCache requestCache() {
		RequestCache rc = (RequestCache)resolveManagedBean("requestCache");
		// Manually initialize the cache, if necessary
		if( ! rc.isInitizlized()) rc.init(services);
		return rc;
	}

	// This will either retrieve the existing managed bean, or generate a new one
	protected Object resolveManagedBean(String managedBeanId) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, managedBeanId);
	}
	
	public String getDefaultSearchText() {
		return defaultSearchText;
	}

}
