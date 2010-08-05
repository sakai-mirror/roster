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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.roster.api.RosterMember;
import org.sakaiproject.roster.api.SakaiProxy;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;

/**
 * <code>EntityProvider</code> to allow Roster to export to Excel via Apache's
 * POI.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterPOIEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, RequestAware {
    
	private static final Log log = LogFactory.getLog(RosterPOIEntityProvider.class);
	
	public final static String ENTITY_PREFIX		= "roster-export";
	public final static String DEFAULT_ID			= ":ID:";
	
	// error messages
	public final static String MSG_INVALID_ID			= "Invalid site ID";
	public final static String MSG_NO_SESSION			= "Must be logged in";
	public final static String MSG_NO_SITE_ID			= "Must provide a site ID";
	public final static String MSG_NO_PARAMETERS		= "Must provide parameters";
	public final static String MSG_NO_FILE_CREATED		= "Error creating file";
	public final static String MSG_NO_EXPORT_PERMISSION = "Current user does not have export permission";
	
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
	
	// misc
	public final static String FILE_EXTENSION		= ".xls";
	public final static String FILENAME_SEPARATOR	= "_";
	public final static String FILENAME_BYGROUP		= "ByGroup";
	public final static String FILENAME_UNGROUPED	= "Ungrouped";
		
	private SakaiProxy sakaiProxy;
	
	private RequestGetter requestGetter;
	
	//private UserDirectoryService userDirectoryService;
	
	public RosterPOIEntityProvider() {
				
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

		// user must be logged in
		String userId = sakaiProxy.getCurrentSessionUserId();
		if (null == userId) {

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

		try {
			
			if (sakaiProxy.hasUserPermission(userId,
					RosterFunctions.ROSTER_FUNCTION_EXPORT, siteId)) {

				Site site = sakaiProxy.getSite(siteId);
				if (null == site) {
					
				}
				export(response, site, parameters);
				
			} else {
								
				writeOut(response, MSG_NO_EXPORT_PERMISSION);
				return;
			}
		} catch (IOException e) {

			e.printStackTrace();
			writeOut(response, MSG_NO_FILE_CREATED);
		}
	}
	
	private void writeOut(HttpServletResponse response, String message) {
		
		try {
			response.getOutputStream().write(message.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void addResponseHeader(HttpServletResponse response, String filename) {
		
		response.addHeader("Content-Encoding", "base64");
		response.addHeader("Content-Type", "application/vnd.ms-excel");
		response.addHeader("Content-Disposition", "attachment; filename=" + filename);
	}
	
	private String createFilename(Site site, String groupId, String viewType, boolean byGroup) {

		String filename = site.getTitle();
		
		if (VIEW_OVERVIEW.equals(viewType)) {
			if (null != groupId || !DEFAULT_GROUP_ID.equals(groupId)) {

				if (null != site.getGroup(groupId)) {
					filename += FILENAME_SEPARATOR + site.getGroup(groupId).getTitle();
				}
			}
		} else if (VIEW_GROUP_MEMBERSHIP.equals(viewType)) {
			if (true == byGroup) {
				filename += FILENAME_SEPARATOR + FILENAME_BYGROUP;
			} else {
				filename += FILENAME_SEPARATOR + FILENAME_UNGROUPED;
			}
		}

		Date date = new Date();
		// ISO formatted date
		DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd");

		filename += FILENAME_SEPARATOR + isoFormat.format(date);

		filename = filename.replaceAll("\\W", FILENAME_SEPARATOR);
		filename += FILE_EXTENSION;

		return filename;
	}
	
	private void export(HttpServletResponse response, Site site,
			Map<String, Object> parameters) throws IOException {

		String groupId = getGroupIdValue(parameters);
		String viewType = getViewTypeValue(parameters);
		boolean byGroup = getByGroupValue(parameters);
		int sortDirection = getSortDirectionValue(parameters);
		String sortField = getSortFieldValue(parameters);

		addResponseHeader(response, createFilename(site, groupId, viewType, byGroup));

		List<List<String>> dataInRows = new ArrayList<List<String>>();

		// may add this later on instead and style it (will need to change
		// this depending on view type e.g. overview, group membership etc.
		createSpreadsheetTitle(dataInRows, site, groupId, viewType);

		List<String> header = createColumnHeader(parameters, viewType);

		List<RosterMember> rosterMembers;
		if (DEFAULT_GROUP_ID.equals(groupId)) {
			rosterMembers = sakaiProxy.getMembership(site.getId(), null);
		} else {
			rosterMembers = sakaiProxy.getMembership(site.getId(), groupId);
		}

		Collections.sort(rosterMembers, new RosterMemberComparator(sortField,
				sortDirection, sakaiProxy.getFirstNameLastName()));

		if (VIEW_OVERVIEW.equals(viewType)) {

			addOverviewRows(dataInRows, rosterMembers, header);

		} else if (VIEW_GROUP_MEMBERSHIP.equals(viewType)) {
			
			if (byGroup) {
				addGroupMembershipByGroupRows(dataInRows, rosterMembers, site, header);
			} else {
				addGroupMembershipUngroupedRows(dataInRows, rosterMembers, header);
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

		workBook.write(response.getOutputStream());
		response.getOutputStream().close();
	}

	private void addOverviewRows(List<List<String>> dataInRows,
			List<RosterMember> rosterMembers, List<String> header) {
		
		dataInRows.add(header);
		// blank line
		dataInRows.add(new ArrayList<String>());
		
		for (RosterMember member : rosterMembers) {

			List<String> row = new ArrayList<String>();

			if (sakaiProxy.getFirstNameLastName()) {
				row.add(member.getDisplayName());
			} else {
				row.add(member.getSortName());
			}
			
			row.add(member.getDisplayId());

			if (true == sakaiProxy.getViewEmailColumn()) {
				row.add(member.getEmail());
			}

			row.add(member.getRole());
			dataInRows.add(row);
		}
	}
	
	private void addGroupMembershipUngroupedRows(List<List<String>> dataInRows,
			List<RosterMember> rosterMembers, List<String> header) {
		
		dataInRows.add(header);
		// blank line
		dataInRows.add(new ArrayList<String>());
		
		for (RosterMember member : rosterMembers) {

			List<String> row = new ArrayList<String>();

			if (sakaiProxy.getFirstNameLastName()) {
				row.add(member.getDisplayName());
			} else {
				row.add(member.getSortName());
			}
			row.add(member.getDisplayId());
			row.add(member.getRole());
			row.add(member.getGroupsToString());
			
			dataInRows.add(row);
		}
	}

	private void addGroupMembershipByGroupRows(List<List<String>> dataInRows,
			List<RosterMember> rosterMembers, Site site, List<String> header) {

		for (Group group : site.getGroups()) {
			List<String> groupTitle = new ArrayList<String>();
			groupTitle.add(group.getTitle());

			dataInRows.add(groupTitle);
			// blank line
			dataInRows.add(new ArrayList<String>());

			dataInRows.add(header);
			// blank line
			dataInRows.add(new ArrayList<String>());

			for (RosterMember member : rosterMembers) {

				if (null != member.getGroups().get(group.getId())) {

					List<String> row = new ArrayList<String>();

					if (sakaiProxy.getFirstNameLastName()) {
						row.add(member.getDisplayName());
					} else {
						row.add(member.getSortName());
					}
					row.add(member.getDisplayId());
					row.add(member.getRole());
					row.add(member.getGroupsToString());
					dataInRows.add(row);
				}
			}

			// blank line
			dataInRows.add(new ArrayList<String>());
		}
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

	private List<String> createColumnHeader(Map<String, Object> parameters,
			String viewType) {
		
		List<String> header = new ArrayList<String>();
		header.add(parameters.get(KEY_FACET_NAME) != null ? parameters.get(
				KEY_FACET_NAME).toString() : DEFAULT_FACET_NAME);
		header.add(parameters.get(KEY_FACET_USER_ID) != null ? parameters.get(
				KEY_FACET_USER_ID).toString() : DEFAULT_FACET_USER_ID);

		if (VIEW_OVERVIEW.equals(viewType)) {

			if (true == sakaiProxy.getViewEmailColumn()) {

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

			if (true == sakaiProxy.getViewEmailColumn()) {

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
	
	private void createSpreadsheetTitle(List<List<String>> dataInRows,
			Site site, String groupId, String viewType) {

		List<String> title = new ArrayList<String>();
		title.add(site.getTitle());
		dataInRows.add(title);
		// blank line
		dataInRows.add(new ArrayList<String>());

		// SAK-18513
		if (VIEW_OVERVIEW.equals(viewType)) {
			if (null != groupId || !DEFAULT_GROUP_ID.equals(groupId)) {

				if (null != site.getGroup(groupId)) {
					List<String> groupTitle = new ArrayList<String>();
					groupTitle.add(site.getGroup(groupId).getTitle());
					dataInRows.add(groupTitle);
					// blank line
					dataInRows.add(new ArrayList<String>());
				}
			}
		}
	}

	@SuppressWarnings("unused")
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
	 * <code>Comparator</code> for <code>RosterMember</code>s.
	 * 
	 * @author d.b.robinson@lancaster.ac.uk
	 */
	private class RosterMemberComparator implements Comparator<RosterMember> {

		private String sortField;
		private int sortDirection;
		private boolean firstNameLastName;
		
		public RosterMemberComparator(String sortField, int sortDirection,
				boolean firstNameLastName) {
			
			this.sortField = sortField;
			this.sortDirection = sortDirection;

			this.firstNameLastName = firstNameLastName;
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
				if (firstNameLastName) {
					return member1.getDisplayName().compareToIgnoreCase(
							member2.getDisplayName());
				} else {
					return member1.getSortName().compareToIgnoreCase(
							member2.getSortName());
				}
			} else if (SORT_DISPLAY_ID.equals(sortField)) {
				return member1.getDisplayId().compareToIgnoreCase(
						member2.getDisplayId());
			} else if (SORT_EMAIL.equals(sortField)) {
				return member1.getEmail().compareToIgnoreCase(
						member2.getEmail());
			} else if (SORT_ROLE.equals(sortField)) {
				return member1.getRole().compareToIgnoreCase(member2.getRole());
			} else if (SORT_STATUS.equals(sortField)) {
				return member1.getStatus().compareToIgnoreCase(
						member2.getStatus());
			} else if (SORT_CREDITS.equals(sortField)) {
				return member1.getCredits().compareToIgnoreCase(
						member2.getCredits());
			}

			log.warn("members not sorted");

			return 0;
		}
		
	}

	/* Spring injections */

	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
		
	/**
	 * {@inheritDoc}
	 */
	public void setRequestGetter(RequestGetter requestGetter) {
		this.requestGetter = requestGetter;
	}
}
