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
			if (userList.size() == 0) 
			{
				User user = new User(username, client);
				handler = new ClientHandler(client, userList, user);
				
				userList.add(user);
				System.out.println("\nNew client: '"+ user.getUsername() + "' accepted.\n");
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
						System.out.println(username + " Has been Rejected *Duplicate Username*" );
						dout.writeUTF("fail");
					} 
				} 
				if (!usernameTaken) 
				{
					User user = new User(username, client);
					handler = new ClientHandler(client, userList, user);
					
					userList.add(user);
					System.out.println("\nNew client: '"+ user.getUsername() + "' accepted.\n");
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
	ObjectOutputStream oos;
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
		String users = "¥¥_userlist_¥¥\n";
		Boolean connected = true;
		
		for (int i = 0; i < userList.size(); i++) 
		{
			users += userList.get(i).getUsername() + "\n";
		}
		try 
		{
			sentToAll("* "+user.getUsername() + " Entered the chat! * \n", 1);
			sentToAll(users, 1);
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
		}
		do
		{
			try 
			{
				received = din.readUTF();
				
				if (received.length() > 16 && received.substring(0, 16).equals("¥¥_disconnect_¥¥")) 
				{
					users = "¥¥_userlist_¥¥\n";
					for (int i = 0; i < userList.size(); i++) 
					{
						if (userList.get(i).getUsername().equals(received.substring(16))) 
						{
							System.out.println("removing user " + userList.get(i).getUsername());
							userList.get(i).getSocket().close();
							userList.remove(i);
							connected = false;
							
							for (int j = 0; j < userList.size(); j++) 
							{
								users += userList.get(j).getUsername() + "\n";
							}
							sentToAll(users, 1);
							sentToAll("* "+received.substring(16) + " Left the chat! * \n", 1);
						}
					}
				} 
				else
					sentToAll(received, 2);

			} 
			catch (IOException e ) 
			{
				e.printStackTrace();
			}
			
		}while (connected);
	}
	
	// type of message 1=system message 2=chat message
	void sentToAll(String msg, int typeOfMessage) throws IOException
	{
		if (typeOfMessage == 1) 
		{
			for (int i = 0; i < userList.size(); i++) 
			{
				dout = new DataOutputStream( userList.get(i).getSocket().getOutputStream());
				dout.writeUTF(msg);
				
				System.out.println("wang shaft " + userList.get(i).getUsername() + "" + userList.get(i).getSocket());
			}
		}
		
		if (typeOfMessage == 2) 
		{
			for (int i = 0; i < userList.size(); i++) 
			{
				dout = new DataOutputStream( userList.get(i).getSocket().getOutputStream());
				dout.writeUTF(user.getUsername() + ": " +msg+"\n");
				
			}
		} 
	}
}

