package main.tactics;

import hlt.*;
import main.model.GameManager;

import java.util.*;
import java.util.Map.Entry;

public class TargetManager {

    private static TargetManager sharedInstance = new TargetManager();
    private static TargetQueue targetQueue = TargetQueue.getSharedInstance();

    private TargetManager() {
        // Singleton Constructor
    }

    public static TargetManager getSharedInstance() {
        return sharedInstance;
    }

    public static Entity selectTarget(Ship ship) {

        Entity target = null;

        target = TargetFinder.targetFreePlanet(ship);
        if (target == null) target = TargetFinder.targetPlanetWithFreeDockingSpots(ship);
        if (target == null) target = TargetFinder.targetWeakPlanetsByDockedShips(ship);
        if (target == null) target = TargetFinder.targetEnemiesShip(ship);
        if (target == null) target = TargetFinder.targetNearestPlanet(ship);


        if (target instanceof Planet) {
            Planet planet = (Planet) target;
            targetQueue.addTarget(target.getId(), ship.getId(), planet.getDockingSpots() - planet.getDockedShips().size(), TargetType.PLANET);
        } else if (target != null) {
            targetQueue.addTarget(target.getId(), ship.getId(), 3, TargetType.SHIP);
        }

        return target;

    }

}
