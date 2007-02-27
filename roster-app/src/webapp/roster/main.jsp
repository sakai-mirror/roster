<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<% response.setContentType("text/html; charset=UTF-8"); %>


<f:view>
	<sakai:view_container title="#{msgs.facet_roster_list}">
		<sakai:view_content>
			<h:form>	
			<h:panelGroup rendered="#{RosterTool.renderRoster}"> 
				<sakai:tool_bar>
					<sakai:tool_bar_item action="#{RosterTool.processActionToggleIdPhotos}" rendered="#{RosterTool.renderOfficialId}"  value="#{RosterTool.idPhotoText}"/>
					<sakai:tool_bar_item action="#{RosterTool.processActionToggleCustomPhotos}" value="#{RosterTool.customPhotoText}"/>
					<h:outputLink value="#{RosterTool.printFriendlyUrl}" target="_new">
					  <h:graphicImage url="/images/printer.png" alt="#{msgs.print_friendly}" title="#{msgs.print_friendly}" />
					</h:outputLink>
				 </sakai:tool_bar>
			</h:panelGroup>
			
			<sakai:view_title  value="#{RosterTool.title}"/>

			<h:outputText value="#{msgs.title_msg}" rendered="#{RosterTool.renderRosterUpdateInfo}" styleClass="instruction" style="display: block;"/>
			 	  			
			<h:outputText value="#{msgs.no_permission_msg}" styleClass="alertMessage" rendered="#{!RosterTool.userMayViewRoster}"/>
			
			<h:panelGroup rendered="#{RosterTool.userMayViewRoster}">
	 	  
	 	      <h:outputLink rendered="#{RosterTool.renderPrivacyMessage}" value="#{msgs.title_missing_participants_link}" target="_blank" >
			      <h:outputText value="#{msgs.title_missing_participants}"/>
			    </h:outputLink>
 
		  	  <f:verbatim><div class="navPanel" style="width: 98%">
		  		  <div class="viewNav" style="width: 100%"></f:verbatim>
							<h:panelGrid columns="2" cellspacing="0" cellpadding="0" style="width: 100%" rendered="#{RosterTool.renderViewMenu || RosterTool.renderExportButton}">
								<h:panelGroup>
									<h:outputLabel for="select1" rendered="#{RosterTool.renderViewMenu}">
										<h:outputText value="#{msgs.roster_view}"/>
									</h:outputLabel>	
									<h:selectOneMenu  id="select1" onchange="this.form.submit();"  valueChangeListener="#{RosterTool.processValueChangeForView}" 
																		value="#{RosterTool.selectedView}" rendered="#{RosterTool.renderViewMenu}">  
										<f:selectItems value="#{RosterTool.viewMenuItems}"/>
									</h:selectOneMenu>
							  </h:panelGroup>	
							  <h:panelGroup>
							    <f:verbatim><div class="itemNav"></f:verbatim>
							      <h:commandButton id="exportButton" actionListener="#{RosterTool.exportRosterCsv}" value="#{msgs.export_for_csv}" 
							 								 title="#{msgs.export_buttonTitle}" accesskey="e" rendered="#{RosterTool.renderExportButton}" />
							    <f:verbatim></div></f:verbatim>
						  	</h:panelGroup>
						  </h:panelGrid>
						<f:verbatim></div>
					</div></f:verbatim>
			
			<f:verbatim><div class="instruction"></f:verbatim>
				 <h:outputText value="#{msgs.no_participants_msg}" rendered="#{!RosterTool.renderRoster}"/>
				 <h:outputText value="#{msgs.participants_msg} #{RosterTool.allUserCount} - " rendered="#{RosterTool.renderRoster}"/>
				 <t:dataList id="roles" value="#{RosterTool.roleStats}" var="roleResult" layout="simple">
		 	         <h:outputText value="#{roleResult.key} (#{roleResult.value})" rendered="#{RosterTool.renderRoster}" />
		 	     </t:dataList>
		 	<f:verbatim></div></f:verbatim>
		 		
			<%--********************* Roster Display *********************--%>
				<h:dataTable styleClass="listHier lines nolines" id="allUserRoster" value="#{RosterTool.roster}" var="searchResultAll" summary="#{msgs.view_profile_list_all_users}  - #{msgs.roster_list_summary}" rendered="#{RosterTool.renderRoster}">
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
							<h:commandLink action="#{RosterTool.toggleLastNameSort}" title="#{msgs.view_profile_list_sort_name}">
						   		<h:outputText value="#{msgs.facet_name}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortLastNameDescending}" alt="#{msgs.view_profile_list_sort_name} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortLastNameAscending}" alt="#{msgs.view_profile_list_sort_name} #{msgs.view_profile_list_sort_asc}"/>
							</h:commandLink>
						</f:facet>			
						<h:commandLink  action="#{RosterTool.processActionDisplayProfile}" value="#{searchResultAll.participant.lastName}, #{searchResultAll.participant.firstName}" title="#{msgs.view_profile_title} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}">
							<f:param value="#{searchResultAll.participant.id}" name="participantId"/>
						</h:commandLink>					
					</h:column>
					
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{RosterTool.toggleUserIdSort}" title="#{msgs.view_profile_list_sort_id}">
								<h:outputText value="#{msgs.facet_userId}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortUserIdDescending}" alt="#{msgs.view_profile_list_sort_id} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortUserIdAscending}" alt="#{msgs.view_profile_list_sort_id} #{msgs.view_profile_list_sort_asc}" />
						   	</h:commandLink>
						</f:facet>								
						<h:outputText value="#{searchResultAll.participant.displayId}" />						
					</h:column>
					
					<h:column>
						<f:facet name="header">
							<h:commandLink action="#{RosterTool.toggleRoleSort}" title="#{msgs.view_profile_list_sort_role}">
								<h:outputText value="#{msgs.facet_role}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortRoleDescending}" alt="#{msgs.view_profile_list_sort_role} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortRoleAscending}" alt="#{msgs.view_profile_list_sort_role} #{msgs.view_profile_list_sort_asc}" />
						   	</h:commandLink>
						</f:facet>								
						<h:outputText value="#{searchResultAll.participant.roleTitle}" /> 						
					</h:column>
					
					<h:column rendered="#{RosterTool.renderSectionsColumn}">
						<f:facet name="header">
							<h:commandLink action="#{RosterTool.toggleSectionsSort}" title="#{msgs.view_profile_list_sort_sections}">
								<h:outputText value="#{msgs.facet_sections}" />
								<h:graphicImage value="/images/sortdescending.gif" rendered="#{RosterTool.sortSectionsDescending}" alt="#{msgs.view_profile_list_sort_sections} #{msgs.view_profile_list_sort_desc}"/>
								<h:graphicImage value="/images/sortascending.gif" rendered="#{RosterTool.sortSectionsAscending}" alt="#{msgs.view_profile_list_sort_sections} #{msgs.view_profile_list_sort_asc}" />
						   	</h:commandLink>
						</f:facet>								
						<h:outputText value="#{searchResultAll.participant.sectionsForDisplay}" /> 						
				    </h:column>
				</h:dataTable> 
				 
			</h:panelGroup>

	 </h:form>
  </sakai:view_content>	
</sakai:view_container>
</f:view> 