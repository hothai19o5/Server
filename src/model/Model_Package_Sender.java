package model;

// Gói tin để chuyển dữ liệu file

import org.json.JSONException;
import org.json.JSONObject;

public class Model_Package_Sender {
    private int fileID;
    private byte[] data;
    private boolean finish;

    public Model_Package_Sender() {
    }

    public Model_Package_Sender(int fileID, byte[] data, boolean finish) {
        this.fileID = fileID;
        this.data = data;
        this.finish = finish;
    }
    
    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public boolean isFinish() {
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }
    
    public JSONObject toJSONObject(){
        try {
            JSONObject json = new JSONObject();
            json.put("fileID", fileID);
            json.put("data", data);
            json.put("finish", finish);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
