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
    <sakai:view title="#{msgs.facet_roster_pictures}" toolCssHref="/sakai-roster-tool/css/roster.css">
		<%="<script src=js/roster.js></script>"%>
        <h:form id="roster_form">
            <t:aliasBean alias="#{viewBean}" value="#{pictures}">
                <%@include file="inc/nav.jspf" %>
            </t:aliasBean>

            <%@include file="inc/filter.jspf" %>

            <h:panelGrid columns="2" styleClass="rosterPicturesFilter" columnClasses="rosterPageHeaderLeft,rosterPageHeaderRight">
                <t:selectOneRadio  value="#{prefs.displayProfilePhotos}" onchange="this.form.submit()" immediate="true">
                    <f:selectItems value="#{pictures.photoSelectItems}" />
                </t:selectOneRadio>
	
                <h:commandButton value="#{msgs.roster_show_names}" actionListener="#{pictures.showNames}" rendered="#{ ! prefs.displayNames}" />
                <h:commandButton value="#{msgs.roster_hide_names}" actionListener="#{pictures.hideNames}" rendered="#{prefs.displayNames}"/>
            </h:panelGrid>

            <t:div styleClass="instruction">
                <h:outputFormat value="#{msgs.no_participants_msg}"
                              rendered="#{empty filter.participants}" >
                    <f:param value="#{filter.searchFilter}"/>
                </h:outputFormat>
            </t:div>

            <t:dataTable
                    newspaperColumns="5"
                    newspaperOrientation="horizontal"
                    value="#{pictures.participants}"
                    var="participant"
                    styleClass="rosterPictures">
                <h:column>
                    <t:div>
                        <h:graphicImage
                                id="profileImage"
                                value="#{participant.profile.pictureUrl}"
                                rendered="#{prefs.displayProfilePhotos && not empty participant.profile.pictureUrl}"
                                title="#{msgs.profile_picture_alt} #{participant.user.displayName}"
                                styleClass="rosterImage"/>
							
                        <h:graphicImage
                                id="profileImageNotAvailable"
                                value="#{msgs.img_unavail}"
                                rendered="#{prefs.displayProfilePhotos && empty participant.profile.pictureUrl}"
                                title="#{msgs.profile_no_picture_available}"
                                styleClass="rosterImage"/>

                        <h:graphicImage
                                id="rosterImage"
                                value="ParticipantImageServlet.prf?photo=#{participant.user.id}"
                                rendered="#{ ! prefs.displayProfilePhotos}"
                                title="#{msgs.profile_picture_alt} #{participant.user.displayName}"
                                styleClass="rosterImage"/>
							
                    </t:div>
                    <t:div rendered="#{prefs.displayNames}">
                        <t:div>
                            <h:outputFormat value="#{participant.user.firstName}">
                                <f:converter converterId="textTruncateConverter"/>
                            </h:outputFormat>
                        </t:div>
                        <t:div>
                            <h:outputFormat value="#{participant.user.lastName}">
                                <f:converter converterId="textTruncateConverter"/>
                            </h:outputFormat>
                        </t:div>
                    </t:div>

                    <t:div>
                        <h:commandLink action="#{profileBean.displayProfile}" value="#{participant.user.displayId}" title="#{msgs.show_profile}">
                            <f:param name="participantId" value="#{participant.user.id}" />
                            <f:param name="returnPage" value="pictures" />
                        </h:commandLink>
                    </t:div>
                </h:column>
            </t:dataTable>
        </h:form>
    </sakai:view>
</f:view>
