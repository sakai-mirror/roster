/* New Roster2 functions */

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
		var testData = {
			firstName : 'daniel',
			surname : 'robinson'
		};
	} else if ('pics' === state) {

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
