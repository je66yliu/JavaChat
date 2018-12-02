package assignment7;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private int portAddress;
    private StringProperty registerNotificationText;
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
        InputStreamReader streamReader = new InputStreamReader(clientSock.getInputStream());
        reader = new BufferedReader(streamReader);
        writer = new PrintWriter(clientSock.getOutputStream());
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
            System.out.println("Logging in with: " + usernameTextField.getText() + "_" + passwordTextField.getText());
            writer.println(Integer.toString(portAddress) + "_" + "LOG_" + usernameTextField.getText() + "_" + passwordTextField.getText());
            writer.flush();
        });

        //Register button
        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            System.out.println("Registering new account: " + usernameTextField.getText() + "_" + passwordTextField.getText());
            writer.println(Integer.toString(portAddress) + "_" + "REG_" + usernameTextField.getText() + "_" + passwordTextField.getText());
            writer.flush();
            usernameTextField.setText("");
            passwordTextField.setText("");
            usernameTextField.requestFocus();
        });

        Button changeToChatRoom = new Button("switch scene test");
        HBox loginButtonBox = new HBox(loginButton, registerButton, changeToChatRoom);

        //Notification label
        registerNotification = new Label("");
//        registerNotificationText = new SimpleStringProperty();
//        registerNotification.setText("Notification");
//        registerNotification.textProperty().bind(registerNotificationText);

        //Grid control
        VBox loginScreenVBox = new VBox(usernameBox, passwordBox, loginButtonBox, registerNotification);
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

        //Auto scroll to bottom when received new messages
        /*
        incoming.textProperty().addListener(new ChangeListener<Object>() {
            @Override
            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                incoming.setScrollTop(Double.MAX_VALUE);
            }
        });*/


        /***** Set up text input *****/
        Label label_EnterText = new Label("Enter Text Here");
        outgoing = new TextField();
        outgoing.setMaxWidth(400);
        outgoing.setMaxHeight(20);
        Button sendText = new Button("Send");


        sendText.setOnAction(e -> {
            System.out.println("Client Sent: " + outgoing.getText());
            writer.println(Integer.toString(portAddress) + "_" + "MSG_" + outgoing.getText());
            writer.flush();
            outgoing.setText("");
            outgoing.requestFocus();
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
        changeToChatRoom.setOnAction(e -> {
            mainStage.setScene(chatRoom);
        });


        mainStage.show();
    }

    public void setRegisterNotificationText(String input) {
        registerNotification.setText(input);
    }

    public void setUsername(String input) {
        this.username = input;
    }

    public static Stage getMainStage() {
        return mainStage;
    }

    public Scene getChatRoomScene() {
        return chatRoom;
    }

    public TextField getUsernameTextField() {
        return usernameTextField;
    }

    public TextField getPasswordTextField() {
        return passwordTextField;
    }

    public void setLabel_username(String input) {
        label_username.setText(input);
    }


    class IncomingReader implements Runnable {

        @Override
        public void run() {
            String messageReceived;
            String messageDelivered;
            String messageType;
            int lengthOfVerification;

            try {
                while ((messageReceived = reader.readLine()) != null) {
                    System.out.println("Client received: " + messageReceived);
                    /*****Process message*****/
                    String[] messageProcessing = messageReceived.split("_");
                    messageType = messageProcessing[0];



                    switch (messageType) {
                        /**
                         * Message type
                         * REG: registering new user
                         * LOG: login to the system
                         * MSG: messages
                         */
                        case "REG":
                            if (messageProcessing[1].equals("dupUser")) {
                                //If the username already exists
                                Platform.runLater(() -> {
                                    setRegisterNotificationText("User exists");
                                });
                            } else if (messageProcessing[1].equals("createdUser")) {
                                //New user will be able to log into the system
                                setUsername(messageProcessing[2]);
                                Platform.runLater(() -> {
                                    getMainStage().setScene(getChatRoomScene());
                                    setLabel_username("Username: " + username);
                                });
                            }

                            break;

                        case "LOG":
                            lengthOfVerification = messageProcessing[0].length()+messageProcessing[1].length();

                            if (messageProcessing[1].equals("SUCCESSFUL")) {

                                System.out.println("Login successful");

                                //User logged in
                                setUsername(messageProcessing[2]);
                                Platform.runLater(() -> {
                                    getMainStage().setScene(getChatRoomScene());
                                    setLabel_username("Username: " + username);
                                });
                            } else if (messageProcessing[1].equals("USER-ONLINE")) {
                                //Current user online
                                Platform.runLater(() -> {
                                    setRegisterNotificationText("You can't loggin from two devices at the same time.");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });
                            } else if (messageProcessing[1].equals("UNSUCCESSFUL")) {

                                System.out.println("Login unsuccessful");

                                //Wrong password

                                Platform.runLater(() -> {
                                    setRegisterNotificationText("Wrong password");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });

                            } else if (messageProcessing[1].equals("NOUSER")) {

                                lengthOfVerification = messageProcessing[0].length()+messageProcessing[1].length();

                                System.out.println("No such user");

                                //No such user
                                Platform.runLater(() -> {
                                    setRegisterNotificationText("User " + messageProcessing[2] + " does not exist");
                                    usernameTextField.setText("");
                                    passwordTextField.setText("");
                                    usernameTextField.requestFocus();
                                });

                            }


                        case "MSG":
                            lengthOfVerification = messageProcessing[0].length();

                            messageDelivered = messageReceived.substring(lengthOfVerification + 1);
                            incoming.appendText(messageDelivered + "\n");
                            break;
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
