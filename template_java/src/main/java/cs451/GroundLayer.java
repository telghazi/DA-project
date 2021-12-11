/* package cs451;

// Java program to illustrate Server side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.io.FileWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Collection;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import cs451.Host;
  
public class EchoServer{

    private Host process;
    private DatagramSocket sendSocket;
	private DatagramSocket receiveSocket;
	private HashSet<String> broadcastMessages;
    private byte[] receive;
    private byte buf[];
    private ConcurrentHashMap<Packet, Boolean> messagesToDeliver;

    public GroundLayer(Host host, int nbr_hosts){
        // Step 1 : Create a socket to listen at port



        process = host;
        messagesToDeliver = new ConcurrentHashMap<Packet, Boolean>();

        try {
            recieveSocket = new DatagramSocket(process.getPort());
            sendSocket = new DatagramSocket();
        }catch(SocketException e){
            System.out.println("An error occurred while connecting.");
        }

		buf = null;
		broadcastMessages = new HashSet<String>() ;
		receive = new byte[65535];
    }


    public void listen(){
        DatagramPacket DpReceive = null;
        while (true)
        {
            DpReceive = new DatagramPacket(receive, receive.length);
  
            // recieve the data in byte buffer.
            try { 
                recieveSocket.receive(DpReceive);
            }catch(IOException e){
                System.out.println("An error occurred");
            }

            String[] parsedMessage = data(receive).toString().split(" ");
            String parsedIp, parsedId, parsedPort, parsedText;
            parsedId = parsedMessage[0];
            parsedPort = parsedMessage[1];
            parsedText = parsedMessage[2];

            System.out.println("Client:" + parsedId + " -" + parsedText);

            if(parsedId != "ACK"){
                messagesToDeliver.get(Integer.parseInt(parsedId)).putIfAbsent(parsedText, true);

                sendAck(Integer.parseInt(parsedPort), parsedText);

                String output = "";
                for (ConcurrentHashMap.Entry<Integer, ConcurrentHashMap<String, Boolean>> entry : messagesToDeliver.entrySet()){
                    int id = entry.getKey();

                    ConcurrentHashMap<String, Boolean> messages = entry.getValue();
                    for (ConcurrentHashMap.Entry<String, Boolean> m : messages.entrySet()){
                        output = output + "d" + " " + Integer.toString(id) + " " + m.getKey() + "\n";
                    }
                }
                System.out.println("============================================================");
                System.out.println(output);
                System.out.println("============================================================");
            }
  
            // Clear the buffer after every message.
            receive = new byte[65535];
        }
    }
  

    public void sendAck(int listening_port, String message){
		String inp = "ACK : " + message;
        // convert the String input into the byte array.
        buf = inp.getBytes();

        // Step 2 : Create the datagramPacket for sending
        // the data.
        DatagramPacket DpSend =
            new DatagramPacket(buf, buf.length, ip, listening_port);

        // Step 3 : invoke the send call to actually send
        // the data.
        try{
            sendSocket.send(DpSend);
        }catch(IOException e){
            System.out.println("An error occurred");
        }
    }

/*    public void writeOutput(String outputFile){
        String output = "";
        for (ConcurrentHashMap.Entry<Integer, ConcurrentHashMap<String, Boolean>> entry : messagesToDeliver.entrySet()){
                int id = entry.getKey();

                ConcurrentHashMap<String, Boolean> messages = entry.getValue();
                for (ConcurrentHashMap.Entry<String, Boolean> m : messages.entrySet()){
                    output = output + "d" + " " + Integer.toString(id) + " " + m.getKey() + "\n";
                }
        }
        try {
            FileWriter myWriter = new FileWriter(outputFile, false);
            myWriter.write(output);
            myWriter.close();
            
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }*/
	


/*    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Boolean>> get_messagesToDeliver(){
        return messagesToDeliver;
    }

/*    public boolean waitAck(String k){
		DatagramPacket DpReceive = null;
		boolean rec = false;
		boolean cont = true;
		while(!rec){
			// Step 2 : create a DatgramPacket to receive the data.
			DpReceive = new DatagramPacket(receive, receive.length);
			
			// Step 3 : revieve the data in byte buffer.
			try {
				receiveSocket.setSoTimeout(10);
				receiveSocket.receive(DpReceive);
			}catch(IOException e){
				cont = false;
			}
			if (cont){
				System.out.println("Server:-" + data(receive));
				System.out.println(data(receive).toString().split(" ")[2]);
				System.out.println(k);
				rec = k.equals(data(receive).toString().split(" ")[2]);
			}
			else {
				System.out.println("Message not received");
				return(false);
			}
			

			// Clear the buffer after every message.
			receive = new byte[65535];
		}
		return rec;
    }*/

/*	public void send(int listening_port, int id, int port){

			String inp = Integer.toString(id)+ " " + Integer.toString(port) + " " + Integer.toString(k);
			// convert the String input into the byte array.
			buf = inp.getBytes();

			boolean rec = false;
			// Step 2 : Create the datagramPacket for sending
			// the data.
			while(!rec){
				DatagramPacket DpSend =
					new DatagramPacket(buf, buf.length, ip, listening_port);

				// Step 3 : invoke the send call to actually send
				// the data.
				try{
					sendSocket.send(DpSend);
				}catch(IOException e){
					System.out.println("An error occurred");
				}

				rec = waitAck(Integer.toString(k));
			}
			broadcastMessages.putIfAbsent(Integer.toString(k), true);
		}

	public void writeOutput(String outputFile){
		String output = "";
        for (HashMap.Entry<String, Boolean> entry : broadcastMessages.entrySet()){
                output = output + "b" + " " + entry.getKey() + "\n";
                
        }
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

	public void disconnect(){
		sendSocket.disconnect();
		receiveSocket.disconnect();
	}

	public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0)
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

	public HashMap<String, Boolean> get_broadcastMessages(){
		return(broadcastMessages);
	}
    /*public HashMap<Integer, HashSet> get_syncCollection(){
        return syncCollection;
    }*/
//}

package cs451;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

class GroundLayer {
    TransportLayer transport;
    Thread thread;
    Thread thread2;

    private int listeningPort;
    private boolean receiving = true;
    private DatagramSocket socket;
    private byte[] buf = new byte[256];

    GroundLayer(int listeningPort) {
        this.listeningPort = listeningPort;
        try {
            socket = new DatagramSocket(listeningPort);
        } catch (SocketException e) {
            System.out.println("Error while opening socket");
            e.printStackTrace();
        }

        // Start listening thread
        thread = new Thread(() -> {
            receive();
        });
        thread.start();
    }

    public void receive() {
        while (true) {
            DatagramPacket rcvdPacket = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(rcvdPacket);
            } catch (IOException e) {
                System.out.println("Error while receiving packet");
                e.printStackTrace();
            }

            InetAddress senderAddress = rcvdPacket.getAddress();
            int senderPort = rcvdPacket.getPort();
            String rcvdPayload = new String(rcvdPacket.getData(), 0, rcvdPacket.getLength());

            transport.receive(senderAddress.getHostName(), senderPort, rcvdPayload);

            if ("**STOP**".equals(rcvdPayload)) {
                receiving = false;
            }
        }
        //socket.close();
    }

    public void send(String destHost, int destPort, String payload) {
        
        byte[] buf = payload.getBytes();
        InetAddress address;
        try {
            address = InetAddress.getByName(destHost);
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, destPort);
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("Error while sending payload");
                e.printStackTrace();
            }
        } catch (UnknownHostException e1) {
            System.out.println("Unknown destination hostname");
            e1.printStackTrace();
        }
    }

} 