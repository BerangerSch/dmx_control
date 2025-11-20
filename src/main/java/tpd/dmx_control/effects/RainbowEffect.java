package tpd.dmx_control.effects;

import javafx.scene.paint.Color;
import tpd.dmx_control.device.LightDevice;

public class RainbowEffect extends DmxEffect {
    
    private double currentHue = 0.0;
    private double hueStep = 2.0; // Color rotation speed

    public RainbowEffect(LightDevice device) {
        super(device, 100); // Default speed 100ms
    }

    @Override
    protected void step() {
        // 1. Calculate next color in the rainbow
        currentHue = (currentHue + hueStep) % 360.0;

        // 2. Create color
        Color nextColor = Color.hsb(currentHue, 1.0, 1.0);

        // 3. Apply color to device
        device.setColor(nextColor);
        device.setDimmer(255); // Ensure full brightness
    }

    @Override
    public void run() {
        step();
    }
}
