<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %> 
<%@ taglib uri="http://myfaces.apache.org/extensions" prefix="x"%>
<%--@ taglib uri="http://sakaiproject.org/jsf/profile" prefix="profile" --%> 
 <% response.setContentType("text/html; charset=UTF-8"); %>
<f:loadBundle basename="org.sakaiproject.tool.roster.bundle.Messages" var="msgs"/>
<%--TODO : apply stylesheet --%>
<f:view>
	<sakai:view_container title="Roster List">
		<sakai:view_content>
		<h:form>	
			<sakai:tool_bar>
				<sakai:tool_bar_item action="#{RosterTool.processActionToggleIdPhotos}" rendered="#{RosterTool.updateAccess}"  value="#{RosterTool.idPhotoText}"/>
				<sakai:tool_bar_item action="#{RosterTool.processActionToggleCustomPhotos}" value="#{RosterTool.customPhotoText}"/>
			 </sakai:tool_bar>
			 <sakai:view_title  value="#{RosterTool.title}"/>
			 <sakai:instruction_message value="#{msgs.title_msg}"/>
	 	  	  <h:outputLink rendered="#{!RosterTool.updateAccess}" value="#{msgs.title_missing_participants_link}" target="_blank" >
			  	 <sakai:instruction_message value="#{msgs.title_missing_participants}"/>
			  </h:outputLink>
	  		 
	  		 <sakai:panel_edit>
	 			<h:outputText value="#{msgs.roster_view}"/>
		 		<h:selectOneMenu  onchange="this.form.submit();"  valueChangeListener="#{RosterTool.processValueChangeForView}" value="#{RosterTool.displayView}">  
					<f:selectItem itemValue="role" itemLabel="#{msgs.roster_defaultViewLabel}" />
					<f:selectItem itemValue="all" itemLabel="#{msgs.roster_viewAllLabel}" />
				</h:selectOneMenu>
			 </sakai:panel_edit> 	
		</h:form>
		 <h:form target="neWindow">
		 	<%--********************* Users View by Role*********************--%>
			<h:panelGrid width="70%" rendered="#{RosterTool.displayByRole}">
				<sakai:flat_list value="#{RosterTool.roles}" var="searchResultRoles">
				 	<h:column>				
					 	<h:outputText value="#{searchResultRoles.role.id}(#{searchResultRoles.userCount})" />					
		 		 		<sakai:flat_list value="#{searchResultRoles.users}" var="searchResult">
		 		 		<%--<x:dataTable cellpadding="3" cellspacing="3"
							id="participantByRoleTable"
							value="#{searchResultRoles.users}"
							var="searchResult"
							sortColumn="#{RosterTool.sortColumn}"
				            sortAscending="#{RosterTool.sortAscending}">--%>
							<%--TODO merge the two sakai flat lists --%>
							<h:column>	
								<f:facet name="header">
									<h:outputText value="#{RosterTool.facet}" />
								</f:facet>
								<h:graphicImage value="ParticipantImageServlet.prf?customPhoto=#{searchResult.participant.id}" height="75" width="75" rendered="#{RosterTool.showCustomPhoto}"/>
								<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResult.participant.id}" width="75" rendered="#{RosterTool.showIdPhoto}"/>
							</h:column>	
							<h:column>
								<f:facet name="header">
									<h:outputText value="#{msgs.facet_profile}" />
								</f:facet>				
								<h:commandLink  action="#{RosterTool.processActionDisplayProfile}" value="Profile">
									<f:param value="#{searchResult.participant.id}" name="participantId"/>
								</h:commandLink>
						 	</h:column>
							
							<h:column>
								<f:facet name="header">
								  <%--	<x:commandSortHeader columnName="lastName" immediate="true" arrow="true">--%>
										<h:outputText value="#{msgs.facet_name}" />
		           					<%--</x:commandSortHeader>	--%>								
								</f:facet>								
								<h:outputText value="#{searchResult.participant.lastName}" /> 
								<h:outputText value=", " /> 
								<h:outputText value="#{searchResult.participant.firstName}" /> 
							</h:column>
							
							<h:column>
								<f:facet name="header">
									<h:outputText value="#{msgs.facet_userId}" />
								</f:facet>								
								<h:outputText value="#{searchResult.participant.id}" /> 						
							</h:column>
						<%--	</x:dataTable> --%>
						</sakai:flat_list>
					</h:column>	
				</sakai:flat_list> 	
			</h:panelGrid> 
			<%--********************* All Users View *********************--%>
			<h:panelGrid width="70%" rendered="#{RosterTool.displayAllUsers}">
				<h:outputText value="All Users(#{RosterTool.allUserCount})" />							 		 		
				<sakai:flat_list value="#{RosterTool.allUsers}" var="searchResultAll">
					<h:column>	
							<f:facet name="header">
								<h:outputText value="#{RosterTool.facet}" />
							</f:facet>
							<h:graphicImage value="ParticipantImageServlet.prf?customPhoto=#{searchResultAll.participant.id}" height="75" width="75" rendered="#{RosterTool.showCustomPhoto}"/>
							<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResultAll.participant.id}" width="75" rendered="#{RosterTool.showIdPhoto}"/>
						</h:column>	
						<h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.facet_profile}" />
							</f:facet>				
							<h:commandLink  action="#{RosterTool.processActionDisplayProfile}" value="Profile" >
								<f:param value="#{searchResultAll.participant.id}" name="participantId"/>
							</h:commandLink>
						</h:column>
						
						<h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.facet_name}" />
							</f:facet>								
							<h:outputText value="#{searchResultAll.participant.lastName}" /> 
							<h:outputText value=", " /> 
							<h:outputText value="#{searchResultAll.participant.firstName}" /> 
						</h:column>
						
						<h:column>
							<f:facet name="header">
								<h:outputText value="#{msgs.facet_userId}" />
							</f:facet>								
							<h:outputText value="#{searchResultAll.participant.id}" /> 						
						</h:column>
				 </sakai:flat_list> 
			</h:panelGrid> 
		 </h:form>
  </sakai:view_content>	
</sakai:view_container>
</f:view> 