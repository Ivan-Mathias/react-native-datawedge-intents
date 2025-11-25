package com.ivanmathias.rndatawedgeintents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RNDataWedgeIntentsModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private static final String TAG = "DataWedgeIntents";
    private static final String EVENT_NAME = "datawedge_broadcast_intent";

    // Using a single instance receiver instead of static/observable complexity
    private final BroadcastReceiver dataWedgeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleIntent(intent);
        }
    };

    private IntentFilter savedIntentFilter = null;
    private boolean isReceiverRegistered = false;

    public RNDataWedgeIntentsModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "DataWedgeIntents";
    }

    @Override
    public Map<String, Object> getConstants() {
        // Returning empty map as we removed deprecated constants.
        // If your JS code relies on specific constants, add them back here.
        return new HashMap<>();
    }

    // --- Lifecycle Management ---

    @Override
    public void onHostResume() {
        // Automatically re-register receiver if we have a filter saved
        if (savedIntentFilter != null && !isReceiverRegistered) {
            registerReceiverSafely(savedIntentFilter);
        }
    }

    @Override
    public void onHostPause() {
        unregisterReceiverSafely();
    }

    @Override
    public void onHostDestroy() {
        unregisterReceiverSafely();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        unregisterReceiverSafely();
    }

    // --- Core Logic ---

    @ReactMethod
    public void registerBroadcastReceiver(ReadableMap filterObj) {
        // 1. Unregister existing if any
        unregisterReceiverSafely();

        // 2. Build the new filter
        IntentFilter filter = new IntentFilter();

        if (filterObj.hasKey("filterActions")) {
            ReadableArray actions = filterObj.getArray("filterActions");
            if (actions != null) {
                for (int i = 0; i < actions.size(); i++) {
                    filter.addAction(actions.getString(i));
                }
            }
        }

        if (filterObj.hasKey("filterCategories")) {
            ReadableArray categories = filterObj.getArray("filterCategories");
            if (categories != null) {
                for (int i = 0; i < categories.size(); i++) {
                    filter.addCategory(categories.getString(i));
                }
            }
        }

        // 3. Save filter for lifecycle restoration
        this.savedIntentFilter = filter;

        // 4. Register immediately
        registerReceiverSafely(filter);
    }

    @ReactMethod
    public void sendBroadcastWithExtras(ReadableMap obj) {
        String action = obj.hasKey("action") ? obj.getString("action") : null;
        if (action == null) return;

        Intent i = new Intent();
        i.setAction(action);

        if (obj.hasKey("extras")) {
            ReadableMap extras = obj.getMap("extras");
            if (extras != null) {
                Map<String, Object> extrasMap = extras.toHashMap();

                for (Map.Entry<String, Object> entry : extrasMap.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (value == null) continue;

                    if (value instanceof Boolean) {
                        i.putExtra(key, (Boolean) value);
                    } else if (value instanceof Number) {
                        double val = ((Number) value).doubleValue();
                        if (val % 1 == 0) {
                            i.putExtra(key, (long) val);
                        } else {
                            i.putExtra(key, val);
                        }
                    } else if (value instanceof String) {
                        String valueStr = (String) value;
                        if (valueStr.startsWith("{")) {
                            try {
                                Bundle bundle = toBundle(new JSONObject(valueStr));
                                i.putExtra(key, bundle);
                            } catch (JSONException e) {
                                i.putExtra(key, valueStr);
                            }
                        } else {
                            i.putExtra(key, valueStr);
                        }
                    }
                }
            }
        }

        getReactApplicationContext().sendBroadcast(i);
    }

    // --- Helpers ---

    private void registerReceiverSafely(IntentFilter filter) {
        if (isReceiverRegistered) return;

        try {
            // ANDROID 12+ FIX:
            // DataWedge is an external application/service.
            // We MUST use RECEIVER_EXPORTED to receive broadcasts from it on Android 14+.
            if (Build.VERSION.SDK_INT >= 34 && getReactApplicationContext().getApplicationInfo().targetSdkVersion >= 34) {
                 getReactApplicationContext().registerReceiver(dataWedgeReceiver, filter, Context.RECEIVER_EXPORTED);
            } else {
                 getReactApplicationContext().registerReceiver(dataWedgeReceiver, filter);
            }
            isReceiverRegistered = true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to register receiver", e);
        }
    }

    private void unregisterReceiverSafely() {
        if (!isReceiverRegistered) return;
        try {
            getReactApplicationContext().unregisterReceiver(dataWedgeReceiver);
        } catch (IllegalArgumentException e) {
            // Ignore if already unregistered
        }
        isReceiverRegistered = false;
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;

        // Convert Bundle to WritableMap
        Bundle extras = intent.getExtras();
        if (extras != null) {
            WritableMap params = Arguments.createMap();
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                // Simple type handling
                if (value instanceof String) {
                    params.putString(key, (String) value);
                } else if (value instanceof Boolean) {
                    params.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    params.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    params.putDouble(key, (Double) value);
                }
                // Note: Complex nested bundles/arrays omitted for brevity and speed,
                // but standard DataWedge scans are flat strings/lines.
            }

            // Add action for context
            params.putString("action", intent.getAction());

            // Emit event
            sendEvent(EVENT_NAME, params);
        }
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        if (getReactApplicationContext().hasActiveCatalystInstance()) {
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    // Legacy JSON to Bundle converter (kept for sendBroadcastWithExtras support)
    private Bundle toBundle(final JSONObject obj) throws JSONException {
        Bundle returnBundle = new Bundle();
        if (obj == null) return null;
        Iterator<?> keys = obj.keys();
        while(keys.hasNext()) {
            String key = (String)keys.next();
            Object value = obj.get(key);
            if (value instanceof String) returnBundle.putString(key, (String) value);
            else if (value instanceof Boolean) returnBundle.putBoolean(key, (Boolean) value);
            else if (value instanceof Integer) returnBundle.putInt(key, (Integer) value);
        }
        return returnBundle;
    }
}
