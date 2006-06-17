<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%--TODO: Refactor Profile Code in order to use profile custom components--%>
<%@ taglib uri="http://sakaiproject.org/jsf/roster" prefix="roster" %> 
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
	<sakai:view_container title="#{msgs.profile_site_roster}">
		<sakai:view_content>
			<h:form>
				<sakai:view_title value="#{msgs.profile_user_profile}"/>
				<%-- TODO: should the image sizes be predetermine for this view? --%>
				<h:graphicImage value="#{msgs.img_unavail}"  rendered="#{RosterTool.participant.showCustomPhotoUnavailableForSelectedProfile}" title="#{msgs.profile_no_picture_available}" styleClass="rosterImage"/>
				<h:graphicImage value="#{RosterTool.participant.participant.profile.pictureUrl}"  rendered="#{RosterTool.participant.showURLPhotoForSelectedProfile}" title="#{msgs.profile_picture_alt} #{RosterTool.participant.participant.profile.firstName} #{RosterTool.participant.participant.profile.lastName}" styleClass="rosterImage"/>
				<h:graphicImage value="ParticipantImageServlet.prf?photo=#{RosterTool.participant.participant.id}"  rendered="#{RosterTool.participant.showCustomIdPhotoForSelectedProfile}" title="#{msgs.profile_picture_alt} #{RosterTool.participant.participant.profile.firstName} #{RosterTool.participant.participant.profile.lastName}" styleClass="rosterImage"/>
				<h4>
					<h:outputText value="#{msgs.profile_public_information}"/>
				</h4>
				<sakai:panel_edit>
					<h:outputText value="#{msgs.profile_first_name}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.firstName}"/>
					<h:outputText value="#{msgs.profile_last_name}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.lastName}"/>
					<h:outputText value="#{msgs.profile_nick_name}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.nickName}"/>
					<h:outputText value="#{msgs.profile_position}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.position}"/>
					<h:outputText value="#{msgs.profile_department}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.department}"/>
					<h:outputText value="#{msgs.profile_school}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.school}"/>
					<h:outputText value="#{msgs.profile_room}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.room}"/>
				</sakai:panel_edit>
				<h4>
					<h:outputText value="#{msgs.profile_personal_information}"/>
				</h4>
				<sakai:panel_edit>
					<h:outputText  value="#{msgs.profile_email}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.email} "/>
					<h:outputText value="#{msgs.profile_homepage}"/>
					<%--need to test for empty value here - and omit the outputLink if null --%>				
					<h:outputLink target="_blank" value="#{RosterTool.participant.participant.profile.homepage}">
						<h:outputText value="#{RosterTool.participant.participant.profile.homepage}"/>
					</h:outputLink>
					<h:outputText  value="#{msgs.profile_work_phone}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.workPhone}"/>
					<h:outputText value="#{msgs.profile_home_phone}"/>
					<h:outputText value="#{RosterTool.participant.participant.profile.homePhone}"/>
					<h:outputText value="#{msgs.profile_other_information}"/>
					<roster:roster_display_HTML value="#{RosterTool.participant.participant.profile.otherInformation}"/>
				</sakai:panel_edit>
				<p class="act">
					<h:commandButton styleClass="active" accesskey="x" id="submit" value="#{msgs.back}" immediate="true" action="#{RosterTool.processCancel}"/>
				</p>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
