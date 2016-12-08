package server.controller;

import global.logging.Log;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;
import server.modules.Server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Maximilian Schirm
 * @created 08.12.2016
 */

public class ServerController {

    private class Console extends OutputStream{

        TextArea textArea;

        public Console(TextArea textArea){
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            Platform.runLater( -> textArea.appendText(String.valueOf((char)b)));
        }
    }

    private Server server;
    private Console consoleStream;

    @FXML
    Label serverAdressLabel;

    @FXML
    TextArea serverConsoleTextArea;



    @FXML
    public void initialize(){
        consoleStream = new Console(serverConsoleTextArea);
        Log.alterOutputStream(consoleStream);
    }

    public void removeMicroserviceButtonPressed(ActionEvent actionEvent) {

    }

    public void removeClientButtonPressed(ActionEvent actionEvent) {

    }

    public void cancelJobButtonPressed(ActionEvent actionEvent) {

    }

    public PrintStream getConsolePrintStream(){

    }

}
