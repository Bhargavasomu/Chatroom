import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client 
{
	/**
	 * This class is meant for the client objects.
	 * It also contains the necessary functions needed to serve the functionality
	 * of a client
	 */
	
	String username;
	// port number through which the object acts as client for TCP
	int clientPortNumTCP = 5432;
	// port number through which the object acts as client for UDP
	int clientPortNumUDP = 5433;
	// port number through which the object acts as server
	int serverPortNum;
	// IP address of client machine
	String clientIP;
	// IP address of main server
	String serverIP;
	// ServerSocket for acting as server during broadcast
	ServerSocket servsock;
	// Thread on which the client acts as TCP server
	Thread serverBehaviourThreadTCP;
	// Thread on which the client acts as UDP server
	Thread serverBehaviourThreadUDP;
	
	// TCP related variables
	Socket tcp_s;
	DataInputStream tcp_dis;
	DataOutputStream tcp_dos;
	
	// TCP Socket for client 
	ServerSocket tcp_ss;
	
	// UDP related variables
	DatagramSocket udp_s;
	DatagramSocket udpServerDs;
	
	
	// Below is the constructor
	public Client(String username, String clientIP, String serverIP) throws UnknownHostException, IOException
	{
		this.username = username;
		this.clientIP = clientIP;
		this.serverIP = serverIP;
		tcp_s = new Socket(this.serverIP, clientPortNumTCP);
		tcp_dis = new DataInputStream(tcp_s.getInputStream());
		tcp_dos = new DataOutputStream(tcp_s.getOutputStream());
		
		udp_s = new DatagramSocket();
	}
	
	public void setServerPortNum(int portNum)
	{
		this.serverPortNum = portNum; 
	}
	
	public void startServerThread() throws IOException
	{
		// Starting TCP server
		servsock = new ServerSocket(serverPortNum);
		serverBehaviourThreadTCP = new IncomingMsgHandlerTCP(servsock);
		serverBehaviourThreadTCP.start();
		
		// Starting UDP server
		udpServerDs = new DatagramSocket(serverPortNum);
		serverBehaviourThreadUDP = new IncomingFileHandlerUDP(udpServerDs);
		serverBehaviourThreadUDP.start();
	}
	
	public void closeConnection() throws IOException
	{
		tcp_s.close();
		tcp_dis.close();
		tcp_dos.close();
		udp_s.close();
	}
	
	public String sendMsgToServer(String msg) throws IOException
	{
		tcp_dos.writeUTF(msg);
		String receivedMsg = tcp_dis.readUTF();
		
		String seperator = "---------------------------------------";
		
		if (msg.equals("list users") && !receivedMsg.equals("Please join a chatroom to see the users"))
		{
			System.out.println();
			System.out.println("USERS IN CHATROOM");
			System.out.println(seperator);
			String[] names = receivedMsg.split(";");
			for (String name : names)
			{
				System.out.println(name);
			}
			receivedMsg = "";
		}
		else if (msg.equals("list all users"))
		{
			System.out.println();
			System.out.println("ALL USERS CONNECTED");
			System.out.println(seperator);
			String[] names = receivedMsg.split(";");
			for (String name : names)
			{
				System.out.println(name);
			}
			receivedMsg = "";
		}
		else if (msg.equals("list chatrooms") && !receivedMsg.equals("No Chatrooms Created Yet"))
		{
			System.out.println();
			System.out.println("ALL CHATROOMS");
			System.out.println(seperator);
			String[] names = receivedMsg.split(";");
			for (String name : names)
			{
				System.out.println(name);
			}
			receivedMsg = "";
		}
		
		return receivedMsg;
	}
	
	public String sendFileToServer(String msgToSend, String fileName, String transferMode) throws IOException
	{
		tcp_dos.writeUTF(msgToSend);
		
		File file = new File(fileName);
		long fileLengthBytes = file.length();
		
		if (transferMode.equals("tcp"))
		{
			tcp_dos.writeUTF(Long.toString(fileLengthBytes));
			
			if (fileLengthBytes == 0)
			{
				return "Cannot Send Empty File";
			}
			
			// Reading File in packets of 16KB and sending it to server
	        byte[] bytes = new byte[16 * 1024];
	        try
	        {
				InputStream in = new FileInputStream(file);
				System.out.println("Sending File " + fileName);
				
				long bytesSent = 0;
				int count;
		        while (bytesSent <= fileLengthBytes)
		        {
		        	count = in.read(bytes);
		            tcp_dos.write(bytes, 0, count);
		            bytesSent += bytes.length;
		        }
		        in.close();
		        
		        return "File Sent";
			}
	        catch (FileNotFoundException e) 
	        {
				return "File Not Found";
			}
		}
		
		else
		{
			InetAddress ip = InetAddress.getByName(serverIP);
			
			byte[] bytes = null;
			bytes = Long.toString(fileLengthBytes).getBytes();
			
			DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, ip, clientPortNumUDP);
			udp_s.send(pkt);
			
			if (fileLengthBytes == 0)
			{
				return "Cannot Send Empty File";
			}
			
			// Reading File in packets of 16KB and sending it to server
	        bytes = new byte[16 * 1024];
	        try
	        {
				InputStream in = new FileInputStream(file);
				System.out.println("Sending File " + fileName);
				
				long bytesSent = 0;
				int count;
		        while (bytesSent <= fileLengthBytes)
		        {
		        	count = in.read(bytes);
		        	pkt = new DatagramPacket(bytes, count, ip, clientPortNumUDP);
		        	udp_s.send(pkt);
		            bytesSent += bytes.length;
		        }
		        in.close();
		        
		        return "File Sent";
			}
	        catch (FileNotFoundException e) 
	        {
				return "File Not Found";
			}
		}
	}
	
}
 