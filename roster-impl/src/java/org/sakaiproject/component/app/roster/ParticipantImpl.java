/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/presence/trunk/presence-api/api/src/java/org/sakaiproject/presence/api/PresenceService.java $
 * $Id: PresenceService.java 7844 2006-04-17 13:06:02Z ggolden@umich.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.component.app.roster;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.user.api.User;

/**
 * @author rshastri
 * 
 */
public class ParticipantImpl implements Participant, Serializable {
	private static final long serialVersionUID = 1L;

	private final static Log log = LogFactory.getLog(ParticipantImpl.class);

	protected User user;
	protected Profile profile;
	protected String roleTitle;

	/**
	 * Constructs a ParticipantImpl.
	 * 
	 * @param user
	 * @param profile
	 * @param roleTitle
	 */
	public ParticipantImpl(User user, Profile profile, String roleTitle) {
		this.user = user;
		this.profile = profile;
		this.roleTitle = roleTitle;
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

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isProfilePhotoPublic() {
		if(profile == null) return false;
		boolean hidden = isTrue(profile.getHidePrivateInfo()) || isTrue(profile.getHidePublicInfo());
		return ! hidden;
	}

	public boolean isOfficialPhotoPreferred() {
		if(profile == null) return false;
		return isTrue(profile.isInstitutionalPictureIdPreferred());
	}

	public boolean isOfficialPhotoPublicAndPreferred() {
		if(profile == null) return false;
		if( ! isProfilePhotoPublic()) return false;
		return isOfficialPhotoPreferred();
	}
	
	private boolean isTrue(Boolean bool) {
		if(bool == null) return false;
		return bool.booleanValue();
	}

}
