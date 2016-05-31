/**
 *  MyQ Garage Door Opener
 *
 *  Copyright 2015 Jason Mok/Brian Beaird
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
 *  Last Updated : 5/31/2016
 *
 */
metadata {
	definition (name: "MyQ Garage Door Opener", namespace: "brbeaird", author: "Jason Mok/Brian Beaird") {
		capability "Garage Door Control"
		capability "Door Control"
		capability "Contact Sensor"
		capability "Refresh"
		capability "Polling"

		capability "Actuator"
		capability "Switch"
		capability "Momentary"
		capability "Sensor"
		
		attribute "lastActivity", "string"
		command "updateDeviceStatus", ["string"]
		command "updateDeviceLastActivity", ["number"]
	}

	simulator {	}

	tiles {
		standardTile("door", "device.door", width: 2, height: 2) {
			state("unknown", label:'${name}', action:"door control.close",  icon:"st.doors.garage.garage-open",    backgroundColor:"#ffa81e", nextState: "closing")
			state("closed",  label:'${name}', action:"door control.open",   icon:"st.doors.garage.garage-closed",  backgroundColor:"#79b821", nextState: "opening")
			state("open",    label:'${name}', action:"door control.close",  icon:"st.doors.garage.garage-open",    backgroundColor:"#ffa81e", nextState: "closing")
			state("opening", label:'${name}', icon:"st.doors.garage.garage-opening", backgroundColor:"#ffe71e", nextState: "open")
			state("closing", label:'${name}', icon:"st.doors.garage.garage-closing", backgroundColor:"#ffe71e", nextState: "closed")
			state("stopped", label:'stopped', action:"door control.close",  icon:"st.doors.garage.garage-opening", backgroundColor:"#1ee3ff", nextState: "closing")
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
def parse(String description) {}

def on() { 
	push("open")
	sendEvent(name: "button", value: "on", isStateChange: true, display: false, displayed: false)
}
def off() { 
    push("closed")
	sendEvent(name: "button", value: "off", isStateChange: true, display: false, displayed: false)
}

def push(String desiredstate) { 
	def doorState = device.currentState("door")?.value
	if ((doorState == "open" || doorState == "stopped") && desiredstate == "closed") {
		close()
	} else if (doorState == "closed" && desiredstate == "open") {
		open()
	} 
	sendEvent(name: "momentary", value: "pushed", display: false, displayed: false)
}

def open()  { 
	parent.sendCommand(this, "desireddoorstate", 1) 
	updateDeviceStatus("opening")	
}
def close() { 
	parent.sendCommand(this, "desireddoorstate", 0) 
	updateDeviceStatus("closing")	
}

def refresh() {	
    parent.syncDoorsWithSensors()
}

def poll() { refresh() }

// update status
def updateDeviceStatus(status) {	
    
    def currentState = device.currentState("door")?.value
    log.debug "Door status updated to : " + status
    
    //Don't do anything if nothing changed
    if (currentState == status){
    	log.debug "No change; door is already set to " + status
        status = ""
    }
    
	if (status == "open") {
    	log.debug "Door is now open"
		sendEvent(name: "door", value: "open", display: true, descriptionText: device.displayName + " is open") 
		sendEvent(name: "contact", value: "open", display: false, displayed: false)	
	}   
	if (status == "closed") {
		log.debug "Door is now closed"
        sendEvent(name: "door", value: "closed", display: true, descriptionText: device.displayName + " is closed")
		sendEvent(name: "contact", value: "closed", display: false, displayed: false)
	}
	if (status == "3") { 
		sendEvent(name: "door", value: "stopped", display: true, descriptionText: device.displayName + " has stopped")
		sendEvent(name: "contact", value: "closed", display: false, displayed: false)
	}
	if (status == "4" || (status=="8" && currentState=="closed")) { 
		sendEvent(name: "door", value: "opening", display: false, displayed: false) 
	}  
	if (status == "5" || (status=="8" && currentState=="open")) { 
		sendEvent(name: "door", value: "closing", display: false, displayed: false) 
	}  
}

def updateDeviceLastActivity(lastActivity) {
	def finalString = lastActivity.format('MM/d/yyyy hh:mm a',location.timeZone)    
	sendEvent(name: "lastActivity", value: finalString, display: false , displayed: false)
}

def log(msg){
	log.debug msg
}
