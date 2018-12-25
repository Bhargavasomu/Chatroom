import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;

public class Chatroom
{
	// Map of all chatrooms and their corresponding objects
	public static Hashtable<String, Chatroom> allValidChatrooms = new Hashtable<String, Chatroom>();
	// Name of the chatroom
	String chatroomName;
	// List of all the users present in the chatroom
	ArrayList<String> usersInChatroom;

	// Constructor
	public Chatroom(String chatroomName)
	{
		this.chatroomName = chatroomName;
		usersInChatroom = new ArrayList<String>();
		// Add this chatroom to list of all valid chatrooms
		allValidChatrooms.put(this.chatroomName, this);
	}

	public void addUser(String username) throws UnknownHostException, IOException
	{
		// First send a broadcast that this user is added to chatroom, to the existing users
		this.broadcastMsg(null, "Server : User " + username + " has been added to the chatroom");

		// Adding user to chatroom list
		usersInChatroom.add(username);
	}

	public void removeUser(String username) throws UnknownHostException, IOException
	{
		// Removing user from chatroom list
		usersInChatroom.remove(username);

		// Then send a broadcast that this user left the chatroom
		this.broadcastMsg(null, "Server : User " + username + " has left the chatroom");

		// Destroy Chatroom if there are no users present in it
		if (usersInChatroom.size() == 0)
		{
			allValidChatrooms.remove(chatroomName);
		}
	}

	public String showAllUsersChatroom()
	{
		String all_users_chatroom = "";
		for(String username : usersInChatroom)
		{
			all_users_chatroom += (username + ";");
		}
		all_users_chatroom = all_users_chatroom.substring(0, all_users_chatroom.length() - 1);

		return all_users_chatroom;
	}

	public void broadcastMsg(String userWhoSentMsg, String msg) throws UnknownHostException, IOException
	{
		// userWhoSentMsg is null, means that the msg is to be sent to all.

		for(String username : usersInChatroom)
		{
			if (username != userWhoSentMsg)
			{
				if (userWhoSentMsg != null)
					msg = userWhoSentMsg + ": " + msg;
				ClientHandler.users.get(username).sendMsgClient(msg);
			}
		}
	}

	public void broadcastFile(String userWhoSentFile, String fileName, String transferMode) throws UnknownHostException, IOException
	{
		// userWhoSentMsg is null, means that the msg is to be sent to all.

		for(String username : usersInChatroom)
		{
			if (username == userWhoSentFile)
				continue;
			ClientHandler.users.get(username).sendFileClient(userWhoSentFile, fileName, transferMode);
		}
	}

}
