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
				connection = DriverManager.getConnection(
						"jdbc:odbc:USERS","","");
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
				String colour = null;
				
				System.out.println(username + password);
				// Check user is in database 
					
				try 
				{
					while (results.next())
					{
						if (results.getString("Username").equals(username) && results.getString("Password").equals(password)) 
						{
							colour = results.getString("Colour");
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
					User user = new User(username, client, colour);
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
						User user = new User(username, client, colour);
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
				
				String newUserColour;
				
				username = din.readUTF();
				password = din.readUTF();
				newUserColour = din.readUTF();
				
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
															+password+"', '"
															+newUserColour+"');");
						System.out.println("INSERT INTO Users VALUES ('"
								+username+"', '"
								+password+"', '"
								+newUserColour+"');");
						
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
			sentToAll("¥¥_*"+user.getUsername() + " Entered the chat! * \n", 1);
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
							sentToAll("¥¥_*"+received.substring(16) + " Left the chat! * \n", 1);
						}
					}
				} 
				else if (received.length() > 3 && received.substring(0,3).equals("/w "))
				{
					String whisperSplit[] = received.split(":", 2);
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
						sendToTarget("¥¥_*"+whisperMessage, user, null);
					}
					
					
				}
				else if (received.length() > 3 && received.substring(0,3).equals("/c "))
				{
					String colour = received.substring(3).toUpperCase().trim();
					System.out.println(colour);
					
					if (colour.matches("BLACK|BLUE|CYAN|GREEN|ORANGE")) 
					{
						
						Connection connection = null;
						try
						{
							connection = DriverManager.getConnection(
									"jdbc:odbc:USERS","","");
						}

						catch(SQLException sqlEx)
						{
							System.out.println(
											"* Cannot connect to database! *");
							System.exit(1);
						}
						
						try
						{
							Statement statement = connection.createStatement();
							statement.executeUpdate("UPDATE Users SET Colour ='"
														+colour+"' WHERE Username ='"
														+user.getUsername()+"';");

						}
						catch(SQLException sqlEx)
						{
							System.out.println("* Cannot execute query! *");
							System.exit(1);
						}
						
						System.out.println(colour);
						for (User user: userList) 
						{
							if (this.user.getUsername().equals(user.getUsername()))
							{
								this.user.setColour(colour);
								sendToTarget("¥¥_*Color Changed to " + colour + "\n", user, null);
								break;
							}
						}
					}
					else 
					{
						sendToTarget("¥¥_*Invalid colour\n", user, null);
					}
					
				}
				else if (received.equals("¥¥_Audio_¥¥"))
				{
					sendFileToClient(user.getSocket(), "cuckoo.au");
				}
				else if (received.equals("¥¥_Video_¥¥"))
				{
					sendFileToClient(user.getSocket(), "MoonWalk.mpeg");
				}
				else if (received.equals("¥¥_Image_¥¥"))
				{
					sendFileToClient(user.getSocket(), "hatman.gif");
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
			dout.writeUTF("¥¥_Whisper to " +userTarget.getUsername()+ ":" + msg+"\n");
			
			dout = new DataOutputStream( userTarget.getSocket().getOutputStream());
			dout.writeUTF("¥¥_Whisper from "+ user.getUsername()+":"+ msg+"\n");
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
				}
			break;
		case 2:
			for (User receivingUser:userList) 
				{
					dout = new DataOutputStream( receivingUser.getSocket().getOutputStream());
					dout.writeUTF(user.getColour()+" "+user.getUsername() + ": " +msg+"\n");
					
				}
			break;
		default:
			break;
		}
	}
	
	void sendFileToClient(Socket socket, String fileName) throws IOException
	{
		Socket mediaSocket = null;
		InetAddress host = InetAddress.getLocalHost();

	    mediaSocket = new Socket(host, 1235);
		
	    File file = new File("src/serverFolder/"+fileName);
	    // Get the size of the file
	    long length = file.length();
	    if (length > Integer.MAX_VALUE) {
	        System.out.println("File is too large.");
	    }
	    byte[] bytes = new byte[(int) length];
	    FileInputStream fis = new FileInputStream(file);
	    BufferedInputStream bis = new BufferedInputStream(fis);
	    BufferedOutputStream out = new BufferedOutputStream(mediaSocket.getOutputStream());

	    int count;

	    while ((count = bis.read(bytes)) > 0) {
	        out.write(bytes, 0, count);
	    }

	    out.flush();
	    out.close();
	    fis.close();
	    bis.close();
	    mediaSocket.close();
	}
	
}
}

