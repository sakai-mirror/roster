// See http://confluence.sakaiproject.org/display/SRT/Default+permissions

function RosterPermissions(data) {
	
	for(var i = 0, j = data.length; i < j; i++) {
	
		if ('roster.export' === data[i]) {
			this.rosterExport = true;
		} else if ('roster.viewallmembers' === data[i]) {
			this.viewAllMembers = true;
		} else if ('roster.viewenrollmentstatus' === data[i]) {
			this.viewEnrollmentStatus = true;
		} else if ('roster.viewgroup' === data[i]) {
			this.viewGroup = true;
		} else if ('roster.viewhidden' === data[i]) {
			this.viewHidden = true;
		} else if ('roster.viewofficialphoto' === data[i]) {
			// TODO this may be removed soon
			this.viewOfficialPhoto = true;
		} else if ('roster.viewprofile' === data[i]) {
			this.viewProfile = true;
		}
	}
		
}