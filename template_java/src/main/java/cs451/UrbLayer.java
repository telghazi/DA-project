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

import cs451.FIFOLayer;


public class UrbLayer {


    Set<String> pending;
    Set<String> delivered;
    Map<String, HashSet<String>> acked;

    Integer messagesInBroadcast;
    //SynchronizedLinkedList<String> messagesToBroadcast;
    TransportLayer transport;
    int nbr_processes;

    FIFOLayer upperLayer = null;

    String myId;

    public UrbLayer(int listeningPort, String id, List<Host> hosts){

        this.pending = Collections.synchronizedSet(new HashSet<>());
        this.delivered = Collections.synchronizedSet(new HashSet<>());
        this.acked = Collections.synchronizedMap(new HashMap<>());

        //this.messagesToBroadcast = new SynchronizedLinkedList<>();
        this.messagesInBroadcast = 0;

        this.transport = new TransportLayer(listeningPort, id, hosts);
        transport.upperLayer = this;
        nbr_processes = hosts.size();
        myId = id;

        System.out.println("================================== \n nbr_processes : " + nbr_processes);
        System.out.println("THRESHOLD : " + nbr_processes/2.0);
    }

    public synchronized void send(String message) {

        String formattedMessage = myId+ "~" + message;

        //System.out.println("URB broadcasting : " +  formattedMessage);

        if (!acked.containsKey(formattedMessage)){
            acked.put(formattedMessage, new HashSet<String>());
        }
        acked.get(formattedMessage).add(myId);
        pending.add(formattedMessage);

        transport.broadcast(formattedMessage);  
    }

    public synchronized void receive(String immediateSender, String formattedMessage) {
        
        //Host originalSender = HostList.getHost(originalSenderId);

        if (!acked.containsKey(formattedMessage)){
            acked.put(formattedMessage, new HashSet<String>());
        }
        acked.get(formattedMessage).add(immediateSender);

        //System.out.println("Checking if " +  formattedMessage + " is being broadcast");
        if(!pending.contains(formattedMessage)) {
            pending.add(formattedMessage);
            transport.broadcast(formattedMessage);
        }

        checkForDelivery(formattedMessage);
    }

    public void checkForDelivery(String formattedMessage){
        if(acked.get(formattedMessage).size() >= nbr_processes/2.0){
            //System.out.println("URB delivering : " +  formattedMessage);
            deliver(formattedMessage);
        }
        

    }

    public synchronized void deliver(String formattedMessage){
        if (upperLayer != null){
            upperLayer.receive(formattedMessage);
        }
        else {
            System.out.println("URB DELIVERING : " + formattedMessage);
        }
    }

}