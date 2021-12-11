package cs451;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    private static EchoClient client = null;
    private static EchoServer server = null;
    private static String outputPath ;
    private static Thread thread;
    static FIFOLayer fifoLayer;
    public static ConcurrentLinkedQueue<String> eventLog = new ConcurrentLinkedQueue<String>();

    private static void writeOutput(){
        
        String output = "";
        if(client != null){
            System.out.println("Writing broadcast messages.");
            for (HashMap.Entry<String, Boolean> entry : client.get_broadcastMessages().entrySet()){
                    output = output + "b" + " " + entry.getKey() + "\n";
                    System.out.println(output);
            }
        }

        if(server != null){
            System.out.println("Writing delivered messages.");
            for (ConcurrentHashMap.Entry<Integer, ConcurrentHashMap<String, Boolean>> entry : server.get_messagesToDeliver().entrySet()){
                    int id = entry.getKey();

                    ConcurrentHashMap<String, Boolean> messages = entry.getValue();
                    for (ConcurrentHashMap.Entry<String, Boolean> m : messages.entrySet()){
                        output = output + "d" + " " + Integer.toString(id) + " " + m.getKey() + "\n";
                        System.out.println(output);
                    }
            }
        }
        try {
            FileWriter myWriter = new FileWriter(outputPath, false);
            myWriter.write(output);
            myWriter.close();
            
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
    }

    private static void handleSignal() {
        //immediately stop network packet processing
        System.out.println("Immediately stopping network packet processing.");
        /*if (server != null) {
            server.disconnect();
        }
        if (client != null) {
            client.disconnect();
        }*/
        //write/flush output file if necessary
        System.out.println("Writing output.");
        /*if (fifoLayer != null){
            fifoLayer.writeOutput(outputPath);
        }*/
        try {
            FileWriter myWriter = new FileWriter(outputPath, false);
            boolean firstline = true;
            for(String event : eventLog){
                if(firstline){
                    myWriter.write(event);
                    firstline = false;
                }
                else {
                    myWriter.write("\n" + event);
                }
            }
            
            myWriter.close();
            
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println(eventLog);

    }

    private static void initSignalHandlers() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                handleSignal();
            }
        });
    }

    private static String[] parseConfig(Parser parser) {

        String[] config_contents = new String[] {"1"};
        try (FileReader reader = new FileReader(parser.config());
            BufferedReader br = new BufferedReader(reader)) {

            // read line by line
            String line;
            line = br.readLine();
            System.out.println(line);
            config_contents = line.split(" ");
            return(config_contents);
           

           

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
            return(config_contents);
        }
        
    }

    public static void talk(FIFOLayer urbLayer, int nbr_messages) {

        String data;
        for(int k = 1; k<nbr_messages+1; k++){

            urbLayer.send(Integer.toString(k));
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Parser parser = new Parser(args);
        parser.parse();

        initSignalHandlers();

        // example
        long pid = ProcessHandle.current().pid();
        System.out.println("My PID: " + pid + "\n");
        System.out.println("From a new terminal type `kill -SIGINT " + pid + "` or `kill -SIGTERM " + pid + "` to stop processing packets\n");

        System.out.println("My ID: " + parser.myId() + "\n");
        System.out.println("List of resolved hosts is:");
        System.out.println("==========================");
        int nbr_hosts_dummy = 0;
        for (Host host: parser.hosts()) {
            nbr_hosts_dummy++;
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
        final int nbr_hosts = nbr_hosts_dummy;
        System.out.println();

        System.out.println("Path to output:");
        System.out.println("===============");
        System.out.println(parser.output() + "\n");

        outputPath = parser.output();

        System.out.println("Path to config:");
        System.out.println("===============");
        System.out.println(parser.config() + "\n");

        System.out.println("Doing some initialization\n");

        int nbr_messages = Integer.parseInt(parseConfig(parser)[0]);
        System.out.println("Sending " + Integer.toString(nbr_messages) + " messages");
        // Retrieve own port for initialisation
        int localPort = -1;
        for ( Host host : parser.hosts()) {
            if (host.getId() == parser.myId())
                localPort = host.getPort();
        }
        

        System.out.println("Broadcasting and delivering messages...\n");


        FIFOLayer fifoLayer = new FIFOLayer(localPort, Integer.toString(parser.myId()), parser.hosts(), outputPath, eventLog);
        talk(fifoLayer, nbr_messages);

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
