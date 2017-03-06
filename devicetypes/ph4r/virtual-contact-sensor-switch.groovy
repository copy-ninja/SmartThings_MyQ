/**
 *  Virtual Contact Sensor Switch
 *
 *  Copyright 2017 Wesley Stocker
 *  ON == Open
 *  OFF == Closed
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
 */
metadata {
	definition (name: "Virtual Contact Sensor Switch", namespace: "ph4r", author: "Wesley Stocker") {
		capability "Actuator"
		capability "Contact Sensor"
		capability "Relay Switch"
		capability "Sensor"
		capability "Switch"

		command "onPhysical"
		command "offPhysical"
		command "open"
		command "close"
	}


	simulator {
		status "open": "contact:open"
		status "closed": "contact:closed"
	}

	tiles {
		standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
			state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
		}
		standardTile("on", "device.switch", decoration: "flat") {
			state "default", label: 'On', action: "onPhysical", backgroundColor: "#ffffff"
		}
		standardTile("off", "device.switch", decoration: "flat") {
			state "default", label: 'Off', action: "offPhysical", backgroundColor: "#ffffff"
		}
        standardTile("contact", "device.contact", width: 2, height: 2) {
			state("closed", label:'${name}', icon:"st.contact.contact.closed", backgroundColor:"#00A0DC", action: "open")
			state("open", label:'${name}', icon:"st.contact.contact.open", backgroundColor:"#e86d13", action: "close")
		}
        main "switch"
		details(["switch","on","off","contact"])
	}
}

def parse(String description) {
	def pair = description.split(":")
	createEvent(name: pair[0].trim(), value: pair[1].trim())
}

def open() {
	log.trace "open()"
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "contact", value: "open")
}

def close() {
	log.trace "close()"
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "contact", value: "closed")
}

def on() {
	log.debug "$version on()"
	sendEvent(name: "switch", value: "on")
	sendEvent(name: "contact", value: "open")
}

def off() {
	log.debug "$version off()"
	sendEvent(name: "switch", value: "off")
    sendEvent(name: "contact", value: "closed")
}

def onPhysical() {
	log.debug "$version onPhysical()"
	sendEvent(name: "switch", value: "on", type: "physical")
	sendEvent(name: "contact", value: "open")
}

def offPhysical() {
	log.debug "$version offPhysical()"
	sendEvent(name: "switch", value: "off", type: "physical")
    sendEvent(name: "contact", value: "closed")
}

private getVersion() {
	"PUBLISHED"
}
