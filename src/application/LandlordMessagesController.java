package application;

import application.model.Application;
import application.model.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class LandlordMessagesController {

    @FXML
    private VBox conversationList;
    @FXML
    private Label lblChatName;
    @FXML
    private Label lblPropertyTitle;
    @FXML
    private ScrollPane messagesScroll;
    @FXML
    private VBox messagesContainer;
    @FXML
    private TextField txtMessage;

    // Search Field
    @FXML
    private TextField txtSearch;

    private int myId;
    private Application currentApp;
    private List<Application> allConversations;
    private SimpleDateFormat timeSdf = new SimpleDateFormat("HH:mm");
    private SimpleDateFormat dateSdf = new SimpleDateFormat("MMM dd");

    @FXML
    public void initialize() {
        if (LoginController.getCurrentUser() != null) {
            this.myId = LoginController.getCurrentUser().getId();
            loadConversations();
        }

        // Search Listener
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((observable, oldValue, newValue) -> filterConversations(newValue));
        }
    }

    private void loadConversations() {
        allConversations = DatabaseHandler.getLandlordConversations(myId);
        renderConversations(allConversations);
    }

    private void filterConversations(String query) {
        if (allConversations == null)
            return;
        if (query == null || query.isEmpty()) {
            renderConversations(allConversations);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Application> filtered = allConversations.stream()
                .filter(app -> (app.getTenantName() != null && app.getTenantName().toLowerCase().contains(lowerQuery))
                        ||
                        (app.getPropertyName() != null && app.getPropertyName().toLowerCase().contains(lowerQuery)))
                .collect(Collectors.toList());

        renderConversations(filtered);
    }

    private void renderConversations(List<Application> apps) {
        conversationList.getChildren().clear();

        if (apps.isEmpty()) {
            Label placeHolder = new Label("No active conversations.");
            placeHolder.setStyle("-fx-padding: 20; -fx-text-fill: #999;");
            conversationList.getChildren().add(placeHolder);
            return;
        }

        for (Application app : apps) {
            HBox item = createConversationItem(app);
            conversationList.getChildren().add(item);
        }
    }

    private HBox createConversationItem(Application app) {
        HBox row = new HBox();
        row.getStyleClass().add("conversation-item");
        row.setSpacing(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setOnMouseClicked(e -> selectConversation(app, row));

        VBox content = new VBox();
        HBox.setHgrow(content, Priority.ALWAYS);

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(app.getTenantName() != null ? app.getTenantName() : "Unknown Tenant");
        nameLbl.getStyleClass().add("conv-name");

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label timeLbl = new Label(dateSdf.format(app.getApplyDate()));
        timeLbl.getStyleClass().add("conv-time");

        top.getChildren().addAll(nameLbl, spacer, timeLbl);

        Label detailsLbl = new Label(app.getPropertyName() + " - " + app.getRoomNumber());
        detailsLbl.getStyleClass().add("conv-preview");

        content.getChildren().addAll(top, detailsLbl);
        row.getChildren().add(content);

        return row;
    }

    private void selectConversation(Application app, HBox row) {
        this.currentApp = app;

        conversationList.getChildren().forEach(n -> n.getStyleClass().remove("active-item"));
        row.getStyleClass().add("active-item");

        lblChatName.setText(app.getTenantName());
        lblPropertyTitle.setText(app.getPropertyName() + " (Room " + app.getRoomNumber() + ")");

        loadMessages(app);
    }

    private void loadMessages(Application app) {
        messagesContainer.getChildren().clear();
        List<Message> messages = DatabaseHandler.getMessages(app.getId());

        if (messages.isEmpty()) {
            Label placeHolder = new Label("No messages yet. Start the conversation!");
            placeHolder.setStyle("-fx-text-fill: #999; -fx-padding: 20;");
            placeHolder.setMaxWidth(Double.MAX_VALUE);
            placeHolder.setAlignment(Pos.CENTER);
            messagesContainer.getChildren().add(placeHolder);
        } else {
            for (Message m : messages) {
                boolean isMe = (m.getSenderId() == myId);
                addMessageBubble(m, isMe);
            }
        }
        scrollToBottom();
    }

    private void addMessageBubble(Message msg, boolean isMe) {
        HBox row = new HBox();
        row.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.getStyleClass().add("msg-row");

        VBox bubble = new VBox();
        bubble.getStyleClass().add("msg-bubble");
        bubble.getStyleClass().add(isMe ? "outgoing" : "incoming");

        Label textLbl = new Label(msg.getMessageText());
        textLbl.setWrapText(true);
        textLbl.setStyle(isMe ? "-fx-text-fill: white;" : "-fx-text-fill: #333;");

        bubble.getChildren().add(textLbl);
        row.getChildren().add(bubble);

        // Timestamp row
        Label timeLbl = new Label(timeSdf.format(msg.getTimestamp()));
        timeLbl.getStyleClass().add(isMe ? "timestamp-right" : "timestamp-left");

        messagesContainer.getChildren().addAll(row, timeLbl);
    }

    @FXML
    private void handleSend() {
        if (currentApp == null)
            return;
        String text = txtMessage.getText().trim();
        if (text.isEmpty())
            return;

        // Send to Tenant (app.getTenantId())
        boolean success = DatabaseHandler.saveMessage(currentApp.getId(), myId, currentApp.getTenantId(), text);
        if (success) {
            txtMessage.clear();
            loadMessages(currentApp); // Refresh
        }
    }

    private void scrollToBottom() {
        Platform.runLater(() -> {
            messagesScroll.applyCss();
            messagesScroll.layout();
            messagesScroll.setVvalue(1.0);
        });
    }
}
