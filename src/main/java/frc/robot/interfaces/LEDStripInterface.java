package frc.robot.interfaces;

public interface LEDStripInterface {
    public enum Colour {
        RED(255,0,0),
        GREEN(0,255,0),
        BLUE(0,0,255),
        YELLOW(255,255,0),
        ORANGE(255,128,0),
        PINK(255,0,255),
        GOLD(212,175,55),
        PURPLE(102,51,153);

        public final int r;
        public final int g;
        public final int b;
        Colour(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public void setColour(Colour c);

    public void setProgressColour(Colour c1, Colour c2, int percent);

    public void setIdle();

    public void setData();
}