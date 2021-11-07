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

public class Main {

    private static EchoClient client = null;
    private static EchoServer server = null;
    private static String outputPath ;

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
        if (server != null) {
            server.writeOutput(outputPath);
        }
        if (client != null) {
            client.writeOutput(outputPath);
        }


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
            config_contents = line.split(" ");
            return(config_contents);
           

           

        } catch (IOException e) {
            System.err.format("IOException: %s%n", e);
            return(config_contents);
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
        int nbr_hosts = 0;
        for (Host host: parser.hosts()) {
            nbr_hosts++;
            System.out.println(host.getId());
            System.out.println("Human-readable IP: " + host.getIp());
            System.out.println("Human-readable Port: " + host.getPort());
            System.out.println();
        }
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
        int server_id = Integer.parseInt(parseConfig(parser)[1]) ;

        String myIp = "";
        int serverPort = 0;
        for (Host host: parser.hosts()) {
            if(host.getId() == server_id){
                myIp = host.getIp();
                serverPort = host.getPort();
                break;
            }
        }
        InetAddress address = null;
        try{
            address = InetAddress.getByName(myIp);
        }catch(UnknownHostException e){
            System.out.println("error");
        }
        
        System.out.println(address.toString());
        if(parser.myId() == server_id){
            System.out.println("I am a server");
            server = new EchoServer(serverPort, nbr_hosts); 
            server.listen();
        }

        else {
            System.out.println("I am a client");
            System.out.println("Sending to " + serverPort);
            client = new EchoClient(parser.myPort(), address);
            client.send(serverPort, parser.myId(), parser.myPort(), nbr_messages);
        }
        
        String ahi = "ahi";
        System.out.println("Broadcasting and delivering messages...\n");

        // After a process finishes broadcasting,
        // it waits forever for the delivery of messages.
        while (true) {
            // Sleep for 1 hour
            Thread.sleep(60 * 60 * 1000);
        }
    }
}
