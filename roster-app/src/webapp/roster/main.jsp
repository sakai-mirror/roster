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
	<sakai:view_container title="#{msgs.facet_roster_list}">
		<sakai:view_content>
		
		<%="<script src=js/roster.js></script>"%>

			<h:form>

				<t:aliasBean alias="#{viewBean}" value="#{overview}">
					<%@include file="inc/nav.jspf" %>
				</t:aliasBean>

				<h:outputText value="#{msgs.title_msg}"
					rendered="#{overview.renderModifyMembersInstructions}" styleClass="instruction"
					style="display: block;" />

				<h:outputLink rendered="#{overview.renderPrivacyMessage}"
					value="#{msgs.title_missing_participants_link}" target="_blank">
					<h:outputText value="#{msgs.title_missing_participants}" />
				</h:outputLink>

				<%@include file="inc/filter.jspf" %>

				<t:div styleClass="instruction">
					<h:outputText value="#{msgs.no_participants_msg}"
						rendered="#{empty filter.participants}" />
				</t:div>

			    <t:dataTable cellpadding="0" cellspacing="0"
			        id="rosterTable"
			        value="#{overview.participants}"
			        var="participant"
			        binding="#{overview.rosterDataTable}"
			        sortColumn="#{prefs.sortColumn}"
			        sortAscending="#{prefs.sortAscending}"
			        styleClass="listHier rosterTable">
			        <h:column>
			            <f:facet name="header">
			                <t:commandSortHeader columnName="displayName" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_name}" />
			                </t:commandSortHeader>
			            </f:facet>
						<h:commandLink action="#{profileBean.displayProfile}" value="#{participant.user.displayName}">
							<f:param name="participantId" value="#{participant.user.id}" />
						</h:commandLink>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <t:commandSortHeader columnName="displayId" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_userId}" />
			                </t:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.user.displayId}"/>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <t:commandSortHeader columnName="email" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_email}" />
			                </t:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.user.email}"/>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <t:commandSortHeader columnName="roleId" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_role}" />
			                </t:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.roleTitle}"/>
			        </h:column>
			        
			        <%/* A dynamic number of section columns will be appended here by the backing bean */%>
			    
			    </t:dataTable>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
