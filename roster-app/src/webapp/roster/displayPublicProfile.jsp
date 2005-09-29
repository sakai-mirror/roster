<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
 
<%--<%@ taglib uri="http://sakaiproject.org/jsf/profile" prefix="profile" %> --%>
<f:loadBundle basename="org.sakaiproject.tool.roster.bundle.Messages" var="msgs"/>
<% response.setContentType("text/html; charset=UTF-8"); %>
 <f:view>
 <h:form>
	<sakai:view_container title="Site Roster">
		<sakai:view_content>
		    <sakai:view_title  value="User Profile"/>
			<h:panelGrid columns="1" border="0" >
			<%-- TODO: should the image sizes be predetermine for this view? --%>
				<h:graphicImage alt="No Picture is Available"  value="/images/pictureUnavailable.jpg" height="75" width="75"/>					
			</h:panelGrid>			
			<h4><h:outputText  value="Public Information"/>	</h4>			 
				<p class="shorttext">
			 	<sakai:panel_edit >	
					<h:outputLabel style ="shorttext" value="#{msgs.profile_first_name}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.firstName}"/> 
					<h:outputLabel value="#{msgs.profile_last_name}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.lastName}"/>
					<h:outputLabel value="#{msgs.profile_nick_name}"/>				
					<h:outputText value="#{RosterTool.participant.participant.profile.nickName}"/>
					<h:outputLabel value="#{msgs.profile_position}"/> 				
					<h:outputText value="#{RosterTool.participant.participant.profile.position}"/> 
					<h:outputLabel value="#{msgs.profile_department}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.department}"/> 
					<h:outputLabel value="#{msgs.profile_school}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.school}"/>
					<h:outputLabel value="#{msgs.profile_room}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.room}"/> 
				</sakai:panel_edit>
				</p>
			 <h4><h:outputText  value="Personal Information"/></h4>
		  		<jsp:include page="personalInfoUnavailable.jsp"/>
	 		 
		 	 <h:panelGrid>
			 	<h:commandButton  id="submit" immediate="true" value="#{msgs.cancel}"  onclick="window.close()" />
			 </h:panelGrid>	
  		</sakai:view_content>
	</sakai:view_container>
  </h:form>
</f:view>

	