package tpd.dmx_control.device;

import javafx.scene.paint.Color;
import tpd.dmx_control.controllers.DmxController;

public class StairvilleLedBar extends LightDevice {

    //Material configuration (Channel mapping in 5 channels mode)
    private static final int OFF_RED = 0;
    private static final int OFF_GREEN = 1;
    private static final int OFF_BLUE = 2;
    private static final int OFF_DIMMER = 3;
    private static final int OFF_STROBE = 4;
    
    public StairvilleLedBar(int startAddress, DmxController controller) {
        super(startAddress, controller);
        // Default initialization
        setDimmer(255);
        setStrobeSpeed(0);
    }

    @Override
    public void setColor(Color color) {
        // Translation : Object Color JavaFx -> Phyical DMX Channels
        setChannel(OFF_RED, (int) (color.getRed() * 255));
        setChannel(OFF_GREEN, (int) (color.getGreen() * 255));
        setChannel(OFF_BLUE, (int) (color.getBlue() * 255));
    }

    @Override
    public void setDimmer(int value) {
        setChannel(OFF_DIMMER, value);
    }

    @Override
    public void setStrobeSpeed(int value) {
        setChannel(OFF_STROBE, value);
    }
}