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

	if ('overview' === state) {
		
	} else if ('pics' === state) {
				
		//templateName,contextObject,output
		SakaiUtils.renderTrimpathTemplate('roster_pics_header_template', arg, 'roster_header');
		SakaiUtils.renderTrimpathTemplate('roster_section_filter_template', arg, 'roster_section_filter');
		SakaiUtils.renderTrimpathTemplate('roster_search_template', arg, 'roster_search');
		SakaiUtils.renderTrimpathTemplate('roster_pics_template', arg, 'roster_content');
		
	} else if ('group_membership' === state) {
		
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
		// document.getElementById('roster_form:search_button').click();
		return false;
	}
	return true;
}
