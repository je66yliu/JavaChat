package assignment7;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import static javafx.scene.paint.Color.WHITE;

public class ClientMain extends Application {
    final private int WINDOW_WIDTH = 800;
    final private int WINDOW_HEIGHT = 600;
    private TextArea incoming;
    private TextField outgoing;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private String username;
    private int portAddress;
    private String ipAddress;
    private Label registerNotification;
    private Scene chatRoom;
    private Scene loginScreenScene;
    private static Stage mainStage;

    TextField usernameTextField;
    PasswordField passwordTextField;
    TextField ipAddressTextField;
    TextField portTextField;
    Label label_username;

    private HBox mainBox;
    private VBox onlineList;
    private ListView<String> groupChatListView;

    private boolean isLoggedIn = false;

    private static ArrayList<String> onlineUsers = new ArrayList<>();

    private static HashMap<String, TextArea> privateChats = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    public void setIpAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public void setPortAddress(int portAddress){
        this.portAddress = portAddress;
    }

    /**
     * Establishes a connection to the server
     *
     * @throws IOException
     */
    private void connectToServer() throws IOException {
        @SuppressWarnings("resource")
        Socket clientSock = new Socket(ipAddress, portAddress);
        //Socket clientSock = new Socket("172.20.10.8", 5050);
        portAddress = clientSock.getLocalPort();
        writer = new ObjectOutputStream(clientSock.getOutputStream());
        reader = new ObjectInputStream(clientSock.getInputStream());
        System.out.println("Connected to the server");
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Check if the ip address is valid
     * @param ip
     * @return a boolean
     */
    public static boolean validateIP(final String ip) {
        return PATTERN.matcher(ip).matches();
    }


    private final Pattern hasUppercase = Pattern.compile("[A-Z]");
    private final Pattern hasLowercase = Pattern.compile("[a-z]");
    private final Pattern hasNumber = Pattern.compile("\\d");
    private final Pattern hasSpecialChar = Pattern.compile("[^a-zA-Z0-9]");

    /**
     * Check if the username is valid
     * @param user
     * @return a string of results
     */
    private String usernameValidation(String user){
        StringBuilder validation = new StringBuilder();
        if (user == null){
                validation.append("Username can't be empty.");
            return validation.toString();
        }

        if (user.isEmpty()){
            validation.append("Username can't be empty.");
            return validation.toString();
        }

        if (user.length()<4){
            validation.append("Username has to be at least 4 characters.\n");
        }

        if(hasSpecialChar.matcher(user).find()){
            validation.append("Username can not contain special characters.\n");
        }

        if (validation.length()==0){
            validation.append("Success");
        }
        return validation.toString();
    }

    /**
     * Check if the password is valid
     * @param pass
     * @return a string of results
     */
    private String passwordValidation(String pass){
        StringBuilder validation = new StringBuilder();

        if (pass == null){
            validation.append("Password can't be empty.");
            return validation.toString();
        }

        if (pass.isEmpty()){
            validation.append("Password can't be empty.");
            return validation.toString();
        }

        if (pass.length()<5){
            validation.append("Password has to be at least 5 characters.\n");
        }

        if (!hasUppercase.matcher(pass).find()){
            validation.append("Password needs an upper case letter.\n");
        }

        if (!hasLowercase.matcher(pass).find()){
            validation.append("Password needs a lower case letter.\n");
        }

        if (!hasNumber.matcher(pass).find()){
            validation.append("Password needs a number.\n");
        }

        if(!hasSpecialChar.matcher(pass).find()){
            validation.append("Password needs a special character.\n");
        }

        if (validation.length()==0){
            validation.append("Success");
        }
        return validation.toString();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        mainStage.setTitle("Pair-40 Chat Room");

        /***********************
         * SERVER CONFIG SCREEN*
         * *********************/

        //IP Address
        Label ipAddressLabel = new Label("Server IP Address: ");

        ipAddressTextField = new TextField();
        ipAddressTextField.setMaxHeight(10);
        ipAddressTextField.setMaxWidth(150);
        ipAddressTextField.setText("127.0.0.1");

        HBox ipConfigBox = new HBox(ipAddressLabel, ipAddressTextField);
        ipConfigBox.setPrefWidth(500);
        ipConfigBox.setSpacing(10);

        //Port number
        Label portLabel = new Label("Port: ");
        portLabel.setAlignment(Pos.CENTER_LEFT);
        portLabel.setTextAlignment(TextAlignment.LEFT);
        portTextField = new TextField();
        portTextField.setMaxHeight(10);
        portTextField.setMaxWidth(150);
        portTextField.setText("5000");
            //Making sure the textfield only takes numbers
        portTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    portTextField.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        HBox portConfigBox = new HBox(portLabel, portTextField);
        portConfigBox.setPrefWidth(500);
        portConfigBox.setSpacing(80);

        //Connection notification
        Label connectionNotification = new Label("");

        //Connect button
        Button connectServerButton = new Button("Connect to server");
        connectServerButton.setOnAction(e->{
            if (validateIP(ipAddressTextField.getText())){
                setIpAddress(ipAddressTextField.getText());
                setPortAddress(Integer.parseInt(portTextField.getText()));
                try {
                    connectToServer();
                    connectionNotification.setText("");
                    mainStage.setScene(loginScreenScene);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    connectionNotification.setText("Connection failed");
                }
            } else{
                connectionNotification.setText("Invalid IP Address");
            }
        });

        //Progress bar
        ProgressBar connectingBar =  new ProgressBar();
        connectingBar.setProgress(-1);


        VBox connectionConfigBox = new VBox();
        connectionConfigBox.setPrefHeight(200);
        connectionConfigBox.setPrefWidth(320);
        connectionConfigBox.setPadding(new Insets(30));

        connectionConfigBox.setAlignment(Pos.CENTER);
        connectionConfigBox.setSpacing(10);
        connectionConfigBox.getChildren().addAll(ipConfigBox,portConfigBox,connectServerButton,connectionNotification,connectingBar);
        connectionConfigBox.setBackground(new Background(new BackgroundFill(Color.WHITE,CornerRadii.EMPTY, Insets.EMPTY)));

        GridPane serverConfigPane = new GridPane();
        serverConfigPane.setAlignment(Pos.CENTER);
        serverConfigPane.setBackground(new Background(new BackgroundFill(Color.rgb(223,125,60),
                CornerRadii.EMPTY, Insets.EMPTY)));
        serverConfigPane.getChildren().addAll(connectionConfigBox);

        Scene serverConfigScene = new Scene(serverConfigPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        serverConfigPane.getStylesheets().add("C:\\School\\2018 Fall\\EE422C\\Project\\Project7\\src\\assignment7\\resources\\connection_style.css");

        mainStage.setScene(serverConfigScene);



        /***************
         * LOGIN SCREEN*
         * *************/
        //Set up login screen

        //Username
        Label usernameLabel = new Label("Username: ");
        usernameTextField = new TextField();
        usernameTextField.setMaxHeight(10);
        usernameTextField.setMaxWidth(100);
        HBox usernameBox = new HBox(usernameLabel, usernameTextField);

        //Password
        Label passwordLabel = new Label("Password: ");
        passwordTextField = new PasswordField();
        passwordTextField.setMaxHeight(10);
        passwordTextField.setMaxWidth(100);
        HBox passwordBox = new HBox(passwordLabel, passwordTextField);

        //Buttons
        Button loginButton = new Button("Login: ");
        loginButton.setOnAction(e -> {
            try {
                //Check illegal characters
                String username = usernameTextField.getText();
                String password = passwordTextField.getText();
                String username_result;
                String password_result;

                System.out.println("Logging in with: " + usernameTextField.getText().trim() + "_" + passwordTextField.getText());

                //Username should only contain a-z, A-Z, 0-9, and be at least 4 characters long
                //Password should contain upper case, lower case, numbers, and special character. Password
                //should be at least 5 characters long

                username_result = usernameValidation(username);
                password_result = passwordValidation(password);

                if (username_result.equals("Success")){
                    if (password_result.equals("Success")){
                        System.out.println("Username and password are invalid format");
                        writer.writeObject(new Message(portAddress, MessageType.LOG, null, usernameTextField.getText(), passwordTextField.getText()));
                        writer.flush();
                    } else{
                        registerNotification.setText(password_result);
                    }
                } else{
                    registerNotification.setText(username_result);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Register button
        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            try {
                //Check illegal characters
                String username = usernameTextField.getText();
                String password = passwordTextField.getText();
                String username_result;
                String password_result;

                System.out.println("Registering new account: " + usernameTextField.getText() + "_" + passwordTextField.getText());

                //Username should only contain a-z, A-Z, 0-9, and be at least 4 characters long
                //Password should contain upper case, lower case, numbers, and special character. Password
                //should be at least 5 characters long

                username_result = usernameValidation(username);
                password_result = passwordValidation(password);

                if (username_result.equals("Success")){
                    if (password_result.equals("Success")){
                        System.out.println("Username and password are in valid format");
                        writer.writeObject(new Message(portAddress, MessageType.REG, null, usernameTextField.getText(), passwordTextField.getText()));
                        writer.flush();
                    } else{
                        registerNotification.setText(password_result);
                    }
                } else{
                    registerNotification.setText(username_result);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }


        });

//        Button changeToChatRoom = new Button("switch scene test");
        HBox loginButtonBox = new HBox(loginButton, registerButton);

        //Notification label
        registerNotification = new Label("");

        //Grid control
        VBox loginScreenVBox = new VBox(usernameBox, passwordBox, loginButtonBox, registerNotification);
        GridPane loginScreenGrid = new GridPane();
        loginScreenGrid.getChildren().addAll(loginScreenVBox);
        loginScreenScene = new Scene(loginScreenGrid, WINDOW_WIDTH, WINDOW_HEIGHT);




        /*******************
         * Chat room SCREEN*
         * *****************/
        //****************************************
        //Set up text output
        label_username = new Label("");
        Label label_ChatHistory = new Label("Chat History");
        incoming = new TextArea();
        incoming.setMaxHeight(200);
        incoming.setMaxWidth(400);
        incoming.setEditable(false);


        //****************************************
        //Set up text input
        Label label_EnterText = new Label("Enter Text Here");
        outgoing = new TextField();
        outgoing.setMaxWidth(400);
        outgoing.setMaxHeight(20);
        Button sendText = new Button("Send");
        Label chatRoomNotification = new Label("");

        outgoing.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                try {
                    if (outgoing.getText() == null || outgoing.getText().isEmpty()) {
                        chatRoomNotification.setText("You have to type something before you send it.");
                    } else {
                        System.out.println(outgoing.getText());
                        writer.writeObject(new Message(portAddress, MessageType.MSG, outgoing.getText(), username, null));
                        writer.flush();
                        outgoing.setText("");
                        usernameTextField.setText("");
                        passwordTextField.setText("");
                        chatRoomNotification.setText("");
                        usernameTextField.requestFocus();
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        sendText.setOnAction(e -> {
            try {
                if (outgoing.getText()==null|| outgoing.getText().isEmpty()){
                    chatRoomNotification.setText("You have to type something before you send it.");
                } else{
                    System.out.println(outgoing.getText());
                    writer.writeObject(new Message(portAddress, MessageType.MSG, outgoing.getText(), username, null));
                    writer.flush();
                    outgoing.setText("");
                    usernameTextField.setText("");
                    passwordTextField.setText("");
                    chatRoomNotification.setText("");
                    usernameTextField.requestFocus();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        HBox textInput = new HBox();
        textInput.getChildren().addAll(outgoing, sendText);


        //****************************************
        //Set up online users
        Label online = new Label("Online");
        onlineList = new VBox();
        onlineList.getChildren().add(online);

        //****************************************
        //Set up group chat ListView and createGroupChat button
        Label createGroupChat = new Label("Create a Group Chat");
        groupChatListView = new ListView<>();
        groupChatListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        Button createGroupChatButton = new Button("Create");


        //****************************************
        //Main Control
        VBox chats = new VBox();
        chats.getChildren().addAll(label_username, label_ChatHistory, incoming, label_EnterText, textInput,chatRoomNotification);

        VBox makeGroupChat = new VBox();
        makeGroupChat.getChildren().addAll(createGroupChat, groupChatListView, createGroupChatButton);

        mainBox = new HBox();
        mainBox.getChildren().addAll(chats, onlineList, makeGroupChat);

        chatRoom = new Scene(mainBox, WINDOW_WIDTH, WINDOW_HEIGHT);


//        //****************************************
//        //Scene Change Control
//        changeToChatRoom.setOnAction(e -> mainStage.setScene(chatRoom));

        //****************************************
        //Closing Controls
        mainStage.setOnCloseRequest(e -> {
            try {
                writer.writeObject(new Message(portAddress, MessageType.LOGOUT, "", username, null));
                reader.close();
                writer.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        mainStage.show();
    }

    /**
     * Updates the list of all users that are online dynamically
     */
    public void updateAllOnlineUsers() {
        onlineList.getChildren().retainAll(onlineList.getChildren().get(0));
        groupChatListView.getItems().clear();
        for (String s : onlineUsers) {
            Button b = new Button();
            if (s.equals(username)) {
                b.setText(s + " (me)");
                onlineList.getChildren().add(b);
            } else {
                b.setText(s);
                onlineList.getChildren().add(b);
                groupChatListView.getItems().add(s);
            }
            b.setOnAction(e -> openNewPrivateChat(s));
        }
    }

    public void openNewPrivateChat(String friend) {
        if (friend.equals(username)) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Error");
            a.setHeaderText("Cannot chat with yourself");
            a.setContentText("Please choose another user.");
            a.showAndWait();
        }
        else if (!privateChats.containsKey(friend)) {
            TextArea ta = new TextArea();
            ta.setMaxHeight(200);
            ta.setMaxWidth(400);
            ta.setEditable(false);

            privateChats.put(friend, ta);

            Label enterText = new Label("Enter a message");

            TextField msg = new TextField();
            msg.setMaxHeight(20);
            msg.setMaxWidth(400);

            Button sendButton = new Button("Send");


            msg.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    try {
                        if (!msg.getText().equals("") && msg.getText() != null) {
                            ta.appendText(username + ": \n" + msg.getText() + "\n\n");
                            Message privateMessage = new Message(portAddress, MessageType.PRIVATE, msg.getText(), username, null);
                            msg.setText("");
                            privateMessage.setRecipient(friend);
                            writer.writeObject(privateMessage);
                            writer.flush();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            sendButton.setOnAction(e -> {
                try {
                    if (!msg.getText().equals("") && msg.getText() != null) {
                        ta.appendText(username + ": \n" + msg.getText() + "\n\n");
                        Message privateMessage = new Message(portAddress, MessageType.PRIVATE, msg.getText(), username, null);
                        msg.setText("");
                        privateMessage.setRecipient(friend);
                        writer.writeObject(privateMessage);
                        writer.flush();
                    }
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            HBox msgPanel = new HBox();
            msgPanel.getChildren().addAll(msg, sendButton);

            VBox mainChatPanel = new VBox();
            mainChatPanel.getChildren().addAll(ta, enterText, msgPanel);

            Stage privateChatWindow = new Stage();
            privateChatWindow.setTitle("Private chat between " + username + ", " + friend);
            privateChatWindow.setScene(new Scene(mainChatPanel, 450, 450));
            privateChatWindow.show();

            privateChatWindow.setOnCloseRequest(e -> {
                try {
                    privateChats.remove(friend);
                    Message privateMessage = new Message(portAddress, MessageType.PRIVATE, " has left the chat.", username, null);
                    privateMessage.setRecipient(friend);
                    writer.writeObject(privateMessage);
                    writer.flush();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
    }


    /**
     * Thread that continuously listens for messages from the server
     */
    class IncomingReader implements Runnable {

        @Override
        public void run() {
            Message message;

            try {
                while ((message = (Message) reader.readObject()) != null) {
                    switch (message.getMessageType()) {


                        //Registering
                        case REG:

                            //User is already registered
                            if (message.getMessage().equals("dupUser")) {
                                Platform.runLater(() -> registerNotification.setText("User exists"));
                            }

                            //New user is successfully registered
                            else if (message.getMessage().equals("createdUser")) {
                                username = message.getUsername();
                                isLoggedIn = true;
                                Platform.runLater(() -> {
                                    mainStage.setScene(chatRoom);
                                    label_username.setText("Username: " + username);
                                });
                            }
                            break;


                        //Logging in
                        case LOG:

                            //Successful login
                            if (message.getMessage().equals("SUCCESSFUL")) {
                                System.out.println("Login successful");
                                username = message.getUsername();
                                isLoggedIn = true;
                                Platform.runLater(() -> {
                                    mainStage.setScene(chatRoom);
                                    label_username.setText("Username: " + username);
                                });
                            }

                            //User is already online
                            else if (message.getMessage().equals("USER-ONLINE")) {
                                Platform.runLater(() -> {
                                    registerNotification.setText("You can't login from two devices at the same time.");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });
                            }

                            //Wrong password
                            else if (message.getMessage().equals("UNSUCCESSFUL")) {
                                System.out.println("Login unsuccessful");
                                Platform.runLater(() -> {
                                    registerNotification.setText("Wrong password");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });
                            }

                            //User is not found in the database
                            else if (message.getMessage().equals("NOUSER")) {
                                System.out.println("No such user");
                                Message finalMessage = message;
                                Platform.runLater(() -> {
                                    registerNotification.setText("User " + finalMessage.getUsername() + " does not exist");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });
                            }
                            break;


                        //Received a message
                        case MSG:
                            incoming.appendText(message.getUsername() + ": \n" + message.getMessage() + "\n\n");
                            break;


                        case PRIVATE:
                            if (message.getRecipient().equals(username)) {
                                //message.getUsername() is the sender
                                if (!privateChats.containsKey(message.getUsername()) && !message.getMessage().equals(" has left the chat.")) {
                                    Message finalMessage1 = message;
                                    Platform.runLater(() -> openNewPrivateChat(finalMessage1.getUsername()));
                                }
                                Message finalMessage2 = message;
                                if (message.getMessage().equals(" has left the chat.")) {
                                    Platform.runLater(() -> privateChats.get(finalMessage2.getUsername()).appendText(finalMessage2.getUsername() + finalMessage2.getMessage() + "\n\n"));
                                }
                                else {
                                    Platform.runLater(() -> privateChats.get(finalMessage2.getUsername()).appendText(finalMessage2.getUsername() + ": \n" + finalMessage2.getMessage() + "\n\n"));
                                }
                            }
                            break;


                        //Notifies this client when another client has logged in
                        case LOGIN:
                            if (isLoggedIn) {
                                onlineUsers.add(message.getUsername());
                                System.out.println("Online Users: " + String.join(", ", onlineUsers));
                                Platform.runLater(ClientMain.this::updateAllOnlineUsers);
                            }
                            break;


                        //Notifies this client when another client has logged out
                        case LOGOUT:
                            onlineUsers.remove(message.getUsername());
                            Platform.runLater(ClientMain.this::updateAllOnlineUsers);
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
