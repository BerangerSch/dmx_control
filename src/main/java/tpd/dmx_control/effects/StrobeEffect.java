package tpd.dmx_control.effects;

import tpd.dmx_control.device.LightDevice;

public class StrobeEffect extends DmxEffect {

    private boolean on = false;

    public StrobeEffect(LightDevice device, long speedMs) {
        super(device, speedMs);
    }

    @Override
    protected void step () {
        on = !on;

        if (on) {
            device.setDimmer(255); // Full brightness
        } else {
            device.setDimmer(0); // Off
        }
    }
    
    @Override
    public void run() {
        step();
    }
}