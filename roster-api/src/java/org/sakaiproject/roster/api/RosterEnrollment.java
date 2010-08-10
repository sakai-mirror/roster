/**
 * 
 */
package org.sakaiproject.roster.api;


/**
 * Container for an enrollment set.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterEnrollment {
	
	private String id;
	private String title;
	
	public RosterEnrollment() {
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
}
