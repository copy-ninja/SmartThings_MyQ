
# SmartThings MyQ

### Overview
This SmartApp integrates Chamberlain/LiftMaster MyQ doors and light controllers into SmartThings. It creates a garage door device and/or a light device in your list of Things and allows you to control the device...

* By pushing the device in the SmartThings mobile app
* Automatically by your presence (coming or going) in a Routine or other SmartThings rules app
* Via switch tiles in a SmartTiles dashboard
* By asking Alexa or Google Home to turn the device on (open) or off (close)


### Device and SmartTiles
![Door device](http://i.imgur.com/zBXS6nQm.png "Door device")  ![Routine](http://i.imgur.com/fmDa1t6m.png "Routine") 


![With SmartTiles](http://i.imgur.com/WhbN2H9m.png "With SmartTiles")    


### Setup
![Login to MyQ](http://i.imgur.com/PLEbmsdm.png "Login to MyQ")
![Select Door](http://i.imgur.com/XUDFLucm.png "Door Select")


### Sensor Selection
![Sensor Selection](http://i.imgur.com/aKf14HHm.png "Sensor selection")
![Push Buttons](http://i.imgur.com/SIkag7Cm.png "Push Buttons")




Credit to copy-ninja, whose version I branched off to create this app to integrate without the need to poll MyQ for the device status. It is strongly recommend you use a Tilt/Contact sensor on the door to keep an accurate status in SmartThings. That said, you can maintain most functionality without one (see no-sensor special notes below). For light controllers, there's no way for SmartThings to know the status, but as long as you only control the light from SmartThings, the status should stay in sync. 

Previous versions of this app relied on polling MyQ for status updates; however, MyQ/SmartThings have restricted the ability to poll the MyQ servers, although sending open/close/on/off commands via API still works as normal.

SmartThings thread here: <a href="https://community.smartthings.com/t/release-myq-lite-for-liftmaster-chamberlain/49150">https://community.smartthings.com/t/beta-myq-lite-for-liftmaster-chamberlain/49150</a>

## Optional Tilt/Contact Sensor
This app has the (strongly recommended) option of interfacing with a tilt sensor on the garage door. When present, a sensor will allow the door status to be known and displayed on the device. This also allows for the "switch" capability on the device, which is necessary for routines, SmartTiles dashboard, and Alexa use. <a href="https://www.amazon.com/gp/product/B00HGVJRX2/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B00HGVJRX2&linkCode=as2&tag=brbeaird0e-20&linkId=05e7b850708aa7cda815de18103d4805">Here's an example of one you can purchase from Amazon.</a>

## Optional MultiSensor Support (Accelerometer Only)
This version now supports the additional use of an Acceleromter for each door, as in a Samsung/SmartThings MultiSensor. If configured, the accelerometer will be used to more accurately track states of opening, closing, and waiting (the alarm before closing actually begins). Generally, this will return the visual status updates from the original version, still without making any status requests from the MyQ servers.

### Special notes when using the no-sensor door version
We have discovered that the no-sensor version of the garage door does not work well with SmartThings routines. This is because routines only take action if the device's status is in the correct position. For example, the no-sensor version of the door has no way to accurately track whether the door is open or closed, so it defaults to "unknown." In this state, a routine will never close the door because it believes the door to always be in a "closed" state.

You have two options to work around this limitation:
1.) Use the mometary push-button devices that will generate if you choose that option during setup. Then, when setting up a routine, have the routine "turn on" the corresponding push button to either open or close the door.
2.) Use the CoRE SmartApp and select the option to disable status optimization. This tells CoRE to always send the desired command regardless of whether SmartThings thinks the door is already opened/closed.

### Garage Door Usage with Alexa (Without Sensors):
For Alexa to respond to commands to open/close the door, make sure you choose the option during setup to create the Pushbutton switches. This will create an "Opener" and "Closer" switch. Once setup is done and you see those switches in your Things, go back into Alexa and run the discovery process. Those switches should show up in the Alexa app and should respond to **"Alexa, turn ON [garage door name] Opener"** and **"Alexa, turn ON [garage door name] Closer."** This method is somewhat awkward as you're saying "turn on" for both types, but it gets the job done for now.

### Garage Door Usage with Alexa (With Sensors):
If your door has sensors, Alexa will respond simply to **"Alexa, turn ON [garage door name]"** or **"Alexa, turn OFF [garage door name]"** once you've completed the setup and done the discovery process in the Alexa app. This is because the MyQ door device has a on/off switch capability that can be used and kept in sync since the sensor updates the door's status. It's not necessary to use the Pushbutton switch in this case.

### Special notes when using Light Controllers:
Since we have no way to keep an exact on/off status on the light, it is strongly recommended that you ONLY control the light via SmartThings (not through the MyQ app or manually at the physical device itself). As long as SmartThings is the only thing making changes, it will essentially always have the correct status. If for some reason the status does get out of sync, you may just need to turn it off and back on in SmartThings to sync it back up.

Alexa should be able to control the light device just like any other switch in your environment.

## Installation Instructions:

###Code Needed:
There are 5 code files available for the installations of this app - 1 SmartApp and 4 Device Handlers. At minimum, you need the main SmartApp and at least one of the device handlers. Note that you only need to install the device handlers you'll plan on using (ex: you can leave off the light controller if you don't have any lights).

| Code Type        | Name           | Location  |
| ------------- |-------------| -----|
| SmartApp      | MyQ Lite | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/smartapps/brbeaird/myq-lite.src/myq-lite.groovy">Link</a> |
| Device Handler | MyQ Garage Door Opener-NoSensor | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener-nosensor.src/myq-garage-door-opener-nosensor.groovy">Link</a> |
| Device Handler | MyQ Garage Door Opener | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener.src/myq-garage-door-opener.groovy">Link</a> |
| Device Handler | Momentary Button Tile | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/smartthings/momentary-button-tile.src/momentary-button-tile.groovy">Link</a> |
| Device Handler | Light Controller | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-light-controller.src/myq-light-controller.groovy">Link</a> |


### Manually:
1. Log in to the <a href="https://graph.api.smartthings.com/ide/">SmartThings IDE</a>. If you don't have a login yet, create one.
2. The first step is to create device handlers for both door types.
3. Click on **My Device Handlers** -> **Create new Device Handler** -> **From Code**.
4. Copy contents of <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener.src/myq-garage-door-opener.groovy">Door Opener (original sensor version) </a> and paste into text area. in SmartApps section. Click **Create**. Click **Publish** > **For Me** (you can ignore this step if you don't have a door sensor)
5. Repeat the previous step for this door type code: <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener-nosensor.src/myq-garage-door-opener-nosensor.groovy">Door Opener (no sensor version)</a> (you can ignore this step if using a sensor)
6. Repeat the previous step for the Momentary Tile device type code: <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/smartthings/momentary-button-tile.src/momentary-button-tile.groovy">Momentary Button Tile</a>
6. Repeat the previous step for the Light Controller device type code: <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-light-controller.src/myq-light-controller.groovy">Light Controller</a>
6. Now we create the SmartApp code. Click **My SmartApps** -> **New Smartapp** -> **From Code**.
7. Copy contents of <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/smartapps/brbeaird/myq-lite.src/myq-lite.groovy">SmartApp</a> and paste into text area. in SmartApps section. Click **Create**. Click **Publish** > **For Me**
8. In your SmartThings mobile app, tap **Automation** -> **SmartApps** -> **Add a SmartApp**. Scroll down and tap **My Apps**. Tap **MyQ Lite**. 
9. Enter in your login details and pick your gateway brand. If login is successful, you'll see a list of doors available for you to pick. After you choose your doors, you can select optional sensors to be tied to those doors. 

### SmartThings IDE GitHub Integration:

If you have not set up the GitHub integration yet or do not know about it, take a look at the SmartThings documentation [here](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html). Note that if you do not have a GitHub account or are not familiar with GitHub, the manual method of installation is recommended.

1. If you haven't already, click on enable GitHub button (upper right). Add a new repository with user `brbeaird`, repository `SmartThings_MyQ`, and branch `master`. This can be done in either the "My Device Handlers" or "My SmartApps" sections
2. Go to "My Device Handlers". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the device types in the "New (only in GitHub)" section. Check both boxes next to them. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
3. Go to "My SmartApps". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the SmartApp in the "New (only in GitHub)" section. Check both boxes next to them. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
4. In your mobile app, tap the "+", go to "My Apps", furnish your log in details and pick your gateway brand, and a list of devices will be available for you to pick.

In the future, should you wish to update, simply repeat steps 2 and 3. The only difference is you will see the device types/SmartApp show up in the "Obsolete (updated in GitHub)" column instead.


### Donate:

If you love this app, feel free to donate.

[![PayPal - The safer, easier way to give online!](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif "Donate")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6QH4Y5KCESYPY)
