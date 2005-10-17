/*******************************************************************************************************
 * $URL:
 * https://source.sakaiproject.org/svn/trunk/sakai/roster/roster-app/src/java/org/sakaiproject/tool/roster/RosterTool.java $
 * $Id: RosterTool.java 1378 2005-08-25 16:25:41Z rshastri@iupui.edu $
 ***********************************************************************************
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
import org.sakaiproject.service.legacy.authzGroup.Role;

/**
 * @author rshastri <a href="mailto:rshastri@iupui.edu ">Rashmi Shastri</a>
 * @version $Id: RosterTool.java 1378 2005-08-25 16:25:41Z rshastri@iupui.edu $
 */
public class RosterTool
{
  private final static Log Log = LogFactory.getLog(RosterTool.class);

  private static final String HIDE_ID_PHOTO = "Hide Official ID Photo";
  private static final String SHOW_ID_PHOTO = "Show Official ID Photo";
  private static final String HIDE_PIC = "Hide Pictures";
  private static final String SHOW_PIC = "Show Pictures";
  private static final String TITLE_SHOW_ROSTER = "Show Roster";
  private static final String TITLE_VIEW_PICTURES = "View Pictures";
  private static final String TITLE_VIEW_OFFICICAL = "View Offficial Photo IDs";
  private static final String PARTICIPANT_ID = "participantId";
  private static final String SORT_LAST_NAME = "lastName";
  private static final String SORT_USER_ID = "id";

  private static final String FACET_PICTURE = "Picture";
  private static final String FACET_OFFICIAL = "Photo ID";

  private static final String VIEW_BY_ROLE = "role";
  private static final String VIEW_ALL = "all";

  private String idPhotoText = SHOW_ID_PHOTO;
  private String customPhotoText = SHOW_PIC;
  private String title = TITLE_SHOW_ROSTER;

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
      setPhotoToggeling(HIDE_ID_PHOTO);
    }
    else
    // if the photo ids are already hidden the display the photos
    {
      setPhotoToggeling(SHOW_ID_PHOTO);
    }
    return "main";
  }

  public String processActionToggleCustomPhotos()
  {
    Log.debug("processActionToggleCustomPhotos()");
    if (showCustomPhoto)
    {
      setPhotoToggeling(HIDE_PIC);
    }
    else
    // if the photo ids are already hidden the display the photos
    {
      setPhotoToggeling(SHOW_PIC);
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
        rosterManager.sortParticipants(alluserList,SORT_USER_ID,true);
        this.allDecoUsers=getAllUsers(alluserList);
        return;
      }
      if (sortBy.equals(SORT_LAST_NAME))
      {
        sortLastNameAscending = true;
        rosterManager.sortParticipants(alluserList,SORT_LAST_NAME,true);
        this.allDecoUsers=getAllUsers(alluserList);
        return;
      }
    }
    else
      
    {
      if (sortBy.equals(SORT_USER_ID))
      {
        sortUserIdDescending = true;
        rosterManager.sortParticipants(alluserList,SORT_USER_ID,false);
        this.allDecoUsers=getAllUsers(alluserList);
        return;
      }
      if (sortBy.equals(SORT_LAST_NAME))
      {
        sortLastNameDescending = true;
        rosterManager.sortParticipants(alluserList,SORT_LAST_NAME,false);
        this.allDecoUsers=getAllUsers(alluserList);
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
    if (changeView != null && changeView.equals(this.VIEW_ALL))
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
   * @param option
   */
  private void setPhotoToggeling(String option)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("setPhotoToggeling(String " + option + ")");
    }
    if (option != null && option.equals(SHOW_ID_PHOTO))// show official
    {
      this.idPhotoText = HIDE_ID_PHOTO;
      this.showIdPhoto = true;
      this.customPhotoText = SHOW_PIC;
      this.showCustomPhoto = false;
      this.title = TITLE_VIEW_OFFICICAL;
      this.facet = FACET_OFFICIAL;
    }
    else
      if (option != null && option.equals(SHOW_PIC))// show custom
      {
        this.idPhotoText = SHOW_ID_PHOTO;
        this.customPhotoText = HIDE_PIC;
        this.showIdPhoto = false;
        this.showCustomPhoto = true;
        this.title = TITLE_VIEW_PICTURES;
        this.facet = FACET_PICTURE;
      }
      else
      // hide all
      {
        this.idPhotoText = SHOW_ID_PHOTO;
        this.showIdPhoto = false;
        this.customPhotoText = SHOW_PIC;
        this.showCustomPhoto = false;
        this.title = TITLE_SHOW_ROSTER;
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
        else
          if (participant.isHidePrivateInfo())
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
    private boolean role_currentSortAscending=false;
    private String role_currentSortBy=SORT_LAST_NAME;
    
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
           role_currentSortAscending=true;
           role_currentSortBy=SORT_USER_ID;  
           return;
        }
        if (sortBy.equals(SORT_LAST_NAME))
        {
          role_sortLastNameAscending = true;
          role_currentSortAscending=true;
          role_currentSortBy=SORT_LAST_NAME;  
          return;
        }
      }
      else
        
      {
        if (sortBy.equals(SORT_USER_ID))
        {
          role_sortUserIdDescending = true;
          role_currentSortAscending=false;
          role_currentSortBy=SORT_USER_ID; 
          return;
        }
        if (sortBy.equals(SORT_LAST_NAME))
        {
          role_sortLastNameDescending = true;
          role_currentSortAscending=false;
          role_currentSortBy=SORT_LAST_NAME; 
          return;
        }

      }

    }
    public List getUsers()
    {
      Log.debug("getUsers()");
//      if (reloadRoleUsers || role_decoUsers == null)
//      {
        roleUserList = rosterManager.getParticipantByRole(role);
        rosterManager.sortParticipants(roleUserList,role_currentSortBy,role_currentSortAscending);
        role_decoUsers = getUsers(roleUserList);
//        reloadRoleUsers = false;
//      }
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
    protected boolean showURLPhoto = false;
    protected boolean showCustomIdPhoto = false;

    public DecoratedParticipant(Participant decParticipant)
    {
      if (Log.isDebugEnabled())
      {
        Log.debug("DecoratedParticipant(Participant " + decParticipant + ")");
      }
      this.decoratedParticipant = decParticipant;
    }

    // User Profile: display a emplty url
    public boolean isShowCustomPhotoUnavailable()
    {
      Log.debug("isShowCustomPhotoUnavailable()");
      if (!showCustomPhoto)
      {
        return false;
      }
      if (decoratedParticipant.getProfile() == null)
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
      if (decoratedParticipant.getProfile() == null)
      {
        return false;
      }
      if (decoratedParticipant.getProfile().getPictureUrl() != null)
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
      if (decoratedParticipant.getProfile() == null)
      {
        return false;
      }
      if (decoratedParticipant.getProfile().isInstitutionalPictureIdPreferred() == null)
      {
        return false;
      }
      if (decoratedParticipant.getProfile().isInstitutionalPictureIdPreferred()
          .booleanValue())
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

    public boolean isHidePrivateInfo()
    {
      Log.debug("isHidePrivateInfo()");
      return (!profileManager.displayCompleteProfile(decoratedParticipant
          .getProfile()));
    }
  }

}
