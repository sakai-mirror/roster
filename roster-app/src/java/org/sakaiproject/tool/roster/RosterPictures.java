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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.jsf.util.LocaleUtil;

public class RosterPictures implements RosterPageBean {
	private static final Log log = LogFactory.getLog(RosterPictures.class);

	protected FilteredProfileListingBean filter;
	public FilteredProfileListingBean getFilter() {
		return filter;
	}
	public void setFilter(FilteredProfileListingBean filter) {
		this.filter = filter;
	}


	protected RosterPreferences prefs;
	public void setPrefs(RosterPreferences prefs) {
		this.prefs = prefs;
	}

	public List<Participant> getParticipants() {
		List<Participant> participants = filter.getParticipants();
		if (participants != null && participants.size() >= 1) {
			Collections.sort(participants, RosterOverview.sortNameComparator);
			if(!prefs.sortAscending) {
				Collections.reverse(participants);
			}
		}
		return participants;
	}
		
	public String getPageTitle() {
		return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "title_pictures");
	}
	public boolean isExportablePage() {
		return false;
	}
	public void export(ActionEvent event) {
		// Do nothing
	}
	
	public void hideNames(ActionEvent event) {
		prefs.setDisplayNames(false);
	}

	public void showNames(ActionEvent event) {
		prefs.setDisplayNames(true);
	}

	public boolean isRenderStatus() {
		return ! filter.getViewableEnrollableSections().isEmpty();
	}

	/**
	 * JSF (at least myfaces) doesn't translate strings to boolean values for radio
	 * buttons properly.  As a workaround, we build the select items manually.
	 * 
	 * @return
	 */
	public List<SelectItem> getPhotoSelectItems() {
		List<SelectItem> items = new ArrayList<SelectItem>(2);
		items.add(new SelectItem(Boolean.FALSE, LocaleUtil.getLocalizedString(
				FacesContext.getCurrentInstance(), ServicesBean.MESSAGE_BUNDLE, "roster_official_photos")));
		items.add(new SelectItem(Boolean.TRUE, LocaleUtil.getLocalizedString(
				FacesContext.getCurrentInstance(), ServicesBean.MESSAGE_BUNDLE, "roster_profile_photos")));
		return items;
	}
}
