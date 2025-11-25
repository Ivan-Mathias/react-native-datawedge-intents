"use strict";

import { Platform, NativeModules } from 'react-native';
const RNDataWedgeIntents = NativeModules.DataWedgeIntents;

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
    if (Platform.OS === 'android') {
      RNDataWedgeIntents.sendBroadcastWithExtras(extrasObject);
    }
  },
  registerBroadcastReceiver(filter) {
    if (Platform.OS === 'android') {
      RNDataWedgeIntents.registerBroadcastReceiver(filter);
    }
  }
};
export { default as DataWedgeService } from "./DataWedgeService.js";
export default DataWedgeIntents;
//# sourceMappingURL=index.js.map