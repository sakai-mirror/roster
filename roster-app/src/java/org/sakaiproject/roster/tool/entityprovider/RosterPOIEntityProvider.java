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
package org.sakaiproject.roster.tool.entityprovider;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.coursemanagement.api.CourseManagementService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.roster.tool.SakaiProxy;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;

/**
 * <code>EntityProvider</code> to allow Roster to export to Excel via Apache's
 * POI.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterPOIEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, RequestAware {
    
	private static final Log log = LogFactory.getLog(RosterPOIEntityProvider.class);
	
	public final static String ENTITY_PREFIX		= "roster";
	public final static String DEFAULT_ID			= ":ID:";
	
	// error messages
	public final static String MSG_INVALID_ID		= "Invalid site ID";
	public final static String MSG_NO_SESSION		= "Must be logged in";
	public final static String MSG_NO_SITE_ID		= "Must provide a site ID";
	public final static String MSG_NO_PARAMETERS	= "Must provide parameters";

	// roster views
	public final static String VIEW_OVERVIEW			= "overview";
	public final static String VIEW_GROUP_MEMBERSHIP	= "group_membership";
	public final static String VIEW_ENROLLMENT_STATUS	= "";
	
	// sort fields
	public final static String SORT_NAME		= "sortName";
	public final static String SORT_DISPLAY_ID	= "displayId";
	public final static String SORT_EMAIL		= "email";
	public final static String SORT_ROLE		= "role";
	public final static String SORT_STATUS		= "status";
	public final static String SORT_CREDITS		= "credits";
	// sort directions
	public final static int SORT_ASCENDING		= 0;
	public final static int SORT_DESCENDING		= 1;
	
	// key passed as parameters
	public final static String KEY_GROUP_ID			= "groupId";
	public final static String KEY_VIEW_TYPE		= "viewType";
	public final static String KEY_SORT_FIELD		= "sortField";
	public final static String KEY_SORT_DIRECTION	= "sortDirection";
	public final static String KEY_BY_GROUP			= "byGroup";
	public final static String KEY_FACET_NAME		= "facetName";
	public final static String KEY_FACET_USER_ID	= "facetUserId";
	public final static String KEY_FACET_EMAIL		= "facetEmail";
	public final static String KEY_FACET_ROLE		= "facetRole";
	public final static String KEY_FACET_GROUPS		= "facetGroups";
	public final static String KEY_FACET_STATUS		= "facetStatus";
	public final static String KEY_FACET_CREDITS	= "facetCredits";
		
	// defaults to use if any keys are not specified
	public final static String DEFAULT_FACET_NAME		= "Name";
	public final static String DEFAULT_FACET_USER_ID	= "User ID";
	public final static String DEFAULT_FACET_EMAIL		= "Email Address";
	public final static String DEFAULT_FACET_ROLE		= "Role";
	public final static String DEFAULT_FACET_GROUPS		= "Groups";
	public final static String DEFAULT_FACET_STATUS		= "Status";
	public final static String DEFAULT_FACET_CREDITS	= "Credits";
	public final static String DEFAULT_GROUP_ID			= "all";
	public final static String DEFAULT_VIEW_TYPE		= VIEW_OVERVIEW;
	public final static String DEFAULT_SORT_FIELD		= DEFAULT_FACET_NAME;
	public final static int DEFAULT_SORT_DIRECTION		= 0;
	public final static boolean DEFAULT_BY_GROUP		= false;
		
	// parameters we get from SakaiProxy
	private final boolean viewEmail;
	private final boolean firstNameLastName;
	
	public final static String ERROR_CREATING_FILE	= "Error creating file";
	
	private RequestGetter requestGetter;
	
	private CourseManagementService courseManagementService;
	private SessionManager sessionManager;
	private SiteService siteService;
	private UserDirectoryService userDirectoryService;
	
	public RosterPOIEntityProvider() {
		
		SakaiProxy sakaiProxy = new SakaiProxy();
		
		firstNameLastName = sakaiProxy.getFirstNameLastName();
		viewEmail = sakaiProxy.getViewEmailColumn();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getEntityPrefix() {

		return ENTITY_PREFIX;
	}
	
	@EntityCustomAction(action = "export-to-excel", viewKey = EntityView.VIEW_SHOW)
	public void exportToExcel(OutputStream out, EntityReference reference,
			Map<String, Object> parameters) {
		
		HttpServletResponse response = requestGetter.getResponse();
		
		if (null == sessionManager.getCurrentSessionUserId()) {
			
			writeOut(response, MSG_NO_SESSION);
			return;
		}
		
		String siteId = reference.getId();
		if (StringUtils.isBlank(reference.getId())
				|| DEFAULT_ID.equals(reference.getId())) {
			
			writeOut(response, MSG_NO_SITE_ID);
			return;
		}

		if (null == parameters) {
			writeOut(response, MSG_NO_PARAMETERS);
			return;
		}
		
		response.addHeader("Content-Encoding", "base64");
		response.addHeader("Content-Type", "application/vnd.ms-excel");
		// TODO fix actual filename (change date to ISO -- need JIRA).
		response.addHeader("Content-Disposition", "attachment; filename=file.xls");

		try {
			
			Site site = siteService.getSite(siteId);

			export(response.getOutputStream(), site, parameters);

		} catch (IdUnusedException e) {

			e.printStackTrace();
			writeOut(response, MSG_INVALID_ID);
			
		} catch (IOException e) {

			e.printStackTrace();
			writeOut(response, ERROR_CREATING_FILE);
		}
	}
	
	private void writeOut(HttpServletResponse response, String message) {
		
		try {
			response.getOutputStream().write(message.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void export(OutputStream out, Site site,
			Map<String, Object> parameters) throws IOException {

		String groupId		= getGroupIdValue(parameters);
		String viewType		= getViewTypeValue(parameters);
		boolean byGroup		= getByGroupValue(parameters);
		int sortDirection	= getSortDirectionValue(parameters);
		String sortField	= getSortFieldValue(parameters);

		List<List<String>> dataInRows = new ArrayList<List<String>>();

		// may add this later on instead and style it
		List<String> title = createSpreadsheetTitle(site, groupId);
		dataInRows.add(title);
		// blank line
		dataInRows.add(new ArrayList<String>());

		List<String> header = createSpreadsheetHeader(parameters, viewType);

		dataInRows.add(header);
		// blank line
		dataInRows.add(new ArrayList<String>());

		Set<Member> members = getSiteOrGroupMembers(site, groupId);

		List<RosterMember> rosterMembers = getRosterMembers(members);

		Collections.sort(rosterMembers, new RosterMemberComparator(sortField,
				sortDirection));

		// TODO roster.viewall? permission check for current user
		if (VIEW_OVERVIEW.equals(viewType)) {

			if (byGroup) {
				// TODO implement SAK-18513 when coding this up.
			} else {

				for (RosterMember member : rosterMembers) {

					List<String> row = new ArrayList<String>();

					row.add(member.name);

					row.add(member.displayId);

					if (true == viewEmail) {
						row.add(member.email);
					}

					row.add(member.role);

					dataInRows.add(row);

				}
			}

		}

		Workbook workBook = new HSSFWorkbook();
		Sheet sheet = workBook.createSheet();

		for (int i = 0; i < dataInRows.size(); i++) {

			Row row = sheet.createRow(i);

			for (int j = 0; j < dataInRows.get(i).size(); j++) {

				Cell cell = row.createCell(j);
				cell.setCellValue(dataInRows.get(i).get(j));
			}
		}

		workBook.write(out);
		out.close();
	}

	private String getSortFieldValue(Map<String, Object> parameters) {
		String sortField;
		if (null != parameters.get(KEY_SORT_FIELD)) {
			sortField = parameters.get(KEY_SORT_FIELD).toString();
		} else {
			sortField = DEFAULT_SORT_FIELD;
		}
		return sortField;
	}

	private int getSortDirectionValue(Map<String, Object> parameters) {

		if (null != parameters.get(KEY_SORT_DIRECTION)) {
			return Integer.parseInt(parameters.get(KEY_SORT_DIRECTION)
					.toString());
		} else {
			return DEFAULT_SORT_DIRECTION;
		}
		
	}

	private boolean getByGroupValue(Map<String, Object> parameters) {

		if (null != parameters.get(KEY_BY_GROUP)) {
			return Boolean
					.parseBoolean(parameters.get(KEY_BY_GROUP).toString());
		} else {
			return DEFAULT_BY_GROUP;
		}
	}

	private String getViewTypeValue(Map<String, Object> parameters) {
		
		if (null != parameters.get(KEY_VIEW_TYPE)) {
			return parameters.get(KEY_VIEW_TYPE).toString();
		} else {
			return DEFAULT_VIEW_TYPE;
		}
	}

	private String getGroupIdValue(Map<String, Object> parameters) {
		
		if (null != parameters.get(KEY_GROUP_ID)) {
			return parameters.get(KEY_GROUP_ID).toString();
		}
		return null;
	}

	private List<String> createSpreadsheetHeader(
			Map<String, Object> parameters, String viewType) {
		List<String> header = new ArrayList<String>();
		header.add(parameters.get(KEY_FACET_NAME) != null ? parameters.get(
				KEY_FACET_NAME).toString() : DEFAULT_FACET_NAME);
		header.add(parameters.get(KEY_FACET_USER_ID) != null ? parameters.get(
				KEY_FACET_USER_ID).toString() : DEFAULT_FACET_USER_ID);

		if (VIEW_OVERVIEW.equals(viewType)) {

			if (true == viewEmail) {

				header.add(parameters.get(KEY_FACET_EMAIL) != null ? parameters
						.get(KEY_FACET_EMAIL).toString() : DEFAULT_FACET_EMAIL);
			}

			header.add(parameters.get(KEY_FACET_ROLE) != null ? parameters.get(
					KEY_FACET_ROLE).toString() : DEFAULT_FACET_ROLE);

		} else if (VIEW_GROUP_MEMBERSHIP.equals(viewType)) {

			header.add(parameters.get(KEY_FACET_ROLE) != null ? parameters.get(
					KEY_FACET_ROLE).toString() : DEFAULT_FACET_ROLE);
			header.add(parameters.get(KEY_FACET_GROUPS) != null ? parameters
					.get(KEY_FACET_GROUPS).toString() : DEFAULT_FACET_GROUPS);

		} else if (VIEW_ENROLLMENT_STATUS.equals(viewType)) {

			if (true == viewEmail) {

				header.add(parameters.get(KEY_FACET_EMAIL) != null ? parameters
						.get(KEY_FACET_EMAIL).toString() : DEFAULT_FACET_EMAIL);
			}

			header.add(parameters.get(KEY_FACET_STATUS) != null ? parameters
					.get(KEY_FACET_STATUS).toString() : DEFAULT_FACET_STATUS);

			header.add(parameters.get(KEY_FACET_CREDITS) != null ? parameters
					.get(KEY_FACET_CREDITS).toString() : DEFAULT_FACET_CREDITS);
		}
		
		return header;
	}
	
	private List<String> createSpreadsheetTitle(Site site, String groupId) {
		List<String> title = new ArrayList<String>();

		if (null == groupId || DEFAULT_GROUP_ID.equals(groupId)) {
			title.add(site.getTitle());
		} else {
			if (null != site.getGroup(groupId)) {
				title.add(site.getTitle() + ": "
						+ site.getGroup(groupId).getTitle());
			}
		}
		return title;
	}

	private Set<Member> getSiteOrGroupMembers(Site site, String groupId) {
		Set<Member> members = null;
		
		if (null == groupId || DEFAULT_GROUP_ID.equals(groupId)) {
			members = site.getMembers();
		} else {
			if (null != site.getGroup(groupId)) {
				members = site.getGroup(groupId).getMembers();				
			}
		}
		return members;
	}

	private List<RosterMember> getRosterMembers(Set<Member> members) {
		List<RosterMember> rosterMembers = new ArrayList<RosterMember>();
		
		for (Member member : members) {

			try {

				User user = userDirectoryService.getUser(member.getUserId());

				RosterMember rosterMember = new RosterMember();
				rosterMember.displayId = member.getUserDisplayId();

				if (true == firstNameLastName) {
					rosterMember.name = user.getDisplayName();
				} else {
					rosterMember.name = user.getSortName();
				}

				if (true == viewEmail) {
					rosterMember.email = user.getEmail();
				} else {
					rosterMember.email = null;
				}

				rosterMember.role = member.getRole().getId();

				rosterMembers.add(rosterMember);

			} catch (UserNotDefinedException e) {

				e.printStackTrace();
			}
		}
		return rosterMembers;
	}

	private void logConfiguration(String groupId, String viewType,
			boolean byGroup, int sortDirection, String sortField,
			List<String> header) {

		StringBuilder headerStr = new StringBuilder();
		Iterator<String> iterator = header.iterator();
		while (iterator.hasNext()) {
			
			headerStr.append(iterator.next());
			
			if (iterator.hasNext()) {
				headerStr.append(" | ");
			}
		}
		
		log.info("Roster spreadsheet export configuration:\n\tgroupId="
				+ groupId + "\n\tviewType=" + viewType + "\n\tbyGroup="
				+ byGroup + "\n\tsortDirection=" + sortDirection
				+ "\n\tsortField=" + sortField + "\n\theader=" + headerStr);
	}
	
	/**
	 * <code>RosterMember</code> wraps together fields from <code>User</code>,
	 * <code>Member</code>, and <code>CourseManagementService</code> for each
	 * site member.
	 * 
	 * @author d.b.robinson@lancaster.ac.uk
	 */
	private class RosterMember {

		public String name;
		public String displayId;
		public String email;
		public String role;
		public String status;
		public String credits;
		
		// TODO
		public String groups;

		public RosterMember() {
			
		}
	}

	/**
	 * <code>Comparator</code> for <code>RosterMember</code>s.
	 * 
	 * @author d.b.robinson@lancaster.ac.uk
	 */
	private class RosterMemberComparator implements Comparator<RosterMember> {

		private String sortField;
		private int sortDirection;
		
		public RosterMemberComparator(String sortField, int sortDirection) {
			this.sortField = sortField;
			this.sortDirection = sortDirection;
		}
		
		public int compare(RosterMember a, RosterMember b) {

			RosterMember member1;
			RosterMember member2;

			if (SORT_DESCENDING == sortDirection) {
				member1 = b;
				member2 = a;
			}
			// just sort ascending by default
			else {
				member1 = a;
				member2 = b;
			}

			if (SORT_NAME.equals(sortField)) {
				return member1.name.compareToIgnoreCase(member2.name);
			} else if (SORT_DISPLAY_ID.equals(sortField)) {
				return member1.displayId.compareToIgnoreCase(member2.displayId);
			} else if (SORT_EMAIL.equals(sortField)) {
				return member1.email.compareToIgnoreCase(member2.email);
			} else if (SORT_ROLE.equals(sortField)) {
				return member1.role.compareToIgnoreCase(member2.role);
			} else if (SORT_STATUS.equals(sortField)) {
				return member1.status.compareToIgnoreCase(member2.status);
			} else if (SORT_CREDITS.equals(sortField)) {
				return member1.credits.compareToIgnoreCase(member2.credits);
			}

			log.warn("members not sorted");

			return 0;
		}
		
	}

	/* Spring injections */

	public void setCourseManagementService(
			CourseManagementService courseManagementService) {
		this.courseManagementService = courseManagementService;
	}
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}
	
	public void setUserDirectoryService(
			UserDirectoryService userDirectoryService) {
		this.userDirectoryService = userDirectoryService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRequestGetter(RequestGetter requestGetter) {
		this.requestGetter = requestGetter;
	}
}
