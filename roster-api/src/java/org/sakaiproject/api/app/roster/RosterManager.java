/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/roster/roster-api/src/java/org/sakaiproject/api/app/roster/RosterManager.java $
 * $Id: RosterManager.java 1244 2005-08-17 16:06:54Z rshastri@iupui.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.api.app.roster;

import java.util.List;

import org.sakaiproject.service.legacy.authzGroup.Role;

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
