# SmartThings MyQ

## Installation Instructions:

### Manually:
1. Log in to the <a href="https://graph.api.smartthings.com/ide/">SmartThings IDE</a>. If you don't have a login yet, create one.
2. Load contents of <a href="https://raw.githubusercontent.com/copy-ninja/SmartThings_MyQ/master/smartapps/copy-ninja/myq-connect.src/myq-connect.groovy">MyQ (Connect)</a> in SmartApps section. From IDE, navigate to <a href="https://graph.api.smartthings.com/ide/app/create#from-code">My SmartApps > + New SmartApp > From Code</a>. Click Save. Click Publish > "For Me"
3. Load contents of <a href="https://raw.githubusercontent.com/copy-ninja/SmartThings_MyQ/master/devicetypes/copy-ninja/myq-garage-door-opener.src/myq-garage-door-opener.groovy">MyQ Garage Door Opener</a> and <a href="https://raw.githubusercontent.com/copy-ninja/SmartThings_MyQ/master/devicetypes/copy-ninja/myq-light-controller.src/myq-light-controller.groovy">MyQ Light Controller</a> in SmartDevices section. From IDE, navigate to <a href="https://graph.api.smartthings.com/ide/device/create#from-code">My Device Type > + New SmartDevice > From Code</a>.  Click Save. Click Publish "For Me" for both devices
4. In your mobile app, tap the "+", go to "My Apps", furnish your log in details and pick your gateway brand, and a list of devices will be available for you to pick

### SmartThings IDE GitHub Integration:

If you have not set up the GitHub integration yet or do not know about it, take a look at the SmartThings documentation [here](http://docs.smartthings.com/en/latest/tools-and-ide/github-integration.html). Note that if you do not have a GitHub account or are not familiar with GitHub, the manual method of installation is recommended.

1. Add a new repository with user `copy-nina`, repository `SmartThings_MyQ`, and branch `master`. This can be done in either the "My Device Handlers" or "My SmartApps" sections
2. Go to "My Device Handlers". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the device types in the "New (only in GitHub)" section. Check both boxes next to them. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
3. Go to "My SmartApps". Click "Update from Repo". Select the "SmartThings_MyQ" repository. You should see the SmartApp in the "New (only in GitHub)" section. Check both boxes next to them. Check the "Publish" checkbox in the bottom right hand corner. Click "Execute Update".
4. In your mobile app, tap the "+", go to "My Apps", furnish your log in details and pick your gateway brand, and a list of devices will be available for you to pick.

In the future, should you wish to update, simply repeat steps 2 and 3. The only difference is you will see the device types/SmartApp show up in the "Obsolete (updated in GitHub)" column instead.

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donate_SM.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=jasonmok83%40gmail%2ecom&lc=US&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donate_SM%2egif%3aNonHosted)
