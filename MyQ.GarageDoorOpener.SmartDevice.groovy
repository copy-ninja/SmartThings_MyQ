/**
 *	MyQ Garage Door Opener SmartDevice
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
	definition (name: "MyQ Garage Door Opener", namespace: "copy-ninja", author: "Jason Mok") {
		capability "Door Control"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Polling"

		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
		
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
			state("default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh")
		}
		standardTile("contact", "device.contact") {
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
		standardTile("button", "device.switch") {
			state("on", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#ffa81e")
			state("off", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#79b821")
		}
		valueTile("lastActivity", "device.lastActivity", inactiveLabel: false, decoration: "flat") {
			state "default", label:'Last activity: ${currentValue}', action:"refresh.refresh", backgroundColor:"#ffffff"
		}

		main "door"
		details(["door", "lastActivity", "refresh"])
	}
}
def installed() { poll() }

def parse(String description) {}

def on() { 
	push() 
	sendEvent(name: "button", value: "on", isStateChange: true, display: false, displayed: false)
}
def off() { 
	sendEvent(name: "button", value: "off", isStateChange: true, display: false, displayed: false)
}

def push() { 
	def doorState = device.currentState("door").value
	if (doorState == "open" || doorState == "opening") {
		close()
	} else if (doorState == "closed" || doorState == "closing") {
		open()
	}
	sendEvent(name: "momentary", value: "pushed", display: false, displayed: false)
}

def open()  { 
	parent.sendCommand(this, "desireddoorstate", 1) 
}
def close() { 
	parent.sendCommand(this, "desireddoorstate", 2) 
}

def refresh() {
	parent.refresh()
	poll()
}

def poll() {
	updateDeviceStatus(parent.getDeviceStatus(this))
	updateDeviceLastActivity(parent.getDeviceLastActivity(this))
}

// update status
def updateDeviceStatus(status) {
	if (status == "1") { 
		sendEvent(name: "door", value: "open", display: true, descriptionText: device.displayName + " was open") 
		sendEvent(name: "contact", value: "open", display: false, displayed: false)
	}   
	if (status == "2") {
		sendEvent(name: "door", value: "closed", display: true, descriptionText: device.displayName + " was closed")
		sendEvent(name: "contact", value: "closed", display: false, displayed: false)
	}
	if (status == "3") { sendEvent(name: "door", value: "open", display: true, descriptionText: device.displayName + " was open") }
	if (status == "4") { sendEvent(name: "door", value: "opening", display: false, displayed: false) }  
	if (status == "5") { sendEvent(name: "door", value: "closing", display: false, displayed: false) }  
}

def updateDeviceLastActivity(long lastActivity) {
	def lastActivityValue = ""
	def diffTotal = now() - lastActivity       
	def diffDays  = (diffTotal / 86400000) as long
	def diffHours = (diffTotal % 86400000 / 3600000) as long
	def diffMins  = (diffTotal % 86400000 % 3600000 / 60000) as long
    
	if      (diffDays == 1)  lastActivityValue += "${diffDays} Day "
	else if (diffDays > 1)   lastActivityValue += "${diffDays} Days "
    
	if      (diffHours == 1) lastActivityValue += "${diffHours} Hour "
	else if (diffHours > 1)  lastActivityValue += "${diffHours} Hours "
    
	if      (diffMins == 1)  lastActivityValue += "${diffMins} Min"
	else if (diffMins > 1)   lastActivityValue += "${diffMins} Mins"    
    
	sendEvent(name: "lastActivity", value: lastActivityValue, display: false , displayed: false)
}
