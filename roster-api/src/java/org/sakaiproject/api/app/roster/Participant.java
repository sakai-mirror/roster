/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/roster/roster-api/src/java/org/sakaiproject/api/app/roster/Participant.java $
 * $Id: Participant.java 1244 2005-08-17 16:06:54Z rshastri@iupui.edu $
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


/**
 * @author rshastri
 *
 */
public interface Participant 
{
  String SORT_BY_LAST_NAME = "lastName";
  String SORT_BY_FIRST_NAME = "firstName";
  String SORT_BY_ID = "userId";
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

  /**
   * @return
   */
  public  String getLastName();

  /**
   * @param lastName
   */
  public  void setLastName(String lastName);

}