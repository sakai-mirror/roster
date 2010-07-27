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
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RequestAware;
import org.sakaiproject.entitybroker.entityprovider.extension.RequestGetter;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.SessionManager;

/**
 * <code>EntityProvider</code> to allow Roster to export to Excel via Apache's
 * POI.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterPOIEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, RequestAware {
    
	private static final Log log = LogFactory.getLog(RosterPOIEntityProvider.class);
	
	public final static String ENTITY_PREFIX	= "roster";
	public final static String DEFAULT_ID		= ":ID:";
	
	public final static String MSG_INVALID_ID	= "Invalid site ID";
	public final static String MSG_NO_SESSION	= "Must be logged in";
	public final static String MSG_NO_SITE_ID	= "Must provide a site ID";
	
	public final static String KEY_GROUP_ID = "groupId";
	public final static String KEY_GROUPED = "grouped";
	
	public final static String ERROR_CREATING_FILE	= "Error creating file";
	
	private RequestGetter requestGetter;
	
	private SessionManager sessionManager;
	private SiteService siteService;
	
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

		String groupId = null;
		if (null != parameters && null != parameters.get(KEY_GROUP_ID)) {
			groupId = parameters.get(KEY_GROUP_ID).toString();
		}

		boolean grouped = false;
		if (null != parameters && null != parameters.get(KEY_GROUPED)) {
			grouped = Boolean.parseBoolean(parameters.get(KEY_GROUPED)
					.toString());
		}

		List<String> header = new ArrayList<String>(4);
		if (null != parameters) {
			header.add(parameters.get("facetName") != null ? parameters.get(
					"facetName").toString() : "Name");
			header.add(parameters.get("facetUserId") != null ? parameters.get(
					"facetUserId").toString() : "User ID");
			header.add(parameters.get("facetRole") != null ? parameters.get(
					"facetRole").toString() : "Role");
			header.add(parameters.get("facetGroups") != null ? parameters.get(
					"facetGroups").toString() : "Groups");
		}

		List<List<String>> dataInRows = new ArrayList<List<String>>();

		dataInRows.add(header);

		if (grouped) {
			// TODO implement SAK-18513 when coding this up.
		} else {

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

	/* Spring injections */
	
	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRequestGetter(RequestGetter requestGetter) {
		this.requestGetter = requestGetter;
	}
}
