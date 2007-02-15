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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.faces.application.Application;
import javax.faces.component.UIColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.custom.sortheader.HtmlCommandSortHeader;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.coursemanagement.CourseSection;

public class RosterOverview extends InitializableBean {
	private static final Log log = LogFactory.getLog(RosterOverview.class);

	private static final String DISPLAY_ROSTER_PRIVACY_MSG = "roster.privacy.display";
	private static final String SECTION_COLUMN_PREFIX = "roster_section_cat_";
	
	// Service & Bean References
	protected FilteredProfileListingBean filter;

	// Service & Bean Setters & Getters
	public FilteredProfileListingBean getFilter() {
		return filter;
	}
	public void setFilter(FilteredProfileListingBean filter) {
		this.filter = filter;
	}

	// UI method calls
	
	public boolean isRenderModifyMembersInstructions() {
		return filter.services.rosterManager.currentUserHasSiteUpdatePerm();
	}

	/**
	 * Determine whether privacy message should be displayed. Will be shown if
	 * roster.privacy.display in sakai.properties is "true" and the user does
	 * not have roster.viewhidden permission
	 * 
	 * @return
	 */
	public boolean isRenderPrivacyMessage() {
		String msgEnabled = ServerConfigurationService.getString(DISPLAY_ROSTER_PRIVACY_MSG);
		if (msgEnabled != null && msgEnabled.equalsIgnoreCase("true")
				&& ! filter.services.rosterManager.currentUserHasViewHiddenPerm()) {
			return true;
		} else {
			return false;
		}
	}

	public HtmlDataTable getRosterDataTable() {
		return null;
	}
	
	public void setRosterDataTable(HtmlDataTable rosterDataTable) {
		Set usedCategories = getUsedCategories();
		
		// Do we need to build the table?
		if (rosterDataTable.findComponent(SECTION_COLUMN_PREFIX + "0") == null) {
			Application app = FacesContext.getCurrentInstance().getApplication();

			// Can this user see section memberships?
			boolean displaySectionMemberships = filter.services.rosterManager.currentUserHasViewSectionMembershipsPerm();
			if(! displaySectionMemberships) return;

			boolean displayStatusAndCredits = filter.isDisplayEnrollmentDetails();
			// Add status and credits columns, if necessary
			if(displayStatusAndCredits) {
				// Status Column
				UIColumn statusCol = new UIColumn();
				statusCol.setId("status");

                HtmlCommandSortHeader statusSortHeader = new HtmlCommandSortHeader();
                statusSortHeader.setId("status");
                statusSortHeader.setRendererType("org.apache.myfaces.SortHeader");
                statusSortHeader.setArrow(true);
                statusSortHeader.setColumnName("status");

				HtmlOutputText statusHeaderText = new HtmlOutputText();
				statusHeaderText.setId("statusHdrTxt");
				statusHeaderText.setValue(LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(), MESSAGE_BUNDLE, "facet_status"));

                statusSortHeader.getChildren().add(statusHeaderText);
                statusCol.setHeader(statusSortHeader);

				HtmlOutputText statusContents = new HtmlOutputText();
				statusContents.setId("status_cell");
				statusContents.setValueBinding("value",
					app.createValueBinding("#{participant.status}"));
				statusCol.getChildren().add(statusContents);
				rosterDataTable.getChildren().add(statusCol);

				// Credits Column
				UIColumn creditsCol = new UIColumn();
				creditsCol.setId("credits");

                HtmlCommandSortHeader creditsSortHeader = new HtmlCommandSortHeader();
                creditsSortHeader.setId("credits");
                creditsSortHeader.setRendererType("org.apache.myfaces.SortHeader");
                creditsSortHeader.setArrow(true);
                creditsSortHeader.setColumnName("credits");

				HtmlOutputText creditsHeaderText = new HtmlOutputText();
				creditsHeaderText.setId("creditsHdrTxt");
				creditsHeaderText.setValue(LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(), MESSAGE_BUNDLE, "facet_credits"));

                creditsSortHeader.getChildren().add(creditsHeaderText);
                creditsCol.setHeader(creditsSortHeader);

				HtmlOutputText creditsContents = new HtmlOutputText();
				creditsContents.setId("credits_cell");
				creditsContents.setValueBinding("value",
					app.createValueBinding("#{participant.credits}"));
				creditsCol.getChildren().add(creditsContents);
				rosterDataTable.getChildren().add(creditsCol);
			}
			
			// Add columns for each category. Be sure to create unique IDs
			// for all child components.
			int colPos = 0;
			for (Iterator iter = usedCategories.iterator(); iter.hasNext(); colPos++) {
				String category = (String)iter.next();
				String categoryName = filter.services.cmService.getSectionCategoryDescription(category);

				UIColumn col = new UIColumn();
				col.setId(SECTION_COLUMN_PREFIX + colPos);

                HtmlCommandSortHeader sortHeader = new HtmlCommandSortHeader();
                sortHeader.setId(SECTION_COLUMN_PREFIX + "sorthdr_" + colPos);
                sortHeader.setRendererType("org.apache.myfaces.SortHeader");
                sortHeader.setArrow(true);
                sortHeader.setColumnName(category);

				HtmlOutputText headerText = new HtmlOutputText();
				headerText.setId(SECTION_COLUMN_PREFIX + "hdr_" + colPos);
				headerText.setValue(categoryName);

                sortHeader.getChildren().add(headerText);
                col.setHeader(sortHeader);

				HtmlOutputText contents = new HtmlOutputText();
				contents.setId(SECTION_COLUMN_PREFIX + "cell_" + colPos);
				contents.setValueBinding("value",
					app.createValueBinding("#{participant.sectionsMap['" + category + "'].title}"));
				col.getChildren().add(contents);
				rosterDataTable.getChildren().add(col);
			}
		}
	}
	
	/**
	 * Gets the categories (including the null category for groups) that are currently
	 * being used in this site context.
	 * 
	 * @param categories
	 * @param sections
	 * @return
	 */
	protected Set<String> getUsedCategories() {
		Set<String> used = new HashSet<String>();
		List sections = filter.services.sectionAwareness.getSections(getSiteContext());
		for(Iterator iter = sections.iterator(); iter.hasNext();) {
			CourseSection section = (CourseSection)iter.next();
			used.add(StringUtils.trimToNull(section.getCategory()));
		}
		return used;
	}

}
