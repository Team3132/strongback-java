package frc.robot.drive.util;

import java.util.function.DoubleSupplier;

public class LowPassFilter implements DoubleSupplier {
    private double last = 0;
    private final double alpha;
    private final DoubleSupplier source;

    public LowPassFilter(DoubleSupplier source, double alpha) {
        this.source = source;
        this.alpha = alpha;
        last = source.getAsDouble();
    }

    @Override
    public double getAsDouble() {
        last = alpha * source.getAsDouble() + (1 - alpha) * last;
        return last;
    }
}