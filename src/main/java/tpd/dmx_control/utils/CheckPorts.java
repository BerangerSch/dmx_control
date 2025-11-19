package tpd.dmx_control.utils;

import com.fazecast.jSerialComm.SerialPort;

public class CheckPorts {
    public static SerialPort[] listAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        System.out.println("Available Serial Ports:");
        for (SerialPort port : ports) {
            System.out.println(" - " + port.getSystemPortName() + ": " + port.getDescriptivePortName());
        }
        return ports;
    }
}
