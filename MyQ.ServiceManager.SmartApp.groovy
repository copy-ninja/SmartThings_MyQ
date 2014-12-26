/**
 *  MyQ Service Manager SmartApp
 * 
 *  Author: Jason Mok
 *  Date: 2014-12-26
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
	name: "MyQ",
	namespace: "copy-ninja",
	author: "Jason Mok",
	description: "Connect MyQ to control your devices",
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
		def doorList = getDoorList()
		return dynamicPage(name: "prefListDoor",  title: "Doors", install:true, uninstall:true) {
			if (doorList) {
				section("Select which door to use"){
					input(name: "doors", type: "enum", required:false, multiple:true, metadata:[values:doorList])
				}
			}
		}  
	} else {
		return dynamicPage(name: "prefListDoor",  title: "Error!", install:false, uninstall:true) {
			section(""){
				paragraph "The username or password you entered is incorrect. Try again. " 
			}
		}  
	}
}

/* Initialization */
def installed() {
	initialize()
    
	// Schedule polling
	unschedule()
	schedule("0 0/" + ((settings.polling.toInteger() > 0 )? settings.polling.toInteger() : 1)  + " * * * ?", refresh )
}

def updated() { initialize() }

def uninstalled() {
	unschedule()
	def deleteDevices = getAllChildDevices()
	deleteDevices.each { deleteChildDevice(it.deviceNetworkId) }
}	

def initialize() {    
	unsubscribe()
	login()
    
	// Get initial device status in state.data
	state.polling = [ 
		last: now(),
		runNow: true
	]
	state.data = [:]
    
	// Create new devices for each selected doors
	def selectedDevices = []
	def doorsList = getDoorList()
	def deleteDevices 
   	 
	if (settings.doors) {
		if (settings.doors[0].size() > 1) {
			selectedDevices = settings.doors
		} else {
			selectedDevices.add(settings.doors)
		}
	}
     
	selectedDevices.each { dni ->    	
		def childDevice = getChildDevice(dni)
		if (!childDevice) {
			if (dni.contains("GarageDoorOpener")) {
				addChildDevice("copy-ninja", "MyQ Garage Door Opener", dni, null, ["name": "MyQ: " + doorsList[dni],  "completedSetup": true])
			}
		} 
	}
    
	//Remove devices that are not selected in the settings
	if (!selectedDevices) {
		deleteDevices = getAllChildDevices()
	} else {
		deleteDevices = getChildDevices().findAll { !selectedDevices.contains(it.deviceNetworkId) }
	}
	deleteDevices.each { deleteChildDevice(it.deviceNetworkId) } 
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
	apiGet("/api/user/validatewithculture", [username: settings.username, password: settings.password, culture: "en"] ) { response ->
		if (response.status == 200) {
			if (response.data.UserId != 0) {
				state.session.userID = response.data.UserId
				state.session.brandID = response.data.BrandId
				state.session.brandName = response.data.BrandName
				state.session.securityToken = response.data.SecurityToken
				state.session.expiration = now() + 300000
				return true
			} else {
				return false
			}
		} else {
			return false
		}
	} 	
}

// Listing all the garage doors you have in MyQ
private getDoorList() { 	    
	def doorList = [:]
	apiGet("/api/userdevicedetails", []) { response ->
		if (response.status == 200) {
			response.data.Devices.each { device ->
				if ((device.TypeId == 47)||(device.TypeId == 259)) {
					def dni = [ app.id, "GarageDoorOpener", device.DeviceId ].join('|')
					device.Attributes.each { 
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
	// set up query
	apiQuery = [ appId: getApiAppID() ] + apiQuery
	if (state.session.securityToken) { apiQuery = apiQuery + [securityToken: state.session.securityToken ] }
    
	// set up parameters
	def apiParams = [ 
		uri: getApiURL(),
		path: apiPath,
		query: apiQuery
	]
	
	// try to call 
	try {
		httpGet(apiParams) { response ->
			if (response.data.ErrorMessage) {
				log.debug "API Error: $response.data"
			}            
			callback(response)
		}
	}	catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// HTTP POST call
private apiPut(apiPath, apiBody = [], callback = {}) {    
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
    
	try {
		httpPut(apiParams) { response ->
			if (response.data.ErrorMessage) {
				log.debug "API Error: $response.data"
			}            
			callback(response)
		}
	} catch (Error e)	{
		log.debug "API Error: $e"
	}
}

// Updates data for devices
private updateDeviceData() {    
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
	state.polling = [ 
		last: now(),
		runNow: true
	]
	
	//update device to state data
	updateDeviceData()
	
	//force devices to poll to get the latest status
	pollAllChild()
}

// Get Device ID
def getChildDeviceID(child) {
	return child.device.deviceNetworkId.split("\\|")[2]
}


// Get single device status
def getDeviceStatus(child) {
	return state.data[child.device.deviceNetworkId].status
}


// Get single device last activity
def getDeviceLastActivity(child) {
	return state.data[child.device.deviceNetworkId].lastAction.toLong()
}

// Send command to start or stop
def sendCommand(child, apiCommand) {
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
	
	return true
}
