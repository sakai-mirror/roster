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

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.faces.event.ActionEvent;

import org.sakaiproject.api.app.roster.Participant;

public abstract class BaseRosterPageBean {

	public abstract String getPageTitle();
	public abstract boolean isExportablePage();
	public abstract void export(ActionEvent event);
	
	// Static comparators
	public static final Comparator<Participant> sortNameComparator;
	public static final Comparator<Participant> displayIdComparator;
	public static final Comparator<Participant> emailComparator;
	public static final Comparator<Participant> roleComparator;
	
	static {
		sortNameComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				int comparison = Collator.getInstance().compare(
						one.getUser().getSortName(),
						another.getUser().getSortName());
				return comparison == 0 ? displayIdComparator.compare(one,
						another) : comparison;
			}
		};

		displayIdComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				return Collator.getInstance().compare(one.getUser().getDisplayId(),
						another.getUser().getDisplayId());
			}
		};

		emailComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				String email1 = one.getUser().getEmail();
				String email2 = another.getUser().getEmail();
				if(email1 != null && email2 == null) {
					return 1;
				}
				if(email1 == null && email2 != null) {
					return -1;
				}
				if(email1 == null && email2 == null) {
					return sortNameComparator.compare(one, another);
				}
				int comparison = Collator.getInstance().compare(one.getUser().getEmail(),
						another.getUser().getEmail());
				return comparison == 0 ? sortNameComparator.compare(one,
						another) : comparison;
			}
		};

		roleComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				int comparison = Collator.getInstance().compare(one.getRoleTitle(),
						another.getRoleTitle());
				return comparison == 0 ? sortNameComparator.compare(one,
						another) : comparison;
			}
		};
	}

	// Service & Bean References
	protected FilteredParticipantListingBean filter;
	public FilteredParticipantListingBean getFilter() {
		return filter;
	}
	public void setFilter(FilteredParticipantListingBean filter) {
		this.filter = filter;
	}
	protected RosterPreferences prefs;
	public void setPrefs(RosterPreferences prefs) {
		this.prefs = prefs;
	}

	// Utility methods
	protected String getSiteReference() {
		return filter.services.siteService.siteReference(getSiteContext());
	}
	
	protected String getSiteContext() {
		return filter.services.toolManager.getCurrentPlacement().getContext();
	}

	public List<Participant> getParticipants() {
		List<Participant> participants = filter.getParticipants();
		if (participants != null && participants.size() >= 1) {
			Collections.sort(participants, getComparator());
			if(!prefs.sortAscending) {
				Collections.reverse(participants);
			}
		}
		return participants;
	}

	protected Comparator<Participant> getComparator() {
		String sortColumn = prefs.sortColumn;
		Comparator<Participant> comparator;
		if (Participant.SORT_BY_ID.equals(sortColumn)) {
			comparator = displayIdComparator;
		} else if (Participant.SORT_BY_EMAIL.equals(sortColumn)) {
			comparator = emailComparator;
		} else if(Participant.SORT_BY_ROLE.equals(sortColumn)) {
			comparator = roleComparator;
		} else {
			// Default to the sort name
			comparator = sortNameComparator;
		}
		return comparator;
	}

	// UI logic
	protected Boolean renderOfficialPhotos;
	protected Boolean renderStatusLink;
	protected Boolean renderPicturesLink;
	protected Boolean renderProfileLinks;

	public boolean isRenderStatusLink() {
		if(renderStatusLink == null) {
			renderStatusLink = ! filter.getViewableEnrollableSections().isEmpty();
		}
		return renderStatusLink.booleanValue();
	}

	public boolean isRenderPicturesLink() {
		if(renderPicturesLink == null) {
			renderPicturesLink = filter.services.rosterManager.isOfficialPhotosViewable() || filter.services.rosterManager.isProfilesViewable();
		}
		return renderPicturesLink.booleanValue();
	}

	public boolean isRenderProfileLinks() {
		if(renderProfileLinks == null) {
			renderProfileLinks = filter.services.rosterManager.isProfilesViewable();
		}
		return renderProfileLinks.booleanValue();
	}

	public boolean isOfficialPhotosAvailableToCurrentUser() {
		if(renderOfficialPhotos == null) {
			renderOfficialPhotos = filter.services.rosterManager.isOfficialPhotosViewable();
		}
		return renderOfficialPhotos.booleanValue();
	}

}
