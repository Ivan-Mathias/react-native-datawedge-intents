"use strict";

import { NativeModules, Platform } from 'react-native';
const RNDataWedgeIntents = NativeModules.DataWedgeIntents;
const DataWedgeService = {
  initialize: ({
    packageName,
    profileName,
    disableKeystroke = true
  }) => {
    if (Platform.OS !== 'android') {
      console.warn('DataWedgeService: Scanning is only supported on Android.');
      return;
    }
    if (!packageName) {
      throw new Error('DataWedgeService: packageName is required for initialization.');
    }
    const finalProfileName = profileName || packageName.replace(/\./g, '_');
    const profileIntentAction = packageName + '.ACTION';
    RNDataWedgeIntents.registerBroadcastReceiver({
      filterActions: [profileIntentAction, 'com.symbol.datawedge.api.RESULT_ACTION'],
      filterCategories: ['android.intent.category.DEFAULT']
    });
    const barcodePluginConfig = {
      PROFILE_NAME: finalProfileName,
      CONFIG_MODE: 'CREATE_IF_NOT_EXIST',
      PLUGIN_CONFIG: {
        PLUGIN_NAME: 'BARCODE',
        PARAM_LIST: {
          scanner_selection: 'auto',
          illumination_mode: 'off',
          decoder_i2of5: 'true',
          decoder_itf14_convert_to_ean13: 'true'
        }
      },
      APP_LIST: [{
        PACKAGE_NAME: packageName,
        ACTIVITY_LIST: ['*']
      }]
    };
    const intentPluginConfig = {
      PROFILE_NAME: finalProfileName,
      CONFIG_MODE: 'CREATE_IF_NOT_EXIST',
      PLUGIN_CONFIG: {
        PLUGIN_NAME: 'INTENT',
        PARAM_LIST: {
          intent_output_enabled: 'true',
          intent_action: profileIntentAction,
          intent_delivery: '2'
        }
      }
    };
    sendCommand('com.symbol.datawedge.api.CREATE_PROFILE', finalProfileName);
    sendCommand('com.symbol.datawedge.api.SET_CONFIG', barcodePluginConfig);
    sendCommand('com.symbol.datawedge.api.SET_CONFIG', intentPluginConfig);
    if (disableKeystroke) {
      const keystrokeConfig = {
        PROFILE_NAME: finalProfileName,
        PLUGIN_CONFIG: {
          PLUGIN_NAME: 'KEYSTROKE',
          PARAM_LIST: {
            keystroke_output_enabled: 'false'
          }
        }
      };
      sendCommand('com.symbol.datawedge.api.SET_CONFIG', keystrokeConfig);
    }
  },
  /**
   * Switch the scanner light (torch) on or off.
   * @param {boolean} flashOn
   */
  setIllumination: flashOn => {
    if (Platform.OS !== 'android') return;
    const illumination_mode = flashOn ? 'torch' : 'off';
    sendCommand('com.symbol.datawedge.api.SWITCH_SCANNER_PARAMS', {
      illumination_mode
    });
  },
  /**
   * Helper to parse the raw intent into a clean object.
   * @param {object} intent
   */
  parseIntentToScan: intent => {
    if (intent && Object.prototype.hasOwnProperty.call(intent, 'com.symbol.datawedge.data_string')) {
      return {
        data: intent['com.symbol.datawedge.data_string'] || '',
        decoder: intent['com.symbol.datawedge.label_type'] || 'unknown',
        timeAtDecode: new Date()
      };
    }
    return null;
  }
};

/**
 * Internal Helper to send Broadcasts
 */
function sendCommand(command, parameter) {
  if (Platform.OS !== 'android') return;
  const extras = {};
  extras[command] = parameter;
  extras.SEND_RESULT = 'true';
  RNDataWedgeIntents.sendBroadcastWithExtras({
    action: 'com.symbol.datawedge.api.ACTION',
    extras
  });
}
export default DataWedgeService;
//# sourceMappingURL=DataWedgeService.js.map