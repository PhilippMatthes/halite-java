package main.boid;

import hlt.*;
import main.model.GameManager;
import main.model.PropertyManager;
import main.tactics.TargetManager;
import main.tactics.TargetQueue;
import main.util.Vector2d;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class Flock {

    private List<Position> boids;
    private List<Position> obstacles;

    public Flock() {
        boids = new ArrayList<>();
        obstacles = new ArrayList<>();
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
            if (entity == null) continue;

            Position closestTarget = ship.getClosestPoint(entity);

            if (entity instanceof Planet) {
                Planet planet = (Planet) entity;
                if (ship.canDock(planet) && ship.getDockingStatus() != Ship.DockingStatus.Docked && !planet.isFull()) {
                    plannedMoves.add(new DockMove(ship, planet));
                    continue;
                }
            }

            v1 = groupFlock(ship);
            v21 = collisionAvoidance(ship, PropertyManager.SEPERATION_DISTANCE, PropertyManager.SEPERATION_FACTOR, boids);
            v22 = collisionAvoidance(ship, PropertyManager.OBSTACLE_AVOIDANCE_DISTANCE, PropertyManager.OBSTACLE_AVOIDANCE_FACTOR, obstacles);
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
                plannedMoves.add(moveTowardsTarget);
            }

        }

        TargetQueue.getSharedInstance().flush();

        return plannedMoves;
    }

    private Move createMoveForTarget(Vector2d targetVector, Ship ship) {
        Move moveTowardsTarget = Navigation.navigateShipTowardsTarget(
                GameManager.getSharedInstance().getGameMap(),
                ship,
                new Position(targetVector.xPos, targetVector.yPos),
                Constants.MAX_SPEED,
                true,
                GameManager.getSharedInstance().getMaxCorrections(),
                GameManager.getSharedInstance().getAngularStepRad()
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
                    GameManager.getSharedInstance().getMaxCorrections(),
                    GameManager.getSharedInstance().getAngularStepRad()
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

            if (ship.getDistanceTo(position) > PropertyManager.REGROUPING_RADIUS) {
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
        center = center.division(PropertyManager.REGROUPING_FACTOR);

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

        bound = bound.division(PropertyManager.BOUNDING_FACTOR);

        return bound;
    }

    private Vector2d positionTend(Ship boidShip, Position target) {
        Vector2d place = new Vector2d(target.getXPos(),target.getYPos());

        Vector2d tend = new Vector2d(place.subtract(getPositionAsVector(boidShip)));
        tend.division(PropertyManager.POSITION_TEND_FACTOR);

        return tend;
    }

}
