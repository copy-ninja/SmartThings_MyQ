# Changelog

## 4.1.2 (2021-10-25)

### Added
 - Added ability to specify redirect auth server

 ### Changed
 - Changed default redirect auth URL to https

## 4.1.1 (2021-09-30)

### Fixed
 - Fixed logic where refresh was happening too frequently

### Changed
 - Added clearer error messaging (MyQ command errors are now passed straight through)

## 4.1.0 (2021-09-24)

### Added
 - Added full oauth support within the SmartApp

## 4.0.1 (2021-09-12)

### Changed
 - Added more backwards-compatibility for AccountId

## 4.0.0 (2021-09-09)

### Changed
 - Update to support MyQ API v6

## 3.1.7 (2021-08-02)

### Fixed
 - Fixed issue where Last Activity was not actually getting saved

## 3.1.6 (2021-07-29)

### Fixed
 - Limit lastest activity search to contact sensor events
 - Clean up door refresh logic

 ## 3.1.5 (2021-01-05)

### Fixed
 - Lower device URL casing

 ## 3.1.4 (2020-08-09)

### Changed
 - Change momentary to virtual switch to support new app

## 3.1.3 (2020-07-30)

### Fixed
 - Fix username/password casing

## 3.1.2 (2020-05-25)

### Changed
 - Update refresh logic to use door status instead of contact



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
