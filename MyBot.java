import hlt.*;
import main.boid.Flock;
import main.model.GameManager;

public class MyBot {

    public static void main(final String[] args) {
        Networking networking = new Networking();
        GameMap gameMap = networking.initialize("Spaceinvader");

        GameManager manager = GameManager.getSharedInstance();
        manager.initialize(gameMap, networking);

        // We now have 1 full minute to analyse the initial map.
        String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        Flock flock = new Flock();

        for (Ship ship : gameMap.getMyPlayer().getShips().values()) {
            flock.addShip(ship);
        }

        while(true) {
            manager.prepareForNextMove();

            Networking.sendMoves(flock.planMoves());
        }
    }
}
