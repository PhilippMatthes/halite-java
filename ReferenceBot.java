import hlt.GameMap;
import hlt.Log;
import hlt.Networking;
import main.boid.Flock;
import main.model.GameManager;

public class ReferenceBot {

    public static void main(final String[] args) {
        Networking networking = new Networking();
        GameMap gameMap = networking.initialize("Reference");

        GameManager manager = GameManager.getSharedInstance();
        manager.initialize(gameMap, networking);

        // We now have 1 full minute to analyse the initial map.
        String initialMapIntelligence =
                "width: " + gameMap.getWidth() +
                "; height: " + gameMap.getHeight() +
                "; players: " + gameMap.getAllPlayers().size() +
                "; planets: " + gameMap.getAllPlanets().size();
        Log.log(initialMapIntelligence);

        Flock flock = new Flock("reference.properties");

        while(true) {
            manager.prepareForNextMove();
            flock.updateShips();
            flock.updateObstacles();

            Networking.sendMoves(flock.planMoves());
        }
    }
}
