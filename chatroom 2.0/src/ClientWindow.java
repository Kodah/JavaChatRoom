import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.Manager;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;

import com.sun.media.MediaPlayer;
import com.sun.media.rtsp.protocol.SetupMessage;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
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
import java.awt.image.ColorModel;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;


public class ClientWindow extends JFrame implements ActionListener, Runnable, 
									KeyListener, ControllerListener

{
	private static final long serialVersionUID = 1L;
	private static JFrame mediaFrame;
	private static JPanel chatScreen, loginScreen, signupScreen, imagePanel;
	private static JButton send, btn_login, btn_signup, logout, 
							btn_submit, btn_goBack, getAudio, getVideo, getImage;
	private static JTextArea ta_chat, ta_users;
	private static JTextPane tp_chat;
	public static Socket socket;
	public static int typeOfInputStream;
	public static JTextField tb_username, tb_password, tb_message;
	public static JTextField tb_newUsername, tb_newPassword, tb_passwordRepeat;
	public static InetAddress host;
	public static DataInputStream din;
	public static DataOutputStream dout;
	public static String username, password;
	public static JLabel curUser, lbl_error, lbl_status, lblimage;
	private File file;
	private Player player;
	
	public static void main(String[] args) 
	{
		ClientWindow frame =  new ClientWindow();
				
		frame.setTitle("Chat");
		frame.setSize(600, 500);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		mediaFrame = new JFrame ("Media");
		mediaFrame.setSize(600, 500);
		mediaFrame.setVisible(false);
	}
	
	public ClientWindow() 
	{
		setLayout(new CardLayout());

		setupLoginPanel();
		setupChatPanel();
		setupSignUpPanel();
	}

	private void setupChatPanel() 
	{
		// Login panel set up
		chatScreen = new JPanel();
		chatScreen.setLayout(new GridBagLayout());
		
		add(chatScreen, "Login");
		chatScreen.setVisible(false);
		
		// Login panel elements 
		ta_chat = new JTextArea(10,30);
		ta_users = new JTextArea(10,50);
		tb_message = new JTextField(30);
		
		ta_chat.setEditable(false);
		ta_users.setEditable(false);
		
		curUser = new JLabel();
		JScrollPane chatArea = new JScrollPane(ta_chat); 
		JScrollPane userArea = new JScrollPane(ta_users); 
		JPanel inputArea = new JPanel();
		JPanel buttonPanel = new JPanel();
		inputArea.setLayout(new GridBagLayout());
		buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
		//buttonsPanel.setBackground(new Color(3));
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
		
		tb_message.setFont(myFont);	
		
		ImageIcon imgIcon = new ImageIcon("cam.gif");
		getImage = new JButton(imgIcon);
		
		ImageIcon audioIcon = new ImageIcon("audio.gif");
		getAudio = new JButton( audioIcon);
		
		ImageIcon vidIcon = new ImageIcon("vid.gif");
		getVideo = new JButton( vidIcon);
		
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.weighty = 1;
		gbc.weightx = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		//create input panel	 
		//gbc.fill = GridBagConstraints.NONE;
		inputArea.add(tb_message, gbc);
		
		gbc.weightx = 0;
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		inputArea.add(send, gbc);
		gbc.weightx = 1;
//		gbc.insets.right = 0;
//		gbc.gridy = 1;
//		gbc.fill = GridBagConstraints.NONE;
//		gbc.gridx = 0;
		buttonPanel.add(getImage);
//		gbc.gridx = 1;
		buttonPanel.add(getAudio);
//		gbc.gridx = 2;
//		gbc.insets = new Insets(0,0,0,0);
		buttonPanel.add(getVideo);
		
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;
//		gbc.gri
		gbc.gridx = 0;
		gbc.gridy = 1;
		inputArea.add(buttonPanel, gbc);
		
		//chat area
		gbc.insets = new Insets(2, 2, 2, 2);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = 0;
		
		ta_chat.setLineWrap(true);
		ta_chat.setWrapStyleWord(true);
		
		chatScreen.add(chatArea, gbc);

		//user area
		gbc.gridx = 1;
		ta_users.setText("user area");
		gbc.weightx = 0.4;
		ta_users.setLineWrap(true);
		ta_users.setWrapStyleWord(true);
		chatScreen.add(userArea, gbc);
	
		// input area 
		gbc.weighty = 0;
		gbc.gridx = 0;
		gbc.gridy = 1;
		chatScreen.add(inputArea, gbc);
		
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.PAGE_END;
		//gbc.weightx = 0.1;
		//gbc.weighty = 0.1;
		gbc.gridx = 1;
		gbc.gridy = 1;

		chatScreen.add(logout, gbc);
//		gbc.anchor = GridBagConstraints.PAGE_START;
		//gbc.fill = GridBagConstraints.BOTH;
//		
		gbc.fill = GridBagConstraints.NONE;		
//		chatScreen.add(myLogo, gbc);
		gbc.anchor = GridBagConstraints.PAGE_START;
		curUser.setText("Logged in as Tom");
		chatScreen.add(curUser, gbc);
		
		getVideo.addActionListener(this);
		getAudio.addActionListener(this);
		getImage.addActionListener(this);
		
		logout.addActionListener(this);
		send.addActionListener(this);
		tb_message.addKeyListener(this);
		
		//ta_chat.setText("<html>Hello every bady</html>");
	}

	private void setupLoginPanel() 
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
		tb_username = new JTextField(20);
		tb_password = new JTextField(20);
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
		tb_username.setHorizontalAlignment(JTextField.CENTER);
		tb_username.setFont(myFont);
		loginScreen.add(tb_username, gbc);
		
		gbc.insets.bottom = 0;
		lbl_password.setText("Password");
		gbc.gridy = 2;
		loginScreen.add(lbl_password, gbc);
				
		gbc.insets.bottom = 20;
		gbc.gridy = 3;
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
		btn_signup.addActionListener(this);
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

	private void setupSignUpPanel()
	{
		signupScreen = new JPanel();
		signupScreen.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		
		add(signupScreen, "Signup");
		signupScreen.setVisible(false);	
		
		tb_newUsername = new JTextField(20);
		tb_newPassword = new JTextField(20);
		tb_passwordRepeat = new JTextField(20);
		JLabel lbl_username = new JLabel("new username:");
		JLabel lbl_password = new JLabel("new password:");
		JLabel lbl_passrepeat = new JLabel("repeat password:");
		lbl_status = new JLabel();
		btn_submit = new JButton("signup");
		btn_goBack = new JButton(" back ");
		
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		signupScreen.add(lbl_username, gbc);
		gbc.gridy = 1;
		gbc.insets.bottom = 20;
		signupScreen.add(tb_newUsername, gbc);
		gbc.gridy = 2;
		gbc.insets.bottom = 0;
		signupScreen.add(lbl_password, gbc);
		gbc.gridy = 3;
		gbc.insets.bottom = 20;
		signupScreen.add(tb_newPassword, gbc);
		gbc.gridy = 4;
		gbc.insets.bottom = 0;
		signupScreen.add(lbl_passrepeat, gbc);
		gbc.gridy = 5;
		signupScreen.add(tb_passwordRepeat, gbc);
		gbc.insets.bottom = 20;
		gbc.gridy = 6;
		gbc.ipady = 15;
		signupScreen.add(lbl_status, gbc);
		gbc.gridy = 7;
		gbc.ipady = 0;
		gbc.gridwidth = 1;
		signupScreen.add(btn_goBack, gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.FIRST_LINE_END;
		signupScreen.add(btn_submit, gbc);
		
		btn_submit.addActionListener(this);
		btn_goBack.addActionListener(this);
	}

	public static void joinServer()throws IOException
	{
		final int PORT = 1234;
		String serverResponce;
		
		try
		{
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			System.out.println("\nHost ID not found!\n");
		}
				
		username = tb_username.getText().trim().replace(" ", "_");
		password = tb_password.getText();
		
		if (username.length() > 0) 
		{
			socket = new Socket(host, PORT);
			din = new DataInputStream( socket.getInputStream() );
			dout = new DataOutputStream( socket.getOutputStream() );
			
			dout.writeUTF("userLoginRequest");
			
			dout.writeUTF(username);
			dout.writeUTF(password);
			
			serverResponce = din.readUTF();
			
			switch (serverResponce) {
			case "success":
				Thread messageListener = new Thread(new ClientWindow(socket, 0));
				
				tb_username.setText("");
				tb_password.setText("");
				lbl_error.setText("");
				loginScreen.setVisible(false);
				chatScreen.setVisible(true);
				curUser.setText("Logged as " + username);
				messageListener.start();
				break;
			case "Duplicate":
				lbl_error.setText("You are logged in somewhere else.");
				break;
			case "notFound":
				lbl_error.setText("User not found.");
				break;
			default:
				lbl_error.setText("Error");
				break;
			}
		}
		else
		{
			lbl_error.setText("Username must not be empty");
		}
	}
		
	private static void signUpNewUser() throws IOException
	{
		final int PORT = 1234;
		String serverResponce;
		
		try
		{
			host = InetAddress.getLocalHost();
		}
		catch(UnknownHostException uhEx)
		{
			System.out.println("\nHost ID not found!\n");
		}

		String username = tb_newUsername.getText();
		String newPassword = tb_newPassword.getText();
		String passwordRepeat = tb_passwordRepeat.getText();
		
		if (newPassword.equals(passwordRepeat)) 
		{
			if (username.length()>2 && newPassword.length() > 2) 
			{
				socket = new Socket(host, PORT);
				din = new DataInputStream( socket.getInputStream() );
				dout = new DataOutputStream( socket.getOutputStream() );
				
				dout.writeUTF("userRegistrationRequest");
				
				dout.writeUTF(username);
				dout.writeUTF(newPassword);
				
				serverResponce = (String)din.readUTF();
				
				switch (serverResponce) {
				case "success":
					lbl_status.setForeground(Color.GREEN);
					lbl_status.setText(String.format("Welcome %s! Sign up successful.", username));
					tb_newPassword.setText(""); 
					tb_newUsername.setText("");  
					tb_passwordRepeat.setText(""); 
					break;
				case "username taken":
					lbl_status.setForeground(Color.RED);
					lbl_status.setText("That username is taken.");
					break;
				default:
					break;
				}
			}
			else
			{
				lbl_status.setForeground(Color.RED);
				lbl_status.setText("username and password must "
						+"be greater than two characters");
			}
		} 
		else 
		{
			lbl_status.setText("Passwords do not match");
		}
		username = tb_username.getText().trim().replace(" ", "_");
		password = tb_password.getText();
	}
	
	private static void sendMessage(String message) throws IOException
	{
		
		if (!message.equals(""))
		{
			dout.writeUTF(message);
		}
		else 
		{
			String messageText = tb_message.getText();	
			
			if (messageText.trim().length() > 0) 
			{
				dout.writeUTF(tb_message.getText().trim());
				
				if (messageText.length()> 3 && messageText.substring(0, 3).equals("/w ")) 
				{
					String messageSplit[] = messageText.split(":");
					tb_message.setText(messageSplit[0]+": ");
				}
				else
					tb_message.setText("");
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
	
	public static void requestMedia(String fileName) throws IOException
	{
		ServerSocket serverSocket = null;

	    try 
	    {
	        serverSocket = new ServerSocket(1235);
	    } 
	    catch (IOException ex) 
	    {
	        System.out.println("Can't setup server on this port number. ");
	    }
		
	    Socket mediaSocket = null;
	    InputStream is = null;
	    FileOutputStream fos = null;
	    BufferedOutputStream bos = null;
	    int bufferSize = 0;
	    
	    try 
	    {
	    	mediaSocket = serverSocket.accept();
	    } 
	    catch (IOException ex) {
	        System.out.println("Can't accept client connection. ");
	    }

	    try 
	    {
	        is = mediaSocket.getInputStream();

	        bufferSize = mediaSocket.getReceiveBufferSize();
	        System.out.println("Buffer size: " + bufferSize);
	    } 
	    catch (IOException ex) 
	    {
	        System.out.println("Can't get socket input stream. ");
	    }

	    try 
	    {
	        fos = new FileOutputStream("src/clientFolder/"+fileName);
	        bos = new BufferedOutputStream(fos);

	    } 
	    catch (FileNotFoundException ex) 
	    {
	        System.out.println("File not found. ");
	    }

	    byte[] bytes = new byte[bufferSize];

	    int count;

	    while ((count = is.read(bytes)) > 0) 
	    {
	    	System.out.println(count);
	        bos.write(bytes, 0, count);
	    }

	    bos.flush();
	    bos.close();
	    is.close();
	    mediaSocket.close();
	    serverSocket.close();
	}
	private void createPlayer(String fileName) throws Exception
   	{
		try
		{
			file = new File("src/clientFolder/"+fileName);
			if (!file.exists())
				throw new FileNotFoundException();
		}
		catch(FileNotFoundException fnfEx)
		{
			JOptionPane.showMessageDialog(this,
				"File not found!", "Invalid file name",
				JOptionPane.ERROR_MESSAGE);
			return;
		}
		
   		if (player != null)
     	{
   			Container pane = mediaFrame.getContentPane();
   			player.stop();
   			Component controlsComponent =
   	               	player.getControlPanelComponent();
   			
     		pane.remove(controlsComponent); 
     		
   			if (player.getVisualComponent() != null)
   			{
   				Component visualComponent =
           			player.getVisualComponent();

   				pane.remove(visualComponent); //
   			}
     	}
		URI uri = file.toURI();
		player = Manager.createPlayer(uri.toURL());

		player.addControllerListener(this);
		player.start();
	}
	
	
	private void createImageFrame(String fileName) 
	{
		imagePanel = new JPanel(new BorderLayout());
		lblimage = new JLabel(new ImageIcon("src/clientFolder/"+fileName));
		imagePanel.add(lblimage);
		
		if (lblimage.isShowing())
		{
			mediaFrame.remove(imagePanel);
		}
		
		if (player != null)
     	{
   			Container pane = mediaFrame.getContentPane();
   			player.stop();
   			Component controlsComponent =
   	               	player.getControlPanelComponent();

			Component visualComponent =
           			player.getVisualComponent();

			pane.remove(visualComponent); 
			pane.remove(controlsComponent); 
     	}
		
		mediaFrame.add(imagePanel);
		mediaFrame.pack();
		mediaFrame.setVisible(true);
	}
	

	public void controllerUpdate(ControllerEvent e)
	{
		if (lblimage.isShowing())
		{
			mediaFrame.remove(imagePanel);
		}
		
		Container pane = mediaFrame.getContentPane();
		
       	if (e instanceof RealizeCompleteEvent)
		{
			Component visualComponent =
               			player.getVisualComponent();
			
     		if (visualComponent != null)
				pane.add(visualComponent, BorderLayout.CENTER);

         	Component controlsComponent =
               	player.getControlPanelComponent();

         	if (controlsComponent != null)
				pane.add(controlsComponent, BorderLayout.SOUTH);
         	
         	mediaFrame.pack();
     		player.start();
			pane.doLayout();
		}
	}	
	
	public void placeholder(String fileName, String mediaType)
	{
		File file = new File("src/clientFolder/"+fileName);
		
		if(!file.exists()) 
		{
			try 
			{
				sendMessage(mediaType);
				requestMedia(fileName);
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
		
		try 
		{				
			try
			{
				if (mediaType.equals("¥¥_Image_¥¥"))
					createImageFrame(fileName);
				else
					createPlayer(fileName);
	 			
	 			mediaFrame.setVisible(true);
			}
			catch (Exception otherEx)
			{
	         	JOptionPane.showMessageDialog(this,
	            	"Unable to load file!", "Invalid file type",
	           		 JOptionPane.ERROR_MESSAGE );
	     	}
			
		} 
		catch (Exception e1) 
		{
			e1.printStackTrace();
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
				sendMessage("");
			} 
			catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
		if(e.getSource() == btn_signup)
		{
			loginScreen.setVisible(false);
			signupScreen.setVisible(true);
		}
		if(e.getSource() == btn_goBack)
		{
			loginScreen.setVisible(true);
			signupScreen.setVisible(false);
		}
		if (e.getSource() == btn_submit) 
		{
			try 
			{
				signUpNewUser();
			} catch (IOException e1) 
			{
				e1.printStackTrace();
			}
		}
		if(e.getSource() == getAudio)
		{
			placeholder("cuckoo.au", "¥¥_Audio_¥¥");
		}
		if(e.getSource() == getVideo)
		{
			placeholder("MoonWalk.mpeg", "¥¥_Video_¥¥");
		}
		if(e.getSource() == getImage)
		{
			placeholder("hatman.gif", "¥¥_Image_¥¥");
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
				sendMessage("");
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
