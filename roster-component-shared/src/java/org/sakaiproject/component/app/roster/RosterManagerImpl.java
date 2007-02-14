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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterFilter;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.FunctionManager;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.section.api.SectionAwareness;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.section.api.coursemanagement.ParticipationRecord;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;

public abstract class RosterManagerImpl implements RosterManager {
	private static final Log LOG = LogFactory.getLog(RosterManagerImpl.class);

	public abstract ProfileManager profileManager();
	public abstract PrivacyManager privacyManager();
	public abstract SectionAwareness sectionService();
	public abstract SiteService siteService();
	public abstract ToolManager toolManager();
	public abstract FunctionManager functionManager();
	public abstract UserDirectoryService userDirectoryService();
	public abstract AuthzGroupService authzGroupService();
	public abstract SecurityService securityService();
	
	public void init() {
		LOG.debug("init()");

		Collection registered = functionManager().getRegisteredFunctions(RosterFunctions.ROSTER_FUNCTION_PREFIX);
		if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_EXPORT)) {
			functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_EXPORT);
		}

		if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWALL)) {
			functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWALL);
		}

		if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN)) {
			functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN);
		}

		if (!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALID)) {
			functionManager().registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALID);
		}
	}

	public void destroy() {
		LOG.debug("destroy()");
	}

	private Participant createParticipantByUser(User user, Profile profile) {
		return new ParticipantImpl(user, profile, getUserRoleTitle(user),
				getViewableSectionsForUser(user));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.roster.RosterManager#getParticipantById(java.lang.String)
	 */
	public Participant getParticipantById(String participantId) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("getParticipantById(String" + participantId + ")");
		}
		if (participantId != null) {
			try {
				User user = userDirectoryService().getUser(participantId);
				Profile profile = profileManager().getUserProfileById(
						participantId);
				// TODO: individual ferpa checks
				return createParticipantByUser(user, profile);
			} catch (UserNotDefinedException e) {
				LOG.error("getParticipantById: " + e.getMessage(), e);
			}
		}
		return null;
	}

	public List<Participant> getRoster() {
		return getRoster(null);
	}

	/**
	 * Retrieve a filtered list of site participants that are viewable by the
	 * current user.
	 * 
	 * We have three different view scenarios:
	 * 
	 * <ol>
	 * 
	 * <li> View all: These users can see every site member, regardless of
	 * privacy settings. </li>
	 * 
	 * <li> View sections: These users can see every member of sections or
	 * groups for which this user is a TA, regardless of privacy settings. These
	 * users also see the other site members who have chosen to show themselves
	 * (privacy setting = off). </li>
	 * 
	 * <li> View non-hidden participants: These users see only site members who
	 * have chosen to show themselves (privacy setting = off). </li>
	 * 
	 * </ol>
	 * 
	 * @param filter
	 * @return List
	 */
	public List<Participant> getRoster(RosterFilter filter) {
		LOG.debug("getRoster called with filter " + filter);
		if (filter == null)
			filter = new LocalRosterFilter();

		List<Participant> participants;

		User currentUser = userDirectoryService().getCurrentUser();
		boolean userHasViewAllPerm = userHasPermission(currentUser,
				RosterFunctions.ROSTER_FUNCTION_VIEWALL);

		// Instructors and users with "viewall" see everybody
		if (userHasViewAllPerm
				|| sectionService().isSiteMemberInRole(
						getSiteId(),
						currentUser.getId(),
						org.sakaiproject.section.api.facade.Role.INSTRUCTOR)) {
			participants = getAllParticipants();
		} else if (sectionService().isSiteMemberInRole(
				getSiteId(),
				currentUser.getId(),
				org.sakaiproject.section.api.facade.Role.TA)) {
			participants = getSectionLeaderParticipants(currentUser);
		} else {
			participants = getPublicParticipants(currentUser);
		}
		return participants;
	}

	private List<Participant> getAllParticipants() {
		Map<String, UserRole> userMap = getUserRoleMap(getSiteReference());
		Map<String, List<CourseSection>> sectionsMap = getSectionsMap(userMap.keySet());
		Map<String, Profile> profiles = profileManager().getProfiles(userMap.keySet());

		return buildParticipantList(userMap, sectionsMap, profiles);
	}
	
	private List<Participant> getSectionLeaderParticipants(User currentUser) {
		Map<String, UserRole> userMap = getUserRoleMap(getSiteReference());
		Map<String, List<CourseSection>> sectionsMap = getSectionsMap(userMap.keySet());

		// Build a list of the sections that this user leads
		List<CourseSection> allSections = sectionService().getSections(getSiteId());
		List<CourseSection> usersSections = new ArrayList<CourseSection>();
		for(Iterator<CourseSection> iter = allSections.iterator(); iter.hasNext();)
		{
			CourseSection section = iter.next();
			if(sectionService().isSectionMemberInRole(section.getUuid(), currentUser.getId(), org.sakaiproject.section.api.facade.Role.INSTRUCTOR)
					|| sectionService().isSectionMemberInRole(section.getUuid(), currentUser.getId(), org.sakaiproject.section.api.facade.Role.TA))
			{
				usersSections.add(section);
			}
		}

		Map<String, List<CourseSection>> filteredSectionsMap = new HashMap<String, List<CourseSection>>();
		
		// Filter out any sections from the sections map that are not led by this user
		for(Iterator<Entry<String, List<CourseSection>>> iter = sectionsMap.entrySet().iterator(); iter.hasNext();)
		{
			Entry<String, List<CourseSection>> entry = iter.next();
			List entrySections = entry.getValue();
			// If the list of sections that the user leads contains any of the entry's sections, add it to our filtered list
			for(Iterator<CourseSection> ledSectionsIter = usersSections.iterator(); ledSectionsIter.hasNext();)
			{
				if(entrySections.contains(ledSectionsIter.next()))
				{
					filteredSectionsMap.put(entry.getKey(), entry.getValue());
					break;
				}
			}
		}
		
		// Build the list of participants from the filteredSectionsMap
		Map<String, Profile> profiles = profileManager().getProfiles(filteredSectionsMap.keySet());

		return buildParticipantList(userMap, sectionsMap, profiles);
	}

	private List<Participant> getPublicParticipants(User currentUser) {
		// The view hidden permission only applies to non view-all or ta users
		if(userHasPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN))
		{
			return getAllParticipants();
		}
		Map<String, UserRole> userMap = getUserRoleMap(getSiteReference());
		
		// Only retain the viewable users
		Set<String> viewableUsers = privacyManager().findViewable(getSiteId(), userMap.keySet());
		userMap.keySet().retainAll(viewableUsers);

		Map<String, List<CourseSection>> sectionsMap = getSectionsMap(userMap.keySet());
		Map<String, Profile> profiles = profileManager().getProfiles(userMap.keySet());

		return buildParticipantList(userMap, sectionsMap, profiles);
	}


	private List<Participant> buildParticipantList(Map<String, UserRole> userMap, Map<String, List<CourseSection>> sectionsMap, Map<String, Profile> profiles) {
		List<Participant> participants = new ArrayList<Participant>();
		for (Iterator<Entry<String, Profile>> iter = profiles.entrySet().iterator(); iter.hasNext();) {
			Entry<String, Profile> entry = iter.next();
			String userId = entry.getKey();
			UserRole userRole = userMap.get(userId);
			Profile profile = entry.getValue();
			participants.add(new ParticipantImpl(userRole.user, profile, userRole.role, sectionsMap.get(userId)));
		}
		return participants;
	}

	/**
	 * Builds a map of user IDs to a list of Sections for that user within the current site context.
	 * 
	 * @param userIds The user IDs to include
	 * @return
	 */
	private Map<String, List<CourseSection>> getSectionsMap(Set<String> userIds)
	{
		Map<String, List<CourseSection>> sectionsMap = new HashMap<String, List<CourseSection>>();
		for(Iterator<CourseSection> iter = sectionService().getSections(getSiteId()).iterator(); iter.hasNext();)
		{
			CourseSection section = iter.next();
			List<ParticipationRecord> sectionMembers = sectionService().getSectionMembers(section.getUuid());
			for(Iterator<ParticipationRecord> participantIter = sectionMembers.iterator(); participantIter.hasNext();)
			{
				ParticipationRecord participant = participantIter.next();
				String userId = participant.getUser().getUserUid();
				if( ! userIds.contains(userId)) continue;
				
				// If this user isn't in the map, add them
				if(sectionsMap.containsKey(userId))
				{
					List<CourseSection> list = new ArrayList<CourseSection>();
					list.add(section);
					sectionsMap.put(userId, list);
				}
				else
				// The user was already in the map, so add this section to the list
				{
					List<CourseSection> list = sectionsMap.get(userId);
					if( ! list.contains(section))
					{
						list.add(section);
					}
				}
			}
			
		}
		return sectionsMap;
	}

	class UserRole {
		User user;
		String role;

		UserRole(User user, String role)
		{
			this.user = user;
			this.role = role;
		}
	}
	
	/**
	 * Gets a map of user IDs to UserRole (User + Role) objects.
	 * 
	 * @return
	 */
	private Map<String, UserRole> getUserRoleMap(String authzRef) {
		Map<String, UserRole> userMap = new HashMap<String, UserRole>();
		Set<String> userIds = new HashSet<String>();
		Set<Member> members;
		
		// Get the member set
		try {
			members = authzGroupService().getAuthzGroup(authzRef).getMembers();
		} catch (GroupNotDefinedException e) {
			LOG.error("getUsersInAllSections: " + e.getMessage(), e);
			return userMap;
		}
		
		// Build a map of userId to role
		Map<String, String> roleMap = new HashMap<String, String>();
		for(Iterator<Member> iter = members.iterator(); iter.hasNext();)
		{
			Member member = iter.next();
			userIds.add(member.getUserId());
			roleMap.put(member.getUserId(), member.getRole().getId());
		}

		// Get the user objects
		List<User> users = userDirectoryService().getUsers(userIds);
		for (Iterator<User> iter = users.iterator(); iter.hasNext();)
		{
			User user = iter.next();
			String role = roleMap.get(user.getId());
			userMap.put(user.getId(), new UserRole(user, role));
		}
		return userMap;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasExportPerm()
	 */
	public boolean currentUserHasExportPerm() {
		return userHasPermission(userDirectoryService().getCurrentUser(),
				RosterFunctions.ROSTER_FUNCTION_EXPORT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasViewOfficialIdPerm()
	 */
	public boolean currentUserHasViewOfficialIdPerm() {
		return userHasPermission(userDirectoryService().getCurrentUser(),
				RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasViewHiddenPerm()
	 */
	public boolean currentUserHasViewHiddenPerm() {
		return userHasPermission(userDirectoryService().getCurrentUser(),
				RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN);
	}

	  /*
	   * (non-Javadoc)
	   * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasSiteUpdatePerm()
	   */
	  public boolean currentUserHasSiteUpdatePerm()
	  {
		  return userHasPermission(userDirectoryService().getCurrentUser(), "site.upd");
	  }
	  
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasViewAllPerm()
	 */
	public boolean currentUserHasViewAllPerm() {
		return userHasPermission(userDirectoryService().getCurrentUser(),
				RosterFunctions.ROSTER_FUNCTION_VIEWALL);
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.api.app.roster.RosterManager#sortParticipants(java.util.List,
	 *      java.lang.String, boolean)
	 */
	public void sortParticipants(List<ParticipantImpl> participants,
			String sortByColumn, boolean ascending) {
		if (participants == null || participants.size() <= 1)
			return;

		Comparator<ParticipantImpl> comparator;
		if (Participant.SORT_BY_NAME.equals(sortByColumn)) {
			comparator = ParticipantImpl.DisplayNameComparator;
		} else if (Participant.SORT_BY_ID.equals(sortByColumn)) {
			comparator = ParticipantImpl.DisplayIdComparator;
		} else if (Participant.SORT_BY_ROLE.equals(sortByColumn)) {
			comparator = ParticipantImpl.RoleComparator;
		} else {
			// This is a section-sorted list
			// FIXME Replace with the section category sort
			comparator = ParticipantImpl.RoleComparator;
		}

		Collections.sort(participants, comparator);
		if (!ascending) {
			Collections.reverse(participants);
		}
	}

	/**
	 * Check if given user has the given permission
	 * 
	 * @param user
	 * @param permissionName
	 * @return boolean
	 */
	private boolean userHasPermission(User user, String permissionName) {
		if (user != null)
			return securityService().unlock(user, permissionName,
					getSiteReference());
		else
			return false;
	}

	/**
	 * @return siteId
	 */
	private String getSiteReference() {
		LOG.debug("getContextSiteId()");
		return ("/site/" + getSiteId());
	}
	
	private String getSiteId() {
		return toolManager().getCurrentPlacement().getContext();
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	private String getUserRoleTitle(User user) {
		try {
			AuthzGroup realm = authzGroupService().getAuthzGroup(getSiteReference());
			Role userRole = realm.getUserRole(user.getId());
			return userRole.getId();
		} catch (GroupNotDefinedException e) {
			LOG.error("GroupNotDefinedException", e);
		}
		return "";
	}

	/**
	 * Determine if sectioning exists in this site
	 * 
	 * @return
	 */
	public boolean siteHasSections() {
		return ! sectionService().getSections(getSiteId()).isEmpty();
	}

	/**
	 * returns sections that user has permission to view
	 * 
	 * @return
	 */
	public List<CourseSection> getViewableSectionsForUser(User user) {
		List<CourseSection> viewableSections = new ArrayList<CourseSection>();
		List<CourseSection> sections = sectionService().getSections(
				getSiteId());
		for (Iterator iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection) iter.next();
			if (sectionService().isSectionMemberInRole(section.getUuid(),
					user.getId(), org.sakaiproject.section.api.facade.Role.TA)
					|| sectionService()
							.isSectionMemberInRole(
									section.getUuid(),
									user.getId(),
									org.sakaiproject.section.api.facade.Role.INSTRUCTOR)) {
				viewableSections.add(section);
			}
		}
		return viewableSections;
	}

	public RosterFilter newFilter(String searchFilter, String sectionFilter,
			String statusFilter) {
		return new LocalRosterFilter(searchFilter, sectionFilter, statusFilter);
	}

	public RosterFilter newFilter() {
		return new LocalRosterFilter();
	}

	public class LocalRosterFilter implements RosterFilter, Serializable {
		private static final long serialVersionUID = 1L;

		protected String statusFilter;

		protected String searchFilter;

		protected String sectionFilter;

		public LocalRosterFilter() {
			this.sectionFilter = VIEW_ALL_SECT;
		}

		public LocalRosterFilter(String statusFilter, String searchFilter,
				String sectionFilter) {
			this.statusFilter = statusFilter;
			this.searchFilter = searchFilter;
			this.sectionFilter = sectionFilter;
		}

		public String getSearchFilter() {
			return searchFilter;
		}

		public void setSearchFilter(String searchFilter) {
			this.searchFilter = searchFilter;
		}

		public String getSectionFilter() {
			return sectionFilter;
		}

		public void setSectionFilter(String sectionFilter) {
			this.sectionFilter = sectionFilter;
		}

		public String getStatusFilter() {
			return statusFilter;
		}

		public void setStatusFilter(String statusFilter) {
			this.statusFilter = statusFilter;
		}

		public String toString() {
			return new ToStringBuilder(this).append(statusFilter).append(
					searchFilter).append(sectionFilter).toString();
		}
	}

}
