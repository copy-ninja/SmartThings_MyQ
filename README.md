
# SmartThings MyQ

##Overview
This SmartApp integrates Chamberlain/LiftMaster MyQ doors into SmartThings. It creates a garage door device in your list of Things and allows you to control the door...

..By pushing the device in the SmartThings mobile app
..Automatically by your presence (coming or going) in a Routine or other SmartThings rules app
..Via switch tiles in a SmartTiles dashboard
..By asking Alexa or Google Home to turn the door on (open) or off (close)

Credit to copy-ninja, whose version I branched off to create this app to integrate without the need to poll MyQ for the door's status. If you use the optional Tilt/Contact sensor, you can still get status updates on the door. Otherwise, normal control functionality with be the same with or without a sensor. Previous versions of this app relied on polling MyQ for status updates; however, MyQ/SmartThings have restricted the ability to poll the MyQ servers, although sending open/close commands still works as normal.

More details can be found on the wiki: https://github.com/brbeaird/SmartThings_MyQ/wiki

SmartThings thread here: <a href="https://community.smartthings.com/t/release-myq-lite-for-liftmaster-chamberlain/49150">https://community.smartthings.com/t/beta-myq-lite-for-liftmaster-chamberlain/49150</a>

##Optional Tilt/Contact Sensor
This app has the option of interfacing with a tilt sensor on the garage door. When present, a sensor will allow the door status to be known and displayed on the device. This also allows for the "switch" capability on the device, which can be added to other apps such as SmartTiles dashboards. <a href="https://www.amazon.com/gp/product/B00HGVJRX2/ref=as_li_tl?ie=UTF8&tag=brbeaird-20&camp=1789&creative=9325&linkCode=as2&creativeASIN=B00HGVJRX2&linkId=b95bd197703395387d5d0bfe06c4866f">Here's an example of one you can purchase from Amazon.</a>

##Optional MultiSensor Support (Accelerometer Only)
This version now supports the additional use of an Acceleromter for each door, as in a Samsung/SmartThings MultiSensor. If configured, the accelerometer will be used to more accurately track states of opening, closing, and waiting (the alarm before closing actually begins). Generally, this will return the visual status updates from the original version, but without making ANY status requests from the MyQ servers.

## Installation Instructions:

### Manually:
1. Log in to the <a href="https://graph.api.smartthings.com/ide/">SmartThings IDE</a>. If you don't have a login yet, create one.
2. Load contents of <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/smartapps/brbeaird/myq-lite.src/myq-lite.groovy">MyQ Lite</a> in SmartApps section. From IDE, navigate to <a href="https://graph.api.smartthings.com/ide/app/create#from-code">My SmartApps > + New SmartApp > From Code</a>. Click Save. Click Publish > "For Me"
3. Load contents of <a href="https://raw.githubusercontent.com/brbeaird/SmartThings_MyQ/master/devicetypes/brbeaird/myq-garage-door-opener.src/myq-garage-door-opener.groovy">MyQ Garage Door Opener</a> in SmartDevices section. From IDE, navigate to <a href="https://graph.api.smartthings.com/ide/device/create#from-code">My Device Type > + New SmartDevice > From Code</a>.  Click Save. Click Publish "For Me" for both devices
4. In your mobile app, tap the "+", go to "My Apps", furnish your log in details and pick your gateway brand, and a list of devices will be available for you to pick. After you choose your doors, you'll then select the physical tilt sensor to go with each door.

### SmartThings IDE GitHub Integration:

If you have not set up the GitHub integration yet or do not know about it, take a look at the SmartThings documentation [here](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html). Note that if you do not have a GitHub account or are not familiar with GitHub, the manual method of installation is recommended.

1. Add a new repository with user `brbeaird`, repository `SmartThings_MyQ`, and branch `master`. This can be done in either the "My Device Handlers" or "My SmartApps" sections
2. Go to "My Device Handlers". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the device types in the "New (only in GitHub)" section. Check both boxes next to them. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
3. Go to "My SmartApps". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the SmartApp in the "New (only in GitHub)" section. Check both boxes next to them. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
4. In your mobile app, tap the "+", go to "My Apps", furnish your log in details and pick your gateway brand, and a list of devices will be available for you to pick.

In the future, should you wish to update, simply repeat steps 2 and 3. The only difference is you will see the device types/SmartApp show up in the "Obsolete (updated in GitHub)" column instead.


### Donate:

If you love this app, feel free to donate.

[![PayPal - The safer, easier way to give online!](https://www.paypalobjects.com/en_US/i/btn/btn_donate_LG.gif "Donate")](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6QH4Y5KCESYPY)
