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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.component.cover.ServerConfigurationService;

/**
 * @author rshastri
 * @version $Id: RosterManagerImpl.java 1244 2005-08-17 16:06:54Z rshastri@iupui.edu $
 */
public class RosterManagerImpl implements RosterManager
{
  private static final Log LOG = LogFactory.getLog(RosterManagerImpl.class);
  /** Dependency: ProfileManager */
  private ProfileManager profileManager;
  private SakaiPersonManager sakaiPersonManager;
  private PrivacyManager privacyManager;

  /**
   * @param sakaiPersonManager
   */
  public void setProfileManager(ProfileManager newProfileManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setProfileManager(ProfileManager " + newProfileManager + ")");
    }

    this.profileManager = newProfileManager;
  }

  /**
   * @param sakaiPersonManager
   */
  public void setSakaiPersonManager(SakaiPersonManager sakaiPersonManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setSakaiPersonManager(SakaiPersonManager "
          + sakaiPersonManager + ")");
    }

    this.sakaiPersonManager = sakaiPersonManager;
  }

  /**
   * @param PrivacyManager
   */
  public void setPrivacyManager(PrivacyManager privacyManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setPrivacyManager(PrivacyManager "
          + privacyManager + ")");
    }

    this.privacyManager = privacyManager;
  }
  
  public void init()
  {
    LOG.debug("init()");
    ; // do nothing (for now)
  }

  public void destroy()
  {
    LOG.debug("destroy()");
    ; // do nothing (for now)
  }

  //TODO: merge all users and getRoles
  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#getRoles()
   */
  public List getRoles()
  {
    LOG.debug("getRoles()");
    List roleList = new ArrayList();
    AuthzGroup realm;
    try
    {
      realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
      Set roles = realm.getRoles();
      if (roles != null && roles.size() > 0)
      {
        Iterator roleIter = roles.iterator();
        while (roleIter.hasNext())
        {
          Role role = (Role) roleIter.next();
          // show site Roles only when users are there for this Role SAK - 2068
          if (role != null && getParticipantByRole(role) != null
              && getParticipantByRole(role).size() > 0) roleList.add(role);
        }
      }
    }
    catch (GroupNotDefinedException e)
    {
      LOG.error(e.getMessage(), e);
    }
    Collections.sort(roleList);
    return roleList;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#getParticipantByRole(org.sakaiproject.service.legacy.authzGroup.Role)
   */
  public List getParticipantByRole(Role role)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getParticipantByRole(Role" + role + ")");
    }

    List users = new ArrayList();
    Set userSet = new TreeSet();
    
    if (role != null)
    {
      AuthzGroup realm;
      try
      {
        realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
        userSet = realm.getUsersHasRole(role.getId());
        
        // we only need to check privacy restrictions if the current user does not have site.upd access
        if (!isInstructor()) {
           	userSet = privacyManager.findViewable(getContextSiteId(), userSet);
        }

        if (userSet != null && userSet.size() > 0)
        {
          Iterator iter = userSet.iterator();
          while (iter.hasNext())
          {
            try
            {
              User user = UserDirectoryService.getUser((String) iter.next());
              if(user != null)
        	  {
        	  	users.add(createParticipantByUser(user));
        	  }
            }
            catch (UserNotDefinedException e)
            {
              LOG.info(e.getMessage(), e);
            }
          }
        }
      }
      catch (GroupNotDefinedException e)
      {
        LOG.error(e.getMessage(), e);
      }
    }
    Collections.sort(users, ParticipantImpl.LastNameComparator);
    return users;
  }

  private Participant createParticipantByUser(User user, Profile profile)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("createParticipantByUser(User " + user + ")");
    }
    return new ParticipantImpl(user.getId(), user.getDisplayId(), user.getEid(), user.getFirstName(), user
        .getLastName(), profile, getUserRoleTitle(user), getUserSections(user));
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#getParticipantById(java.lang.String)
   */
  public Participant getParticipantById(String participantId)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getParticipantById(String" + participantId + ")");
    }
    if (participantId != null)
    {
      try
      {
        User user = UserDirectoryService.getUser(participantId);
        Profile profile = profileManager.getUserProfileById(participantId);
        //TODO: individual ferpa checks
        return createParticipantByUser(user, profile);
      }
      catch (UserNotDefinedException e)
      {
        LOG.error(e.getMessage(), e);
      }
    }
    return null;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#getAllUsers()
   */
  public List getAllUsers()
  {
    LOG.debug("getAllUsers");
    List users = new ArrayList();
    List roster = new ArrayList();
    Set userIds = new TreeSet();
    
    AuthzGroup realm;
    try
    {
      realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
      users.addAll(UserDirectoryService.getUsers(realm.getUsers()));
      
      if (users == null)
      {
        return null;
      }
      Iterator userIterator = users.iterator();
      while (userIterator.hasNext())
      {
        userIds.add(((User) userIterator.next()).getId());
      }
      // Check privacy restrictions if user is not an instructor
      if (!isInstructor())
      {
        userIds = privacyManager.findViewable(getContextSiteId(), userIds);
      }
      
      if (userIds != null && userIds.size() > 0)
      {
    	Map<String, Profile> profiles = profileManager.getProfiles(userIds);
    	
        Iterator iter = userIds.iterator();
        while (iter.hasNext())
        {
          try
          {
            User user = UserDirectoryService.getUser((String) iter.next());
            if(user != null)
      	  	{
      	  		roster.add(createParticipantByUser(user, profiles.get(user.getId())));
      	  	}
          }
          catch (UserNotDefinedException e)
          {	
            LOG.info(e.getMessage(), e);
          }
        }
      }
    }
    catch (GroupNotDefinedException e)
    {
      LOG.debug(e.getMessage(), e);
    }

    Collections.sort(roster, ParticipantImpl.LastNameComparator);
    return roster;
  }

  /**
   * @param siteUsers
   * @return
   */
  /*private List getNonFERPAMembers(List siteUsers)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getNonFERPAMembers(List " + siteUsers + ")");
    }
    if (siteUsers == null || (siteUsers != null && siteUsers.size() < 1))
    {
      return null;
    }
    List agentUuids = new ArrayList();
    Iterator iter = siteUsers.iterator();
    while (iter.hasNext())
    {
      String userId = (String) iter.next();
	  if(!"true".equalsIgnoreCase(ServerConfigurationService.getString
			("separateIdEid@org.sakaiproject.user.api.UserDirectoryService")) && 
			profileManager.getAgentUuidByEnterpriseId(userId) != null)
      {
        agentUuids.add(profileManager.getAgentUuidByEnterpriseId(userId));
      }
	  else
		agentUuids.add(userId);
    }
    List siteFerpaUsersAgentUuids = sakaiPersonManager
        .isFerpaEnabled(agentUuids);
    if (siteFerpaUsersAgentUuids == null
        || (siteFerpaUsersAgentUuids != null && siteFerpaUsersAgentUuids.size() < 1))
    {
      LOG.debug("This site contains no FERPA user");
      return siteUsers;
    }
    List siteFerpaUsersEnterpriseIds = new ArrayList();
    Iterator iter2 = siteFerpaUsersAgentUuids.iterator();
    while (iter2.hasNext())
    {
      SakaiPerson sp = (SakaiPerson) iter2.next();
      String agentUuid = sp.getAgentUuid();
      if (profileManager.getEnterpriseIdByAgentUuid(agentUuid) != null)
      {
        siteFerpaUsersEnterpriseIds.add(profileManager
            .getEnterpriseIdByAgentUuid(agentUuid));
      }
    }
    List nonFerpaUsers = new ArrayList();
    Iterator siteUserIter = siteUsers.iterator();
    while (siteUserIter.hasNext())
    {
      String siteMember = (String) siteUserIter.next();
      try
      {
        if (!siteFerpaUsersEnterpriseIds.contains(siteMember)||isInstructor(UserDirectoryService.getUser(siteMember)))
        {
          //Add non ferpa member
          nonFerpaUsers.add(siteMember);
        }
      }
      catch (UserNotDefinedException e)
      {
        //Log and move on.
        LOG.debug("User not found");
      }
    }
    if (nonFerpaUsers.size() < 1)
    {
      //all the site members are ferpa users
      return null;
    }
    return nonFerpaUsers;
  }*/

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#isInstructor()
   */
  public boolean isInstructor()
  {
    LOG.debug("isInstructor()");
    return isInstructor(UserDirectoryService.getCurrentUser());
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#sortParticipants(java.util.List, java.lang.String, boolean)
   */
  public void sortParticipants(List participants, String sortByColumn,
      boolean ascending)
  {
    Comparator comparator;
    if (Participant.SORT_BY_LAST_NAME.equals(sortByColumn))
    {
      comparator = ParticipantImpl.LastNameComparator;
    }
    else
      if (Participant.SORT_BY_FIRST_NAME.equals(sortByColumn))
      {
        comparator = ParticipantImpl.FirstNameComparator;

      }
      else
      {
        comparator = ParticipantImpl.UserIdComparator;
      }
    Collections.sort(participants, comparator);
    if (!ascending)
    {
      Collections.reverse(participants);
    }
  }

  /**
   * Check if the given user has site.upd access
   * @param user
   * @return
   */
  private boolean isInstructor(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("isInstructor(User " + user + ")");
    }
    if (user != null)
      return SecurityService.unlock(user, "site.upd", getContextSiteId());
    else
      return false;
  }

  /**
   * @return siteId
   */
  private String getContextSiteId()
  {
    LOG.debug("getContextSiteId()");
    return ("/site/" + ToolManager.getCurrentPlacement().getContext());
  }

}
