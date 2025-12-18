package application.model;

import java.sql.Timestamp;

public class Message {
    private int id;
    private int applicationId;
    private int senderId;
    private int receiverId;
    private String messageText;
    private Timestamp timestamp;

    public Message(int id, int applicationId, int senderId, int receiverId, String messageText, Timestamp timestamp) {
        this.id = id;
        this.applicationId = applicationId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public String getMessageText() {
        return messageText;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
