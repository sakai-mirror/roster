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

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.app.roster.Participant;
import org.sakaiproject.api.app.roster.RosterManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.ResourceLoader;

/**
 * @author rshastri <a href="mailto:rshastri@iupui.edu ">Rashmi Shastri</a>
 * @version $Id: RosterTool.java 1378 2005-08-25 16:25:41Z rshastri@iupui.edu $
 */
public class RosterTool
{
  private final static Log Log = LogFactory.getLog(RosterTool.class);
  private ResourceLoader msgs = new ResourceLoader("org.sakaiproject.tool.roster.bundle.Messages");
 
  private static final String ROSTER_PRIVACY_URL = "roster.privacy.url";
  private static final String ROSTER_PRIVACY_TEXT = "roster.privacy.text";
  private static final String PARTICIPANT_ID = "participantId";

  private String idPhotoText = msgs.getString("show_id_photo");
  private String customPhotoText = msgs.getString("show_pic");
  private String title = msgs.getString("title_show_roster");

  protected RosterManager rosterManager;
  protected ProfileManager profileManager;

  private DecoratedParticipant participant;

  private boolean showIdPhoto = false;
  private boolean showCustomPhoto = false;
  private boolean reloadRoster = true;
  private String facet = "";
  private int allUserCount = 0;
  private List decoRoster = null;
  private List rosterList = null;
  private List menuItems = null;
  private boolean rosterProcessed = false;

  // sort column
  private boolean sortLastNameDescending = false;
  private boolean sortUserIdDescending = false;
  private boolean sortRoleDescending = false;
  private boolean sortSectionsDescending = false;
  private boolean sortLastNameAscending = false;
  private boolean sortUserIdAscending = false;
  private boolean sortRoleAscending = true;  // default to sort by role
  private boolean sortSectionsAscending = false;
  
  public static final String CSV_DELIM = ",";
  public static final String SORT_ASC = "ascending";
  public static final String SORT_DESC = "descending";
  
  private String selectedView = RosterManager.VIEW_NO_SECT;

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
      setSort(SORT_DESC, Participant.SORT_BY_ID);
    }
    else
    {
      setSort(SORT_ASC, Participant.SORT_BY_ID);
    }
    return "main";
  }

  public String toggleLastNameSort()
  {
    Log.debug("toggleLastNameSort()");
    if (sortLastNameAscending)
    {
      setSort(SORT_DESC, Participant.SORT_BY_LAST_NAME);
    }
    else
    {
      setSort(SORT_ASC, Participant.SORT_BY_LAST_NAME);
    }
    return "main";
  }
  
  public String toggleRoleSort()
  {
    Log.debug("toggleRoleSort()");
    if (sortRoleAscending)
    {
      setSort(SORT_DESC, Participant.SORT_BY_ROLE);
    }
    else
    {
      setSort(SORT_ASC, Participant.SORT_BY_ROLE);
    }
    return "main";
  }
  
  public String toggleSectionsSort()
  {
    Log.debug("toggleSectionsSort()");
    if (sortSectionsAscending)
    {
      setSort(SORT_DESC, Participant.SORT_BY_SECTIONS);
    }
    else
    {
      setSort(SORT_ASC, Participant.SORT_BY_SECTIONS);
    }
    return "main";
  }
  
  private void sortRoster()
  {
	  if (sortLastNameDescending)
		  setSort(SORT_DESC, Participant.SORT_BY_LAST_NAME);
	  else if (sortLastNameAscending)
		  setSort(SORT_ASC, Participant.SORT_BY_LAST_NAME);
	  else if (sortUserIdDescending)
		  setSort(SORT_DESC, Participant.SORT_BY_ID);
	  else if (sortUserIdAscending)
		  setSort(SORT_ASC, Participant.SORT_BY_ID);
	  else if (sortRoleDescending)
		  setSort(SORT_DESC, Participant.SORT_BY_ROLE);
	  else if (sortRoleAscending)
		  setSort(SORT_ASC, Participant.SORT_BY_ROLE);
	  else if (sortSectionsDescending)
		  setSort(SORT_DESC, Participant.SORT_BY_SECTIONS);
	  else if (sortSectionsAscending)
		  setSort(SORT_ASC, Participant.SORT_BY_SECTIONS);
  }
  
  private void setSort(String sortOrder, String sortBy)
  {
    sortLastNameDescending = false;
    sortUserIdDescending = false;
    sortRoleDescending = false;
    sortSectionsDescending = false;
    sortLastNameAscending = false;
    sortUserIdAscending = false;
    sortRoleAscending = false;
    sortSectionsAscending = false;
    
    if (sortOrder.equals(SORT_ASC))
    {
      if (sortBy.equals(Participant.SORT_BY_ID))
      {
        sortUserIdAscending = true;
        rosterManager.sortParticipants(rosterList, Participant.SORT_BY_ID, true);
        this.decoRoster = getRoster(rosterList);
        return;
      }
      if (sortBy.equals(Participant.SORT_BY_LAST_NAME))
      {
        sortLastNameAscending = true;
        rosterManager.sortParticipants(rosterList, Participant.SORT_BY_LAST_NAME, true);
        this.decoRoster = getRoster(rosterList);
        return;
      }
      if (sortBy.equals(Participant.SORT_BY_ROLE))
      {
        sortRoleAscending = true;
        rosterManager.sortParticipants(rosterList, Participant.SORT_BY_ROLE, true);
        this.decoRoster = getRoster(rosterList);
        return;
      }
      if (sortBy.equals(Participant.SORT_BY_SECTIONS))
      {
    	  sortSectionsAscending = true;
    	  rosterManager.sortParticipants(rosterList, Participant.SORT_BY_SECTIONS, true);
          this.decoRoster = getRoster(rosterList);
          return;
      }
    }
    else

    {
      if (sortBy.equals(Participant.SORT_BY_ID))
      {
        sortUserIdDescending = true;
        rosterManager.sortParticipants(rosterList, Participant.SORT_BY_ID, false);
        this.decoRoster = getRoster(rosterList);
        return;
      }
      if (sortBy.equals(Participant.SORT_BY_LAST_NAME))
      {
        sortLastNameDescending = true;
        rosterManager.sortParticipants(rosterList, Participant.SORT_BY_LAST_NAME, false);
        this.decoRoster = getRoster(rosterList);
        return;
      }
      if (sortBy.equals(Participant.SORT_BY_ROLE))
      {
        sortRoleDescending = true;
        rosterManager.sortParticipants(rosterList, Participant.SORT_BY_ROLE, false);
        this.decoRoster = getRoster(rosterList);
        return;
      }
      if (sortBy.equals(Participant.SORT_BY_SECTIONS))
      {
        sortSectionsDescending = true;
        rosterManager.sortParticipants(rosterList, Participant.SORT_BY_SECTIONS, false);
        this.decoRoster = getRoster(rosterList);
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
  
  public boolean isSortRoleAscending()
  {
    Log.debug("isSortRoleAscending()");
    return sortRoleAscending;
  }

  public boolean isSortRoleDescending()
  {
    Log.debug("isSortRoleDescending()");
    return sortRoleDescending;
  }
  
  public boolean isSortSectionsAscending()
  {
    Log.debug("isSortSectionsAscending()");
    return sortSectionsAscending;
  }

  public boolean isSortSectionsDescending()
  {
    Log.debug("isSortSectionsDescending()");
    return sortSectionsDescending;
  }

  public String processValueChangeForView(ValueChangeEvent vce)
  {
    if (Log.isDebugEnabled())
      Log.debug("processValueChangeForView(ValueChangeEvent " + vce + ")");
    String changeView = (String) vce.getNewValue();
    if (changeView != null)
    {
      if (!changeView.equalsIgnoreCase(selectedView))
      {
    	  // don't reload if changing from "View All Sections" or "View All My Sections" to "View No Sections"
    	  if (!((changeView.equalsIgnoreCase(RosterManager.VIEW_ALL_SECT) || changeView.equalsIgnoreCase(RosterManager.VIEW_MY_SECT)) && selectedView.equalsIgnoreCase(RosterManager.VIEW_NO_SECT)) &&
    		  !(changeView.equalsIgnoreCase(RosterManager.VIEW_NO_SECT) && (selectedView.equalsIgnoreCase(RosterManager.VIEW_ALL_SECT) || selectedView.equalsIgnoreCase(RosterManager.VIEW_ALL_SECT))))
    		  reloadRoster = true;
      }

      setSelectedView(changeView);
      getRoster();
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

  public String getSelectedView()
  {
    Log.debug("getSelectedView()");
    return selectedView;
  }

  public void setSelectedView(String selected)
  {
    Log.debug("setSelectedView(String " + selected + ")");
    this.selectedView = selected;
  }

  public boolean isRenderPhotoColumn()
  {
    Log.debug("isRenderPhotoColumn()");
    return (isShowIdPhoto() || isShowCustomPhoto());
  }
  
  public boolean isRenderExportButton()
  {
	  return rosterManager.currentUserHasExportPerm() && isRenderRoster();
  }
  
  public boolean isRenderOfficialId()
  {
	  return rosterManager.currentUserHasViewOfficialIdPerm();
  }
  
  public boolean isRenderSectionsColumn()
  {
	  if (selectedView.equals(RosterManager.VIEW_NO_SECT))
		  return false;
	  
	  return isRenderViewMenu();
  }
  
  public boolean isRenderPrivacyMessage()
  {
	  return !rosterManager.currentUserHasViewHiddenPerm();
  }
  
  public boolean isRenderViewMenu()
  {  
	  getViewMenuItems();
	  if (menuItems != null && !menuItems.isEmpty())
		  return true;
	  
	  return false;
  }
  
  public boolean isRenderRoster()
  {
	  if (!rosterProcessed)
		  getRoster();
	  
	  return decoRoster != null && decoRoster.size() > 0;
  }
  
  public boolean isRenderRosterUpdateInfo()
  {
	  return rosterManager.currentUserHasSiteUpdatePerm();
  }
  
  public boolean isUserMayViewRoster()
  {
	  return rosterManager.currentUserHasViewAllPerm() || rosterManager.currentUserHasViewSectionPerm();
  }

  public List getRoster()
  {
    Log.debug("getRoster()");
    if (reloadRoster || decoRoster == null)
    {
      rosterList = rosterManager.getRoster(selectedView);
      sortRoster();  // allows us to maintain the previously selected sort view
      reloadRoster = false;
      rosterProcessed = true;
      decoRoster = getRoster(rosterList);
    }
    return decoRoster;
  }

  private List getRoster(List list)
  {
    Log.debug("getAllUsers()");
    decoRoster = new ArrayList();
    if (list == null || list.size() < 1)
    {
      return null;
    }
    this.allUserCount = list.size();
    Iterator iter = list.iterator();

    while (iter.hasNext())
    {
      decoRoster.add(new DecoratedParticipant((Participant) iter.next()));
    }
    return decoRoster;
  }

  public int getAllUserCount()
  {
    Log.debug("getAllUserCount()");
    return allUserCount;
  }
  
  public void setViewMenuItems(List menuItems)
  {
	  this.menuItems = menuItems;
  }
  
  /**
   * Returns the list of items to populate the "View" menu
   * @return
   */
  public List getViewMenuItems()
  {
	  if (menuItems != null)
		  return menuItems;

	  List selectItemList = new ArrayList();

	  List viewableSections = rosterManager.getViewableSectionsForCurrentUser();

	  if (viewableSections != null && !viewableSections.isEmpty())
	  {
		  Collections.sort(viewableSections);  // display the sections in ABC order in menu
		  
		  selectItemList.add(new SelectItem(RosterManager.VIEW_NO_SECT, msgs.getString("roster_viewNoSections")));

		  if (rosterManager.currentUserHasViewAllPerm())
		  {
			  selectItemList.add(new SelectItem(RosterManager.VIEW_ALL_SECT, msgs.getString("roster_viewAllSections")));
		  }
		  else if (rosterManager.currentUserHasViewSectionPerm())
		  {
			  selectItemList.add(new SelectItem(RosterManager.VIEW_MY_SECT, msgs.getString("roster_viewAllMySections")));
		  }
		  
		  for (Iterator i = viewableSections.iterator(); i.hasNext();)
		  {
			  String section = (String) i.next();
			  selectItemList.add(new SelectItem(section, section));
		  }
	  }

	  menuItems = selectItemList;
	  return selectItemList;       
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
   if((!rosterManager.currentUserHasViewHiddenPerm()) && ServerConfigurationService.getString(ROSTER_PRIVACY_TEXT)!=null &&
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

  /* Note: The CSV export code below was largely copied from 
   * Gradebook - org.sakai.tool.gradebook.ui.ExportBean */
  
  /**
   * Export the current roster view to a csv file
   * @param event
   */
  public void exportRosterCsv(ActionEvent event) {
	  writeAsCsv(getRosterAsCSV(isRenderSectionsColumn()), getFileName());
  }

  /**
   * returns a comma-delimited string representation of the current roster view
   * @param showSectionsCol
   * @return
   */
  private String getRosterAsCSV(boolean showSectionsCol)
  { 
	  if (rosterList == null || rosterList.isEmpty())
		  return "";

	  StringBuffer sb = new StringBuffer();

	  // Add the headers
	  sb.append(msgs.getString("export_name"));
	  sb.append(CSV_DELIM);
	  sb.append(msgs.getString("export_userId"));
	  sb.append(CSV_DELIM);
	  sb.append(msgs.getString("export_role"));
	  sb.append(CSV_DELIM);
	  if (showSectionsCol)
	  {
		  sb.append(msgs.getString("export_sections"));
		  sb.append(CSV_DELIM);
	  }
	  sb.append("\n");

	  // Add the data
	  for(Iterator rosterIter = rosterList.iterator(); rosterIter.hasNext();) {
		  Participant participant = (Participant)rosterIter.next();
		  appendQuoted(sb, participant.getLastName() + ", " + participant.getFirstName());
		  sb.append(CSV_DELIM);
		  sb.append(participant.getEid());
		  sb.append(CSV_DELIM);
		  sb.append(participant.getRoleTitle());
		  sb.append(CSV_DELIM);
		  if (showSectionsCol)
		  {
			  appendQuoted(sb, participant.getSectionsForDisplay());
			  sb.append(CSV_DELIM);
		  }

		  sb.append("\n");
	  }
	  return sb.toString();
  }

  private StringBuffer appendQuoted(StringBuffer sb, String toQuote) {
	  if ((toQuote.indexOf(',') >= 0) || (toQuote.indexOf('"') >= 0)) {
		  String out = toQuote.replaceAll("\"", "\"\"");
		  sb.append("\"").append(out).append("\"");
	  } else {
		  sb.append(toQuote);
	  }
	  return sb;
  }

  /**
   * @param csvString
   * @param fileName
   */
  private void writeAsCsv(String csvString, String fileName) {
	  FacesContext faces = FacesContext.getCurrentInstance();
	  HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
	  protectAgainstInstantDeletion(response);
	  response.setContentType("text/comma-separated-values");
	  response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".csv");
	  response.setContentLength(csvString.length());
	  OutputStream out = null;
	  try {
		  out = response.getOutputStream();
		  out.write(csvString.getBytes());
		  out.flush();
	  } catch (IOException e) {
		  Log.error(e);
		  e.printStackTrace();
	  } finally {
		  try {
			  if (out != null) out.close();
		  } catch (IOException e) {
			  Log.error(e);
			  e.printStackTrace();
		  }
	  }
	  faces.responseComplete();
  }

  /**
   * Gets the filename for the export
   *
   * @return The appropriate filename for the export
   */
  private String getFileName() {
	  String prefix = msgs.getString("export_filename_prefix");
	  Date now = new Date();
	  DateFormat df = new SimpleDateFormat(msgs.getString("export_filename_date_format"));
	  StringBuffer fileName = new StringBuffer(prefix);
	  String siteId = ToolManager.getCurrentPlacement().getContext();
	  if(siteId.trim() != null) {
		  siteId = siteId.replaceAll("\\s", "_"); // replace whitespace with '_'
		  fileName.append("_");
		  fileName.append(siteId);
	  }
	  fileName.append("_");
	  fileName.append(df.format(now));
	  return fileName.toString();
  }
    
    /**
     * Try to head off a problem with downloading files from a secure HTTPS
     * connection to Internet Explorer.
     *
     * When IE sees it's talking to a secure server, it decides to treat all hints
     * or instructions about caching as strictly as possible. Immediately upon
     * finishing the download, it throws the data away.
     *
     * Unfortunately, the way IE sends a downloaded file on to a helper
     * application is to use the cached copy. Having just deleted the file,
     * it naturally isn't able to find it in the cache. Whereupon it delivers
     * a very misleading error message like:
     * "Internet Explorer cannot download roster from sakai.yoursite.edu.
     * Internet Explorer was not able to open this Internet site. The requested
     * site is either unavailable or cannot be found. Please try again later."
     *
     * There are several ways to turn caching off, and so to be safe we use
     * several ways to turn it back on again.
     *
     * This current workaround should let IE users save the files to disk.
     * Unfortunately, errors may still occur if a user attempts to open the
     * file directly in a helper application from a secure web server.
     *
     * TODO Keep checking on the status of this.
     */
    public static void protectAgainstInstantDeletion(HttpServletResponse response) {
    	response.reset();	// Eliminate the added-on stuff
    	response.setHeader("Pragma", "public");	// Override old-style cache control
    	response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
    }

}
