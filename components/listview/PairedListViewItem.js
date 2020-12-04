import React, { Component } from 'react';
import { StyleSheet, TouchableOpacity, View, Text } from 'react-native';
import { NativeEventEmitter, NativeModules } from 'react-native';
//import { withNavigation } from 'react-navigation';

export default class PairedListViewItem extends Component<Props> {
    constructor(props: Props) {
        super(props);

        this.state = {
            device: this.props.device // we pass device through props
        };
    }

    doPairDevice = () => {
        console.log('$ call pairDevice > Address: ' + this.state.device.address);
        NativeModules.PenManager.doPairDevice(this.state.device.address);
     }

    render() {
        return (
            <TouchableOpacity style={styles.listViewItemContainer}>
                <View style={styles.info}>
                    <Text style={styles.heroName}>{this.state.device.name}</Text>
                    <Text style={styles.text}>{this.state.device.address}</Text>
                </View>
            </TouchableOpacity>
        )
    }
}

//export default withNavigation(ListViewItem);

const styles = StyleSheet.create({
    listViewItemContainer: {
        flexDirection: 'row',
        margin: 5
    },
    info: {
        justifyContent: 'center',
        marginLeft: 5
    },
    text: {
        fontSize: 12
    },
    heroName: {
        fontSize: 12,
        fontWeight: 'bold'
    }
});