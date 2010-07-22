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
var SORT_NAME = 'sortName';
var SORT_USER_ID = 'displayId';
var SORT_EMAIL = 'email';
var SORT_ROLE = 'role';

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

// sakai.properties
var defaultSortColumn = SORT_NAME;
var firstNameLastName = false;
var hideSingleGroupFilter = false;
var viewEmailColumn = true;
// end of sakai.properties

var sortColumn = null;
var overviewSortParams = {headers:{0: {sorter:'urls'}}, sortList:[[0,0]]};
var groupSortParams = {headers:{0: {sorter:'urls'}, 3: {sorter:false}}, sortList:[[0,0]]};

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
		return switchState('overview');
	});

	$('#navbar_pics_link').bind('click', function(e) {
		return switchState('pics');
	});

	$('#navbar_group_membership_link').bind('click', function(e) {
		return switchState('group_membership');
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
	
	// process sakai.properties
	if (arg.defaultSortColumn) {
		defaultSortColumn = arg.defaultSortColumn;
	}
	
	sortColumn = defaultSortColumn;
	
	if (arg.firstNameLastName) {
		firstNameLastName = arg.firstNameLastName;
	}
			
	if (arg.hideSingleGroupFilter) {
		hideSingleGroupFilter = arg.hideSingleGroupFilter;
	}
	
	if (arg.viewEmailColumn) {
		viewEmailColumn = arg.viewEmailColumn;
	}
	// end of sakai.properties
	
	rosterCurrentUserPermissions = new RosterPermissions(
			SakaiUtils.getCurrentUserPermissions(rosterSiteId,'roster'));

	if (window.frameElement) {
		window.frameElement.style.minHeight = '600px';
	}
	
	if (SORT_NAME === sortColumn) {
		overviewSortParams.sortList = [[0,0]];
	} else if (SORT_USER_ID === sortColumn) {
		overviewSortParams.sortList = [[1,0]];
	} else if (SORT_EMAIL === sortColumn) {
		
		if (viewEmailColumn) {
			overviewSortParams.sortList = [[2,0]];
		}
		
	} else if (SORT_ROLE === sortColumn) {
	
		if (viewEmailColumn) {
			overviewSortParams.sortList = [[2,0]];
		} else {
			overviewSortParams.sortList = [[3,0]];
		}
	}
	
	// Now switch into the requested state
	switchState(arg.state, arg);

})();

function switchState(state, arg, searchQuery) {

	// $('#cluetip').hide();
		
	var site = getSite();
	
	// hide group membership link if there are no groups
	if (site.siteGroups.length === 0) {
		$('#navbar_group_membership_link').hide();
	}
		
	if ('overview' === state) {
		
		var members = getMembers(site, searchQuery);
		var roles = getRolesUsingMembers(site, members);
		
		var roles = getRolesUsingMembers(site, members);
		
		SakaiUtils.renderTrimpathTemplate('roster_overview_header_template', {},
				'roster_header');
		
		if (site.siteGroups.length > 0) {
			
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
	
		if (members.length > 0) {
			
			SakaiUtils.renderTrimpathTemplate('roster_overview_template',
					{'membership':members, 'siteId':site.id,
					'groupToView':groupToView, 'firstNameLastName':firstNameLastName,
					'viewEmailColumn':viewEmailColumn},
					'roster_content');
		} else {
			
			SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_content');
		}
		
		$(document).ready(function() {
			
			if (site.siteGroups.length > 0) {
				
				$('#roster_form_section_filter').val(groupToViewText);
				$('#roster_form_section_filter').change(function(e) {
					groupToView = this.options[this.selectedIndex].value;
					groupToViewText = this.options[this.selectedIndex].text;
				
					switchState('overview');
				});
			}
			
			$('#roster_form_search_button').bind('click', function(e) {
				
				if (roster_form_search_field.value != roster_search_text &&
						roster_form_search_field.value != "") {
					
					searchQuery = roster_form_search_field.value.toLowerCase();
					switchState('overview', null, searchQuery);
				}
			});
			
			$('#roster_form_clear_button').bind('click', function(e) {
				switchState('overview');
			});
			
			$('#roster_form_rosterTable').tablesorter(overviewSortParams);
		});
		
	} else if ('pics' === state) {
		
		var members = getMembers(site, searchQuery);
		var roles = getRolesUsingMembers(site, members);
		
		SakaiUtils.renderTrimpathTemplate('roster_pics_header_template', {},
				'roster_header');
		
		if (site.siteGroups.length > 0) {
			
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
		
		if (members.length > 0) {
			
			SakaiUtils.renderTrimpathTemplate('roster_pics_template',
					{'membership':members, 'siteId':site.id,
					'groupToView':groupToView, 'viewSingleColumn':viewSingleColumn,
					'hideNames':hideNames},'roster_content');
		} else {
			
			SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_content');
		}
		
		$(document).ready(function() {
			
			if (site.siteGroups.length > 0) {
				$('#roster_form_section_filter').val(groupToViewText);
				$('#roster_form_section_filter').change(function(e) {
					groupToView = this.options[this.selectedIndex].value;
					groupToViewText = this.options[this.selectedIndex].text;
			
					switchState('pics', null, searchQuery);
				});
			}
			
			// configure 'hide names' button
			$('#roster_form_hide_names').bind('click', function(e) {
				
				if (hideNames) {
					hideNames = false;
				} else {
					hideNames = true;
				}
				
				switchState('pics', null, searchQuery);
			});
			
			$('#roster_form_pics_view').bind('click', function(e) {
				
				if (viewSingleColumn) {
					viewSingleColumn = false;
				} else {
					viewSingleColumn = true;
				}
				
				switchState('pics', null, searchQuery);
			});
			
			$('#roster_form_search_button').bind('click', function(e) {
				
				if (roster_form_search_field.value != roster_search_text &&
						roster_form_search_field.value != "") {
					
					searchQuery = roster_form_search_field.value.toLowerCase();
					switchState('pics', null, searchQuery);
				}
			});
			
			$('#roster_form_clear_button').bind('click', function(e) {
				// lazy
				switchState('pics');
			});
		});
		
	} else if ('group_membership' === state) {
		
		var roles = getRoles(site);
		
		SakaiUtils.renderTrimpathTemplate('roster_groups_header_template', arg, 'roster_header');
						
		SakaiUtils.renderTrimpathTemplate('empty_template', {}, 'roster_search');
				
		var groupsByUserId = getSiteGroupsByUserId(site);
		
		if (roster_group_bygroup === grouped) {
			
			SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_template',
					{'arg':arg, 'siteId':site.id}, 'roster_section_filter');
			
			SakaiUtils.renderTrimpathTemplate('roster_grouped_template',
					{'membership':getMembership()['membership_collection'],
					'groupsByUserId':groupsByUserId, 'siteGroups':site.siteGroups,
					'rolesText':getRolesByGroupRoleFragments(site),'siteId':rosterSiteId},
					'roster_content');
			
		} else {
			
			SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_with_participants_template',
					{'arg':arg, 'siteId':site.id,'roleFragments':getRoleFragments(roles),
				'participants':getCurrentlyDisplayingParticipants(roles)}, 'roster_section_filter');
			
			SakaiUtils.renderTrimpathTemplate('roster_ungrouped_template',
					{'membership':getMembership()['membership_collection'],
					'groupsByUserId':groupsByUserId,'siteId':rosterSiteId},
					'roster_content');
		}
		
		$(document).ready(function() {
			$('#roster_form_group_choice').val(grouped);
			$('#roster_form_group_choice').change(function(e) {
				
				grouped = this.options[this.selectedIndex].text;
				
				switchState('group_membership');
			});
			
			$('table').tablesorter(groupSortParams);
		});
		
	} else if ('profile' === state) {
		
		var profileMarkup = SakaiUtils.getProfileMarkup(arg.userId);
				
		$('#roster_content').html(profileMarkup);
	}
}

function getSite() {
	
	var site;
	
	jQuery.ajax({
    	url : "/direct/site/" + rosterSiteId + ".json?includeGroups=true",
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			site = data;
		}
	});
	
	return site;
}

function getMembership() {
	
	var membership;
	
	jQuery.ajax({
    	url : "/direct/membership/site.json?siteId=" + rosterSiteId,
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			membership = data;
		}
	});
	
	return membership;
}

function getCurrentlyDisplayingParticipants(roles) {
	
	var participants = 0;
	
	for (var i = 0, j = roles.length; i < j; i++) {
		
		participants = participants + roles[i].roleCount;
	}
	
	return currently_displaying_participants.replace(/\{0\}/, participants);
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

// used by group membership view
function getRoles(site) {
	
	var roles = new Array();
	
	for (var i = 0, j = site.userRoles.length; i < j; i++) {
		
		jQuery.ajax({
	    	url : "/direct/membership.json?siteId=" + rosterSiteId + "&role=" + site.userRoles[i],
	      	dataType : "json",
	       	async : false,
			cache: false,
		   	success : function(data) {
				roles[i] = { roleType: site.userRoles[i],
						roleCount: data['membership_collection'].length};
			}
		});
	}
		
	return roles;
}

// used by overview and pictures view
function getRolesUsingMembers(site, members) {
	
	var roles = new Array();
	
	for (var i = 0, j = site.userRoles.length; i < j; i++) {
		roles[i] = { roleType: site.userRoles[i], roleCount: 0 };
	}
		
	for (var i = 0, j = members.length; i < j; i++) {
		
		for (var k = 0, l = roles.length; k < l; k++) {
			
			if (roles[k].roleType === members[i].memberRole) {
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

function getRolesByGroup(site) {
	
	var members = getMembership()['membership_collection'];
	var rolesByGroup = new Array();
	
	for (var i = 0, j = site.siteGroups.length; i < j; i++) {
				
		var groupId = site.siteGroups[i].id;
		rolesByGroup[groupId] = new Object();
		rolesByGroup[groupId].groupId = groupId;
		rolesByGroup[groupId].groupTitle = site.siteGroups[i].title;
		rolesByGroup[groupId].roles = new Array();
		
		for (var k = 0, l = members.length; k < l; k++) {
			
			for (var m = 0, n = site.siteGroups[i].users.length; m < n; m++) {
								
				if (members[k].userId === site.siteGroups[i].users[m]) {
					
					var role = members[k].memberRole;
					
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

function getRolesByGroupRoleFragments(site) {

	var rolesByGroup = getRolesByGroup(site);
	
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

function getMembers(site, searchQuery) {
	
	var members;
	
	// view all users
	if (groupToViewText === roster_sections_all ||
			groupToViewText === roster_section_sep_line) {
		
		members = getMembership()['membership_collection'];			
	// view a specific group (note: search is done within group if selected)
	} else {
		members = getGroupMembers(site,groupToView);
	}
	
	var membersToReturn = new Array();
	var memberCount = 0;
	
	// search is performed against all members or selected group
	if (searchQuery) {
		
		for (var i = 0, j = members.length; i < j; i++) {
							
			if (members[i].userDisplayName.toLowerCase().indexOf(searchQuery) >= 0 ||
					members[i].userDisplayId.toLowerCase().indexOf(searchQuery) >= 0) {
									
				membersToReturn[memberCount] = members[i];
				memberCount++;
			}
		}
	} else {
		membersToReturn = members;
	}
	
	return membersToReturn;
}

function getGroupMembers(site,groupId) {
	
	var usersToRender;
	for (var i = 0, j = site.siteGroups.length; i < j; i++) {
		if (site.siteGroups[i].id === groupToView) {
			usersToRender = site.siteGroups[i].users;
			break;
		}
	}
	
	var members = getMembership()['membership_collection'];
	var groupMembers = new Array();
	
	for (var i = 0, j = members.length; i < j; i++) {
		for (var k = 0, l = usersToRender.length; k < l; k++) {
			if (members[i].userId === usersToRender[k]) {
				groupMembers[groupMembers.length] = members[i];
				continue;
			}
		}
	}
	
	return groupMembers;
}

function getSiteGroupsByUserId(site) {
	
	var groupsByUserId = new Array();
	
	for (var i = 0, k = site.siteGroups.length; i < k; i++) {	
		for (var j = 0, l = site.siteGroups[i].users.length; j < l; j++) {
		
			var userId = site.siteGroups[i].users[j];
			
			if (undefined === groupsByUserId[userId]) {
				groupsByUserId[userId] = new Array();
			}
			
			groupsByUserId[userId][groupsByUserId[userId].length] = 
				site.siteGroups[i].title;		
		}
	}
	
	return groupsByUserId;
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
