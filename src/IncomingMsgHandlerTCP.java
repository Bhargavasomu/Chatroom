import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class IncomingMsgHandlerTCP extends Thread
{
	/**
	 * This class is used to handle the TCP messages or files that come from the server to the client
	 */
	
	ServerSocket servsock;
	
	public IncomingMsgHandlerTCP(ServerSocket servsock)
	{
		this.servsock = servsock;
	}
	
	@SuppressWarnings("unused")
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				Socket s = servsock.accept();
				
				// obtaining input and out streams
				DataInputStream dis = new DataInputStream(s.getInputStream());
	            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
	            
	            // typeOfReceive is either MSG or FILE
	            String typeOfReceive = dis.readUTF();
	            
	            if (typeOfReceive.equals("MSG"))
	            {
		            String receivedMsg = dis.readUTF();
		            System.out.println("\n\t" + receivedMsg);
	            }
	            else if (typeOfReceive.equals("FILE"))
	            {
	            	// Receiving File Name
	            	String fileName = dis.readUTF();
	            	// Receive fileSize
					long fileSize = Long.parseLong(dis.readUTF());
	            	
	            	/** Receiving <file> from <user> */
	            	String initMsg = dis.readUTF();
	            	System.out.println("\n\t" + initMsg);
	            	
	            	/** Receiving the physical file */
	            	
	            	// Attempting to create a new file
					File file = new File(fileName);
					// file.createNewFile creates a new file if not present, else if present does nothing
					file.createNewFile();
					
					long bytesReceived = 0;
					
					// FileStream writer
					OutputStream out = new FileOutputStream(file);
					
					byte[] bytes = new byte[16*1024];

			        int count;
			        while(bytesReceived <= fileSize)
			        {
			        	count = dis.read(bytes);
			            out.write(bytes, 0, count);
			            bytesReceived += bytes.length;
			        }
			        out.close();
			        
			        System.out.println("\tReceived File");
	            }
	            
	            System.out.print(">> ");
	            s.close();
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
