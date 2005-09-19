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
import org.sakaiproject.api.kernel.tool.cover.ToolManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.legacy.realm.Realm;
import org.sakaiproject.service.legacy.realm.Role;
import org.sakaiproject.service.legacy.realm.cover.RealmService;
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

  /**
   * @param sakaiPersonManager
   */
  public void setProfileManager(ProfileManager newProfileManager)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("setProfileManager(ProfileManager " + newProfileManager + ")");
    }

    profileManager = newProfileManager;
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

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#getRoles()
   */
  public List getRoles()
  {
    LOG.debug("getRoles()");
    List roleList = new ArrayList();
    Realm realm;
    try
    {
      realm = RealmService.getRealm(getContextSiteId());
      Set roles = realm.getRoles();
      if (roles != null && roles.size() > 0)
      {
        Iterator roleIter = roles.iterator();
        while (roleIter.hasNext())
        {
          Role role = (Role) roleIter.next();
          if (role != null) roleList.add(role);
        }
      }
    }
    catch (IdUnusedException e)
    {
      LOG.error(e.getMessage(), e);
    }
    return roleList;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#getParticipantByRole(org.sakaiproject.service.legacy.realm.Role)
   */
  public List getParticipantByRole(Role role)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("getParticipantByRole(Role" + role + ")");
    }
    List users = new ArrayList();
    if (role != null)
    {
      Realm realm;
      try
      {
        realm = RealmService.getRealm(getContextSiteId());
        Set usersByRole = realm.getUsersWithRole(role.getId());
        if (usersByRole != null && usersByRole.size() > 0)
        {
          Iterator iter = usersByRole.iterator();
          while (iter.hasNext())
          {
            User user = UserDirectoryService.getUser((String) iter.next());
            users.add(new ParticipantImpl(user.getId(), user.getFirstName(),
                user.getLastName()));
          }
        }
      }
      catch (IdUnusedException e)
      {
        LOG.error(e.getMessage(), e);
      }
    }
    return users;
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
        Participant participant = new ParticipantImpl(user.getId(), user
            .getFirstName(), user.getLastName());
        return participant;
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

    Realm realm;

    try
    {
      realm = RealmService.getRealm(getContextSiteId());
      users.addAll(UserDirectoryService.getUsers(realm.getUsers()));
      for (int i = 0; i < users.size(); i++)
      {
        User user = (User) users.get(i);
        Participant participant = new ParticipantImpl(user.getId(), user
            .getFirstName(), user.getLastName());
        roster.add(participant);
      }
    }
    catch (IdUnusedException e)
    {
      LOG.debug(e.getMessage(), e);
    }

    return roster;
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
