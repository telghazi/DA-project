package cs451;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;

import cs451.Host;

import java.util.List;

public class TransportLayer {
    static final String ACK = "ACK";
    static final int DELAY = 10;

    private String idProcess;

    UrbLayer upperLayer;
    int packetsSent = 0;
    List<Host> hosts;
    GroundLayer groundLayer;
    HashSet<Packet> delivered;
    Set<Packet> acknowledged;
    Set<Packet> toSend;
    SenderManager senderManager;

    public ReentrantLock lock = new ReentrantLock();
    

    public boolean isFree = false ;



    int maxSequence;

    TransportLayer(int listeningPort, String id, List<Host> hosts_) {
        idProcess = id;
        delivered = new HashSet<>();
        acknowledged = Collections.synchronizedSet(new HashSet<Packet>()); // Multithread proof
        toSend = Collections.synchronizedSet(new HashSet<Packet>());
        maxSequence = 0;
        groundLayer = new GroundLayer(listeningPort);
        groundLayer.transport = this;
        hosts = hosts_;

        senderManager = new SenderManager();
        senderManager.schedule();
    }


    public Packet reconstruct(Packet packet){
        return(new Packet(idProcess, packet.sq_nbr, false));
    }

    public void receive(String sourceHostname, int sourcePort, String rcvdPayload) {

        Packet packet = new Packet(rcvdPayload);
        String packetId = packet.sq_nbr;


        if (packet.isAck) {
            Packet sentPacket = reconstruct(packet);
            acknowledged.add(packet);
        }
        else {
            sendAck(sourceHostname, sourcePort, packet.sq_nbr);
            if (!delivered.contains(packet)) {
                delivered.add(packet);
                deliver(packet);
            } else {
            }
        }
    }

    public synchronized void send(String destHostname, int destPort, int idDest, String payload) {

        int sequenceNumber = ++maxSequence;
        String rawPayload = idProcess + ";" + payload + ";SEND";
        String packetToReceive = Integer.toString(idDest) + ";" + payload +  ";ACK";
        Packet packetId = new Packet(packetToReceive);
        packetId.destHostname = destHostname;
        packetId.destPort = destPort;
        packetId.payload = rawPayload;

        synchronized (toSend){
        toSend.add(packetId);
        }
    }

    public synchronized void broadcast(String message) {
        for (Host host : hosts) {
            send(host.getIp(), host.getPort(), host.getId(), message);
        } 
    }

    public void sendAck(String destHostname, int destPort, String sequenceNumber){
        String rawPayload = idProcess + ";" + sequenceNumber + ";" + ACK;
        groundLayer.send(destHostname, destPort, rawPayload);
    }

    class SenderManager {
        private Timer timer;

        public SenderManager() {
            this.timer = new Timer();
        }

        public synchronized void schedule() {
            
            // Define new task
            Runnable myRunnable = new Runnable(){
            //TimerTask task = new TimerTask() {
                @Override
                public synchronized void run() {

                    Set<Packet> toRemove = Collections.synchronizedSet(new HashSet<Packet>());
                    synchronized (toSend){
                        Set<Packet> copy = new HashSet<>();
                           copy.addAll(toSend);
                        for(Packet packet : copy){
                            if (acknowledged.contains(packet)) {
                                //toSend.remove(packet);
                                toRemove.add(packet);
                            }
                            else{
                                groundLayer.send(packet.destHostname , packet.destPort, packet.payload);
                            }
                        }
    
                        toSend.removeAll(toRemove);
                    }
                    }
                    

                    
			};
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(myRunnable, 10000, DELAY, TimeUnit.MILLISECONDS);
		}
	}

    public void deliver(Packet packet){
        upperLayer.receive(packet.id, packet.sq_nbr);

    }

}