import java.net.Socket;


public class User 
{

	private String username;
	private Socket socket;
	private String colour;
	
	public User (String username, Socket socket, String colour)
	{
		this.username = username;
		this.socket = socket;
		this.colour = colour;
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public Socket getSocket()
	{
		return socket;
	}
	public String getColour()
	{
		return colour;
	}
	public void setColour(String colour)
	{
		this.colour = colour;
	}
}
