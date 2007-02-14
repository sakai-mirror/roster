<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%
response.setContentType("text/html; charset=UTF-8");
%>


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

				<x:div class="navPanel" style="width: 98%">
					<x:div class="viewNav" style="width: 100%">

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
			        sortColumn="#{overview.sortColumn}"
			        sortAscending="#{preferencesBean.rosterSortAscending}"
			        styleClass="listHier rosterTable">
			        <h:column>
			            <f:facet name="header">
			                <x:commandSortHeader columnName="studentName" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.roster_table_header_name}" />
			                </x:commandSortHeader>
			            </f:facet>
						<h:commandLink action="profile" value="#{participant.user.displayName}">
							<f:param name="participantId" value="#{participant.id}" />
						</h:commandLink>
			        </h:column>
			        <h:column>
			            <f:facet name="header">
			                <x:commandSortHeader columnName="displayId" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.roster_table_header_id}" />
			                </x:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{enrollment.user.displayId}"/>
			        </h:column>
			        
			        <%/* A dynamic number of section columns will be appended here by the backing bean */%>
			    
			    </x:dataTable>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
