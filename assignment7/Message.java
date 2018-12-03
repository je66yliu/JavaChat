package assignment7;

import java.io.Serializable;
import java.net.Socket;

enum MessageType implements Serializable {
    MSG, PRIVATE, REG, LOG, LOGIN, LOGOUT
}

public class Message implements Serializable {

    private int socketPort;
    private MessageType messageType;
    private String message;
    private String username;
    private String password;
    private Socket clientSock = null;

    private String recipient = "";

    public Message(int socketPort, MessageType messageType, String message, String username, String password) {
        this.socketPort = socketPort;
        this.messageType = messageType;
        this.message = message;
        this.username = username;
        this.password = password;
    }

    public int getSocketPort() {
        return socketPort;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Socket getClientSock() {
        return clientSock;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setClientSock(Socket clientSock) {
        this.clientSock = clientSock;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
}
