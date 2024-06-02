package model;

public class Model_Receive_Message {

    private int messageType;    // Kiểu tin nhắn text, emoji, file
    private int fromUserID;     // Gửi từ người dùng có ID là ...
    private String text;    // Nội dung tin nhắn
    private Model_Receive_Image dataImage;  // Các thông tin về ảnh nhận được
    private Model_Receive_File dataFile; // Các thông tin về file nhận được

    public Model_Receive_File getDataFile() {
        return dataFile;
    }

    public void setDataFile(Model_Receive_File dataFile) {
        this.dataFile = dataFile;
    }

    public Model_Receive_Image getDataImage() {
        return dataImage;
    }

    public void setDataImage(Model_Receive_Image dataImage) {
        this.dataImage = dataImage;
    }
    
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
    
    public int getMessageType(){
        return messageType;
    }
    
    public void setMessageType(int messageType){
        this.messageType = messageType;
    }

    public Model_Receive_Message(int fromUserID, String text, int messageType, Model_Receive_Image dataImage, Model_Receive_File dataFile) {
        this.messageType = messageType;
        this.fromUserID = fromUserID;
        this.text = text;
        this.dataImage = dataImage;
        this.dataFile = dataFile;
    }
    
}