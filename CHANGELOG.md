# Changelog

## 3.1.1 (2019-11-07)

### Added
 - Battery capability is now supported on sensor door device type
 - SmartApp config page now links to this changelog

### Fixed
 - Fixed issue where the app would incorrectly show updates available if the version server was unreachable

## 3.1.0 (2019-10-18)

### Changed

- Changed API calls to use the MyQ API V5 endpoints

### Notes
 - Important! After updating your code, be sure to open the SmartApp, tap "modify devices", and go through the steps selecting your devices. This is necessary to make sure the app picks up the new ID's from MyQ.
 - This update will try to keep your existing devices and simply update them based on name matching. However, if you end up with duplicate devices, you may need to delete the old ones manually or uninstall/reinstall.

### Known Issues
 - The Momentary push button for open/close will not automatically be updated. If you already had buttons in use, this update will create new ones, and you'll need to discard the old ones.

## 3.0.0 (2019-10-09)

### Added
 - Optional push notifications when version updates are available
 - Optional push notifications when door or lamp module commands fail

### Changed
 - Improve SmartApp screens and setup flow
 - Cap on number of doors has been removed. Behind the scenes, this also means sensor variables are fully
dynamic instead of hard-coded.

### Removed
 - Removed acceleration functionality (for simplification - may re-add later)
 - Removed extra refreshes (hopefully these aren't actually needed anymore)
