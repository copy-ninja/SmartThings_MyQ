

## Notice: This SmartApp (and all others using Groovy) is obsolete. A new integration using the Edge driver platform is available here: https://github.com/brbeaird/SmartThings-MyQ-Edge.

# SmartThings MyQ Lite SmartApp (Obsolete)

### Current Notes
* Please note this SmartApp cannot read the status of the MyQ door using the MyQ tilt sensor. Getting the door status will require a separate SmartThings-compatible sensor. It's an unfortunate hassle but is the only way we can do this without polling MyQ for status (which they will not allow done from the SmartThings cloud).
* This SmartApp generally works with the new SmartThings app but still has some bugs when viewing the door tiles (a fix for this is in progress). It is still fully supported in the Classic mobile app.
* If you get an error saying "No supported devices found," the most likely cause is you're running an older version of the SmartApp. Double check to see you're using the latest code. If you're still having trouble, there's a small chance your IDE info is not sync'd properly across all SmartThings cloud shards. One way to tell this is by checking the Hubs link and noticing your hub missing. Another  way to check that is to reset all cookies and log in again (or try from a different computer) to see if it logs you into a different shard at that point, which will let you verify if things are out of sync. If there's a problem with that, SmartThings support can help.

## Installation Instructions:



### SmartThings Community Installer (Strongly Recommended install/update method)
By far, the easiest way to install this Smartapp (and many other popular ones) and keep it updated is to use the SmartThings Community Installer. Instructions for that can be found <a href="http://thingsthataresmart.wiki/index.php?title=Community_Installer_(Free_Marketplace)">here</a>. If you go that route, you can find MyQ on the list of apps, tap to install, and ignore the other install information below. The same goes for installing updates in the future.

### Code Needed:
There are 6 code files available for the installations of this app - 1 SmartApp and 5 Device Handlers. At minimum, you need the main SmartApp and at least one of the device handlers. Note that you only need to install the device handlers you'll plan on using (ex: you can leave off the light controller if you don't have any lights).

| Code Type        | Name           | Location  | Notes |
| ------------- |-------------| -----|-----|
| SmartApp      | MyQ Lite | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/smartapps/brbeaird/myq-lite.src/myq-lite.groovy">Link</a> |Required|
| Device Handler | MyQ Garage Door Opener | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener.src/myq-garage-door-opener.groovy">Link</a> |Needed if using door sensors|
| Device Handler | MyQ Garage Door Opener-NoSensor | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener-nosensor.src/myq-garage-door-opener-nosensor.groovy">Link</a> |Needed if NOT using door sensors|
| Device Handler | MyQ Action Switch | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-action-switch.src/myq-action-switch.groovy">Link</a> |Helpful for no-sensor installs to add buttons in automations/ActionTiles/Alexa|
| Device Handler | Light Controller | <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-light-controller.src/myq-light-controller.groovy">Link</a> |Only needed if using a plug-in MyQ Lamp Controller|


### Manual code copy/pasting:
1. Log in to the <a href="https://account.smartthings.com">SmartThings IDE</a>. If you don't have a login yet, create one.
2. The first step is to create device handlers for both door types.
3. Click on **My Device Handlers** -> **Create new Device Handler** -> **From Code**.
4. Copy contents of <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener.src/myq-garage-door-opener.groovy">Door Opener (original sensor version) </a> and paste into text area. in SmartApps section. Click **Create**. Click **Publish** > **For Me** (you can ignore this step if you don't have a door sensor)
5. Repeat the previous step for this door type code: <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener-nosensor.src/myq-garage-door-opener-nosensor.groovy">Door Opener (no sensor version)</a> (you can ignore this step if using a sensor)
6. Repeat the previous step for the Virtual Switch Tile device type code: <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/virtual-switch.src/virtual-switch.groovy">Virtual Switch Tile</a>
6. Repeat the previous step for the Light Controller device type code: <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-light-controller.src/myq-light-controller.groovy">Light Controller</a>
6. Now we create the SmartApp code. Click **My SmartApps** -> **New Smartapp** -> **From Code**.
7. Copy contents of <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/smartapps/brbeaird/myq-lite.src/myq-lite.groovy">SmartApp</a> and paste into text area. in SmartApps section. Click **Create**. Click **Publish** > **For Me**
8. In your SmartThings mobile app, tap **Automation** -> **SmartApps** -> **Add a SmartApp**. Scroll down and tap **My Apps**. Tap **MyQ Lite**.
9. Enter in your login details and pick your gateway brand. If login is successful, you'll see a list of doors available for you to pick. After you choose your doors, you can select optional sensors to be tied to those doors.

### SmartThings IDE GitHub Integration:

If you have not set up the GitHub integration yet or do not know about it, take a look at the SmartThings documentation [here](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html). Note that if you do not have a GitHub account or are not familiar with GitHub, the manual method of installation is recommended.

1. If you haven't already, click on enable GitHub button (upper right). Add a new repository with user `brbeaird`, repository `SmartThings_MyQ`, and branch `master`. This can be done in either the "My Device Handlers" or "My SmartApps" sections
2. Go to "My Device Handlers". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the device types in the "New (only in GitHub)" section. Check both boxes next to them **(important: only check boxes for .groovy files, ignore all other file types)**. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
3. Go to "My SmartApps". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the SmartApp in the "New (only in GitHub)" section. Check both boxes next to them **(important: only check boxes for .groovy files, ignore all other file types)**. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
4. In your mobile app, tap the "+", go to "My Apps", furnish your log in details and pick your gateway brand, and a list of devices will be available for you to pick.

In the future, should you wish to update, simply repeat steps 2 and 3. The only difference is you will see the device types/SmartApp show up in the "Obsolete (updated in GitHub)" column instead.

### Authentication
First of all, special thanks to hjdhjd and the work at [homebridge-myq](https://github.com/hjdhjd/homebridge-myq) for figuring all of this out and documenting it so well.
The newest version of the MyQ API uses an OAuth flow. The SmartApp can handle most of this on its own, but there is one step that requires halting a web redirect to grab a code out of the URL. This is something the SmartThings groovy implemlentation cannot do. As a workaround, I now have the SmartApp make a callout to a 3rd-party endpoint I am hosting (http://brbeaird.herokuapp.com/getRedirectCode). The SmartApp passes in a URL and cookie from an auth'd MyQ session, and in return, the endpoint passes back a code. The SmartApp then uses that code to generate the OAuth access code and refresh token. It is important to note that because no part of your email, password, or the SmartApp-generated PKCE challenge verifier is passed over, the endpoint does not have the ability to login as you or generate a token itself. Also note that the endpoint is only called when a refresh token is not available, which is typically only upon initial install. Otherwise, callouts should be very rare, and the SmartApp will be able to cycle through refresh tokens on its own.

If you would like to host this auth redirect server yourself, you can find the code at https://github.com/brbeaird/MyQRedirectCode. You can then specify your server's URL in the "Advanced (optional)" section on the Username/password input page of the SmartApp setup.

Still, if you would prefer to avoid the need to make this callout, I have left in the option to generate a refresh token on your own and provide it to the SmartApp. I also included some javascript code and pre-compiled binaries to do that. Instructions are below.

### Manual Token Auth (Optional)
This involves logging into MyQ to get an access token. You can then enter that token into the SmartApp auth section.
1. Download the [myQTokenGenerator](/myQTokenGenerator) file applicable to your operating system
2. On Linux or MacOS, you will need run the appropriate chmod commands and enable it to be run. For MacOS:
   1. Make sure your system allows running these files: System preferences > Security & Privacy > give permission to run
   2. From the terminal, navigate to the directory of the utility and run `chmod u+x myqtokengenerator-macos`
   3. Run `./myqtokengenerator-macos`
4. On Windows, double-click and run, granting permissions as needed.
5. At the prompt, enter your MyQ username and password. If successful, the utility will output an access token.
   ![SmartApp List](/icons/TokenGenerator.png "Token Generator")
6. Back in the SmartApp setup, choose the "Manual Token" Login method, then paste in your token. You might find it easier to do this via web browser of the computer you used to generate the token (https://my.smartthings.com).


### Overview
This SmartApp integrates Chamberlain/LiftMaster MyQ doors and plug-in lamp module controllers into SmartThings. It creates a garage door device and/or a light device in your list of Things and allows you to control the device...

* By tapping the device in the SmartThings mobile app
* Automatically by your presence (coming or going) in an automation or other SmartThings rules app
* Via tiles in an ActionTiles dashboard
* By asking Alexa or Google Home to turn the device on (open) or off (close)


### Device and ActionTiles
![Door device](/icons/mobileAppDevice.jpg "Door device")
![With ActionTiles](https://i.imgur.com/8BSYtMI.png "With ActionTiles")


### Setup and Sensor Selection
![Select Door](/icons/mobileAppSelection.jpg "Door Select")
![Sensor Selection](/icons/mobileAppSensors.jpg "Sensor selection")


This SmartApp works best when you have a Tilt/Contact sensor on the door to keep an accurate status in SmartThings. **Unfortunately, the MyQ included tilt sensor will not work as SmartThings cannot communicate with it**. That said, you can maintain most functionality without a sensor at all (see no-sensor special notes below).

This SmartApp can control MyQ lamp modules (not the actual lights in the garage door openers) For the lamp controllers, there's no way for SmartThings to know the status, but as long as you only control the light from SmartThings, the status should stay in sync.

Previous versions of this app (known as simply SmartThings_MyQ) relied on polling MyQ for status updates; however, MyQ/SmartThings have restricted the ability to poll the MyQ servers, although sending open/close/on/off commands via API still works as normal.

SmartThings thread here: <a href="https://community.smartthings.com/t/release-myq-lite-for-liftmaster-chamberlain/49150">https://community.smartthings.com/t/beta-myq-lite-for-liftmaster-chamberlain/49150</a>

Credit to copy-ninja, whose version I branched off to create this app to integrate without the need to poll MyQ for the device status.

### Optional Tilt/Contact Sensor
This app has the (strongly recommended) option of interfacing with a tilt sensor on the garage door. When present, a sensor will allow the door status to be known and displayed on the device. This also allows for the "switch" capability on the device, which is necessary for automations, ActionTiles dashboard, and Alexa use. <a href="https://www.amazon.com/gp/product/B00HGVJRX2/ref=as_li_tl?ie=UTF8&camp=1789&creative=9325&creativeASIN=B00HGVJRX2&linkCode=as2&tag=brbeaird0e-20&linkId=05e7b850708aa7cda815de18103d4805">Here's an example of one you can purchase from Amazon.</a>


### Using MyQ Premium with IFTTT and Virtual Sensors
If you have a subscription to MyQ premium, you can use IFTTT and virtual sensors to use the full "sensor version" device handler that knows the door status. You can find a good write-up of that here:
https://community.smartthings.com/t/how-to-smartthings-myq-smartapp-using-myq-sensors-with-ifttt/149601

### Special notes when using automations with the no-sensor door version
We have discovered that the no-sensor version of the garage door does not work well with SmartThings automations. This is because automations only take action if the device's status is in the correct position. For example, the no-sensor version of the door has no way to accurately track whether the door is open or closed, so it defaults to "unknown." In this state, a automations will never close the door because it believes the door to always be in a "closed" state.

You have two options to work around this limitation:
1.) Use the MyQ Action Switch devices that will generate if you choose that option during setup. Then, when setting up an automation, have the automation "turn on" the corresponding push button to either open or close the door.
2.) Use the CoRE SmartApp and select the option to disable status optimization. This tells CoRE to always send the desired command regardless of whether SmartThings thinks the door is already opened/closed.


### Garage Door Usage with Alexa/Google Home (Without a door sensor):
For Alexa to respond to commands to open/close the door, make sure you choose the option during setup to create the Pushbutton switches. This will create an "Opener" and "Closer" switch. Once setup is done and you see those switches in your Things, go back into Alexa and run the discovery process. Those switches should show up in the Alexa app and should respond to **"Alexa, turn ON [garage door name] Opener"** and **"Alexa, turn ON [garage door name] Closer."**

If you want a less-awkward way to open the door, you can set up an Alexa routine or a Google Home shortcut that translates something like "Alexa, open the garage" to the full "Alexa, turn on..." phrase from above.

### Garage Door Usage with Alexa/Google Home (With a door sensor):
If your door has a sensor, Alexa will respond simply to **"Alexa, turn ON [garage door name]"** or **"Alexa, turn OFF [garage door name]"** once you've completed the setup and done the discovery process in the Alexa app. This is because the MyQ door device has a on/off switch capability that can be used and kept in sync since the sensor updates the door's status. It's not necessary to use the Pushbutton switch in this case.

If you want a less-awkward way to open the door, you can set up an Alexa routine or a Google Home shortcut that translates something like "Alexa, open the garage" to the full "Alexa, turn on..." phrase from above.

### Special notes when using Light Controllers:
Since we have no way to keep an exact on/off status on the light, it is strongly recommended that you ONLY control the light via SmartThings (not through the MyQ app or manually at the physical device itself). As long as SmartThings is the only thing making changes, it will essentially always have the correct status. If for some reason the status does get out of sync, you may just need to turn it off and back on in SmartThings to sync it back up.

Alexa should be able to control the light device just like any other switch in your environment.


### Donate/Sponsor:

If you love this app, feel free to donate or check out the GitHub Sponsor program.

| Platform        | Wallet/Link | QR Code  |
|------------- |-------------|------|
| GitHub Sponsorship      | https://github.com/sponsors/brbeaird |  |
| Bitcoin      | 1gLEpa5VUpYx77p4nfqHkWrpZK4opFrgV | <img src="https://i.imgur.com/ubrZjaz.png" />
| Paypal      | [![PayPal - The safer, easier way to give online!](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif "Donate")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6QH4Y5KCESYPY) |
