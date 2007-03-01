<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
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

				<t:aliasBean alias="#{viewBean}" value="#{pictures}">
					<%@include file="inc/nav.jspf" %>
				</t:aliasBean>

				<h:outputLink rendered="#{overview.renderPrivacyMessage}"
					value="#{msgs.title_missing_participants_link}" target="_blank">
					<h:outputText value="#{msgs.title_missing_participants}" />
				</h:outputLink>

				<%@include file="inc/filter.jspf" %>

				<t:div styleClass="instruction">
					<h:outputText value="#{msgs.no_participants_msg}"
						rendered="#{empty filter.participants}" />
				</t:div>

				<t:dataTable newspaperColumns="5" newspaperOrientation="horizontal" value="#{pictures.participants}" var="participant">
					<h:column>
						<h:graphicImage id="profileImage" value="#{participant.profile.pictureUrl}" rendered="#{pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
						<h:graphicImage id="rosterImage" value="ParticipantImageServlet.prf?photo=#{participant.user.id}" rendered="#{ ! pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
						<f:verbatim><br/></f:verbatim>
						<h:outputText value="#{participant.user.sortName}"/>
					</h:column>
				</t:dataTable>

<%--
				<t:dataList layout="simple" value="#{pictures.participants}" var="participant">
					<h:graphicImage id="profileImage" value="#{participant.profile.pictureUrl}" rendered="#{pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
					<h:graphicImage id="rosterImage" value="ParticipantImageServlet.prf?photo=#{participant.user.id}" rendered="#{ ! pictures.displayProfilePhoto}" title="#{msgs.profile_picture_alt} #{participant.user.displayName}" styleClass="rosterImage"/>
					<h:outputText value="#{participant.user.sortName}"/>
				</t:dataList>
--%>
			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
