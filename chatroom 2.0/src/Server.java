import java.io.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
		ClientHandler handler ;
		String username, password, requestType;
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
			boolean userFound = false;
			
			client = serverSocket.accept();
			din = new DataInputStream( client.getInputStream());
			dout = new DataOutputStream(client.getOutputStream());
			
			Connection connection = null;
			try
			{
				Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
				connection = DriverManager.getConnection(
									"jdbc:odbc:USERS","","");
			}
			catch(ClassNotFoundException cnfEx)
			{
				System.out.println("* Unable to load driver! *");
				System.exit(1);
			}
			catch(SQLException sqlEx)
			{
				System.out.println(
								"* Cannot connect to database! *");
				System.exit(1);
			}
			
			ResultSet results = null;
			try
			{
				Statement statement = connection.createStatement();
				results = statement.executeQuery(
										"SELECT * FROM Users");	
			}
			catch(SQLException sqlEx)
			{
				System.out.println("* Cannot execute query! *");
				System.exit(1);
			}
			
			//Wait for client.
			
			requestType = din.readUTF();
			System.out.println(requestType);
			switch (requestType) 
			{
			case "userLoginRequest":
				username = din.readUTF();
				password = din.readUTF();
				
				System.out.println(username + password);
				// Check user is in database 
					
				try 
				{
					while (results.next())
					{
						if (results.getString("Username").equals(username) && results.getString("Password").equals(password)) 
						{
							userFound = true;
							break;
						}
					}
				} 
				catch (SQLException sqlEx) 
				{
					System.out.println("error loop" + sqlEx);
					System.exit(1);
				}
			
				//
				// if first client
				if (userList.isEmpty() && userFound) 
				{
					User user = new User(username, client);
					handler = new ClientHandler(client, userList, user);
					
					userList.add(user);
					System.out.println("\nNew client: '"+ user.getUsername() + "' accepted.\n");
					dout.writeUTF("success");
					handler.start();
				} 
				else if (userFound)
				{
					// check if username is taken
					boolean usernameTaken = false;
					
					for (int i = 0; i < userList.size(); i++)
					{
						if (userList.get(i).getUsername().equals(username)) 
						{
							usernameTaken = true;
							System.out.println(username + " Has been Rejected *Duplicate Username*" );
							dout.writeUTF("Duplicate");
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
				else 
				{
					dout.writeUTF("notFound");
					System.out.println(username + " Has been Rejected *not found in database* " );
				}
				break;
				//
			case "userRegistrationRequest":
				
				username = din.readUTF();
				password = din.readUTF();
				
				try 
				{
					while (results.next())
					{
						if (results.getString("Username").equals(username)) 
						{
							userFound = true;
							break;
						}
					}
				} 
				catch (SQLException sqlEx) 
				{
					System.out.println("error loop" + sqlEx);
					System.exit(1);
				}
				
				if (userFound) 
				{
					dout.writeUTF("username taken");
				} 
				else 
				{
					try
					{
						Statement statement = connection.createStatement();
						
						statement.executeUpdate("INSERT INTO Users VALUES ('"
															+username+"', '"
															+password+"');");
						
						System.out.println(String.format("%s - Added to Users database", username));
					}
					catch(SQLException sqlEx)
					{
						System.out.println("* Cannot execute query! *");
						System.exit(1);
					}
					dout.writeUTF("success");
				}
				
				
				break;
			default:
				break;
			}
			
			
		}while (true);
	}

static class ClientHandler extends Thread
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
				else if (received.substring(0,3).equals("/w "))
				{
					String whisperSplit[] = received.split(":");
					String whisperTarget = whisperSplit[0].substring(3);
					String whisperMessage = whisperSplit[1];
					User userTarget = null;
					boolean targetExists = false;
					
					for (User user: userList) 
					{
						if (user.getUsername().equals(whisperTarget))
						{
							targetExists = true;
							userTarget = user;
							break;
						}
					}
					
					// check if target exists and user is not whispering themself
					if (targetExists && !whisperTarget.equals(user.getUsername()))
					{
						System.out.println(whisperTarget + whisperMessage);
						sendToTarget(whisperMessage, user, userTarget);
					}
					else
					{
						System.out.println(whisperTarget+" not Found");
						whisperMessage = whisperTarget+" not found\n";
						sendToTarget(whisperMessage, user, userTarget);
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
	
	void sendToTarget(String msg, User user, User userTarget) throws IOException
	{
		if (userTarget == null)
		{
			dout = new DataOutputStream( user.getSocket().getOutputStream());
			dout.writeUTF(msg);
		}
		else
		{
			dout = new DataOutputStream( user.getSocket().getOutputStream());
			dout.writeUTF("Whisper to " +userTarget.getUsername()+ ":" + msg);
			
			dout = new DataOutputStream( userTarget.getSocket().getOutputStream());
			dout.writeUTF("Whisper from "+ user.getUsername()+":"+ msg);
		}
	}
	
	// type of message 1=system message 2=chat message
	void sentToAll(String msg, int typeOfMessage) throws IOException
	{
		switch (typeOfMessage) 
		{
		case 1:
			for (User user:userList) 
				{
					dout = new DataOutputStream( user.getSocket().getOutputStream());
					dout.writeUTF(msg);
					
					System.out.println("wang shaft " + user.getUsername());
				}
			break;
		case 2:
			for (User receivingUser:userList) 
				{
					dout = new DataOutputStream( receivingUser.getSocket().getOutputStream());
					dout.writeUTF(user.getUsername() + ": " +msg+"\n");
					
				}
			break;
		default:
			break;
			}
		}
	}
}

