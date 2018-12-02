package assignment7;

import sun.nio.cs.ext.ISCII91;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLOutput;
import java.util.Observable;
import java.util.*;

public class ServerMain extends Observable {

    private static ArrayList<Socket> SocketConnected = new ArrayList<Socket>();
    private static ArrayList<String> userConnected = new ArrayList<>();
    private static HashMap<Integer, String> socketPort_Username = new HashMap<Integer, String>();
    private static HashMap<String, String> username_Password = new HashMap<String, String>();

    private int port = 5000;

    public ArrayList<Socket> getSocketConnected() {
        return SocketConnected;
    }

    public static void main(String[] args) {
        System.out.println("start");
        try {
            new ServerMain().initNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initNetwork() throws Exception {
        @SuppressWarnings("resource")
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSock = serverSocket.accept();
            SocketConnected.add(clientSock);

            ClientObserver writer = new ClientObserver(clientSock.getOutputStream());
            Thread t = new Thread(new ClientHandler(clientSock));
            t.start();
            this.addObserver(writer);
            System.out.println("Got a connection from " + clientSock);
        }
    }

    class ClientHandler implements Runnable {
        private BufferedReader reader;

        public ClientHandler(Socket clientSock) {
            Socket sock = clientSock;
            try {
                reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            String messageReceived;
            String messageDelivered;
            String messageType;
            int lengthOfVerification;
            int socketPort;
            String username;
            String password;
            Socket clientSock = null;

            try {
                while ((messageReceived = reader.readLine()) != null) {
                    System.out.println("Sever read: " + messageReceived);

                    /***** Process message type *****/
                    /* Message type
                    Message: MSG_
                    Username/Password: UPS_
                    * */
                    String[] messageProcessing = messageReceived.split("_");
                    socketPort = Integer.parseInt(messageProcessing[0]);
                    messageType = messageProcessing[1];

                    //Finding the specific user socket
                    ArrayList<Socket> users = getSocketConnected();
                    for (Socket c : users) {
                        if (c.getPort() == socketPort) {
                            clientSock = c;
                        }
                    }
                    PrintWriter writer = new PrintWriter(clientSock.getOutputStream());


                    switch (messageType) {
                        case "MSG":
                            lengthOfVerification = messageProcessing[0].length() + messageProcessing[1].length() + 2;
                            messageDelivered = messageReceived.substring(lengthOfVerification);

                            setChanged();
                            notifyObservers("MSG_" + messageDelivered);
                            break;

                        case "REG":
                            username = messageProcessing[2];
                            password = messageProcessing[3];


                            //Check existing user
                            if (username_Password.containsKey(username)) {
                                //user exists
                                System.out.println("User exists");

                                writer.println("REG_" + "dupUser_");
                                writer.flush();

                            } else {
                                //new user
                                socketPort_Username.put(socketPort, username);
                                username_Password.put(username, password);

                                System.out.println("User " + username + " created");

                                userConnected.add(username);

                                writer.println("REG_" + "createdUser_" + username);
                                writer.flush();
                            }

                            System.out.println(socketPort_Username.toString());
                            System.out.println(username_Password.toString());

                            break;

                        case "LOG":
                            username = messageProcessing[2];
                            password = messageProcessing[3];

                            boolean online = userConnected.contains(username);

                            if (username_Password.containsKey(username)) {
                                //User exists
                                //Check if password is correct
                                if (username_Password.get(username).equals(password)) {
                                    if (online) {
                                        System.out.println("Current user online");
                                        writer.println("LOG_USER-ONLINE_" + username);
                                        writer.flush();
                                    } else {
                                        //Correct password
                                        System.out.println("Logged in as " + username);
                                        userConnected.add(username);
                                        //Tell UI to change to chat room
                                        writer.println("LOG_SUCCESSFUL_" + username);
                                        writer.flush();
                                    }
                                } else {
                                    //Incorrect password
                                    System.out.println("Password incorrect");
                                    //Tell UT to change notification label
                                    writer.println("LOG_UNSUCCESSFUL_" + username);
                                    writer.flush();
                                }
                            } else {
                                //Non Existing user
                                System.out.println(username + " does not exist.");
                                writer.println("LOG_NOUSER_" + username);
                                writer.flush();
                            }

                            System.out.println(socketPort_Username.toString());
                            System.out.println(username_Password.toString());

                            break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public String[] subArray(String[] arr, int start) {
            return Arrays.copyOfRange(arr, start, arr.length);
        }
    }
}
