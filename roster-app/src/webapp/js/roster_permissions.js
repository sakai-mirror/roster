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