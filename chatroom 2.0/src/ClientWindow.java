import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class ClientWindow extends JFrame implements ActionListener, Runnable, KeyListener 

{
	private static final long serialVersionUID = 1L;
	private static JPanel chatScreen;
	private static JPanel loginScreen;
	private static JButton send, btn_login, btn_signup, logout;
	private static JTextArea ta_chat;
	private static JTextArea ta_users;
	public static Socket socket;
	public static int typeOfInputStream;
	public static JTextField tb_username = new JTextField();
	public static JTextField tb_password = new JTextField();
	public static JTextField tb_message = new JTextField();
	public static InetAddress host;
	public static DataInputStream din;
	public static DataOutputStream dout;
	public static String username, password;
	public static JLabel curUser, lbl_error;
	
	public static void main(String[] args) 
	{
		ClientWindow frame =  new ClientWindow();
		
		frame.setTitle("Chat");
		frame.setSize(600, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public ClientWindow() 
	{
		setLayout(new CardLayout());
		
		setUpLoginPanel();
		setUpChatPanel();
	}

	private void setUpChatPanel() 
	{
		// Login panel set up
		chatScreen = new JPanel();
		chatScreen.setLayout(new GridBagLayout());
		
		add(chatScreen, "Login");
		chatScreen.setVisible(false);
		
		// Login panel elements 
		ta_chat = new JTextArea(10,30);
		ta_users = new JTextArea(10,50);
		
		ta_chat.setEditable(false);
		ta_users.setEditable(false);
		
		curUser = new JLabel();
		JScrollPane chatArea = new JScrollPane(ta_chat); 
		JScrollPane userArea = new JScrollPane(ta_users); 
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridBagLayout());
		send = new JButton("Send");
		logout = new JButton("logout");
		GridBagConstraints gbc = new GridBagConstraints();
		Font myFont = new Font("Serif", Font.BOLD, 18); 
		Font chatFont = new Font("Garamond Pro", Font.BOLD, 14); 
		
		ta_chat.setFont(chatFont);
		Font logoFont = new Font("buxton sketch", Font.BOLD, 28);
		JLabel myLogo = new JLabel("TJIM");
		
		myLogo.setFont(logoFont);
		myLogo.setHorizontalAlignment(SwingConstants.CENTER);
		
		Font logoFonttag = new Font("buxton sketch", Font.BOLD, 15);
		JLabel myLogotag = new JLabel("Tom's Java Instant messenger");
		Font curUserFont = new Font("buxton sketch", Font.PLAIN, 14);
		
		curUser.setFont(curUserFont);
		myLogotag.setFont(logoFonttag);
		myLogotag.setHorizontalAlignment(SwingConstants.CENTER);
		
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		//create input panel
		tb_message.setColumns(30);
		tb_message.setFont(myFont);
		buttonsPanel.add(tb_message, gbc);
		gbc.anchor = GridBagConstraints.LAST_LINE_END;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = 1;
		buttonsPanel.add(send, gbc);
		
		//chat area
		gbc.insets = new Insets(5, 10, 5, 10);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0.8;
		gbc.weighty = 0.9;
		
		ta_chat.setLineWrap(true);
		ta_chat.setWrapStyleWord(true);
		
		chatScreen.add(chatArea, gbc);

		//user area
		gbc.gridx = 1;
		ta_users.setText("user area");
		gbc.weightx = 0.2;
		ta_users.setLineWrap(true);
		ta_users.setWrapStyleWord(true);
		chatScreen.add(userArea, gbc);
	
		// input area 
		gbc.weighty = 0.1;
		gbc.gridx = 0;
		gbc.gridy = 1;
		chatScreen.add(buttonsPanel, gbc);
		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.PAGE_END;
		//gbc.weightx = 0.1;
		//gbc.weighty = 0.1;
		gbc.gridx = 1;
		gbc.gridy = 1;

		chatScreen.add(logout, gbc);
		gbc.anchor = GridBagConstraints.PAGE_START;
		//gbc.fill = GridBagConstraints.BOTH;
		
		gbc.fill = GridBagConstraints.NONE;		
		chatScreen.add(myLogo, gbc);
		gbc.anchor = GridBagConstraints.CENTER;
		chatScreen.add(curUser, gbc);
		
		logout.addActionListener(this);
		send.addActionListener(this);
		tb_message.addKeyListener(this);
	}

	private void setUpLoginPanel() 
	{

		// Login panel set up
		loginScreen = new JPanel();
		loginScreen.setLayout(new GridBagLayout());
		
		add(loginScreen, "Login");
		loginScreen.setVisible(true);
		
		// Login panel elements 
		
		// text boxes
		
		btn_login = new JButton();
		btn_signup = new JButton();
		JLabel lbl_username = new JLabel();
		JLabel lbl_password = new JLabel();
		lbl_error = new JLabel();
		GridBagConstraints gbc = new GridBagConstraints();
		Font myFont = new Font("Serif", Font.BOLD, 18);
				
		lbl_username.setText("Username");
		gbc.gridx = 0;
		gbc.gridy = 0;
		loginScreen.add(lbl_username, gbc);
				
		gbc.insets.bottom = 30;
		gbc.gridy = 1;
		tb_username.setColumns(20);
		tb_username.setHorizontalAlignment(JTextField.CENTER);
		tb_username.setFont(myFont);
		loginScreen.add(tb_username, gbc);
		
		gbc.insets.bottom = 0;
		lbl_password.setText("Password");
		gbc.gridy = 2;
		loginScreen.add(lbl_password, gbc);
				
		gbc.insets.bottom = 20;
		gbc.gridy = 3;
		tb_password.setColumns(20);
		tb_password.setHorizontalAlignment(JTextField.CENTER);
		tb_password.setFont(myFont);
		loginScreen.add(tb_password, gbc);
		
		
		//buttons
		gbc.fill = GridBagConstraints.NONE;
		gbc.insets.right = 200;
		gbc.insets.left = 200;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		
		
		gbc.gridy = 4;
		btn_login.setText("Enter ");
		loginScreen.add(btn_login, gbc);
		
		gbc.anchor = GridBagConstraints.WEST;
		//gbc.gridy = 3;
		btn_signup.setText("Signup");		
		loginScreen.add(btn_signup, gbc);
		
		
		gbc.insets.bottom = 0;
		gbc.insets.top = 70;
		gbc.gridy = 5;
		gbc.insets.right = 0;
		gbc.insets.left = 0;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		lbl_error.setForeground(Color.RED);
		lbl_error.setText("");
		loginScreen.add(lbl_error, gbc);
		
		btn_login.addActionListener(this);
		tb_username.addKeyListener(this);
		tb_password.addKeyListener(this);
		
		// close event listener to alert server to remove user and prevent server crash
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	if (loginScreen.isVisible()) 
            	{
            		System.exit(0);
				} 
            	else
	            {
	        		try 
	        		{
						dout.writeUTF("¥¥_disconnect_¥¥" + username);
						System.exit(0);
					} 
	        		catch (IOException e1) 
	        		{
						e1.printStackTrace();
					}
            	}
                
            }
        });
	}

	
	public static void joinServer()throws IOException
	{
		final int PORT = 1234;
		String success;
		
		try
		{
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			System.out.println("\nHost ID not found!\n");
		}

		// make connection to database
		
		Connection connection = null;
		Statement statement = null;
		ResultSet results = null;		
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
		
		
		// query database with log in details
		
		username = tb_username.getText().trim().replace(" ", "_");
		password = tb_password.getText();
		
		if (username.length() > 0) 
		{
			boolean userFound = false;
			
			try
			{
				statement = connection.createStatement();
				results = statement.executeQuery(
										"SELECT * FROM Users");	
			}
			catch(SQLException sqlEx)
			{
				System.out.println("* Cannot execute query! *");
				System.exit(1);
			}
			
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
			
			if (userFound) // user is in the database!
			{
				socket = new Socket(host, PORT);
				din = new DataInputStream( socket.getInputStream() );
				dout = new DataOutputStream( socket.getOutputStream() );
				
				dout.writeUTF(username);
				success = (String)din.readUTF();
				
				if (success.equals("success")) 
				{
					Thread messageListener = new Thread(new ClientWindow(socket, 0));
					
					tb_username.setText("");
					tb_password.setText("");
					loginScreen.setVisible(false);
					chatScreen.setVisible(true);
					curUser.setText("Logged as " + username);
					messageListener.start();
				} 
				else 
				{
					lbl_error.setText("You are logged in somewhere else.");
				}
			}
			else
			{
				lbl_error.setText("User not found");
			}
		} 
		else
		{
			lbl_error.setText("Username must not be empty");
		}
	}
		
	private static void sendMessage() throws IOException
	{
		if (tb_message.getText().trim().length() > 0) 
		{
			dout.writeUTF(tb_message.getText().trim());
			tb_message.setText("");
		}
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == btn_login)
		{
			try 
			{
				lbl_error.setText("");
				joinServer();
			} 
			catch (Exception e2) 
			{
				lbl_error.setText("Connection to server failed.");
			}
		}
		
		
		if (e.getSource() == logout)
		{
			try 
			{
				dout.writeUTF("¥¥_disconnect_¥¥" + username);
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
			
			ta_chat.setText("");
			loginScreen.setVisible(true);
			chatScreen.setVisible(false);
		}
		if(e.getSource() == send)
		{
			try 
			{
				sendMessage();
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
				
	}
	public ClientWindow (Socket socket, int typeOfInputStream) // 0=dataInputStream, 1=objectInputStream 
	{
		ClientWindow.socket = socket;
		ClientWindow.typeOfInputStream = typeOfInputStream;
	}

	@Override
	public void run() 
	{
		switch (typeOfInputStream) 
		{
		case 0: // dataInputStream receiver
			try 
			{
				din = new DataInputStream( socket.getInputStream() );
				
				while (true) 
				{
				// Get the next message
					String message = din.readUTF();
					
					if (message.length() > 14 && message.substring(0, 14).equals("¥¥_userlist_¥¥")) 
					{
						ta_users.setText(message.substring(15));
					} 
					else
					{
						ta_chat.append(message);
					}
				}
				
			} catch( IOException e) { break; }
			
		default:
			break;
		}
	}

	

	@Override 
	// Enter keypress event handlers 
	public void keyReleased(KeyEvent e) 
	{
		if ((e.getSource() == tb_username || e.getSource() == tb_password) && e.getKeyCode() == KeyEvent.VK_ENTER ) 
		{
			try 
			{
				joinServer();
			} 
			catch (Exception e2) 
			{
				lbl_error.setText("Connection to server failed.");
			}
		}
		if (e.getSource() == tb_message && e.getKeyCode() == KeyEvent.VK_ENTER )
		{
			try 
			{
				sendMessage();
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
	}

	@Override // unused
	public void keyPressed(KeyEvent e) {}
	
	@Override // unused
	public void keyTyped(KeyEvent e) {}
}
