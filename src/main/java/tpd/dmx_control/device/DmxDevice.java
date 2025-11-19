package tpd.dmx_control.device;

import tpd.dmx_control.controllers.DmxController;

public abstract class DmxDevice {
    
    protected int startAddress;
    protected DmxController controller;

    public DmxDevice(int startAddress, DmxController controller) {
        this.startAddress = startAddress;
        this.controller = controller;
    }

    /**
     * Send a value on a relative channel to the device.
     * @param offset Offset (10 = 1st channel of the device)
     * @param value Value (0-255)
     */
    protected void setChannel(int offset, int value) {
        // The real DMX address = Start address + offset
        // E.g. : If the device starts at 5, the RED channel (offset 0) is at address 5
        controller.setChannel(this.startAddress + offset, value);
    }

    public int getStartAddress() {
        return startAddress;
    }
}
