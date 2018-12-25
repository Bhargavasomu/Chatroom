import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

public class Server_main
{

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException
	{
		/**
		 * This is the main interface for the server. Functionalities are
		 * 		1) Assigning a mini server for each client using threads upto N(Maximum Number of clients it can support)
		 */

		// Maximum number of connections the server can support
		int maxNumClientConnections = 0;
		// port number through which TCP communication happens
		int portNumTCP = 5432;
		// port number through which UDP communication happens
		int portNumUDP = 5433;

		ServerSocket ss = new ServerSocket(portNumTCP);
		DatagramSocket ds = new DatagramSocket(portNumUDP);

		if (args.length == 1)
		{
			// Taking maxClients number as argument
			maxNumClientConnections = Integer.parseInt(args[0]);
		}
		else
		{
			System.err.println("ERR: The maximum number of clients should be the only argument");
			// Terminate code
			System.exit(0);
		}

		while(true)
		{
			Socket s = ss.accept();

			// obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            if (ClientHandler.NumConnectedClients < maxNumClientConnections)
            {
                // create a new thread object
                Thread t = new ClientHandler(ds, s, dis, dos);

                // Invoking the start() method
                t.start();
            }
            else
            {
            	// N clients are already filled up
            	dos.writeUTF("Server is Busy, please try later");
            	dis.close();
            	dos.close();
            	s.close();
            }

		}
	}

}
