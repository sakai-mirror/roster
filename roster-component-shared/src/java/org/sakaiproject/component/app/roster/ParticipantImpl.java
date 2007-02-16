/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-api/api/src/java/org/sakaiproject/presence/api/PresenceService.java $
 * $Id: PresenceService.java 7844 2006-04-17 13:06:02Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006 The Sakai Foundation.
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

package org.sakaiproject.component.app.roster;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.Serializable;
import java.text.Collator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.coursemanagement.api.Enrollment;
import org.sakaiproject.section.api.coursemanagement.CourseSection;
import org.sakaiproject.user.api.User;

/**
 * @author rshastri
 * 
 */
public class ParticipantImpl implements Participant, Serializable {
	private static final long serialVersionUID = 1L;

	private final static Log Log = LogFactory.getLog(ParticipantImpl.class);

	protected User user;

	protected Profile profile;

	protected String roleTitle;

	protected List<CourseSection> sections;

	protected Enrollment enrollment;

	private Map<String, CourseSection> sectionsMap;

	public static final Comparator<ParticipantImpl> DisplayNameComparator;

	public static final Comparator<ParticipantImpl> DisplayIdComparator;

	public static final Comparator<ParticipantImpl> RoleComparator;

	/**
	 * Constructs a ParticipantImpl.
	 * 
	 * @param user
	 * @param profile
	 * @param roleTitle
	 * @param enrolledSections
	 */
	public ParticipantImpl(User user, Profile profile, String roleTitle,
			List<CourseSection> enrolledSections, Enrollment enrollment) {
		this.user = user;
		this.profile = profile;
		this.roleTitle = roleTitle;
		this.sections = enrolledSections;
		this.enrollment = enrollment;
	}

	static {
		DisplayNameComparator = new Comparator<ParticipantImpl>() {
			public int compare(ParticipantImpl one, ParticipantImpl another) {
				int comparison = Collator.getInstance().compare(
						one.user.getDisplayName(),
						another.user.getDisplayName());
				return comparison == 0 ? DisplayIdComparator.compare(one,
						another) : comparison;
			}
		};

		DisplayIdComparator = new Comparator<ParticipantImpl>() {
			public int compare(ParticipantImpl one, ParticipantImpl another) {
				return Collator.getInstance().compare(one.user.getDisplayId(),
						another.user.getDisplayId());

			}
		};

		RoleComparator = new Comparator<ParticipantImpl>() {
			public int compare(ParticipantImpl one, ParticipantImpl another) {
				int comparison = Collator.getInstance().compare(one.roleTitle,
						another.roleTitle);
				return comparison == 0 ? DisplayNameComparator.compare(one,
						another) : comparison;
			}
		};
	}

	public Profile getProfile() {
		return profile;
	}

	public void setProfile(Profile profile) {
		this.profile = profile;
	}

	public String getRoleTitle() {
		return roleTitle;
	}

	public void setRoleTitle(String roleTitle) {
		this.roleTitle = roleTitle;
	}

	public Map<String, CourseSection> getSectionsMap() {
		if (sectionsMap == null) {
			sectionsMap = new HashMap<String, CourseSection>();
			if (sections != null) {
				for (Iterator<CourseSection> iter = sections.iterator(); iter
						.hasNext();) {
					CourseSection section = iter.next();
					sectionsMap.put(section.getCategory(), section);
				}
			}
		}
		return sectionsMap;
	}

	public void setSections(List<CourseSection> sections) {
		this.sections = sections;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Enrollment getEnrollment() {
		return enrollment;
	}

}
