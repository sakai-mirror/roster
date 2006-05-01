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
        while(lastName1.startsWith(" "))
        {
        	lastName1 = lastName1.replaceFirst(" ", "");
        }
        while(lastName2.startsWith(" "))
        {
        	lastName2 = lastName2.replaceFirst(" ", "");
        }
        while(firstName1.startsWith(" "))
        {
        	firstName1 = firstName1.replaceFirst(" ", "");
        }
        while(firstName2.startsWith(" "))
        {
        	firstName2 = firstName2.replaceFirst(" ", "");
        }
        
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
        while(lastName1.startsWith(" "))
        {
        	lastName1 = lastName1.replaceFirst(" ", "");
        }
        while(lastName2.startsWith(" "))
        {
        	lastName2 = lastName2.replaceFirst(" ", "");
        }
        while(firstName1.startsWith(" "))
        {
        	firstName1 = firstName1.replaceFirst(" ", "");
        }
        while(firstName2.startsWith(" "))
        {
        	firstName2 = firstName2.replaceFirst(" ", "");
        }

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
        String userId2 = ((Participant) otherParticipant).getId();
        return userId1.compareTo(userId2);

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
