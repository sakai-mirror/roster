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
	<sakai:view_container title="#{msgs.title_enrollment_status}">
		<sakai:view_content>
		
		<%="<script src=js/roster.js></script>"%>

			<h:form>

				<t:aliasBean alias="#{viewBean}" value="#{status}">
					<%@include file="inc/nav.jspf" %>
				</t:aliasBean>


				<%-- Initialize the filter --%>
				<h:outputText value="#{enrollmentStatusFilter.init}"/>

		        <t:div>

					<t:div>
		        		    <h:selectOneMenu id="statusFilter" value="#{enrollmentStatusFilter.statusFilter}" onchange="this.form.submit()" immediate="true">
		        		    	<f:selectItem itemLabel="#{msgs.roster_enrollment_status_all}" itemValue=""/>
		        		    	<f:selectItems value="#{enrollmentStatusFilter.statusSelectItems}"/>
		        		   	</h:selectOneMenu>

						<h:panelGroup>
							<h:outputLabel for="sectionFilter" value="#{msgs.enrollment_status_filter}"/>
		        		    <h:selectOneMenu id="sectionFilter" value="#{enrollmentStatusFilter.sectionFilter}" onchange="this.form.submit()" immediate="true">
		        		    	<f:selectItems value="#{enrollmentStatusFilter.sectionSelectItems}"/>
		        		   	</h:selectOneMenu>
		        		</h:panelGroup>
	        		</t:div>

					<t:div>
	    		        <h:inputText id="search" value="#{enrollmentStatusFilter.searchFilter}"
	        		        onfocus="clearIfDefaultString(this, '#{msgs.roster_search_text}')"/>
	        		    <h:commandButton value="#{msgs.roster_search_button}" actionListener="#{enrollmentStatusFilter.search}"/>
	        		    <h:commandButton value="#{msgs.roster_clear_button}" actionListener="#{enrollmentStatusFilter.clearSearch}"/>
	        		    
	        		    <h:outputFormat value="#{msgs.enrollments_currently_displaying}">
	        		    	<f:param value="#{enrollmentStatusFilter.participantCount}"/>
	        		    	<f:param value="#{enrollmentStatusFilter.statusFilter}"/>
	        		    </h:outputFormat>
	        		</t:div>
        		</t:div>


			    <t:dataTable cellpadding="0" cellspacing="0"
			        id="rosterTable"
			        value="#{status.participants}"
			        var="participant"
			        sortColumn="#{enrollmentStatusPrefs.sortColumn}"
			        sortAscending="#{enrollmentStatusPrefs.sortAscending}"
			        styleClass="listHier rosterTable">
			        <h:column>
			            <f:facet name="header">
			                <t:commandSortHeader columnName="displayName" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_name}" />
			                </t:commandSortHeader>
			            </f:facet>
						<h:outputText value="#{participant.user.displayName}"/>
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
			                <t:commandSortHeader columnName="status" immediate="true" arrow="true">
			                    <h:outputText value="#{msgs.facet_status}" />
			                </t:commandSortHeader>
			            </f:facet>
			            <h:outputText value="#{participant.enrollmentStatus}"/>
			        </h:column>
			    
			    </t:dataTable>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
