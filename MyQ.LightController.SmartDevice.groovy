/**
 *	MyQ Light Controller SmartDevice
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
	definition (name: "MyQ Light Controller", namespace: "copy-ninja", author: "Jason Mok") {
		capability "Refresh"
		capability "Polling"

		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	simulator {	}

	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state("off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on")
			state("on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off")
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state("default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh")
		}
		main "button"
		details(["button", "refresh"])
	}
}
def installed() { poll() }

def parse(String description) {}

def on() { 
	//log.debug "On.."
	parent.sendCommand(this, "desiredlightstate", 1) 
}
def off() { 
	//log.debug "Off.."
	parent.sendCommand(this, "desiredlightstate", 0) 
}

def refresh() {
	//log.debug "Refresh.."
	parent.refresh()
	//poll()
}

def poll() {
	updateDeviceStatus(parent.getDeviceStatus(this))
}

// update status
def updateDeviceStatus(status) {
	//log.debug "Status : " + status
	if (status == "0") { 
		sendEvent(name: "button", value: "off", display: true, descriptionText: device.displayName + " was off") 
	}   
	if (status == "1") {
		sendEvent(name: "button", value: "on", display: true, descriptionText: device.displayName + " was off") 
	}
}
