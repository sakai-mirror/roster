/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/roster/roster-component-shared/src/java/org/sakaiproject/component/app/roster/RosterManagerImpl.java $
 * $Id: RosterManagerImpl.java 1244 2005-08-17 16:06:54Z rshastri@iupui.edu $
 **********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005 The Regents of the University of Michigan, Trustees of Indiana University,
 *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package org.sakaiproject.component.app.roster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.api.common.edu.person.SakaiPerson;
import org.sakaiproject.api.common.edu.person.SakaiPersonManager;
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.authzGroup.AuthzGroup;
import org.sakaiproject.service.legacy.authzGroup.Role;
import org.sakaiproject.service.legacy.authzGroup.cover.AuthzGroupService;
import org.sakaiproject.service.legacy.security.cover.SecurityService;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.service.legacy.user.cover.UserDirectoryService;

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
    catch (IdUnusedException e)
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
    List usersByRole = new ArrayList();
    if (role != null)
    {
      AuthzGroup realm;
      try
      {
        realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
        Set userSet = realm.getUsersHasRole(role.getId());
        Iterator userSetIter = userSet.iterator();
        while (userSetIter.hasNext())
        {
          usersByRole.add((String) userSetIter.next());
        }
        if (!isInstructor())

        {
          usersByRole = getNonFERPAMembers(usersByRole);
        }

        if (usersByRole != null && usersByRole.size() > 0)
        {
          Iterator iter = usersByRole.iterator();
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
            catch (IdUnusedException e)
            {
              LOG.info("Swallow IdUnusedException");	
              LOG.info(e.getMessage(), e);
            }

          }
        }
      }
      catch (IdUnusedException e)
      {
        LOG.error(e.getMessage(), e);
      }
    }
    Collections.sort(users, ParticipantImpl.LastNameComparator);
    return users;
  }

  private Participant createParticipantByUser(User user)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("createParticipantByUser(User " + user + ")");
    }
    return new ParticipantImpl(user.getId(), user.getFirstName(), user
        .getLastName(), profileManager.getUserProfileById(user.getId()));
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
        //TODO: individual ferpa checks
        return createParticipantByUser(user);
      }
      catch (IdUnusedException e)
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
    AuthzGroup realm;
    try
    {
      realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
      users.addAll(UserDirectoryService.getUsers(realm.getUsers()));
      List userIds = new ArrayList();
      if (users == null)
      {
        return null;
      }
      Iterator userIterator = users.iterator();
      while (userIterator.hasNext())
      {
        userIds.add(((User) userIterator.next()).getId());
      }
      // Get FERPA DETAILs only for non instructors
      if (!isInstructor())
      {
        userIds = getNonFERPAMembers(userIds);
      }
      for (int i = 0; i < userIds.size(); i++)
      {
        String userId = (String) userIds.get(i);
        try
        {
          User user = UserDirectoryService.getUser(userId);
          roster.add(createParticipantByUser(user));
        }
        catch (IdUnusedException e)
        {
          LOG.debug(e.getMessage(), e);
        }
      }
    }
    catch (IdUnusedException e)
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
  private List getNonFERPAMembers(List siteUsers)
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
      if (profileManager.getAgentUuidByEnterpriseId(userId) != null)
      {
        agentUuids.add(profileManager.getAgentUuidByEnterpriseId(userId));
      }
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
      if (!siteFerpaUsersEnterpriseIds.contains(siteMember))
      {
        //Add non ferpa member
        nonFerpaUsers.add(siteMember);
      }
    }
    if (nonFerpaUsers.size() < 1)
    {
      //all the site members are ferpa users
      return null;
    }
    return nonFerpaUsers;
  }

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
