package assignment7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;

public class ServerMain extends Observable {

    int port =5000;

    public static void main(String[] args) {
        System.out.println("start");
        try{
            new ServerMain().initNetwork();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void initNetwork() throws Exception {
        @SuppressWarnings("resource")
        ServerSocket serverSocket = new ServerSocket(port);

        while(true){
            Socket clientSock = serverSocket.accept();
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
            String message;
            try {
                while ((message = reader.readLine()) != null) {
                    System.out.println("Sever read: " + message);
                    setChanged();
                    notifyObservers(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}