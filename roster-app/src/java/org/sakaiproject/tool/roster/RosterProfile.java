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

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;

public class RosterProfile extends InitializableBean {
	private static final Log log = LogFactory.getLog(RosterProfile.class);

	// Service & Bean References
	protected FilteredProfileListingBean filter;

	// Service & Bean Setters & Getters
	public FilteredProfileListingBean getFilter() {
		return filter;
	}

	public void setFilter(FilteredProfileListingBean filter) {
		this.filter = filter;
	}

	protected Participant participant;

	public String displayProfile() {
		String userId = StringUtils.trimToNull((String) FacesContext
				.getCurrentInstance().getExternalContext()
				.getRequestParameterMap().get("participantId"));
		if (userId == null) {
			log.debug("Can not display a profile for null");
			return "profileNotFound";
		}
		participant = filter.services.rosterManager.getParticipantById(userId);

		if (filter.services.userDirectoryService.getCurrentUser().getId()
				.equals(userId)) {
			// This user is looking at him/her self
			return "completeProfile";
		}

		if (participant == null || participant.getProfile() == null) {
			if (log.isDebugEnabled())
				log.debug("Can not display a missing profile for user "
						+ userId);
			return "profileNotFound";
		}
		if (participant.getProfile().getHidePrivateInfo() == null
				|| participant.getProfile().getHidePrivateInfo()) {
			if (log.isDebugEnabled())
				log.debug("Displaying the public profile for " + userId);
			return "publicProfile";
		}
		if (log.isDebugEnabled())
			log.debug("Displaying the complete profile for " + userId);
		return "completeProfile";
	}

	public Participant getParticipant() {
		return participant;
	}

	public boolean isShowCustomPhotoUnavailableForSelectedProfile() {
		if (participant == null || participant.getProfile() == null) {
			return true;
		}
		Profile profile = participant.getProfile();
		if (!filter.services.profileManager.displayCompleteProfile(profile)) {
			return true;
		}

		if (profile.isInstitutionalPictureIdPreferred() == null) {
			return true;
		}
		if (!profile.isInstitutionalPictureIdPreferred().booleanValue()
				&& (profile.getPictureUrl() == null || profile.getPictureUrl().length() < 1)) {
			return true;
		}
		return false;
	}

	public boolean isShowURLPhotoForSelectedProfile() {
		if (participant == null || participant.getProfile() == null) {
			return false;
		}
		Profile profile = participant.getProfile();
		if (filter.services.profileManager.displayCompleteProfile(profile)
				&& profile.getPictureUrl() != null
				&& profile.getPictureUrl().length() > 0) {
			return true;
		}
		return false;
	}

    public boolean isShowCustomIdPhotoForSelectedProfile()
    {
		if (participant == null || participant.getProfile() == null) {
			return false;
		}
		Profile profile = participant.getProfile();
      if (profile.isInstitutionalPictureIdPreferred() == null)
      {
        return false;
      }
      if (filter.services.profileManager.displayCompleteProfile(profile)
          && profile.isInstitutionalPictureIdPreferred().booleanValue())
      {
        return true;
      }
      return false;
    }
}
