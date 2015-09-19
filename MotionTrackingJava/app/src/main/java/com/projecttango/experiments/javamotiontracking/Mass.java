package com.projecttango.experiments.javamotiontracking;

/**
 * Created by tom on 9/19/15.
 */
public class Mass {
    private static final double GRAVITY = 0.01;

    private final long id;

    private Position position;
    private Vector velocity;

    public Mass(long id, Position position, Vector velocity) {
        this.id = id;
        this.position = position;
        this.velocity = velocity;
    }

    public long getId() {
        return id;
    }

    public Position getPosition() {
        return position;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void accelerateToward(Mass body) {
        // Assume mass of 1 for both bodies
        double force = GRAVITY / Math.pow(position.distanceTo(body.position), 2);
        velocity = velocity.add(position.vectorTo(body.position).scale(force));
    }

    public void move() {
        position = position.move(velocity);
    }

    @Override
    public String toString() {
        return "ID=" + id + ", position=" + position + ", velocity=" + velocity;
    }
}
