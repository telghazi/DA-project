package cs451;

// Java program to illustrate Server side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
  
public class EchoServer{

    private DatagramSocket ds;
    private byte[] receive;

    public EchoServer(int listening_port){
        // Step 1 : Create a socket to listen at port
        
        try {
            ds = new DatagramSocket(listening_port);
        }catch(SocketException e){
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
                ds.receive(DpReceive);
            }catch(IOException e){
                System.out.println("An error occurred");
            }
            System.out.println("Client:-" + data(receive));
  
            // Exit the server if the client sends "bye"
            if (data(receive).toString().equals("bye"))
            {
                System.out.println("Client sent bye.....EXITING");
                break;
            }
  
            // Clear the buffer after every message.
            receive = new byte[65535];
        }
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