package tpd.dmx_control.effects;

import tpd.dmx_control.device.LightDevice;

public abstract class DmxEffect implements Runnable {
    
    protected LightDevice device ; // Device to control
    protected volatile boolean running = false; // Effect state
    private Thread workerThread; // Thread for the effect

    // Effect speed (waiting time in ms between each step)
    protected long speedMs = 50;

    public DmxEffect(LightDevice device, long speedMs) {
        this.device = device;
        this.speedMs = speedMs;
    }

    /**
     * Start effect in a separate thread.
     */
    public void start() {
        if (!running) {
            running = true;
            workerThread = new Thread(this);
            workerThread.setDaemon(true); // Stop if main app stops
            workerThread.start();
        }
    }

    /**
     * Stop properly the effect.
     */
    public void stop(){
        running = false;
        if (workerThread != null) {
            try {
                // 1. Run effect specific logic;
                step();

                // 2. Wait for thread to finish
                Thread.sleep(speedMs);
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }

    /**
     * Abstract Method : the creative logic
     * Each effect must implement its own step.
     */
    protected abstract void step();

    public boolean isRunning() {
        return running;
    }
}
