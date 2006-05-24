<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %> 
<% response.setContentType("text/html; charset=UTF-8"); %>
<f:view>
	<sakai:view_container title="#{msgs.facet_roster_list}">
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
		  		 		<f:verbatim>
		  		 			<div class="navPanel">
		  		 				<div class="viewNav">
		  		 		</f:verbatim>
							<h:panelGrid columns="1" style="width:100%;margin:0" cellspacing="0" cellpadding="0">
								<h:panelGroup>
									<h:outputLabel for="select1">
										<h:outputText value="#{msgs.roster_view}"/>
									</h:outputLabel>	
									<h:selectOneMenu  id="select1" onchange="this.form.submit();"  valueChangeListener="#{RosterTool.processValueChangeForView}" value="#{RosterTool.displayView}">  
										<f:selectItem itemValue="role" itemLabel="#{msgs.roster_defaultViewLabel}" />
										<f:selectItem itemValue="all" itemLabel="#{msgs.roster_viewAllLabel}" />
									</h:selectOneMenu>
							</h:panelGroup>	
						</h:panelGrid>
						 <f:verbatim>
							</div>
							</div>
						 </f:verbatim>

		 	<%--********************* Users View by Role*********************--%>
	   		<h:dataTable width="90%" id="allRoles"  rendered="#{RosterTool.displayByRole}" value="#{RosterTool.roles}" var="searchResultRoles" summary="layout">
				<h:column>			
					<f:verbatim><h4></f:verbatim>
						<h:outputText value="#{searchResultRoles.role.id} (#{searchResultRoles.userCount})" />					
			 		 <f:verbatim></h4></f:verbatim>
			 		 <h:dataTable columnClasses="rosterGroupByRoleColumns_1,rosterGroupByRoleColumns_2" styleClass="listHier lines nolines"  id="participantsByRole"  value="#{searchResultRoles.users}" var="searchResult"  summary="#{searchResultRoles.role.id}  - #{msgs.roster_list_summary}">
	 		 		  <%--TODO merge the two sakai flat lists --%>
					<h:column rendered="#{RosterTool.renderPhotoColumn}">	
						<f:facet name="header">
							<h:outputText value="#{RosterTool.facet}" />
						</f:facet>
						<h:graphicImage value="#{msgs.img_unavail}" rendered="#{searchResult.showCustomPhotoUnavailable}" alt="#{msgs.profile_no_picture_available}" styleClass="rosterImage"/>
						<h:graphicImage value="#{searchResult.participant.profile.pictureUrl}" rendered="#{searchResult.showURLPhoto}" alt="#{msgs.profile_picture_alt} #{searchResult.participant.firstName} #{searchResult.participant.firstName}" styleClass="rosterImage"/>
						<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResult.participant.id}"  rendered="#{searchResult.showCustomIdPhoto}" alt="#{msgs.profile_picture_alt} #{searchResult.participant.firstName} #{searchResult.participant.lastName}" styleClass="rosterImage"/>
						<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResult.participant.id}"  rendered="#{RosterTool.showIdPhoto}" alt="#{msgs.profile_picture_alt} #{searchResult.participant.firstName} #{searchResult.participant.lastName}" styleClass="rosterImage"/>
					</h:column>	
					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.facet_profile}" />
						</f:facet>				
						<h:commandLink  target="_blank" action="#{RosterTool.processActionDisplayProfile}" value="#{msgs.view_profile}" title="#{msgs.view_profile_title} #{searchResult.participant.firstName} #{searchResult.participant.lastName}">
							<f:param value="#{searchResult.participant.id}" name="participantId"/>
						</h:commandLink>
				 	</h:column>
								
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{searchResultRoles.toggleLastNameSort}" title="#{msgs.view_profile_list_sort_name}">
						   		<h:outputText value="#{msgs.facet_name}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{searchResultRoles.role_sortLastNameDescending}" alt="#{msgs.view_profile_list_sort_name} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{searchResultRoles.role_sortLastNameAscending}" alt="#{msgs.view_profile_list_sort_name} #{msgs.view_profile_list_sort_asc}"/>
							</h:commandLink>
						</f:facet>								
						<h:outputText value="#{searchResult.participant.lastName}" /> 
						<h:outputText value=", " /> 
						<h:outputText value="#{searchResult.participant.firstName}" /> 
					</h:column>
								
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{searchResultRoles.toggleUserIdSort}" title="#{msgs.view_profile_list_sort_id}">
								<h:outputText value="#{msgs.facet_userId}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{searchResultRoles.role_sortUserIdDescending}" alt="#{msgs.view_profile_list_sort_id} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{searchResultRoles.role_sortUserIdAscending}" alt="#{msgs.view_profile_list_sort_id} #{msgs.view_profile_list_sort_asc}"/>
						   	</h:commandLink>
						</f:facet>								
						<h:outputText value="#{searchResult.participant.eid}" />
					</h:column>
				</h:dataTable>
			</h:column>	
		</h:dataTable> 	
		 
			<%--********************* All Users View *********************--%>

		<h:dataTable width="90%"  id="allUserRosterDisplay" rendered="#{RosterTool.displayAllUsers}" value="1"  summary="layout">
			<h:column>
				<f:verbatim><h4></f:verbatim>
				<h:outputText value="#{msgs.view_profile_list_all_users} (#{RosterTool.allUserCount})" />							 		 		
				<f:verbatim></h4></f:verbatim>
		 
				<h:dataTable styleClass="listHier lines nolines" id="allUserRoster" value="#{RosterTool.allUsers}" var="searchResultAll" summary="#{msgs.view_profile_list_all_users}  - #{msgs.roster_list_summary}">
					<h:column rendered="#{RosterTool.renderPhotoColumn}">	
						<f:facet name="header">
							<h:outputText value="#{RosterTool.facet}" />
						</f:facet>
						<h:graphicImage value="#{msgs.img_unavail}"  rendered="#{searchResultAll.showCustomPhotoUnavailable}"  alt="#{msgs.profile_no_picture_available}" styleClass="rosterImage"/>
						<h:graphicImage value="#{searchResultAll.participant.profile.pictureUrl}" rendered="#{searchResultAll.showURLPhoto}" alt="#{msgs.profile_picture_alt} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}" styleClass="rosterImage"/>
						<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResultAll.participant.id}"  rendered="#{searchResultAll.showCustomIdPhoto}" alt="#{msgs.profile_picture_alt} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}" styleClass="rosterImage"/>
						<h:graphicImage value="ParticipantImageServlet.prf?photo=#{searchResultAll.participant.id}"  rendered="#{RosterTool.showIdPhoto}" alt="#{msgs.profile_picture_alt} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}" styleClass="rosterImage"/>
					</h:column>	
					<h:column>
						<f:facet name="header">	
							<h:outputText value="#{msgs.facet_profile}" />
						</f:facet>				
						<h:commandLink  action="#{RosterTool.processActionDisplayProfile}" value="#{msgs.view_profile}" title="#{msgs.view_profile_title} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}">
							<f:param value="#{searchResultAll.participant.id}" name="participantId"/>
						</h:commandLink></h:column>
					
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{RosterTool.toggleLastNameSort}" title="#{msgs.view_profile_list_sort_name}">
						   		<h:outputText value="#{msgs.facet_name}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortLastNameDescending}" alt="#{msgs.view_profile_list_sort_name} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortLastNameAscending}" alt="#{msgs.view_profile_list_sort_name} #{msgs.view_profile_list_sort_desc}"/>
							</h:commandLink>
						</f:facet>								
						<h:outputText value="#{searchResultAll.participant.lastName}" /> 
						<h:outputText value=", " /> 
						<h:outputText value="#{searchResultAll.participant.firstName}" /> 
					</h:column>
					
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{RosterTool.toggleUserIdSort}" title="#{msgs.view_profile_list_sort_id}">
								<h:outputText value="#{msgs.facet_userId}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortUserIdDescending}" alt="#{msgs.view_profile_list_sort_id} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortUserIdAscending}" alt="#{msgs.view_profile_list_sort_id} #{msgs.view_profile_list_sort_asc}" />
						   	</h:commandLink>
						</f:facet>								
						<h:outputText value="#{searchResultAll.participant.eid}" /> 						
					</h:column>
				 </h:dataTable> 

			 </h:column>
		  </h:dataTable>  
	 </h:form>
  </sakai:view_content>	
</sakai:view_container>
</f:view> 
