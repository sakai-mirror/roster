<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
 
<%--<%@ taglib uri="http://sakaiproject.org/jsf/profile" prefix="profile" %> --%>
<% response.setContentType("text/html; charset=UTF-8"); %>
 <f:view>
 <h:form>
	<sakai:view_container title="#{msgs.profile_site_roster}">
		<sakai:view_content>
		    <sakai:view_title  value="#{msgs.profile_usr_profile}"/>
			<h:panelGrid columns="1" border="0" >
			<%-- TODO: should the image sizes be predetermine for this view? --%>
				<h:graphicImage alt="#{msgs.profile_no_picture_available}"  value="#{msgs.img_unavail}" height="75" width="75"/>					
			</h:panelGrid>			
			<h4><h:outputText  value="#{msgs.profile_public_information}"/>	</h4>			 
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
			 <h4><h:outputText  value="#{msgs.profile_personal_information}"/></h4>
		  		<jsp:include page="personalInfoUnavailable.jsp"/>
	 		 
		 	 <h:panelGrid>
			 	<h:commandButton  id="submit"  value="#{msgs.back}" immediate="true" action="#{RosterTool.processCancel}" />
			 </h:panelGrid>	
  		</sakai:view_content>
	</sakai:view_container>
  </h:form>
</f:view>

	
