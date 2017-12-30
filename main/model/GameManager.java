package main.model;


import hlt.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The GameManager is a singleton class holding all relevant information
 * for retrieving and sending moves and further information.
 */
public class GameManager {

    private static final GameManager sharedInstance = new GameManager();

    private double angularStepRad = Math.PI/180 * 5;
    private int maxCorrections = ((int) (180.0/angularStepRad)) + 1;
    private boolean increaseStepRad = false;

    private GameMap gameMap;
    private Networking networking;
    private int round = 0;

    private Map<Integer, Position> storedEnemyShipPositions = new HashMap<>();
    private Map<Integer, Position> predictedEnemyShipPositions = new HashMap<>();

    private GameManager() {
        // Singleton constructor
        initializeTimer();
    }

    private void initializeTimer() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                increaseStepRad = true;
            }
        }, 1000);
    }

    public int getRound() {
        return round;
    }

    public int getMaxCorrections() {
        return maxCorrections;
    }

    public double getAngularStepRad() {
        return angularStepRad;
    }

    public void initialize(GameMap gameMap, Networking networking) {
        if (gameMap == null || networking == null) throw new NullPointerException();
        this.gameMap = gameMap;
        this.networking = networking;
    }

    public static GameManager getSharedInstance() {
        return sharedInstance;
    }


    public void prepareForNextMove() {
        networking.updateMap(gameMap);
        round += 1;

        predictedEnemyShipPositions = predictEnemyShipPositions(storedEnemyShipPositions, getEnemyShipPositions());

        if (increaseStepRad) {
            double angularStep = Math.min(angularStepRad + Math.PI/180 * 5, Math.PI/180 * 45);
            maxCorrections = ((int) (180.0/angularStepRad)) + 1;
        }
    }

    public void finishCurrentMove() {
        storedEnemyShipPositions.clear();
        storedEnemyShipPositions = getEnemyShipPositions();
    }

    private Map<Integer, Position> predictEnemyShipPositions(
            Map<Integer, Position> storedEnemyShipPositions,
            Map<Integer, Position> currentEnemyShipPositions) {

        Map<Integer, Position> _predictedEnemyShipPositions = new HashMap<>();

        for (Integer shipID : storedEnemyShipPositions.keySet()) {
            if (currentEnemyShipPositions.containsKey(shipID)) {

                Position oldPosition = storedEnemyShipPositions.get(shipID);
                Position newPosition = currentEnemyShipPositions.get(shipID);

                _predictedEnemyShipPositions.put(
                        shipID,
                        new Position(
                                newPosition.getXPos()*2 - oldPosition.getXPos(),
                                newPosition.getYPos()*2 - oldPosition.getYPos()
                        )
                );
            }
        }

        return _predictedEnemyShipPositions;
    }

    public GameMap getGameMap() {
        return gameMap;
    }

    public Map<Integer, Position> getPredictedEnemyShipPositions() {
        return predictedEnemyShipPositions;
    }

    public static Ship getPredictedEnemyShip(Ship ship) {
        Map<Integer, Position> predictedPositions = GameManager.getSharedInstance().getPredictedEnemyShipPositions();
        if (predictedPositions.containsKey(ship.getId())) {
            Position predictedPosition = predictedPositions.get(ship.getId());
            return new Ship(
                    ship.getOwner(),
                    ship.getId(),
                    predictedPosition.getXPos(),
                    predictedPosition.getYPos(),
                    ship.getHealth(),
                    ship.getDockingStatus(),
                    ship.getDockedPlanet(),
                    ship.getDockingProgress(),
                    ship.getWeaponCooldown()
            );
        } else {
            return ship;
        }
    }

    public static List<Planet> getFreePlanets(Position position) {
        return GameManager.getSharedInstance().getGameMap().getAllPlanets().values().stream()
                .filter(planet -> !planet.isOwned())
                .sorted(Comparator.comparing(planet -> planet.getDistanceTo(position)))
                .collect(Collectors.toList());
    }

    public static List<Planet> getPlanetsWithSpareProduction(Position position) {
        return GameManager.getSharedInstance().getGameMap().getAllPlanets().values().stream()
                .filter(planet -> planet.getDockingSpots() < planet.getDockedShips().size() && planet.getOwner() == GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .sorted(Comparator.comparing(planet -> planet.getDistanceTo(position)))
                .collect(Collectors.toList());
    }

    public static List<Planet> getAvailablePlanets(Position position) {
        return GameManager.getSharedInstance().getGameMap().getAllPlanets().values().stream()
                .filter(planet -> planet.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .sorted(Comparator.comparing(planet -> planet.getDistanceTo(position)))
                .collect(Collectors.toList());
    }

    public static List<Planet> getNearestPlanets(Position position) {
        return GameManager.getSharedInstance().getGameMap().getAllPlanets().values().stream()
                .sorted(Comparator.comparing(planet -> planet.getDistanceTo(position)))
                .collect(Collectors.toList());
    }

    public static Map<Integer, Position> getEnemyShipPositions() {
        Map<Integer, Position> enemyShipPositions = new HashMap<>();

        List<Ship> ships = GameManager.getSharedInstance().getGameMap().getAllShips().stream()
                .filter(ship -> ship.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .collect(Collectors.toList());

        for (Ship ship : ships) {
            enemyShipPositions.put(ship.getId(), new Position(ship.getXPos(), ship.getYPos()));
        }

        return enemyShipPositions;
    }

    public static List<Ship> getEnemyShips(Position position) {
        return GameManager.getSharedInstance().getGameMap().getAllShips().stream()
                .filter(ship -> ship.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .sorted(Comparator.comparing(ship -> ship.getDistanceTo(position)))
                .collect(Collectors.toList());
    }

    public static List<Ship> getEnemyShips() {
        return GameManager.getSharedInstance().getGameMap().getAllShips().stream()
                .filter(ship -> ship.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .collect(Collectors.toList());
    }

    public static List<Planet> getWeakEnemyPlanetsByNumberOfDockedShips(Position position, int numberOfDockedShips) {
        return GameManager.getSharedInstance().getGameMap().getAllPlanets().values().stream()
                .filter(planet -> planet.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .filter(planet -> planet.getDockedShips().size() == numberOfDockedShips)
                .sorted(Comparator.comparing(planet -> planet.getRadius()))
                .collect(Collectors.toList());
    }

    public static List<Planet> getWeakEnemyPlanets(Position position) {
        return GameManager.getSharedInstance().getGameMap().getAllPlanets().values().stream()
                .filter(planet -> planet.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .sorted(Comparator.comparing(planet -> planet.getDockedShips().size()))
                .collect(Collectors.toList());
    }

    public static List<Ship> getShipsDockedToPlanet(Planet planet) {
        return GameManager.getSharedInstance().getGameMap().getAllShips().stream()
                .filter(ship -> ship.getDockedPlanet() == planet.getId())
                .sorted(Comparator.comparing(Entity::getHealth))
                .collect(Collectors.toList());
    }

    public static int getMaxNumberOfDockedShips() {
        List<Planet> planets = GameManager.getSharedInstance().getGameMap().getAllPlanets().values().stream()
                .sorted(Comparator.comparing(planet -> planet.getDockedShips().size()))
                .collect(Collectors.toList());
        return planets.get(planets.size() - 1).getDockedShips().size();
    }

    public static List<Ship> getEnemyShipsInProximityOfPlanet(Planet planet) {
        return GameManager.getSharedInstance().getGameMap().getAllShips().stream()
                .filter(ship -> ship.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .filter(ship -> ship.getDistanceTo(planet) < planet.getRadius() + PropertyManager.DEFENSE_ACTION_RADIUS)
                .collect(Collectors.toList());
    }

    public static List<Ship> getOwnShipsInProximityOfPlanet(Planet planet) {
        return GameManager.getSharedInstance().getGameMap().getAllShips().stream()
                .filter(ship -> ship.getOwner() == GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .filter(ship -> ship.getDistanceTo(planet) < planet.getRadius() + PropertyManager.DEFENSE_ACTION_RADIUS)
                .collect(Collectors.toList());
    }

    public static boolean enemyInProximityOfPlanet(Planet planet) {
        return !getEnemyShips().stream()
                .filter(ship -> planet.getDistanceTo(ship) < PropertyManager.SAFE_DOCKING_RADIUS)
                .collect(Collectors.toList())
                .isEmpty();
    }

    public static List<Ship> getAllHostileProximalShips() {
        List<Ship> ships = new ArrayList<>();
        for (Planet planet : GameManager.getSharedInstance().getGameMap().getAllPlanets().values()) {
            ships.addAll(getEnemyShipsInProximityOfPlanet(planet));
        }
        return ships;
    }
}
