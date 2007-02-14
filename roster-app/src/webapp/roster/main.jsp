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
	<sakai:view_container title="#{msgs.facet_roster_list}">
		<sakai:view_content>
			<h:form>
				<sakai:tool_bar>
					<sakai:tool_bar_item disabled="true"
						value="#{msgs.navbar_overview}" />
					<sakai:tool_bar_item action="pictures" value="#{msgs.navbar_pics}" />
					<h:outputLink value="printFriendlyRoster" target="_new">
						<h:graphicImage url="/images/printer.png"
							alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					</h:outputLink>
				</sakai:tool_bar>

				<sakai:view_title value="#{msgs.title_show_roster}" />

				<h:outputText value="#{msgs.title_msg}"
					rendered="#{overview.renderModifyMembersInstructions}" styleClass="instruction"
					style="display: block;" />

				<h:outputLink rendered="#{overview.renderPrivacyMessage}"
					value="#{msgs.title_missing_participants_link}" target="_blank">
					<h:outputText value="#{msgs.title_missing_participants}" />
				</h:outputLink>

				<x:div styleClass="navPanel" style="width: 98%">
					<x:div styleClass="viewNav" style="width: 100%">

					</x:div>
				</x:div>

				<x:div styleClass="instruction">
					<h:outputText value="#{msgs.no_participants_msg}"
						rendered="#{empty filter.participants}" />
				</x:div>


			    <x:dataTable cellpadding="0" cellspacing="0"
			        id="rosterTable"
			        value="#{filter.participants}"
			        var="participant"
			        binding="#{overview.rosterDataTable}"
			        sortColumn="#{prefs.sortColumn}"
			        sortAscending="#{prefs.sortAscending}"
			        styleClass="listHier rosterTable">
			        <h:column>
			            <f:facet name="header">
			                <x:commandSortHeader columnName="displayName" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_name}" />
			                </x:commandSortHeader>
			            </f:facet>
						<h:commandLink action="profile" value="#{participant.user.displayName}">
							<f:param name="participantId" value="#{participant.user.id}" />
						</h:commandLink>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <x:commandSortHeader columnName="displayId" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_userId}" />
			                </x:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.user.displayId}"/>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <x:commandSortHeader columnName="email" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_email}" />
			                </x:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.user.email}"/>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <x:commandSortHeader columnName="roleId" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_role}" />
			                </x:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.roleTitle}"/>
			        </h:column>
			        
			        <%/* A dynamic number of section columns will be appended here by the backing bean */%>
			    
			    </x:dataTable>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
