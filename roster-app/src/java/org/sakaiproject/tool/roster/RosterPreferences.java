/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
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
	public static final String DISPLAY_NAME_COLUMN = "sortName";
	public static final String DISPLAY_ID_COLUMN = "displayId";
	public static final String ROLE_COLUMN = "role";
	public static final String EMAIL_COLUMN = "email";

	protected String sortColumn;
	protected boolean sortAscending;
	protected boolean displayNames;
	protected boolean displayProfilePhotos;

	// Keep the "return page" here, since this is a session scoped bean.  Ugh, this is so nasty.
	protected String returnPage;

	public RosterPreferences() {
		sortColumn = DISPLAY_NAME_COLUMN;
		sortAscending = true;
		displayNames = true;
	}

	public String getReturnPage() {
		return returnPage;
	}

	public void setReturnPage(String returnPage) {
		this.returnPage = returnPage;
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
