/**
 *	MyQ Smart Device
 *
 *	Author: Jason Mok
 *	Date: 2014-12-26
 *
 ***************************
 *
 *  Copyright 2014 Jason Mok
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 **************************
 *
 * REQUIREMENTS:
 * Refer to MyQ Service Manager SmartApp
 *
 **************************
 * 
 * USAGE:
 * Put this in Device Type. Don't install until you have all other device types scripts added
 * Refer to MyQ Service Manager SmartApp
 *
 */
metadata {
	definition (name: "MyQ Garage Door", namespace: "copy-ninja", author: "Jason Mok") {
		capability "Door Control"
        capability "Refresh"
        capability "Polling"
		
        attribute "lastActivity", "string"
	}

	simulator {	}

	tiles {
		standardTile("door", "device.door", width: 2, height: 2) {
			state("closed",  label:'${name}', action:"door control.open",  icon:"st.doors.garage.garage-closed",  backgroundColor:"#79b821", nextState:"opening")
			state("open",    label:'${name}', action:"door control.close", icon:"st.doors.garage.garage-open",    backgroundColor:"#ffa81e", nextState:"closing")
			state("opening", label:'${name}', action:"refresh.refresh",    icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e")
			state("closing", label:'${name}', action:"refresh.refresh",    icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e")
            state("unknown", label:'${name}', action:"refresh.refresh",    icon:"st.doors.garage.garage-open",    backgroundColor:"#ffa81e")
		}
		standardTile("refresh", "device.door", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("lastActivity", "device.lastActivity", inactiveLabel: false, decoration: "flat") {
			state "default", label:'${currentValue}', backgroundColor:"#ffffff"
		}

		main "door"
		details(["door", "lastActivity", "refresh"])
	}
}
def installed() { poll() }

def parse(String description) {}

def open()  { 
	log.debug "opening.."
    parent.sendCommand(this, 1) 
    poll()  
}
def close() { 
	log.debug "closing.."
    parent.sendCommand(this, 2) 
    poll()  
}

def refresh() {
	log.debug "refreshing.."
    parent.refresh()
    poll()
}

def poll() {
	log.debug "polling.."
    
    //update device
	updateDeviceStatus(parent.getDeviceStatus(this))
    
    //get last activity
    def lastActivity = parent.getDeviceLastActivity(this)
    sendEvent(name: "lastActivity", value: lastActivity, display: true, descriptionText: device.displayName + " was open")
	
}

// update status
def updateDeviceStatus(status) {
	log.debug "Current Device Status: " + status
	if (deviceStatus == "1") {
		sendEvent(name: "door", value: "open", display: true, descriptionText: device.displayName + " was open")
	}   
    if (deviceStatus == "2") {
		sendEvent(name: "door", value: "closed", display: true, descriptionText: device.displayName + " was closed")
	}
    if (deviceStatus == "3") {
		sendEvent(name: "door", value: "open", display: true, descriptionText: device.displayName + " was open")
	}
	if (deviceStatus == "4") {
		sendEvent(name: "door", value: "opening", display: true)
	}  
    if (deviceStatus == "5") {
		sendEvent(name: "door", value: "closing", display: true)
	}  
}
