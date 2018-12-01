package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.*;

public class ServerMain extends Observable {

    private static ArrayList<Socket> SocketConnected = new ArrayList<Socket>();
    private static HashMap<Integer, String> socketPort_Username = new HashMap<Integer, String>();
    private static HashMap<String, String> username_Password = new HashMap<String, String>();

    private int port = 5000;

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
            try {
                reader = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            String messageReceived;
            String messageDelivered;
            String messageType;
            int lengthOfVerivication;
            int socketPort;

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


                    switch (messageType) {
                        case "MSG":
                            lengthOfVerivication = messageProcessing[0].length() + messageProcessing[1].length() + 2;
                            messageDelivered = messageReceived.substring(lengthOfVerivication);

                            setChanged();
                            notifyObservers(messageDelivered);
                            break;

                        case "UPS":

                            String username = messageProcessing[2];
                            String password = messageProcessing[3];

                            //Check existing user
                            if (username_Password.containsKey(username)){
                                System.out.println("User exists");
                            } else{
                                socketPort_Username.put(socketPort,username);
                                username_Password.put(username,password);
                            }

                            System.out.println(socketPort_Username.toString());
                            System.out.println(username_Password.toString());

                            setChanged();
                            notifyObservers("notification_");
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
