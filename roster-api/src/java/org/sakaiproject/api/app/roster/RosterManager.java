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

package org.sakaiproject.api.app.roster;

import java.util.List;

/**
 * @author rshastri 
 */
public interface RosterManager
{
  // Roster filters
  public static final String VIEW_ALL_SECT = "all";
  public static final String VIEW_MY_SECT = "my";
  public static final String VIEW_NO_SECT = "no";
	  
  public void init();

  public void destroy();

  /**
   * Check for export permission (roster.export)
   * @return
   */
  public boolean currentUserHasExportPerm();
  
  /**
   * Check for view all permission (roster.viewall)
   * @return
   */
  public boolean currentUserHasViewAllPerm();
  
  /**
   * Check for view section permission (roster.viewsection)
   * @return
   */
  public boolean currentUserHasViewSectionPerm();
  
  /**
   * Check for view hidden permission (roster.viewhidden)
   * @return
   */
  public boolean currentUserHasViewHiddenPerm();
  
  /**
   * Check for view official id permission (roster.viewofficialid) 
   * @return
   */
  public boolean currentUserHasViewOfficialIdPerm();
  
  /**
   * Check for site update permission (site.upd) 
   * @return
   */
  public boolean currentUserHasSiteUpdatePerm();
  
  /**
   * Check to see if the site has any sections/groups
   * @return
   */
  public boolean siteHasSections();
  
  /**
   *  Get the sections viewable by current user
   * @return
   */
  public List getViewableSectionsForCurrentUser();

  /**
   * @return List of all the participants in the site viewable to current user
   */
  public List getRoster(String filter);
  
  /**
   * Returns a participant by the id
   * @param participantId
   * @return
   */
  public Participant getParticipantById(String participantId);
  
  /**
   * Sort the participants ascendingly or decendingly
   * by various available columns
   * @param participants
   * @param sortByColumn
   * @param ascending
   */
  public void sortParticipants(List participants, String sortByColumn, boolean ascending); 
  
}
