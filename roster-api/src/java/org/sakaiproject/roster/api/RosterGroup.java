/**
 * 
 */
package org.sakaiproject.roster.api;

import java.util.List;

/**
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterGroup {

	private String id;
	private String title;
	private List<String> userIds;
	
	public RosterGroup() {
		
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

	public List<String> getUserIds() {
		return userIds;
	}

	public void setUserIds(List<String> userIds) {
		this.userIds = userIds;
	}
	
	
}
