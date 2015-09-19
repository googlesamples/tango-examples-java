package com.projecttango.experiments.javamotiontracking;

/**
 * Created by tom on 9/19/15.
 */
public class Position {
    private final double x, y, z;

    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public double getZ() {
        return z;
    }

    public Vector vectorTo(Position pos) {
        return new Vector(pos.x - x, pos.y - y, pos.z - z);
    }

    public double distanceTo(Position pos) {
        return vectorTo(pos).getMagnitude();
    }

    public Position move(Vector velocity) {
        return new Position(x + velocity.getDx(), y + velocity.getDy(), z + velocity.getDz());
    }

    @Override
    public String toString() {
        return "[" + x + "," + y + "," + z + "]";
    }
}
