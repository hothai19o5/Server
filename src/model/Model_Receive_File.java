package model;

import org.json.JSONException;
import org.json.JSONObject;

public class Model_Receive_File {
    private int fileID;
    private String file;
    
    public Model_Receive_File(Object json){
        JSONObject obj = (JSONObject) json;
        try {
            // Lấy dữ liệu từ khóa
            fileID = obj.getInt("fileID");
            file = obj.getString("file");
        } catch (JSONException e) {
            System.err.println(e);
        }
    }
    
    public JSONObject toJsonOject(){
        try {
            JSONObject json = new JSONObject();
            json.put("fileID", fileID);
            json.put("file", file);
            return json;
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
    }

    public Model_Receive_File() {
    }

    public Model_Receive_File(int fileID, String file) {
        this.fileID = fileID;
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
    
    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }
    
}
