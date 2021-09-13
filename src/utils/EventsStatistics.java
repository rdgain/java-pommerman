package utils;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;

public class EventsStatistics {


    final static String experimentsFolderPath = "res/gamelogs/";

    public ArrayList<String> events;

    // TODO: Configured for 4 agents by default
//    public int[] bombPlacementsAttempted = {0, 0, 0, 0};
    public int[] bombsTriggered = {0, 0, 0, 0};

    public int[] bombsPlaced = {0, 0, 0, 0};
    public int[] woodsDestroyed = {0, 0, 0, 0};
    public int[] powerUpsTaken = {0, 0, 0, 0};
    public int[] killedBy = {-1, -1, -1, -1};  // id of player who killed idx
    public int[] wastedBombs = {0, 0, 0, 0};  // bombs that explode and touch nothing else

    public static int REP = 0;

    public EventsStatistics(){
        events = new ArrayList<>();
    }

    public EventsStatistics copy() {
        EventsStatistics es = new EventsStatistics();
        es.bombsTriggered = bombsTriggered.clone();
        es.bombsPlaced = bombsPlaced.clone();
        es.woodsDestroyed = woodsDestroyed.clone();
        es.powerUpsTaken = powerUpsTaken.clone();
        es.killedBy = killedBy.clone();
        es.wastedBombs = wastedBombs.clone();
        return es;
    }

    public void saveToTextFile(String gameIdStr, long seed){

        File file = new File(experimentsFolderPath+ gameIdStr + "/");
        if (! file.exists()){
            file.mkdir();
        }

        if (file.listFiles() == null) {
            throw new Error("Folder specified at " + experimentsFolderPath + " does not exist nor could be created.");
        }

        String path = experimentsFolderPath + gameIdStr + "/" + seed + "_" + REP + "_events.txt";

        try {
            FileWriter writer = new FileWriter(path, true);
            for (String event : events){
                writer.write(event);
            }
            writer.close();

        } catch (IOException i) {
            i.printStackTrace();
        }
    }

}
