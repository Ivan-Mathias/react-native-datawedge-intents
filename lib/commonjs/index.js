"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
Object.defineProperty(exports, "DataWedgeService", {
  enumerable: true,
  get: function () {
    return _DataWedgeService.default;
  }
});
exports.default = void 0;
var _reactNative = require("react-native");
var _DataWedgeService = _interopRequireDefault(require("./DataWedgeService.js"));
function _interopRequireDefault(e) { return e && e.__esModule ? e : { default: e }; }
const RNDataWedgeIntents = _reactNative.NativeModules.DataWedgeIntents;

// Define constants in JS to avoid bridge calls and Java dependency
const Constants = {
  ACTION_SOFTSCANTRIGGER: 'com.symbol.datawedge.api.ACTION_SOFTSCANTRIGGER',
  ACTION_SCANNERINPUTPLUGIN: 'com.symbol.datawedge.api.ACTION_SCANNERINPUTPLUGIN',
  ACTION_ENUMERATESCANNERS: 'com.symbol.datawedge.api.ACTION_ENUMERATESCANNERS',
  ACTION_SETDEFAULTPROFILE: 'com.symbol.datawedge.api.ACTION_SETDEFAULTPROFILE',
  ACTION_RESETDEFAULTPROFILE: 'com.symbol.datawedge.api.ACTION_RESETDEFAULTPROFILE',
  ACTION_SWITCHTOPROFILE: 'com.symbol.datawedge.api.ACTION_SWITCHTOPROFILE',
  START_SCANNING: 'START_SCANNING',
  STOP_SCANNING: 'STOP_SCANNING',
  TOGGLE_SCANNING: 'TOGGLE_SCANNING',
  ENABLE_PLUGIN: 'ENABLE_PLUGIN',
  DISABLE_PLUGIN: 'DISABLE_PLUGIN'
};
const DataWedgeIntents = {
  ...Constants,
  sendBroadcastWithExtras(extrasObject) {
    if (_reactNative.Platform.OS === 'android') {
      RNDataWedgeIntents.sendBroadcastWithExtras(extrasObject);
    }
  },
  registerBroadcastReceiver(filter) {
    if (_reactNative.Platform.OS === 'android') {
      RNDataWedgeIntents.registerBroadcastReceiver(filter);
    }
  }
};
var _default = exports.default = DataWedgeIntents;
//# sourceMappingURL=index.js.map