package com.rnlivesdksampleapp;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.anoto.live.penaccess.client.BluetoothDeviceList;
import com.anoto.live.penaccess.client.BluetoothPairedDeviceList;
import com.anoto.live.penaccess.client.IPenAccessListener;
import com.anoto.live.penaccess.client.IPenData;
import com.anoto.live.penaccess.client.ISettings;
import com.anoto.live.penaccess.client.SetActivationStateListener;
import com.anoto.live.penaccess.client.SetDisconnectListener;
import com.anoto.live.penaccess.client.SetPenDownConListener;
import com.anoto.live.penaccess.client.SetPenNotifyMemThrshldListener;
import com.anoto.live.penaccess.client.SetPenStreamingListener;
import com.anoto.live.penaccess.client.SetPenTimeListener;
import com.anoto.live.penaccess.client.SetResetListener;
import com.anoto.live.penaccess.client.UpdatePenFirmwareListener;
import com.anoto.live.penaccess.responseobjects.ArchiveInfo;
import com.anoto.live.penaccess.responseobjects.Info;
import com.anoto.live.penaccess.responseobjects.Notification;
import com.anoto.live.penaccess.responseobjects.PairedDevice;
import com.anoto.live.penaccess.responseobjects.Settings;
import com.anoto.live.penaccess.responseobjects.Status;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;


import com.anoto.live.penaccess.client.PenManager;
import com.google.gson.Gson;
import com.livescribe.afp.PageTemplate;
import com.rnlivesdksampleapp.paperlib.AfdHandler;
import com.rnlivesdksampleapp.paperlib.afd.AFDHelper;
import com.rnlivesdksampleapp.paperlib.records.AfdRec;
import com.rnlivesdksampleapp.paperlib.records.ImageRec;
import com.rnlivesdksampleapp.paperlib.records.TemplateRec;
import com.rnlivesdksampleapp.penaccess.model.JsonNotification;


public class LiveSDKModule extends ReactContextBaseJavaModule implements IPenAccessListener {

    private static final String TAG = "LiveSDKModule";
    private static ReactApplicationContext reactContext;

    // Event Type
    private static final String EVENT_STATUS_BLUETOOTH_DEVICE_FOUND = "EventBluetoothDeviceFound";
    private static final String EVENT_STATUS_BLUETOOTH_PAIRED_DEVICE = "EventBluetoothPairedDevice";
    private static final String EVENT_STATUS_BLUETOOTH         = "EventBluetoothStatus";
    private static final String EVENT_PEN_SETTING              = "EventPenSetting";
    private static final String EVENT_PEN_STATUS               = "EventPenStatus";
    private static final String EVENT_PEN_INFO                 = "EventPenInfo";
    private static final String EVENT_STREAMING                = "EventStreaming";
    private static final String EVENT_PAGE_TEMPLATE            = "EventPageTemplate";
    private static final String EVENT_LOG                      = "EventLog";

    // Event Parameter Key
    private static final String EVENT_PARAM_KEY_TYPE           = "Type";
    private static final String EVENT_PARAM_KEY_DEVICE_NAME    = "Name";
    private static final String EVENT_PARAM_KEY_DEVICE_ADDRESS = "Address";
    private static final String EVENT_PARAM_KEY_MESSAGE        = "Message";

    // Event Parameter Key - Setting
    private static final String EVENT_PARAM_KEY_SETTING_ACTIVATION_STATE = "SettingActivationState";
    private static final String EVENT_PARAM_KEY_SETTING_BLUETOOTH_ON = "SettingBluetoothOn";
    private static final String EVENT_PARAM_KEY_SETTING_DISCONNECT_TIME = "SettingDisconnectTime";
    private static final String EVENT_PARAM_KEY_SETTING_DISCOVERABLE = "SettingDiscoverable";
    private static final String EVENT_PARAM_KEY_SETTING_LEFTHAND = "SettingLeftHand";
    private static final String EVENT_PARAM_KEY_SETTING_MUTE = "SettingMute";
    private static final String EVENT_PARAM_KEY_SETTING_PEN_NAME = "SettingPenName";
    private static final String EVENT_PARAM_KEY_SETTING_NOTIFY_MEMORY_THRESHHOLD = "SettingNotifyMemoryThreshHold";
    private static final String EVENT_PARAM_KEY_SETTING_PENDOWN_CON = "SettingPenDownCon";
    private static final String EVENT_PARAM_KEY_SETTING_PEN_TIME = "SettingPenTime";
    private static final String EVENT_PARAM_KEY_SETTING_PEN_ID = "SettingPenId";

    // Event Parameter Key - Status
    private static final String EVENT_PARAM_KEY_STATUS_BATTERY = "StatusBattery";
    private static final String EVENT_PARAM_KEY_STATUS_MEMORYFULL = "StatusMemoryFull";
    private static final String EVENT_PARAM_KEY_STATUS_USED_MEMORY = "StatusUsedMemory";
    private static final String EVENT_PARAM_KEY_STATUS_ENCON = "StatusEncOn";
    private static final String EVENT_PARAM_KEY_STATUS_PENID = "StatusPenId";

    // Event Parameter Key - Info
    private static final String EVENT_PARAM_KEY_INFO_ADDRESS = "InfoAddress";
    private static final String EVENT_PARAM_KEY_INFO_PEN_ID = "InfoPenID";
    private static final String EVENT_PARAM_KEY_INFO_FIRMWARE_VERSION = "InfoFirmwareVersion";
    private static final String EVENT_PARAM_KEY_INFO_PID = "InfoPID";
    private static final String EVENT_PARAM_KEY_INFO_VID = "InfoVID";
    private static final String EVENT_PARAM_KEY_INFO_TOTAL_MEMORY = "InfoTotalMemory";

    // Event Parameter Key - Page Template
    private static final String EVENT_PARAM_KEY_PAGE_TEMPLATE_WIDTH = "PageWidth";
    private static final String EVENT_PARAM_KEY_PAGE_TEMPLATE_HEIGHT = "PageHeight";
    private static final String EVENT_PARAM_KEY_PAGE_TEMPLATE_PAGENUMBER = "PageNumber";

    // Bluetooth Status Type
    private static final int BLUETOOTH_STATUS_IDLE             = 0;
    private static final int BLUETOOTH_STATUS_ENABLING         = 1;
    private static final int BLUETOOTH_STATUS_ENABLED          = 2;
    private static final int BLUETOOTH_STATUS_DISABLED         = 3;
    private static final int BLUETOOTH_STATUS_DISCOVERING      = 4;
    private static final int BLUETOOTH_STATUS_DISCOVER_DONE    = 5;
    private static final int BLUETOOTH_STATUS_READY_TO_PAIR    = 6;
    private static final int BLUETOOTH_STATUS_PAIRING          = 7;
    private static final int BLUETOOTH_STATUS_PAIRING_ENDED    = 8;
    private static final int BLUETOOTH_STATUS_UNABL_BOND       = 9;
    private static final int BLUETOOTH_STATUS_BONDED           = 10;
    private static final int BLUETOOTH_STATUS_WAIT_FOR_CONNECT = 11;
    private static final int BLUETOOTH_STATUS_CONNECTED        = 12;
    private static final int BLUETOOTH_STATUS_DISCONNECTED     = 13;
    private static final int BLUETOOTH_STATUS_PAIRED_DEVICE    = 14;

    private PenManager mPenManager;
    private AfdHandler mAFDHandler;
    private ISettings mISetting;
    private com.rnlivesdksampleapp.Settings mSetting;
    private Gson gson = new Gson();

    private Callback mDiscoveredDevice;
    private String mTargetPairDevice;
    private List<BluetoothDevice> mBluetoothFoundDeviceList = new ArrayList<BluetoothDevice>();

    private boolean mIsConnected = false;

    LiveSDKModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;

        mPenManager = new PenManager();
        mSetting = new com.rnlivesdksampleapp.Settings(false, false);
        mAFDHandler = AfdHandler.getInstance(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "PenManager";
    }

    //----------------------------------------------------------------------------------------------
    // Bluetooth Event
    //----------------------------------------------------------------------------------------------
    private void sendEventToClient(ReactContext reactContext,
                                   String eventName,
                                   @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    //----------------------------------------------------------------------------------------------
    // Expose LiveSDK APIs
    //----------------------------------------------------------------------------------------------

    @ReactMethod
    public void startSDK() {

        Log.d(TAG,"LiveSDK > startSDK >");

        WritableMap params = Arguments.createMap();
        params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > LiveSDK is started");
        sendEventToClient(reactContext, EVENT_LOG, params);
        if (mPenManager != null) {
            mPenManager.stop();
            mSetting.setScanForNewPen(true);
            mPenManager.start(reactContext, this, mSetting);
        }
    }
    
    @ReactMethod
    public void stopSDK() {
        Log.d(TAG,"LiveSDK > stopSDK > ");
        mPenManager.stop();
    }

    @ReactMethod
    public void scanForDevice() {
        Log.d(TAG,"LiveSDK > scanForDevice > ");

        WritableMap params = Arguments.createMap();
        params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > scanForDevice >");
        sendEventToClient(reactContext, EVENT_LOG, params);

        if (mPenManager != null) {
            mPenManager.stop();
            mSetting.setScanForNewPen(true);
            mPenManager.start(reactContext, this, mSetting);
        }
    }

    //----------------------------------------------------------------------------------------------
    // Pen APIs
    //----------------------------------------------------------------------------------------------

    @ReactMethod
    public void doScanDevice() {
        Log.d(TAG,"LiveSDK > scanForNewPen > ");

        WritableMap params = Arguments.createMap();
        params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Start to scan");
        sendEventToClient(reactContext, EVENT_LOG, params);

        mBluetoothFoundDeviceList.clear();

        if (mPenManager != null) {
            mPenManager.stop();
            mSetting.setScanForNewPen(true);
            mPenManager.start(reactContext, this, mSetting);
        }
    }

    @ReactMethod
    public void doPairDevice(String address) {
        Log.d(TAG,"LiveSDK > pairDevice > " + address);

        WritableMap params = Arguments.createMap();
        params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Trying to pair with device [" + address + "]");
        sendEventToClient(reactContext, EVENT_LOG, params);

        mSetting.setScanForNewPen(false);

        mTargetPairDevice = address;
    }

    @ReactMethod
    public void doDisconnect() throws IOException {

        Log.d(TAG,"LiveSDK > setDisconnect");

        if(!mIsConnected) {
            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Error > Pen is not connected");
            sendEventToClient(reactContext, EVENT_LOG, params);
            return;
        }

        mPenManager.setDisconnect(new SetDisconnectListener() {
            @Override
            public void setDisconnectCompleted() {
                // Update Settings
                Log.d(TAG,"LiveSDK > setDisconnect > setDisconnectCompleted");

                try {
                    requestSettings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void setDisconnectFailed(Exception e) {

                Log.d(TAG,"LiveSDK > setDisconnect > setDisconnectFailed");

            }
        });
    }

    @ReactMethod
    public void requestSettings() throws IOException {
        Log.d(TAG,"LiveSDK > requestSetings >");
        if(!mIsConnected) {
            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Error > Pen is not connected");
            sendEventToClient(reactContext, EVENT_LOG, params);
            return;
        }
        mPenManager.getSettings();
    }

    @ReactMethod
    public void requestStatus() throws IOException {
        Log.d(TAG,"LiveSDK > requestStatus >");
        if(!mIsConnected) {
            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Error > Pen is not connected");
            sendEventToClient(reactContext, EVENT_LOG, params);
            return;
        }
        mPenManager.getStatus();
    }

    @ReactMethod
    public void requestInfo() throws IOException {
        Log.d(TAG,"LiveSDK > requestInfo");
        if(!mIsConnected) {
            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Error > Pen is not connected");
            sendEventToClient(reactContext, EVENT_LOG, params);
            return;
        }
        mPenManager.getInfo();
    }

    @ReactMethod
    public void requestReset() throws IOException {

        Log.d(TAG,"LiveSDK > requestReset");
        if(!mIsConnected) {
            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Error > Pen is not connected");
            sendEventToClient(reactContext, EVENT_LOG, params);
            return;
        }
        mPenManager.setReset(true, new SetResetListener() {
            @Override
            public void setResetCompleted() {
                Log.d(TAG,"setResetCompleted");

                WritableMap params = Arguments.createMap();
                params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Reset completed");
                sendEventToClient(reactContext, EVENT_LOG, params);
            }
            @Override
            public void setResetFailed(Exception e) {
                Log.d(TAG,"setResetFailed : exception? " + e.toString());

                WritableMap params = Arguments.createMap();
                params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Reset failed. Exception [" + e.toString() + "]");
                sendEventToClient(reactContext, EVENT_LOG, params);
            }
        });
    }

    @ReactMethod
    public void requestPairedDevicesWithPen() throws IOException {

        Log.d(TAG,"LiveSDK > requestPairedDevicesWithPen");

        WritableMap params = Arguments.createMap();
        params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Request to get the paired devices");
        sendEventToClient(reactContext, EVENT_LOG, params);

        mPenManager.getPairedDevicesWithPen();
    }

    @ReactMethod
    public void updatePenFirmware(byte[] aFirmwareData) throws IOException {

        Log.d(TAG,"LiveSDK > updatePenFirmware");

        mPenManager.updatePenFirmware(aFirmwareData, new UpdatePenFirmwareListener() {

            @Override
            public void fwWriteStarted(final IPenData aPenData) {
                Log.d(TAG,"LiveSDK > updatePenFirmware > fwWriteStarted");
            }

            @Override
            public void fwWriteFailed(Exception aException) {
                Log.d(TAG,"LiveSDK > updatePenFirmware > fwWriteFailed");
            }
        });
    }

    @ReactMethod
    public void setStreaming(final boolean on, boolean hover, boolean time, boolean store, boolean noBuf) throws IOException {

        Log.d(TAG,"LiveSDK > setStreaming > [on " + on + "] hover ["+ hover + "] time [" + time + "] store ["+store+"] noBuf [" + noBuf +"]");

        WritableMap params = Arguments.createMap();
        params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > setStreaming > on [" + on + "] hover ["+ hover + "] time [" + time + "] store ["+store+"] noBuf [" + noBuf +"]");
        sendEventToClient(reactContext, EVENT_LOG, params);

        mPenManager.setStreamWithFeature(on, hover, time, store, noBuf, new SetPenStreamingListener() {
            @Override
            public void setPenStreamingCompleted() {
                Log.d("TAG", "setPenStreamingCompleted");
            }

            @Override
            public void setPenStreamingFailed(Exception aException) {
                Log.d("TAG", "setPenStreamingFailed: " + aException);
            }
        });
    }

    @ReactMethod
    public void setActivationState(final Integer activationState) throws IOException {

        Log.d(TAG,"LiveSDK > setActivationState");

        mPenManager.setActivationState(activationState.intValue(), new SetActivationStateListener() {

            @Override
            public void setActivationStateCompleted() {

                Log.d(TAG,"LiveSDK > setActivationState > setActivationStateCompleted");
                // Update Settings
                try {
                    requestSettings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void setActivationStateFailed(Exception e) {
                Log.d(TAG,"LiveSDK > setActivationState > setActivationStateFailed");
            }
        });
    }

    @ReactMethod
    public void setPenTime(Double aUtcTime) throws IOException {

        Log.d(TAG,"LiveSDK > setPenTime");

        mPenManager.setPenTime(new Double(aUtcTime).longValue(), new SetPenTimeListener() {

            @Override
            public void setPenTimeCompleted() {

                Log.d(TAG,"LiveSDK > setPenTime > setPenTimeCompleted");
                // Update settings
                try {
                    requestSettings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void setPenTimeFailed(Exception e) {
                Log.d(TAG,"LiveSDK > setPenTime > setPenTimeFailed");
            }
        });
    }

    @ReactMethod
    public void setPenNotifyMemThrshld(Integer aNotifyMemThrshld) throws IOException {

        Log.d(TAG,"LiveSDK > setPenNotifyMemThrshld");

        mPenManager.setPenNotifyMemThrshld(aNotifyMemThrshld, new SetPenNotifyMemThrshldListener() {

            @Override
            public void setPenNotifyMemThrshldCompleted() {
                Log.d(TAG,"LiveSDK > setPenNotifyMemThrshld > setPenNotifyMemThrshld");
            }

            @Override
            public void setPenNotifyMemThrshldFailed(Exception e) {
                Log.d(TAG,"LiveSDK > setPenNotifyMemThrshld > setPenNotifyMemThrshldFailed");
            }
        });
    }

    @ReactMethod
    public void cancelFirmwareUpdate() throws IOException {

        Log.d(TAG,"LiveSDK > cancelFirmwareUpdate");

        mPenManager.cancelFirmwareUpdate();
    }

    @ReactMethod
    public synchronized void requestEvents() {
        Log.d(TAG,"LiveSDK > requestEvents");
    }

    @ReactMethod
    public void setPenDownCon(final boolean penDownCon) throws IOException {

        Log.d(TAG,"LiveSDK > setPenDownCon");

        mPenManager.setPenDownCon(penDownCon, new SetPenDownConListener() {
            @Override
            public void setPenDownConCompleted() {
                Log.d(TAG,"LiveSDK > setPenDownCon > setPenDownConCompleted");
                // Update Settings
                try {
                    requestSettings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void setPenDownConFailed(Exception e) {
                Log.d(TAG,"LiveSDK > setPenDownCon > setPenDownConFailed");
            }
        });
    }

    @ReactMethod
    public void getPageTemplate(Double pageAddress) throws IOException  {
        Log.d(TAG,"LiveSDK > getPageTemplate for Address of " + new Double(pageAddress).longValue() + " ==========================================================");

        double width = 3000;
        double height = 5000;
        int number = 10;

        long targetPageAddress = new Double(pageAddress).longValue(); //Double.doubleToLongBits(pageAddress);
        Log.d(TAG,"LiveSDK > getPageTemplate for target Address of " + targetPageAddress);
        PageTemplate pageTemplate = mAFDHandler.getPageTemplate(targetPageAddress);

        if(pageTemplate == null) {
            Log.d(TAG,"LiveSDK > getPageTemplate > Failed to get imageRect ");
            return;
        }

        width = pageTemplate.getWidth();
        height = pageTemplate.getHeight();
        number = pageTemplate.getPageNumber();


        WritableMap params = Arguments.createMap();
        params.putDouble(EVENT_PARAM_KEY_PAGE_TEMPLATE_WIDTH, width);
        params.putDouble(EVENT_PARAM_KEY_PAGE_TEMPLATE_HEIGHT, height);
        params.putInt(EVENT_PARAM_KEY_PAGE_TEMPLATE_PAGENUMBER, number);
        sendEventToClient(reactContext, EVENT_PAGE_TEMPLATE, params);
    }

    //----------------------------------------------------------------------------------------------
    // Implement IPenAccessListener
    //----------------------------------------------------------------------------------------------

    @Override
    public void handleEnablingBluetooth() {
        Log.d(TAG,"LiveSDK > handleEnablingBluetooth");
        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_ENABLING);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Bluetooth Enabling");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);
    }

    @Override
    public void handleBluetoothEnabled(boolean b) {
        Log.d(TAG,"LiveSDK > handleBluetoothEnabled");
        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_ENABLED);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Bluetooth Enabled");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);
    }

    @Override
    public void handleDiscoveryStarted() {
        Log.d(TAG,"LiveSDK > handleDiscoveryStarted");
        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_DISCOVERING);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Discovering");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);
    }

    @Override
    public void handleDeviceFound(BluetoothDeviceList bluetoothDeviceList) {
        Log.d(TAG,"LiveSDK > handleDeviceFound");

        int i = 0;
        for (BluetoothDevice device: bluetoothDeviceList.getDevices()) {
            Log.d(TAG,"LiveSDK > handleDeviceFound > Device [" + i +"] Name ["+ device.getName()+"] Address ["+ device.getAddress()+"]");

            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_DEVICE_NAME, device.getName());
            params.putString(EVENT_PARAM_KEY_DEVICE_ADDRESS, device.getAddress());
            sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH_DEVICE_FOUND, params);

            if(!mBluetoothFoundDeviceList.contains(device)) {
                mBluetoothFoundDeviceList.add(device);
                Log.d(TAG,"LiveSDK > handleDeviceFound > added device to mBluetoothFoundDeviceList ["+ mBluetoothFoundDeviceList.size()+"]");
            }
        }
    }

    @Override
    public void handleDiscoveryEnded(boolean b) {

        Log.d(TAG,"LiveSDK > handleDiscoveryEnded");
        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_DISCOVER_DONE);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Discovery Ended");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

    }

    @Override
    public void handleChooseDevice(BluetoothDeviceList bluetoothDeviceList) {

        Log.d(TAG,"LiveSDK > handleChooseDevice");
        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_READY_TO_PAIR);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Ready to pair. Choose device to pair");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

        if(mTargetPairDevice != null) {
            for(BluetoothDevice device : mBluetoothFoundDeviceList) {
                if(device.getAddress().equals(mTargetPairDevice)) {
                    Log.d(TAG,"LiveSDK > handleChooseDevice > chooseDevice >Name ["+ device.getName()+"] Address ["+ device.getAddress()+"]");
                    bluetoothDeviceList.chooseDevice(device);
                    break;
                }
            }
        }
    }

    @Override
    public void handleDiscoverPairedStarted() {

        Log.d(TAG,"LiveSDK > handleDiscoverPairedStarted");

        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_PAIRING);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Pairing started");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

    }

    @Override
    public void handleDiscoverPairedEnded(boolean b) {

        Log.d(TAG,"LiveSDK > handleDiscoverPairedEnded");

        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_PAIRING_ENDED);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Pairing Ended");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

    }

    @Override
    public void handlePairedDevicesWithHost(BluetoothPairedDeviceList bluetoothPairedDeviceList) {
        Log.d(TAG,"LiveSDK > handlePairedDevicesWithHost > Device List : " + bluetoothPairedDeviceList.getDevices().size());
        int i = 0;
        for (BluetoothDevice device: bluetoothPairedDeviceList.getDevices()) {
            Log.d(TAG,"LiveSDK > handlePairedDevicesWithHost > Device [" + i++ +"] Name ["+ device.getName()+"] Address ["+ device.getAddress()+"]");
            
            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_DEVICE_NAME, device.getName());
            params.putString(EVENT_PARAM_KEY_DEVICE_ADDRESS, device.getAddress());

            sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH_PAIRED_DEVICE, params);
        }
    }

    @Override
    public void handleUnableToBond(String s) {

        Log.d(TAG,"LiveSDK > handleUnableToBond > s > " + s);

        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_UNABL_BOND);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Unable to Bond");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

    }

    @Override
    public void handleWaitingForConnect() {

        Log.d(TAG,"LiveSDK > handleWaitingForConnect");

        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_WAIT_FOR_CONNECT);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Wait for Connect");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

    }

    @Override
    public void handleConnected(BluetoothDevice bluetoothDevice) {

        Log.d(TAG, "LiveSDK > handleConnected > Name [" + bluetoothDevice.getName() + "] Address [" + bluetoothDevice.getAddress() + "]");
        this.mIsConnected = true;

        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_CONNECTED);
        params.putString(EVENT_PARAM_KEY_DEVICE_NAME, bluetoothDevice.getName());
        params.putString(EVENT_PARAM_KEY_DEVICE_ADDRESS, bluetoothDevice.getAddress());
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Connected");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

        Log.d(TAG, "LiveSDK > handleConnected > setStreamWithFeature");

        WritableMap params2 = Arguments.createMap();
        params2.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > setStreaming > on [" + true + "] hover ["+ false + "] time [" + false + "] store ["+false+"] noBuf [" + true +"]");
        sendEventToClient(reactContext, EVENT_LOG, params2);

        mPenManager.setStreamWithFeature(true, false, false, false, true, new SetPenStreamingListener() {
            @Override
            public void setPenStreamingCompleted() {
                Log.d(TAG, "LiveSDK > handleConnected > setStreamWithFeature > setPenStreamingCompleted");

                WritableMap params = Arguments.createMap();
                params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Completed to set the streaming feature");
                sendEventToClient(reactContext, EVENT_LOG, params);
            }

            @Override
            public void setPenStreamingFailed(Exception aException) {
                Log.d(TAG, "LiveSDK > handleConnected > setStreamWithFeature > setPenStreamingFailed");
                WritableMap params = Arguments.createMap();
                params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Failed to set the streaming feature");
                sendEventToClient(reactContext, EVENT_LOG, params);
            }
        });

        WritableMap params3 = Arguments.createMap();
        params3.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > setPenTime");
        sendEventToClient(reactContext, EVENT_LOG, params3);

        mPenManager.setPenTime(System.currentTimeMillis(), new SetPenTimeListener() {
            @Override
            public void setPenTimeCompleted() {
                Log.d(TAG, "LiveSDK > handleConnected > setPenTime > setPenTimeCompleted");

                WritableMap params = Arguments.createMap();
                params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Completed to set the pen time");
                sendEventToClient(reactContext, EVENT_LOG, params);
            }

            @Override
            public void setPenTimeFailed(Exception e) {
                Log.d(TAG, "LiveSDK > handleConnected > setPenTime > setPenTimeFailed");

                WritableMap params = Arguments.createMap();
                params.putString(EVENT_PARAM_KEY_MESSAGE, "# Log > Failed to set the pen time");
                sendEventToClient(reactContext, EVENT_LOG, params);
            }
        });

    }

    @Override
    public void handleDisconnected(BluetoothDevice bluetoothDevice) {

        Log.d(TAG,"LiveSDK > handleDisconnected > Name [" + bluetoothDevice.getName() + "] Address [" + bluetoothDevice.getAddress() +"]");
        this.mIsConnected = false;

        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_TYPE, BLUETOOTH_STATUS_DISCONNECTED);
        params.putString(EVENT_PARAM_KEY_MESSAGE, "> Disconnected");
        sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH, params);

    }

    @Override
    public void handleData(IPenData iPenData) {

        Log.d(TAG,"LiveSDK > handleData");

    }

    @Override
    public void handleSettings(Settings settings) {

        Log.d(TAG,"LiveSDK > handleSettings");

        settings.getActivationState();
                settings.getBluetoothOn();
                settings.getDisconnectTime();
                settings.getDiscoverable();
                settings.getLeftHand();
                settings.getMute();
                settings.getName();
                settings.getNotifyMemThrshld();
                settings.getPenDownCon();
                settings.getPenId();
                settings.getTime();

        WritableMap params = Arguments.createMap();
        params.putInt(EVENT_PARAM_KEY_SETTING_ACTIVATION_STATE, settings.getActivationState());
        params.putBoolean(EVENT_PARAM_KEY_SETTING_BLUETOOTH_ON, settings.getBluetoothOn());
        params.putBoolean(EVENT_PARAM_KEY_SETTING_DISCOVERABLE, settings.getDiscoverable());
        params.putBoolean(EVENT_PARAM_KEY_SETTING_LEFTHAND, settings.getLeftHand());
        params.putBoolean(EVENT_PARAM_KEY_SETTING_MUTE, settings.getMute());
        params.putString(EVENT_PARAM_KEY_SETTING_PEN_NAME, settings.getName());
        params.putInt(EVENT_PARAM_KEY_SETTING_NOTIFY_MEMORY_THRESHHOLD, settings.getNotifyMemThrshld());
        params.putBoolean(EVENT_PARAM_KEY_SETTING_PENDOWN_CON, settings.getPenDownCon());
        params.putDouble(EVENT_PARAM_KEY_SETTING_PEN_ID, settings.getPenId());
        params.putDouble(EVENT_PARAM_KEY_SETTING_PEN_TIME, settings.getTime());
        params.putInt(EVENT_PARAM_KEY_SETTING_DISCONNECT_TIME, settings.getDisconnectTime());
        sendEventToClient(reactContext, EVENT_PEN_SETTING, params);
    }

    @Override
    public void handleStatus(Status status) {

        Log.d(TAG,"LiveSDK > handleStatus > " + status.toString());

        WritableMap params = Arguments.createMap();
        params.putDouble(EVENT_PARAM_KEY_STATUS_PENID, status.getPenId());
        params.putInt(EVENT_PARAM_KEY_STATUS_BATTERY, status.getBattery());
        params.putBoolean(EVENT_PARAM_KEY_STATUS_MEMORYFULL, status.getMemFull());
        params.putDouble(EVENT_PARAM_KEY_STATUS_USED_MEMORY, status.getUsedMem());
        params.putBoolean(EVENT_PARAM_KEY_STATUS_ENCON, status.getEncrOn());
        sendEventToClient(reactContext, EVENT_PEN_STATUS, params);
    }

    @Override
    public void handleInfo(Info info) {

        Log.d(TAG,"LiveSDK > handleInfo > ");
        Log.d(TAG,"LiveSDK > handleInfo > Address      [" + info.getBluetoothAddress() +"]");
        Log.d(TAG,"LiveSDK > handleInfo > F/W version  [" + info.getFirmwareVersion() +"]");
        Log.d(TAG,"LiveSDK > handleInfo > Serial       [" + info.getPenSerial() +"]");
        Log.d(TAG,"LiveSDK > handleInfo > PenID        [" + info.getPenID() +"]");
        Log.d(TAG,"LiveSDK > handleInfo > Produce ID   [" + info.getProductID() +"]");
        Log.d(TAG,"LiveSDK > handleInfo > Vender ID    [" + info.getVendorID() +"]");
        Log.d(TAG,"LiveSDK > handleInfo > Total Memory [" + info.getTotalMemory() +"]");

        WritableMap params = Arguments.createMap();
        params.putDouble(EVENT_PARAM_KEY_INFO_PEN_ID, info.getPenID());
        params.putString(EVENT_PARAM_KEY_INFO_ADDRESS, info.getBluetoothAddress());
        params.putString(EVENT_PARAM_KEY_INFO_FIRMWARE_VERSION, info.getFirmwareVersion());
        params.putDouble(EVENT_PARAM_KEY_INFO_PID, info.getPenID());
        params.putDouble(EVENT_PARAM_KEY_INFO_VID, info.getVendorID());
        params.putDouble(EVENT_PARAM_KEY_INFO_TOTAL_MEMORY, info.getTotalMemory());
        sendEventToClient(reactContext, EVENT_PEN_INFO, params);

    }

    @Override
    public void handleNotification(Notification notification) {

        Log.d(TAG,"LiveSDK > handleNotification ");
        String notificationJsonData = notification.getNotificationJsonData();
        JsonNotification jsonNotification = gson.fromJson(notificationJsonData, JsonNotification.class);
        Log.e(TAG, "handleNotification: " + notification.toString() );


        if (jsonNotification != null  && jsonNotification.isDrawingElement()) {

            WritableMap params = Arguments.createMap();

            params.putInt("Type", jsonNotification.getMessageType());
            params.putDouble("PenId", jsonNotification.getPenId());
            params.putDouble("Version", jsonNotification.getVersion());
            params.putDouble("Time", jsonNotification.getTimestamp());
            params.putDouble("Address", jsonNotification.getPageAddress());
            params.putInt("CoordX", jsonNotification.getX());
            params.putInt("CoordY", jsonNotification.getY());
            params.putInt("Force", jsonNotification.getPressure());

            sendEventToClient(reactContext, EVENT_STREAMING, params);
        } else {
//            this.setFirstStroke(false);
//            addEvent(Event.EventEnum.ENotification);
        }
    }

    @Override
    public void log(String s) {

    }

    @Override
    public void handlePairedDevicesWithPen(ArrayList<PairedDevice> arrayList) {
        Log.d(TAG,"LiveSDK > handlePairedDevicesWithPen");
        for (PairedDevice device:arrayList) {
            Log.d(TAG,"LiveSDK > handlePairedDevicesWithPen > Name [" + device.getName()+"] Address [" + device.getMac() +"]");

            WritableMap params = Arguments.createMap();
            params.putString(EVENT_PARAM_KEY_DEVICE_NAME, device.getName());
            params.putString(EVENT_PARAM_KEY_DEVICE_ADDRESS, device.getMac());
            sendEventToClient(reactContext, EVENT_STATUS_BLUETOOTH_PAIRED_DEVICE, params);

        }
    }

    @Override
    public void handleArchiveInfo(ArrayList<ArchiveInfo> arrayList) {

        Log.d(TAG,"LiveSDK > handleArchiveInfo");

    }
}
