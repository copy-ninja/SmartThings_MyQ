/**
 * -----------------------
 * ------ SMART APP ------
 * -----------------------
 *
 *  MyQ Lite
 *
 *  Copyright 2021 Jason Mok/Brian Beaird/Barry Burke/RBoy Apps
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
import java.security.MessageDigest
import groovy.transform.Field

include 'asynchttp'

String appVersion() { return "4.1.0" }
String appModified() { return "2021-09-25"}
String appAuthor() { return "Brian Beaird" }
String gitBranch() { return "brbeaird" }
String getAppImg(imgName) 	{ return "https://raw.githubusercontent.com/${gitBranch()}/SmartThings_MyQ/master/icons/$imgName" }

definition(
	name: "MyQ Lite",
	namespace: "brbeaird",
	author: "Jason Mok/Brian Beaird/Barry Burke",
	description: "Integrate MyQ with Smartthings",
	category: "SmartThings Labs",
	iconUrl:   "https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/icons/myq.png",
	iconX2Url: "https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/icons/myq@2x.png",
	iconX3Url: "https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/icons/myq@3x.png"
)

appSetting "MyQToken"

preferences {
	page(name: "mainPage", title: "MyQ Lite")
    page(name: "prefLogInMethod", title: "MyQ")
    page(name: "prefLogIn", title: "MyQ")
    page(name: "loginResultPage", title: "MyQ")
	page(name: "prefListDevices", title: "MyQ")
    page(name: "sensorPage", title: "MyQ")
    page(name: "noDoorsSelected", title: "MyQ")
    page(name: "summary", title: "MyQ")
    page(name: "prefUninstall", title: "MyQ")
}

def appInfoSect(sect=true)	{
	def str = ""
	str += "${app?.name} (v${appVersion()})"
	str += "\nAuthor: ${appAuthor()}"
	section() { paragraph str, image: getAppImg("myq@2x.png") }
}

def mainPage() {
    if (state.previousVersion == null){
        state.previousVersion = 0;
    }

    if (!state.oauth){
        state.oauth = [access_token: "", expiration: now() - 10000]
    }

    //Brand new install (need to grab version info)
    if (!state.latestVersion){
    	getVersionInfo(0, 0)
        state.currentVersion = [:]
        state.currentVersion['SmartApp'] = appVersion()
    }
    //Version updated
    else{
        getVersionInfo(state.previousVersion, appVersion())
        state.previousVersion = appVersion()
    }

    //If fresh install, go straight to login page
    if (!settings.loginMethod){
    	state.lastPage = "prefListDevices"
        return prefLogInMethod()
    }

     state.lastPage = "mainPage"

    dynamicPage(name: "mainPage", nextPage: "", uninstall: false, install: true) {
        appInfoSect()
        def devs = refreshChildren()
        def refreshMinutesAgo = state.oauth?.lastRefresh ? (now() - state.oauth?.lastRefresh) / 1000 / 60 : 0
        def lastRefresh = state.oauth?.lastRefresh ? "Last refresh: ${Math.round(refreshMinutesAgo)} minutes ago." : "(not yet refreshed)"
        def loginMessage = "Token loaded. ${lastRefresh}"
        def authMethod = loginMethod == "Email/Password" ? "Email (${settings.username})" : "Manual token"

        section("MyQ Account"){
            paragraph title: "", "Auth method: ${authMethod}"
            paragraph title: "", "Auth status: ${loginMessage}"
            href "prefLogInMethod", title: "", description: "Tap to modify account", params: [nextPageName: "mainPage"]
        }
        section("Connected Devices") {
        	paragraph title: "", "${devs?.size() ? devs?.join("\n") : "No MyQ Devices Connected"}"
            href "prefListDevices", title: "", description: "Tap to modify devices"
            input "prefDoorErrorNotify", "bool", required: false, defaultValue: true, title: "Notify on door command errors"
        }
        section("App and Handler Versions"){
            state.currentVersion.each { device, version ->
            	paragraph title: "", "${device} ${version} (${versionCompare(device)})"
            }
            href(name: "Release notes", title: "Release notes",
             required: false,
             url: "https://github.com/${gitBranch()}/SmartThings_MyQ/blob/master/CHANGELOG.md")
            input "prefUpdateNotify", "bool", required: false, title: "Notify when new version is available"
        }
        section("Uninstall") {
            paragraph "Tap below to completely uninstall this SmartApp and devices (doors and lamp control devices will be force-removed from automations and SmartApps)"
            href(name: "", title: "",  description: "Tap to Uninstall", required: false, page: "prefUninstall")
        }
    }
}

def versionCompare(deviceName){
    if (!state.currentVersion || !state.latestVersion || state.latestVersion == [:]){
        return 'latest'
    }
    if (state.currentVersion[deviceName] == state.latestVersion[deviceName]){
    	return 'latest'
    }
    else{
   		return "${state.latestVersion[deviceName]} available"
    }
}

def refreshChildren(){
	def useSensors = 0
    def useButtons = 0
    state.currentVersion = [:]
    state.currentVersion['SmartApp'] = appVersion()
    def devices = []
    childDevices.each { child ->
        def myQId = child.getMyQDeviceId() ? "ID: ${child.getMyQDeviceId()}" : 'Missing MyQ ID'
        def devName = child.name
        if (child.typeName == "MyQ Garage Door Opener"){
        	devName = devName + " (${child.currentContact})  ${myQId}"
            state.currentVersion['DoorDevice'] = child.showVersion()
            useSensors = 1
        }
        else if (child.typeName == "MyQ Garage Door Opener-NoSensor"){
        	devName = devName + " (No sensor)   ${myQId}"
            state.currentVersion['DoorDeviceNoSensor'] = child.showVersion()
		}
        else if (child.typeName == "MyQ Light Controller"){
        	devName = devName + " (${child.currentSwitch})  ${myQId}"
            state.currentVersion['LightDevice'] = child.showVersion()
        }
        else if (child.typeName == "MyQ Action Switch"){
        	useButtons = 1
        }
        else{
        	return
		}
        devices.push(devName)
    }
    state.useSensors = useSensors
    state.useButtons = useButtons
    return devices
}

/* Preferences */
def prefLogInMethod(params) {
	return dynamicPage(name: "prefLogInMethod", title: "Connect to MyQ", nextPage:"prefLogIn", uninstall:false, install: false, submitOnChange: true) {
		section("Login Method"){
			paragraph "Choose your login method. Email/Password is the recommend option, but you may also choose to provide a manually-generated refresh token."
            input(name: "loginMethod", type: "enum", title: "Login Method", options: ["Email/Password","Manual Token"])
		}
    }
}


def prefLogIn(params) {
    state.installMsg = ""
    def showUninstall = username != null && password != null
	return dynamicPage(name: "prefLogIn", title: "Connect to MyQ", nextPage:"loginResultPage", uninstall:false, install: false, submitOnChange: true) {
		if (loginMethod == "Email/Password"){
                section("Login Credentials"){
                input("username", "email", title: "Username", description: "MyQ Username (email address)")
                input("password", "password", title: "Password", description: "MyQ password")
		    }
        }
        else{
            section("Advanced (optional)"){
                paragraph "Manual refresh token entry: enter a manually-generated refresh token below if you prefer that the SmartApp not handle generating it. " +
                    "Note that this may result in more frequent auth issues and you may need to enter a fresh token here occasionally."
                input "manualRefreshToken", "password", required: false, title: "Manual refresh token"
            }
        }
	}
}

def loginResultPage(){
	log.debug "login result next page: ${state.lastPage}"

    //If manual refresh token has been changed, use it
    if (manualRefreshToken != state.oauth.lastManualRefreshToken){
    	log.debug "Using new manual refresh token."
        state.oauth.refreshToken = manualRefreshToken
        state.oauth.lastManualRefreshToken = manualRefreshToken
	}
    if (login()) {
    	if (state.lastPage == "prefListDevices")
        	return prefListDevices()
        else
        	return mainPage()
    }
    else{
    	def loginError = manualRefreshToken ? "Login failed with manual token. " : "The username or password you entered is incorrect. "
        loginError += "Go back and try again."
        return dynamicPage(name: "loginResultPage", title: "Login Error", install:false, uninstall:false) {
			section(""){
				paragraph loginError
			}
		}
    }
}

def prefUninstall() {
    log.debug "Removing MyQ Devices..."
    def msg = "Devices have been removed. Tap the three dots in the top right and then Delete to complete the process."
    childDevices.each {
		try{
			deleteChildDevice(it.deviceNetworkId, true)
		}
		catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
            msg = "There was a problem removing your device(s). Check the IDE logs for details."
		}
	}

    return dynamicPage(name: "prefUninstall",  title: "Uninstall", install:false, uninstall:true) {
        section("Uninstallation"){
			paragraph msg
		}
    }
}

def getDeviceSelectionList(deviceType){
	def testing
}

def prefListDevices() {
    state.lastPage = "prefListDevices"
    if (login()) {
    	getMyQDevices()

        state.useSensors = 0
        state.doorList = [:]
        state.lightList = [:]
        state.MyQDataPending.each { id, device ->
        	if (device.typeName == 'door'){
            	state.doorList[id] = device.name
            }
            else if (device.typeName == 'light'){
            	state.lightList[id] = device.name
            }
        }

		if ((state.doorList) || (state.lightList)){
        	def nextPage = "sensorPage"
            if (!state.doorList){nextPage = "summary"}  //Skip to summary if there are no doors to handle
                return dynamicPage(name: "prefListDevices",  title: "Devices", nextPage:nextPage, install:false, uninstall:false) {
                    if (state.doorList) {
                        section("Select which garage door/gate to use"){
                            input(name: "doors", type: "enum", required:false, multiple:true, metadata:[values:state.doorList])
                        }
                    }
                    if (state.lightList) {
                        section("Select which lights to use"){
                            input(name: "lights", type: "enum", required:false, multiple:true, metadata:[values:state.lightList])
                        }
                    }
                    section("Advanced (optional)", hideable: true, hidden:true){
        	            paragraph "BETA: Enable the below option if you would like to force the Garage Doors to behave as Door Locks (sensor required)." +
                        			"This may be desirable if you only want doors to open up via PIN with Alexa voice commands. " +
                                    "Note this is still considered highly experimental and may break many other automations/apps that need the garage door capability."
        	            input "prefUseLockType", "bool", required: false, title: "Create garage doors as door locks?"
					}
                }

        }else {
			return dynamicPage(name: "prefListDevices",  title: "Error!", install:false, uninstall:true) {
				section(""){
					paragraph "Could not find any supported device(s). Please report to author about these devices: " +  state.unsupportedList
				}
			}
		}
	} else {
		return dynamicPage(name: "prefListDevices",  title: "Error!", install:false, uninstall:true) {
				section(""){
					paragraph "Login error: ${state.loginError}"
				}
			}
	}
}


def sensorPage() {

    //If MyQ ID changes, the old stale ID will still be listed in the settings array. Let's get a clean count of valid doors selected
    state.validatedDoors = []
    if (doors instanceof List && doors.size() > 1){
        doors.each {
            if (state.MyQDataPending[it] != null){
                state.validatedDoors.add(it)
            }
        }
    }
    else{
    	state.validatedDoors = doors	//Handle single door
    }

    return dynamicPage(name: "sensorPage",  title: "Optional Sensors and Action Switches", nextPage:"summary", install:false, uninstall:false) {
        def sensorCounter = 1
        state.validatedDoors.each{ door ->
            section("Setup options for " + state.MyQDataPending[door].name){
                input "door${sensorCounter}Sensor",  "capability.contactSensor", required: false, multiple: false, title: state.MyQDataPending[door].name + " Contact Sensor"
                input "prefDoor${sensorCounter}PushButtons", "bool", required: false, title: "Create separate on/off action switches?"
            }
            sensorCounter++
            state.useSensors = 1
        }
        section("Sensor setup"){
        	paragraph "For each door above, you can specify an optional sensor that allows the device type to know whether the door is open or closed. This helps the device function as a switch " +
            	"you can turn on (to open) and off (to close) in other automations and SmartApps."
           	paragraph "Alternatively, you can choose the other option below to have separate additional opener and closer switch devices created. This is recommened if you have no sensors but still want a way to open/close the " +
            "garage from SmartTiles and other interfaces like Google Home that can't function with the built-in open/close capability. See wiki for more details"
        }
    }
}

def summary() {
	state.installMsg = ""
    try{
    	initialize()
    }

    //If error thrown during initialize, try to get the line number and display on installation summary page
    catch (e){
		def errorLine = "unknown"
        try{
        	log.debug e.stackTrace
            def pattern = ( e.stackTrace =~ /groovy.(\d+)./   )
            errorLine = pattern[0][1]
        }
        catch(lineError){}

		log.debug "Error at line number ${errorLine}: ${e}"
        state.installMsg = "There was a problem updating devices:\n ${e}.\nLine number: ${errorLine}\nLast successful step: ${state.lastSuccessfulStep}"
    }

    return dynamicPage(name: "summary",  title: "Summary", install:true, uninstall:true) {
        section("Installation Details:"){
			paragraph state.installMsg
		}
    }
}

/* Initialization */
def installed() {
}

def updated() {
	log.debug "MyQ Lite changes saved."
    unschedule()
    runEvery3Hours(updateVersionInfo)   //Check for new version every 3 hours

    if (state.useSensors == 1 && state.validatedDoors){
    	refreshAll()
    	runEvery30Minutes(refreshAll)
    }
    stateCleanup()
}

/* Version Checking */

//Called from scheduler every 3 hours
def updateVersionInfo(){
	getVersionInfo('versionCheck', appVersion())
}

//Get latest versions for SmartApp and Device Handlers
def getVersionInfo(oldVersion, newVersion){
    //Don't check for updates more 5 minutes

    if (state.lastVersionCheck && oldVersion == newVersion && (now() - state.lastVersionCheck) / 1000/60 < 5 ){
    	return
    }
    state.lastVersionCheck = now()
    log.info "Checking for latest version..."
    def params = [
        uri:  'http://www.brbeaird.com/getVersion',
        contentType: 'application/json',
        body: [
        	app: "myq",
            platform: "ST",
            prevVersion: oldVersion,
            currentVersion: newVersion,
        	sensor: state.useSensors == 1 ? true : false,
            door: state.validatedDoors?.size(),
            lock: prefUseLock ? true: false,
            light: state.validatedLights?.size(),
            button: state.useButtons
        ]
    ]
    def callbackMethod = oldVersion == 'versionCheck' ? 'updateCheck' : 'handleVersionUpdateResponse'
    asynchttp_v1.post(callbackMethod, params)
}

//When version response received (async), update state with the data
def handleVersionUpdateResponse(response, data) {
    if (response.hasError() || !response.json?.SmartApp) {
        log.error "Error getting version info: ${response.errorMessage}"
        state.latestVersion = [:]
    }
    else {state.latestVersion = response.json}
}

//In case of periodic update check, also refresh installed versions and update the version warning message
def updateCheck(response, data) {
	handleVersionUpdateResponse(response,data)
    refreshChildren()
    updateVersionMessage()
}

def updateVersionMessage(){
	state.versionMsg = ""
    state.currentVersion.each { device, version ->
    	if (versionCompare(device) != 'latest'){
        	state.versionMsg = "MyQ Lite Updates are available."
    	}
    }

    //Notify if updates are available
    if (state.versionMsg != ""){
        sendNotificationEvent(state.versionMsg)

        //Send push notification if enabled
        if (prefUpdateNotify){

            //Don't notify if we've sent a notification within the last 1 day
            if (state.lastVersionNotification){
            	def timeSinceLastNotification = (now() - state.lastVersionNotification) / 1000
                if (timeSinceLastNotification < 60*60*23){
                	return
                }
            }
            sendPush(state.versionMsg)
            state.lastVersionNotification = now()
    	}
    }
}


def uninstall(){
    log.debug "Removing MyQ Devices..."
    childDevices.each {
		try{
			deleteChildDevice(it.deviceNetworkId, true)
		}
		catch (e) {
			log.debug "Error deleting ${it.deviceNetworkId}: ${e}"
		}
	}
}

def uninstalled() {
	log.debug "MyQ removal complete."
    getVersionInfo(state.previousVersion, 0);
}


def initialize() {

    log.debug "Initializing..."
    state.data = state.MyQDataPending
    state.lastSuccessfulStep = ""
    unsubscribe()

    //Check existing installed devices against MyQ data
    verifyChildDeviceIds()

    //Mark sensors onto state door data
    def doorSensorCounter = 1
    state.validatedDoors.each{ door ->
        if (settings["door${doorSensorCounter}Sensor"]){
            state.data[door].sensor = "door${doorSensorCounter}Sensor"
        }
        doorSensorCounter++
    }
    state.lastSuccessfulStep = "Sensor Indexing"

    //Create door devices
    def doorCounter = 1
    state.validatedDoors.each{ door ->
        createChilDevices(door, settings[state.data[door].sensor], state.data[door].name, settings["prefDoor${doorCounter}PushButtons"])
        doorCounter++
    }
    state.lastSuccessfulStep = "Door device creation"


    //Create light devices
    if (lights){
        state.validatedLights = []
        if (lights instanceof List && lights.size() > 1){
            lights.each { lightId ->
                if (state.data[lightId] != null){
                    state.validatedLights.add(lightId)
                }
            }
        }
        else{
            state.validatedLights = lights
        }
        state.validatedLights.each { light ->
            if (light){
                def myQAccountId = state.data[light].myQAccountId
                def myQDeviceId = state.data[light].myQDeviceId
                def DNI = [ app.id, "LightController", myQDeviceId ].join('|')
                def lightName = state.data[light].name
                def childLight = getChildDevice(state.data[light].child)

                if (!childLight) {
                    log.debug "Creating child light device: " + light

                    try{
                        childLight = addChildDevice("brbeaird", "MyQ Light Controller", DNI, getHubID(), ["name": lightName])
                        state.data[myQDeviceId].child = DNI
                        state.installMsg = state.installMsg + lightName + ": created light device. \r\n\r\n"
                    }
                    catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                    {
                        log.debug "Error! " + e
                        state.installMsg = state.installMsg + lightName + ": problem creating light device. Check your IDE to make sure the brbeaird : MyQ Light Controller device handler is installed and published. \r\n\r\n"
                    }
                }
                else{
                    log.debug "Light device already exists: " + lightName
                    state.installMsg = state.installMsg + lightName + ": light device already exists. \r\n\r\n"
                }
                log.debug "Setting ${lightName} status to ${state.data[light].status}"
                childLight.updateDeviceStatus(state.data[light].status)
                childLight.updateMyQDeviceId(myQDeviceId, myQAccountId)
            }
        }
        state.lastSuccessfulStep = "Light device creation"
    }

    // Remove unselected devices
    getChildDevices().each{ child ->
    	log.debug "Checking ${child} for deletion"
        def myQDeviceId = child.getMyQDeviceId()
        if (myQDeviceId){
        	if (!(myQDeviceId in state.validatedDoors) && !(myQDeviceId in state.validatedLights)){
            	try{
                	log.debug "Child ${child} with ID ${myQDeviceId} not found in selected list. Deleting."
                    deleteChildDevice(child.deviceNetworkId, true)
                	log.debug "Removed old device: ${child}"
                    state.installMsg = state.installMsg + "Removed old device: ${child} \r\n\r\n"
                }
                catch (e)
                {
                    sendPush("Warning: unable to delete device: ${child}. You'll need to manually remove it.")
                    log.debug "Error trying to delete device: ${child} - ${e}"
                    log.debug "Device is likely in use in a Routine, or SmartApp (make sure and check Alexa, ActionTiles, etc.)."
                }
            }
        }
    }
    state.lastSuccessfulStep = "Old device removal"

    //Set initial values
    if (state.validatedDoors){
    	syncDoorsWithSensors()
    }
    state.lastSuccessfulStep = "Setting initial values"

    //Subscribe to sensor events
    settings.each{ key, val->
        if (key.contains('Sensor')){
        	subscribe(val, "contact", sensorHandler)
        }
    }
}

def verifyChildDeviceIds(){
	//Try to match existing child devices with latest MyQ data
    childDevices.each { child ->
        def matchingId
        if (child.typeName != 'MyQ Action Switch' && child.typeName != 'Virtual Switch'){
            //Look for a matching entry in MyQ
            state.data.each { myQId, myQData ->
                if (child.getMyQDeviceId() == myQId){
                    log.debug "Found matching ID for ${child}"
                    matchingId = myQId
                }

                //If no matching ID, try to match on name
                else if (child.name == myQData.name || child.label == myQData.name){
                    log.debug "Found matching ID (via name) for ${child}"
                    child.updateMyQDeviceId(myQId, myQData.myQAccountId)	//Update child to new ID
                    matchingId = myQId
                }
            }

            log.debug "final matchingid for ${child.name} ${matchingId}"
            if (matchingId){
                state.data[matchingId].child = child.deviceNetworkId
            }
            else{
                log.debug "WARNING: Existing child ${child} does not seem to have a valid MyQID"
            }
        }
    }
}

def createChilDevices(door, sensor, doorName, prefPushButtons){
    def sensorTypeName = "MyQ Garage Door Opener"
    def noSensorTypeName = "MyQ Garage Door Opener-NoSensor"
    def lockTypeName = "MyQ Lock Door"

    if (door){

    	def myQDeviceId = state.data[door].myQDeviceId
        def myQAccountId = state.data[door].myQAccountId
        def DNI = [ app.id, "GarageDoorOpener", myQDeviceId ].join('|')

        //Has door's child device already been created?
        def existingDev = getChildDevice(state.data[door].child)
        def existingType = existingDev?.typeName

        if (existingDev){
        	log.debug "Child already exists for " + doorName + ". Sensor name is: " + sensor
            state.installMsg = state.installMsg + doorName + ": door device already exists. \r\n\r\n"
            existingDev.updateMyQDeviceId(myQDeviceId, myQAccountId)

            if (prefUseLockType && existingType != lockTypeName){
                try{
                    log.debug "Type needs updating to Lock version"
                    existingDev.deviceType = lockTypeName
                    state.installMsg = state.installMsg + doorName + ": changed door device to lock version." + "\r\n\r\n"
                }
                catch(physicalgraph.exception.NotFoundException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem changing door to no-sensor type. Check your IDE to make sure the brbeaird : " + lockTypeName + " device handler is installed and published. \r\n\r\n"
                }
            }
            else if ((!sensor) && existingType != noSensorTypeName){
            	try{
                    log.debug "Type needs updating to no-sensor version"
                    existingDev.deviceType = noSensorTypeName
                    state.installMsg = state.installMsg + doorName + ": changed door device to No-sensor version." + "\r\n\r\n"
                    existingDev.resetToUnknown()
                }
                catch(physicalgraph.exception.NotFoundException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem changing door to no-sensor type. Check your IDE to make sure the brbeaird : " + noSensorTypeName + " device handler is installed and published. \r\n\r\n"
                }
            }

            else if (sensor && existingType != sensorTypeName && !prefUseLockType){
            	try{
                    log.debug "Type needs updating to sensor version"
                    existingDev.deviceType = sensorTypeName
                    state.installMsg = state.installMsg + doorName + ": changed door device to sensor version." + "\r\n\r\n"
                }
                catch(physicalgraph.exception.NotFoundException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem changing door to sensor type. Check your IDE to make sure the brbeaird : " + sensorTypeName + " device handler is installed and published. \r\n\r\n"
                }
            }
        }
        else{
            log.debug "Creating child door device " + door
            def childDoor

            if (prefUseLockType){
                try{
                    log.debug "Creating door with lock type"
                    childDoor = addChildDevice("brbeaird", lockTypeName, DNI, getHubID(), ["name": doorName])
                    childDoor.updateMyQDeviceId(myQDeviceId, myQAccountId)
                    state.installMsg = state.installMsg + doorName + ": created lock device \r\n\r\n"
                }
                catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem creating door device (lock type). Check your IDE to make sure the brbeaird : " + sensorTypeName + " device handler is installed and published. \r\n\r\n"

                }
            }

            else if (sensor){
                try{
                    log.debug "Creating door with sensor"
                    childDoor = addChildDevice("brbeaird", sensorTypeName, DNI, getHubID(), ["name": doorName])
                    childDoor.updateMyQDeviceId(myQDeviceId, myQAccountId)
                    state.installMsg = state.installMsg + doorName + ": created door device (sensor version) \r\n\r\n"
                }
                catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem creating door device (sensor type). Check your IDE to make sure the brbeaird : " + sensorTypeName + " device handler is installed and published. \r\n\r\n"

                }
            }
            else{
                try{
                    log.debug "Creating door with no sensor"
                    childDoor = addChildDevice("brbeaird", noSensorTypeName, DNI, getHubID(), ["name": doorName])
                    childDoor.updateMyQDeviceId(myQDeviceId, myQAccountId)
                    state.installMsg = state.installMsg + doorName + ": created door device (no-sensor version) \r\n\r\n"
                    childDoor.resetToUnknown()
                }
                catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem creating door device (no-sensor type). Check your IDE to make sure the brbeaird : " + noSensorTypeName + " device handler is installed and published. \r\n\r\n"
                }
            }
            state.data[door].child = childDoor.deviceNetworkId
        }

        //Create action switch devices
        if (prefPushButtons){
        	def existingOpenButtonDev = getChildDevice(door + " Opener")
            def existingCloseButtonDev = getChildDevice(door + " Closer")
            if (!existingOpenButtonDev){
                try{
                	def openButton = addChildDevice("brbeaird", "MyQ Action Switch", door + " Opener", getHubID(), [name: doorName + " Opener", label: doorName + " Opener"])
                    openButton.off()
                	state.installMsg = state.installMsg + doorName + ": created open action switch device. \r\n\r\n"
                	subscribe(openButton, "switch.on", doorButtonOpenHandler)
                }
                catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e
                    state.installMsg = state.installMsg + doorName + ": problem creating MyQ Action Switch device. Check your IDE to make sure the brbeaird : MyQ Action Switch device handler is installed and published. \r\n\r\n"
                }
            }
            else{
            	log.debug "Switch needs updating to new Action Type version"
                existingOpenButtonDev.deviceType = "MyQ Action Switch"
                subscribe(existingOpenButtonDev, "switch.on", doorButtonOpenHandler)
                state.installMsg = state.installMsg + doorName + ": Open action switch device already exists. Subscription recreated. \r\n\r\n"
                log.debug "subscribed to button: " + existingOpenButtonDev
            }

            if (!existingCloseButtonDev){
                try{
                    def closeButton = addChildDevice("brbeaird", "MyQ Action Switch", door + " Closer", getHubID(), [name: doorName + " Closer", label: doorName + " Closer"])
                    closeButton.off()
                    state.installMsg = state.installMsg + doorName + ": created close action switch device. \r\n\r\n"
                    subscribe(closeButton, "switch.on", doorButtonCloseHandler)
                }
                catch(physicalgraph.app.exception.UnknownDeviceTypeException e)
                {
                    log.debug "Error! " + e
                }
            }
            else{
                log.debug "Switch needs updating to new Action Type version"
                existingCloseButtonDev.deviceType = "MyQ Action Switch"
                subscribe(existingCloseButtonDev, "switch.on", doorButtonCloseHandler)
                state.installMsg = state.installMsg + doorName + ": Close action switch device already exists. Subscription recreated. \r\n\r\n"
                log.debug "subscribed to button: " + existingOpenButtonDev
            }
        }

        //Cleanup defunct action switch devices if no longer wanted
        else{
        	def pushButtonIDs = [door + " Opener", door + " Closer"]
            def devsToDelete = getChildDevices().findAll { pushButtonIDs.contains(it.deviceNetworkId)}
            log.debug "button devices to delete: " + devsToDelete
			devsToDelete.each{
            	log.debug "deleting button: " + it
                try{
                	deleteChildDevice(it.deviceNetworkId, true)
                    state.installMsg = state.installMsg + "Removed ${it}. \r\n\r\n"
                } catch (e){
                    state.installMsg = state.installMsg + "Warning: unable to delete action switch - you'll need to manually remove it. \r\n\r\n"
                    log.debug "Error trying to delete button " + it + " - " + e
                    log.debug "Button  is likely in use in a Routine, or SmartApp (make sure and check SmarTiles!)."
                }

            }
        }
    }
}


def syncDoorsWithSensors(child){
    state.validatedDoors.each { door ->
        log.debug "Refreshing ${door} ${state.data[door].child}"
        if (state.data[door].sensor){
            updateDoorStatus(state.data[door].child, settings[state.data[door].sensor], '', state.data[door].name)
        }
    }
}

def updateDoorStatus(doorDNI, sensor, child, doorName){
    try{
        if (!sensor){//If we got here somehow without a sensor, bail out
        	log.debug "Warning: no sensor found for ${doorDNI}"
            return 0}

		if (!doorDNI){
        	log.debug "Invalid doorDNI for sensor ${sensor} ${child}"
            return 0
        }

        //Get door to update and set the new value
        def doorToUpdate = getChildDevice(doorDNI)

        //Get current sensor value
        def currentSensorValue = "unknown"
        currentSensorValue = sensor.latestValue("contact")
        def currentDoorState = doorToUpdate.latestValue("door")
        doorToUpdate.updateSensorBattery(sensor.latestValue("battery"))

        if (currentDoorState != currentSensorValue){
        	log.debug "Updating ${doorName} from ${currentDoorState} to ${currentSensorValue} from sensor ${sensor}"
        }

        doorToUpdate.updateDeviceStatus(currentSensorValue)
        doorToUpdate.updateDeviceSensor("${sensor} is ${currentSensorValue}")

        //Write to child log if this was initiated from one of the doors
        if (child){child.log("Updating as ${currentSensorValue} from sensor ${sensor}")}

        //Get latest activity timestamp for the sensor (data saved for up to a week)
        def latestEvent
        def eventsSinceYesterday = sensor.eventsSince(new Date() - 7)
        def foundContactEvent = 0
        eventsSinceYesterday.each{ event ->
            if (foundContactEvent == 0 && event.name == "contact"){
                latestEvent = event.date
                foundContactEvent = 1
            }
        }

        //Update timestamp
        if (latestEvent){
            doorToUpdate.updateDeviceLastActivity(latestEvent)
        }
        else{	//If the door has been inactive for more than a week, timestamp data will be null. Keep current value in that case.
            log.debug "Door: ${doorName} Null timestamp detected from sensor ${sensor}. Keeping current value."
        }
    }catch (e) {
        log.debug "Error updating door: ${doorDNI}: ${e}"
    }
}

def refresh(child){
    def door = child.device.deviceNetworkId
    def doorName = state.data[child.getMyQDeviceId()].name
    child.log("refresh called from " + doorName + ' (' + door + ')')
    syncDoorsWithSensors(child)
}

def refreshAll(){
    syncDoorsWithSensors()
}

def refreshAll(evt){
	refreshAll()
}

def sensorHandler(evt) {
    log.debug "Sensor change detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId

    state.validatedDoors.each{ door ->
        if (settings[state.data[door].sensor]?.id == evt.deviceId)
            updateDoorStatus(state.data[door].child, settings[state.data[door].sensor], null, state.data[door].name)
    }
}

def doorButtonOpenHandler(evt) {
    try{
        log.debug "Door open button push detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId + " DNI: " + evt.getDevice().deviceNetworkId
        evt.getDevice().off()
        def myQDeviceId = evt.getDevice().deviceNetworkId.replace(" Opener", "")
        def doorDevice = getChildDevice(state.data[myQDeviceId].child)
        doorDevice.open()
    }catch(e){
    	def errMsg = "Warning: MyQ Open button command failed - ${e}"
        log.error errMsg
    }
}

def doorButtonCloseHandler(evt) {
	try{
		log.debug "Door close button push detected: Event name  " + evt.name + " value: " + evt.value   + " deviceID: " + evt.deviceId + " DNI: " + evt.getDevice().deviceNetworkId
        evt.getDevice().off()
        def myQDeviceId = evt.getDevice().deviceNetworkId.replace(" Closer", "")
        def doorDevice = getChildDevice(state.data[myQDeviceId].child)
        doorDevice.close()
	}catch(e){
    	def errMsg = "Warning: MyQ Close button command failed - ${e}"
        log.error errMsg
    }
}


def getSelectedDevices( settingsName ) {
	def selectedDevices = []
	(!settings.get(settingsName))?:((settings.get(settingsName)?.getAt(0)?.size() > 1)  ? settings.get(settingsName)?.each { selectedDevices.add(it) } : selectedDevices.add(settings.get(settingsName)))
	return selectedDevices
}

/* Access Management */
private login() {

   //If people still have an app setting but no new manual pref entry yet, use that one
   if (loginMethod != "Email/Password" && !manualRefreshToken && appSettings.MyQToken != ""){
       log.debug "Using legacy app setting method."
       state.oauth.refreshToken = appSettings.MyQToken;
   }

   //If we do not have a refresh token and a manual one wasn't entered, automatically fetch a new one using the email/password oauth flow
   if (!state.oauth?.refreshToken && !manualRefreshToken){
       if (!getNewAccessToken()){
           return
       }
   }

   //If the access token has expired, use the refresh token to get a new one
   if (!state.oauth?.expiration || now() > state?.oauth.expiration){
       log.warn "Token has expired (${state.oauth.expiration}). Using refresh token to get new access token."
       if (!doLogin()){
			return false
        }
   }
    return true
}


private doLogin() {
    try {
        def tokenBody = [
			"client_id": "IOS_CGI_MYQ",
            "client_secret": "UD4DXnKyPWq25BSw",
            "grant_type": "refresh_token",
            "redirect_uri": "com.myqops://ios",
            "scope": "MyQ_Residential offline_access",
            "refresh_token": state.oauth.refreshToken
        ]

        return httpPost([ uri: "https://partner-identity.myq-cloud.com", path: "/connect/token", headers: ["Content-Type": "application/x-www-form-urlencoded", "User-Agent": "null"], body: tokenBody ]) { response ->
            log.debug "Got LOGIN response: STATUS: ${response.status}"
            //log.debug "Got LOGIN POST response: STATUS: ${response.status}\n\nDATA: ${response.data}"
            if (response.status == 200) {
                state.oauth.lastRefresh = now()
                state.oauth.access_token = response.data.access_token
                state.oauth.refreshToken = response.data.refresh_token
                state.oauth.expiration = now() + (response.data.expires_in * 1000)
                return true
            } else {
                log.error "Unknown LOGIN POST status: ${response.status} data: ${response.data}"
                state.loginMessage = "${response.status}-${response.data}"
                state.oauth.expiration = now() - 1000
            }
            return false
        }
    } catch (e)	{
        log.warn "API POST Error: $e"
    }
    return false
}

//Get devices listed on your MyQ account
private getMyQDevices() {
	state.MyQDataPending = [:]
    state.unsupportedList = []

    //Get accounts
    def accounts = httpGet([ uri: "https://accounts.myq-cloud.com/api/v6.0/accounts", headers: getMyQHeaders()]) { response ->
        return response.data.accounts
    }
    if (!accounts){
        log.warn "No accounts found."
        return
    }

    accounts.each { account ->
        log.debug "Getting devices for account ${account.id}"

        def devices = httpGet([ uri: "https://devices.myq-cloud.com/api/v5.2/Accounts/${account.id}/Devices", headers: getMyQHeaders()]) { response ->
            return response.data.items
        }
        devices.each { device ->
            // 2 = garage door, 5 = gate, 7 = MyQGarage(no gateway), 9 = commercial door, 17 = Garage Door Opener WGDO
            //if (device.MyQDeviceTypeId == 2||device.MyQDeviceTypeId == 5||device.MyQDeviceTypeId == 7||device.MyQDeviceTypeId == 17||device.MyQDeviceTypeId == 9) {
            if (device.device_family == "garagedoor") {
                log.debug "Found door: ${device.name}"
                def dni = device.serial_number
                def description = device.name
                def doorState = device.state.door_state
                def updatedTime = device.last_update

                //Ignore any doors with blank descriptions
                if (description != ''){
                    log.debug "Got valid door: ${description} type: ${device.device_family} status: ${doorState} type: ${device.device_type}"
                    //log.debug "Storing door info: " + description + "type: " + device.device_family + " status: " + doorState +  " type: " + device.device_type
                    state.MyQDataPending[dni] = [ status: doorState, lastAction: updatedTime, name: description, typeId: device.MyQDeviceTypeId, typeName: 'door', sensor: '', myQDeviceId: device.serial_number, myQAccountId: account.id]
                }
                else{
                    log.debug "Door " + device.MyQDeviceId + " has blank desc field. This is unusual..."
                }
            }

            //Lights
            else if (device.device_family == "lamp") {
                def dni = device.serial_number
                def description = device.name
                def lightState = device.state.lamp_state
                def updatedTime = device.state.last_update

                //Ignore any lights with blank descriptions
                if (description && description != ''){
                    log.debug "Got valid light: ${description} type: ${device.device_family} status: ${lightState} type: ${device.device_type}"
                    state.MyQDataPending[dni] = [ status: lightState, lastAction: updatedTime, name: description, typeName: 'light', type: device.MyQDeviceTypeId, myQDeviceId: device.serial_number, myQAccountId: account.id ]
                }
            }

            //Unsupported devices
            else{
                state.unsupportedList.add([name: device.name, typeId: device.device_family, typeName: device.device_type])
            }
        }
    }
}

def getHubID(){
    def hubs = location.hubs.findAll{ it.type == physicalgraph.device.HubType.PHYSICAL }

    //Try and find a valid hub on the account
    def chosenHub
    hubs.each {
        if (it != null){
            chosenHub = it
        }
    }

    if (chosenHub != null){
        log.debug "Chosen hub for child devices: ${chosenHub} (${chosenHub.id})"
        return chosenHub.id
    }
    else{
        log.debug "No physical hubs found. Sending NULL"
        return null
    }
}

import groovy.transform.Field

@Field final MAX_RETRIES = 1 // Retry count before giving up

private getMyQHeaders() {
	return [
        "Authorization": "Bearer ${state.oauth.access_token}"
    ]
}

// HTTP PUT call (Send commands)
private apiPut(apiPath, apiBody = [], actionText = "") {
    if (!login()){
        log.error "Unable to complete PUT, login failed"
        sendNotificationEvent("Warning: MyQ command failed due to bad login.")
        //if (prefDoorErrorNotify){sendPush("Warning: MyQ command failed due to bad login.")}
        return false
    }
    try {
        //log.debug "Calling out PUT ${apiPath}${getMyQHeaders()}"
        return httpPut([ uri: apiPath, headers: getMyQHeaders()]) { response ->
            if (response.status != 200 && response.status != 204 && response.status != 202) {
                log.warn "Unexpected command response - ${response.status} ${response.data}"
            }
            return true;
        }
    } catch (e)	{
        if (e.response.data?.description == "Device already in desired state."){
            log.debug "Device already in desired state. Command ignored."
        	return true
		}
        sendNotificationEvent("Warning: MyQ command failed - ${e.response.status}")
        if (prefDoorErrorNotify){sendPush("Warning: MyQ command failed for ${actionText} - ${e}")}
        return false
    }
}

def sendDoorCommand(myQDeviceId, myQAccountId, command) {
	if (!myQAccountId){
    	if (!state.session?.accountId){
        	def doorError = "Door is missing accountId. Be sure you have the latest DTH installed and retry setup config."
            log.error doorError
            if (prefDoorErrorNotify){sendPush(doorError)}
            return false
        }
        else{
        	myQAccountId = state.session.accountId  //Bandaid for people who haven't tapped through the modify menu yet to assign accountId to door device
        }
    }
    state.lastCommandSent = now()
    return apiPut("https://account-devices-gdo.myq-cloud.com/api/v5.2/Accounts/${myQAccountId}/door_openers/${myQDeviceId}/${command}")
    return true
}

def sendLampCommand(myQDeviceId, myQAccountId, command) {
	if (!myQAccountId){
    	if (!state.session?.accountId){
        	def lampError = "Lamp is missing accountId. Be sure you have the latest DTH installed and retry setup config."
            log.error lampError
            if (prefDoorErrorNotify){sendPush(lampError)}
            return false
        }
        else{
        	myQAccountId = state.session.accountId  //Bandaid for people who haven't tapped through the modify menu yet to assign accountId to door device
        }
    }
    state.lastCommandSent = now()
    return apiPut("https://account-devices-lamp.myq-cloud.com/api/v5.2/Accounts/${myQAccountId}/lamps/${myQDeviceId}/${command}")
}

//Transition for people who have not yet clicked through "modify devices" steps
def getDefaultAccountId(){
	return state.session.accountId
}

//Remove old unused pieces of state
def stateCleanup(){
    if (state.latestDoorNoSensorVersion){state.remove('latestDoorNoSensorVersion')}
    if (state.latestDoorVersion){state.remove('latestDoorVersion')}
    if (state.latestLightVersion){state.remove('latestLightVersion')}
    if (state.latestSmartAppVersion){state.remove('latestSmartAppVersion')}
    if (state.thisDoorNoSensorVersion){state.remove('thisDoorNoSensorVersion')}
    if (state.thisDoorVersion){state.remove('thisDoorVersion')}
    if (state.thisLightVersion){state.remove('thisLightVersion')}
    if (state.thisSmartAppVersion){state.remove('thisSmartAppVersion')}
    if (state.versionWarning){state.remove('versionWarning')}
    if (state.polling){state.remove('polling')}
}

//Available to be called from child devices for special logging
def notify(message){
	sendNotificationEvent(message)
}


private getNewAccessToken(){
    log.debug "Doing fresh login for new access token."
    String code_verifier = generateCodeVerifier()
    String code_challenge = generateCodeChallange(code_verifier)
    //log.trace "code_verifier: $code_verifier, code_challenge: $code_challenge"

    def MYQ_API_CLIENT_ID = "IOS_CGI_MYQ"
    def MYQ_API_CLIENT_SECRET = "VUQ0RFhuS3lQV3EyNUJTdw=="
    def MYQ_API_REDIRECT_URI = "com.myqops://ios"
    def MYQ_GRANT_TYPE = "authorization_code"
    def authEndpoint = "https://partner-identity.myq-cloud.com"
    def authPath = "/connect/authorize"
    def tokenPath = "/connect/token"
    def searchParams = [
        client_id: "IOS_CGI_MYQ",
        code_challenge: code_challenge,
        code_challenge_method: "S256",
        redirect_uri: "com.myqops://ios",
        response_type: "code",
        scope: "MyQ_Residential offline_access"
    ]


    //log.debug searchParams
    def authHeaders = [
        "User-Agent": "null",
        contentType: "text/plain"
    ]

    def loginPostUrl
    def requestVerificationToken
    def authpageCookie

    //log.debug "${authEndpoint}${authPath}${searchParams}"
    try {
        httpGet([ uri: authEndpoint, path: authPath, query: searchParams, headers: authHeaders]) { response ->
            //log.debug "auth data text response ${response.data}"
            def doc = response.data
            requestVerificationToken = doc[0].children[1].children[0].children[0].children[2].children[2].attributes["value"]
            loginPostUrl = doc[0].children[1].children[0].children[0].children[2].attributes["action"]

            def cookieTest = response.headers["Set-Cookie"]
            def cleanCookie = []
            response.headers.each { header ->
                //log.trace header
                if (header.name == "Set-Cookie"){
                    //log.trace "${header.name}:${header.value}"
                    def cleanedCookie = header.value.split(";")[0]
                    //log.debug "cleaned: ${cleanedCookie}"
                    cleanCookie << cleanedCookie
                }
            }

            authpageCookie = cleanCookie.join("; ")
        }
    } catch (e) {
        log.error "Verification token status: ${e}"
    }

    log.debug "Got verification token:${requestVerificationToken}\nauthpageCookie:${authpageCookie}\nloginPostUrl:${authEndpoint}\n${loginPostUrl}"

    if (!requestVerificationToken || !authpageCookie || !loginPostUrl) {
        log.error "Cannot get verification token, auth cookie or login url"
        return
    }

    def loginBody = [
        "Email": settings.username,
        "Password": settings.password,
        "__RequestVerificationToken": requestVerificationToken
    ]

    def loginHeaders = [
        "Content-Type": "application/x-www-form-urlencoded",
        "Cookie": authpageCookie,
        "User-Agent": "null"
    ]

    def loginCookie = ""
    def redirectUrl = ""
    try {
        httpPost([ uri: authEndpoint + loginPostUrl, headers: loginHeaders, body: loginBody]) { response ->
            //log.debug "login response ${response.data}"
            def cleanCookie = []
            response.headers.each { header ->
                //log.trace "header: ${header}"
                if (header.name == "Set-Cookie"){
                    //log.trace "Header -> ${header.name}-${header.value}"
                    def cleanedCookie = header.value.split(";")[0]
                    //log.debug "cleaned: ${cleanedCookie}"
                    cleanCookie << cleanedCookie
                }
            }
            loginCookie = cleanCookie.join("; ")
            redirectUrl = response.headers["Location"].value
        }
    } catch (e) {
        log.error "Login cookie status: ${e}"
    }

    //log.debug "Got redirectURL ${authEndpoint}\n${redirectUrl}\nloginCookie:${loginCookie}"

    if (!redirectUrl || !loginCookie) {
        log.error "Cannot get Authendpoint or Cookie"
        return
    }

    def redirectHeaders = [
        "Cookie": loginCookie,
        "User-Agent": "null"
    ]

    //This step requires halting the redirect and grabbing the code from MyQ. Because we cannot do this with Groovy, we hand off the cookie and redirectURL to a cloud-hosted app.
    // Note that the challenge verifier is NOT passed over, so the cloud app will not be able to login or actually generate a token for your account
    log.trace "uri: ${authEndpoint + redirectUrl}, headers: ${redirectHeaders}"
    def code
    def scope
    try {
        httpPostJson([ uri: "http://brbeaird.herokuapp.com", path: "/getRedirectCode", headers: ["Content-Type": "application/json"], body: ["redirectUrl": authEndpoint + redirectUrl, "cookie": loginCookie]]) { response ->
            //log.trace "Redirect response: ${response.status}\n${response.data}"
            if (response.status == 200) {
                code = response.data.code
                scope = response.data.scope
                //log.debug "Got code ${code}, scope: ${scope}"
            } else {
                log.warn "Failed to get redirect code"
            }
        }
    } catch (e) {
        log.error "Code status: ${e}"
    }

    if (!code || !scope) {
        log.error "No code or scope returned"
        return
    }

    def tokenRequestBody = [
        "client_id": MYQ_API_CLIENT_ID,
        "client_secret": new String(MYQ_API_CLIENT_SECRET.decodeBase64()),
        "code": code,
        "code_verifier": code_verifier,
        "grant_type": MYQ_GRANT_TYPE,
        "redirect_uri": MYQ_API_REDIRECT_URI,
        "scope": scope
    ].collect { k,v -> "${java.net.URLEncoder.encode(k, "UTF-8")}=${java.net.URLEncoder.encode(v, "UTF-8")}" }.join("&")

    def tokenHeaders = [
        "Content-Type": "application/x-www-form-urlencoded",
        "User-Agent": "null"
    ]

    //log.trace "$tokenRequestBody\n$tokenHeaders"

    def refreshToken

    try {
    // "https://webhook.site/7ef2ea2d-441e-4f14-b4bd-b4ab4f458a49"
        httpPost([ uri: authEndpoint + tokenPath, headers: tokenHeaders, body: tokenRequestBody]) { response ->
            //log.debug "Token response ${response.status}"
            if (response.status == 200) {
                state.oauth.refreshToken = refreshToken
                state.oauth.lastRefresh = now()
                state.oauth.access_token = response.data.access_token
                state.oauth.expiration = now() + (response.data.expires_in * 1000)
                refreshToken = response.data.refresh_token
                log.info "Successfully generated new access/refresh tokens"
                return true
            } else {
                log.warn "Failed to get token: ${response.status}"
                return false
            }
        }
    } catch (e) {
        log.error "Token status: ${e}"
        return false
    }
}

def byte[] randomGenerator(int n) {
  return new Random().with {
    (1..n).collect { nextInt(256) }
  }
}

@Field final byte[] ENCODE_WEBSAFE = [
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
        'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',
        'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v',
        'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_',
        ]

private encodeBase64Url(byte[] input) {
    /**
    * Emit a new line every this many output tuples.  Corresponds to
    * a 76-character line length (the maximum allowable according to
    * <a href="http://www.ietf.org/rfc/rfc2045.txt">RFC 2045</a>).
    */
    final int LINE_GROUPS = 19

    int offset = 0
    int len = input.length
    boolean finish = true

    boolean do_padding = false
    boolean do_newline = false
    boolean do_cr = false
    byte[] alphabet = ENCODE_WEBSAFE

    byte[] tail = new byte[2]
    int tailLen = 0
    int count = do_newline ? LINE_GROUPS : -1

    // Compute the exact length of the array we will produce.
    int output_len = len / 3 * 4

    // Account for the tail of the data and the padding bytes, if any.
    if (do_padding) {
        if (len % 3 > 0) {
            output_len += 4
        }
    } else {
        switch (len % 3) {
            case 0: break
            case 1: output_len += 2
            break
            case 2: output_len += 3
            break
        }
    }

    // Account for the newlines, if any.
    if (do_newline && len > 0) {
        output_len += (((len-1) / (3 * LINE_GROUPS)) + 1) *
            (do_cr ? 2 : 1)
    }

    // Using local variables makes the encoder about 9% faster.
    byte[] output = new byte[output_len]
    int op = 0

    int p = offset
    len += offset
    int v = -1

    // First we need to concatenate the tail of the previous call
    // with any input bytes available now and see if we can empty
    // the tail.

    switch (tailLen) {
        case 0:
        // There was no tail.
        break

        case 1:
        if (p+2 <= len) {
            // A 1-byte tail with at least 2 bytes of
            // input available now.
            v = ((tail[0] & 0xff) << 16) |
                ((input[p++] & 0xff) << 8) |
                (input[p++] & 0xff)
            tailLen = 0
        }
        break

        case 2:
        if (p+1 <= len) {
            // A 2-byte tail with at least 1 byte of input.
            v = ((tail[0] & 0xff) << 16) |
                ((tail[1] & 0xff) << 8) |
                (input[p++] & 0xff)
            tailLen = 0
        }
        break
    }

    if (v != -1) {
        output[op++] = alphabet[(v >> 18) & 0x3f]
        output[op++] = alphabet[(v >> 12) & 0x3f]
        output[op++] = alphabet[(v >> 6) & 0x3f]
        output[op++] = alphabet[v & 0x3f]
        if (--count == 0) {
            if (do_cr) output[op++] = (byte)'\r'
            output[op++] = (byte)'\n'
            count = LINE_GROUPS
        }
    }

    // At this point either there is no tail, or there are fewer
    // than 3 bytes of input available.

    // The main loop, turning 3 input bytes into 4 output bytes on
    // each iteration.
    while (p+3 <= len) {
        v = ((input[p] & 0xff) << 16) |
            ((input[p+1] & 0xff) << 8) |
            (input[p+2] & 0xff)
        output[op] = alphabet[(v >> 18) & 0x3f]
        output[op+1] = alphabet[(v >> 12) & 0x3f]
        output[op+2] = alphabet[(v >> 6) & 0x3f]
        output[op+3] = alphabet[v & 0x3f]
        p += 3
        op += 4
        if (--count == 0) {
            if (do_cr) output[op++] = (byte)'\r'
            output[op++] = (byte)'\n'
            count = LINE_GROUPS
        }
    }

    if (finish) {
        // Finish up the tail of the input.  Note that we need to
        // consume any bytes in tail before any bytes
        // remaining in input there should be at most two bytes
        // total.

        if (p-tailLen == len-1) {
            int t = 0
            v = ((tailLen > 0 ? tail[t++] : input[p++]) & 0xff) << 4
            tailLen -= t
            output[op++] = alphabet[(v >> 6) & 0x3f]
            output[op++] = alphabet[v & 0x3f]
            if (do_padding) {
                output[op++] = (byte)'='
                output[op++] = (byte)'='
            }
            if (do_newline) {
                if (do_cr) output[op++] = (byte)'\r'
                output[op++] = (byte)'\n'
            }
        } else if (p-tailLen == len-2) {
            int t = 0
            v = (((tailLen > 1 ? tail[t++] : input[p++]) & 0xff) << 10) |
                (((tailLen > 0 ? tail[t++] : input[p++]) & 0xff) << 2)
            tailLen -= t
            output[op++] = alphabet[(v >> 12) & 0x3f]
            output[op++] = alphabet[(v >> 6) & 0x3f]
            output[op++] = alphabet[v & 0x3f]
            if (do_padding) {
                output[op++] = (byte)'='
            }
            if (do_newline) {
                if (do_cr) output[op++] = (byte)'\r'
                output[op++] = (byte)'\n'
            }
        } else if (do_newline && op > 0 && count != LINE_GROUPS) {
            if (do_cr) output[op++] = (byte)'\r'
            output[op++] = (byte)'\n'
        }

        if (tailLen != 0) {
            log.error "Taillen != 0: $tailLen"
        }
        if (p != len) {
            log.error "p != len: $p,$len"
        }
    } else {
        // Save the leftovers in tail to be consumed on the next
        // call to encodeInternal.

        if (p == len-1) {
            tail[tailLen++] = input[p]
        } else if (p == len-2) {
            tail[tailLen++] = input[p]
            tail[tailLen++] = input[p+1]
        }
    }

    if (op != output_len) {
        //log.error "op != output_len: $op,$output_len"

        // Strip the last 2 extra padding bytes
        byte[] tmp = new byte[output.length - 2]
        for (int i = 0; i < output.length - 2; i++) {
            tmp[i] = output[i]
        }

        return tmp
    } else {
        return output
    }
}

def byte[] base64EncodeUrlSafe(byte[] data) {
    byte[] encode = data.encodeBase64(true).toString() //data.collect { it as char }
    //log.trace "${encode.size()}:${encode}"
    for (int i = 0; i < encode.length; i++) {
        if (encode[i] == '+') {
            encode[i] = '-'
        } else if (encode[i] == '/') {
            encode[i] = '_'
        }
    }
    return encode
}

String generateCodeVerifier() {
    byte[] codeVerifier = randomGenerator(32)
    //log.trace "${codeVerifier}\n${encodeBase64Url(codeVerifier)}"
    return new String(encodeBase64Url(codeVerifier), "ISO_8859_1")
}

String generateCodeChallange(String codeVerifier) {
    //log.trace "${codeVerifier.size()},${codeVerifier}"
    byte[] bytes = codeVerifier.getBytes("US-ASCII")
    MessageDigest messageDigest = MessageDigest.getInstance("SHA-256")
    messageDigest.update(bytes, 0, bytes.length)
    byte[] digest = messageDigest.digest()
    //log.debug "${codeVerifier}\n${bytes}\n${digest}\n${digest.encodeBase64()}"
    return new String(encodeBase64Url(digest), "ISO_8859_1")
}
