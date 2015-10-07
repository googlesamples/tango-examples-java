package com.projecttango.experiments.javamotiontracking;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tom on 9/19/15.
 */
public class StarSystem {
    private final Set<Mass> stars = new HashSet<Mass>();
    private final Set<Mass> planets = new HashSet<Mass>();

    public void addStar(Position pos) {
        stars.add(new Mass(-1, pos, new Vector(0, 0, 0)));
    }

    public void addPlanet(long id, Position pos, Vector vel) {
        planets.add(new Mass(id, pos, vel));
    }

    public void tick() {
        for (Mass planet : planets) {
            for (Mass star : stars) {
                planet.accelerateToward(star);
                planet.move();
            }
        }
    }

    public Collection<Mass> getStars() {
        return Collections.unmodifiableSet(stars);
    }

    public Collection<Mass> getPlanets() {
        return Collections.unmodifiableSet(planets);
    }
}
