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
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.jsf.util.LocaleUtil;
import org.sakaiproject.section.api.coursemanagement.CourseSection;

public class RosterStatus extends InitializableBean {
	private static final Log log = LogFactory.getLog(RosterStatus.class);

	public static final Comparator<Participant> enrollmentStatusComparator;
	public static final Comparator<Participant> enrollmentCreditsComparator;

	static {
		enrollmentStatusComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				String status1 = one.getEnrollmentStatus();
				String status2 = another.getEnrollmentStatus();
				if(status1 != null && status2 == null) {
					return 1;
				}
				if(status1 == null && status2 != null) {
					return -1;
				}
				if(status1 == null && status2 == null) {
					return RosterOverview.sortNameComparator.compare(one, another);
				}
				int comparison = Collator.getInstance().compare(one.getEnrollmentStatus(),
						another.getEnrollmentStatus());
				return comparison == 0 ? RosterOverview.sortNameComparator.compare(one,
						another) : comparison;
			}
		};

		enrollmentCreditsComparator = new Comparator<Participant>() {
			public int compare(Participant one, Participant another) {
				String credits1 = one.getEnrollmentCredits();
				String credits2 = another.getEnrollmentCredits();
				if(credits1 != null && credits2 == null) {
					return 1;
				}
				if(credits1 == null && credits2 != null) {
					return -1;
				}
				if(credits1 == null && credits2 == null) {
					return RosterOverview.sortNameComparator.compare(one, another);
				}
				int comparison = Collator.getInstance().compare(one.getEnrollmentCredits(),
						another.getEnrollmentCredits());
				return comparison == 0 ? RosterOverview.sortNameComparator.compare(one,
						another) : comparison;
			}
		};

	}
	// Service & Bean References
	protected FilteredStatusListingBean filter;

	// Service & Bean Setters & Getters
	public FilteredStatusListingBean getFilter() {
		return filter;
	}
	public void setFilter(FilteredStatusListingBean filter) {
		this.filter = filter;
	}
	
	public List<Participant> getParticipants() {
		List<Participant> participants = filter.getParticipants();
		if (participants != null && participants.size() >= 1) {
			Collections.sort(participants, getComparator());
			if(!filter.prefs.sortAscending) {
				Collections.reverse(participants);
			}
		}
		return participants;
	}
	
	protected Comparator<Participant> getComparator() {
		String sortColumn = filter.prefs.sortColumn;

		Comparator<Participant> comparator;

		if (Participant.SORT_BY_ID.equals(sortColumn)) {
			comparator = RosterOverview.displayIdComparator;
		} else if (Participant.SORT_BY_NAME.equals(sortColumn)) {
			comparator = RosterOverview.sortNameComparator;
		} else if (Participant.SORT_BY_EMAIL.equals(sortColumn)) {
			comparator = RosterOverview.emailComparator;
		} else if (Participant.SORT_BY_STATUS.equals(sortColumn)) {
			comparator = enrollmentStatusComparator;
		} else if (Participant.SORT_BY_CREDITS.equals(sortColumn)) {
			comparator = enrollmentCreditsComparator;
		} else {
			comparator = RosterOverview.sortNameComparator;
		}
		return comparator;
	}
	
	public String getPageTitle() {
		return LocaleUtil.getLocalizedString(FacesContext.getCurrentInstance(),
				InitializableBean.MESSAGE_BUNDLE, "title_status");
	}

}
