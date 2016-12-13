/**
 *  MyQ Garage Door Opener NoSensor
 *
 *  Copyright 2016 Brian Beaird
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
 *  Last Updated : 12/12/2016
 *
 */
metadata {
	definition (name: "MyQ Garage Door Opener-NoSensor", namespace: "brbeaird", author: "Brian Beaird") {
		capability "Garage Door Control"
		capability "Door Control"		
	}

	simulator {	}

	tiles {
		
		multiAttributeTile(name:"door", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.door", key: "PRIMARY_CONTROL") {
				attributeState "unknown", label:'MyQ Door (No sensor)', icon:"st.doors.garage.garage-closed",    backgroundColor:"#6495ED"
				attributeState "moving",  label:'${name}', action:"door control.open",   icon:"st.doors.garage.garage-closed",  backgroundColor:"#4169E1", nextState: "opening"
				attributeState "open",    label:'${name}', action:"door control.close",  icon:"st.doors.garage.garage-open",    backgroundColor:"#ffa81e", nextState: "waiting"
				attributeState "opening", label:'${name}', 								 icon:"st.doors.garage.garage-opening", backgroundColor:"#cec236"
				attributeState "closing", label:'${name}', 								 icon:"st.doors.garage.garage-closing", backgroundColor:"#cec236"
				attributeState "waiting", label:'${name}', 								 icon:"st.doors.garage.garage-closing", backgroundColor:"#cec236", nextState: "closing"
				attributeState "stopped", label:'${name}', action:"door control.close",  icon:"st.doors.garage.garage-closing", backgroundColor:"#1ee3ff", nextState: "closing"
			}			
		}
		
        valueTile("openButton", "device.longText", width: 3, height: 2) {
			state "val", label:'OPEN', action: "open", backgroundColor:"#ffffff"
		}        
        valueTile("closeButton", "device.longText", width: 3, height: 2) {
			state "val", label:'CLOSE', action: "close", backgroundColor:"#ffffff"
		}
		main "door"
		details(["door", "openButton", "closeButton"])
	}
}

def open()  {
    openPrep()
    parent.sendCommand(this, "desireddoorstate", 1)    
}
def close() {
    closePrep()
    parent.sendCommand(this, "desireddoorstate", 0) 
}

def openPrep(){
	sendEvent(name: "door", value: "opening", descriptionText: "Open button pushed.", isStateChange: true, display: false, displayed: true)
    log.debug "Opening!"
    runIn(20, resetToUnknown)	//Force a sync with tilt sensor after 20 seconds    
}

def closePrep(){
	sendEvent(name: "door", value: "closing", descriptionText: "Close button pushed.", isStateChange: true, display: false, displayed: true)
    log.debug "Closing!"
    runIn(20, resetToUnknown)	//Force a sync with tilt sensor after 20 seconds     
}



def resetToUnknown(){
	sendEvent(name: "door", value: "unknown", isStateChange: true, display: false, displayed: false)
}

def log(msg){
	log.debug msg
}