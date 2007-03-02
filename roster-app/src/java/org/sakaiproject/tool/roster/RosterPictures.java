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

import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.jsf.util.LocaleUtil;

public class RosterPictures {
	private static final Log log = LogFactory.getLog(RosterPictures.class);

	protected boolean displayProfilePhoto;

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
	
	public boolean isDisplayProfilePhoto() {
		return displayProfilePhoto;
	}
	public void setDisplayProfilePhoto(boolean displayProfilePhoto) {
		this.displayProfilePhoto = displayProfilePhoto;
	}
	
	public String getPageTitle() {
		return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				ServicesBean.MESSAGE_BUNDLE, "title_pictures");
	}

}
