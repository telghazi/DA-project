package cs451;

// Java program to illustrate Server side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.io.FileWriter;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;

  
public class EchoServer{

    private DatagramSocket recieveSocket;
    private DatagramSocket ackSender;
    private byte[] receive;
    private byte buf[];
    private InetAddress ip;
    private HashMap<Integer, HashMap<String, Boolean>> messagesToDeliver;

    public EchoServer(int listening_port, int nbr_hosts){
        // Step 1 : Create a socket to listen at port
        messagesToDeliver = new HashMap<Integer, HashMap<String, Boolean>>(nbr_hosts);
        for (int k = 1; k < nbr_hosts+1; k++ ){
            messagesToDeliver.put(k, new HashMap<String, Boolean>());
        }
        try {
            recieveSocket = new DatagramSocket(listening_port);
        }catch(SocketException e){
            System.out.println("An error occurred while connecting");
        }

        try {
            ackSender = new DatagramSocket();
        }catch(SocketException e){
        System.out.println("An error occurred while connecting");
        }

        try {
			ip = InetAddress.getLocalHost();
			buf = null;
		}catch(Exception e){
			System.out.println("An error occurred");
		}

        receive = new byte[65535];
    }


    public void listen(){
        DatagramPacket DpReceive = null;
        while (true)
        {
  
            // Step 2 : create a DatgramPacket to receive the data.
            DpReceive = new DatagramPacket(receive, receive.length);
  
            // Step 3 : revieve the data in byte buffer.
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

            messagesToDeliver.get(Integer.parseInt(parsedId)).putIfAbsent(parsedText, true);
            
            sendAck(Integer.parseInt(parsedPort), parsedText);

  
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
            ackSender.send(DpSend);
        }catch(IOException e){
            System.out.println("An error occurred");
        }
    }

    public void writeOutput(String outputFile){
        String output = "";
        for (HashMap.Entry<Integer, HashMap<String, Boolean>> entry : messagesToDeliver.entrySet()){
                int id = entry.getKey();

                HashMap<String, Boolean> messages = entry.getValue();
                for (HashMap.Entry<String, Boolean> m : messages.entrySet()){
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
    }
	

    public void disconnect(){
        //recieveSocket.disconnect();
    }
    // A utility method to convert the byte array
    // data into a string representation.   
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
}