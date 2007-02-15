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

import org.sakaiproject.api.app.profile.Profile;
import java.util.List;


/**
 * @author rshastri
 *
 */
public interface Participant 
{
  String SORT_BY_LAST_NAME = "lastName";
  String SORT_BY_FIRST_NAME = "firstName";
  String SORT_BY_ID = "id";
  String SORT_BY_ROLE = "role";
  String SORT_BY_SECTIONS = "sections";
  
  /**
   * @return FirstName 
   */
  public  String getFirstName();

  /**
   * @param firstName
   */
  public  void setFirstName(String firstName);

  /**
   * @return
   */
  public  String getId();

  /**
   * @param id
   */
  public  void setId(String id);

  public String getDisplayId();
  public void setDisplayId(String displayId);
  
  /**
   * @return
   */
  public  String getEid();

  /**
   * @param eid
   */
  public  void setEid(String eid);

  /**
   * @return
   */
  public  String getLastName();

  /**
   * @param lastName
   */
  public  void setLastName(String lastName);

  /**
   * @return
   */
  public Profile getProfile();
  
  /**
   * @param profile
   */
  public void setProfile(Profile profile);
  /**
   * @return
   */
  public String getRoleTitle();
  
  /**
   * 
   * @param roleTitle
   */
  public void setRoleTitle(String roleTitle);
  
  /**
   * 
   * @return List of groups/sections Participant is a member of
   */
  public List getSections();
  
  /**
   * 
   * @param sections 
   * List of groups/sections Participant is a member of
   */
  public void setSections(List sections);
  
  /**
   * Display version of section membership
   * @return
   */
  public String getSectionsForDisplay();
  
}