package assignment7;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class ClientMain extends Application {
    final private int WINDOW_WIDTH = 800;
    final private int WINDOW_HEIGHT = 600;
    private TextArea incoming;
    private TextField outgoing;
    private ObjectInputStream reader;
    private ObjectOutputStream writer;
    private String username;
    private int portAddress;
    private Label registerNotification;
    private Scene chatRoom;
    private static Stage mainStage;

    TextField usernameTextField;
    PasswordField passwordTextField;
    Label label_username;

    private HBox mainBox;
    private VBox onlineList;

    private boolean isLoggedIn = false;

    private static ArrayList<String> onlineUsers = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Establishes a connection to the server
     *
     * @throws IOException
     */
    private void connectToServer() throws IOException {
        @SuppressWarnings("resource")
        Socket clientSock = new Socket("127.0.0.1", 5000);
        //Socket clientSock = new Socket("172.20.10.8", 5050);
        portAddress = clientSock.getLocalPort();
        writer = new ObjectOutputStream(clientSock.getOutputStream());
        reader = new ObjectInputStream(clientSock.getInputStream());
        System.out.println("Connected to the server");
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
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
        connectToServer();

        mainStage = primaryStage;

        mainStage.setTitle("Pair-40 Chat Room");

        //****************************************
        //Set up login screen
        //Username
        Label usernameLabel = new Label("Username: ");
        usernameTextField = new TextField();
        usernameTextField.setMaxHeight(10);
        usernameTextField.setMaxWidth(60);
        HBox usernameBox = new HBox(usernameLabel, usernameTextField);

        //Password
        Label passwordLabel = new Label("Password: ");
        passwordTextField = new PasswordField();
        passwordTextField.setMaxHeight(10);
        passwordTextField.setMaxWidth(60);
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
                        System.out.println("Username and password are in valid format");
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
        Scene loginScreenScene = new Scene(loginScreenGrid, WINDOW_WIDTH, WINDOW_HEIGHT);

        mainStage.setScene(loginScreenScene);


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
        Label chatRoomNotification = new Label("notification");

        outgoing.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER)  {
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
        //Main Control
        VBox chats = new VBox();
        chats.getChildren().addAll(label_username, label_ChatHistory, incoming, label_EnterText, textInput,chatRoomNotification);

        mainBox = new HBox();
        mainBox.getChildren().addAll(chats, onlineList);

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
        for (String s : onlineUsers) {
            if (s.equals(username)) {
                onlineList.getChildren().add(new Button(s + " (me)"));
            } else {
                onlineList.getChildren().add(new Button(s));
            }
        }
    }

    public void CreateGroupChat() {

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
