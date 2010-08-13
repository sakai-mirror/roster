/**
 * Copyright (c) 2008-2010 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Daniel Robinson (d.b.robinson@lancaster.ac.uk)
 * Adrian Fish (a.fish@lancaster.ac.uk)
 */
var ADMIN = 'admin';

var DEFAULT_GROUP_ID = 'all';
var DEFAULT_ENROLLMENT_STATUS = 'All';

var STATE_OVERVIEW = 'overview';
var STATE_PICTURES = 'pics';
var STATE_GROUP_MEMBERSHIP = 'group_membership';
var STATE_ENROLLMENT_STATUS = 'status'
var STATE_VIEW_PROFILE = 'profile';

var SORT_NAME = 'sortName';
var SORT_DISPLAY_ID = 'displayId';
var SORT_EMAIL = 'email';
var SORT_ROLE = 'role';
var SORT_STATUS	= "status";
var SORT_CREDITS	= "credits";

var columnSortFields = [];

/* Stuff that we always expect to be setup */
var rosterSiteId = null;
var rosterCurrentUserPermissions = null;
var rosterCurrentState = null;
var rosterCurrentUser = null;

// These are default behaviours, and are global so the tool remembers
// the user's choices.
var grouped = roster_group_ungrouped;
var hideNames = false;
var viewSingleColumn = false;
var groupToView = null;
var groupToViewText = roster_sections_all;
var enrollmentSetToView = null;
var enrollmentSetToViewText = null;
var enrollmentStatusToViewText = roster_enrollment_status_all;

// sakai.properties
var defaultSortColumn = SORT_NAME;
var firstNameLastName = false;
var hideSingleGroupFilter = false;
var viewEmailColumn = true;
// end of sakai.properties

var sortColumn = null;
var overviewSortParams = {headers:{1: {sorter:'urls'}}, sortList:[[0,0]]};
var groupSortParams = {headers:{1: {sorter:'urls'}, 3: {sorter:false}}, sortList:[[0,0]]};
var enrollmentSortParams = {headers:{1: {sorter:'urls'}, 2: {sorter:'urls'}}, sortList:[[0,0]]};

// sortEnd is used to update this so we know which column and direction the
// tables are sorted in when exporting
var currentSortColumn = 0;
var currentSortDirection = 0;

// tablesorter parser for URLs
$.tablesorter.addParser({
	id: 'urls',is: function(s) { return false; },
	format: function(s) { return s.replace(new RegExp(/<.*?>/),""); },
	type: 'text'
});

/* New Roster functions */

(function() {
	    
	// We need the toolbar in a template so we can swap in the translations
	SakaiUtils.renderTrimpathTemplate('roster_navbar_template', {},
			'roster_navbar');
	
	$('#navbar_overview_link').bind('click', function(e) {
		return switchState(STATE_OVERVIEW);
	});

	$('#navbar_pics_link').bind('click', function(e) {
		return switchState(STATE_PICTURES);
	});

	$('#navbar_group_membership_link').bind('click', function(e) {
		return switchState(STATE_GROUP_MEMBERSHIP);
	});
	
	$('#navbar_enrollment_status_link').bind('click', function(e) {
		return switchState(STATE_ENROLLMENT_STATUS);
	});
		
	var arg = SakaiUtils.getParameters();

	if (!arg || !arg.siteId) {
		alert('The site id  MUST be supplied as a page parameter');
		return;
	}
	rosterSiteId = arg.siteId;

	rosterCurrentUser = SakaiUtils.getCurrentUser();

	if (!rosterCurrentUser) {
		alert("No current user. Have you logged in?");
		return;
	}
	
	getRosterCurrentUserPermissions();
	
	// process sakai.properties
	if (arg.firstNameLastName) {
		if ('true' == arg.firstNameLastName) {
			firstNameLastName = true;
		} else {
			// default = false
			firstNameLastName = false;
		}
	}
			
	if (arg.hideSingleGroupFilter) {
		if ('true' == arg.hideSingleGroupFilter) {
			hideSingleGroupFilter = true;
		} else {
			// default = false
			hideSingleGroupFilter = false;
		}
	}
	
	if (arg.viewEmailColumn) {
		if ('false' == arg.viewEmailColumn) {
			viewEmailColumn = false;
		} else {
			// default = true
			viewEmailColumn = true;
		}
		
	}
	
	if (arg.defaultSortColumn) {
		
		if (SORT_NAME == arg.defaultSortColumn ||
				SORT_DISPLAY_ID == arg.defaultSortColumn ||
				SORT_ROLE == arg.defaultSortColumn ||
				SORT_STATUS == arg.defaultSortColumn ||
				SORT_CREDITS == arg.defaultSortColumn) {
			
			defaultSortColumn = arg.defaultColumn;
		} else if (SORT_EMAIL == arg.defaultSortColumn && true == viewEmailColumn) {
			// if chosen sort is email, check that email column is viewable
			defaultSortColumn = arg.defaultColumn;
		}
	}
	
	sortColumn = defaultSortColumn;
	// end of sakai.properties

	if (window.frameElement) {
		window.frameElement.style.minHeight = '600px';
	}
		
	// Now switch into the requested state
	switchState(arg.state, arg);

})();

function switchState(state, arg, searchQuery) {
	
	// for export to Excel
	setColumnSortFields(state);
	
	// $('#cluetip').hide();
		
	var site = getRosterSite();

	if (!rosterCurrentUserPermissions.viewEnrollmentStatus ||
			site.siteEnrollmentSets.length === 0) {
		
		$('#navbar_enrollment_status_link').hide();
	}
	
	// hide group membership link if there are no groups
	if (site.siteGroups.length === 0) {
		$('#navbar_group_membership_link').hide();
	}
		
	if (STATE_OVERVIEW === state) {
		
		configureOverviewTableSort();
		
		var members = getMembers(searchQuery, false);
		var roles = getRolesUsingRosterMembers(members, site.userRoles);
		
		SakaiUtils.renderTrimpathTemplate('roster_overview_header_template',
				{'siteTitle':site.title,
				'displayTitleMsg':rosterCurrentUserPermissions.viewAllMembers},
				'roster_header');
		
		if (site.siteGroups.length > 0/* && members.length > 0*/) {
			
			SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
					{'groupToViewText':groupToViewText,'siteGroups':site.siteGroups},
					'roster_section_filter');
			
		} else {
			
			SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');
		}
		
		SakaiUtils.renderTrimpathTemplate('roster_search_with_participants_template',
				{'roleFragments':getRoleFragments(roles),
				'participants':getCurrentlyDisplayingParticipants(roles)},
				'roster_search');
	
	
		SakaiUtils.renderTrimpathTemplate('roster_overview_template',
				{'membership':members, 'siteId':rosterSiteId,
				'groupToView':groupToView, 'firstNameLastName':firstNameLastName,
				'viewEmailColumn':viewEmailColumn,
				'viewProfile':rosterCurrentUserPermissions.viewProfile},
				'roster_content');
		
		$(document).ready(function() {
			
			readyExportButton(state);
			readySearchButton(state);
			readyClearButton(state);
			readySectionFilter(site, state);
			
			$('#roster_form_rosterTable').tablesorter(overviewSortParams);
			
			$('#roster_form_rosterTable').bind("sortEnd",function() {
				currentSortColumn = this.config.sortList[0][0];
				currentSortDirection = this.config.sortList[0][1];
		    });
		});
		
	} else if (STATE_PICTURES === state) {
		
		var members = getMembers(searchQuery, true);
		var roles = getRolesUsingRosterMembers(members, site.userRoles);
		
		SakaiUtils.renderTrimpathTemplate('roster_pics_header_template',
				{'siteTitle':site.title}, 'roster_header');
		
		if (site.siteGroups.length > 0/* && members.length > 0*/) {
			
			SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
					{'groupToViewText':groupToViewText,'siteGroups':site.siteGroups},
					'roster_section_filter');
		} else {
			
			SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_section_filter');			
		}
		
		SakaiUtils.renderTrimpathTemplate('roster_search_with_participants_template',
				{'roleFragments':getRoleFragments(roles),
				'participants':getCurrentlyDisplayingParticipants(roles)},
				'roster_search');
		

		SakaiUtils.renderTrimpathTemplate('roster_pics_template',
				{'membership':members, 'siteId':rosterSiteId,
				'groupToView':groupToView, 'viewSingleColumn':viewSingleColumn,
				'hideNames':hideNames,
				'viewProfile':rosterCurrentUserPermissions.viewProfile,
				'viewPhoto':rosterCurrentUserPermissions.viewPhoto},
				'roster_content');
		
		$(document).ready(function() {
			
			readySearchButton(state);
			readyClearButton(state);
			readySectionFilter(site, state);
			
			readyHideNamesButton(state, searchQuery);
			readyViewSingleColumnButton(state, searchQuery);
		});
		
	} else if (STATE_GROUP_MEMBERSHIP === state) {
		
		configureGroupMembershipTableSort();
		
		var members = getRosterMembership();
		var roles = getRolesUsingRosterMembers(members, site.userRoles);
		
		SakaiUtils.renderTrimpathTemplate('roster_groups_header_template',
				{'siteTitle':site.title,
				'displayTitleMsg':rosterCurrentUserPermissions.viewAllMembers},
				'roster_header');
						
		SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_search');
						
		if (roster_group_bygroup === grouped) {
			
			SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_template',
					{'arg':arg, 'siteId':rosterSiteId}, 'roster_section_filter');

			SakaiUtils.renderTrimpathTemplate('roster_grouped_template',
					{'membership':members,
					'siteGroups':site.siteGroups, 'rolesText':getRolesByGroupRoleFragments(site, members),
					'siteId':rosterSiteId, 'viewProfile':rosterCurrentUserPermissions.viewProfile},
					'roster_content');
			
		} else {
			
			SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_with_participants_template',
					{'arg':arg, 'siteId':rosterSiteId,'roleFragments':getRoleFragments(roles),
				'participants':getCurrentlyDisplayingParticipants(roles)}, 'roster_section_filter');
			
			SakaiUtils.renderTrimpathTemplate('roster_ungrouped_template',
					{'membership':members, 'siteId':rosterSiteId,
					'viewProfile':rosterCurrentUserPermissions.viewProfile},
					'roster_content');
		}
		
		$(document).ready(function() {
			
			readyExportButton(state);
			
			$('#roster_form_group_choice').val(grouped);
			$('#roster_form_group_choice').change(function(e) {
				
				grouped = this.options[this.selectedIndex].text;
				
				switchState('group_membership');
			});
			
			$('table').tablesorter(groupSortParams);
		});
		
	} else if (STATE_VIEW_PROFILE === state) {
		
		var profileMarkup = SakaiUtils.getProfileMarkup(arg.userId);
				
		$('#roster_content').html(profileMarkup);
		
	} else if (STATE_ENROLLMENT_STATUS === state) {
				
		configureEnrollmentStatusTableSort();
		
		if (null === enrollmentSetToView && null != site.siteEnrollmentSets[0]) {
			enrollmentSetToView = site.siteEnrollmentSets[0].id;
		}
		
		var enrollment = getEnrolledMembers(searchQuery);
		
		SakaiUtils.renderTrimpathTemplate('roster_enrollment_header_template',
				{'siteTitle':site.title}, 'roster_header');
		
		SakaiUtils.renderTrimpathTemplate('roster_enrollment_section_filter_template',
				{'enrollmentSets':site.siteEnrollmentSets,
				'enrollmentStatusDescriptions':site.enrollmentStatusDescriptions},
				'roster_section_filter');
				
		SakaiUtils.renderTrimpathTemplate('roster_search_with_students_template',
				{'students':getCurrentlyDisplayingStudents(enrollment, null)},
				'roster_search');
		
		SakaiUtils.renderTrimpathTemplate('roster_enrollment_status_template',
				{'enrollment':enrollment, 'enrollmentStatus':enrollmentStatusToViewText,
				'siteId':rosterSiteId, 'firstNameLastName':firstNameLastName,
				'viewEmailColumn':viewEmailColumn,
				'viewProfile':rosterCurrentUserPermissions.viewProfile},
				'roster_content');
				
		$(document).ready(function() {
			
			readyExportButton(state);
			readyEnrollmentFilters(site.siteEnrollmentSets.length);
			
			readySearchButton(state);
			readyClearButton(state);
			
			$('#roster_form_rosterTable').tablesorter(overviewSortParams);
			
			$('#roster_form_rosterTable').bind("sortEnd",function() {
				currentSortColumn = this.config.sortList[0][0];
				currentSortDirection = this.config.sortList[0][1];
		    });
		});
	}
}

function getRosterSite() {
	
	var site;

	jQuery.ajax({
    	url : "/direct/roster-membership/" + rosterSiteId + "/get-site.json",
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			site = data;
			if (undefined == site.siteGroups) {
				site.siteGroups = new Array();
			}
			
			if (undefined == site.userRoles) {
				site.userRoles = new Array();
			}
			
			if (undefined == site.siteEnrollmentSets) {
				site.siteEnrollmentSets = new Array();
			}
		}
	});
	
	return site;
	
}

function getRosterMembership(groupId, sorted, sortField, sortDirection) {
	
	var membership;
	
	var url = "/direct/roster-membership/" + rosterSiteId + "/get-membership.json?sorted=" + sorted;
	if (groupId) {
		url += "&groupId=" + groupId;
	}
	
	jQuery.ajax({
    	url : url,
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			membership = data['roster-membership_collection'];
		},
		error : function() {
			membership = new Array();
		}
	});
	
	return membership;
}

function getRosterEnrollment() {
	
	var enrollment;
	// TODO pass enrollment status required?
	var url = "/direct/roster-membership/" + rosterSiteId + "/get-enrollment.json?enrollmentSetId=" + enrollmentSetToView;
	
	jQuery.ajax({
    	url : url,
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			enrollment = data['roster-membership_collection'];
		},
		error : function() {
			enrollment = new Array();
		}
	});
	
	return enrollment;
	
}

function getCurrentlyDisplayingParticipants(roles) {
	
	var participants = 0;
	
	for (var i = 0, j = roles.length; i < j; i++) {
		
		participants = participants + roles[i].roleCount;
	}
	
	return currently_displaying_participants.replace(/\{0\}/, participants);
}

function getCurrentlyDisplayingStudents(enrollment, enrollmentType) {
	
	var currentEnrollments = enrollments_currently_displaying.replace(/\{0\}/,
			enrollment.length);
	
	if (enrollmentType) {
		currentEnrollments = currentEnrollments.replace(/\{1\}/, enrollmentType);
	} else {
		// do all needs no string		
		currentEnrollments = currentEnrollments.replace(/\{1\}/, '');
	}
	
	return currentEnrollments;
}

function getRoleFragments(roles) {
	
	var roleFragments = new Array();
	
	for (var i = 0, j = roles.length; i < j; i++) {
				
		var frag = role_breakdown_fragment.replace(/\{0\}/, roles[i].roleCount);
		frag = frag.replace(/\{1\}/, roles[i].roleType);
		
		if (i != j - 1) {
			frag = frag + ", ";
		}
		
		roleFragments[i] = frag;
	}	
	return roleFragments;
}

function getRolesUsingRosterMembers(members, roleTypes) {
	
	var roles = new Array();
		
	for (var i = 0, j = roleTypes.length; i < j; i++) {
		roles[i] = { roleType: roleTypes[i], roleCount: 0 };
	}
	
	for (var i = 0, j = members.length; i < j; i++) {
		
		for (var k = 0, l = roles.length; k < l; k++) {
			
			if (roles[k].roleType === members[i].role) {
				roles[k].roleCount++;
				continue;
			}
		}
	}
	
	// filter out roles with 0 members of that role type
	var rolesToReturn = new Array();
	var rolesCount = 0;
	for (var i = 0, j = roles.length; i < j; i++) {
		
		if (roles[i].roleCount != 0) {
			rolesToReturn[rolesCount] = roles[i];
			rolesCount++;
		}
	}
	
	return rolesToReturn;
}



function getRolesByGroup(site, members) {
	
	var rolesByGroup = new Array();
	
	for (var i = 0, j = site.siteGroups.length; i < j; i++) {
				
		var groupId = site.siteGroups[i].id;

		rolesByGroup[groupId] = new Object();
		rolesByGroup[groupId].groupId = groupId;
		rolesByGroup[groupId].groupTitle = site.siteGroups[i].title;
		rolesByGroup[groupId].roles = new Array();
		
		for (var k = 0, l = members.length; k < l; k++) {
			
			for (var m = 0, n = site.siteGroups[i].userIds.length; m < n; m++) {
								
				if (members[k].userId === site.siteGroups[i].userIds[m]) {
					
					var role = members[k].role;
					
					// if we haven't processed this type of role before, create it
					if (undefined === rolesByGroup[groupId].roles[role]) {
						rolesByGroup[groupId].roles[role] = { 'roleType':role, 'roleCount':0 }
					}
						
					rolesByGroup[groupId].roles[role].roleCount =
						rolesByGroup[groupId].roles[role].roleCount + 1;		
				}
			}
		}
	}
	
	return rolesByGroup;
}

function getRolesByGroupRoleFragments(site, members) {

	var rolesByGroup = getRolesByGroup(site, members);
	
	var rolesByGroupRoleFragments = new Array();
	
	for (var group in rolesByGroup) {
		
		rolesByGroupRoleFragments[group] = new Object();
		
		var participants = 0;
		rolesByGroupRoleFragments[group].roles = new Array();
		
		var numberOfRoles = 0;
		for (var role in rolesByGroup[group].roles) {
			numberOfRoles++;
		}
		
		var roleNumber = 1;
		for (var role in rolesByGroup[group].roles) {
			
			rolesByGroupRoleFragments[group].roles[role] = new Object();
			rolesByGroupRoleFragments[group].roles[role].frag = 
				role_breakdown_fragment.replace(/\{0\}/,
						rolesByGroup[group].roles[role].roleCount);
			
			rolesByGroupRoleFragments[group].roles[role].frag =
				rolesByGroupRoleFragments[group].roles[role].frag.replace(/\{1\}/,
						rolesByGroup[group].roles[role].roleType);
			
			if (roleNumber != numberOfRoles) {
				rolesByGroupRoleFragments[group].roles[role].frag = 
					rolesByGroupRoleFragments[group].roles[role].frag + ", ";
			}
								
			participants = participants + rolesByGroup[group].roles[role].roleCount;
			
			roleNumber++;
		}
		
		
		rolesByGroupRoleFragments[group].participants =
			currently_displaying_participants.replace(/\{0\}/, participants);
	}
	
	return rolesByGroupRoleFragments;
}

function getMembers(searchQuery, sorted) {
		
	var members;
	
	// view all users
	if (groupToViewText === roster_sections_all ||
			groupToViewText === roster_section_sep_line) {
		
		members = getRosterMembership(null, sorted, null, null);			
	// view a specific group (note: search is done within group if selected)
	} else {
		members = getRosterMembership(groupToView, sorted, null, null);
	}

	if (searchQuery) {
		return getMembersFromSearchQuery(members, searchQuery);		
	} else {
		return members;
	}
}

function getEnrolledMembers(searchQuery) {
	// TODO pass enrollment status required?
	var enrollment = getRosterEnrollment();
		
	if (searchQuery) {
		return getMembersFromSearchQuery(enrollment, searchQuery);		
	} else {
		return enrollment;
	}

}

function getMembersFromSearchQuery(members, searchQuery) {
	
	var membersToReturn = new Array();
	var memberCount = 0;
	
	for (var i = 0, j = members.length; i < j; i++) {
						
		if (members[i].displayName.toLowerCase().indexOf(searchQuery) >= 0 ||
				members[i].displayId.toLowerCase().indexOf(searchQuery) >= 0) {
								
			membersToReturn[memberCount] = members[i];
			memberCount++;
		}
	}
	
	return membersToReturn;
}

function readyClearButton(state) {
	
	$('#roster_form_clear_button').bind('click', function(e) {
		switchState(state);
	});
}

function readyExportButton(viewType) {
		
	$('#export_button').bind('click', function(e) {
	
		var groupId = null;
		if (null != groupToView) {
			groupId = groupToView;
		} else {
			groupId = DEFAULT_GROUP_ID;
		}
		
		var byGroup = false;
		if (grouped === roster_group_bygroup) {
			byGroup = true;
		}
		
		var enrollmentStatus = null;
		if (enrollmentStatusToViewText == roster_enrollment_status_all) {
			enrollmentStatus = DEFAULT_ENROLLMENT_STATUS;
		} else {
			enrollmentStatus = enrollmentStatusToViewText;
		}
		
		e.preventDefault();
		window.location.href="/direct/roster-export/" + rosterSiteId +
			"/export-to-excel?groupId=" + groupId +
			"&viewType=" + viewType +
			"&sortField=" + columnSortFields[currentSortColumn] +
			"&sortDirection=" + currentSortDirection +
			"&byGroup=" + byGroup + 
			"&enrollmentSetId=" + enrollmentSetToView +
			"&enrollmentStatus=" + enrollmentStatus +
			"&facetName=" + facet_name +
			"&facetUserId=" + facet_userId +
			"&facetEmail=" + facet_email +
			"&facetRole=" + facet_role +
			"&facetGroups=" + facet_groups +
			"&facetStatus=" + facet_status +
			"&facetCredits=" + facet_credits;
	});
		
	// hide export button if necessary
	if (STATE_OVERVIEW === viewType || STATE_GROUP_MEMBERSHIP === viewType || 
			STATE_ENROLLMENT_STATUS === viewType) {
		
		if (rosterCurrentUserPermissions.rosterExport) {
			$('#export_button').show();
		} else {
			$('#export_button').hide();
		}
	}
}

function readySearchButton(state) {
	
	$('#roster_form_search_button').bind('click', function(e) {
		
		if (roster_form_search_field.value != roster_search_text &&
				roster_form_search_field.value != "") {
			
			searchQuery = roster_form_search_field.value.toLowerCase();
			switchState(state, null, searchQuery);
		}
	});
}

function readySectionFilter(site, state) {
	
	if (site.siteGroups.length > 0) {
		
		$('#roster_form_section_filter').val(groupToViewText);
		$('#roster_form_section_filter').change(function(e) {
			
			if (this.options[this.selectedIndex].value != roster_section_sep_line) {
				
				groupToView = this.options[this.selectedIndex].value;
				groupToViewText = this.options[this.selectedIndex].text;
		
				switchState(state);
			}
		});
	}
}

function readyEnrollmentFilters(numberOfEnrollmentSets) {
			
	if (numberOfEnrollmentSets > 0) {
		
		$('#roster_form_enrollment_set_filter').val(enrollmentSetToViewText);
		$('#roster_form_enrollment_set_filter').change(function(e) {
			enrollmentSetToView = this.options[this.selectedIndex].value;
			enrollmentSetToViewText = this.options[this.selectedIndex].text;
			
			switchState(STATE_ENROLLMENT_STATUS);
		});
	}
	
	$('#roster_form_enrollment_status_filter').val(enrollmentStatusToViewText);
	$('#roster_form_enrollment_status_filter').change(function(e) {
		
		enrollmentStatusToViewText = this.options[this.selectedIndex].text;
				
		switchState(STATE_ENROLLMENT_STATUS);
	});
	
}

function readyHideNamesButton(state, searchQuery) {

	$('#roster_form_hide_names').bind('click', function(e) {
		
		if (true === hideNames) {
			hideNames = false;
		} else {
			hideNames = true;
		}
		
		switchState(state, null, searchQuery);
	});
}

function readyViewSingleColumnButton(state, searchQuery) {
	
	$('#roster_form_pics_view').bind('click', function(e) {
		
		if (true === viewSingleColumn) {
			viewSingleColumn = false;
		} else {
			viewSingleColumn = true;
		}
		
		switchState(state, null, searchQuery);
	});
}

function getRosterCurrentUserPermissions() {
		
	if (rosterCurrentUser.id === ADMIN) {
		
		var data = ['roster.export',
				'roster.viewallmembers',
				'roster.viewenrollmentstatus',
				'roster.viewgroup',
				'roster.viewhidden',
				'roster.viewofficialphoto',
				'roster.viewprofile'];

		rosterCurrentUserPermissions = new RosterPermissions(data);
		
	} else {
		rosterCurrentUserPermissions = new RosterPermissions(
			SakaiUtils.getCurrentUserPermissions(rosterSiteId, 'roster'));		
	}
	
}

function configureOverviewTableSort() {
	
	if (SORT_NAME === sortColumn) {
		overviewSortParams.sortList = [[0,0]];
	} else if (SORT_DISPLAY_ID === sortColumn) {
		overviewSortParams.sortList = [[1,0]];
	} else if (SORT_EMAIL === sortColumn) {
		
		if (true === viewEmailColumn) {
			overviewSortParams.sortList = [[2,0]];
		}
		
	} else if (SORT_ROLE === sortColumn) {
	
		if (true === viewEmailColumn) {
			overviewSortParams.sortList = [[3,0]];
		} else {
			overviewSortParams.sortList = [[2,0]];
		}
	}
}

function configureGroupMembershipTableSort() {
	
	if (SORT_NAME === sortColumn) {
		groupSortParams.sortList = [[0,0]];
	} else if (SORT_DISPLAY_ID === sortColumn) {
		groupSortParams.sortList = [[1,0]];
	} else if (SORT_ROLE === sortColumn) {
		groupSortParams.sortList = [[2,0]];
	}
}

function configureEnrollmentStatusTableSort() {
	
	// enrollment table doesn't have role, so use name as default sort column
	if (SORT_NAME === sortColumn || SORT_ROLE === sortColumn) {
		enrollmentSortParams.sortList = [[0,0]];
	} else if (SORT_DISPLAY_ID === sortColumn) {
		enrollmentSortParams.sortList = [[1,0]];
	} else if (SORT_EMAIL === sortColumn) {
		
		if (true === viewEmailColumn) {
			enrollmentSortParams.sortList = [[2,0]];
		}
		
	} else if (SORT_STATUS === sortColumn) {
	
		if (true === viewEmailColumn) {
			enrollmentSortParams.sortList = [[3,0]];
		} else {
			enrollmentSortParams.sortList = [[2,0]];
		}
	} else if (SORT_CREDITS === sortColumn) {
		
		if (true === viewEmailColumn) {
			enrollmentSortParams.sortList = [[4,0]];
		} else {
			enrollmentSortParams.sortList = [[3,0]];
		}
	}
}

// this computes the columns array which is used to determine the sortField
// when exporting to Excel
function setColumnSortFields(state) {
	
	columnSortFields[0] = SORT_NAME;
	columnSortFields[1] = SORT_DISPLAY_ID;
	
	if (STATE_GROUP_MEMBERSHIP === state) {
		columnSortFields[2] = SORT_ROLE;
		// n.b. no sort by groups column
	} else if (STATE_OVERVIEW === state) {
		
		if (true === viewEmailColumn) {
			columnSortFields[2] = SORT_EMAIL;
			columnSortFields[3] = SORT_ROLE;
		} else {
			columnSortFields[2] = SORT_ROLE;
		}
	} else if (STATE_ENROLLMENT_STATUS === state) {
		
		if (true === viewEmailColumn) {
			columnSortFields[2] = SORT_EMAIL;
			columnSortFields[3] = SORT_STATUS;
			columnSortFields[4] = SORT_CREDITS;
		} else {
			columnSortFields[2] = SORT_STATUS;
			columnSortFields[3] = SORT_CREDITS;
		}
	}	
}

/* Original Roster functions */
function clearIfDefaultString(formField, defaultString) {
	if (formField.value == defaultString) {
		formField.value = "";
	}
}

function handleEnterKey(field, event) {
	var keyCode = event.keyCode ? event.keyCode : event.which ? event.which
			: event.charCode;
	if (keyCode == 13) {
		document.getElementById('roster_form_search_button').click();
		//return false;
	}
	return true;
}
