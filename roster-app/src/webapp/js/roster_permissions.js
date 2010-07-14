function RosterPermissions(data) {
	
	for(var i = 0, j = data.length; i < j; i++) {
	
		if ('roster.export' === data[i]) {
			this.rosterExport = true;
		} else if ('roster.viewallmembers' === data[i]) {
			this.viewAllMembers = true;
		} else if ('roster.viewgroup' === data[i]) {
			this.viewGroup = true;
		} else if ('roster.viewprofile' === data[i]) {
			this.viewProfile = true;
		}
		
		// TODO check if any missing.
	}
		
}