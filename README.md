
# SmartThings MyQ

##Overview
This version was branched off from copy-ninja's original version. While it works largely the same, the biggest difference is that it relies on a separately installed tilt sensor on each door. This allows the app to get instant status updates without needing poll MyQ at all. This is necessary because MyQ/SmartThings have restricted the ability to poll the MyQ servers, although sending open/close commands still works as normal.

More details can be found on the wiki: https://github.com/brbeaird/SmartThings_MyQ/wiki

SmartThings thread here: <a href="https://community.smartthings.com/t/beta-myq-lite-for-liftmaster-chamberlain/49150">https://community.smartthings.com/t/beta-myq-lite-for-liftmaster-chamberlain/49150</a>

##Tilt Sensor Requirement
Please note that this version does require that you acquire a separate physical tilt sensor for each of your MyQ doors.  <a href="http://www.amazon.com/Ecolink-Z-Wave-Wireless-Tilt-Sensor/dp/B00HGVJRX2">Here's an example of one you can purchase from Amazon.</a> While this requirement is more costly and not ideal, the status updates tend to be much quicker and more reliable than using MyQ server polling.

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




