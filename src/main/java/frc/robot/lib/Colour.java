package frc.robot.lib;

public enum Colour {
    RED(0,"red",255,0,0),
    YELLOW(1,"yellow",255,255,0),
    BLUE(2,"blue",0,0,255),
    GREEN(3,"green",0,255,0),
    UNKNOWN(-1,"unknown",0,0,0),
    ORANGE(-2,"orange",255,128,0),
    PINK(-3,"pink",255,0,255),
    GOLD(-4,"gold",212,175,55),
    PURPLE(-5,"purple",102,51,153);

    public final int id;
    public final String name;
    public final int r;
    public final int g;
    public final int b;
    public final int NUM_COLOURS = 4;
    Colour(int id, String name, int r, int g, int b) {
        this.id = id;
        this.name = name;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public static Colour of(int id) {
        switch(id) {
            case 0:
                return RED;
            case 1:
                return YELLOW;
            case 2:
                return BLUE;
            case 3:
                return GREEN;
            default:
                return UNKNOWN;
        }
    }

    public boolean equals (Colour colour) {
        return this.id == colour.id;
    }

    public Colour next (double direction) {
        return Colour.of((this.id + NUM_COLOURS + (direction < 0 ? 1 : -1)) % NUM_COLOURS);
    }

    @Override
    public String toString () {
        return name + "(" + id + ")";
    }
}
