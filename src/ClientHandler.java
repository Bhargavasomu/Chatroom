import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientHandler extends Thread
{
	public static int NumConnectedClients = 0;
	// This hashtable(synchronized automatically) is to associate the usernames with their threads
	public static Hashtable<String, ClientHandler> users = new Hashtable<String, ClientHandler>();

	// Stores the name of the chatroom the user is present in. It is default null
	String nameExistingChatroom;
	final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    final DatagramSocket ds;
    String username;
    // IP address of the client
    String clientIP;
    // portnumber for which the client acts like a server
    int clientPortNum;

	public ClientHandler(DatagramSocket ds, Socket s, DataInputStream dis, DataOutputStream dos)
    {
		this.ds = ds;
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.username = null;
        this.nameExistingChatroom = null;
        this.clientPortNum = 0;
        this.clientIP = null;
        ClientHandler.NumConnectedClients += 1;
    }

	public String getUserName()
	{
		return username;
	}

	public int getServerPortNum()
	{
		return clientPortNum;
	}

	public String getNameExistingChatroom()
	{
		return nameExistingChatroom;
	}

	public void setNameExistingChatroom(String chatroomName)
	{
		nameExistingChatroom = chatroomName;
	}

	// A utility method to convert the byte array data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while ((i < a.length) && (a[i] != 0))
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

	@SuppressWarnings("unused")
	public void sendMsgClient(String msg) throws UnknownHostException, IOException
	{
		Socket s = new Socket(clientIP, clientPortNum);

		DataInputStream tcp_dis = new DataInputStream(s.getInputStream());
		DataOutputStream tcp_dos = new DataOutputStream(s.getOutputStream());

		// Sending Tag first and then the msg
		tcp_dos.writeUTF("MSG");
		tcp_dos.writeUTF(msg);

		s.close();
	}

	@SuppressWarnings("unused")
	public void sendFileClient(String user, String fileName, String transferMode) throws UnknownHostException, IOException
	{
		// user is the username who is broadcasting the file

		if (transferMode.equals("tcp"))
		{
			Socket s = new Socket(clientIP, clientPortNum);

			DataInputStream tcp_dis = new DataInputStream(s.getInputStream());
			DataOutputStream tcp_dos = new DataOutputStream(s.getOutputStream());

			// Sending the tag and then the file
			tcp_dos.writeUTF("FILE");
			tcp_dos.writeUTF(fileName);

			File file = new File(fileName);
			long fileLengthBytes = file.length();
			tcp_dos.writeUTF(Long.toString(fileLengthBytes));

			tcp_dos.writeUTF("Receiving " + fileName + " from " + user);

			// Reading File in packets of 16KB and sending it to server
	        byte[] bytes = new byte[16 * 1024];
	        try
	        {
				InputStream in = new FileInputStream(file);

				long bytesSent = 0;
				int count;
		        while (bytesSent <= fileLengthBytes)
		        {
		        	count = in.read(bytes);
		            tcp_dos.write(bytes, 0, count);
		            bytesSent += bytes.length;
		        }
		        in.close();
			}
	        catch (FileNotFoundException e)
	        {
	        	System.out.println(e);
			}

			s.close();
		}

		else if (transferMode.equals("udp"))
		{
			DatagramSocket ds = new DatagramSocket();
			InetAddress ip = InetAddress.getByName(clientIP);
			// Reading File in packets of 16KB and sending it to server
	        byte[] bytes = new byte[16 * 1024];

	        File file = new File(fileName);
			long fileLengthBytes = file.length();

			// Sending fileName, fileSize and userWhoSentTheFile
	        String preInfo = fileName + " " + Long.toString(fileLengthBytes) + " " + user;
	        byte[] temp = preInfo.getBytes();
	        DatagramPacket pkt = new DatagramPacket(temp, temp.length, ip, clientPortNum);
			ds.send(pkt);

	        try
	        {
				InputStream in = new FileInputStream(file);

				long bytesSent = 0;
				int count;
		        while (bytesSent <= fileLengthBytes)
		        {
		        	count = in.read(bytes);
		        	pkt = new DatagramPacket(bytes, count, ip, clientPortNum);
		        	ds.send(pkt);
		            bytesSent += bytes.length;
		        }
		        in.close();
			}
	        catch (FileNotFoundException e)
	        {
				System.out.println("File Not Found");
			}

	        ds.close();
		}
	}

	public boolean portInUsage()
	{
		Enumeration<String> enumKey = users.keys();
		while(enumKey.hasMoreElements())
		{
		    String key = enumKey.nextElement();
		    if (key.equals(username))
			    continue;
		    ClientHandler val = users.get(key);
		    if (val.getServerPortNum() == this.clientPortNum)
		    {
		    	return true;
		    }
		}
		return false;
	}

	public String getAllConnectedUsers()
	{
		String all_users = "";

		Enumeration<String> enumKey = users.keys();
		while(enumKey.hasMoreElements())
		{
		    String key = enumKey.nextElement();
		    ClientHandler val = users.get(key);
		    all_users += (val.getUserName() + ";");
		}
		all_users = all_users.substring(0, all_users.length() - 1);

		return all_users;
	}

	public String getAllChatrooms()
	{
		if (Chatroom.allValidChatrooms.size() == 0)
			return "No Chatrooms Created Yet";

		String all_chatrooms = "";

		Enumeration<String> enumKey = Chatroom.allValidChatrooms.keys();
		while(enumKey.hasMoreElements())
		{
		    String chatroomName = enumKey.nextElement();
		    all_chatrooms += (chatroomName + ";");
		}
		all_chatrooms = all_chatrooms.substring(0, all_chatrooms.length() - 1);

		return all_chatrooms;
	}

	public String clean(String str)
	{
		/**
		 * Removes unnecessary spaces and newlines.
		 * Returns the string with words having only one space between them.
		 */
		// Removing extra spaces, newlines at the start and end of the string
		str = str.trim();
		// Replace multiple spaces with single space
		str = str.replaceAll("\\s+", " ");

		return str;
	}


	@Override
    public void run()
    {
		String receivedMsg;

		while(true)
		{
            try
            {
            	// receive the cmnd from client
				receivedMsg = dis.readUTF();
				receivedMsg = clean(receivedMsg);

				if(receivedMsg.startsWith("NAME:"))
				{
					this.username = receivedMsg.split(":",-2)[1];

					if (users.containsKey(username))
					{
						// Username already exists
						ClientHandler.NumConnectedClients -= 1;
						this.dos.writeUTF("Username already occupied");
						this.s.close();
						break;
					}

					System.out.println("Client Added : " + username);
					this.dos.writeUTF("Added user");

					// Associating the thread with the username
					ClientHandler.users.put(username, this);

					continue;
				}

				if(receivedMsg.startsWith("PORT:"))
				{
					this.clientPortNum = Integer.parseInt(receivedMsg.split(":",-2)[1]);

					// check if this port is occupied by any other users
					boolean portOccupied = portInUsage();
					if (portOccupied)
					{
						this.dos.writeUTF("Port Already in use");
						this.clientPortNum = 0;
						continue;
					}

					this.dos.writeUTF("Port is ok to use");
					continue;
				}

				if(receivedMsg.startsWith("IP:"))
				{
					this.clientIP = receivedMsg.split(":",-2)[1];
					this.dos.writeUTF("IP Noted");
					continue;
				}

				if(receivedMsg.equals("list all users"))
				{
					String temp = getAllConnectedUsers();
					this.dos.writeUTF(temp);
					continue;
				}

				if (receivedMsg.startsWith("create chatroom"))
				{
					if (nameExistingChatroom != null)
					{
						// Means that he is already part of some existing chatroom
						this.dos.writeUTF("Already part of some Chatroom, please exit to create new Chatroom");
						continue;
					}
					String[] words = receivedMsg.split(" ");
					if (words.length != 3)
					{
						// Invalid command, return error msg
						this.dos.writeUTF("Invalid Command");
						continue;
					}

					String chatroomName = words[2];
					if (Chatroom.allValidChatrooms.contains(chatroomName))
					{
						// Chatroom name specified already exists and cannot be recreated
						this.dos.writeUTF("Chatroom with this name already exists");
						continue;
					}

					// Creating a new chatroom
					Chatroom mychatroom = new Chatroom(chatroomName);
					System.out.println("New Chatroom created : " + chatroomName);

					// Adding the user to the chatroom
					mychatroom.addUser(username);
					nameExistingChatroom = chatroomName;
					System.out.println(username + " added to chatroom " + chatroomName);

					this.dos.writeUTF("Chatroom " + chatroomName + " created successfully");
					continue;
				}

				if (receivedMsg.equals("list chatrooms"))
				{
					String all_chatrooms = getAllChatrooms();
					this.dos.writeUTF(all_chatrooms);
					continue;
				}

				if (receivedMsg.startsWith("join"))
				{
					if (nameExistingChatroom != null)
					{
						// If user is already part of another chatroom, cannot join another chatroom
						this.dos.writeUTF("Already part of some Chatroom, please exit to join new Chatroom");
						continue;
					}
					String[] words = receivedMsg.split(" ");
					if (words.length != 2)
					{
						// Invalid command, return error msg
						this.dos.writeUTF("Invalid Command");
						continue;
					}
					String chatroomName = words[1];
					if (!Chatroom.allValidChatrooms.containsKey(chatroomName))
					{
						// Chatroom name specified doesn't exist and cannot be joined
						this.dos.writeUTF("Chatroom with this name doesn't exist");
						continue;
					}

					Chatroom.allValidChatrooms.get(chatroomName).addUser(username);
					nameExistingChatroom = chatroomName;

					System.out.println(username + " joined chatroom " + chatroomName);

					this.dos.writeUTF("Joined Chatroom " + chatroomName);
					continue;
				}

				if (receivedMsg.startsWith("add"))
				{
					if (nameExistingChatroom == null)
					{
						// If user isn't part of any chatroom, cannot add another user
						this.dos.writeUTF("Please join a Chatroom to add users");
						continue;
					}
					String[] words = receivedMsg.split(" ");
					if (words.length != 2)
					{
						// Invalid command, return error msg
						this.dos.writeUTF("Invalid Command");
						continue;
					}
					String nameUserToBeAdded = words[1];
					if (!ClientHandler.users.containsKey(nameUserToBeAdded))
					{
						// username specified doesn't exist and cannot be added to chatroom
						this.dos.writeUTF("User : " + nameUserToBeAdded + " with this name doesn't exist");
						continue;
					}
					ClientHandler specifiedUserClientHandler = ClientHandler.users.get(nameUserToBeAdded);
					if (specifiedUserClientHandler.getNameExistingChatroom() != null)
					{
						// Means that the specified user is already part of some other chatroom
						this.dos.writeUTF("User : " + nameUserToBeAdded + " is part of some other chatroom and can't be added");
						continue;
					}

					// Adding the specified user to chatroom
					Chatroom.allValidChatrooms.get(nameExistingChatroom).addUser(nameUserToBeAdded);
					specifiedUserClientHandler.setNameExistingChatroom(nameExistingChatroom);

					// Msg to be printed on server side
					System.out.println(nameUserToBeAdded + " was added to the chatroom " + nameExistingChatroom);

					// Msg to be printed on the client side who is trying to add another user
					this.dos.writeUTF("Added user " + nameUserToBeAdded + " to this Chatroom");

					// Msg to appear to the user who is being added
					specifiedUserClientHandler.sendMsgClient("Server : You have been added to chatroom " + nameExistingChatroom);

					continue;
				}

				if (receivedMsg.equals("leave"))
				{
					// If user is not in any chatroom, then throw error
					if (nameExistingChatroom == null)
					{
						this.dos.writeUTF("Please join a chatroom to leave");
						continue;
					}

					// Removing user from chatroom
					Chatroom.allValidChatrooms.get(nameExistingChatroom).removeUser(username);
					String prevChatroomName = nameExistingChatroom;
					nameExistingChatroom = null;

					System.out.println(username + " left the chatroom " + prevChatroomName);
					this.dos.writeUTF("Left Chatroom " + prevChatroomName);
					continue;
				}

				if (receivedMsg.equals("list users"))
				{
					// List all the users present in the chatroom
					if (nameExistingChatroom == null)
					{
						this.dos.writeUTF("Please join a chatroom to see the users");
						continue;
					}

					String all_users_chatroom = Chatroom.allValidChatrooms.get(nameExistingChatroom).showAllUsersChatroom();
					this.dos.writeUTF(all_users_chatroom);
					continue;
				}

				if(receivedMsg.startsWith("reply"))
				{
					// Cmnd not valid if not part of any chatroom
					if (nameExistingChatroom == null)
					{
						this.dos.writeUTF("Please join a chatroom to send messages or files");
						continue;
					}

					// Broadcast msg format
					String msgBroadcastCmndPattern = "reply \"([^\"]*)\"";
					Pattern r = Pattern.compile(msgBroadcastCmndPattern);
					Matcher m = r.matcher(receivedMsg);
					if (m.matches())
					{
						String broadcastMsg = m.group(1);
						Chatroom.allValidChatrooms.get(nameExistingChatroom).broadcastMsg(username, broadcastMsg);

						this.dos.writeUTF("Sent Msg to Chatroom");
						continue;
					}

					// Broadcast file format
					String fileBroadcastCmndPattern = "reply (\\S+) (tcp|udp)";
					Pattern r1 = Pattern.compile(fileBroadcastCmndPattern);
					Matcher m1 = r1.matcher(receivedMsg);
					if (m1.matches())
					{
						String fileName = m1.group(1);
						String transferMode = m1.group(2);

						if (transferMode.equals("tcp"))
						{
							// Attempting to create a new file
							File file = new File(fileName);
							// file.createNewFile creates a new file if not present, else if present does nothing
							file.createNewFile();

							// Receive fileSize
							long fileSize = Long.parseLong(this.dis.readUTF());
							if (fileSize == 0)
								continue;
							long bytesReceived = 0;

							// FileStream writer
							OutputStream out = new FileOutputStream(file);

							byte[] bytes = new byte[16*1024];

					        int count;
					        while(bytesReceived <= fileSize)
					        {
					        	count = this.dis.read(bytes);
					            out.write(bytes, 0, count);
					            bytesReceived += bytes.length;
					        }
					        out.close();

					        Chatroom.allValidChatrooms.get(nameExistingChatroom).broadcastFile(username, fileName, "tcp");
						}

						else if (transferMode.equals("udp"))
						{
							// Attempting to create a new file
							File file = new File(fileName);
							// file.createNewFile creates a new file if not present, else if present does nothing
							file.createNewFile();

							byte[] receive = new byte[16*1024];
							DatagramPacket pktReceive;

							// Receive fileSize
							pktReceive = new DatagramPacket(receive, receive.length);
							ds.receive(pktReceive);
							long fileSize = Long.parseLong(data(receive).toString());
							if (fileSize == 0)
								continue;
							long bytesReceived = 0;

							// FileStream writer
							OutputStream out = new FileOutputStream(file);
							int packetsReceived = 0;
							long packetsNum = (fileSize + (16*1024)-1)/(16*1024);
							System.out.println("Number of Packets that should server be receiving : " + Long.toString(packetsNum));
					        while(bytesReceived <= fileSize)
					        {
					        	receive = new byte[16*1024];
					        	pktReceive = new DatagramPacket(receive, receive.length);
					        	ds.receive(pktReceive);
								packetsReceived += 1;
								System.out.println("Received Packet number : " + Integer.toString(packetsReceived));

					        	String strData = data(receive).toString();

					            out.write(strData.getBytes());
					            bytesReceived += receive.length;
					        }
					        out.close();

					        Chatroom.allValidChatrooms.get(nameExistingChatroom).broadcastFile(username, fileName, "udp");
						}

						continue;
					}

					this.dos.writeUTF("Invalid Command");
					continue;
				}

				if(receivedMsg.equals("Exit") || receivedMsg.equals("exit"))
                {
					System.out.println("User : " + username + " has exited");
					// The number of clients is decreased as he exited
					ClientHandler.NumConnectedClients -= 1;
					// Remove this user from the list of all users connected
					ClientHandler.users.remove(username);
					// Remove the user from the chatroom he is connected to, if at all connected to any chatroom
					if (nameExistingChatroom != null)
						Chatroom.allValidChatrooms.get(nameExistingChatroom).removeUser(username);

					this.dos.writeUTF("Bye Bye " + username);
                    break;
                }

				this.dos.writeUTF("Invalid Command");
				continue;

			}
            catch (IOException e)
            {
            	// If user exits by cntrl-C , it comes to this part of code

            	System.out.println("User : " + username + " has exited");
            	// The number of clients is decreased as he exited
            	ClientHandler.NumConnectedClients -= 1;
            	// Remove this user from the list of all users connected
            	ClientHandler.users.remove(username);
            	// Remove the user from the chatroom he is connected to, if at all connected to any chatroom
				if (nameExistingChatroom != null)
				{
					try
					{
						Chatroom.allValidChatrooms.get(nameExistingChatroom).removeUser(username);
					}
					catch (IOException e1)
					{
						System.out.println("Not able to remove user from the chatroom");
					}
				}

            	break;
			}

		}

		try
        {
            // closing resources
            this.dis.close();
            this.dos.close();
            this.s.close();
        }
		catch(IOException e)
		{
            e.printStackTrace();
        }

    }
}
