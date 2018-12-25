import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client_main 
{

	public static void main(String[] args) throws UnknownHostException, ClassNotFoundException, IOException 
	{
		/**
		 * This is the main interface for the clients. The functionalities are
		 * 		1) Reading username
		 * 		2) Establishing Connection between client and server
		 * 		3) Reading usercommands
		 * 		4) Taking necessary actions as per cmnd
		 */
		String username;
		String msgToSend;
		String acceptanceMsg;
		String responseMsg;
		
		String clientIP = null;
		String serverIP = null;
		
		int serverPortNum;
		
		if (args.length == 2)
		{
			// Taking maxClients number as argument
			clientIP = args[0];
			serverIP = args[1];
		}
		else
		{
			System.err.println("ERR: Not mentioned Client IP or Server IP");
			// Terminate code
			System.exit(0);
		}
		
		Scanner sc = new Scanner(System.in);
		Client clientObject;
		
		// Validating username and checking if server is busy with N clients
		while(true)
		{
			// Taking username from the user
			System.out.print("Username: ");
			username = sc.nextLine();
			
			if (username.contains(";"))
			{
				System.out.println("Username cannot have ; in it");
				continue;
			}
			
			// Creating client object and automatically connecting it to the server
			clientObject  = new Client(username, clientIP, serverIP);
		
			acceptanceMsg = clientObject.sendMsgToServer("NAME:" + username);
			if (acceptanceMsg.equals("Server is Busy, please try later"))
			{
				System.out.println(acceptanceMsg);
				sc.close();
				return;
			}
			else if (acceptanceMsg.equals("Username already occupied"))
			{
				System.out.println(acceptanceMsg);
				continue;
			}
			else
				break;
		}
		
		// Validating that the Port number is not in use by another client
		while(true)
		{
			// Taking the port number where the client will be acting as server
			System.out.print("Port Number for Receiving Messages: ");
			serverPortNum = sc.nextInt();
			// just to capture the newline
			sc.nextLine();
			
			// Sending serverPortNum to server
			acceptanceMsg = clientObject.sendMsgToServer("PORT:" + Integer.toString(serverPortNum));
			if(acceptanceMsg.equals("Port Already in use"))
			{
				System.out.println(acceptanceMsg);
				continue;
			}
			else
			{
				clientObject.setServerPortNum(serverPortNum);
				break;
			}
		}
		
		// Sending the IP address of client to 
		clientObject.sendMsgToServer("IP:" + clientIP);
		
		// To listen for broadcast msgs and msgs coming from the server
		clientObject.startServerThread();
		
		String fileBroadcastCmndPattern = "reply (\\S+) (tcp|udp)";
		Pattern r = Pattern.compile(fileBroadcastCmndPattern);
		
		while(true)
        {
			System.out.print(">> ");
			msgToSend = sc.nextLine();
			
			Matcher m = r.matcher(msgToSend);
			if (m.matches())
			{
				String fileName = m.group(1);
				String transferMode = m.group(2);
				
				responseMsg = clientObject.sendFileToServer(msgToSend, fileName, transferMode);
				System.out.println(responseMsg);
				continue;
			}
		
            responseMsg = clientObject.sendMsgToServer(msgToSend);
             
            // If client sends exit, then close this connection
            if (msgToSend.equals("Exit") || msgToSend.equals("exit"))
            {
                clientObject.closeConnection();
                System.out.println(responseMsg);
                System.out.println("Connection closed");
                sc.close();
                System.exit(0);;
            }
            
            System.out.println(responseMsg);
        }
	}

}
