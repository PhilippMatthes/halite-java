package main.model;


import hlt.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The GameManager is a singleton class holding all relevant information
 * for retrieving and sending moves and further information.
 */
public class GameManager {

    private static final GameManager sharedInstance = new GameManager();

    private GameMap gameMap;
    private Networking networking;
    private int round = 0;

    private GameManager() {
        // Singleton constructor
    }

    public int getRound() {
        return round;
    }

    /**
     * Initialize the GameManager singleton class with the needed parameters.
     *
     * @param gameMap    the game map
     * @param networking the networking
     *
     * @throws NullPointerException if a parameter is null.
     */
    public void initialize(GameMap gameMap, Networking networking) {
        if (gameMap == null || networking == null) throw new NullPointerException();
        this.gameMap = gameMap;
        this.networking = networking;
    }

    /**
     * Gets shared singleton instance.
     *
     * @return the shared singleton instance
     */
    public static GameManager getSharedInstance() {
        return sharedInstance;
    }




    /**
     * Prepare for next move.
     *
     * Takes care of the following things:
     *      - Update the game map
     */
    public void prepareForNextMove() {
        networking.updateMap(gameMap);
        round += 1;
    }


    /**
     * Gets game map.
     *
     * @return the game map
     */
    public GameMap getGameMap() {
        return gameMap;
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

    public static List<Ship> getEnemyShips(Position position) {
        return GameManager.getSharedInstance().getGameMap().getAllShips().stream()
                .filter(ship -> ship.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .sorted(Comparator.comparing(ship -> ship.getDistanceTo(position)))
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
}
