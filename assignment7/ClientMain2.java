//package assignment7;
//
//import javafx.application.Application;
//import javafx.beans.value.ChangeListener;
//import javafx.beans.value.ObservableValue;
//import javafx.event.ActionEvent;
//import javafx.scene.Node;
//import javafx.scene.Scene;
//import javafx.scene.control.Button;
//import javafx.scene.control.Label;
//import javafx.scene.control.TextArea;
//import javafx.scene.control.TextField;
//import javafx.scene.layout.GridPane;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//import java.beans.EventHandler;
//import java.io.*;
//import java.net.Socket;
//import java.util.Collection;
//
//public class ClientMain2 extends Application {
//    final private int WINDOW_WIDTH = 800;
//    final private int WINDOW_HEIGHT = 600;
//    private TextArea incoming;
//    private TextField outgoing;
//    private ObjectInputStream reader;
//    private ObjectOutputStream writer;
//    private String username;
//    private int userPot;
//    private int portAddress;
//
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    private void connectToServer() throws IOException {
//        @SuppressWarnings("resource")
//        Socket clientSock = new Socket("127.0.0.1", 5000);
//        portAddress = clientSock.getLocalPort();
//        writer = new ObjectOutputStream(clientSock.getOutputStream());
//        reader = new ObjectInputStream(clientSock.getInputStream());
//        System.out.println("Connected to the server");
//        Thread readerThread = new Thread(new IncomingReader());
//        readerThread.start();
//    }
//
//
//    @Override
//    public void start(Stage primaryStage) throws Exception {
//        connectToServer();
//
//        primaryStage.setTitle("Pair-40 Chat Room");
//
//        /***** Set up login screen *****/
//        //Username
//        Label usernameLabel = new Label("Username: ");
//        TextField usernameTextField = new TextField();
//        usernameTextField.setMaxHeight(10);
//        usernameTextField.setMaxWidth(60);
//        HBox usernameBox = new HBox(usernameLabel, usernameTextField);
//
//        //Password
//        Label passwordLabel = new Label("Password: ");
//        TextField passwordTextField = new TextField();
//        passwordTextField.setMaxHeight(10);
//        passwordTextField.setMaxWidth(60);
//        HBox passwordBox = new HBox(passwordLabel, passwordTextField);
//
//        //Buttons
//        Button loginButton = new Button("Login: ");
//
//        //Register button
//        Button registerButton = new Button("Register");
////        registerButton.setOnAction(e -> {
////            try {
////                System.out.println("Client Username_password: " + usernameTextField.getText() + "_" + passwordTextField.getText());
////                writer.writeObject(portAddress + "_" + "UPS_" + usernameTextField.getText() + "_" + passwordTextField.getText());
////                writer.flush();
////                usernameTextField.setText("");
////                passwordTextField.setText("");
////                usernameTextField.requestFocus();
////            }
////            catch (IOException ex) {
////                ex.printStackTrace();
////            }
////        });
//
//        Button changeToChatRoom = new Button("switch scene test");
//        HBox loginButtonBox = new HBox(loginButton, registerButton, changeToChatRoom);
//
//        //Notification label
//        Label registerNotification = new Label("");
//
//        //Grid control
//        VBox loginScreenVBox = new VBox(usernameBox, passwordBox, loginButtonBox,registerNotification);
//        GridPane loginScreenGrid = new GridPane();
//        loginScreenGrid.getChildren().addAll(loginScreenVBox);
//        Scene loginScreenScene = new Scene(loginScreenGrid, WINDOW_WIDTH, WINDOW_HEIGHT);
//
//        primaryStage.setScene(loginScreenScene);
//
//
//        /***** Set up text output *****/
//        Label label_ChatHistory = new Label("Chat History");
//        incoming = new TextArea();
//        incoming.setMaxHeight(200);
//        incoming.setMaxWidth(400);
//        incoming.setEditable(false);
//
//        //Auto scroll to bottom when received new messages
//        /*
//        incoming.textProperty().addListener(new ChangeListener<Object>() {
//            @Override
//            public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
//                incoming.setScrollTop(Double.MAX_VALUE);
//            }
//        });*/
//
//
//        /***** Set up text input *****/
//        Label label_EnterText = new Label("Enter Text Here");
//        outgoing = new TextField();
//        outgoing.setMaxWidth(400);
//        outgoing.setMaxHeight(20);
//        Button sendText = new Button("Send");
//
//
//        sendText.setOnAction(e -> {
//            try {
//                System.out.println(outgoing.getText());
//                writer.writeObject(new Message(portAddress, MessageType.MSG, outgoing.getText(), null, null));
//                writer.flush();
//                outgoing.setText("");
//                usernameTextField.setText("");
//                passwordTextField.setText("");
//                usernameTextField.requestFocus();
//            }
//            catch (IOException ex) {
//                ex.printStackTrace();
//            }
//        });
//
//        HBox textInput = new HBox();
//        textInput.getChildren().addAll(outgoing, sendText);
//
//
//        /***** Main Control *****/
//        VBox mainBox = new VBox();
//        mainBox.getChildren().addAll(label_ChatHistory, incoming, label_EnterText, textInput);
//
//        GridPane chatRoomGrid = new GridPane();
//        chatRoomGrid.getChildren().addAll(mainBox);
//
//        Scene chatRoom = new Scene(chatRoomGrid, WINDOW_WIDTH, WINDOW_HEIGHT);
//
//
//        /***** Scene Change Control *****/
//        changeToChatRoom.setOnAction(e -> primaryStage.setScene(chatRoom));
//
//
//        primaryStage.show();
//    }
//
//
//    class IncomingReader implements Runnable {
//
//        @Override
//        public void run() {
//            Message message;
//
//            try {
//                while ((message = (Message)reader.readObject()) != null) {
//                    /*****Process message*****/
//
//
//                    incoming.appendText(message.getMessage() + "\n");
//                }
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
