import { Platform, NativeModules } from 'react-native'

interface RNDataWedgeIntentsModule {
  sendBroadcastWithExtras(extras: any): void
  registerBroadcastReceiver(filter: any): void
}

const RNDataWedgeIntents = NativeModules.DataWedgeIntents as RNDataWedgeIntentsModule

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
}

const DataWedgeIntents = {
  ...Constants,

  sendBroadcastWithExtras(extrasObject: any) {
    if (Platform.OS === 'android') {
      RNDataWedgeIntents.sendBroadcastWithExtras(extrasObject)
    }
  },

  registerBroadcastReceiver(filter: { filterActions: string[]; filterCategories: string[] }) {
    if (Platform.OS === 'android') {
      RNDataWedgeIntents.registerBroadcastReceiver(filter)
    }
  }
}

export { RNDataWedgeIntents }
export default DataWedgeIntents
