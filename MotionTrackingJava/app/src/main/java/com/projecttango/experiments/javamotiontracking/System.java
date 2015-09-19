package com.projecttango.experiments.javamotiontracking;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by tom on 9/19/15.
 */
public class System {
    private final Set<Mass> stars = new HashSet<Mass>();
    private final Set<Mass> planets = new HashSet<Mass>();

    public void addStar(Position pos) {
        stars.add(new Mass(pos, new Vector(0, 0, 0)));
    }

    public void addPlanet(Position pos, Vector vel) {
        planets.add(new Mass(pos, vel));
    }

    public void tick() {
        for (Mass planet : planets) {
            for (Mass star : stars) {
                planet.accelerateToward(star);
                planet.move();
            }
        }
    }
}
