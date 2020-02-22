package frc.robot.lib;

public enum LEDColour {
    RED(255,0,0),
    YELLOW(255,255,0),
    BLUE(0,0,255),
    GREEN(0,255,0),
    UNKNOWN(0,0,0),
    ORANGE(255,128,0),
    PINK(255,0,255),
    GOLD(212,175,55),
    WHITE(255,255,255),
    PURPLE(102,51,153);

    public final int r;
    public final int g;
    public final int b;

    LEDColour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}