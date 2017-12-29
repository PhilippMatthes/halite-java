package main.tactics;

import hlt.*;
import main.model.GameManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TargetManager {

    private static TargetManager sharedInstance = new TargetManager();

    private HashMap<Ship, Position> targets = new HashMap<>();

    private TargetManager() {
        // Singleton Constructor
    }

    public static TargetManager getSharedInstance() {
        return sharedInstance;
    }

    public Map<Ship, Position> getTargets() {
        return targets;
    }

    public static Entity selectTarget(Ship ship) {

        List<Planet> allPlanets = GameManager.getAllPlanets(ship);
        Entity target = allPlanets.get(0);

        for (Ship enemyShip : GameManager.getEnemyShips(ship)) {
            if (!TargetManager.getSharedInstance().getTargets().values().contains(enemyShip)) {
                target = enemyShip;
                break;
            }
        }

        for (Planet planet : GameManager.getWeakestEnemyPlanets(ship)) {
            List<Ship> dockedShips = GameManager.getShipsDockedToPlanet(planet);

            boolean foundShip = false;

            for (Ship enemyShip : dockedShips) {
                if (!TargetManager.getSharedInstance().getTargets().values().contains(enemyShip)) {
                    foundShip = true;
                    target = enemyShip;
                }
            }

            if (foundShip) break;
        }

        for (Planet planet : GameManager.getFreePlanets(ship)) {
            if (!TargetManager.getSharedInstance().getTargets().values().contains(planet)) {
                target = planet;
                break;
            }
        }

        return target;

    }

    public static double turnsNeededToReachTarget(Ship ship, Position position) {
        return ship.getDistanceTo(position) / (double) Constants.MAX_SPEED;
    }

    public static double capacityForPlanet(Planet planet) {
        return planet.getRemainingProduction();
    }

}
