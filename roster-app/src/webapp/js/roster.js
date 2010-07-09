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
	
	console.log(site.siteGroups);
	
	// hide links groups if there are no groups
	if (null === getSite().siteGroups) {
		$('#navbar_group_membership_link').hide();
	}
	
	if ('overview' === state) {
		
		SakaiUtils.renderTrimpathTemplate('roster_overview_header_template', arg, 'roster_header');
		SakaiUtils.renderTrimpathTemplate('roster_section_filter_template', arg, 'roster_section_filter');
		
		// render search template with site roles
		SakaiUtils.renderTrimpathTemplate('roster_search_template', {'roles':getRoles()}, 'roster_search');
		
		// render overview template with site membership
		SakaiUtils.renderTrimpathTemplate('roster_overview_template',{'membership':getMembership()['membership_collection']},'roster_content');
		
	} else if ('pics' === state) {
		
		SakaiUtils.renderTrimpathTemplate('roster_pics_header_template', arg, 'roster_header');
		SakaiUtils.renderTrimpathTemplate('roster_section_filter_template', arg, 'roster_section_filter');
		
		// render search template with site roles
		SakaiUtils.renderTrimpathTemplate('roster_search_template', {'roles':getRoles()}, 'roster_search');
		
		// render pics template with site membership
		SakaiUtils.renderTrimpathTemplate('roster_pics_template',{'membership':getMembership()['membership_collection']},'roster_content');
		
	} else if ('group_membership' === state) {
		
		SakaiUtils.renderTrimpathTemplate('roster_groups_header_template', arg, 'roster_header');
		SakaiUtils.renderTrimpathTemplate('roster_group_section_filter_template', arg, 'roster_section_filter');
		
		// TODO code here to sort users by groups
		
		// render group template with site membership
		SakaiUtils.renderTrimpathTemplate('roster_group_template',{'membership':getMembership()['membership_collection']},'roster_content');
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
