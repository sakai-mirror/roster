/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/roster/roster-app/src/java/org/sakaiproject/tool/roster/ParticipantImageServlet.java $
 * $Id: ParticipantImageServlet.java 1378 2005-08-25 16:25:41Z rshastri@iupui.edu $
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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.ProfileManager;
import org.sakaiproject.api.kernel.component.cover.ComponentManager;

public class ParticipantImageServlet extends HttpServlet
{
  private static final Log LOG = LogFactory
      .getLog(ParticipantImageServlet.class);
  private static final String UNIVERSITY_ID_PHOTO = "photo";
  private static final String CONTENT_TYPE = "image/jpeg";
  private ProfileManager profileManager;

  private static final String IMAGE_PATH = "/images/";
  private static final String UNIVERSITY_ID_IMAGE_UNAVAILABLE = "/officialPhotoUnavailable.jpg";

  /**
   * The doGet method of the servlet. <br>
   *
   * This method is called when a form has its tag value method equals to get.
   *
   * @param request the request send by the client to the server
   * @param response the response send by the server to the client
   * @throws ServletException if an error occurred
   * @throws IOException if an error occurred
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException
  {
    if (LOG.isDebugEnabled())
      LOG.debug("doGet(HttpServletRequest" + request + ", HttpServletResponse"
          + response + ")");
    response.setContentType(CONTENT_TYPE);
    String userId = null;
    OutputStream stream = response.getOutputStream();
    userId = (String) request.getParameter(UNIVERSITY_ID_PHOTO);
    if (userId != null && userId.trim().length() > 0)
    {
      displayUniversityIDPhoto(userId, stream, response);
    }
    else
    {
      displayLocalImage(stream, UNIVERSITY_ID_IMAGE_UNAVAILABLE);
    }
  
  }

  private void displayUniversityIDPhoto(String userId, OutputStream stream,
      HttpServletResponse response)
  {
    if (LOG.isDebugEnabled())
      LOG.debug("displayUniversityIDPhoto (String " + userId
          + "OutputStream stream, HttpServletResponse response)");
    try
    {
      byte[] displayPhoto;
      displayPhoto = getProfileManager().getInstitutionalPhotoByUserId(userId,
          true);
      if (displayPhoto != null && displayPhoto.length > 0)
      {
        LOG.debug("Display University ID photo for user:" + userId);
        response.setContentLength(displayPhoto.length);
        stream.write(displayPhoto);
        stream.flush();

      }
      else
      {
        LOG.debug("Display University ID photo for user:" + userId
            + " is unavailable");
        displayLocalImage(stream, UNIVERSITY_ID_IMAGE_UNAVAILABLE);
      }
    }
    catch (IOException e)
    {
      LOG.error(e.getMessage(), e);
    }

  }

  
  private void displayLocalImage(OutputStream stream, String UNAVAILABLE_IMAGE)
  {
    if (LOG.isDebugEnabled())
      LOG.debug("displayPhotoUnavailable(OutputStream" + stream + ", String "
          + UNAVAILABLE_IMAGE + ")");
    try
    {
      BufferedInputStream in = null;
      try
      {

        in = new BufferedInputStream(new FileInputStream(getServletContext()
            .getRealPath(IMAGE_PATH)
            + UNAVAILABLE_IMAGE));
        int ch;

        while ((ch = in.read()) != -1)
        {
          stream.write((char) ch);
        }
      }

      finally
      {
        if (in != null) in.close();
      }
    }
    catch (FileNotFoundException e)
    {
      LOG.error(e.getMessage(), e);
    }
    catch (IOException e)
    {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * get the component manager
   * @return profile manager
   */
  public ProfileManager getProfileManager()
  {
    LOG.debug("getProfileManager()");
    if (profileManager == null)
    {
      return (ProfileManager) ComponentManager.get(ProfileManager.class
          .getName());
    }
    return profileManager;
  }

}
