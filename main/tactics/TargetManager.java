package main.tactics;

import hlt.Entity;
import hlt.Planet;
import hlt.Position;
import hlt.Ship;
import main.model.GameManager;

import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class TargetManager {

    private static TargetManager sharedInstance = new TargetManager();

    private HashMap<Ship, Position> targets;

    private TargetManager() {
        // Singleton Constructor
        targets = new HashMap<>();
    }

    public static TargetManager getSharedInstance() {
        return sharedInstance;
    }

    public HashMap<Ship, Position> getTargets() {
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

        for (Planet planet : allPlanets) {
            if (!TargetManager.getSharedInstance().getTargets().values().contains(planet)) {
                target = planet;
                break;
            }
        }

        for (Planet planet : GameManager.getAvailablePlanets(ship)) {
            if (!TargetManager.getSharedInstance().getTargets().values().contains(planet)) {
                target = planet;
                break;
            }
        }

        for (Planet planet : GameManager.getFreePlanets(ship)) {
            if (!TargetManager.getSharedInstance().getTargets().values().contains(planet)) {
                target = planet;
                break;
            }
        }

        return target;
    }

}
