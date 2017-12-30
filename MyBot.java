import hlt.*;
import main.boid.Flock;
import main.model.GameManager;
import main.model.PropertyManager;

import java.io.IOException;

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

        try {
            PropertyManager.loadProperties("flock.properties");
        } catch (IOException e) {
            throw new NullPointerException();
        }

        Flock flock = new Flock();

        while(true) {
            manager.prepareForNextMove();
            flock.updateShips();
            flock.updateObstacles();

            Networking.sendMoves(flock.planMoves());
            manager.finishCurrentMove();
        }
    }
}
