/**
 *	MyQ Service Manager SmartApp
 * 
 *  Author: Jason Mok
 *  Date: 2014-12-20
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
 */
definition(
    name: "MyQ Garage Door",
    namespace: "copy-ninja",
    author: "Jason Mok",
    description: "Connect MyQ to control your garage door",
    category: "SmartThings Labs",
    iconUrl:   "http://smartthings.copyninja.net/icons/MyQ@1x.png",
    iconX2Url: "http://smartthings.copyninja.net/icons/MyQ@2x.png",
    iconX3Url: "http://smartthings.copyninja.net/icons/MyQ@3x.png"
)

preferences {
	page(name: "prefLogIn", title: "MyQ")    
	page(name: "prefListDoor", title: "MyQ")
}

/* Preferences */
def prefLogIn() {
	def showUninstall = username != null && password != null 
    return dynamicPage(name: "prefLogIn", title: "Connect to MyQ", nextPage:"prefListDoor", uninstall:showUninstall, install: false) {
    	
        section("Login Credentials"){
        	input("username", "text", title: "Username", description: "MyQ Username (email address)")
        	input("password", "password", title: "Password", description: "MyQ password")
        }
        section("Brand"){
        	input(name: "brand", title: "Brand", type: "enum",  metadata:[values:["Liftmaster","Chamberlain","Craftsman"]] )
        }
        section("Connectivity"){
        	input(name: "polling", title: "Server Polling (in Minutes)", type: "int", description: "in minutes", defaultValue: "5" )
        }              
    }
}

def prefListDoor() {
    if (forceLogin()) {
        return dynamicPage(name: "prefListDoor",  title: "Doors", install:true, uninstall:true) {
            section("Select which door to use"){
                input(name: "doors", type: "enum", required:false, multiple:true, metadata:[values:getDoorList()])
            }
        }  
    }
}

/* Initialization */
def installed() {
	log.info  "installed()"
	log.debug "Installed with settings: " + settings
    unschedule()
    forceLogin()
	initialize()
}

def updated() {
	log.info  "updated()"
	log.debug "Updated with settings: " + settings
    unschedule()
	unsubscribe()
	login()
    initialize()
}

def uninstalled() {
	def delete = getAllChildDevices()
	delete.each { deleteChildDevice(it.deviceNetworkId) }
}	

def initialize() {    
	log.info  "initialize()"
    
    // Get initial device status in state.data
    refresh()
    
    // Create new devices for each selected doors
    def selectedDoors = []
    def doorsList = getDoorList()
    def delete 
   	
    log.debug "settings.doors: " + settings.doors
    if (settings.doors) {
    	if (settings.doors[0].size() > 1) {
        	selectedDoors = settings.doors
        } else {
        	selectedDoors.add(settings.doors)
        }
    }
    
    log.debug "selectedDoors: " + selectedDoors
    selectedDoors.each { dni ->    	
    	def childDoorDevice = getChildDevice(dni)
        if (!childDoorDevice) {
            addChildDevice("copy-ninja", "MyQ Garage Door", dni, null, ["name": "MyQ: " + doorsList[dni],  "completedSetup": true])
        } 
    }
    
    //Remove devices that are not selected in the settings
    if (!selectedDoors) {
    	delete = getAllChildDevices()
    } else {
    	delete = getChildDevices().findAll { !selectedDoors.contains(it.deviceNetworkId) }
    }
    delete.each { deleteChildDevice(it.deviceNetworkId) } 
	
    def allChildDoors = getAllChildDevices()
    log.debug "allChildDoors: " + allChildDoors
    

}

/* Access Management */
private forceLogin() {
	//Reset token and expiry
	state.session = [ 
    	userID: 0,
		brandID: 0,
		brandName: settings.brand,
		securityToken: null,
		expiration: 0
	]
    
    state.polling = [ 
		last: now(),
		runNow: true
	]
    
    //Reset data
    state.data = [:]
    
	return doLogin()
}

private login() {
	if (!(state.session.expiration > now())) {
		return doLogin()
    } else {
    	return true
    }
}

private doLogin() { 
	log.info "Trying to log in"
    apiGet("/api/user/validatewithculture", [username: settings.username, password: settings.password, culture: "en"] ) { response ->
        if (response.status == 200) {
        	state.session.userID = response.data.UserId
    		state.session.brandID = response.data.BrandId
    		state.session.brandName = response.data.BrandName
            state.session.securityToken = response.data.SecurityToken
            state.session.expiration = now() + 300000
            log.debug "Log in success: " + state.session
            return true
        } else {
            return false
        }
    } 	
}


// Listing all the garage doors you have in MyQ
private getDoorList() { 	    
	log.info "Trying to get door list"
    def doorList = [:]
    apiGet("/api/userdevicedetails", []) { response ->
    	if (response.status == 200) {
			response.data.Devices.each { device ->
            	if ((device.TypeId == 47)||(device.TypeId == 259)) {
                    def dni = [ app.id, device.DeviceId ].join('|')
                    device.Attributes.each{ 
                    	if (it.Name=="desc") {
                        	doorList[dni] = it.Value
                        }
                        if (it.Name=="doorstate") { 
                        	state.data[dni] = [ 
                            	status: it.Value,
                                lastAction: it.UpdatedTime
                            ]
                        }     
                    }                    
                }
			}
		}
	}    
    log.debug "Door List: " + doorList
    log.debug "state data: " + state.data
    return doorList
}

/* api connection */

// get URL 
private getApiURL() {
	if (settings.brand == "Craftsman") {
    	return "https://craftexternal.myqdevice.com"
    } else {
    	return "https://myqexternal.myqdevice.com"
    }
}

private getApiAppID() {
	if (settings.brand == "Craftsman") {
    	return "eU97d99kMG4t3STJZO/Mu2wt69yTQwM0WXZA5oZ74/ascQ2xQrLD/yjeVhEQccBZ"
    } else {
    	return "NWknvuBd7LoFHfXmKNMBcgajXtZEgKUh4V7WNzMidrpUUluDpVYVZx+xT4PCM5Kx"
    }
}
	
// HTTP GET call
private apiGet(apiPath, apiQuery = [], callback = {}) {
	log.info "Calling HTTP GET"    
	
    // set up query
    apiQuery = [ appId: getApiAppID() ] + apiQuery
    if (state.session.securityToken) { apiQuery = apiQuery + [securityToken: state.session.securityToken ] }
    
    // set up final parameters
	def apiParams = [ 
    	uri: getApiURL(),
    	path: apiPath,
        query: apiQuery
	]
    log.debug "apiParams: " + apiParams 
	
    // try to call 
    try {
		httpGet(apiParams) { response ->
			if (response.data.ErrorMessage) {
				log.debug "API Error: $response.data"
			}            
			callback(response)
		}
	}
    catch (Error e)	{
		log.debug "API Error: $e"
	}
}
// HTTP POST call
private apiPut(apiPath, apiBody = [], callback = {}) {
	log.debug "Calling HTTP PUT"
    
    // set up body
    apiBody = [ ApplicationId: getApiAppID() ] + apiBody
    if (state.session.securityToken) { apiBody = apiBody + [securityToken: state.session.securityToken ] }
    
    // set up final parameters
	def apiParams = [ 
    	uri: getApiURL(),
    	path: apiPath,
        contentType: "application/json; charset=utf-8",
        body: apiBody
	]
    log.debug "apiParams: " + apiParams 
    
    try {
		httpPut(apiParams) { response ->
			if (response.data.ErrorMessage) {
				log.debug "API Error: $response.data"
			}            
			callback(response)
		}
	}
    catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// Updates data for devices
private updateDeviceData() {    
	log.info "updateDeviceData()"
    // automatically checks if the token has expired, if so login again
    if (login()) {        
        // Next polling time, defined in settings
        def next = (state.polling.last?:0) + ((settings.polling.toInteger() > 0 ? settings.polling.toInteger() : 1) * 60 * 1000)
        if ((now() > next) || (state.polling.runNow)) {
            // set polling states
            state.polling.last = now()
            state.polling.runNow = false

            // Get all the door information, updated to state.data
            getDoorList()
        }
    }
}

// Get Door ID
private getChildDeviceID(child) {
	return child.device.deviceNetworkId.split("\\|")[1]
}

//Poll all the child
def pollAllChild() {
    // get all the children and send updates
    def childDevice = getAllChildDevices()
    childDevice.each { 
        log.debug "Polling " + it.deviceNetworkId
        it.poll()
    }
}


/* for SmartDevice to call */

// Refresh data
def refresh() {
	log.info "refresh()"
	state.polling = [ 
    	last: now(),
        runNow: true
    ]
    state.data = [:]
    
    //update device to state data
    updateDeviceData()
    
    //force devices to poll to get the latest status
    pause(1000)
    pollAllChild()
}

// Get single device status
def getDeviceStatus(child) {
	log.info "getDeviceStatus()"
	return state.data[child.device.deviceNetworkId].status
}
// Get single device last activity
def getDeviceLastActivity(child) {
	log.info "getDeviceLastActivity()"
	return state.data[child.device.deviceNetworkId].lastAction
}

// Send command to start or stop
def sendCommand(child, apiCommand) {
	log.debug "received command: " + apiCommand
	def apiPath = "/api/deviceattribute/putdeviceattribute"
	def apiBody = [
    	DeviceId: getChildDeviceID(child),
        AttributeName: "desireddoorstate",
        AttributeValue: apiCommand
    ]    
    
	//Try to get the latest data first
	updateDeviceData()    
	
    //Send command
    apiPut(apiPath, apiBody) 	
	
	//Forcefully get the latest data after waiting for 2.5 seconds
	pause(12000)
	refresh()
	
	return true
}
