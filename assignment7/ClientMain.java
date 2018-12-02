package assignment7;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.beans.EventHandler;
import java.io.*;
import java.net.Socket;
import java.util.Collection;

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
    TextField passwordTextField;
    Label label_username;

    public static void main(String[] args) {
        launch(args);
    }

    private void connectToServer() throws IOException {
        @SuppressWarnings("resource")
        Socket clientSock = new Socket("127.0.0.1", 5000);
        portAddress = clientSock.getLocalPort();
        writer = new ObjectOutputStream(clientSock.getOutputStream());
        reader = new ObjectInputStream(clientSock.getInputStream());
        System.out.println("Connected to the server");
        Thread readerThread = new Thread(new IncomingReader());
        readerThread.start();
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        connectToServer();

        mainStage = primaryStage;

        mainStage.setTitle("Pair-40 Chat Room");

        /***** Set up login screen *****/
        //Username
        Label usernameLabel = new Label("Username: ");
        usernameTextField = new TextField();
        usernameTextField.setMaxHeight(10);
        usernameTextField.setMaxWidth(60);
        HBox usernameBox = new HBox(usernameLabel, usernameTextField);

        //Password
        Label passwordLabel = new Label("Password: ");
        passwordTextField = new TextField();
        passwordTextField.setMaxHeight(10);
        passwordTextField.setMaxWidth(60);
        HBox passwordBox = new HBox(passwordLabel, passwordTextField);

        //Buttons
        Button loginButton = new Button("Login: ");
        loginButton.setOnAction(e -> {
            try {
                System.out.println("Logging in with: " + usernameTextField.getText() + "_" + passwordTextField.getText());
                writer.writeObject(new Message(portAddress, MessageType.LOG, null, usernameTextField.getText(), passwordTextField.getText()));
                writer.flush();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        //Register button
        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            try {
                System.out.println("Registering new account: " + usernameTextField.getText() + "_" + passwordTextField.getText());
                writer.writeObject(new Message(portAddress, MessageType.REG, null, usernameTextField.getText(), passwordTextField.getText()));
                writer.flush();
                usernameTextField.setText("");
                passwordTextField.setText("");
                usernameTextField.requestFocus();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        Button changeToChatRoom = new Button("switch scene test");
        HBox loginButtonBox = new HBox(loginButton, registerButton, changeToChatRoom);

        //Notification label
        registerNotification = new Label("");

        //Grid control
        VBox loginScreenVBox = new VBox(usernameBox, passwordBox, loginButtonBox,registerNotification);
        GridPane loginScreenGrid = new GridPane();
        loginScreenGrid.getChildren().addAll(loginScreenVBox);
        Scene loginScreenScene = new Scene(loginScreenGrid, WINDOW_WIDTH, WINDOW_HEIGHT);

        mainStage.setScene(loginScreenScene);


        /***** Set up text output *****/
        label_username = new Label("");
        Label label_ChatHistory = new Label("Chat History");
        incoming = new TextArea();
        incoming.setMaxHeight(200);
        incoming.setMaxWidth(400);
        incoming.setEditable(false);


        /***** Set up text input *****/
        Label label_EnterText = new Label("Enter Text Here");
        outgoing = new TextField();
        outgoing.setMaxWidth(400);
        outgoing.setMaxHeight(20);
        Button sendText = new Button("Send");

        sendText.setOnAction(e -> {
            try {
                System.out.println(outgoing.getText());
                writer.writeObject(new Message(portAddress, MessageType.MSG, outgoing.getText(), null, null));
                writer.flush();
                outgoing.setText("");
                usernameTextField.setText("");
                passwordTextField.setText("");
                usernameTextField.requestFocus();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        HBox textInput = new HBox();
        textInput.getChildren().addAll(outgoing, sendText);


        /***** Main Control *****/
        VBox mainBox = new VBox();
        mainBox.getChildren().addAll(label_username, label_ChatHistory, incoming, label_EnterText, textInput);

        GridPane chatRoomGrid = new GridPane();
        chatRoomGrid.getChildren().addAll(mainBox);

        chatRoom = new Scene(chatRoomGrid, WINDOW_WIDTH, WINDOW_HEIGHT);


        /***** Scene Change Control *****/
        changeToChatRoom.setOnAction(e -> mainStage.setScene(chatRoom));

        /***** Closing Controls *****/
        mainStage.setOnCloseRequest(e -> {
            try {
                writer.writeObject(new Message(portAddress, MessageType.EXIT, "", username, null));
                reader.close();
                writer.close();
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        mainStage.show();
    }


    class IncomingReader implements Runnable {

        @Override
        public void run() {
            Message message;

            try {
                while ((message = (Message)reader.readObject()) != null) {
                    switch (message.getMessageType()) {
                        case REG:
                            if (message.getMessage().equals("dupUser")) {
                                Platform.runLater(() -> registerNotification.setText("User exists"));
                            }
                            else if (message.getMessage().equals("createdUser")) {
                                username = message.getUsername();
                                Platform.runLater(() -> {
                                    mainStage.setScene(chatRoom);
                                    label_username.setText("Username: " + username);
                                });
                            }
                            break;

                        case LOG:
                            if (message.getMessage().equals("SUCCESSFUL")) {
                                System.out.println("Login successful");
                                username = message.getUsername();
                                Platform.runLater(() -> {
                                    mainStage.setScene(chatRoom);
                                    label_username.setText("Username: " + username);
                                });
                            }
                            else if (message.getMessage().equals("USER-ONLINE")) {
                                Platform.runLater(() -> {
                                    registerNotification.setText("You can't login from two devices at the same time.");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });
                            }
                            else if (message.getMessage().equals("UNSUCCESSFUL")) {
                                System.out.println("Login unsuccessful");
                                Platform.runLater(() -> {
                                    registerNotification.setText("Wrong password");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });
                            }
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

                        case MSG:
                            incoming.appendText(message.getMessage() + '\n');
                            break;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
