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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.util.ResourceLoader;

public class FilteredStatusListingBean extends FilteredParticipantListingBean implements Serializable {

	private static final String ALL_STATUS="ALL_STATUS";
	private static final Log log = LogFactory.getLog(FilteredStatusListingBean.class);
	private static final long serialVersionUID = 1L;

	protected String statusFilter;
	protected Set<String> studentRoles; // TODO Is it OK to store this in the session?
	
	public void init() {
		// Get the student roles before any filtering occurs
		try {
			AuthzGroup azg = services.authzService.getAuthzGroup(getSiteReference());
			studentRoles = azg.getRolesIsAllowed(SectionAwareness.STUDENT_MARKER);
		} catch (GroupNotDefinedException gnde) {
			log.error("Unable to find site " + getSiteReference());
			studentRoles = new HashSet<String>();
		}

		// Set the filter status before any filtering occurs
		if(this.statusFilter == null) {
			this.statusFilter = ALL_STATUS;
		}

		// Proceed with the filtering
		super.init();
	}
	
	protected List<Participant> findParticipants() {
		if(log.isDebugEnabled()) log.debug("Finding participants filtered by enrollment status");
		// Find the enrollment status descriptions for the current user's locale
		Locale locale = new ResourceLoader().getLocale();
		Map<String, String> statusCodes = services.cmService.getEnrollmentStatusDescriptions(locale);

		// Make sure we're looking at a section with an enrollment set
		if(sectionFilter == null) {
			this.sectionFilter = (String)getSectionSelectItems().get(0).getValue();
		}
		
		List<Participant> participants = services.rosterManager.getRoster(sectionFilter);
		
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			if(filterParticipant(participant)) iter.remove();
		}

		// Decorate the participants returned by our filtering
		Map<String, Enrollment> enrollmentMap = getEnrollmentMap(sectionFilter);
		List<Participant> enrolledParticipants = new ArrayList<Participant>(participants.size());
		for(Iterator<Participant> iter = participants.iterator(); iter.hasNext();) {
			Participant participant = iter.next();
			String status = null;
			String credits = null;
			Enrollment enr = enrollmentMap.get(participant.getUser().getEid());
			if(enr != null) {
				status = statusCodes.get(enr.getEnrollmentStatus());
				credits = enr.getCredits();
			}
			EnrolledParticipant ep = new EnrolledParticipant(participant, status, credits);
			enrolledParticipants.add(ep);
		}
		
		if(ALL_STATUS.equals(statusFilter) || StringUtils.trimToNull(statusFilter) == null) {
			// No need for further filtering
			return enrolledParticipants;
		}
		
		// Filter the participants further, by status
		for(Iterator<Participant> iter = enrolledParticipants.iterator(); iter.hasNext();) {
			if( ! statusFilter.equals(((EnrolledParticipant)iter.next()).getEnrollmentStatus())) {
				iter.remove();
			}
		}
		
		return enrolledParticipants;
	}
	
	/**
	 * Gets a map of user EIDs to Enrollments for a given section.
	 * @param sectionEid
	 * @return
	 */
	private Map<String, Enrollment> getEnrollmentMap(String sectionUuid) {
		Map<String, Enrollment> enrollmentMap = new HashMap<String, Enrollment>();

		CourseSection internalSection = null;
		Section cmSection = null;
		EnrollmentSet es = null;
		try {
			internalSection = services.sectionAwareness.getSection(sectionUuid);
			cmSection = services.cmService.getSection(internalSection.getEid());
			es = cmSection.getEnrollmentSet();
		} catch (Exception e) {
			log.warn(e);
			return enrollmentMap;
		}
		
		Set<Enrollment> enrollments = services.cmService.getEnrollments(es.getEid());
		for(Iterator<Enrollment> iter = enrollments.iterator(); iter.hasNext();) {
			Enrollment enr = iter.next();
			enrollmentMap.put(enr.getUserId(), enr);
		}
		return enrollmentMap;
	}

	/**
	 * Filter this participant?
	 */
	protected boolean filterParticipant(Participant participant) {
		if(super.filterParticipant(participant)) return true;
		return ! studentRoles.contains(participant.getRoleTitle()); 
	}

	
	public List<SelectItem> getSectionSelectItems() {
		return statusRequestCache().getSectionSelectItems();
	}
	
	public boolean isMultipleEnrollableSectionsDisplayed() {
		return getSectionSelectItems().size() > 1;
	}
	
	public List<SelectItem> getViewableEnrollableSectionSelectItems() {
		List<SelectItem> selectItems = new ArrayList<SelectItem>();
		for(Iterator<CourseSection> iter = getViewableEnrollableSections().iterator(); iter.hasNext();) {
			CourseSection section = iter.next();
			selectItems.add(new SelectItem(section.getUuid(), section.getTitle()));
		}
		return selectItems;
	}

	
	/**
	 * Because the status filter is displayed here as a text, we need to ensure that
	 * it's never null.
	 * 
	 * @return
	 */
	public String getViewFilter() {
		String retValue = super.getViewFilter();
		if(retValue == null) {
			return "";
		} else {
			return retValue;
		}
	}

	public String getStatusFilter() {
		return statusFilter;
	}

	public void setStatusFilter(String statusFilter) {
		this.statusFilter = statusFilter;
	}
	

	public String getCurrentlyDisplayingMessage() {
		String key = LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "enrollments_currently_displaying");

		Object[] params = new Object[2];
		params[0] = participantCount;
		if(ALL_STATUS.equals(statusFilter)) {
			params[1] = "";
		} else {
			params[1] = statusFilter;
		}
		return MessageFormat.format(key, params);
	}
	
	public String getAllStatus() {
		return ALL_STATUS;
	}
	
	protected StatusRequestCache statusRequestCache() {
		StatusRequestCache rc = (StatusRequestCache)resolveManagedBean("statusRequestCache");
		// Manually initialize the cache, if necessary
		if( ! rc.isInitizlized()) rc.init(services);
		return rc;
	}
	
	public String getFirstSectionTitle() {
		return statusRequestCache().sectionSelectItems.get(0).getLabel();
	}

}
