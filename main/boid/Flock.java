package main.boid;

import hlt.*;
import main.model.GameManager;
import main.tactics.TargetManager;
import main.util.Vector2d;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class Flock {

    private List<Position> boids;
    private List<Position> obstacles;

    private static boolean REINFORCEMENT_LEARNING = true;

    private static double REGROUPING_FACTOR = 5;
    private static double REGROUPING_RADIUS = Constants.SPAWN_RADIUS * 3;
    private static double BOUNDING_FACTOR = 1;
    private static double SEPERATION_DISTANCE = Constants.SPAWN_RADIUS;
    private static double SEPERATION_FACTOR = 1;
    private static double OBSTACLE_AVOIDANCE_DISTANCE = Constants.SPAWN_RADIUS * 3;
    private static double OBSTACLE_AVOIDANCE_FACTOR = 5;
    private static double POSITION_TEND_FACTOR = 1;

    public Flock(String standardLocation) {
        boids = new ArrayList<>();
        obstacles = new ArrayList<>();
        try{
            loadProperties(standardLocation);
        } catch (IOException e) {
            Log.log(Arrays.toString(e.getStackTrace()));
        }
    }

    public void updateShips() {
        boids = new ArrayList<>(
                GameManager.getSharedInstance()
                .getGameMap().getMyPlayer()
                .getShips().values().stream()
                .filter(ship -> ship.getDockingStatus() != Ship.DockingStatus.Docked)
                .collect(Collectors.toList())
        );
    }

    public void updateObstacles() {
        obstacles = new ArrayList<>(
                GameManager.getSharedInstance()
                .getGameMap().getAllShips().stream()
                .filter(ship -> ship.getOwner() != GameManager.getSharedInstance().getGameMap().getMyPlayerId())
                .collect(Collectors.toList())
        );
    }

    public List<Move> planMoves() {
        Vector2d v1, v21, v22, v23, v3, v4, v5 = new Vector2d();

        List<Move> plannedMoves = new ArrayList<>();

        for (Position shipPosition : boids) {

            Ship ship = (Ship) shipPosition;

            Entity entity = TargetManager.selectTarget(ship);
            Position closestTarget = ship.getClosestPoint(entity);

            if (entity instanceof Planet) {
                Planet planet = (Planet) entity;
                if (ship.canDock(planet) && ship.getDockingStatus() != Ship.DockingStatus.Docked && !planet.isFull()) {
                    TargetManager.getSharedInstance().getTargets().put(ship, planet);
                    plannedMoves.add(new DockMove(ship, planet));
                    continue;
                }
            }

            v1 = groupFlock(ship);
            v21 = collisionAvoidance(ship, SEPERATION_DISTANCE, SEPERATION_FACTOR, boids);
            v22 = collisionAvoidance(ship, OBSTACLE_AVOIDANCE_DISTANCE, OBSTACLE_AVOIDANCE_FACTOR, obstacles);
            v4 = bounding(ship);
            v5 = positionTend(ship, closestTarget);

            Vector2d sum = new Vector2d();
            sum = sum.add(v1);
            sum = sum.add(v21);
            sum = sum.add(v22);
            sum = sum.add(v4);
            sum = sum.add(v5);

            Vector2d targetVector = new Vector2d(getPositionAsVector(ship).add(sum));

            Move moveTowardsTarget = createMoveForTarget(targetVector, ship);

            if (moveTowardsTarget != null) {
                TargetManager.getSharedInstance().getTargets().put(ship, entity);
                plannedMoves.add(moveTowardsTarget);
            }

        }

        TargetManager.getSharedInstance().getTargets().clear();

        return plannedMoves;
    }

    private Move createMoveForTarget(Vector2d targetVector, Ship ship) {
        Move moveTowardsTarget = Navigation.navigateShipTowardsTarget(
                GameManager.getSharedInstance().getGameMap(),
                ship,
                new Position(targetVector.xPos, targetVector.yPos),
                Constants.MAX_SPEED,
                true,
                Constants.MAX_NAVIGATION_CORRECTIONS - 1,
                Math.PI/180.0
        );

        if (moveTowardsTarget != null) {
            return moveTowardsTarget;
        } else {
            moveTowardsTarget = Navigation.navigateShipTowardsTarget(
                    GameManager.getSharedInstance().getGameMap(),
                    ship,
                    new Position(targetVector.xPos, targetVector.yPos),
                    Constants.MAX_SPEED,
                    false,
                    Constants.MAX_NAVIGATION_CORRECTIONS - 1,
                    Math.PI/180.0
            );

            if (moveTowardsTarget != null) {
                return moveTowardsTarget;
            } else {
                return null;
            }
        }
    }

    private Vector2d getPositionAsVector(Position position) {
        return new Vector2d(position.getXPos(), position.getYPos());
    }

    private Vector2d groupFlock(Ship ship) {
        Vector2d center = new Vector2d();

        int groupSize = 0;

        for (Position position : boids) {

            Ship boidShip = (Ship) position;

            if (ship.getDistanceTo(position) > REGROUPING_RADIUS) {
                continue;
            }

            if (boidShip.getXPos() != ship.getXPos() && boidShip.getYPos() != ship.getYPos()) {
                continue;
            }

            center = center.add(getPositionAsVector(boidShip));
            groupSize += 1;
        }
        center = center.division(groupSize);
        center = center.subtract(getPositionAsVector(ship));
        center = center.division(REGROUPING_FACTOR);

        return center;
    }

    private Vector2d collisionAvoidance(Position position, double distance, double factor, List<Position> obstacles) {
        Vector2d correction = new Vector2d();
        Vector2d cPosition = new Vector2d(getPositionAsVector(position));

        for (Position obstacle : obstacles) {
            if (!position.equals(obstacle)) {
                Vector2d aPosition = getPositionAsVector(obstacle);
                Vector2d xD = new Vector2d(aPosition.xPos - cPosition.xPos, aPosition.yPos - cPosition.yPos);

                if(Math.abs(xD.xPos) < distance && Math.abs(xD.yPos) < distance) {
                    correction = correction.subtract(xD).division(factor);
                }

            }
        }
        return correction;
    }

    private Vector2d bounding(Ship boidShip) {
        Vector2d bound = new Vector2d();
        int xMin = 0,
                xMax = GameManager.getSharedInstance().getGameMap().getWidth(),
                yMin = 0,
                yMax = GameManager.getSharedInstance().getGameMap().getHeight();

        Vector2d cPos = getPositionAsVector(boidShip);

        if (cPos.xPos < xMin) {
            bound.xPos += 1;
        } else if (cPos.xPos > xMax){
            bound.xPos += -1;
        }
        if (cPos.yPos < yMin) {
            bound.yPos += 1;
        } else if (cPos.yPos > yMax){
            bound.yPos += -1;
        }

        bound = bound.division(BOUNDING_FACTOR);

        return bound;
    }

    private Vector2d positionTend(Ship boidShip, Position target) {
        Vector2d place = new Vector2d(target.getXPos(),target.getYPos());

        Vector2d tend = new Vector2d(place.subtract(getPositionAsVector(boidShip)));
        tend.division(POSITION_TEND_FACTOR);

        return tend;
    }

    public static void loadProperties(String standardLocation) throws IOException {
        Properties props = new Properties();
        InputStream is;
        File f;
        if (REINFORCEMENT_LEARNING) {
            f = new File("bot"+GameManager.getSharedInstance().getGameMap().getMyPlayerId()+".properties");
        } else {
            f = new File(standardLocation);
        }
        is = new FileInputStream(f);
        props.load(is);

        try {
            REGROUPING_FACTOR = Double.parseDouble(props.getProperty("REGROUPING_FACTOR", ""));
            REGROUPING_RADIUS = Double.parseDouble(props.getProperty("REGROUPING_RADIUS", ""));
            BOUNDING_FACTOR = Double.parseDouble(props.getProperty("BOUNDING_FACTOR", ""));
            SEPERATION_DISTANCE = Double.parseDouble(props.getProperty("SEPERATION_DISTANCE", ""));
            SEPERATION_FACTOR = Double.parseDouble(props.getProperty("SEPERATION_FACTOR", ""));
            OBSTACLE_AVOIDANCE_DISTANCE = Double.parseDouble(props.getProperty("OBSTACLE_AVOIDANCE_DISTANCE", ""));
            OBSTACLE_AVOIDANCE_FACTOR = Double.parseDouble(props.getProperty("OBSTACLE_AVOIDANCE_FACTOR", ""));
            POSITION_TEND_FACTOR = Double.parseDouble(props.getProperty("POSITION_TEND_FACTOR", ""));
        } catch (NumberFormatException e) {
            Log.log(Arrays.toString(e.getStackTrace()));
        }
        is.close();
    }

    public void storeProperties() {
        try {
            Properties props = new Properties();
            props.setProperty("REGROUPING_FACTOR", String.format("%s", REGROUPING_FACTOR));
            props.setProperty("REGROUPING_RADIUS", String.format("%s", REGROUPING_RADIUS));
            props.setProperty("BOUNDING_FACTOR", String.format("%s", BOUNDING_FACTOR));
            props.setProperty("SEPERATION_DISTANCE", String.format("%s", SEPERATION_DISTANCE));
            props.setProperty("SEPERATION_FACTOR", String.format("%s", SEPERATION_FACTOR));
            props.setProperty("OBSTACLE_AVOIDANCE_DISTANCE", String.format("%s", OBSTACLE_AVOIDANCE_DISTANCE));
            props.setProperty("OBSTACLE_AVOIDANCE_FACTOR", String.format("%s", OBSTACLE_AVOIDANCE_FACTOR));
            props.setProperty("POSITION_TEND_FACTOR", String.format("%s", POSITION_TEND_FACTOR));

            File f;
            if (REINFORCEMENT_LEARNING) {
                f = new File("bot"+GameManager.getSharedInstance().getGameMap().getMyPlayerId()+".properties");
            } else {
                f = new File("flock.properties");
            }

            OutputStream out = new FileOutputStream( f );
            props.store(out, "This is the Flock Properties File used for Reinforcement Learning!");
        }
        catch (Exception e ) {
            Log.log(Arrays.toString(e.getStackTrace()));
        }
    }

}
