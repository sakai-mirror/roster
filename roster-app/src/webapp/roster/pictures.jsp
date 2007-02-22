<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/extensions" prefix="x"%>
<%
response.setContentType("text/html; charset=UTF-8");
%>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.roster.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view_container title="#{msgs.facet_roster_pictures}">
		<sakai:view_content>
		
		<%="<script src=js/roster.js></script>"%>

			<h:form>
				<sakai:tool_bar>
					<sakai:tool_bar_item action="overview" value="#{msgs.navbar_overview}" />
					<sakai:tool_bar_item action="pictures" value="#{msgs.navbar_pics}" disabled="true" />
					<h:outputLink value="printFriendlyRoster" target="_new">
						<h:graphicImage url="/images/printer.png"
							alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					</h:outputLink>
				</sakai:tool_bar>

				<sakai:view_title value="#{msgs.title_show_roster}" />

				<h:outputLink rendered="#{overview.renderPrivacyMessage}"
					value="#{msgs.title_missing_participants_link}" target="_blank">
					<h:outputText value="#{msgs.title_missing_participants}" />
				</h:outputLink>

				<%@include file="inc/filter.jspf" %>

				<x:div styleClass="instruction">
					<h:outputText value="#{msgs.no_participants_msg}"
						rendered="#{empty filter.participants}" />
				</x:div>

				<x:newspaperTable newspaperColumns="5" newspaperOrientation="horizontal" value="#{pictures.participants}" var="participant">
					<h:column>
						<h:graphicImage id="profileImage" value="#{participant.profile.pictureUrl}" rendered="#{pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
						<h:graphicImage id="rosterImage" value="ParticipantImageServlet.prf?photo=#{participant.user.id}" rendered="#{ ! pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
						<f:verbatim><br/></f:verbatim>
						<h:outputText value="#{participant.user.sortName}"/>
					</h:column>
				</x:newspaperTable>

<%--
				<x:dataList layout="simple" value="#{pictures.participants}" var="participant">
					<h:graphicImage id="profileImage" value="#{participant.profile.pictureUrl}" rendered="#{pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
					<h:graphicImage id="rosterImage" value="ParticipantImageServlet.prf?photo=#{participant.user.id}" rendered="#{ ! pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
					<h:outputText value="#{participant.user.sortName}"/>
				</x:dataList>
--%>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
