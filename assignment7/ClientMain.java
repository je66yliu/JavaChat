package assignment7;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableListValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Pattern;

import static javafx.scene.paint.Color.WHITE;

public class ClientMain extends Application {
    final private int WINDOW_WIDTH = 800;
    final private int WINDOW_HEIGHT = 600;
    private TextArea incoming;
    private TextArea outgoing;
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
    private static HashMap<String, Stage> privateChatWindows = new HashMap<>();

    private static boolean isInGroupChat = false;
    private static ArrayList<String> groupChatMembers = new ArrayList<>();
    private Stage groupChatStage;
    private TextArea groupChatTextArea;

    public static void main(String[] args) {
        launch(args);
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPortAddress(int portAddress) {
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

    /****Emoji Initialization****/
    byte[] emoji1_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81};
    String emoji1_String = new String(emoji1_byte, Charset.forName("UTF-8"));

    byte[] emoji2_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x82};
    String emoji2_String = new String(emoji2_byte, Charset.forName("UTF-8"));

    byte[] emoji3_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x83};
    String emoji3_String = new String(emoji3_byte, Charset.forName("UTF-8"));

    byte[] emoji4_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x84};
    String emoji4_String = new String(emoji4_byte, Charset.forName("UTF-8"));

    byte[] emoji5_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x85};
    String emoji5_String = new String(emoji5_byte, Charset.forName("UTF-8"));

    byte[] emoji6_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x86};
    String emoji6_String = new String(emoji6_byte, Charset.forName("UTF-8"));

    byte[] emoji7_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x87};
    String emoji7_String = new String(emoji7_byte, Charset.forName("UTF-8"));

    byte[] emoji8_byte = new byte[]{(byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x88};
    String emoji8_String = new String(emoji8_byte, Charset.forName("UTF-8"));


    /**** Input validation ****/

    private static final Pattern PATTERN = Pattern.compile(
            "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Check if the ip address is valid
     *
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
     *
     * @param user
     * @return a string of results
     */
    private String usernameValidation(String user) {
        StringBuilder validation = new StringBuilder();
        if (user == null) {
            validation.append("Username can't be empty.");
            return validation.toString();
        }

        if (user.isEmpty()) {
            validation.append("Username can't be empty.");
            return validation.toString();
        }

        if (user.length() < 4) {
            validation.append("Username has to be at least 4 characters.\n");
        }

        if (hasSpecialChar.matcher(user).find()) {
            validation.append("Username can not contain special characters.\n");
        }

        if (validation.length() == 0) {
            validation.append("Success");
        }
        return validation.toString();
    }

    /**
     * Check if the password is valid
     *
     * @param pass
     * @return a string of results
     */
    private String passwordValidation(String pass) {
        StringBuilder validation = new StringBuilder();

        if (pass == null) {
            validation.append("Password can't be empty.");
            return validation.toString();
        }

        if (pass.isEmpty()) {
            validation.append("Password can't be empty.");
            return validation.toString();
        }

        if (pass.length() < 5) {
            validation.append("Password has to be at least 5 characters.\n");
        }

        if (!hasUppercase.matcher(pass).find()) {
            validation.append("Password needs an upper case letter.\n");
        }

        if (!hasLowercase.matcher(pass).find()) {
            validation.append("Password needs a lower case letter.\n");
        }

        if (!hasNumber.matcher(pass).find()) {
            validation.append("Password needs a number.\n");
        }

        if (!hasSpecialChar.matcher(pass).find()) {
            validation.append("Password needs a special character.\n");
        }

        if (validation.length() == 0) {
            validation.append("Success");
        }
        return validation.toString();
    }

    /**
     * Create a HBox that contains the logo of of the app
     *
     * @return the new logo HBox object
     */
    public HBox getLogo(int size, int padding) {
        Text group_text = new Text("Group");
        group_text.setFill(Color.LIGHTBLUE);
        group_text.setFont(Font.font("Helvetica", FontWeight.BOLD, FontPosture.ITALIC, size));
        Text we_text = new Text("We");
        we_text.setFill(Color.WHITE);
        we_text.setFont(Font.font("Helvetica", FontWeight.BOLD, FontPosture.ITALIC, size));

        HBox titleBox = new HBox(group_text, we_text);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setSpacing(0);
        titleBox.setPadding(new Insets(padding));
        return titleBox;
    }

    /**
     * Create a combo box that contains the emojis
     *
     * @return emoji combo box
     */
    public ComboBox<String> getEmojiBox(TextArea input) {
        ComboBox<String> emoji = new ComboBox<>();
        emoji.getItems().addAll(emoji1_String, emoji2_String, emoji3_String, emoji4_String, emoji5_String,
                emoji6_String, emoji7_String, emoji8_String);
        emoji.setPromptText(emoji1_String);

        emoji.setOnAction(e -> {
            input.appendText(emoji.getValue());
            emoji.setPromptText(emoji1_String);
            input.requestFocus();
        });
        return emoji;
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        mainStage = primaryStage;
        mainStage.setTitle("Pair-40 Chat Room");

        System.out.println(Font.getFamilies().toString());
        /***********************
         * SERVER CONFIG SCREEN*
         * *********************/

        //IP Address
        Label ipAddressLabel = new Label("Server IP Address: ");
        ipAddressLabel.setFont(Font.font("Book Antiqua"));

        ipAddressTextField = new TextField();
        ipAddressTextField.setMaxHeight(10);
        ipAddressTextField.setMaxWidth(150);
        ipAddressTextField.setText("127.0.0.1");

        HBox ipConfigBox = new HBox(ipAddressLabel, ipAddressTextField);
        ipConfigBox.setPrefWidth(500);
        ipConfigBox.setSpacing(10);

        //Port number
        Label portLabel = new Label("Port: ");
        portLabel.setFont(Font.font("Book Antiqua"));

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
        portConfigBox.setSpacing(83);

        //Connection notification
        Label connectionNotification = new Label("");
        connectionNotification.setFont(Font.font("Book Antiqua"));


        //Connect button
        Button connectServerButton = new Button("Connect to server");
        connectServerButton.setOnAction(e -> {
            if (validateIP(ipAddressTextField.getText())) {
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
            } else {
                connectionNotification.setText("Invalid IP Address");
            }
        });

        //Progress bar
        ProgressBar connectingBar = new ProgressBar();
        connectingBar.setPrefWidth(150);
        connectingBar.setProgress(-1);


        VBox connectionConfigBox = new VBox();
        connectionConfigBox.setPrefHeight(200);
        connectionConfigBox.setPrefWidth(340);
        connectionConfigBox.setPadding(new Insets(30));
        connectionConfigBox.setAlignment(Pos.CENTER);
        connectionConfigBox.setSpacing(10);
        connectionConfigBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        connectionConfigBox.getChildren().addAll(ipConfigBox, portConfigBox, connectServerButton, connectionNotification, connectingBar);

        String connectionConfigBoxLayout = "-fx-border-color: orange;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 3;\n" +
                "-fx-border-style: dashed;\n";

        connectionConfigBox.setStyle(connectionConfigBoxLayout);

        GridPane serverConfigPane = new GridPane();
        serverConfigPane.setAlignment(Pos.CENTER);
        serverConfigPane.setBackground(new Background(new BackgroundFill(Color.rgb(223, 125, 60),
                CornerRadii.EMPTY, Insets.EMPTY)));

        serverConfigPane.add(getLogo(50, 20), 0, 0);
        serverConfigPane.add(connectionConfigBox, 0, 1);

        Scene serverConfigScene = new Scene(serverConfigPane, WINDOW_WIDTH, WINDOW_HEIGHT);

        // mainStage.setScene(serverConfigScene);


        /***************
         * LOGIN SCREEN*
         * *************/
        //Set up login screen

        //Username
        Label usernameLabel = new Label("Username: ");
        usernameLabel.setFont(Font.font("Book Antiqua"));


        usernameTextField = new TextField();
        usernameTextField.setMaxHeight(10);
        usernameTextField.setMaxWidth(200);
        HBox usernameBox = new HBox(usernameLabel, usernameTextField);
        usernameBox.setSpacing(20);

        //Password
        Label passwordLabel = new Label("Password: ");
        passwordLabel.setFont(Font.font("Book Antiqua"));


        passwordTextField = new PasswordField();
        passwordTextField.setMaxHeight(10);
        passwordTextField.setMaxWidth(200);
        HBox passwordBox = new HBox(passwordLabel, passwordTextField);
        passwordBox.setSpacing(23);

        //Buttons
        Button loginButton = new Button("Login");
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

                if (username_result.equals("Success")) {
                    if (password_result.equals("Success")) {
                        System.out.println("Username and password are invalid format");
                        writer.writeObject(new Message(portAddress, MessageType.LOG, null, usernameTextField.getText(), passwordTextField.getText()));
                        writer.flush();
                    } else {
                        registerNotification.setText(password_result);
                    }
                } else {
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

                if (username_result.equals("Success")) {
                    if (password_result.equals("Success")) {
                        System.out.println("Username and password are in valid format");
                        writer.writeObject(new Message(portAddress, MessageType.REG, null, usernameTextField.getText(), passwordTextField.getText()));
                        writer.flush();
                    } else {
                        registerNotification.setText(password_result);
                    }
                } else {
                    registerNotification.setText(username_result);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }


        });

//        Button changeToChatRoom = new Button("switch scene test");
        HBox loginButtonBox = new HBox(loginButton, registerButton);
        loginButtonBox.setAlignment(Pos.CENTER);
        loginButtonBox.setSpacing(40);

        //Notification label
        registerNotification = new Label("");
        registerNotification.setFont(Font.font("Book Antiqua"));


        //Grid control

        VBox loginScreenVBox = new VBox(usernameBox, passwordBox, loginButtonBox, registerNotification);
        loginScreenVBox.setPrefHeight(200);
        loginScreenVBox.setPrefWidth(340);
        loginScreenVBox.setPadding(new Insets(30));
        loginScreenVBox.setAlignment(Pos.CENTER);
        loginScreenVBox.setSpacing(10);

        String loginScreeVBoxLayout = "-fx-border-color: orange;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 5;\n" +
                "-fx-border-style: dashed;\n";

        loginScreenVBox.setStyle(loginScreeVBoxLayout);

        loginScreenVBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        GridPane loginScreenGrid = new GridPane();
        loginScreenGrid.setAlignment(Pos.CENTER);
        loginScreenGrid.setBackground(new Background(new BackgroundFill(Color.rgb(223, 125, 60),
                CornerRadii.EMPTY, Insets.EMPTY)));
        loginScreenGrid.add(getLogo(50, 20), 0, 0);
        loginScreenGrid.add(loginScreenVBox, 0, 1);

        loginScreenScene = new Scene(loginScreenGrid, WINDOW_WIDTH, WINDOW_HEIGHT);


        /*******************
         * Chat room SCREEN*
         * *****************/
        //****************************************
        //Set up text output
        label_username = new Label("");
        label_username.setFont(Font.font("Book Antiqua", 14));


        Label label_ChatHistory = new Label("Chat History");
        label_ChatHistory.setFont(Font.font("Book Antiqua"));

        incoming = new TextArea();
        incoming.setPrefHeight(400);
        incoming.setPrefWidth(350);
        incoming.setEditable(false);
        incoming.setWrapText(true);
        incoming.setFont(Font.font("Helvetica", 12));


        //****************************************
        //Set up text input
        Label label_EnterText = new Label("Enter Text Here:");
        label_EnterText.setFont(Font.font("Book Antiqua"));

        outgoing = new TextArea();
        outgoing.setPrefWidth(350);
        outgoing.setPrefHeight(70);
        outgoing.setWrapText(true);
        outgoing.setFont(Font.font("Helvetica", 14));

        ComboBox<String> emoji_chatroom = getEmojiBox(outgoing);
        HBox toolBox = new HBox();
        toolBox.setSpacing(30);
        toolBox.getChildren().addAll(label_EnterText, emoji_chatroom);

        Button sendText = new Button("Send");
        sendText.setPrefWidth(90);
        sendText.setPrefHeight(70);
        Label chatRoomNotification = new Label("");
        chatRoomNotification.setFont(Font.font("Book Antiqua"));

        outgoing.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                try {
                    if (outgoing.getText() == null || outgoing.getText().isEmpty()) {
                        chatRoomNotification.setText("You have to type something before you send it.");
                    } else {
                        System.out.println(outgoing.getText());
                        writer.writeObject(new Message(portAddress, MessageType.MSG, outgoing.getText(), username, null));
                        writer.flush();
                        chatRoomNotification.setText("");
                        outgoing.setText("");
                        outgoing.requestFocus();
                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    keyEvent.consume();
                }
            }
        });

        sendText.setOnAction(e -> {
            try {
                if (outgoing.getText() == null || outgoing.getText().isEmpty()) {
                    chatRoomNotification.setText("You have to type something before you send it.");
                } else {
                    System.out.println(outgoing.getText());
                    writer.writeObject(new Message(portAddress, MessageType.MSG, outgoing.getText(), username, null));
                    writer.flush();
                    outgoing.setText("");
                    chatRoomNotification.setText("");
                    outgoing.requestFocus();
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });


        HBox textInput = new HBox();
        textInput.setSpacing(10);
        textInput.getChildren().addAll(outgoing, sendText);


        //****************************************
        //Set up online users
        Label online = new Label("Online Users:");
        online.setFont(Font.font("Book Antiqua"));

        onlineList = new VBox();
        onlineList.getChildren().add(online);
        onlineList.setPrefWidth(100);

        //****************************************
        //Set up group chat ListView and createGroupChat button
        Label createGroupChat = new Label("Create a Group Chat:");
        createGroupChat.setFont(Font.font("Book Antiqua"));

        groupChatListView = new ListView<>();
        groupChatListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        groupChatListView.setPrefWidth(100);
        groupChatListView.setPrefHeight(480);
        Button createGroupChatButton = new Button("Create");

        createGroupChatButton.setOnAction(e -> {
            ObservableList<String> members = groupChatListView.getSelectionModel().getSelectedItems();
            ArrayList<String> getMembers = new ArrayList<>(members);
            openNewGroupChat(getMembers);
        });

        //****************************************
        //Main Control
        //Chat box
        VBox chats = new VBox();
        chats.setSpacing(10);
        chats.getChildren().addAll(label_username, label_ChatHistory, incoming, toolBox, textInput, chatRoomNotification);

        //Groupchat box
        VBox makeGroupChat = new VBox();
        makeGroupChat.setSpacing(10);
        makeGroupChat.getChildren().addAll(createGroupChat, groupChatListView, createGroupChatButton);


        //Chat function box
        Separator separator1 = new Separator();
        Separator separator2 = new Separator();
        separator1.setOrientation(Orientation.VERTICAL);
        separator2.setOrientation(Orientation.VERTICAL);
        separator1.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));
        separator2.setBackground(new Background(new BackgroundFill(Color.GREY, CornerRadii.EMPTY, Insets.EMPTY)));

        mainBox = new HBox();
        mainBox.getChildren().addAll(makeGroupChat, separator1, onlineList, separator2, chats);
        mainBox.setPadding(new Insets(0));
        mainBox.setSpacing(10);
        mainBox.setAlignment(Pos.CENTER);

        //Overall chat room box
        VBox chat_room_box = new VBox();
        chat_room_box.setSpacing(10);
        chat_room_box.getChildren().addAll(getLogo(50, 0), mainBox);
        chat_room_box.setPadding(new Insets(30, 30, 30, 30));
        chat_room_box.setBackground(new Background(new BackgroundFill(Color.rgb(251, 176, 108),
                CornerRadii.EMPTY, Insets.EMPTY)));

        String chatroombox = "-fx-border-color: darkorange;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 5;\n" +
                "-fx-border-style: dashed;\n";

        chat_room_box.setStyle(chatroombox);

        chatRoom = new Scene(chat_room_box, WINDOW_WIDTH, WINDOW_HEIGHT);


        /******/


        mainStage.setScene(serverConfigScene);


//        //****************************************
//        //Scene Change Control
//        changeToChatRoom.setOnAction(e -> mainStage.setScene(chatRoom));

        //****************************************
        //Closing Controls
        mainStage.setOnCloseRequest(e -> {
            try {
                //Leave all the private chats first, and close all private chat windows
                Set<String> currentlyChatting = privateChats.keySet();
                for (String friend : currentlyChatting) {
                    leaveChat(friend);
                    privateChatWindows.get(friend).close();
                    privateChatWindows.remove(friend);
                }

                if (isInGroupChat) {
                    leaveGroupChat(groupChatMembers);
                    groupChatStage.close();
                }

                writer.writeObject(new Message(portAddress, MessageType.LOGOUT, "", username, null));
                writer.flush();
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
        } else if (!privateChats.containsKey(friend)) {
            Label label_chathistory = new Label("Chat History:");
            label_chathistory.setFont(Font.font("Book Antiqua"));


            TextArea ta = new TextArea();
            ta.setPrefWidth(230);
            ta.setPrefHeight(400);
            ta.setEditable(false);

            privateChats.put(friend, ta);

            Label enterText = new Label("Enter a message:");
            enterText.setFont(Font.font("Book Antiqua"));

            TextArea msg = new TextArea();
            msg.setPrefHeight(70);
            msg.setPrefWidth(300);

            ComboBox<String> privateEmojiBox = getEmojiBox(msg);
            HBox toolBox = new HBox();
            toolBox.setSpacing(30);
            toolBox.getChildren().addAll(enterText, privateEmojiBox);

            Button sendButton = new Button("Send");
            sendButton.setPrefWidth(90);
            sendButton.setPrefHeight(50);

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
                            keyEvent.consume();
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
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            HBox msgPanel = new HBox();
            msgPanel.setSpacing(10);
            msgPanel.getChildren().addAll(msg, sendButton);


            VBox mainChatPanel = new VBox();

            mainChatPanel.setSpacing(10);
            mainChatPanel.getChildren().addAll(getLogo(40, 0), label_chathistory, ta, toolBox, msgPanel);
            mainChatPanel.setPadding(new Insets(30, 30, 30, 30));
            mainChatPanel.setBackground(new Background(new BackgroundFill(Color.rgb(251, 176, 108),
                    CornerRadii.EMPTY, Insets.EMPTY)));

            String privateChatBox = "-fx-border-color: darkorange;\n" +
                    "-fx-border-insets: 5;\n" +
                    "-fx-border-width: 3;\n" +
                    "-fx-border-style: dashed;\n";

            mainChatPanel.setStyle(privateChatBox);

            Stage privateChatWindow = new Stage();
            privateChatWindow.setTitle("Private chat between " + username + ", " + friend);
            privateChatWindow.setScene(new Scene(mainChatPanel, 450, 450));

            privateChatWindows.put(friend, privateChatWindow);

            privateChatWindow.show();

            privateChatWindow.setOnCloseRequest(e -> leaveChat(friend));
        }
    }

    public void openNewGroupChat(ArrayList<String> members) {
        if (!isInGroupChat) {
            isInGroupChat = true;

            groupChatTextArea = new TextArea();
            groupChatTextArea.setPrefWidth(230);
            groupChatTextArea.setPrefHeight(400);
            groupChatTextArea.setEditable(false);

            groupChatMembers.addAll(members);

            Label label_chathistory = new Label("Chat History:");
            label_chathistory.setFont(Font.font("Book Antiqua"));

            Label enterText = new Label("Enter a message");
            enterText.setFont(Font.font("Book Antiqua"));

            TextArea msg = new TextArea();
            msg.setPrefHeight(70);
            msg.setPrefWidth(300);

            ComboBox<String> privateEmojiBox = getEmojiBox(msg);
            HBox toolBox = new HBox();
            toolBox.setSpacing(30);
            toolBox.getChildren().addAll(enterText, privateEmojiBox);

            Button sendButton = new Button("Send");
            sendButton.setPrefWidth(90);
            sendButton.setPrefHeight(50);

            msg.setOnKeyPressed(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    try {
                        if (!msg.getText().equals("") && msg.getText() != null) {
                            groupChatTextArea.appendText(username + ": \n" + msg.getText() + "\n\n");
                            Message groupChatMessage = new Message(portAddress, MessageType.GROUP, msg.getText(), username, null);
                            msg.setText("");
                            groupChatMessage.setGroupChatRecipients(members);
                            writer.writeObject(groupChatMessage);
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
                        groupChatTextArea.appendText(username + ": \n" + msg.getText() + "\n\n");
                        Message groupChatMessage = new Message(portAddress, MessageType.GROUP, msg.getText(), username, null);
                        msg.setText("");
                        groupChatMessage.setGroupChatRecipients(members);
                        writer.writeObject(groupChatMessage);
                        writer.flush();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            HBox msgPanel = new HBox();
            msgPanel.setSpacing(10);
            msgPanel.getChildren().addAll(msg, sendButton);

            VBox mainChatPanel = new VBox();

            mainChatPanel.setSpacing(10);
            mainChatPanel.getChildren().addAll(getLogo(40, 0), label_chathistory, groupChatTextArea, toolBox, msgPanel);
            mainChatPanel.setPadding(new Insets(30, 30, 30, 30));
            mainChatPanel.setBackground(new Background(new BackgroundFill(Color.rgb(251, 176, 108),
                    CornerRadii.EMPTY, Insets.EMPTY)));

            String privateChatBox = "-fx-border-color: darkorange;\n" +
                    "-fx-border-insets: 5;\n" +
                    "-fx-border-width: 3;\n" +
                    "-fx-border-style: dashed;\n";

            mainChatPanel.setStyle(privateChatBox);

            groupChatStage = new Stage();
            groupChatStage.setTitle("Group chat between " + username + ", " + String.join(", ", members));
            groupChatStage.setScene(new Scene(mainChatPanel, 450, 450));

            groupChatStage.show();

            groupChatStage.setOnCloseRequest(e -> leaveGroupChat(members));
        }
    }

    public void leaveChat(String friend) {
        try {
            privateChats.remove(friend);
            Message privateMessage = new Message(portAddress, MessageType.PRIVATE, " has left the chat.", username, null);
            privateMessage.setRecipient(friend);
            writer.writeObject(privateMessage);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void leaveGroupChat(ArrayList<String> members) {
        try {
            isInGroupChat = false;
            Message groupMessage = new Message(portAddress, MessageType.GROUP, " has left the chat.", username, null);
            groupMessage.setGroupChatRecipients(members);
            writer.writeObject(groupMessage);
            writer.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
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
                                    label_username.setText("Current User:  " + username);
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
                                    label_username.setText("Current User:  " + username);
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
                                } else {
                                    Platform.runLater(() -> privateChats.get(finalMessage2.getUsername()).appendText(finalMessage2.getUsername() + ": \n" + finalMessage2.getMessage() + "\n\n"));
                                }
                            }
                            break;


                        //Group chatting
                        case GROUP:
                            if (message.getGroupChatRecipients().contains(username)) {
                                if (!isInGroupChat) {
                                    ArrayList<String> members = new ArrayList<>(message.getGroupChatRecipients());
                                    members.add(message.getUsername());
                                    members.remove(username);
                                    Platform.runLater(() -> openNewGroupChat(members));
                                }
                                if (message.getMessage().equals(" has left the chat.")) {
                                    Message finalMessage3 = message;
                                    Platform.runLater(() -> groupChatTextArea.appendText(finalMessage3.getUsername() + finalMessage3.getMessage() + "\n\n"));
                                } else {
                                    Message finalMessage4 = message;
                                    Platform.runLater(() -> groupChatTextArea.appendText(finalMessage4.getUsername() + ": \n" + finalMessage4.getMessage() + "\n\n"));
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
