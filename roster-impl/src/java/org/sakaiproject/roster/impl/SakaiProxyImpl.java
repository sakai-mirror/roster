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
package org.sakaiproject.roster.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.authz.api.GroupProvider;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.coursemanagement.api.EnrollmentSet;
import org.sakaiproject.coursemanagement.api.Section;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.roster.api.RosterEnrollment;
import org.sakaiproject.roster.api.RosterGroup;
import org.sakaiproject.roster.api.RosterMember;
import org.sakaiproject.roster.api.RosterSite;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <code>SakaiProxy</code> acts as a proxy between Roster and Sakai components.
 * 
 * @author Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * @author Adrian Fish (a.fish@lancaster.ac.uk)
 */
public class SakaiProxyImpl implements SakaiProxy {

	private static final Log log = LogFactory.getLog(SakaiProxyImpl.class);
	
	public final static String DEFAULT_SORT_COLUMN = "sortName";
	public final static Boolean DEFAULT_FIRST_NAME_LAST_NAME = false;
	public final static Boolean DEFAULT_HIDE_SINGLE_GROUP_FILTER = false;
	public final static Boolean DEFAULT_VIEW_EMAIL_COLUMN = true;
	
	//private AuthzGroupService authzGroupService = null;
	private CourseManagementService courseManagementService;
	//private FunctionManager functionManager = null;
	//private SecurityService securityService = null;
	private ServerConfigurationService serverConfigurationService = null;
	private SessionManager sessionManager = null;
	private SiteService siteService;
	private ToolManager toolManager = null;
	private UserDirectoryService userDirectoryService = null;
	
	/**
	 * Creates a new instance of <code>SakaiProxyImpl</code>
	 */
	public SakaiProxyImpl() {

		log.info("SakaiProxy initialized");
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getCurrentUserId() {
		
		// TODO should this be done via SessionManager instead?
		
		if (null == userDirectoryService.getCurrentUser()) {
			log.warn("cannot retrieve current user");
			return null;
		}
		
		return userDirectoryService.getCurrentUser().getId();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentSiteId() {
		return toolManager.getCurrentPlacement().getContext();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getDefaultSortColumn() {
		
		return serverConfigurationService
				.getString("roster.defaultSortColumn", DEFAULT_SORT_COLUMN);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getFirstNameLastName() {

		return serverConfigurationService.getBoolean(
				"roster.display.firstNameLastName", DEFAULT_FIRST_NAME_LAST_NAME);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getHideSingleGroupFilter() {

		return serverConfigurationService.getBoolean(
				"roster.display.hideSingleGroupFilter",
				DEFAULT_HIDE_SINGLE_GROUP_FILTER);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean getViewEmailColumn() {

		return serverConfigurationService.getBoolean("roster_view_email",
				DEFAULT_VIEW_EMAIL_COLUMN);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getMembership(String siteId, String groupId) {
		
		List<RosterMember> rosterMembers = new ArrayList<RosterMember>();
		
		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}
		
		if (null == site) {
			return null;
		}
		
		// permissions are handled inside this method call
		Set<Member> membership = getMembership(groupId, getCurrentUserId(), site);
		if (null == membership) {
			return null;
		}
		
		for (Member member : membership) {

			try {

				RosterMember rosterMember = getRosterMember(member, site);

				rosterMembers.add(rosterMember);

			} catch (UserNotDefinedException e) {
				e.printStackTrace();
			}
		}
		
		if (rosterMembers.size() == 0) {
			return null;
		}
		
		return rosterMembers;
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Map<String, RosterMember> getMembershipMapped(String siteId,
			String groupId) {

		Map<String, RosterMember> rosterMembers = new HashMap<String, RosterMember>();

		Site site = null;
		try {
			site = siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}

		if (null == site) {
			return null;
		}

		// permissions are handled inside this method call
		Set<Member> membership = getMembership(groupId, getCurrentUserId(), site);
		if (null == membership) {
			return null;
		}

		for (Member member : membership) {

			try {

				RosterMember rosterMember = getRosterMember(member, site);

				rosterMembers.put(rosterMember.getEid(), rosterMember);

			} catch (UserNotDefinedException e) {
				e.printStackTrace();
			}
		}

		return rosterMembers;
	}

	private Set<Member> getMembership(String groupId, String currentUserId,
			Site site) {
		
		Set<Member> membership = new HashSet<Member>();

		if (site.isAllowed(currentUserId, RosterFunctions.ROSTER_FUNCTION_VIEWALL)) {
			if (null == groupId) {
				// get all members
				membership.addAll(site.getMembers());
			} else if (null != site.getGroup(groupId)){
				// get all members of requested groupId
				membership.addAll(site.getGroup(groupId).getMembers());
			} else {
				// assume invalid groupId specified
				return null;
			}
		} else {
			if (null == groupId) {
				// get all members of groups current user is allow
				for (Group group : site.getGroups()) {
					if (group.isAllowed(currentUserId, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP)) {
						membership.addAll(group.getMembers());
					}
				}
			} else if (null != site.getGroup(groupId)){
				// get all members of requested groupId if current user is member
				if (site.getGroup(groupId).isAllowed(currentUserId, RosterFunctions.ROSTER_FUNCTION_VIEWGROUP)) {
					membership.addAll(site.getGroup(groupId).getMembers());
				}
			} else {
				// assume invalid groupId specified or user not member
				return null;
			}
		}
		return membership;
	}
	
	private RosterMember getRosterMember(Member member, Site site)
			throws UserNotDefinedException {
		
		String userId = member.getUserId();

		User user = userDirectoryService.getUser(userId);

		RosterMember rosterMember = new RosterMember(userId);
		rosterMember.setEid(user.getEid());
		rosterMember.setDisplayId(member.getUserDisplayId());
		rosterMember.setRole(member.getRole().getId());

		rosterMember.setEmail(user.getEmail());
		rosterMember.setDisplayName(user.getDisplayName());
		rosterMember.setSortName(user.getSortName());

		Collection<Group> groups = site.getGroupsWithMember(userId);
		Iterator<Group> groupIterator = groups.iterator();

		while (groupIterator.hasNext()) {
			Group group = groupIterator.next();

			rosterMember.addGroup(group.getId(), group.getTitle());
		}

		return rosterMember;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public RosterSite getSiteDetails(String siteId) {

		String currentUserId = getCurrentSessionUserId();
		if (null == currentUserId) {
			return null;
		}

		Site site = getSite(siteId);
		// only if user is a site member
		if (null == site.getMember(currentUserId)) {
			return null;
		}

		if (null == site) {
			return null;
		}

		RosterSite rosterSite = new RosterSite();

		rosterSite.setId(site.getId());
		rosterSite.setTitle(site.getTitle());

		List<RosterGroup> siteGroups = new ArrayList<RosterGroup>();

		boolean viewAll = site.isAllowed(currentUserId,
				RosterFunctions.ROSTER_FUNCTION_VIEWALL);

		for (Group group : site.getGroups()) {

			if (viewAll
					|| group.isAllowed(currentUserId,
							RosterFunctions.ROSTER_FUNCTION_VIEWGROUP)) {

				RosterGroup rosterGroup = new RosterGroup();
				rosterGroup.setId(group.getId());
				rosterGroup.setTitle(group.getTitle());

				List<String> userIds = new ArrayList<String>();

				for (Member member : group.getMembers()) {
					userIds.add(member.getUserId());
				}

				rosterGroup.setUserIds(userIds);

				siteGroups.add(rosterGroup);
			}
		}

		if (0 == siteGroups.size()) {
			// to avoid IndexOutOfBoundsException in EB code
			rosterSite.setSiteGroups(null);
		} else {
			rosterSite.setSiteGroups(siteGroups);
		}

		List<String> userRoles = new ArrayList<String>();
		for (Role role : site.getRoles()) {
			userRoles.add(role.getId());
		}

		if (0 == userRoles.size()) {
			// to avoid IndexOutOfBoundsException in EB code
			rosterSite.setUserRoles(null);
		} else {
			rosterSite.setUserRoles(userRoles);
		}

		GroupProvider groupProvider = (GroupProvider) ComponentManager
				.get(GroupProvider.class);

		Map<String, String> statusCodes = courseManagementService
				.getEnrollmentStatusDescriptions(Locale.getDefault());

		rosterSite.setEnrollmentStatusDescriptions(new ArrayList<String>(statusCodes.values()));
		
		if (null == groupProvider) {
			log.warn("no group provider installed");
		} else {
			String[] sectionIds = groupProvider.unpackId(getSite(siteId)
					.getProviderGroupId());

			List<RosterEnrollment> siteEnrollmentSets = new ArrayList<RosterEnrollment>();

			// avoid duplicates
			List<String> enrollmentSetIdsProcessed = new ArrayList<String>();

			for (String sectionId : sectionIds) {

				Section section = courseManagementService.getSection(sectionId);
				if (null == section) {
					continue;
				}

				EnrollmentSet enrollmentSet = section.getEnrollmentSet();
				if (null == enrollmentSet) {
					continue;
				}

				if (enrollmentSetIdsProcessed.contains(enrollmentSet.getEid())) {
					continue;
				}

				RosterEnrollment rosterEnrollmentSet = new RosterEnrollment();
				rosterEnrollmentSet.setId(enrollmentSet.getEid());
				rosterEnrollmentSet.setTitle(enrollmentSet.getTitle());
				siteEnrollmentSets.add(rosterEnrollmentSet);

				enrollmentSetIdsProcessed.add(enrollmentSet.getEid());
			}

			if (0 == siteEnrollmentSets.size()) {
				// to avoid IndexOutOfBoundsException in EB code
				rosterSite.setSiteEnrollmentSets(null);
			} else {
				rosterSite.setSiteEnrollmentSets(siteEnrollmentSets);
			}
		}
		return rosterSite;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<RosterMember> getEnrolledMembership(String siteId,
			String enrollmentSetId) {

		try {
			Site site = siteService.getSite(siteId);

			if (false == site.isAllowed(getCurrentUserId(),
					RosterFunctions.ROSTER_FUNCTION_VIEWENROLLMENTSTATUS)) {
				return null;
			}
		} catch (IdUnusedException e) {
			e.printStackTrace();
			return null;
		}

		List<RosterMember> enrolledMembers = new ArrayList<RosterMember>();

		// TODO fix how to get the Locale
		Map<String, String> statusCodes = courseManagementService
				.getEnrollmentStatusDescriptions(Locale.getDefault());

		Map<String, RosterMember> membership = getMembershipMapped(siteId, null);

		EnrollmentSet enrollmentSet = courseManagementService
				.getEnrollmentSet(enrollmentSetId);

		if (null == enrollmentSet) {
			return null;
		}

		for (Enrollment enrollment : courseManagementService
				.getEnrollments(enrollmentSet.getEid())) {

			RosterMember member = membership.get(enrollment.getUserId());

			RosterMember enrolledMember = new RosterMember(member.getUserId());
			enrolledMember.setCredits(enrollment.getCredits());
			enrolledMember.setDisplayId(member.getDisplayId());
			enrolledMember.setDisplayName(member.getDisplayName());
			enrolledMember.setEid(member.getEid());
			enrolledMember.setEmail(member.getEmail());
			enrolledMember.setRole(member.getRole());
			enrolledMember.setSortName(member.getSortName());
			enrolledMember.setStatus(statusCodes.get(enrollment
					.getEnrollmentStatus()));

			enrolledMembers.add(enrolledMember);
		}

		if (0 == enrolledMembers.size()) {
			// to avoid IndexOutOfBoundsException in EB code
			return null;
		}
		return enrolledMembers;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Site getSite(String siteId) {

		try {
			return siteService.getSite(siteId);
		} catch (IdUnusedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public User getUser(String userId) {
		
		if (StringUtils.isBlank(userId)) {
			return null;
		}
		
		User user = null;
		try {
			user = userDirectoryService.getUser(userId);
		} catch (UserNotDefinedException e) {

			e.printStackTrace();
		}
		return user;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getRoleTypes(String siteId) {
		
		List<String> roleTypes = new ArrayList<String>();
		try {
			Site site = siteService.getSite(siteId);
			Set<Role> roles = site.getRoles();
			
			for (Role role : roles) {
				roleTypes.add(role.getId());
			}
			
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}

		return roleTypes;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCurrentSessionUserId() {
		
		return sessionManager.getCurrentSessionUserId();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Boolean hasUserPermission(String userId, String permission,
			String siteId) {

		try {
			Site site = siteService.getSite(siteId);
			Role userRole = site.getUserRole(userId);
			
			if (null != userRole && userRole.getAllowedFunctions().contains(permission)) {
				return true;
			}
			
		} catch (IdUnusedException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/* Spring injections */
	
	public void setCourseManagementService(
			CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}
	
//	public void setFunctionManager(FunctionManager functionManager) {
//		this.functionManager = functionManager;
//	}
	
//	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
//		this.authzGroupService = authzGroupService;
//	}
	
//	public void setSecurityService(SecurityService securityService) {
//		this.securityService = securityService;
//	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void setServerConfigurationService(
			ServerConfigurationService serverConfigurationService) {
		this.serverConfigurationService = serverConfigurationService;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}
	
	public void setUserDirectoryService(
			UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}
	
}
