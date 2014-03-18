import java.io.*;
import java.net.*;
import java.util.*;

public class Server
{
	@SuppressWarnings("resource")
	public static void main(String[] args)
							throws IOException
	{
		ServerSocket serverSocket = null;
		final int PORT = 1234;
		Socket client;
		ClientHandler handler;
		String username;
		DataInputStream din;
		DataOutputStream dout;

		ArrayList<User> userList = new ArrayList<User>();
		
		try
		{
			serverSocket = new ServerSocket(PORT);
		}
		catch (IOException ioEx)
		{
			System.out.println("\nUnable to set up port!");
			System.exit(1);
		}

		System.out.println("\nServer running...\n");
		
		do
		{
			client = serverSocket.accept();
			din = new DataInputStream( client.getInputStream());
			dout = new DataOutputStream(client.getOutputStream());
			//Wait for client.
			username = din.readUTF();
			
			// if first client
			if (userList.size() == 0) {
				User user = new User(username, client);
				userList.add(user);
				
				System.out.println("\nNew client: '"+ user.getUsername() + "' accepted.\n");
				handler = new ClientHandler(client, userList, user);
				dout.writeUTF("success");
				handler.start();
			} 
			else 
			{
				// check if username is taken
				boolean usernameTaken = false;
				
				for (int i = 0; i < userList.size(); i++)
				{
					if (userList.get(i).getUsername().equals(username)) 
					{
						usernameTaken = true;
						System.out.println("well shit" + userList.size() );
						dout.writeUTF("fail");
					} 
				} 
				if (usernameTaken == false) {
					User user = new User(username, client);
					userList.add(user);
					
					System.out.println("\nNew client: '"+ user.getUsername() + "' accepted.\n");
					handler = new ClientHandler(client, userList, user);
					dout.writeUTF("success");
					handler.start();
				}
			}
		}while (true);
	}
}

class ClientHandler extends Thread
{
	private Socket client;

	DataInputStream din;
	DataOutputStream dout;
	ArrayList<User> userList;
	User user;
	
	public ClientHandler(Socket socket, ArrayList<User> userList, User user) throws IOException
	{
		client = user.getSocket();
		this.userList = userList;
		din = new DataInputStream( client.getInputStream());
		dout = new DataOutputStream( client.getOutputStream());
		this.user = user;
	}

	public void run() 
	{
		String received = null;
		try {
			sentToAll(user.getUsername() + " Entered the chat! \n", 1);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		do
		{
			try {
				received = din.readUTF();
				//dout.writeUTF(user.getUsername()+": "+ received);
				sentToAll(received, 2);

			} catch (IOException e ) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}while (!received.equals("QUIT"));

		try
		{
			System.out.println("Closing down connection...");
			client.close();
		}
		catch(IOException ioEx)
		{
			System.out.println("* Disconnection problem! *");
		}
	}
	
	// type of message 0=user disconnected 1=user connected 2=user chat message 
	void sentToAll(String msg, int typeOfMessage) throws IOException{
		
		if (typeOfMessage == 1) {
			for (int i = 0; i < userList.size(); i++) {
				dout = new DataOutputStream( userList.get(i).getSocket().getOutputStream());
				dout.writeUTF(msg);
			}
		}
		
		if (typeOfMessage == 2) {
			for (int i = 0; i < userList.size(); i++) {
				dout = new DataOutputStream( userList.get(i).getSocket().getOutputStream());
				dout.writeUTF(user.getUsername() + ": " +msg+"\n");
			}
		} 
		
		
	}
}

