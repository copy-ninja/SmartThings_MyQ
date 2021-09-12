/**
 *  MyQ Garage Door Opener NoSensor
 *
 *  Copyright 2019 Brian Beaird
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
	definition (name: "MyQ Garage Door Opener-NoSensor", namespace: "brbeaird", author: "Brian Beaird", ocfDeviceType: "oic.d.garagedoor", vid: "9a894a1f-5648-3799-ab9f-4a157697ef5a", mnmn: "SmartThingsCommunity") {
		capability "Door Control"
		capability "Garage Door Control"
        capability "Actuator"
        capability "towertalent27877.myqopen"
        capability "towertalent27877.myqclose"

        attribute "OpenButton", "string"
        attribute "CloseButton", "string"
        attribute "myQDeviceId", "string"
        attribute "myQAccountId", "string"

        command "open"
        command "close"
        command "sendOpen"
        command "sendClose"
        command "updateMyQDeviceId", ["string"]
	}

	simulator {	}

	tiles {

		multiAttributeTile(name:"door", type: "lighting", width: 6, height: 4, canChangeIcon: false) {
			tileAttribute ("device.door", key: "PRIMARY_CONTROL") {
				attributeState "unknown", label:'MyQ Door (No sensor)', icon:"st.doors.garage.garage-closed",    backgroundColor:"#6495ED"
			}
		}

        standardTile("openBtn", "device.OpenButton", width: 3, height: 3) {
            state "normal", label: 'Open', icon: "st.doors.garage.garage-open", backgroundColor: "#e86d13", action: "open", nextState: "opening"
            state "opening", label: 'Opening', icon: "st.doors.garage.garage-opening", backgroundColor: "#cec236", action: "open"
		}
        standardTile("closeBtn", "device.CloseButton", width: 3, height: 3) {
            state "normal", label: 'Close', icon: "st.doors.garage.garage-closed", backgroundColor: "#00a0dc", action: "close", nextState: "closing"
            state "closing", label: 'Closing', icon: "st.doors.garage.garage-closing", backgroundColor: "#cec236", action: "close"
		}

		main "door"
		details(["door", "openBtn", "closeBtn"])
	}
}

def open()  {
    openPrep()
    parent.sendDoorCommand(getMyQDeviceId(), getMyQAccountId(), "open")
}
def close() {
    closePrep()
    parent.sendDoorCommand(getMyQDeviceId(), getMyQAccountId(), "close")
}

def sendOpen()  {
    openPrep()
    parent.sendDoorCommand(getMyQDeviceId(), getMyQAccountId(),"open")
}
def sendClose() {
    closePrep()
    parent.sendDoorCommand(getMyQDeviceId(), getMyQAccountId(),"close")
}

def openPrep(){
	sendEvent(name: "door", value: "opening", descriptionText: "Open button pushed.", isStateChange: true, display: false, displayed: true)
    log.debug "Opening!"
    runIn(20, resetToUnknown) //Reset to normal state after 20 seconds
}

def closePrep(){
	sendEvent(name: "door", value: "closing", descriptionText: "Close button pushed.", isStateChange: true, display: false, displayed: true)
    log.debug "Closing!"
    runIn(20, resetToUnknown)  //Reset to normal state after 20 seconds
}



def resetToUnknown(){
	sendEvent(name: "door", value: "closed", isStateChange: true, display: false, displayed: false)
}

def getMyQDeviceId(){
    if (device.currentState("myQDeviceId")?.value)
    	return device.currentState("myQDeviceId").value
	else{
        def newId = device.deviceNetworkId.split("\\|")[2]
        sendEvent(name: "myQDeviceId", value: newId, display: true , displayed: true)
        return newId
    }
}

def getMyQAccountId(){
    if (device.currentState("myQAccountId")?.value)
    	return device.currentState("myQAccountId").value
	else{
        return parent.getDefaultAccountId()
    }
}

def updateMyQDeviceId(Id, account) {
	log.debug "Setting MyQID to ${Id}, accountId to ${account}"
    sendEvent(name: "myQDeviceId", value: Id, display: true , displayed: true)
    sendEvent(name: "myQAccountId", value: account, display: true , displayed: true)
}

def log(msg){
	log.debug msg
}

def showVersion(){
	return "4.0.1"
}