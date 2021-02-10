import React, { Component } from "react";
import { StyleSheet, Button, View, SafeAreaView, Text, Alert, ScrollView, TouchableOpacity, TouchableHighlight } from 'react-native';
import { NativeEventEmitter, NativeModules } from 'react-native';
//import styles from 'react-native';
import { ListDevices } from './components/listview/data/MockListHeroes';
import PairedListView from './components/listview/PairedListView';
import ListView from './components/listview/ListView';
import Device  from './components/listview/models/Device';


// Event Type
const EVENT_STATUS_BLUETOOTH_DEVICE_FOUND = "EventBluetoothDeviceFound";
const EVENT_STATUS_BLUETOOTH_PAIRED_DEVICE = "EventBluetoothPairedDevice";
const EVENT_STATUS_BLUETOOTH = "EventBluetoothStatus";
const EVENT_PEN_SETTING      = "EventPenSetting";
const EVENT_PEN_STATUS       = "EventPenStatus";
const EVENT_PEN_INFO         = "EventPenInfo";
const EVENT_STREAMING        = "EventStreaming";
const EVENT_PAGE_TEMPLATE    = "EventPageTemplate";
const EVENT_LOG              = "EventLog";

// Event Parameter Key
const EVENT_PARAM_KEY_TYPE           = "Type";
const EVENT_PARAM_KEY_MESSAGE        = "Message";
const EVENT_PARAM_KEY_DEVICE_NAME    = "Name";
const EVENT_PARAM_KEY_DEVICE_ADDRESS = "Address"; 

// Event Parameter Key - Setting
const EVENT_PARAM_KEY_SETTING_ACTIVATION_STATE = "SettingActivationState";
const EVENT_PARAM_KEY_SETTING_BLUETOOTH_ON = "SettingBluetoothOn";
const EVENT_PARAM_KEY_SETTING_DISCONNECT_TIME = "SettingDisconnectTime";
const EVENT_PARAM_KEY_SETTING_DISCOVERABLE = "SettingDiscoverable";
const EVENT_PARAM_KEY_SETTING_LEFTHAND = "SettingLeftHand";
const EVENT_PARAM_KEY_SETTING_MUTE = "SettingMute";
const EVENT_PARAM_KEY_SETTING_PEN_NAME = "SettingPenName";
const EVENT_PARAM_KEY_SETTING_NOTIFY_MEMORY_THRESHHOLD = "SettingNotifyMemoryThreshHold";
const EVENT_PARAM_KEY_SETTING_PENDOWN_CON = "SettingPenDownCon";
const EVENT_PARAM_KEY_SETTING_PEN_TIME = "SettingPenTime";
const EVENT_PARAM_KEY_SETTING_PEN_ID = "SettingPenId";

// Event Parameter Key - Status
const EVENT_PARAM_KEY_STATUS_BATTERY = "StatusBattery";
const EVENT_PARAM_KEY_STATUS_MEMORYFULL = "StatusMemoryFull";
const EVENT_PARAM_KEY_STATUS_USED_MEMORY = "StatusUsedMemory";
const EVENT_PARAM_KEY_STATUS_ENCON = "StatusEncOn";
const EVENT_PARAM_KEY_STATUS_PENID = "StatusPenId";

// Event Parameter Key - Info
const EVENT_PARAM_KEY_INFO_ADDRESS = "InfoAddress";
const EVENT_PARAM_KEY_INFO_PEN_ID = "InfoPenID";
const EVENT_PARAM_KEY_INFO_FIRMWARE_VERSION = "InfoFirmwareVersion";
const EVENT_PARAM_KEY_INFO_PID = "InfoPID";
const EVENT_PARAM_KEY_INFO_VID = "InfoVID";
const EVENT_PARAM_KEY_INFO_TOTAL_MEMORY = "InfoTotalMemory";

// Event Parameter Key - Page Template
const EVENT_PARAM_KEY_PAGE_TEMPLATE_WIDTH = "PageWidth";
const EVENT_PARAM_KEY_PAGE_TEMPLATE_HEIGHT = "PageHeight";
const EVENT_PARAM_KEY_PAGE_TEMPLATE_PAGENUMBER = "PageNumber";

// Bluetooth Status Type
const BLUETOOTH_STATUS_IDLE             = 0;
const BLUETOOTH_STATUS_ENABLING         = 1;
const BLUETOOTH_STATUS_ENABLED          = 2;
const BLUETOOTH_STATUS_DISABLED         = 3;
const BLUETOOTH_STATUS_DISCOVERING      = 4;
const BLUETOOTH_STATUS_DISCOVER_DONE    = 5;
const BLUETOOTH_STATUS_READY_TO_PAIR    = 6;
const BLUETOOTH_STATUS_PAIRING          = 7;
const BLUETOOTH_STATUS_PAIRING_ENDED    = 8;
const BLUETOOTH_STATUS_UNABL_BOND       = 9;
const BLUETOOTH_STATUS_BONDED           = 10;
const BLUETOOTH_STATUS_WAIT_FOR_CONNECT = 11;
const BLUETOOTH_STATUS_CONNECTED        = 12;
const BLUETOOTH_STATUS_DISCONNECTED     = 13;
const BLUETOOTH_STATUS_PAIRED_DEVICE    = 14;

// Streaming Event Type
const TYPE_NEWSESSION                   = 2;
const TYPE_COORD                        = 3;
const TYPE_PENUP                        = 4;
const TYPE_ACTIONAREA                   = 5;
const TYPE_PENDOWN                      = 6;
const TYPE_COORD_WITH_TIMESTAMP         = 7;


const styles = StyleSheet.create({
    container: {
      flex: 1,
      justifyContent: 'center',
      margin: 2,
    },
    menuLayout: {
        flex: 1,
        flexDirection: 'column',
        justifyContent: 'flex-start',
        backgroundColor: 'white',
    },
    containerButton: {
        margin: 2,
    },
    contentsLayout: {
        flex: 3.5,
        flexDirection: 'column',
        justifyContent: 'flex-start',
        margin: 2,
        backgroundColor: 'white'
      },
    contentTitle: {
        margin: 0,
        justifyContent: 'center',
        backgroundColor: 'skyblue',
        height: 40,
    },
    contentDeviceListLayout: {
        flex: 2,
        margin: 2,
        backgroundColor: 'white'
    },
    contentConnectedDeviceLayout: {
        flex: 0.5,
        margin: 0,
        backgroundColor: 'skyblue'
    },
    contentLogLayout: {
        flex: 7,
        margin: 2,
        backgroundColor: 'white'
      },

    logItem: {
        fontSize: 11,
        backgroundColor: 'white'
      },

    title: {
        fontSize: 14,
        fontWeight: 'bold',
        textAlign: 'center',
        backgroundColor: 'skyblue'
    },
    bigTitle: {
        textAlign: 'center',
        marginVertical: 10,
        fontSize: 15,
        fontWeight: 'bold',
        backgroundColor: 'skyblue'
    },
    button: {
        height: 60,
        color: "#394f66",
        alignContent: 'center',
        backgroundColor: "#394f66",
        justifyContent: "center",
        alignItems: "center"
      },
    text: {
        color: "#ffffff"
    }
  });


const initialState = {
    pairableDevices: [],
    log:""
}

var pageAddress = 0;

export default class App extends Component {

    constructor() {
        super();

         this.state = {
            pairedDevices: [],
            pairableDevices: [],
            connectedDevice: "Disconnected",
            log:""
         };

         this.baseState = this.state;
    }

    
    componentDidMount() {

        //-----------------------------------------------------------
        // EVENT_STATUS_BLUETOOTH
        //-----------------------------------------------------------
        const BluetoothStatusEmitter = new NativeEventEmitter(NativeModules.PenManager);
        this.eventListener = BluetoothStatusEmitter.addListener(EVENT_STATUS_BLUETOOTH, (event) => {
            const message = '# Event > Type [' + event.Type + '] ' + event.Message;
            console.log(message);

            // Update log 
            let log = this.state.log;
            if(log.length > 5000) {
            log = "";
            }
            log = log + '\n' + message;
            this.setState({ log }); 

            if(event.Type === BLUETOOTH_STATUS_CONNECTED) {
                console.log('# Connect Status: ' + 'Name: ' + event.Name + ' Address: ' + event.Address );
                let connectedDevice = this.state.connectedDevice;
                connectedDevice = event.Name;
                this.setState({ connectedDevice });
            } else if(event.Type === BLUETOOTH_STATUS_DISCONNECTED) {
                let connectedDevice = this.state.connectedDevice;
                connectedDevice = "Disconnected";
                this.setState({ connectedDevice });
            }
        });

        //-----------------------------------------------------------
        // EVENT_STATUS_BLUETOOTH_DEVICE_FOUND
        //-----------------------------------------------------------
        this.updatePairedDeviceEvent = new NativeEventEmitter(NativeModules.PenManager);
        this.updatePairedDeviceEvent.addListener(EVENT_STATUS_BLUETOOTH_PAIRED_DEVICE, (event) => this.updatePairedDevice(event));

        //-----------------------------------------------------------
        // EVENT_STATUS_BLUETOOTH_DEVICE_FOUND
        //-----------------------------------------------------------
        this.updatePairableDeviceEvent = new NativeEventEmitter(NativeModules.PenManager);
        this.updatePairableDeviceEvent.addListener(EVENT_STATUS_BLUETOOTH_DEVICE_FOUND, (event) => this.updateFoundDevice(event));

        //-----------------------------------------------------------
        // EVENT_PEN_SETTING
        //-----------------------------------------------------------
        const PenSettingEmitter = new NativeEventEmitter(NativeModules.PenManager);
        this.eventListener = PenSettingEmitter.addListener(EVENT_PEN_SETTING, (event) => {
            // console.log('# Setting > ActivationState > ' + event.SettingActivationState);
            // console.log('# Setting > BluetoothOn     > ' + event.SettingBluetoothOn);
            // console.log('# Setting > DisconnectTime  > ' + event.SettingDisconnectTime);
            // console.log('# Setting > Discoverable    > ' + event.SettingDiscoverable);
            // console.log('# Setting > LeftHand        > ' + event.SettingLeftHand);
            // console.log('# Setting > Mute            > ' + event.SettingMute);
            // console.log('# Setting > PenTime         > ' + event.SettingPenTime);
            // console.log('# Setting > PenId           > ' + event.SettingPenId);
            let message = '# Setting > ActivationState  [ ' + event.SettingActivationState + ' ]\n'
                        + '# Setting > BluetoothOn  [ ' + event.SettingBluetoothOn + ' ]\n'
                        + '# Setting > DisconnectTime  [ ' + event.SettingDisconnectTime + ' ]\n'
                        + '# Setting > Discoverable  [ ' + event.SettingDiscoverable + ' ]\n'
                        + '# Setting > LeftHand [ ' + event.SettingLeftHand + ' ]\n'
                        + '# Setting > Mute  [ ' + event.SettingMute + ' ]\n'
                        + '# Setting > PenTime  [ ' + event.SettingPenTime + ' ]\n'
                        + '# Setting > PenId  [ ' + event.SettingPenId + ' ]\n'
                        + '# Setting > ---------------------------------------------------------------\n';
            
            // Update log 
            let log = this.state.log;
            if(log.length > 5000) {
               log = "";
            }
            log = log + '\n' + message;
            this.setState({ log }); 
        });

        //-----------------------------------------------------------
        // EVENT_PEN_STATUS
        //-----------------------------------------------------------
        const PenStatusEmitter = new NativeEventEmitter(NativeModules.PenManager);
        this.eventListener = PenStatusEmitter.addListener(EVENT_PEN_STATUS, (event) => {
            // console.log('# Status > PenID [ ' + event.StatusPenId);
            // console.log('# Status > Battery [ ' + event.StatusBattery);
            // console.log('# Status > MemoryFull [ ' + event.StatusMemoryFull);
            // console.log('# Status > UsedMemory [ ' + event.StatusUsedMemory);
            // console.log('# Status > StatusEncOn [ ' + event.StatusEncOn);

            let message = '# Status > PenID  [ ' + event.StatusPenId + '] \n'
                        + '# Status > Battery  [ ' + event.StatusBattery + '] \n'
                        + '# Status > MemoryFull  [ ' + event.StatusMemoryFull + '] \n'
                        + '# Status > UsedMemory  [ ' + event.StatusUsedMemory + '] \n'
                        + '# Status > StatusEncOn  [ ' + event.StatusEncOn + '] \n'
                        + '# Status > ---------------------------------------------------------------\n';
            // Update log 
            let log = this.state.log;
            if(log.length > 5000) {
               log = "";
            }
            log = log + '\n' + message;
            this.setState({ log }); 
        });

        //-----------------------------------------------------------
        // EVENT_PEN_INFO
        //-----------------------------------------------------------
        const PenInfoEmitter = new NativeEventEmitter(NativeModules.PenManager);
        this.eventListener = PenInfoEmitter.addListener(EVENT_PEN_INFO, (event) => {
            // console.log('# Info > InfoAddress         > ' + event.InfoAddress);
            // console.log('# Info > InfoPenID           > ' + event.InfoPenID);
            // console.log('# Info > InfoFirmwareVersion > ' + event.InfoFirmwareVersion);
            // console.log('# Info > PID                 > ' + event.InfoPID);
            // console.log('# Info > VID                 > ' + event.InfoVID);
            // console.log('# Info > TotlaMemory         > ' + event.InfoTotalMemory);            

            let message = '# Info > InfoAddress  [' + event.InfoAddress + '] \n'
                        + '# Info > InfoPenID  [' + event.InfoPenID + '] \n'
                        + '# Info > InfoFirmwareVersion  [' + event.InfoFirmwareVersion + '] \n'
                        + '# Info > PID  [ ' + event.InfoPID + '] \n'
                        + '# Info > VID  [ ' + event.InfoVID + '] \n'
                        + '# Info > TotlaMemory  [' + event.InfoTotalMemory+ '] \n'
                        + '# Info > ---------------------------------------------------------------\n';

            // Update log 
            let log = this.state.log;
            if(log.length > 5000) {
               log = "";
            }
            log = log + '\n' + message;
            this.setState({ log }); 
        });
        
        //-----------------------------------------------------------
        // EVENT_LOG
        //-----------------------------------------------------------
        const EventLogEmitter = new NativeEventEmitter(NativeModules.PenManager);
        this.eventListener = EventLogEmitter.addListener(EVENT_LOG, (event) => {
            console.log('# Log > '+ event.Message);
            
            // Update log 
            let log = this.state.log;
            if(log.length > 5000) {
               log = "";
            }
            log = log + '\n' + event.Message;
            this.setState({ log }); 
        });

        //-----------------------------------------------------------
        // EVENT_STREAMING
        //-----------------------------------------------------------
        const PenStreamingEmitter = new NativeEventEmitter(NativeModules.PenManager);
        this.eventListener = PenStreamingEmitter.addListener(EVENT_STREAMING, (event) => {

            var currentPageAddress;

            console.log('# Streaming: Type: ' + event.Type + ' PenId:' + event.PenId + ' Version: ' + event.Version + ' Time: ' + event.Time + ' Address: ' + event.Address + ' CoordX: ' + event.CoordX + ' CoordY: ' + event.CoordY + ' Force: ' + event.Force);
           
            var streaming;
            if(event.Type === TYPE_NEWSESSION) {
                streaming =  "# NewSession > T: " + event.Time + " A: " + event.Address + " X: " + event.CoordX + " Y: " + event.CoordY + " F: " + event.Force;
            } else if(event.Type === TYPE_PENDOWN) {
                streaming =  "# PenDown > T: " + event.Time + " A: " + event.Address;
                currentPageAddress = event.Address;
                // Get Page Template
                if(pageAddress == 0 || pageAddress != currentPageAddress) {
                    console.log("# Log > Get Page Template for address " + currentPageAddress);
                    this.getPageTemplate(currentPageAddress);
                    pageAddress = currentPageAddress;
                }    
            } else if(event.Type === TYPE_COORD) {
                streaming =  "# Coord > X: " + event.CoordX + " Y: " + event.CoordY + " F: " + event.Force;
            } else if(event.Type === TYPE_COORD_WITH_TIMESTAMP) {
                streaming =  "# Coord > T: " + event.Time + " A: " + event.Address + " X: " + event.CoordX + " Y: " + event.CoordY + " F: " + event.Force;
            } else if(event.Type === TYPE_PENUP) {
                streaming =  "# Penup > T: " + event.Time + " A: " + event.Address;
            } else if(event.Type === TYPE_ACTIONAREA) {
                streaming =  "# ActionArea > T: " + event.Time + " A: " + event.Address + " X: " + event.CoordX + " Y: " + event.CoordY + " F: " + event.Force;
            } else {
                streaming =  "# "+ event.Type + " > T: " + event.Time + " A: " + event.Address + " X: " + event.CoordX + " Y: " + event.CoordY + " F: " + event.Force;
            }
        

            let log = this.state.log;
            if(log.length > 5000) {
                log = "";
            }
            log = log + '\n' + streaming;
            this.setState({ log }); 
        });

        //-----------------------------------------------------------
        // EVENT_PAGE_TEMPLATE
        //-----------------------------------------------------------
        const PageTemplateEmitter = new NativeEventEmitter(NativeModules.PenManager);
        this.eventListener = PageTemplateEmitter.addListener(EVENT_PAGE_TEMPLATE, (event) => {
            console.log('# Page: Type: ' + event.Type + ' PageWidth:' + event.PageWidth + ' PageHeight: ' + event.PageHeight + ' PageNumber: ' + event.PageNumber);

            let pageTemplate = '# Page > ---------------------------------------------------------------\n'
                             + '# Page > Width [' + event.PageWidth + '] \n'
                             + '# Page > Height [' + event.PageHeight + '] \n'
                             + '# Page > Page Number [' + event.PageNumber + '] \n'
                             + '# Page > ---------------------------------------------------------------';

            let log = this.state.log;
            if(log.length > 5000) {
                log = "";
            }
            log = log + '\n' + pageTemplate;
            this.setState({ log }); 
        });

        //-----------------------------------------------------------
        // Start SDK
        //-----------------------------------------------------------
        NativeModules.PenManager.startSDK();
    }

    componentWillUnmount() {
        this.eventListener.remove(); //Removes the listener
    }
   
    //
    //  Start SDK
    //
    doStartSDK = () => {
        this.resetPairedDevice();
        this.resetPairableDevice();
        //NativeModules.PenManager.startSDK();
        NativeModules.PenManager.getPairedDevices();
    }

    //
    //  Stop SDK
    //
    doStopSDK = () => {
        this.resetPairedDevice();
        this.resetPairableDevice();
        NativeModules.PenManager.stopSDK();
    }

    //
    // Update the paired device list
    //
    updatePairedDevice = (event) => {

        console.log('> Paired Device Name: ' + event.Name + ' Address: ' + event.Address); 

         let pairedDevices = this.state.pairedDevices;
         let newDevice = new Device(event.Name, event.Address);

         console.log('updatePairedDevice > devices length: ' + pairedDevices.length);

        if(pairedDevices && newDevice) {
            const count = pairedDevices.length;
            var isNewDevice = true;
            for(let i = 0; i< count; i++) {
                if(newDevice.address === pairedDevices[i].address) { 
                    isNewDevice = false;
                    break;
                }
            }
            if(isNewDevice) {
                pairedDevices.push(newDevice);
                this.setState({ pairedDevices });
            }
        } else {
            console.log('unexpected error');
        }
    }

    //
    // Update the pairable device list
    //
    updateFoundDevice = (event) => {

        console.log('> Found Device Name: ' + event.Name + ' Address: ' + event.Address); 

         let pairableDevices = this.state.pairableDevices;
         let newDevice = new Device(event.Name, event.Address);

         console.log('updateFoundDevice > devices length: ' + pairableDevices.length);

        if(pairableDevices && newDevice) {
            const count = pairableDevices.length;
            var isNewDevice = true;
            for(let i = 0; i< count; i++) {
                if(newDevice.address === pairableDevices[i].address) {
                    isNewDevice = false;
                    break;
                }
            }
            if(isNewDevice) {
                pairableDevices.push(newDevice);
                this.setState({ pairableDevices });
            }
        } else {
            console.log('unexpected error');
        }
    }

    //
    //  reset the paired Devices listview
    //
    resetPairedDevice = () => {
        var baseState = this.state.pairedDevices.splice(0,this.state.pairedDevices.length);
        this.setState({ baseState });
    }

    //
    //  reset pairable Devices listview
    //
    resetPairableDevice = () => {
        var baseState = this.state.pairableDevices.splice(0,this.state.pairableDevices.length);
        this.setState({ baseState });
    }

    //
    //  scan a pen
    //
    doScanDevice = () => {
        this.resetPairableDevice();
        NativeModules.PenManager.doScanDevice();
    }

    //
    //  get the page template information (width and height)
    //
    getPageTemplate = (pageAddress) => {
        console.log("# Log > getPageTemplate for " + pageAddress);
        NativeModules.PenManager.getPageTemplate(pageAddress);
        console.log("# Log > end getPageTemplate for " + pageAddress);
    }
    
    //
    //  UI Renderer
    //

    render() {
        return (
            <View style={{flex: 1, flexDirection: 'row'}}>

                {/* Buttons Layout  */}
                
                <View style={styles.menuLayout}>
                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => this.doStartSDK()}>
                            <Text style={styles.text}>Start SDK</Text>
                        </TouchableOpacity>  
                    </View>
                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => this.doStopSDK()}>
                            <Text style={styles.text}>Stop SDK</Text>
                        </TouchableOpacity>  
                    </View>
                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => this.doScanDevice()}>
                            <Text style={styles.text}>Scan</Text>
                        </TouchableOpacity>  
                    </View>
                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => NativeModules.PenManager.doDisconnect()}>
                            <Text style={styles.text}>Disconnect</Text>
                        </TouchableOpacity>                    
                    </View>

                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => NativeModules.PenManager.requestSettings()}>
                            <Text style={styles.text}>Pen Setting</Text>
                        </TouchableOpacity>
                    </View>
              
                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => NativeModules.PenManager.requestStatus()}>
                            <Text style={styles.text}>Pen Status</Text>
                        </TouchableOpacity>
                    </View>

                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => NativeModules.PenManager.requestInfo()}>
                            <Text style={styles.text}>Pen Info</Text>
                        </TouchableOpacity>
                    </View>

                    <View style={styles.containerButton}>
                        <TouchableOpacity activeOpacity={0.8} style={styles.button} onPress={() => NativeModules.PenManager.requestReset()}>
                            <Text style={styles.text}>Pen Reset</Text>
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Contents (Bluetoot list & Log ) Layout  */}

                <View style={styles.contentsLayout}>

                    {/* Paired Device */}
                    <View style={styles.contentTitle}>
                        <Text style={styles.title}>
                            {"Paired Devices"}
                        </Text>
                    </View>

                    <View style={styles.contentDeviceListLayout}>
                        <ScrollView >
                            <PairedListView devices={this.state.pairedDevices} updatePairedDevice={this.updatePairedDevice} resetDevices={this.resetPairedDevice} />
                        </ScrollView>
                    </View>

                    {/* Pairable Device */}
                    <View style={styles.contentTitle}>
                        <Text style={styles.title}>
                            {"Pairable Devices"}
                        </Text>
                    </View>

                    <View style={styles.contentDeviceListLayout}>
                        <ScrollView >
                            <ListView devices={this.state.pairableDevices} updateFoundDevice={this.updateFoundDevice} resetDevices={this.resetPairableDevice} />
                        </ScrollView>
                    </View>


                    <View style={styles.contentConnectedDeviceLayout}>
                        <Text style={styles.bigTitle}>
                            {this.state.connectedDevice}
                        </Text>
                    </View>

                    <View style={styles.contentLogLayout}>
                        <ScrollView style={styles.contentLogLayout}
                                ref={ref => {this.scrollView = ref}}
                                onContentSizeChange={() => this.scrollView.scrollToEnd({animated: true})}> 
                            <Text style={styles.logItem}>
                                {this.state.log}
                            </Text>
                        </ScrollView>
                    </View>
                </View>
            </View>
        );
    }
}
