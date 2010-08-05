/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.roster.tool.entityprovider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.roster.api.SakaiProxy;

/**
 * <code>EntityProvider</code> to allow Roster to access site, membership, and
 * enrollment data for the current user. The provider respects Roster
 * permissions, so shouldn't expose any data the current user should not have
 * access to.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterSiteEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable, Outputable {

	private static final Log log = LogFactory.getLog(RosterSiteEntityProvider.class);
	
	public final static String ENTITY_PREFIX		= "roster-membership";
	public final static String DEFAULT_ID			= ":ID:";
	
	public final static String ERROR_INVALID_SITE	= "Invalid site ID";
	
	private SakaiProxy sakaiProxy;
		
	public RosterSiteEntityProvider() {
		
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}
	
	@EntityCustomAction(action = "get-membership", viewKey = EntityView.VIEW_SHOW)
	public Object getMembership(EntityReference reference) {
		
		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			return ERROR_INVALID_SITE;
		}
		
		return sakaiProxy.getMembership(reference.getId(), null);
	}
	
	@EntityCustomAction(action = "get-roles", viewKey = EntityView.VIEW_SHOW)
	public Object getRoles(EntityReference reference) {
		
		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			return ERROR_INVALID_SITE;
		}	
		
		return sakaiProxy.getRoleTypes(reference.getId());
	}
	
	@EntityCustomAction(action = "get-site", viewKey = EntityView.VIEW_SHOW)
	public Object getSite(EntityReference reference) {
		
		if (null == reference.getId() || DEFAULT_ID.equals(reference.getId())) {
			return ERROR_INVALID_SITE;
		}
		
		return sakaiProxy.getSiteDetails(reference.getId());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String[] getHandledOutputFormats() {
		return new String[] { Formats.JSON };
	}
	
	/* Spring injections */
	
	public void setSakaiProxy(SakaiProxy sakaiProxy) {
		this.sakaiProxy = sakaiProxy;
	}
    
	
}
