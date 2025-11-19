package tpd.dmx_control.device;

import javafx.scene.paint.Color;
import tpd.dmx_control.controllers.DmxController;

public abstract class LightDevice extends DmxDevice {
    
    public LightDevice(int startAddress, DmxController controller) {
        super(startAddress, controller);
    }

    // -- COMMON CONTRACT TO ALL LIGHT DEVICES --

    /**
     * Define the color of the light.
     * Every device will translate this color to its own channels.
     * @param color Color to set (JavaFX Color)
     */
    public abstract void setColor(Color color);

    /**
     * Define the intensity of the light (Master Dimmer).*
     * @param  value 0-255
     */
    public abstract void setDimmer(int value);

    /***
     * Define the strobe speed of the light.
     * @param value 0 (Off) - 255 (Max)
     */
    public abstract void setStrobeSpeed(int value);
}
