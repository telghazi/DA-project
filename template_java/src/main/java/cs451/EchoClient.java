package cs451;

// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.IOException;
import java.io.FileWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;

public class EchoClient{

	private DatagramSocket sendSocket;
	private DatagramSocket ackReceiver;
	private InetAddress ip;
	private byte buf[];
	private byte[] receive;
	private HashMap<String, Boolean> broadcastMessages;

	public EchoClient(int listening_port, InetAddress address){
		
		// Step 1:Create the socket object for
		// carrying the data.
		System.out.println("Opening Socket");
		try {
            ackReceiver = new DatagramSocket(listening_port);
        }catch(SocketException e){
            System.out.println("An error occurred while connecting");
        }

		try {
			sendSocket = new DatagramSocket();
		}catch(SocketException e){
			System.out.println("An error occurred");
		}

		try {
			ip = InetAddress.getLocalHost();
			buf = null;
		}catch(UnknownHostException e){
			System.out.println("An error occurred");
		}

		broadcastMessages = new HashMap<String, Boolean>() ;
		receive = new byte[65535];
	}

	public boolean waitAck(String k){
		DatagramPacket DpReceive = null;
		boolean rec = false;
		boolean cont = true;
		while(!rec){
			// Step 2 : create a DatgramPacket to receive the data.
			DpReceive = new DatagramPacket(receive, receive.length);
			
			// Step 3 : revieve the data in byte buffer.
			try {
				ackReceiver.setSoTimeout(10);
				ackReceiver.receive(DpReceive);
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
    }

	public void send(int listening_port, int id, int port, int nbr_messages){

		for (int k=1 ; k<nbr_messages+1; k++)
		{
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
		ackReceiver.disconnect();
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
}
