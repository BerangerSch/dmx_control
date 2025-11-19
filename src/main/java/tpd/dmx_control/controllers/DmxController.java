package tpd.dmx_control.controllers;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Arrays;

public class DmxController implements Runnable {

    private SerialPort serialPort;
    private final byte[] dmxData = new byte[512]; // DMX512 environment supports 512 channels
    private boolean running = false;
    private Thread outputThread;

    // Configuration standard DMX
    private static final int BAUD_RATE = 250000;

    /**
     * Connects to the USB serial port
     * @param portName The name of the serial port (e.g., "/dev/ttyUSB0" on Linux or "/dev/cu.usbserial..." on macOS)
     * @return true if connection is successful, false otherwise
     */
    public boolean connect(String portName) {
        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(BAUD_RATE);
        serialPort.setNumDataBits(8);
        serialPort.setNumStopBits(2); // DMX uses 2 stop bits
        serialPort.setParity(SerialPort.NO_PARITY);

        // To avoid latency issues
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_NONBLOCKING, 0, 0);

        if (serialPort.openPort()) {
            System.out.println("Connecté au port : " + portName);
            startTransmission();
            return true;
        } else {
            System.out.println("Échec de la connexion au port : " + portName);
            return false;
        }
    }

    /**
     * Starts data transmission loop
     */
    private void startTransmission() {
        running = true;
        outputThread = new Thread(this);
        outputThread.setPriority(Thread.MAX_PRIORITY); // High priority for signal stability
        outputThread.start();
    }

    /**
     * Stops data transmission and close the port
     */
    public void stop() {
        running = false;
        if (outputThread != null) {
            try {
                outputThread.join(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
        }
        System.out.println("DMX Controller arrêté.");
    }

    /**
     * Update DMX channel value
     * @param channel DMX channel (1-512)
     * @param value Value to set (0-255)
     */
    public void setChannel(int channel, int value) {
        if (channel >= 1 && channel <= 512) {
            // Java byte is signed (-128 - 127, but DMX expects 0 - 255).
            // We stock brute data, the cast (byte) deals with binary conversion automatically.
            // Make sure int value is between 0 and 255.
            dmxData[channel - 1] = (byte) value;
        }
    }
    
    /**
     * Reinitialize all lights to 0 (Blackout)
     */
    public void blackOut() {
        Arrays.fill(dmxData, (byte) 0);
    }

    /**
     * The main loop generating DMX512 signal
     */
    @Override
    public void run() {
        // Temporary buffer : Start Code (1 byte) + 512 channels
        byte[] packet = new byte[513];
        packet[0] = 0x00; // DMX Start Code (always 0 for dimmers/lights)

        while (running) {
            try {
                // 1. Simulate BREAK signal (low line)
                // Break must lasts at least 88µs
                // setBreak() put  TX line to 0.
                serialPort.setBreak();
                Thread.sleep(1); // 1ms is enough (and minimum reliable in standard Java)

                // 2. End of BREAK (Mark After Break - MAB)
                // The line goes back to high state before sending data
                serialPort.clearBreak();
                // A small delay (MAB) is ofter required, but Java running time may handle it.
                // Thread.sleep(0, 10000); // Optional 10µs if needed

                // 3. Data preparation
                // We copy current data to the packet buffer (thread-safe basicly)
                System.arraycopy(dmxData, 0, packet, 1, 512);
            
                // 4. Send complete DMX packet (Start Code + 512 chanels)
                serialPort.writeBytes(packet, packet.length);

                // 5. Wait before next frame (to reach ~30-40 Hz)
                // A complete frame takes ~23ms to send at 250k bauds.
                // We add a small delay to avoid saturating the line.
                Thread.sleep(20);
            } catch (Exception e) {
                e.printStackTrace();
                running = false;
            }
        }
    }
}
