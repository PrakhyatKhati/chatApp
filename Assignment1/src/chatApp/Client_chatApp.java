package chatApp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
/**
 * 
 * @author khati
 *Assignment-1_Chatapp CMPT842-01
 *Refereces: YouTube Videos and GitHub Projects.
 *
 *Before you run the Client_chatApp.java file please update the file address at databasePath variable as a string.
 */


public class Client_chatApp {

	

	private JFrame frame;
	private JTextField clientTypingBoard;
	private JList clientActiveUsersList;
	private JTextArea clientMessageBoard;
	private JButton clientKillProcessBtn;
	private JRadioButton oneToNRadioBtn;
	private JRadioButton HistoryRadioBtn;
	private JRadioButton broadcastBtn;

	DataInputStream inputStream;
	DataOutputStream outStream;
	DefaultListModel<String> dm;
	String id, clientIds = "";
	
	//Please update the path of your system where you want to save you history. 
	//Its recommended to be save in the same directory where you keep all files . 
	
	String databasePath ="C:\\Computer Science department\\CMPT-842\\Assignment1\\";
	
	
	
	
	/**
	 * Create the application.
	 * @throws IOException 
	 * The new thread for each client is created from the client_login_chatapp  and the constructor call will initialize the required variable and the UI.
	 * The socket and Id will be taken from the Client_Login_ChatApp. 
	 * */
	
	
	public Client_chatApp(String id, Socket s) throws IOException { 
		initialize(); // 
		
		this.id = id;
		try {
			frame.setTitle("Client View - " + id); 
			dm = new DefaultListModel<String>(); 
			// show that list on UI component JList named clientActiveUsersList
			clientActiveUsersList.setModel(dm);
			inputStream = new DataInputStream(s.getInputStream()); 
			outStream = new DataOutputStream(s.getOutputStream());
			// A new thread to read the message.
			new Read().start(); 
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		 
	}
	
   /**
    *  ### Important ###
    * @author khati
    *The read thread need to be updated as per the system as File is create to store and receive old history messages of the users. 
    *Note :- The file is the same as user ID and so it need to be update before running the system. 
    **/
	

	class Read extends Thread {
		@Override
		public void run() {
			
			while (true) {
					
				// Create a file to store the message into text.
				// Please update the address of the file as per your system. 
				// Create the text file as per the user ID.
				File file =new File(databasePath+id+".txt");
				if(!file.exists()) { // if already  exist than it wont create the same text file. 
		        	try {
						file.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
				
			      try {
					String m = inputStream.readUTF();  // read message from server, this will contain @@#@%=<comma seperated clientsIds>
					System.out.println("inside read thread : " + m); 
					/**
					 * The total list of user is read from the server and is identified using the identifier @@#@%= if the message contains message instead of the 
					 * Identifies then it is directly displayed to the  client message board and will be save to the text file create above.
					 */
					if (m.contains("@@#@%=")) {
						m = m.substring(6); 
						dm.clear(); 
						StringTokenizer st = new StringTokenizer(m, ","); 
						while (st.hasMoreTokens()) {
							String u = st.nextToken();
							if (!id.equals(u)) 
								dm.addElement(u); 
													
						}
					} else {
						clientMessageBoard.append("" + m + "\n"); 
						// saving the message to the text file create.
						messageToBeSave(m,file);
					}
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
			}
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() { 
		frame = new JFrame();
		frame.setBounds(100, 100, 926, 705);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Client View");

		clientMessageBoard = new JTextArea();
		clientMessageBoard.setEditable(false);
		clientMessageBoard.setBounds(12, 25, 530, 495);
		frame.getContentPane().add(clientMessageBoard);

		clientTypingBoard = new JTextField();
		clientTypingBoard.setHorizontalAlignment(SwingConstants.LEFT);
		clientTypingBoard.setBounds(12, 533, 530, 84);
		frame.getContentPane().add(clientTypingBoard);
		clientTypingBoard.setColumns(10);

		JButton clientSendMsgBtn = new JButton("Send");
		clientSendMsgBtn.addActionListener(new ActionListener() { // action to be taken on send message button
			
			public void actionPerformed(ActionEvent e) {
				
			//NOTE :- Please update the path of your system in order to save the history files .
				File file =new File(databasePath+id+".txt");
	       	
				
				String textAreaMessage = clientTypingBoard.getText(); 
				// only if message is not empty then send it further.
				if (textAreaMessage != null && !textAreaMessage.isEmpty()) {  
					try {
						String messageToBeSentToServer = "";
						String cast = "broadcast"; 
						int flag = 0; 
						// if 1-to-N is selected then its a MultiCast. 
						if (oneToNRadioBtn.isSelected()) { 
							cast = "multicast"; 
							List<String> clientList = clientActiveUsersList.getSelectedValuesList(); 
							if (clientList.size() == 0) 
								flag = 1;
							// append all the UserNames selected in a variable
							for (String selectedUsr : clientList) { 
								if (clientIds.isEmpty())
									clientIds += selectedUsr;
								else
									clientIds += "," + selectedUsr;
							}
							// place to prepare message to be sent to server side 
							messageToBeSentToServer = cast + ":" + clientIds + ":" + textAreaMessage; 
						} else {
							messageToBeSentToServer = cast + ":" + textAreaMessage; 
						}
						if (cast.equalsIgnoreCase("multicast")) { 
							if (flag == 1) { 
								// for MultiCast check if no user was selected then prompt a message dialog
								JOptionPane.showMessageDialog(frame, "No user selected");
							} else { 
								//The message will be save to the files created from the user IDs, resulting in unique text files for each user.
								//The message will be sent to all the selected users.
								outStream.writeUTF(messageToBeSentToServer);
								clientTypingBoard.setText("");
								//show the sent message to the sender's message board
								clientMessageBoard.append("< You sent msg to " + clientIds + ">" + textAreaMessage + "\n");
								messageToBeSave("< You sent msg to " + clientIds + ">" + textAreaMessage,file);
							}
						} else { // in case of broadcast the message will be sent to all the active users.
							outStream.writeUTF(messageToBeSentToServer);
							clientTypingBoard.setText("");
							clientMessageBoard.append("< You sent msg to All >" + textAreaMessage + "\n");
							//The message will be save to the file created from the user ID, resulting in unique text file for each user.
							messageToBeSave("< You sent msg to All >" + textAreaMessage,file);
						}
						clientIds = ""; 
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(frame, "User does not exist anymore."); 
					}
				}
			}
		});
		clientSendMsgBtn.setBounds(554, 533, 137, 84);
		frame.getContentPane().add(clientSendMsgBtn);

		clientActiveUsersList = new JList();
		clientActiveUsersList.setToolTipText("Active Users");
		clientActiveUsersList.setBounds(554, 63, 327, 457);
		frame.getContentPane().add(clientActiveUsersList);

		clientKillProcessBtn = new JButton("Kill Process");
		clientKillProcessBtn.addActionListener(new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				try {
					// closes the thread and show the message on server and client's message board.
					outStream.writeUTF("exit"); 
											
					clientMessageBoard.append("You are disconnected now.\n"); 
					frame.dispose();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		clientKillProcessBtn.setBounds(703, 533, 193, 84);
		frame.getContentPane().add(clientKillProcessBtn);

		JLabel lblNewLabel = new JLabel("Active Users");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(559, 43, 95, 16);
		frame.getContentPane().add(lblNewLabel);

		oneToNRadioBtn = new JRadioButton("1 to N");
		oneToNRadioBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientActiveUsersList.setEnabled(true);
			}
		});
		oneToNRadioBtn.setSelected(true);
		oneToNRadioBtn.setBounds(682, 24, 72, 25);
		frame.getContentPane().add(oneToNRadioBtn);

		broadcastBtn = new JRadioButton("Broadcast");
		broadcastBtn.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				clientActiveUsersList.setEnabled(false);
			}
		});
		broadcastBtn.setBounds(774, 24, 107, 25);
		frame.getContentPane().add(broadcastBtn);
		//
		
		HistoryRadioBtn = new JRadioButton("History");
		HistoryRadioBtn.addActionListener(new ActionListener() {
			
		//Action performed when clicking the history button	
			public void actionPerformed(ActionEvent e) {
				clientTypingBoard.setText(null);
				
		//NOTE :- Please update the File path of your respective system in the variable databasePath.
				File file =new File(databasePath+id+".txt");	
			        if(!file.exists()) {
			        	try {
							file.createNewFile();
						} catch (IOException ex) {
							// TODO 
							ex.printStackTrace();
						}
			        }
				try {
					retrieveHistoryMsg(file);
				} catch (Exception e1) {
					// TODO 
					e1.printStackTrace();
				}
				
				clientActiveUsersList.setEnabled(true);
			}
		});
		HistoryRadioBtn.setSelected(false);
		HistoryRadioBtn.setBounds(580, 24, 72, 25);
		frame.getContentPane().add(HistoryRadioBtn);		
		ButtonGroup btngrp = new ButtonGroup();
		btngrp.add(oneToNRadioBtn);
		btngrp.add(broadcastBtn);
		

		frame.setVisible(true);
	}
	// Method to save the message of the user into the text file create using IDs.
	public void messageToBeSave(String chatmessage,File fileName) throws Exception {
		chatmessage=chatmessage+"\n";
		FileOutputStream fos = new FileOutputStream(fileName, true);
            fos.write(chatmessage.getBytes());
            fos.close();
            fos.flush();
        }
	//Method to retrieve the message of the user into the text file when the history radio button is pressed.
	public void retrieveHistoryMsg(File fileName) throws Exception {
		FileInputStream fis = new FileInputStream(fileName);
		byte[] bytes = new byte[1024];
        int readCount = 0;
        while ((readCount = fis.read(bytes)) != -1) {
            System.out.println(new String(bytes, 0, readCount)); 
            clientMessageBoard.setText(null);
			clientMessageBoard.append(new String(bytes, 0, readCount));
        }
        fis.close();
       }
	
	
	
         
        
}
