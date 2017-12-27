package main.boid;

import hlt.Move;
import hlt.Ship;

import java.util.ArrayList;
import java.util.List;

public class Flock {

    private ArrayList<Ship> boids;
    private double movementFactor = 1000;
    private double boundingFactor = 10;
    private int seperationDistance = 13;
    private double seperationFactor = 50;

    public Flock() {
        boids = new ArrayList<>();
    }

    public boolean addShip(Ship ship) {
        return boids.add(ship);
    }

    public boolean removeShip(Ship ship) {
        return boids.remove(ship);
    }

    public List<Move> planMoves() {
        return new ArrayList<>();
    }

}
