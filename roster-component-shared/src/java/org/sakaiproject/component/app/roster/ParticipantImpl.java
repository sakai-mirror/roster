/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/roster/roster-component-shared/src/java/org/sakaiproject/component/app/roster/ParticipantImpl.java $
 * $Id: ParticipantImpl.java 1244 2005-08-17 16:06:54Z rshastri@iupui.edu $
 **********************************************************************************
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

package org.sakaiproject.component.app.roster;

import java.util.Comparator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.api.app.profile.Profile;
import org.sakaiproject.api.app.roster.Participant;

/**
 * @author rshastri
 *
 */
public class ParticipantImpl implements Participant
{
  private final static Log Log = LogFactory.getLog(ParticipantImpl.class);
  private String id;
  private String firstName;
  private String lastName;
  private Profile profile;
  public static Comparator LastNameComparator;
  public static Comparator FirstNameComparator;
  public static Comparator UserIdComparator;

  /**
   * 
   * @param id
   * @param fname
   * @param lname
   * @param profile TODO
   */
  public ParticipantImpl(String id, String fname, String lname, Profile profile)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("ParticipantImpl(String " + id + ", String " + fname
          + ", String " + lname + ")");
    }
    setId(id);
    setFirstName(fname);
    setLastName(lname);
    setProfile(profile);
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.component.app.roster.Participant#getFirstName()
   */
  public String getFirstName()
  {
    Log.debug("getFirstName()");
    return firstName;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.component.app.roster.Participant#setFirstName(java.lang.String)
   */
  public void setFirstName(String firstName)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("setFirstName(String" + firstName + ")");
    }
    this.firstName = firstName;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.component.app.roster.Participant#getId()
   */
  public String getId()
  {
    Log.debug("getId()");
    return id;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.component.app.roster.Participant#setId(java.lang.String)
   */
  public void setId(String id)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("setId(String" + id + ")");
    }
    this.id = id;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.component.app.roster.Participant#getLastName()
   */
  public String getLastName()
  {
    Log.debug("getLastName()");
    return lastName;
  }

  /* (non-Javadoc)
   * @see org.sakaiproject.component.app.roster.Participant#setLastName(java.lang.String)
   */
  public void setLastName(String lastName)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("setLastName(String" + lastName + ")");
    }
    this.lastName = lastName;
  }
 

  public boolean equals(Object o)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("equals(Object" + o + ")");
    }
    if (!(o instanceof Participant)) return false;
    Participant participant = (Participant) o;
    return participant.hashCode() == this.hashCode();
  }

  public String toString()
  {
    Log.debug("toString()");
    return firstName + " " + lastName;

  }

  public int compareTo(Object anotherParticipant) throws ClassCastException
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("compare(Object " + anotherParticipant + ")");
    }
    if (!(anotherParticipant instanceof Participant))
      throw new ClassCastException("A Participant object expected.");
    String anotherParticipantLastName = ((Participant) anotherParticipant)
        .getLastName();
    String anotherParticipantFirstName = ((Participant) anotherParticipant)
        .getFirstName();
    int lastNameComp = this.lastName.compareTo(anotherParticipantLastName);
    return (lastNameComp != 0 ? lastNameComp : this.firstName
        .compareTo(anotherParticipantFirstName));
  }

  static
  {
    LastNameComparator = new Comparator()

    {
      public int compare(Object participant, Object otherParticipant)
      {
        if (Log.isDebugEnabled())
        {
          Log.debug("compare(Object " + participant + ", Object "
              + otherParticipant + ")");
        }
        String lastName1 = ((Participant) participant).getLastName()
            .toUpperCase();
        String firstName1 = ((Participant) participant).getFirstName()
            .toUpperCase();
        String lastName2 = ((Participant) otherParticipant).getLastName()
            .toUpperCase();
        String firstName2 = ((Participant) otherParticipant).getFirstName()
            .toUpperCase();

        if (!(lastName1.equals(lastName2)))
          return lastName1.compareTo(lastName2);
        else
          return firstName1.compareTo(firstName2);
      }
    };

    FirstNameComparator = new Comparator()
    {
      public int compare(Object participant, Object otherParticipant)
      {
        if (Log.isDebugEnabled())
        {
          Log.debug("compare(Object " + participant + ", Object "
              + otherParticipant + ")");
        }
        String lastName1 = ((Participant) participant).getLastName()
            .toUpperCase();
        String firstName1 = ((Participant) participant).getFirstName()
            .toUpperCase();
        String lastName2 = ((Participant) otherParticipant).getLastName()
            .toUpperCase();
        String firstName2 = ((Participant) otherParticipant).getFirstName()
            .toUpperCase();

        if (!(firstName1.equals(firstName2)))
          return firstName1.compareTo(firstName2);
        else
          return lastName1.compareTo(lastName2);
      }
    };

    UserIdComparator = new Comparator()
    {
      public int compare(Object participant, Object otherParticipant)
      {
        if (Log.isDebugEnabled())
        {
          Log.debug("compare(Object " + participant + ", Object "
              + otherParticipant + ")");
        }
        String userId1 = ((Participant) participant).getId();
        String userId2 = ((Participant) participant).getId();
        return userId1.compareTo(userId1);

      }
    };
  }

  public Profile getProfile()
  {
    Log.debug("getProfile()");
    return profile;
  }

  public void setProfile(Profile profile)
  {
    if (Log.isDebugEnabled())
    {
      Log.debug("setProfile(Profile" + profile + ")");
    }
    this.profile = profile;    
  }

}
