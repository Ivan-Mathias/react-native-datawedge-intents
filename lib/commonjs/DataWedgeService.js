"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.default = void 0;
var _reactNative = require("react-native");
const RNDataWedgeIntents = _reactNative.NativeModules.DataWedgeIntents;
const DataWedgeService = {
  initialize: ({
    packageName,
    profileName,
    disableKeystroke = true
  }) => {
    if (_reactNative.Platform.OS !== 'android') {
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
    const datawedgeConfig = {
      PROFILE_NAME: finalProfileName,
      PROFILE_ENABLED: 'true',
      CONFIG_MODE: 'CREATE_IF_NOT_EXIST',
      APP_LIST: [{
        PACKAGE_NAME: packageName,
        ACTIVITY_LIST: ['*']
      }],
      PLUGIN_CONFIG: [{
        PLUGIN_NAME: 'BARCODE',
        RESET_CONFIG: 'true',
        PARAM_LIST: {
          scanner_selection: 'auto',
          illumination_mode: 'off',
          decoder_i2of5: 'true',
          decoder_itf14_convert_to_ean13: 'true'
        }
      }, {
        PLUGIN_NAME: 'INTENT',
        RESET_CONFIG: 'true',
        PARAM_LIST: {
          intent_output_enabled: 'true',
          intent_action: profileIntentAction,
          intent_category: 'android.intent.category.DEFAULT',
          intent_delivery: '2'
        }
      }, ...(disableKeystroke ? [{
        PLUGIN_NAME: 'KEYSTROKE',
        RESET_CONFIG: 'true',
        PARAM_LIST: {
          keystroke_output_enabled: 'false'
        }
      }] : [])]
    };
    sendCommand('com.symbol.datawedge.api.SET_CONFIG', datawedgeConfig);
  },
  /**
   * Switch the scanner light (torch) on or off.
   * @param {boolean} flashOn
   */
  setIllumination: flashOn => {
    if (_reactNative.Platform.OS !== 'android') return;
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
  if (_reactNative.Platform.OS !== 'android') return;
  const extras = {};
  extras[command] = parameter;
  extras.SEND_RESULT = 'true';
  RNDataWedgeIntents.sendBroadcastWithExtras({
    action: 'com.symbol.datawedge.api.ACTION',
    extras
  });
}
var _default = exports.default = DataWedgeService;
//# sourceMappingURL=DataWedgeService.js.map