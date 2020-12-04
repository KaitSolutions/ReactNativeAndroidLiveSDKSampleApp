import React, { Component } from 'react';
import { ScrollView } from 'react-native';
import ListViewItem from './ListViewItem';
import Device from './models/Device';


export default class ListView extends Component<Props> {
    constructor(props: Props) {
        super(props);

        this.state = {
            devices: this.props.devices
        };
    }

    showDeviceLists() {
        let result;
        result = this.state.devices.map((device: Device, key: any) =>
            <ListViewItem device={device} key={key} updateFoundDevice={this.props.updateFoundDevice}/>
        );

        return result;
    }
   
    // addDevice(device: Device) {
    //     let result;
    //     result = this.state.devices.map((device: Device, key: any) =>
    //         <ListViewItem device={device} key={key} updateFoundDevice={this.props.updateFoundDevice}/>
    //     );
    //     return result;
    // }

    render() {
        return (
            <ScrollView>
                {this.showDeviceLists()}
            </ScrollView>
        )
    }
}