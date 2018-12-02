package assignment7;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.*;

public class ServerMain extends Observable {

//    private static ArrayList<Socket> socketConnected = new ArrayList<>();
    private static ArrayList<String> onlineUsers = new ArrayList<>();
    private static HashMap<Integer, String> socketPort_Username = new HashMap<>();
    private static HashMap<String, String> username_Password = new HashMap<>();

    private int port = 5000;

    public static void main(String[] args) {
        System.out.println("start");
        try {
            new ServerMain().initNetwork();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Initializes network, and continuously listens for new connections
     * @throws Exception
     */
    private void initNetwork() throws Exception {
        @SuppressWarnings("resource")
        ServerSocket serverSocket = new ServerSocket(port);

        while (true) {
            Socket clientSock = serverSocket.accept();
//            socketConnected.add(clientSock);

            ObjectOutputStream output = new ObjectOutputStream(clientSock.getOutputStream());
            ObjectInputStream input = new ObjectInputStream(clientSock.getInputStream());
            ClientObserver obs = new ClientObserver(output);
            Thread t = new Thread(new ClientHandler(output, input, obs));
            t.start();
            this.addObserver(obs);
            System.out.println("Got a connection from " + clientSock);
        }
    }

    /**
     * Handles the socket connection of a single client
     */
    class ClientHandler implements Runnable {
        private ObjectInputStream reader;
        private ObjectOutputStream writer;
        private ClientObserver obs;

        public ClientHandler(ObjectOutputStream out, ObjectInputStream in, ClientObserver obs) {
            this.reader = in;
            this.writer = out;
            this.obs = obs;
        }


        @Override
        public void run() {
            Message messageReceived;

            try {
                while ((messageReceived = (Message)reader.readObject()) != null) {
                    System.out.println("Server read: " + messageReceived.getMessage());

                    String username = messageReceived.getUsername();
                    String password = messageReceived.getPassword();

                    switch (messageReceived.getMessageType()) {


                        //Client wants to send message
                        case MSG:
                            setChanged();
                            notifyObservers(new Message(0, MessageType.MSG, messageReceived.getMessage(), username, null));
                            break;


                        //Client wants to register as a new user
                        case REG:

                            //The username that the client tries to register already exists
                            if (username_Password.containsKey(username)) {
                                System.out.println("User exists");
                                writer.writeObject(new Message(0, MessageType.REG, "dupUser", null, null));
                                writer.flush();
                            }

                            //Registration is successful
                            else {
                                socketPort_Username.put(messageReceived.getSocketPort(), username);
                                username_Password.put(username, password);
                                System.out.println("User " + username + " created");

                                writer.writeObject(new Message(0, MessageType.REG, "createdUser", username, null));
                                writer.flush();

                                //Whenever a user first logs in, notify it of all online users so it can display it
                                if (!onlineUsers.contains(username)) {
                                    for (String s : onlineUsers) {
                                        writer.writeObject(new Message(0, MessageType.LOGIN, null, s, null));
                                        writer.flush();
                                    }
                                }

                                onlineUsers.add(username);

                                setChanged();
                                notifyObservers(new Message(0, MessageType.LOGIN, null, username, null));
                                System.out.println("Online users :" + String.join(", ", onlineUsers));
                            }

                            System.out.println(socketPort_Username.toString());
                            System.out.println(username_Password.toString());
                            break;


                        //Client wants to login
                        case LOG:
                            boolean online = onlineUsers.contains(username);

                            //The username exists in the database
                            if (username_Password.containsKey(username)) {

                                //The username and password combination is correct
                                if (username_Password.get(username).equals(password)) {

                                    //The user that the client is logging in with is already online
                                    if (online) {
                                        System.out.println("Current user online");
                                        writer.writeObject(new Message(0, MessageType.LOG, "USER-ONLINE", null, null));
                                        writer.flush();
                                    }

                                    //Username exists, password correct, current user is not already online = successful login
                                    else {
                                        System.out.println("Logged in as " + username);

                                        writer.writeObject(new Message(0, MessageType.LOG, "SUCCESSFUL", username, null));
                                        writer.flush();

                                        //Whenever a user first logs in, notify it of all online users so it can display it
                                        if (!onlineUsers.contains(username)) {
                                            for (String s : onlineUsers) {
                                                writer.writeObject(new Message(0, MessageType.LOGIN, null, s, null));
                                                writer.flush();
                                            }
                                        }

                                        onlineUsers.add(username);

                                        setChanged();
                                        notifyObservers(new Message(0, MessageType.LOGIN, null, username, null));
                                        System.out.println("Online users :" + String.join(", ", onlineUsers));
                                    }
                                }

                                //The client entered a wrong password
                                else {
                                    System.out.println("Password incorrect");
                                    writer.writeObject(new Message(0, MessageType.LOG, "UNSUCCESSFUL", null, null));
                                    writer.flush();
                                }
                            }

                            //The username the client tried to login with does not exist
                            else {
                                System.out.println(username + " does not exist.");
                                writer.writeObject(new Message(0, MessageType.LOG, "NOUSER", username, null));
                                writer.flush();
                            }

                            System.out.println(socketPort_Username.toString());
                            System.out.println(username_Password.toString());
                            break;


                        //Client wants to logout
                        case LOGOUT:
                            onlineUsers.remove(username);
                            deleteObserver(obs);
                            setChanged();
                            notifyObservers(new Message(0, MessageType.LOGOUT, null, username, null));
                            reader.close();
                            writer.close();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Observes the server, and sends message to clients when appropriate
     */
    class ClientObserver implements Observer {

        ObjectOutputStream outputStream;

        public ClientObserver(ObjectOutputStream out) {
            this.outputStream = out;
        }

        @Override
        public void update(Observable o, Object arg) {
            try {
                outputStream.writeObject(arg);
                outputStream.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
