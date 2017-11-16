> Cordova plugin for honeywell barcode reader using [AidcSDK](http://download.salamandre.tm.fr/api/com/honeywell/aidc/package-summary.html)

## Supported Platforms

- Android

## Installation

### With npm
```bash
npm install --save @neo9/cordova-honeywell-scanner
```

### With cordova
```bash
cordova plugin add @neo9/cordova-honeywell-scanner
```

### With ionic
```
ionic cordova plugin add @neo9/cordova-honeywell-scanner
```

## Usage

This module instantiate the aidc manager during the init phase of the plugin

### listConnectedBarcodeDevices

Return the connected barcode devices

```js
// Return an array of devices ex: (name: 'Internal Scanner', id: 'dcs.scanner.imager')
window.cordova.plugins.honeywell.listConnectedBarcodeDevices((result) => {
  result.devices.forEach((device) => console.log(`name: ${device.name}, id: ${device.id}`));
}, (err) => console.error(err));
```

### selectDevice

Select a specific barcode reader device (`deviceName`: string returned by the listConnectedBarcodeDevices `id` key (optional)).</br>
If no deviceName is specified the plugin will select the device returned by the createBarcodeReader instance

```js
// Select default device
window.cordova.plugins.honeywell.selectDevice(null, () => {
  console.info('default codebar device connected');
}, (err) => console.info(err));

// Select specific device returned by the id key of listConnectedBarcodeDevices function
window.cordova.plugins.honeywell.selectDevice('dcs.scanner.imager', () => {
  console.info('dcs.scanner.imager codebar device connected');
}, (err) => console.info(err));
```

### claim

Claim the access of the device</br>
For optimization, please call this function during apps resume event

```js
window.cordova.plugins.honeywell.claim(() => console.info('claim success'), (err) => console.info(err));
```

### release

Release the current connected device if exist.</br>
For optimization, please call this function during apps pause event


```js
window.cordova.plugins.honeywell.release(() => console.info('release success'), (err) => console.info(err));
```

### register

Bind the device callback event. Each call of the register function will remove the old callback

```js
window.cordova.plugins.honeywell.register((event) => {
  if (event.success) console.info(`new data from barcode device : ${event.data}`);
  else console.info('scan triggered but no data');
}, (err) => console.info(err));
```

### unregister

Unbind the current callback function returned by register function

```js
window.cordova.plugins.honeywell.unregister(() => console.info('unregister done'));
```

### enableTrigger

Enable the trigger button. The trigger button is enable by default

```js
window.cordova.plugins.honeywell.enableTrigger(() => console.info('trigger enabled'));
```

### disableTrigger

Disable the trigger button.

```js
window.cordova.plugins.honeywell.disableTrigger(() => console.info('trigger disabled'));
```
