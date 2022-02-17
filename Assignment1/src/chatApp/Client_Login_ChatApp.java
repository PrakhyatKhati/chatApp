package chatApp;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * 
 * @author khati
 *	Assignment-1_Chatapp CMPT842-01
 * Refereces: YouTube Videos and GitHub Projects.
 */

public class Client_Login_ChatApp {

	
	

	private JFrame frame;
	private JTextField clientName;
	private int port = 8818;

	/**
	 * Launch the application.This is the main function which will make UI visible.
	 */
	public static void main(String[] args) { 
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client_Login_ChatApp firstwindow = new Client_Login_ChatApp();
					firstwindow.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application and  Initialize the contents of the frame.
	 */
	public Client_Login_ChatApp() {
		initialize();
	}
	private void initialize() { 
		frame = new JFrame();
		frame.setBounds(100, 100, 619, 342);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client Login");

		clientName = new JTextField();
		clientName.setBounds(207, 50, 276, 61);
		frame.getContentPane().add(clientName);
		clientName.setColumns(10);

		JButton clientLoginBtn = new JButton("Connect");
		clientLoginBtn.addActionListener(new ActionListener() {
			
			//action will be taken on clicking login button
			/**
			 * The UserName entered by the user is the ID of the user, We create a socket and Initialize the input and output stream
			 * to send and receive message via the stream, UserName is sent via the stream and message from the server regarding the username 
			 * is received via DataInputStream from the socket.  
			 * 
			 * A socket is create with port 8818 and Localhost as an serverIp.
			 */
			public void actionPerformed(ActionEvent e) {
				try {
					String id = clientName.getText(); 
					Socket s = new Socket("localhost", port); 
					DataInputStream inputStream = new DataInputStream(s.getInputStream()); 
					DataOutputStream outStream = new DataOutputStream(s.getOutputStream());
					outStream.writeUTF(id); 
					
					String msgFromServer = new DataInputStream(s.getInputStream()).readUTF(); 
					
					if(msgFromServer.equals("Username already taken")) {//if server sent this message then prompt user to enter other username
						JOptionPane.showMessageDialog(frame,  "Username already exist\n"); // show message in other dialog box
					}
					else if(msgFromServer.equals("Username already more")){
						JOptionPane.showMessageDialog(frame,  "Can't sign in-Too many users.\n");
					}
					else {
					//	If the username is not taken and there are less then 10 user than,
					//	 create a new thread of Client view and close the register jframe
						new Client_chatApp(id, s); 
						frame.dispose();
					}
				}catch(Exception ex) {
					ex.printStackTrace();
				}
				
				
				
			}
		});
		
		clientLoginBtn.setFont(new Font("Tahoma", Font.PLAIN, 17));
		clientLoginBtn.setBounds(207, 139, 132, 61);
		frame.getContentPane().add(clientLoginBtn);
		
		JLabel lblNewLabel = new JLabel("Username");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(44, 55, 132, 47);
		frame.getContentPane().add(lblNewLabel);
	}

}
