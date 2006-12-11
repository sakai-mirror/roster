<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<% response.setContentType("text/html; charset=UTF-8"); %>

<f:view>
	<sakai:view_container title="#{msgs.print_friendly}">
		<sakai:view_content>
			<h:form>
				<div class="act">
					<h:outputLink onclick="window.print();" title="#{msgs.send_to_printer}" value="#{RosterTool.printFriendlyUrl}">
						<h:graphicImage url="/images/printer.png" alt="#{msgs.send_to_printer}"/>
						<h:outputText value=" #{msgs.send_to_printer}" />
					</h:outputLink>
					<h:outputText value=" | " />
					<h:outputLink onclick="window.close();" title="#{msgs.close_window}" value="#{RosterTool.printFriendlyUrl}">
						<h:outputText value=" #{msgs.close_window}" />
					</h:outputLink>
				</div>
				
				<sakai:view_title value="#{RosterTool.printFriendlyTitle}" />
				<div class="instruction"><h:outputText
					value="#{msgs.roster_section_label} #{RosterTool.displayedSection}"
					rendered="#{RosterTool.displayedSection != null}" /></div>

				<h:dataTable styleClass="listHier" id="allUserRoster"
					value="#{RosterTool.roster}" var="searchResultAll"
					summary="#{msgs.view_profile_list_all_users}  - #{msgs.roster_list_summary}"
					rendered="#{RosterTool.renderRoster}">
					<h:column rendered="#{RosterTool.renderPhotoColumn}">
						<f:facet name="header">
							<h:outputText value="#{RosterTool.facet}" />
						</f:facet>
						<h:graphicImage value="#{msgs.img_unavail}"
							rendered="#{searchResultAll.showCustomPhotoUnavailable}"
							alt="#{msgs.profile_no_picture_available}"
							styleClass="rosterImage" />
						<h:graphicImage
							value="#{searchResultAll.participant.profile.pictureUrl}"
							rendered="#{searchResultAll.showURLPhoto}"
							alt="#{msgs.profile_picture_alt} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}"
							styleClass="rosterImage" />
						<h:graphicImage
							value="ParticipantImageServlet.prf?photo=#{searchResultAll.participant.id}"
							rendered="#{searchResultAll.showCustomIdPhoto}"
							alt="#{msgs.profile_picture_alt} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}"
							styleClass="rosterImage" />
						<h:graphicImage
							value="ParticipantImageServlet.prf?photo=#{searchResultAll.participant.id}"
							rendered="#{RosterTool.showIdPhoto}"
							alt="#{msgs.profile_picture_alt} #{searchResultAll.participant.firstName} #{searchResultAll.participant.lastName}"
							styleClass="rosterImage" />
					</h:column>

					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.facet_name}" />
						</f:facet>
						<h:outputText
							value="#{searchResultAll.participant.lastName}, #{searchResultAll.participant.firstName}" />

					</h:column>

					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.facet_userId}" />
						</f:facet>
						<h:outputText value="#{searchResultAll.participant.eid}" />
					</h:column>

					<h:column>
						<f:facet name="header">
							<h:outputText value="#{msgs.facet_role}" />
						</f:facet>
						<h:outputText value="#{searchResultAll.participant.roleTitle}" />
					</h:column>

					<h:column rendered="#{RosterTool.renderSectionsColumn}">
						<f:facet name="header">
							<h:outputText value="#{msgs.facet_sections}" />
						</f:facet>
						<h:outputText
							value="#{searchResultAll.participant.sectionsForDisplay}" />
					</h:column>
				</h:dataTable>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
					