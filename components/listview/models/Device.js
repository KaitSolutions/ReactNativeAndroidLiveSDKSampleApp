export default class Device {
    name: string;
    address: string;
    
    constructor(
        name: string,
        address: string
    ) {
        this.name = name;
        this.address = address;
    }

    updateInfo(device: Device) {
        if (device) {
            this.name = device.name;
            this.address = device.address;
        }
    }

    clone() {
        return new Device(this.name, this.address);
    }
}