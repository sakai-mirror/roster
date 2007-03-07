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

public class RosterPreferences {
	public static final String DISLAY_NAME_COLUMN = "displayName";
	public static final String DISLAY_ID_COLUMN = "displayId";
	public static final String ROLE_COLUMN = "roleId";
	public static final String EMAIL_COLUMN = "email";

	protected String sortColumn;
	protected boolean sortAscending;
	protected boolean displaySectionColumns;
	protected boolean displayNames;
	protected boolean displayProfilePhotos;
	
	public RosterPreferences() {
		sortColumn = ROLE_COLUMN;
		sortAscending = true;
		displayNames = true;
	}
	
	public boolean isSortAscending() {
		return sortAscending;
	}
	public void setSortAscending(boolean sortAscending) {
		this.sortAscending = sortAscending;
	}
	public String getSortColumn() {
		return sortColumn;
	}
	public void setSortColumn(String sortColumn) {
		this.sortColumn = sortColumn;
	}
	public boolean isDisplaySectionColumns() {
		return displaySectionColumns;
	}
	public void setDisplaySectionColumns(boolean displaySectionColumns) {
		this.displaySectionColumns = displaySectionColumns;
	}

	public boolean isDisplayNames() {
		return displayNames;
	}

	public void setDisplayNames(boolean displayNames) {
		this.displayNames = displayNames;
	}

	public boolean isDisplayProfilePhotos() {
		return displayProfilePhotos;
	}

	public void setDisplayProfilePhotos(boolean displayProfilePhotos) {
		this.displayProfilePhotos = displayProfilePhotos;
	}	
}
