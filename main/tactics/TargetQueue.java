package main.tactics;

import hlt.Entity;
import hlt.Log;

import java.util.*;

public class TargetQueue {

    private static TargetQueue sharedInstance = new TargetQueue();

    private Map<Integer, Map.Entry<Integer, List<Integer>>> planetTargets;
    private Map<Integer, Map.Entry<Integer, List<Integer>>> shipTargets;

    private TargetQueue() {
        planetTargets = new HashMap<>();
        shipTargets = new HashMap<>();
    }

    public static TargetQueue getSharedInstance() {
        return sharedInstance;
    }

    public Map.Entry<Integer, List<Integer>> get(Integer id, TargetType type) {
        switch (type) {
            case SHIP:
                return shipTargets.get(id);
            case PLANET:
                return planetTargets.get(id);
            default:
                return null;
        }
    }

    public boolean targetCapacityIsFull(Integer id, TargetType type) {
        Map.Entry<Integer, List<Integer>> entry = get(id, type);
        if (entry == null) {
            return false;
        } else {
            return entry.getValue().size() >= entry.getKey();
        }
    }

    public void addTarget(Integer targetID, Integer shipID, int capacity, TargetType type) {
        Map.Entry<Integer, List<Integer>> entry = get(targetID, type);
        if (entry == null) {
            List<Integer> ships = new ArrayList<>();
            ships.add(shipID);
            entry = new AbstractMap.SimpleEntry<>(capacity, ships);
        } else {
            List<Integer> ships = entry.getValue();
            if (!ships.contains(shipID)) ships.add(shipID);
            entry = new AbstractMap.SimpleEntry<>(capacity, ships);
        }
        switch (type) {
            case SHIP:
                shipTargets.put(targetID, entry);
                return;
            case PLANET:
                planetTargets.put(targetID, entry);
                return;
            default:
                return;
        }
    }

    public void flush() {
        Log.log("");
        Log.log("Flushing.");
        Log.log("Planet targets: "+planetTargets.toString());
        Log.log("Ship targets: "+shipTargets.toString());
        Log.log("");
        planetTargets.clear();
        shipTargets.clear();
    }



}
