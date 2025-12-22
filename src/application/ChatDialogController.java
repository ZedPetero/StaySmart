package application;

import application.model.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;

public class ChatDialogController {

    @FXML
    private Label lblTitle;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextArea txtMessage;

    private int applicationId;
    private int otherUserId;
    private int myId;
    private SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm");

    @FXML
    public void initialize() {
        if (LoginController.getCurrentUser() != null) {
            this.myId = LoginController.getCurrentUser().getId();
        }
    }

    public void setContext(int applicationId, int otherUserId, String otherUserName) {
        this.applicationId = applicationId;
        this.otherUserId = otherUserId;
        lblTitle.setText("Chat with " + otherUserName);
        loadMessages();
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        List<Message> messages = DatabaseHandler.getMessages(applicationId);

        for (Message m : messages) {
            boolean isMe = (m.getSenderId() == myId);
            messagesContainer.getChildren().add(createBubble(m, isMe));
        }

        scrollToBottom();
    }

    @FXML
    private void handleSend() {
        String text = txtMessage.getText().trim();
        if (text.isEmpty())
            return;

        boolean success = DatabaseHandler.saveMessage(applicationId, myId, otherUserId, text);
        if (success) {
            txtMessage.clear();
            loadMessages();
        }
    }

    private HBox createBubble(Message msg, boolean isMe) {
        HBox row = new HBox();
        row.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        VBox bubble = new VBox(2);
        bubble.setMaxWidth(300);
        bubble.getStyleClass().add(isMe ? "msg-me" : "msg-other");

        Label textLbl = new Label(msg.getMessageText());
        textLbl.setWrapText(true);
        textLbl.setStyle("-fx-text-fill: " + (isMe ? "white" : "#333") + "; -fx-font-size: 13px;");

        Label timeLbl = new Label(sdf.format(msg.getTimestamp()));
        timeLbl.setStyle("-fx-text-fill: " + (isMe ? "#e3f2fd" : "#888") + "; -fx-font-size: 10px;");
        timeLbl.setAlignment(Pos.BOTTOM_RIGHT);

        bubble.getChildren().addAll(textLbl, timeLbl);
        row.getChildren().add(bubble);
        return row;
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            scrollPane.applyCss();
            scrollPane.layout();
            scrollPane.setVvalue(1.0);
        });
    }
}
