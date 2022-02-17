package chatApp;



import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

/**
 * 
 * @author Prakhyat Khati 
 * Assignment-1_Chatapp CMPT842-01
 * Refereces: YouTube Videos and GitHub Projects.
 * 
 */

public class Server_chatApp {
	
	// The total number of clients can only reach to 10 so the variable total_clients check the number of active user to notify New clients.
	
	private  int total_clients =10;
	//initial number of client 
	private  int current_clients_number=0 ;
	private ServerSocket serverSocket; 
	private JTextArea serverMessageBoard; 
	private JList totalUserList;  
	private JList totalactiveClientList;
	
	// keeps the mapping of all the userNames used and their socket connection		
	private static Map<String, Socket> totalListofUsers = new ConcurrentHashMap<>(); 																
	private static Set<String> activeListofUsers = new HashSet<>(); 
	// keeps list of active users for display on UI 
	private DefaultListModel<String> activeModelDlm = new DefaultListModel<String>();
	// keeps list of all users for display on UI
	private DefaultListModel<String> allModelDlm = new DefaultListModel<String>(); 
	private static int port = 8818;  // port number to be used 
	private JFrame frame; 
	
	 

	
	/**
	 * Launch the application, the function starts and creates an object to make the Jframe visible. 
	 */
	public static void main(String[] args) {  
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Server_chatApp windowserver = new Server_chatApp();  
					windowserver.frame.setVisible(true); 
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application and all the components of swing App will be initialized here, the post for the socket connect is 8818,We will create a server socket.
	 */
	public Server_chatApp() {
		initialize();  
		try {
			serverSocket = new ServerSocket(port);  
			serverMessageBoard.append("Server started on port: " + port + "\n"); // print messages to server message board
			serverMessageBoard.append("Waiting for the clients...\n");
			new ClientAccept().start(); // this will create a thread for client
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
/**
 * 
 * @author khati
 * The socket for the client is created , the thread will receive the UserName sent from client register view and also create and output stream for client,
 * The dataInputStream and datOutputStream is used to read raw stream from the socket. The readUTF method of dataInput stream class reads from the stream
 * in a representation of a UniCode character string encoded in modified UTF-8 format. 
 */
	class ClientAccept extends Thread {
		@Override
		public void run() {
			while (true) {
				try {
					Socket clientSocket = serverSocket.accept();  
					String userName = new DataInputStream(clientSocket.getInputStream()).readUTF(); 
					DataOutputStream clientOutStream = new DataOutputStream(clientSocket.getOutputStream()); 
					
					if (activeListofUsers != null && activeListofUsers.contains(userName)) { // if username is in use then we need to prompt user to enter new name
						clientOutStream.writeUTF("Username already taken");
					}
					// check the number of active user and if the number is more than 10 then is the Client login app notifies 
					//that to the User.
					else if(total_clients == current_clients_number) // just to check the user count.
					{
						clientOutStream.writeUTF("Username already more");
					}
					else {
						totalListofUsers.put(userName, clientSocket); // add new user to allUserList and activeListofUsers
						activeListofUsers.add(userName);
						current_clients_number=current_clients_number + 1;
						serverMessageBoard.append("Number of active user is  :- "+   current_clients_number   +"\n");// print current clients number in server
						clientOutStream.writeUTF(""); // clear the existing message
						activeModelDlm.addElement(userName); 
						//if userName taken previously then don't add to JList if not than add it.
						if (!allModelDlm.contains(userName)) 
							allModelDlm.addElement(userName);
						totalactiveClientList.setModel(activeModelDlm); 
						totalUserList.setModel(allModelDlm);
						// Message is printed to the serverMessage Board in order to notify is user is connected.
						serverMessageBoard.append("Client " + userName + " Connected...\n"); // A thread is created  to read messages
						new MessageRead(clientSocket, userName).start(); 
						//A thread is created to update all the active members. 
						new PrepareCLientList().start(); 
					}
					
				} catch (IOException ioex) {  
					ioex.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
/**
 * 
 * @author khati
 *This class reads the message coming from the client and take appropriate actions,The socket and the username will be provided by 
 *the client.
 *Check whether alluserList is empty or not and Identify whether the client has selected MultiCast or Broadcast during sending messsage to active users. 
 *If Multiple users are selected than msdList contains the list of client which will receive message. 
 *If the Message is BroadCase than all the active user will receive the chat message.
 *If Client process is killed than server will be notified along will all the active users, and it the user will be removed from the active user list.
 */
	class MessageRead extends Thread { 
		Socket s;
		String Id;
		private MessageRead(Socket s, String userName) { 
			this.s = s;
			this.Id = userName;
		}

		@Override
		public void run() {
			while (totalUserList != null && !totalListofUsers.isEmpty()) {  // if allUserList is not empty then proceed further
				try {
					String message = new DataInputStream(s.getInputStream()).readUTF(); // read message from client
					serverMessageBoard.append("message read ==> " + message+"\n"); // just print the message for testing
					String[] msgList = message.split(":"); // A unique identifier is created to identify what action to take on the received message from client
					//actionToBeTaken:clients_for_receiving_msg:message
					if (msgList[0].equalsIgnoreCase("multicast")) { 
						String[] sendToList = msgList[1].split(","); 
						for (String usr : sendToList) { 
							try {
								if (activeListofUsers.contains(usr)) { 
									new DataOutputStream(((Socket) totalListofUsers.get(usr)).getOutputStream())
											.writeUTF("<<< " + Id + " >>>" + msgList[2]); 
								}
							} catch (Exception e) { 
								e.printStackTrace();
							}
						}
					} else if (msgList[0].equalsIgnoreCase("broadcast")) { 
						
						Iterator<String> itr1 = totalListofUsers.keySet().iterator(); 
						while (itr1.hasNext()) {
							String usrName = (String) itr1.next(); // 
							if (!usrName.equalsIgnoreCase(Id)) { 
								try {
									if (activeListofUsers.contains(usrName)) { 
										new DataOutputStream(((Socket) totalListofUsers.get(usrName)).getOutputStream())
												.writeUTF("<<< " + Id + " >>>" + msgList[1]);
									} else {
										//if user is not active then notify the sender about the disconnected client
										new DataOutputStream(s.getOutputStream())
												.writeUTF("Message couldn't be delivered to user " + usrName + " because it is disconnected.\n");
									}
								} catch (Exception e) {
									e.printStackTrace(); 
								}
							}
						}
						// if a client's process is killed then notify other clients
					} else if (msgList[0].equalsIgnoreCase("exit")) { 
						activeListofUsers.remove(Id); 
						serverMessageBoard.append(Id + " disconnected....\n"); // print message on server message board
						current_clients_number--;
						serverMessageBoard.append("Number of active user-->"+ current_clients_number +"\n");
						
						new PrepareCLientList().start(); 

						Iterator<String> itr = activeListofUsers.iterator(); // iterate over other active users
						while (itr.hasNext()) {
							String usrName2 = (String) itr.next();
							if (!usrName2.equalsIgnoreCase(Id)) { 
								try {
									new DataOutputStream(((Socket) totalListofUsers.get(usrName2)).getOutputStream())
											.writeUTF(Id + " disconnected..."); 
								} catch (Exception e) { 
									e.printStackTrace();
								}
								new PrepareCLientList().start(); 
							}
						}
						activeModelDlm.removeElement(Id); 
						totalactiveClientList.setModel(activeModelDlm); 
						
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
/**
 * 
 * @author khati
 * This thread prepares the list of active user to be displayed on the UI, Iterate all over the active user and set and output 
 * stream and send all the list of active users with identifier prefix " @@#@%" 
 *
 */
	class PrepareCLientList extends Thread { // it prepares the list of active user to be displayed on the UI
		@Override
		public void run() {
			try {
				String ids = "";
				Iterator itr = activeListofUsers.iterator(); 
				// prepare string of all the users
				while (itr.hasNext()) { 
					String key = (String) itr.next();
					ids += key + ",";
				}
				if (ids.length() != 0) {
					ids = ids.substring(0, ids.length() - 1);
				}
				itr = activeListofUsers.iterator();
				 // iterate over all active users
				while (itr.hasNext()) {
					String key = (String) itr.next();
					try {
						new DataOutputStream(((Socket) totalListofUsers.get(key)).getOutputStream())
								.writeUTF("@@#@%=" + ids);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Initialize the contents of the frame, The ServerView contains the Active User list,All User list and log of all the activities 
	 * between the client, The number of Active user count is also displays. 
	 * here components of Swing App UI are initilized 
	 */
	private void initialize() { 
		frame = new JFrame();
		frame.setBounds(100, 100, 796, 530);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("Server View");

		serverMessageBoard = new JTextArea();
		serverMessageBoard.setEditable(false);
		serverMessageBoard.setBounds(12, 29, 489, 435);
		frame.getContentPane().add(serverMessageBoard);
		serverMessageBoard.setText("Starting the Server...\n");

		totalUserList = new JList();
		totalUserList.setBounds(526, 324, 218, 140);
		frame.getContentPane().add(totalUserList);

		totalactiveClientList = new JList();
		totalactiveClientList.setBounds(526, 78, 218, 156);
		frame.getContentPane().add(totalactiveClientList);

		JLabel lblNewLabel = new JLabel("All Usernames");
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setBounds(530, 295, 127, 16);
		frame.getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Active Users");
		lblNewLabel_1.setBounds(526, 53, 98, 23);
		frame.getContentPane().add(lblNewLabel_1);

	}
}