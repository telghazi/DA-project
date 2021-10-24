package cs451;

// Java program to illustrate Client side
// Implementation using DatagramSocket
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class EchoClient{

	private DatagramSocket ds;
	private InetAddress ip;
	private byte buf[];

	public EchoClient(){
		
		// Step 1:Create the socket object for
		// carrying the data.
		try {
			ds = new DatagramSocket();
		}catch(SocketException e){
			System.out.println("An error occurred");
		}

		try {
			ip = InetAddress.getLocalHost();
			buf = null;
		}catch(UnknownHostException e){
			System.out.println("An error occurred");
		}
	}

	public void send(int listening_port){

		Scanner sc = new Scanner(System.in);
		// loop while user not enters "bye"
		while (true)
		{
			String inp = sc.nextLine();

			// convert the String input into the byte array.
			buf = inp.getBytes();

			// Step 2 : Create the datagramPacket for sending
			// the data.
			DatagramPacket DpSend =
				new DatagramPacket(buf, buf.length, ip, listening_port);

			// Step 3 : invoke the send call to actually send
			// the data.
			try{
				ds.send(DpSend);
			}catch(IOException e){
				System.out.println("An error occurred");
			}
			// break the loop if user enters "bye"
			if (inp.equals("bye"))
				break;
		}
	}
}
