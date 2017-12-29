package main.tactics;

import hlt.Planet;
import hlt.Ship;
import main.model.GameManager;

import java.util.List;

public class TargetFinder {

    public static Ship targetEnemiesShip(Ship ship) {
        if (ship == null) return null;
        for (Ship enemyShip : GameManager.getEnemyShips(ship)) {
            if (!TargetQueue.getSharedInstance().targetCapacityIsFull(enemyShip.getId(), TargetType.SHIP)) {
                return enemyShip;
            }
        }
        return null;
    }

    public static Ship targetWeakPlanetsByDockedShips(Ship ship) {
        if (ship == null) return null;

        Ship target = null;

        for (Planet planet : GameManager.getWeakEnemyPlanets(ship)) {
            List<Ship> dockedShips = GameManager.getShipsDockedToPlanet(planet);

            boolean foundShip = false;

            for (Ship enemyShip : dockedShips) {
                if (!TargetQueue.getSharedInstance().targetCapacityIsFull(enemyShip.getId(), TargetType.SHIP)) {
                    foundShip = true;
                    target = enemyShip;
                }
            }

            if (foundShip) break;
        }

        return target;
    }

    public static Planet targetFreePlanet(Ship ship) {
        if (ship == null) return null;
        for (Planet planet : GameManager.getFreePlanets(ship)) {
            if (!TargetQueue.getSharedInstance().targetCapacityIsFull(planet.getId(), TargetType.PLANET)) {
                return planet;
            }
        }
        return null;
    }

    public static Planet targetPlanetWithFreeDockingSpots(Ship ship) {
        if (ship == null) return null;
        for (Planet planet : GameManager.getPlanetsWithSpareProduction(ship)) {
            if (!TargetQueue.getSharedInstance().targetCapacityIsFull(planet.getId(), TargetType.PLANET)) {
                return planet;
            }
        }
        return null;
    }

    public static Planet targetNearestPlanet(Ship ship) {
        if (ship == null) return null;
        for (Planet planet : GameManager.getNearestPlanets(ship)) {
            if (!TargetQueue.getSharedInstance().targetCapacityIsFull(planet.getId(), TargetType.PLANET)) {
                return planet;
            }
        }
        return null;
    }

}
