 /***********************************************************************************
 *
 * Copyright (c) 2013 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.jsf.roster;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import org.apache.myfaces.renderkit.html.HtmlLinkRenderer;

import org.sakaiproject.util.FormattedText;

public class RosterSimpleEscapedLinkRenderer extends HtmlLinkRenderer {
	
	public void encodeBegin(FacesContext context, UIComponent component)
		      throws IOException
		  {
		    if (!component.isRendered()) return;
		    ResponseWriter writer = context.getResponseWriter();
		    String rawValue = (String) component.getAttributes().get("value");
		    //We eliminate all suspicious html tags 
		    String value = FormattedText.processFormattedText(rawValue, new StringBuilder(), true, false);
			//Can't put null value in component
			if (value == null) {
				value = "";
			}
 		    component.getAttributes().put ("value", value);
		    super.encodeBegin (context,component);
		    
		  }
	
}
