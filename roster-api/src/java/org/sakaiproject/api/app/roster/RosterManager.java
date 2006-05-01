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

import org.sakaiproject.authz.api.Role;

/**
 * @author rshastri 
 */
public interface RosterManager
{
  public void init();

  public void destroy();

  /**
   * List all the Roles in site.
   * @return
   */
  public List getRoles();

  /**
   * List all the site users by a given role 
   * @param roleId
   * @return
   */
  public List getParticipantByRole(Role role);

  /**
   * Check for site.upd access for current user 
   * @return
   */
  public boolean isInstructor();

  /**
   * @return List of all the participants in the site
   */
  public List getAllUsers();

  
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
