package main.model;

import hlt.Constants;
import hlt.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Properties;

public class PropertyManager {

    public static boolean REINFORCEMENT_LEARNING = false;

    public static double REGROUPING_FACTOR = 5;
    public static double REGROUPING_RADIUS = Constants.SPAWN_RADIUS * 3;
    public static double BOUNDING_FACTOR = 1;
    public static double SEPERATION_DISTANCE = Constants.SPAWN_RADIUS;
    public static double SEPERATION_FACTOR = 1;
    public static double OBSTACLE_AVOIDANCE_DISTANCE = Constants.SPAWN_RADIUS * 3;
    public static double OBSTACLE_AVOIDANCE_FACTOR = 5;
    public static double POSITION_TEND_FACTOR = 1;

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

        Log.log("Loaded factors: ");
        Log.log(REGROUPING_FACTOR+"");
        Log.log(REGROUPING_RADIUS+"");
        Log.log(BOUNDING_FACTOR+"");
        Log.log("...");

        is.close();
    }

    /*
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
    */

}
