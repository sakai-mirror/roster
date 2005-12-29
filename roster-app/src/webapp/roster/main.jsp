<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %> 
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:loadBundle basename="org.sakaiproject.tool.roster.bundle.Messages" var="msgs"/>
 <link href='/sakai-roster-tool/css/roster.css' rel='stylesheet' type='text/css' /> 
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
	 	  	  <h:outputLink rendered="#{RosterTool.renderPrivacyAlert}" value="#{RosterTool.privacyAlertUrl}" target="_blank" >
			  	 <sakai:instruction_message value="#{RosterTool.privacyAlert}"/>
			  </h:outputLink>
	  		 
	  		 <sakai:panel_edit>
	 			<h:outputText value="#{msgs.roster_view}"/>
		 		<h:selectOneMenu  onchange="this.form.submit();"  valueChangeListener="#{RosterTool.processValueChangeForView}" value="#{RosterTool.displayView}">  
					<f:selectItem itemValue="role" itemLabel="#{msgs.roster_defaultViewLabel}" />
					<f:selectItem itemValue="all" itemLabel="#{msgs.roster_viewAllLabel}" />
				</h:selectOneMenu>
			 </sakai:panel_edit> 	
		 	<%--********************* Users View by Role*********************--%>
		   		<h:dataTable width="90%" id="allRoles"  rendered="#{RosterTool.displayByRole}" value="#{RosterTool.roles}" var="searchResultRoles">
				 	<h:column>				
					 	<f:verbatim><h4></f:verbatim>
					 		<h:outputText value="#{searchResultRoles.role.id} (#{searchResultRoles.userCount})" />					
		 		 		<f:verbatim></h4></f:verbatim>
		 		 		<h:dataTable columnClasses="rosterGroupByRoleColumns_1,rosterGroupByRoleColumns_2" styleClass="listHier"  id="participantsByRole"  value="#{searchResultRoles.users}" var="searchResult">
		 		 		  <%--TODO merge the two sakai flat lists --%>
							<h:column rendered="#{RosterTool.renderPhotoColumn}">	
								<f:facet name="header">
									<h:outputText value="#{RosterTool.facet}" />
								</f:facet>
								<h:graphicImage value="/images/pictureUnavailable.jpg" height="75" width="75" rendered="#{searchResult.showCustomPhotoUnavailable}"/>
								<h:graphicImage value="#{searchResult.participant.profile.pictureUrl}" height="75" width="75" rendered="#{searchResult.showURLPhoto}"/>
								<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResult.participant.id}"  width="75" rendered="#{searchResult.showCustomIdPhoto}"/>
								<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResult.participant.id}" width="75" rendered="#{RosterTool.showIdPhoto}"/>
							</h:column>	
							<h:column>
								<f:facet name="header">
									<h:outputText value="#{msgs.facet_profile}" />
								</f:facet>				
								<h:commandLink  target="_blank" action="#{RosterTool.processActionDisplayProfile}" value="Profile">
									<f:param value="#{searchResult.participant.id}" name="participantId"/>
								</h:commandLink>
						 	</h:column>
							
							<h:column>
								<f:facet name="header">
								   	<h:commandLink action="#{searchResultRoles.toggleLastNameSort}">
								   		<h:outputText value="#{msgs.facet_name}" />
								   		<h:graphicImage value="/images/sortdescending.gif" rendered="#{searchResultRoles.role_sortLastNameDescending}"/>
								   		<h:graphicImage value="/images/sortascending.gif" rendered="#{searchResultRoles.role_sortLastNameAscending}"/>
									</h:commandLink>
								</f:facet>								
								<h:outputText value="#{searchResult.participant.lastName}" /> 
								<h:outputText value=", " /> 
								<h:outputText value="#{searchResult.participant.firstName}" /> 
							</h:column>
							
							<h:column>
								<f:facet name="header">
									<h:commandLink action="#{searchResultRoles.toggleUserIdSort}">
										<h:outputText value="#{msgs.facet_userId}" />
										<h:graphicImage value="/images/sortdescending.gif" rendered="#{searchResultRoles.role_sortUserIdDescending}"/>
									   	<h:graphicImage value="/images/sortascending.gif" rendered="#{searchResultRoles.role_sortUserIdAscending}"/>
								   	</h:commandLink>
								</f:facet>								
								<h:outputText value="#{searchResult.participant.id}" /> 						
							</h:column>
						<%--	</x:dataTable> --%>
						</h:dataTable>
					</h:column>	
				</h:dataTable> 	
		 
			<%--********************* All Users View *********************--%>
			<h:dataTable width="90%"  id="allUserRosterDisplay" rendered="#{RosterTool.displayAllUsers}" value="1">
				<h:column>
					<f:verbatim><h4></f:verbatim>
						<h:outputText value="All Users (#{RosterTool.allUserCount})" />							 		 		
					<f:verbatim></h4></f:verbatim>
			 
					<h:dataTable styleClass="listHier" id="allUserRoster" value="#{RosterTool.allUsers}" var="searchResultAll">
					<h:column rendered="#{RosterTool.renderPhotoColumn}">	
							<f:facet name="header">
								<h:outputText value="#{RosterTool.facet}" />
							</f:facet>
							<h:graphicImage value="/images/pictureUnavailable.jpg" height="75" width="75" rendered="#{searchResultAll.showCustomPhotoUnavailable}"/>
							<h:graphicImage value="#{searchResultAll.participant.profile.pictureUrl}" height="75" width="75" rendered="#{searchResultAll.showURLPhoto}"/>
							<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResultAll.participant.id}"  width="75" rendered="#{searchResultAll.showCustomIdPhoto}"/>
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
								<h:commandLink action="#{RosterTool.toggleLastNameSort}">
							   		<h:outputText value="#{msgs.facet_name}" />
							   		<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortLastNameDescending}"/>
							   		<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortLastNameAscending}"/>
								</h:commandLink>
							</f:facet>								
							<h:outputText value="#{searchResultAll.participant.lastName}" /> 
							<h:outputText value=", " /> 
							<h:outputText value="#{searchResultAll.participant.firstName}" /> 
						</h:column>
						
						<h:column>
							<f:facet name="header">
								<h:commandLink action="#{RosterTool.toggleUserIdSort}">
									<h:outputText value="#{msgs.facet_userId}" />
									<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortUserIdDescending}"/>
								   	<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortUserIdAscending}"/>
							   	</h:commandLink>
							</f:facet>								
							<h:outputText value="#{searchResultAll.participant.id}" /> 						
						</h:column>
				 </h:dataTable> 
				  </h:column>
			</h:dataTable> 
		 </h:form>
  </sakai:view_content>	
</sakai:view_container>
</f:view> 