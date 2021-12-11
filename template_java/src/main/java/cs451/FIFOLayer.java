package cs451;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs451.UrbLayer;

public class FIFOLayer{

    UrbLayer urbLayer ;
    int lsn = 0;
    int next[];
    HashMap<String,HashMap<String, String>> toDeliver = new HashMap<String,HashMap<String, String>>();
    String output = "";
    FileWriter myWriter;
    ConcurrentLinkedQueue eventLog;

    public FIFOLayer(int listeningPort, String id, List<Host> hosts, String outputPath, ConcurrentLinkedQueue eventLog){
        urbLayer = new UrbLayer(listeningPort, id, hosts);
        next = new int[hosts.size()];
        Arrays.fill(next, 0);
        urbLayer.upperLayer = this;
        this.eventLog = eventLog;

    }

    public synchronized void send(String message){
        output = output + "b "+message + "\n";
        eventLog.add("b "+message);
        System.out.println("b "+message);
        urbLayer.send(message + "~" + Integer.toString(lsn));
        lsn++;
    }

    public synchronized void receive(String formattedMessage){
        String arr[] = formattedMessage.split("~");
        String id = arr[0];
        String message = arr[1];
        String sq_nbr = arr[2];

        if (!toDeliver.containsKey(id)){
            toDeliver.put(id, new HashMap<String, String>());
        }

        toDeliver.get(id).put(sq_nbr, message);

        checkForDelivery(id);
    }

    public synchronized void checkForDelivery(String id){
        int intId = Integer.parseInt(id) - 1;
        String current_sqnbr = Integer.toString(next[intId]);
        if(toDeliver.get(id).containsKey(current_sqnbr)){
            eventLog.add("d " + id + " " + toDeliver.get(id).get(current_sqnbr));
            System.out.println("d " + id + " " + toDeliver.get(id).get(current_sqnbr));
            next[intId]++;
            checkForDelivery(id);
        }

    }

    public void writeOutput(String outputFile){
        System.out.println("Oui");
        try {
            FileWriter myWriter = new FileWriter(outputFile, false);
            myWriter.write(output);
            myWriter.close();
            
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}