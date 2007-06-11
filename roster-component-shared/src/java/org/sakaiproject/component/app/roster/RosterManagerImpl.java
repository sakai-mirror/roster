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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.api.app.roster.RosterFunctions;
import org.sakaiproject.api.privacy.PrivacyManager;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.GroupNotDefinedException;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.authz.cover.FunctionManager;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
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
  private PrivacyManager privacyManager;
  
  private static final String SITE_UPD_PERM = "site.upd";

  /**
   * @param sakaiProfileManager
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
    
    Collection registered = FunctionManager.getInstance().getRegisteredFunctions(RosterFunctions.ROSTER_FUNCTION_PREFIX);
    if(!registered.contains(RosterFunctions.ROSTER_FUNCTION_EXPORT)) {
        FunctionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_EXPORT);
    }

    if(!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWALL)) {
        FunctionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWALL);
    }

    if(!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN)) {
        FunctionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN);
    }

    if(!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALID)) {
        FunctionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALID);
    }
    
    if(!registered.contains(RosterFunctions.ROSTER_FUNCTION_VIEWSECTION)) {
        FunctionManager.registerFunction(RosterFunctions.ROSTER_FUNCTION_VIEWSECTION);
    }
  }

  public void destroy()
  {
    LOG.debug("destroy()");
    ; // do nothing (for now)
  }

  private Participant createParticipantByUser(User user, Profile profile)
  {
    if (LOG.isDebugEnabled())
    {
      LOG.debug("createParticipantByUser(User " + user + ")");
    }
    return new ParticipantImpl(user.getId(), user.getDisplayId(), user.getEid(), user.getFirstName(), user
        .getLastName(), profile, getUserRoleTitle(user), getUserSections(user), user.getEmail());
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
        LOG.error("getParticipantById: " + e.getMessage(), e);
      }
    }
    return null;
  }

  /**
   * retrieve the list of participants filtered by the passed filter 
   * (such as Show All Sections) that are viewable by the current user
   * @param filter
   * @return List   
   */
  public List getRoster(String filter)
  {
    LOG.debug("getRoster");
    
    /* we have several possibilities to return:
    1. All users
    2. All users in current user's section(s)  
    3. All users in the selected section
     - note: privacy is considered for each option
  */
    
    if (filter == null)
    	return null;
    
    User currentUser = UserDirectoryService.getCurrentUser();
    boolean userHasViewAllPerm = userHasPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWALL);
    boolean userHasViewSectionPerm = userHasPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWSECTION);
    
    List roster = new ArrayList();
    Set userIds = new HashSet();
    
      // view all sections
      if (filter.equalsIgnoreCase(VIEW_ALL_SECT))
      {   
    	  // first check the current user's authorization for this option - must have roster.viewall
    	  if (!userHasViewAllPerm)
    		  return null;
    	  
    	  userIds = getUsersInAllSections();
      }
      else if (filter.equalsIgnoreCase(VIEW_MY_SECT))
      {
    	  // first check that current user has roster.viewsection permission
    	  if (!userHasViewSectionPerm)
    		  return null;
    	  
    	  userIds = getUsersInCurrentUsersSections(currentUser);	  
      }
      else if (filter.equalsIgnoreCase(VIEW_NO_SECT))
      {
    	  if (userHasViewAllPerm || (!siteHasSections() && (userHasViewAllPerm || userHasViewSectionPerm)))
    	  {
    		  userIds = getUsersInAllSections();
    	  }
    	  else if (userHasViewSectionPerm)
    	  {
    		  userIds = getUsersInCurrentUsersSections(currentUser);
    	  }  
      }
      else   // we will assume the filter is a specific section
      {
    	  
    	  // first check that current user has roster.viewsection or roster.viewall permission
    	  if (!userHasViewAllPerm && !userHasViewSectionPerm)
    		  return null;
    	  
    	  Set usersInSection = getUsersInSection(filter);
    	  if (usersInSection == null || usersInSection.isEmpty())
    		  return null;
    	  
    	  // if user does not have viewall perm, must check to see if member of section
    	  if (!userHasViewAllPerm)
    	  {
    		  if (!usersInSection.contains(currentUser.getId()))
    			  return null;
    	  }

    	  userIds = usersInSection;
      }
      
      if (userIds == null || userIds.isEmpty())
    	  return null;
        
      // Check for privacy restrictions
      if (!userHasPermission(currentUser, RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN))
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
            LOG.info("getRoster: " + e.getMessage(), e);
          }
        }
      }

    return roster;
  }
  
  /**
   * Get the userids of all the users in the site
   * @return
   */
  private Set getUsersInAllSections()
  {
	  List users = new ArrayList();
	  Set userIds = new HashSet();

	  AuthzGroup realm;
	  try
	  {
		  realm = AuthzGroupService.getAuthzGroup(getContextSiteId());
		  users.addAll(UserDirectoryService.getUsers(realm.getUsers()));

		  if (users == null)
			  return null;

		  Iterator userIterator = users.iterator();
		  while (userIterator.hasNext())
		  {
			  userIds.add(((User) userIterator.next()).getId());
		  }
	  }
	  catch (GroupNotDefinedException e)
	  {
		  LOG.error("getUsersInAllSections: " + e.getMessage(), e);
	  }
	  
	  return userIds;
  }
  
  /**
   * Returns the userids of the users who are in the current user's section(s)
   * @param currentUser
   * @return
   */
  private Set getUsersInCurrentUsersSections(User currentUser)
  {
	  Set userIds = new HashSet();
	  List userSections = getUserSections(currentUser); 
	  Iterator sectionIter = userSections.iterator();
	  while (sectionIter.hasNext())
	  {
		  String section = (String)sectionIter.next();
		  Set usersInSection = getUsersInSection(section);
		  if (usersInSection != null && !usersInSection.isEmpty())
		  {
			  Iterator userIter = usersInSection.iterator();
			  while (userIter.hasNext())
			  {
				  String userInSection = (String)userIter.next();
				  if (!userIds.contains(userInSection))
					  userIds.add(userInSection);
			  }
		  } 
	  }	
	  
	  return userIds;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasExportPerm()
   */
  public boolean currentUserHasExportPerm()
  {
	  return userHasPermission(UserDirectoryService.getCurrentUser(), RosterFunctions.ROSTER_FUNCTION_EXPORT);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasViewOfficialIdPerm()
   */
  public boolean currentUserHasViewOfficialIdPerm()
  {
	  return userHasPermission(UserDirectoryService.getCurrentUser(), RosterFunctions.ROSTER_FUNCTION_VIEWOFFICIALID);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasViewHiddenPerm()
   */
  public boolean currentUserHasViewHiddenPerm()
  {
	  return userHasPermission(UserDirectoryService.getCurrentUser(), RosterFunctions.ROSTER_FUNCTION_VIEWHIDDEN);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasViewSectionPerm()
   */
  public boolean currentUserHasViewSectionPerm()
  {
	  return userHasPermission(UserDirectoryService.getCurrentUser(), RosterFunctions.ROSTER_FUNCTION_VIEWSECTION);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasViewAllPerm()
   */
  public boolean currentUserHasViewAllPerm()
  {
	  return userHasPermission(UserDirectoryService.getCurrentUser(), RosterFunctions.ROSTER_FUNCTION_VIEWALL);
  }
  
  /*
   * (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#currentUserHasSiteUpdatePerm()
   */
  public boolean currentUserHasSiteUpdatePerm()
  {
	  return userHasPermission(UserDirectoryService.getCurrentUser(), SITE_UPD_PERM);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.api.app.roster.RosterManager#sortParticipants(java.util.List, java.lang.String, boolean)
   */
  public void sortParticipants(List participants, String sortByColumn,
      boolean ascending)
  {
	if (participants == null || participants.size() <= 1)
		return;
	
    Comparator comparator;
    if (Participant.SORT_BY_LAST_NAME.equals(sortByColumn))
    {
      comparator = ParticipantImpl.LastNameComparator;
    }
    else if (Participant.SORT_BY_FIRST_NAME.equals(sortByColumn))
    {
      comparator = ParticipantImpl.FirstNameComparator;
    }
    else if (Participant.SORT_BY_SECTIONS.equals(sortByColumn))
    {
      comparator = ParticipantImpl.SectionsComparator;
    }
    else if (Participant.SORT_BY_ID.equals(sortByColumn))
    {
      comparator = ParticipantImpl.UserIdComparator;
    }
    // oncourse
    else if (Participant.SORT_BY_EMAIL.equals(sortByColumn))
    {
      comparator = ParticipantImpl.EmailComparator;
    }
    // end oncourse
    else
    {
    	comparator = ParticipantImpl.RoleComparator;
    }
    
    Collections.sort(participants, comparator);
    if (!ascending)
    {
      Collections.reverse(participants);
    }
  }
  
  
  
  /**
   * Check if given user has the given permission
   * @param user
   * @param permissionName
   * @return boolean
   */
  private boolean userHasPermission(User user, String permissionName)
  {
	  if (user != null)
		  return SecurityService.unlock(user, permissionName, getContextSiteId());
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
  
  /**
   * 
   * @param user
   * @return
   */
  private String getUserRoleTitle(User user) {
	  try
	  {
		  return AuthzGroupService.getUserRole(user.getId(),getContextSiteId());
	  }
	  catch(Exception e)
	  {
		  LOG.error("Exception", e);
	  }
	  return "";
  }
 
  /**
   * Determine if sectioning exists in this site
   * @return 
   */
  public boolean siteHasSections()
  {
	  List sections = getAllSections();
	  if (sections != null && !sections.isEmpty())
		  return true;
	  
	  return false;
  }
  
  /**
   * returns sections that user has permission to view
   * @return
   */
  public List getViewableSectionsForCurrentUser()
  {
	  if (currentUserHasViewAllPerm())
	  {
		  return getAllSections();
	  } 
	  else if (currentUserHasViewSectionPerm())
	  {  
		  return getUserSections(UserDirectoryService.getCurrentUser());
	  }
	  
	  return null;
  }
  
  /**
   * Returns the userids of the users in the given section
   * @param sectionName
   * @return
   */
  private Set getUsersInSection(String sectionName)
  {
	  Set usersInSection = new HashSet();
	  try
	  {
		  Site currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext()); 

		  Collection groups = currentSite.getGroups();
		  for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
		  {
			  Group currentGroup = (Group) groupIterator.next(); 
			  if (currentGroup.getTitle().equalsIgnoreCase(sectionName))
			  {
				  Set userSet = currentGroup.getUsers();
				  for (Iterator setIter = userSet.iterator(); setIter.hasNext();)
				  {
					  String userId = (String)setIter.next();
					  usersInSection.add(userId);
				  }

				  return usersInSection;
			  }
		  }
	  } 
	  catch (Exception e) 
	  {
		  LOG.error("getUsersInSection" + e.getMessage(), e);
	  }

	  return usersInSection;
  }
  
  /**
   * 
   * @param user
   * @return
   */
  private List getUserSections(User user) 
  {
	  List sections = new ArrayList();
	  try
	  {
		  Site currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());

		  Collection groups = currentSite.getGroupsWithMember(user.getId());
		  for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
	      {
	        Group currentGroup = (Group) groupIterator.next(); 
        	sections.add(currentGroup.getTitle());
	      }
		  
		  if (sections.size() > 1)
		  {
			  Collections.sort(sections);
		  }
	  } 
	  catch (Exception e) 
	  {
		  LOG.error("getUserSections: " + e.getMessage(), e);
	  }

	  return sections;
  }
  
  private List getAllSections()
  {
	  List sections = new ArrayList();
	  try
	  {
		  Site currentSite = SiteService.getSite(ToolManager.getCurrentPlacement().getContext()); 
		  Collection groups = currentSite.getGroups();
		  for (Iterator groupIterator = groups.iterator(); groupIterator.hasNext();)
	      {
	        Group currentGroup = (Group) groupIterator.next(); 
        	sections.add(currentGroup.getTitle());
	      }
	  } 
	  catch (Exception e) 
	  {
		  LOG.error("getAllSections: " + e.getMessage(), e);
	  }

	  return sections;
  }

}
