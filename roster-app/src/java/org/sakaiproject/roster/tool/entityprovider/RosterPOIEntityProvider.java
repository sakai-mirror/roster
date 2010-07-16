/**
 * 
 */
package org.sakaiproject.roster.tool.entityprovider;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

/**
 * <code>EntityProvider</code> to allow Roster to export to Excel via Apache's
 * POI.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 */
public class RosterPOIEntityProvider extends AbstractEntityProvider implements
		AutoRegisterEntityProvider, ActionsExecutable {

	private static final Log log = LogFactory.getLog(RosterPOIEntityProvider.class);
	
	public final static String ENTITY_PREFIX	= "roster";
	public final static String DEFAULT_ID		= ":ID:";
	public final static String DEFAULT_MSG		= "Roster export to Excel method";
	public final static String INVALID_ID		= "Invalid site ID";
	
	/**
	 * {@inheritDoc}
	 */
	public String getEntityPrefix() {

		return ENTITY_PREFIX;
	}

	@EntityCustomAction(action = "export-roster-to-excel", viewKey = EntityView.VIEW_SHOW)
	public String test(EntityReference reference) {
		if (StringUtils.isBlank(reference.getId())) {
			return INVALID_ID;
		} else if (DEFAULT_ID.equals(reference.getId())) {
			return DEFAULT_MSG;
		} else
			return "test";
	}
	
}
