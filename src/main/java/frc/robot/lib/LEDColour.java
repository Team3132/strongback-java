package frc.robot.lib;

import frc.robot.Constants;

public enum LEDColour {
    RED(255, 0, 0),
    YELLOW(255, 255, 0),
    BLUE(0, 0, 255),
    GREEN(0, 255, 0),
    ORANGE(255, 122, 0),
    MAGENTA(255, 0, 255),
    WHITE(255, 255, 255),
    PURPLE(100, 50, 155),
    CYAN(0, 255, 255),
    BROWN(150, 75, 0);

    public final int r;
    public final int g;
    public final int b;

    LEDColour(int r, int g, int b) {
        this.r = (int)(r * Constants.LED_BRIGHTNESS_PERCENTAGE);
        this.g = (int)(g * Constants.LED_BRIGHTNESS_PERCENTAGE);
        this.b = (int)(b * Constants.LED_BRIGHTNESS_PERCENTAGE);
    }
}