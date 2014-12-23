/**
 *	MyQ Smart Device
 *
 *	Author: Jason Mok
 *	Date: 2014-12-20
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
	        
		attribute "lastAction", "string"
	    
	}

	simulator { }

	tiles {
		standardTile("door", "device.door", width: 2, height: 2, canChangeIcon: false) {
			state "closed",  label: "closed",  action: "doorControl.open",  icon: "st.doors.garage.garage-closed",  backgroundColor: "#ffffff", nextState: "opening"
            state "closing", label: "closing", action: "doorControl.open",  icon: "st.doors.garage.garage-closing", backgroundColor: "#D4741A", nextState: "closed"
			state "open",    label: "open",    action: "doorControl.close", icon: "st.doors.garage.garage-open",    backgroundColor: "#57BF17", nextState: "closing"
			state "opening", label: "opening", action: "doorControl.close", icon: "st.doors.garage.garage-opening", backgroundColor: "#D4741A", nextState: "open"
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("lastAction", "device.lastAction", inactiveLabel: false, decoration: "flat") {
			state "default", label:'${currentValue}', backgroundColor:"#ffffff"
		}

		main "door"
		details(["door","refresh","runTime"])
	}
}

def installed() { }

def parse(String description) {}

def open()  { 
    parent.sendCommand(this, "1") 
    poll()  
}
def close() { 
    parent.sendCommand(this, "2") 
    poll()  
}

def refresh() {
    parent.refresh()
    poll()
}

def poll() {
	def deviceStatus = parent.getDeviceStatus(this)
	
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
