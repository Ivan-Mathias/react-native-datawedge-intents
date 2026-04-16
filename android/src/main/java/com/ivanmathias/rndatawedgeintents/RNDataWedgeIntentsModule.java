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
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
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
                putReadableMapIntoIntent(i, extras);
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
                putValueInWritableMap(params, key, value);
            }

            // Add action for context
            params.putString("action", intent.getAction());

            // Emit event
            sendEvent(EVENT_NAME, params);
        }
    }

    private void putValueInWritableMap(WritableMap map, String key, Object value) {
        if (value == null) {
            map.putNull(key);
            return;
        }

        if (value instanceof String) {
            map.putString(key, (String) value);
        } else if (value instanceof Boolean) {
            map.putBoolean(key, (Boolean) value);
        } else if (value instanceof Integer) {
            map.putInt(key, (Integer) value);
        } else if (value instanceof Long) {
            map.putDouble(key, ((Long) value).doubleValue());
        } else if (value instanceof Float) {
            map.putDouble(key, ((Float) value).doubleValue());
        } else if (value instanceof Double) {
            map.putDouble(key, (Double) value);
        } else if (value instanceof Bundle) {
            map.putMap(key, bundleToWritableMap((Bundle) value));
        } else if (value instanceof ArrayList<?>) {
            map.putArray(key, arrayListToWritableArray((ArrayList<?>) value));
        } else {
            map.putString(key, String.valueOf(value));
        }
    }

    private WritableMap bundleToWritableMap(Bundle bundle) {
        WritableMap map = Arguments.createMap();
        for (String nestedKey : bundle.keySet()) {
            Object nestedValue = bundle.get(nestedKey);
            putValueInWritableMap(map, nestedKey, nestedValue);
        }
        return map;
    }

    private WritableArray arrayListToWritableArray(ArrayList<?> list) {
        WritableArray array = Arguments.createArray();
        for (Object item : list) {
            if (item == null) {
                array.pushNull();
            } else if (item instanceof String) {
                array.pushString((String) item);
            } else if (item instanceof Boolean) {
                array.pushBoolean((Boolean) item);
            } else if (item instanceof Integer) {
                array.pushInt((Integer) item);
            } else if (item instanceof Long) {
                array.pushDouble(((Long) item).doubleValue());
            } else if (item instanceof Float) {
                array.pushDouble(((Float) item).doubleValue());
            } else if (item instanceof Double) {
                array.pushDouble((Double) item);
            } else if (item instanceof Bundle) {
                array.pushMap(bundleToWritableMap((Bundle) item));
            } else if (item instanceof ArrayList<?>) {
                array.pushArray(arrayListToWritableArray((ArrayList<?>) item));
            } else {
                array.pushString(String.valueOf(item));
            }
        }
        return array;
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
        if (getReactApplicationContext().hasActiveCatalystInstance()) {
            getReactApplicationContext()
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        }
    }

    private void putReadableMapIntoIntent(Intent intent, ReadableMap map) {
        ReadableMapKeySetIterator iterator = map.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = map.getType(key);

            switch (type) {
                case Null:
                    break;
                case Boolean:
                    intent.putExtra(key, map.getBoolean(key));
                    break;
                case Number:
                    double number = map.getDouble(key);
                    if (number % 1 == 0) {
                        intent.putExtra(key, (long) number);
                    } else {
                        intent.putExtra(key, number);
                    }
                    break;
                case String:
                    intent.putExtra(key, map.getString(key));
                    break;
                case Map:
                    intent.putExtra(key, readableMapToBundle(map.getMap(key)));
                    break;
                case Array:
                    putReadableArrayIntoIntent(intent, key, map.getArray(key));
                    break;
            }
        }
    }

    private Bundle readableMapToBundle(ReadableMap map) {
        Bundle bundle = new Bundle();
        ReadableMapKeySetIterator iterator = map.keySetIterator();

        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            ReadableType type = map.getType(key);

            switch (type) {
                case Null:
                    break;
                case Boolean:
                    bundle.putBoolean(key, map.getBoolean(key));
                    break;
                case Number:
                    double number = map.getDouble(key);
                    if (number % 1 == 0) {
                        bundle.putLong(key, (long) number);
                    } else {
                        bundle.putDouble(key, number);
                    }
                    break;
                case String:
                    bundle.putString(key, map.getString(key));
                    break;
                case Map:
                    bundle.putBundle(key, readableMapToBundle(map.getMap(key)));
                    break;
                case Array:
                    putReadableArrayIntoBundle(bundle, key, map.getArray(key));
                    break;
            }
        }

        return bundle;
    }

    private void putReadableArrayIntoIntent(Intent intent, String key, ReadableArray array) {
        if (array == null || array.size() == 0) {
            if ("ACTIVITY_LIST".equals(key)) {
                intent.putExtra(key, new String[0]);
            } else if ("APP_LIST".equals(key) || "PLUGIN_CONFIG".equals(key)) {
                intent.putExtra(key, new Bundle[0]);
            } else {
                intent.putStringArrayListExtra(key, new ArrayList<String>());
            }
            return;
        }

        ReadableType firstType = firstNonNullType(array);
        if (firstType == null) {
            intent.putStringArrayListExtra(key, new ArrayList<String>());
            return;
        }

        switch (firstType) {
            case String: {
                String[] values = new String[array.size()];
                int valueCount = 0;
                for (int i = 0; i < array.size(); i++) {
                    if (array.getType(i) == ReadableType.String) {
                        values[valueCount++] = array.getString(i);
                    }
                }
                String[] normalizedValues = new String[valueCount];
                System.arraycopy(values, 0, normalizedValues, 0, valueCount);

                if ("ACTIVITY_LIST".equals(key)) {
                    intent.putExtra(key, normalizedValues);
                } else {
                    ArrayList<String> valuesList = new ArrayList<>();
                    for (String value : normalizedValues) {
                        valuesList.add(value);
                    }
                    intent.putStringArrayListExtra(key, valuesList);
                }
                break;
            }
            case Map: {
                ArrayList<Bundle> bundles = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    if (array.getType(i) == ReadableType.Map) {
                        bundles.add(readableMapToBundle(array.getMap(i)));
                    }
                }
                Bundle[] bundleArray = bundles.toArray(new Bundle[0]);
                if ("APP_LIST".equals(key) || "PLUGIN_CONFIG".equals(key)) {
                    intent.putExtra(key, bundleArray);
                } else {
                    intent.putParcelableArrayListExtra(key, bundles);
                }
                break;
            }
            default:
                break;
        }
    }

    private void putReadableArrayIntoBundle(Bundle bundle, String key, ReadableArray array) {
        if (array == null || array.size() == 0) {
            if ("ACTIVITY_LIST".equals(key)) {
                bundle.putStringArray(key, new String[0]);
            } else if ("APP_LIST".equals(key) || "PLUGIN_CONFIG".equals(key)) {
                bundle.putParcelableArray(key, new Bundle[0]);
            } else {
                bundle.putStringArrayList(key, new ArrayList<String>());
            }
            return;
        }

        ReadableType firstType = firstNonNullType(array);
        if (firstType == null) {
            bundle.putStringArrayList(key, new ArrayList<String>());
            return;
        }

        switch (firstType) {
            case String: {
                String[] values = new String[array.size()];
                int valueCount = 0;
                for (int i = 0; i < array.size(); i++) {
                    if (array.getType(i) == ReadableType.String) {
                        values[valueCount++] = array.getString(i);
                    }
                }
                String[] normalizedValues = new String[valueCount];
                System.arraycopy(values, 0, normalizedValues, 0, valueCount);

                if ("ACTIVITY_LIST".equals(key)) {
                    bundle.putStringArray(key, normalizedValues);
                } else {
                    ArrayList<String> valuesList = new ArrayList<>();
                    for (String value : normalizedValues) {
                        valuesList.add(value);
                    }
                    bundle.putStringArrayList(key, valuesList);
                }
                break;
            }
            case Map: {
                ArrayList<Bundle> bundles = new ArrayList<>();
                for (int i = 0; i < array.size(); i++) {
                    if (array.getType(i) == ReadableType.Map) {
                        bundles.add(readableMapToBundle(array.getMap(i)));
                    }
                }
                Bundle[] bundleArray = bundles.toArray(new Bundle[0]);
                if ("APP_LIST".equals(key) || "PLUGIN_CONFIG".equals(key)) {
                    bundle.putParcelableArray(key, bundleArray);
                } else {
                    bundle.putParcelableArrayList(key, bundles);
                }
                break;
            }
            default:
                break;
        }
    }

    @Nullable
    private ReadableType firstNonNullType(ReadableArray array) {
        for (int i = 0; i < array.size(); i++) {
            ReadableType type = array.getType(i);
            if (type != ReadableType.Null) {
                return type;
            }
        }
        return null;
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
