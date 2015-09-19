package com.projecttango.experiments.javamotiontracking;

/**
 * Created by tom on 9/19/15.
 */
public class Vector {
    private final double dx, dy, dz;

    public Vector(double dx, double dy, double dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public double getDx() {
        return dx;
    }

    public double getDy() {
        return dy;
    }

    public double getDz() {
        return dz;
    }

    public double getMagnitude() {
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2) + Math.pow(dz, 2));
    }

    public Vector scale(double magnitude) {
        double mag = getMagnitude();
        return new Vector(dx / mag * magnitude, dy / mag * magnitude, dz / mag * magnitude);
    }

    public Vector add(Vector delta) {
        return new Vector(dx + delta.dx, dy + delta.dy, dz + delta.dz);
    }

    @Override
    public String toString() {
        return "[" + dx + "," + dy + "," + dz + "]";
    }
}
