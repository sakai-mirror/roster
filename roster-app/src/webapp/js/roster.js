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

/* New Roster functions */

/* Stuff that we always expect to be setup */
var rosterSiteId = null;
// var rosterCurrentUserPermissions = null;
var rosterCurrentState = null;
var rosterCurrentUser = null;

// these are default behaviours. they need to be global because the roster
// tool remembers these if the user navigates away
var grouped = roster_group_ungrouped;
var hideNames = false;
var groupToView = null;
var groupToViewText = roster_sections_all;

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

	// configure 'hide names' button
	$('#roster_form:hide_names').bind('click', function(e) {
		
		if (hideNames) {
			hideNames = false;
		} else {
			hideNames = true;
		}
		return switchState('pics');
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

	// rosterCurrentUserPermissions = new
	// RosterPermissions(SakaiUtils.getCurrentUserPermissions(rosterSiteId,'roster2'));

	if (window.frameElement) {
		window.frameElement.style.minHeight = '600px';
	}
	
	// Now switch into the requested state
	switchState(arg.state, arg);

})();

function switchState(state, arg) {

	// $('#cluetip').hide();
		
	var site = getSite();
		
	// hide links groups if there are no groups
	if (site.siteGroups.length === 0) {
		$('#navbar_group_membership_link').hide();
	}
	
	if ('overview' === state) {
			
		SakaiUtils.renderTrimpathTemplate('roster_overview_header_template',
				arg, 'roster_header');
				
		SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
				{'groupToViewText':groupToViewText,'siteGroups':site.siteGroups},
				'roster_section_filter');
		
		$(document).ready(function() {
			$('#roster_form_section_filter').val(groupToViewText);
			$('#roster_form_section_filter').change(function(e) {
				groupToView = this.options[this.selectedIndex].value;
				groupToViewText = this.options[this.selectedIndex].text;
			
				switchState('overview');
			});
		});
		
		// render search template with site roles
		SakaiUtils.renderTrimpathTemplate('roster_search_template',
				{'roles':getRoles()}, 'roster_search');
		
		// render overview template with site membership
		SakaiUtils.renderTrimpathTemplate('roster_overview_template',
				{'membership':getMembership()['membership_collection'],
				'siteId':site.id},'roster_content');
		
		var members;
		// view all users
		if (groupToViewText === roster_sections_all ||
				groupToViewText === roster_section_sep_line) {
			
			members = getMembership()['membership_collection'];			
		// view a specific group
		} else {
			members = getGroupMembers(site,groupToView);
		}
		
		SakaiUtils.renderTrimpathTemplate('roster_overview_template',
				{'membership':members, 'siteId':site.id,
				'groupToView':groupToView},'roster_content');
		
	} else if ('pics' === state) {
		
		SakaiUtils.renderTrimpathTemplate('roster_pics_header_template', arg, 'roster_header');
		
		SakaiUtils.renderTrimpathTemplate('roster_section_filter_template',
				{'groupToViewText':groupToViewText,'siteGroups':site.siteGroups},
				'roster_section_filter');
		
		$(document).ready(function() {
			$('#roster_form_section_filter').val(groupToViewText);
			$('#roster_form_section_filter').change(function(e) {
				groupToView = this.options[this.selectedIndex].value;
				groupToViewText = this.options[this.selectedIndex].text;
			
				switchState('pics');
			});
		});
				
		// render search template with site roles
		SakaiUtils.renderTrimpathTemplate('roster_search_template',
				{'roles':getRoles()}, 'roster_search');
					
		var members;
		// view all users
		if (groupToViewText === roster_sections_all ||
				groupToViewText === roster_section_sep_line) {
			
			members = getMembership()['membership_collection'];			
		// view a specific group
		} else {
			members = getGroupMembers(site,groupToView);
		}
		
		SakaiUtils.renderTrimpathTemplate('roster_pics_template',
				{'membership':members, 'siteId':site.id,
				'groupToView':groupToView, 'hideNames':hideNames},
				'roster_content');
			
	} else if ('group_membership' === state) {
			
		SakaiUtils.renderTrimpathTemplate('roster_groups_header_template', arg, 'roster_header');
		SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_template', {'arg':arg, 'siteId':site.id}, 'roster_section_filter');
				
		$(document).ready(function() {
			$('#roster_form_group_choice').val(grouped);
			$('#roster_form_group_choice').change(function(e) {
				
				grouped = this.options[this.selectedIndex].text;
				
				switchState('group_membership');
			});
		});
		
		var groupsByUserId = new Array();
        
		for (var i = 0, groups = site.siteGroups.length; i < groups; i++) {
					
			for (var j = 0, groupUsers = site.siteGroups[i].users.length; j < groupUsers; j++) {
			
				var userId = site.siteGroups[i].users[j];
				
				if (undefined === groupsByUserId[userId]) {
					groupsByUserId[userId] = new Array();
				}
				
				groupsByUserId[userId][groupsByUserId[userId].length] = site.siteGroups[i].title;		
			}
		}
		
		if (roster_group_bygroup === grouped) {
						
			SakaiUtils.renderTrimpathTemplate('roster_grouped_template',
					{'membership':getMembership()['membership_collection'],
					'groupsByUserId':groupsByUserId, 'siteGroups':site.siteGroups},
					'roster_content');
			
		} else {
			
			SakaiUtils.renderTrimpathTemplate('roster_ungrouped_template',
					{'membership':getMembership()['membership_collection'],
					'groupsByUserId':groupsByUserId},'roster_content');
		}		
		
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

function getRoles() {
	
	var roles = {};
	
	jQuery.ajax({
    	url : "/direct/membership.json?siteId=" + rosterSiteId + "&role=access",
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			roles['1'] = { roleType: "access", roleCount: data['membership_collection'].length};
		}
	});
	
	jQuery.ajax({
    	url : "/direct/membership.json?siteId=" + rosterSiteId + "&role=maintain",
      	dataType : "json",
       	async : false,
		cache: false,
	   	success : function(data) {
			roles['2'] = { roleType: "maintain", roleCount: data['membership_collection'].length};
		}
	});
	
	return roles;
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
		// document.getElementById('roster_form:search_button').click();
		return false;
	}
	return true;
}
