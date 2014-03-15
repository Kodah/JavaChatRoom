import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.SwingConstants;

import java.awt.CardLayout;
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
import java.util.Scanner;


public class ClientWindow extends JFrame implements ActionListener, Runnable, KeyListener 

{
	private static final long serialVersionUID = 1L;
	private static JPanel chatScreen;
	private static JPanel loginScreen;
	private JButton send, btn_login, logout;
	private static JTextArea ta_chat;
	private static JTextArea ta_users;
	public static Socket socket;
	public static JTextField tb_username = new JTextField();
	public static JTextField tb_message = new JTextField();
	public static InetAddress host;
	public static DataInputStream din;
	public static DataOutputStream dout;
	public static ObjectInputStream ois;
	public static String username;
	
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
		gbc.weightx = 0.1;
		gbc.weighty = 0.1;
		gbc.gridx = 1;
		gbc.gridy = 1;

		chatScreen.add(logout, gbc);
		gbc.anchor = GridBagConstraints.PAGE_START;
		gbc.fill = GridBagConstraints.BOTH;
		chatScreen.add(myLogo, gbc);

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
		
		//JTextField tb_username = new JTextField();
		btn_login = new JButton();
		JLabel lbl_username = new JLabel();
		GridBagConstraints gbc = new GridBagConstraints();
		Font myFont = new Font("Serif", Font.BOLD, 18);
		gbc.insets = new Insets(5, 5, 0, 0);
		
		lbl_username.setText("Please enter your username");
		gbc.gridx = 0;
		gbc.gridy = 0;
		loginScreen.add(lbl_username, gbc);
		
		gbc.insets.bottom = 10;
		tb_username.setColumns(20);
		tb_username.setHorizontalAlignment(JTextField.CENTER);
		tb_username.setFont(myFont);
		gbc.gridy = 1;
		loginScreen.add(tb_username, gbc);
		
		btn_login.setText("Enter");
		gbc.gridy = 2;
		loginScreen.add(btn_login, gbc);	
		btn_login.addActionListener(this);
		tb_username.addKeyListener(this);
		
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
		Scanner keyboard;
		String success;
		keyboard = new Scanner(System.in);
		
		try
		{
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			System.out.println("\nHost ID not found!\n");
		}

		username = tb_username.getText();
		
		if (username.length() > 0) 
		{
			socket = new Socket(host, PORT);
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );
			
			
			dout.writeUTF(username);
			success = (String)din.readUTF();
			
			if (success.equals("success")) 
			{
				Thread messageListener = new Thread(new ClientWindow(socket));
				
				loginScreen.setVisible(false);
				chatScreen.setVisible(true);
				messageListener.start();
			} 
			else 
			{
				tb_username.setText("Username is taken, sorry.");
			}
		} 
		else
		{
			tb_username.setText("Mustn't be empty");
		}
		
		keyboard.close();
	}
		
	private static void sendMessage() throws IOException
	{
		dout.writeUTF(tb_message.getText());
		tb_message.setText("");
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (e.getSource() == btn_login)
		{
			try 
			{
				joinServer();
			} 
			catch (Exception e2) 
			{
				tb_username.setText("Connection to server failed.");
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
	public ClientWindow (Socket socket) 
	{
		ClientWindow.socket = socket;
	}

	@Override
	public void run() 
	{
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
			
		} catch( IOException e) { }
	}

	

	@Override 
	// Enter keypress event handlers 
	public void keyReleased(KeyEvent e) 
	{
		if (e.getSource() == tb_username && e.getKeyCode() == KeyEvent.VK_ENTER ) 
		{
			try 
			{
				joinServer();
			} 
			catch (Exception e2) 
			{
				tb_username.setText("Connection to server failed.");
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
