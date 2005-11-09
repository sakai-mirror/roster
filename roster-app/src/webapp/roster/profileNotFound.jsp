<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %> 
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:loadBundle basename="org.sakaiproject.tool.roster.bundle.Messages" var="msgs"/>
<f:view >
 <h:form>
	<sakai:view_container title="Roster Profile">
		<sakai:view_content >
	 		<h:panelGrid>
			 	<h:graphicImage id="image"  alt="No Picture is Available" url="/images/pictureUnavailable.jpg"/>			
		 	</h:panelGrid>
			 
		 	<h4>
				<h:panelGrid style="instructor" columns="1"  border="0">
					<h:outputText  value="Public Information"/>	
				</h:panelGrid>
			</h4>
		 		<h:outputText value ="No public information is available for this user."/>
 		 	<h4>
				<h:panelGrid style="instructor" columns="1"  border="0">
					<h:outputText  value="Personal Information"/>	
				</h:panelGrid>
			</h4>
 		 	<h:panelGrid>
			 	<jsp:include page="personalInfoUnavailable.jsp"/>			
			</h:panelGrid>
			 	
 		 	 <h:panelGrid>
			 	<h:commandButton  id="submit"  value="#{msgs.back}"  immediate="true" action="#{RosterTool.processCancel}" />
			 </h:panelGrid>	
	</sakai:view_content>	
	</sakai:view_container>	
	</h:form>
</f:view> 