var cordova = require('cordova');
var exec = require('cordova/exec');

var Aidc = function () {
  this.claim = function (success_cb, error_cb) {
    exec(success_cb, error_cb, "HoneywellAidc", "claim", []);
  };

  this.release = function (success_cb, error_cb) {
    exec(success_cb, error_cb, "HoneywellAidc", "release", []);
  };

  this.selectDevice = function (deviceName, success_cb, error_cb) {
    exec(success_cb, error_cb, "HoneywellAidc", "selectDevice", [deviceName]);
  };

  this.register = function (callback) {
    exec(callback, null, "HoneywellAidc", "register", []);
  };

  this.unregister = function () {
    exec(null, null, "HoneywellAidc", "unregister", []);
  };

  this.enableTrigger = function (success_cb, error_cb) {
    exec(success_cb, error_cb, "HoneywellAidc", "enableTrigger", []);
  };

  this.disableTrigger = function (success_cb, error_cb) {
    exec(success_cb, error_cb, "HoneywellAidc", "disableTrigger", []);
  };

  this.listConnectedBarcodeDevices = function (success_cb, error_cb) {
    exec(success_cb, error_cb, "HoneywellAidc", "listConnectedBarcodeDevices", []);
  }
};

Aidc.prototype._success = function (barcode) {
  console.info('Aidc._success');

  cordova.fireWindowEvent('barcode', { barcode: barcode });
};

Aidc.prototype._error = function () {
  console.info('Aidc._error');
};

console.info("Loaded Honeywell Aidc JavaScript");

var aidc = new Aidc();
module.exports = aidc;
