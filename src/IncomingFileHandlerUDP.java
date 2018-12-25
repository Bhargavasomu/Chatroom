import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class IncomingFileHandlerUDP extends Thread
{
	/**
	 * This class is used to handle the UDP messages or files that come from the server to the client
	 */
	DatagramSocket ds;

	// Constructor
	public IncomingFileHandlerUDP(DatagramSocket ds)
	{
		this.ds = ds;
	}

	// A utility method to convert the byte array data into a string representation.
    public static StringBuilder data(byte[] a)
    {
        if (a == null)
            return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while ((i<a.length) && (a[i] != 0))
        {
            ret.append((char) a[i]);
            i++;
        }
        return ret;
    }

	@Override
	public void run()
	{
		byte[] receive = null;
		DatagramPacket pktReceive;
		try
		{
			while(true)
			{
				// Initializing the variables
				receive = new byte[16*1024];
				pktReceive = new DatagramPacket(receive, receive.length);

				// Receiving filename, filesize, userWhoSentFile seperated by space
				ds.receive(pktReceive);
				String comb = data(receive).toString();
				String[] temp = comb.split(" ");
				String fileName = temp[0];
				long fileSize = Long.parseLong(temp[1]);
				String userWhoSentMsg = temp[2];
				long bytesReceived = 0;

				// Attempting to create a new file
				File file = new File(fileName);
				// file.createNewFile creates a new file if not present, else if present does nothing
				file.createNewFile();

				// FileStream writer
				OutputStream out = new FileOutputStream(file);

				System.out.println("\n\t Receiving " + fileName + " from " + userWhoSentMsg);
				long packetsNum = (fileSize + (16*1024)-1)/(16*1024);
				System.out.println("Number of Packets that should server be receiving : " + Long.toString(packetsNum));
				int packetsReceived = 0;
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

		        System.out.println("\tReceived File");
		        System.out.print(">> ");
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
