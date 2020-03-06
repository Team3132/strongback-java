package frc.robot.lib;

public enum LEDColour {
    RED(51,0,0),
    YELLOW(51,51,0),
    BLUE(0,0,51),
    GREEN(0,51,0),
    ORANGE(51,26,0),
    PINK(51,0,51),
    WHITE(51,51,51),
    PURPLE(20,10,31);

    public final int r;
    public final int g;
    public final int b;

    LEDColour(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }
}