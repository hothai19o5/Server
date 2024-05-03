package model;

import app.MessageType;
import org.json.JSONException;
import org.json.JSONObject;

public class Model_Receive_Message {

    private int fromUserID; // ID người gửi
    private String text;    // Nội dung tin nhắn
    private MessageType messageType;    // Kiểu tin nhắn
    
    public int getFromUserID() {
        return fromUserID;
    }

    public void setFromUserID(int fromUserID) {
        this.fromUserID = fromUserID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
    
    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Model_Receive_Message(int fromUserID, String text, MessageType messageType) {
        this.fromUserID = fromUserID;
        this.text = text;
    }
    // Constructor nhận đối tượng JSON và chuyển nó thành thuộc tính của lớp
    public Model_Receive_Message(Object json) {
        JSONObject obj = (JSONObject) json;
        try {
            // Lấy dữ liệu từ khóa
            messageType = MessageType.toMessageType(obj.getInt("messageType"));
            fromUserID = obj.getInt("fromUserID");
            text = obj.getString("text");
        } catch (JSONException e) {
            System.err.println(e);
        }
    }

   // Tạo đối tượng JSON từ các thuộc tính của lớp
    public JSONObject toJsonObject() {
        try {
            JSONObject json = new JSONObject();
            // Tạo các cặp khóa dữ liệu
            json.put("messageType", messageType.getValue());
            json.put("fromUserID", fromUserID);
            json.put("text", text);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }
}