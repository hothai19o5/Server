package model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author admin
 */
public class Model_Register {
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public Model_Register() {
    }

    public Model_Register(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }
    // Nếu muốn gửi được dữ liệu từ client sang server thì cần Model_Register giống nhau, lỗi này fix mất 3 ngày, ( Bùi Hùng fix )
    public JSONObject toJSonObject(){
        try {
            JSONObject json = new JSONObject();
            json.put("userName", userName);
            json.put("password", password);
            return json;
        } catch (JSONException e) {
            return null;
        }
    }

}
