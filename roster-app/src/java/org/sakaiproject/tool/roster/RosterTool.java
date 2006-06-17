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

package org.sakaiproject.tool.roster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.authz.api.Role;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author rshastri <a href="mailto:rshastri@iupui.edu ">Rashmi Shastri</a>
 * @version $Id: RosterTool.java 1378 2005-08-25 16:25:41Z rshastri@iupui.edu $
 */
public class RosterTool
{
  private final static Log Log = LogFactory.getLog(RosterTool.class);
  private ResourceLoader msgs = new ResourceLoader("org.sakaiproject.tool.roster.bundle.Messages");

  private static final String PARTICIPANT_ID = "participantId";
  private static final String SORT_LAST_NAME = "lastName";
  private static final String SORT_USER_ID = "id";

  private static final String VIEW_BY_ROLE = "role";
  private static final String VIEW_ALL = "all";
  
  private static final String ROSTER_PRIVACY_URL = "roster.privacy.url";
  private static final String ROSTER_PRIVACY_TEXT = "roster.privacy.text";

  private String idPhotoText = msgs.getString("show_id_photo");
  private String customPhotoText = msgs.getString("show_pic");
  private String title = msgs.getString("title_show_roster");

  protected RosterManager rosterManager;
  protected ProfileManager profileManager;

  private DecoratedParticipant participant;

  private boolean showIdPhoto = false;
  private boolean showCustomPhoto = false;
  private boolean reloadAllUsers = true;
  private boolean reloadRoles = true;
  private String facet = "";
  private String displayView = VIEW_BY_ROLE;
  private int allUserCount = 0;
  private List roles = null;
  private List allDecoUsers = null;
  private List alluserList = null;

  // sort column
  private boolean sortLastNameDescending = false;
  private boolean sortUserIdDescending = false;
  private boolean sortLastNameAscending = true;
  private boolean sortUserIdAscending = false;

  public RosterTool()
  {

  }

  public String processActionToggleIdPhotos()
  {
    Log.debug("processActionToggleIdPhotos()");
    if (showIdPhoto)
    {
      setPhotoToggeling(msgs.getString("hide_id_photo"));
    }
    else
    // if the photo ids are already hidden the display the photos
    {
      setPhotoToggeling(msgs.getString("show_id_photo"));
    }
    return "main";
  }

  public String processActionToggleCustomPhotos()
  {
    Log.debug("processActionToggleCustomPhotos()");
    if (showCustomPhoto)
    {
      setPhotoToggeling(msgs.getString("hide_pic"));
    }
    else
    // if the photo ids are already hidden the display the photos
    {
      setPhotoToggeling(msgs.getString("show_pic"));
    }
    return "main";
  }

  public String toggleUserIdSort()
  {
    Log.debug("toggleUserIdSort()");
    if (sortUserIdAscending)
    {
      setSort("descending", SORT_USER_ID);
    }
    else
    {
      setSort("ascending", SORT_USER_ID);
    }
    return "main";
  }

  public String toggleLastNameSort()
  {
    Log.debug("toggleLastNameSort()");
    if (sortLastNameAscending)
    {
      setSort("descending", SORT_LAST_NAME);
    }
    else
    {
      setSort("ascending", SORT_LAST_NAME);
    }
    return "main";
  }

  private void setSort(String sortOrder, String sortBy)
  {
    // reloadAllUsers=true;
    sortLastNameDescending = false;
    sortUserIdDescending = false;
    sortLastNameAscending = false;
    sortUserIdAscending = false;
    if (sortOrder.equals("ascending"))
    {
      if (sortBy.equals(SORT_USER_ID))
      {
        sortUserIdAscending = true;
        rosterManager.sortParticipants(alluserList, SORT_USER_ID, true);
        this.allDecoUsers = getAllUsers(alluserList);
        return;
      }
      if (sortBy.equals(SORT_LAST_NAME))
      {
        sortLastNameAscending = true;
        rosterManager.sortParticipants(alluserList, SORT_LAST_NAME, true);
        this.allDecoUsers = getAllUsers(alluserList);
        return;
      }
    }
    else

    {
      if (sortBy.equals(SORT_USER_ID))
      {
        sortUserIdDescending = true;
        rosterManager.sortParticipants(alluserList, SORT_USER_ID, false);
        this.allDecoUsers = getAllUsers(alluserList);
        return;
      }
      if (sortBy.equals(SORT_LAST_NAME))
      {
        sortLastNameDescending = true;
        rosterManager.sortParticipants(alluserList, SORT_LAST_NAME, false);
        this.allDecoUsers = getAllUsers(alluserList);
        return;
      }

    }

  }

  public boolean isSortLastNameAscending()
  {
    Log.debug("isSortLastNameAscending()");
    return sortLastNameAscending;
  }

  public boolean isSortLastNameDescending()
  {
    Log.debug("isSortLastNameDescending()");
    return sortLastNameDescending;
  }

  public boolean isSortUserIdAscending()
  {
    Log.debug("isSortUserIdAscending()");
    return sortUserIdAscending;
  }

  public boolean isSortUserIdDescending()
  {
    Log.debug("isSortUserIdDescending()");
    return sortUserIdDescending;
  }

  public String processValueChangeForView(ValueChangeEvent vce)
  {
    if (Log.isDebugEnabled())
      Log.debug("processValueChangeForView(ValueChangeEvent " + vce + ")");
    String changeView = (String) vce.getNewValue();
    if (changeView != null && changeView.equals(VIEW_ALL))
    {
      setDisplayView(VIEW_ALL);
      getAllUsers();
    }
    else
    {
      setDisplayView(VIEW_BY_ROLE);
      getRoles();
    }
    return "main";
  }

  public boolean isShowCustomPhoto()
  {
    Log.debug("isShowCustomPhoto()");
    return (showCustomPhoto);
  }

  public String getIdPhotoText()
  {
    Log.debug("getIdPhotoText()");
    return idPhotoText;
  }

  public String getCustomPhotoText()
  {
    Log.debug("getCustomPhotoText()");
    return customPhotoText;
  }

  public String processCancel()
  {
    Log.debug("processCancel()");
    return "main";
  }

  public DecoratedParticipant getParticipant()
  {
    Log.debug("getParticipant()");
    return participant;
  }

  public RosterManager getRosterManager()
  {
    Log.debug("getRosterManager()");
    return rosterManager;
  }

  public void setRosterManager(RosterManager rosterManager)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("setRosterManager(RosterManager " + rosterManager + ")");
    }
    this.rosterManager = rosterManager;
  }

  public ProfileManager getProfileManager()
  {
    Log.debug("getProfileManager()");
    return profileManager;
  }

  public boolean isShowIdPhoto()
  {
    Log.debug("isShowIdPhoto()");
    return (showIdPhoto);
  }

  public void setProfileManager(ProfileManager profileService)
  {

    if (Log.isDebugEnabled())
    {
      Log.debug("setProfileManager(ProfileManager " + profileService + ")");
    }
    this.profileManager = profileService;
  }

  public boolean isShowTool()
  {
    Log.debug("isShowTool()");
    return profileManager.isShowTool();
  }

  public String getTitle()
  {
    Log.debug("getTitle()");
    return title;
  }

  public String getFacet()
  {
    Log.debug("getFacet()");
    return facet;
  }

  public String getDisplayView()
  {
    Log.debug("getDisplayView()");
    return displayView;
  }

  public void setDisplayView(String display)
  {
    Log.debug("setDisplayView(String " + display + ")");
    this.displayView = display;
  }

  public boolean isDisplayByRole()
  {
    Log.debug("isDisplayByRole()");
    return this.displayView.equals(VIEW_BY_ROLE);
  }

  public boolean isDisplayAllUsers()
  {
    Log.debug("isDisplayByRole()");
    return this.displayView.equals(VIEW_ALL);
  }

  public boolean isUpdateAccess()
  {
    Log.debug("isUpdateAccess()");
    return rosterManager.isInstructor();
  }

  public boolean isRenderPhotoColumn()
  {
    Log.debug("isRenderPhotoColumn()");
    return (isShowIdPhoto() || isShowCustomPhoto());
  }

  public List getRoles()
  {
    Log.debug("getRoles()");
    if (reloadRoles || roles == null)
    {
      roles = new ArrayList();
      List tempRoleList = rosterManager.getRoles();
      {
        if (tempRoleList != null && tempRoleList.size() > 0)
        {
          Iterator iter = tempRoleList.iterator();

          while (iter.hasNext())
          {
            roles.add(new DecoratedRole((Role) iter.next()));
          }
        }
      }
      reloadRoles = false;
    }
    return roles;

  }

  public List getAllUsers()
  {
    Log.debug("getAllUsers()");
    if (reloadAllUsers || allDecoUsers == null)
    {
      alluserList = rosterManager.getAllUsers();
      reloadAllUsers = false;
      allDecoUsers = getAllUsers(alluserList);
    }
    return allDecoUsers;
  }

  private List getAllUsers(List list)
  {
    Log.debug("getAllUsers()");
    allDecoUsers = new ArrayList();
    if (list == null || list.size() < 1)
    {
      return null;
    }
    this.allUserCount = list.size();
    Iterator iter = list.iterator();

    while (iter.hasNext())
    {
      allDecoUsers.add(new DecoratedParticipant((Participant) iter.next()));
    }
    return allDecoUsers;
  }

  public int getAllUserCount()
  {
    Log.debug("getAllUserCount()");
    return allUserCount;
  }

  /**
   * Set variables for photo display
   * 
   * @param option
   */
  private void setPhotoToggeling(String option)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("setPhotoToggeling(String " + option + ")");
    }
    if (option != null && option.equals(msgs.getString("show_id_photo")))// show official
    {
      this.idPhotoText = msgs.getString("hide_id_photo");
      this.showIdPhoto = true;
      this.customPhotoText = msgs.getString("show_pic");
      this.showCustomPhoto = false;
      this.title = msgs.getString("title_view_official");
      this.facet = msgs.getString("facet_official");
    }
    else
      if (option != null && option.equals(msgs.getString("show_pic")))// show custom
      {
        this.idPhotoText = msgs.getString("show_id_photo");
        this.customPhotoText = msgs.getString("hide_pic");
        this.showIdPhoto = false;
        this.showCustomPhoto = true;
        this.title = msgs.getString("title_view_pictures");
        this.facet = msgs.getString("facet_picture");
      }
      else
      // hide all
      {
        this.idPhotoText = msgs.getString("show_id_photo");
        this.showIdPhoto = false;
        this.customPhotoText = msgs.getString("show_pic");
        this.showCustomPhoto = false;
        this.title = msgs.getString("title_show_roster");
        this.facet = "";
      }

  }

  public String processActionDisplayProfile()
  {
    Log.debug("processActionDisplayProfile()");
    try
    {
      ExternalContext context = FacesContext.getCurrentInstance()
          .getExternalContext();
      Map paramMap = context.getRequestParameterMap();
      Iterator itr = paramMap.keySet().iterator();
      while (itr.hasNext())
      {
        String key = (String) itr.next();
        if (key != null && key.equals(PARTICIPANT_ID))
        {
          String participantId = (String) paramMap.get(key);
          participant = new DecoratedParticipant(rosterManager
              .getParticipantById(participantId));
          break;
        }
      }
      if (participant.getParticipant().getProfile() != null)
      {
        if (participant.isDisplayCompleteProfile())
        {
          return "displayCompleteProfile";
        }
        if (participant.displayPublicProfile())
        {
          return "displayPublicProfile";
        }
      }
      return "profileNotFound";
    }
    catch (Exception e)
    {
      Log.error(e.getMessage(), e);
      return "profileNotFound";
    }
  }

  public class DecoratedRole
  {
    protected Role role;
    protected List role_decoUsers = null;
    protected List roleUserList = null;
    private boolean role_sortLastNameDescending = false;
    private boolean role_sortUserIdDescending = false;
    private boolean role_sortLastNameAscending = true;
    private boolean role_sortUserIdAscending = false;
    private boolean role_currentSortAscending = true;// sort ascending by last name by default
    private String role_currentSortBy = SORT_LAST_NAME;

    public DecoratedRole(Role decRole)
    {
      if (Log.isDebugEnabled())
      {
        Log.debug("DecoratedRole(Role" + decRole + ")");
      }
      role = decRole;
    }

    public void toggleUserIdSort()
    {
      Log.debug("toggleUserIdSort()");
      if (role_sortUserIdAscending)
      {
        setSort("descending", SORT_USER_ID);
      }
      else
      {
        setSort("ascending", SORT_USER_ID);
      }
    }

    public void toggleLastNameSort()
    {
      Log.debug("toggleLastNameSort()");
      if (role_sortLastNameAscending)
      {
        setSort("descending", SORT_LAST_NAME);
      }
      else
      {
        setSort("ascending", SORT_LAST_NAME);
      }
    }

    private void setSort(String sortOrder, String sortBy)
    {
      role_sortLastNameDescending = false;
      role_sortUserIdDescending = false;
      role_sortLastNameAscending = false;
      role_sortUserIdAscending = false;

      if (sortOrder.equals("ascending"))
      {
        if (sortBy.equals(SORT_USER_ID))
        {
          role_sortUserIdAscending = true;
          role_currentSortAscending = true;
          role_currentSortBy = SORT_USER_ID;
          return;
        }
        if (sortBy.equals(SORT_LAST_NAME))
        {
          role_sortLastNameAscending = true;
          role_currentSortAscending = true;
          role_currentSortBy = SORT_LAST_NAME;
          return;
        }
      }
      else

      {
        if (sortBy.equals(SORT_USER_ID))
        {
          role_sortUserIdDescending = true;
          role_currentSortAscending = false;
          role_currentSortBy = SORT_USER_ID;
          return;
        }
        if (sortBy.equals(SORT_LAST_NAME))
        {
          role_sortLastNameDescending = true;
          role_currentSortAscending = false;
          role_currentSortBy = SORT_LAST_NAME;
          return;
        }

      }

    }

    public List getUsers()
    {
      Log.debug("getUsers()");
      // if (reloadRoleUsers || role_decoUsers == null)
      // {
      roleUserList = rosterManager.getParticipantByRole(role);
      rosterManager.sortParticipants(roleUserList, role_currentSortBy,
          role_currentSortAscending);
      role_decoUsers = getUsers(roleUserList);
      // reloadRoleUsers = false;
      // }
      return role_decoUsers;
    }

    // will convert a participant list into decorated participant list
    private List getUsers(List list)
    {
      if (Log.isDebugEnabled())
      {
        Log.debug("getUsers(List " + list + ")");
      }
      role_decoUsers = new ArrayList();

      if (list != null && list.size() > 0)
      {
        Iterator iter = list.iterator();

        while (iter.hasNext())
        {
          role_decoUsers
              .add(new DecoratedParticipant((Participant) iter.next()));
        }
      }

      return role_decoUsers;
    }

    public Role getRole()
    {
      Log.debug("getRole()");
      return role;
    }

    public int getUserCount()
    {
      Log.debug("getUserCount()");
      List tempUserList = rosterManager.getParticipantByRole(role);
      {
        if (tempUserList != null && tempUserList.size() > 0)
        {
          return tempUserList.size();
        }
      }
      return 0;
    }

    public boolean isRole_sortLastNameAscending()
    {
      Log.debug("isRole_sortLastNameAscending()");
      return role_sortLastNameAscending;
    }

    public boolean isRole_sortLastNameDescending()
    {
      Log.debug("isRole_sortLastNameDescending()");
      return role_sortLastNameDescending;
    }

    public boolean isRole_sortUserIdAscending()
    {
      Log.debug("isRole_sortUserIdAscending()");
      return role_sortUserIdAscending;
    }

    public boolean isRole_sortUserIdDescending()
    {
      Log.debug("isRole_sortUserIdDescending()");
      return role_sortUserIdDescending;
    }

  }
  public class DecoratedParticipant
  {

    protected Participant decoratedParticipant;
    protected boolean showCustomPhotoUnavailable = false;

    public DecoratedParticipant(Participant decParticipant)
    {
      if (Log.isDebugEnabled())
      {
        Log.debug("DecoratedParticipant(Participant " + decParticipant + ")");
      }
      this.decoratedParticipant = decParticipant;
    }

    // User Profile: display a emplty url or no blocked profile
    public boolean isShowCustomPhotoUnavailable()
    {
      Log.debug("isShowCustomPhotoUnavailable()");
      if (!showCustomPhoto)
      {
        return false;
      }
      return isShowCustomPhotoUnavailableForSelectedProfile();
    }

    public boolean isShowCustomPhotoUnavailableForSelectedProfile()
    {
      Log.debug("isShowCustomPhotoUnavailableForSelectedProfile()");
      if (decoratedParticipant.getProfile() == null)
      {
        return true;
      }
      if (!profileManager.displayCompleteProfile(decoratedParticipant
          .getProfile()))
      {
        return true;
      }

      if (decoratedParticipant.getProfile().isInstitutionalPictureIdPreferred() == null)
      {
        return true;
      }
      if (!decoratedParticipant.getProfile()
          .isInstitutionalPictureIdPreferred().booleanValue()
          && (decoratedParticipant.getProfile().getPictureUrl() == null || decoratedParticipant
              .getProfile().getPictureUrl().length() < 1))
      {
        return true;
      }
      return false;
    }

    // User Profile : display a non empty url
    public boolean isShowURLPhoto()
    {

      Log.debug("isShowURLPhoto()");
      if (!showCustomPhoto)
      {
        return false;
      }
      return isShowURLPhotoForSelectedProfile();
    }

    public boolean isShowURLPhotoForSelectedProfile()
    {
      Log.debug("isShowURLPhotoForSelectedProfile()");
      if (decoratedParticipant.getProfile() == null)
      {
        return false;
      }
      if (profileManager.displayCompleteProfile(decoratedParticipant
          .getProfile())
          && decoratedParticipant.getProfile().getPictureUrl() != null
          && decoratedParticipant.getProfile().getPictureUrl().length() > 0)
      {
        return true;
      }
      return false;
    }

    // User Profile : display id Photo
    public boolean isShowCustomIdPhoto()
    {
      Log.debug("isShowCustomIdPhoto()");
      if (!showCustomPhoto)
      {
        return false;
      }
      return isShowCustomIdPhotoForSelectedProfile();

    }

    public boolean isShowCustomIdPhotoForSelectedProfile()
    {
      Log.debug("isShowCustomIdPhotoForSelectedProfile");
      if (decoratedParticipant.getProfile() == null)
      {
        return false;
      }
      if (decoratedParticipant.getProfile().isInstitutionalPictureIdPreferred() == null)
      {
        return false;
      }
      if (profileManager.displayCompleteProfile(decoratedParticipant
          .getProfile())
          && decoratedParticipant.getProfile()
              .isInstitutionalPictureIdPreferred().booleanValue())
      {
        return true;
      }
      return false;
    }

    public Participant getParticipant()
    {
      Log.debug("getParticipant()");
      return decoratedParticipant;
    }

    public boolean isDisplayCompleteProfile()
    {
      Log.debug("isDisplayCompleteProfile()");
      return profileManager.displayCompleteProfile(decoratedParticipant
          .getProfile());
    }

    // if only public profile should be displayed.
    public boolean displayPublicProfile()
    {
      Log.debug("displayPublicProfile()");
      if (decoratedParticipant.getProfile() == null)
      {
        return false;
      }
      if (profileManager.displayCompleteProfile(decoratedParticipant
          .getProfile()))
      {
        return true;
      }
      if (decoratedParticipant.getProfile().getHidePublicInfo() == null)
      {
        return false;
      }
      if (decoratedParticipant.getProfile().getHidePublicInfo().booleanValue() == true)
      {
        return false;
      }
      if (decoratedParticipant.getProfile().getHidePrivateInfo() == null)
      {
        return true;
      }
      if (decoratedParticipant.getProfile().getHidePrivateInfo().booleanValue() == true)
      {
        return true;
      }

      return false;
    }
  }

  /**
   * Enable privacy message
   * @return
   */
  public boolean getRenderPrivacyAlert()
  {
   if((!isUpdateAccess()) && ServerConfigurationService.getString(ROSTER_PRIVACY_TEXT)!=null &&
       ServerConfigurationService.getString(ROSTER_PRIVACY_TEXT).trim().length()>0 )
   {
     return true;
   }
    return false;
  }
  
  /**
   * Get Privacy message link  from sakai.properties
   * @return
   */
  public String getPrivacyAlertUrl()
  {
    return ServerConfigurationService.getString(ROSTER_PRIVACY_URL);
  }
  
  /**
   * Get Privacy message from sakai.properties
   * @return
   */
  public String getPrivacyAlert()
  {
    return ServerConfigurationService.getString(ROSTER_PRIVACY_TEXT);
  }
}
