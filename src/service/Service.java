package service;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import java.sql.SQLException;
import java.util.List;
import javax.swing.JTextArea;
import model.Model_Register;
import model.Model_Message;
import model.Model_User_Account;

/**
 *
 * @author admin
 */
public class Service {
    // Khai báo các biến instance để đảm bảo lớp Service chỉ có một đối tượng duy nhất (Singleton pattern).
    private static Service instance;
    // server là đối tượng SocketIOServer để quản lý kết nối và giao tiếp với các client.
    private SocketIOServer server;
    private ServiceUser serviceUser;
    // server là đối tượng SocketIOServer để quản lý kết nối và giao tiếp với các client.
    private JTextArea textArea;
    // server là đối tượng SocketIOServer để quản lý kết nối và giao tiếp với các client.
    private final int PORT_NUMBER = 9999;
    // Phương thức getInstance trả về đối tượng duy nhất của lớp Service. Nếu chưa có đối tượng nào, nó sẽ tạo mới một đối tượng với textArea đã cho.
    public static Service getInstance(JTextArea textArea){
        if(instance == null){
            instance = new Service(textArea);
        }
        return instance;
    }
    // Constructor riêng tư của lớp Service để khởi tạo textArea.
    private Service(JTextArea textArea){
        this.textArea = textArea;
        serviceUser = new ServiceUser();
    }
    // Phương thức startServer để khởi tạo và bắt đầu server.
    public void startServer(){
        // Đầu tiên, nó tạo một đối tượng Configuration và thiết lập cổng lắng nghe là PORT_NUMBER.
        Configuration config = new Configuration();
        config.setPort(PORT_NUMBER);
        // Sau đó, nó tạo một đối tượng SocketIOServer với cấu hình đã cho.
        server = new SocketIOServer(config);
        // Tiếp theo, nó đăng ký một ConnectListener để xử lý sự kiện khi có client kết nối đến server và ghi log vào textArea.
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient sioc) {
                textArea.append("One client connected\n");
            }
        });
        // Nó cũng đăng ký một DataListener để xử lý sự kiện khi nhận được dữ liệu từ client với tên sự kiện là "register" và dữ liệu thuộc kiểu Model_Register.
        server.addEventListener("register", Model_Register.class, new DataListener<Model_Register>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Register t, AckRequest ar) throws Exception {
                // Khi nhận được dữ liệu, nó sẽ gọi phương thức register của lớp ServiceUser để đăng ký người dùng mới.
                Model_Message message = serviceUser.register(t);
                // Kết quả đăng ký sẽ được gửi lại cho client thông qua ar.sendAckData.
                ar.sendAckData(message.isAction(), message.getMessage(), message.getData());
                if(message.isAction()){
                    textArea.append("User Register: " + t.getUserName() + "    Pass: " + t.getPassword() + "\n");
                    server.getBroadcastOperations().sendEvent("list_user", (Model_User_Account) message.getData());
                    
                }
            }
        });
        
        server.addEventListener("list_user", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient sioc, Integer userID, AckRequest ar) throws Exception {
                try {
                    List<Model_User_Account> list = serviceUser.getUser(userID);
                    sioc.sendEvent("list_user", list.toArray());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        // Sau đó, server sẽ bắt đầu chạy bằng cách gọi phương thức start() và ghi log vào textArea.
        server.start();
        textArea.append("Server has start on port: "+PORT_NUMBER+"\n");
    }
}
