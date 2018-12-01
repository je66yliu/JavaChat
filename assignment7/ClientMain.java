package assignment7;

import javafx.application.Application;
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
    private int WINDOW_WIDTH;
    private int WINDOW_HEIGHT;
    private TextArea incoming;
    private TextField outgoing;
    private BufferedReader reader;
    private PrintWriter writer;



    public static void main(String[] args) {
        launch(args);
    }

    private void connectToServer() throws IOException {
        @SuppressWarnings("resource")
        Socket clientSock = new Socket("127.0.0.1", 5000);
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

        primaryStage.setTitle("Pair-40 Chat Room");

        /***** Set up text output *****/
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


        sendText.setOnAction(e->{
            System.out.println(outgoing.getText());
            writer.println(outgoing.getText());
            writer.flush();
            outgoing.setText("");
            outgoing.requestFocus();
        });

        HBox textInput = new HBox();
        textInput.getChildren().addAll(outgoing, sendText);


        /***** Main Control *****/
        VBox mainBox = new VBox();
        mainBox.getChildren().addAll(label_ChatHistory, incoming, label_EnterText, textInput);

        GridPane mainGrid = new GridPane();
        mainGrid.getChildren().addAll(mainBox);

        Scene mainScene = new Scene(mainGrid, 500, 500);

        primaryStage.setScene(mainScene);
        primaryStage.show();
    }


    class IncomingReader implements Runnable {

        @Override
        public void run() {
            String message;

            try {
                while ((message = reader.readLine()) != null) {
                    incoming.appendText(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
