package service;

import com.corundumstudio.socketio.AckRequest; // Để xử lý các yêu cầu xác nhận từ client
import com.corundumstudio.socketio.Configuration; // Cấu hình cho SocketIO server
import com.corundumstudio.socketio.SocketIOClient; // Đại diện cho một client kết nối với server
import com.corundumstudio.socketio.SocketIOServer; // Máy chủ SocketIO để quản lý kết nối và sự kiện
import com.corundumstudio.socketio.listener.ConnectListener; // Để lắng nghe sự kiện kết nối
import com.corundumstudio.socketio.listener.DataListener; // Để lắng nghe sự kiện dữ liệu
import com.corundumstudio.socketio.listener.DisconnectListener; // Để lắng nghe sự kiện ngắt kết nối
import java.io.IOException;
import java.sql.SQLException; // Xử lý ngoại lệ SQL
import java.util.ArrayList; // Tạo danh sách các phần tử
import java.util.List; // Khai báo loại List
import javax.swing.JTextArea; // Để hiển thị thông tin trên giao diện người dùng
import model.Model_Client; // Định nghĩa đối tượng Model_Client
import model.Model_File;
import model.Model_Login; // Định nghĩa đối tượng Model_Login
import model.Model_Register; // Định nghĩa đối tượng Model_Register
import model.Model_Message; // Định nghĩa đối tượng Model_Message
import model.Model_Package_Sender;
import model.Model_Receive_Image;
import model.Model_Receive_Message;
import model.Model_Send_Message;
import model.Model_User_Account; // Định nghĩa đối tượng Model_User_Account

public class Service {

    // Khai báo các biến instance để đảm bảo lớp Service chỉ có một đối tượng duy nhất (Singleton pattern).
    private static Service instance; // Biến instance của lớp Singleton Service
    private SocketIOServer server; // Đối tượng SocketIOServer quản lý kết nối và giao tiếp với các client
    private ServiceUser serviceUser; // Dịch vụ quản lý người dùng
    private ServiceFile serviceFile;
    private JTextArea textArea; // Đối tượng JTextArea để hiển thị thông tin
    private final int PORT_NUMBER = 9999; // Cổng mà máy chủ sẽ lắng nghe
    private List<Model_Client> listClient; // Danh sách các client đã kết nối

    // Phương thức getInstance trả về đối tượng duy nhất của lớp Service. Nếu chưa có đối tượng nào, nó sẽ tạo mới một đối tượng với textArea đã cho.
    public static Service getInstance(JTextArea textArea) {
        if (instance == null) { // Nếu instance chưa tồn tại, tạo mới
            instance = new Service(textArea); // Tạo đối tượng Service mới với textArea
        }
        return instance; // Trả về instance
    }

    // Constructor riêng tư của lớp Service để khởi tạo textArea, serviceUser, listClient
    private Service(JTextArea textArea) {
        this.textArea = textArea; // Lưu trữ đối tượng JTextArea
        serviceUser = new ServiceUser(); // Tạo đối tượng serviceUser mới
        serviceFile = new ServiceFile();
        listClient = new ArrayList<>(); // Khởi tạo danh sách client rỗng
    }

    // Phương thức startServer để khởi tạo và bắt đầu server.
    public void startServer() {
        // Đầu tiên, tạo đối tượng Configuration và thiết lập cổng lắng nghe là PORT_NUMBER.
        Configuration config = new Configuration(); // Cấu hình server
        config.setPort(PORT_NUMBER); // Thiết lập cổng lắng nghe
        // Sau đó, tạo đối tượng SocketIOServer với cấu hình đã cho.
        server = new SocketIOServer(config); // Tạo máy chủ SocketIO với cấu hình
        // Đăng ký một ConnectListener để xử lý sự kiện khi có client kết nối đến server và ghi log vào textArea

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient sioc) {
                textArea.append("One client connected\n"); // Ghi log khi có client kết nối
            }
        });
        
        // Đăng ký sự kiện DataListener cho sự kiện "login".
        server.addEventListener("login", Model_Login.class, new DataListener<Model_Login>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Login t, AckRequest ar) throws Exception {
                // Xử lý khi nhận dữ liệu đăng nhập từ client
                Model_User_Account login = serviceUser.login(t); // Thực hiện đăng nhập
                if (login != null) {
                    // Gửi xác nhận thành công và thông tin người dùng về client
                    ar.sendAckData(true, login); // Gửi xác nhận thành công
                    // Thêm client mới vào danh sách client từ dữ liệu nhận được
                    addClient(sioc, login); // Thêm client vào danh sách                  
                    // Thông báo khi người dùng đã kết nối thành công
                    userConnect(login.getUserID()); 
                } else {
                    // Nếu đăng nhập thất bại, gửi xác nhận không thành công
                    ar.sendAckData(false);
                }
            }
        });
        
        // Đăng ký một DataListener cho sự kiện "register".
        server.addEventListener("register", Model_Register.class, new DataListener<Model_Register>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Register t, AckRequest ar) throws Exception {
                // Xử lý khi nhận dữ liệu đăng ký từ client
                Model_Message message = serviceUser.register(t); // Đăng ký người dùng mới
                // Gửi phản hồi về kết quả đăng ký
                ar.sendAckData(message.isAction(), message.getMessage(), message.getData());
                
                if (message.isAction()) { // Nếu đăng ký thành công
                    textArea.append("User Register: " + t.getUserName() + "    Pass: " + t.getPassword() + "\n"); // Ghi log đăng ký mới
                    // Gửi sự kiện "list_user" tới tất cả các client
                    server.getBroadcastOperations().sendEvent("list_user", (Model_User_Account) message.getData());
                    // Thêm client mới vào danh sách client
                        addClient(sioc, (Model_User_Account) message.getData());
                }
            }
        });
        
        // Đăng ký sự kiện DataListener cho sự kiện "list_user".
        server.addEventListener("list_user", Integer.class, new DataListener<Integer>() {
            @Override
            public void onData(SocketIOClient sioc, Integer userID, AckRequest ar) throws Exception {
                // Trả lại danh sách các tài khoản người dùng ngoại trừ người dùng có userID đã cho
                try {
                    List<Model_User_Account> list = serviceUser.getUser(userID); // Lấy danh sách người dùng
                    // Gửi danh sách người dùng trở lại client
                    sioc.sendEvent("list_user", list.toArray());
                } catch (SQLException e) { // Xử lý ngoại lệ SQL
                    e.printStackTrace();
                }
            }
        });
        
        // Đăng ký một DisconnectListener để xử lý khi client ngắt kết nối
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient sioc) {
                // Khi client ngắt kết nối, xác định client nào và gửi sự kiện "user_status"
                int userID = removeClient(sioc); // Xóa client khỏi danh sách
                if (userID != 0) {
                    userDisconnect(userID); // Gửi sự kiện người dùng đã ngắt kết nối
                }
            }
        });
        
        // Đăng ký bộ lắng nghe sự kiện từ người dùng gửi rồi gửi tin nhắn tới người dùng đích
        server.addEventListener("send_to_user", Model_Send_Message.class, new DataListener<Model_Send_Message>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Send_Message t, AckRequest ar) throws Exception {
                // Server nhận tin nhắn từ client và gửi tới người dùng đích
                sendToClient(t, ar);
            }
        });
        // Tạo bộ lắng nghe sự kiện gửi file từ client
        server.addEventListener("send_file", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient sioc, Model_Package_Sender t, AckRequest ar) throws Exception {
                try {
                    serviceFile.receiveFile(t); // Kiểm tra đã gửi xong file chưa, nếu chưa xong thì gửi tiếp
                    if (t.isFinish()) {
                        ar.sendAckData(true);   // Xác nhận đã nhận được file
                        Model_Receive_Image dataImage = new Model_Receive_Image(); 
                        dataImage.setFileID(t.getFileID());
                        Model_Send_Message message = serviceFile.closeFile(dataImage);  // Tạo message là ảnh dạng BlurHash hoặc text 
                        //  Gửi message tới client đích
                        sendTempFileToClient(message, dataImage);
                    } else {
                        ar.sendAckData(true);
                    }
                } catch (IOException | SQLException e) {
                    ar.sendAckData(false);
                    e.printStackTrace();
                }
            }
        });
        // Sau đó, bắt đầu server
        server.start(); // Khởi động server
        textArea.append("Server has start on port: " + PORT_NUMBER + "\n"); // Ghi log khi server đã khởi động
    }

    private void userConnect(int userID) {
        // Gửi sự kiện "user_status" cho tất cả các client, xác nhận người dùng đã kết nối
        server.getBroadcastOperations().sendEvent("user_status", userID, true);
    }
    
    private void userDisconnect(int userID) {
        // Gửi sự kiện "user_status" cho tất cả các client, xác nhận người dùng đã ngắt kết nối
        server.getBroadcastOperations().sendEvent("user_status", userID, false);
    }
    
    private void addClient(SocketIOClient client, Model_User_Account user) {
        // Thêm một client mới vào danh sách client đã kết nối
        listClient.add(new Model_Client(client, user));
    }
    
    public int removeClient(SocketIOClient client) {
        // Tìm và xóa client khỏi danh sách, sau đó trả lại userID của nó
        for (Model_Client mc : listClient) {
            if (mc.getClient().equals(client)) { // So sánh đối tượng client
                listClient.remove(mc); // Xóa client khỏi danh sách
                return mc.getUser().getUserID(); // Trả lại userID của client
            }
        }
        return 0; // Nếu không tìm thấy, trả về 0
    }
    
    // Server nhận tin nhắn từ client và gửi tới người dùng đích
    public void sendToClient(Model_Send_Message data, AckRequest ar){
        if (data.getMessageType() == 3 || data.getMessageType() == 4) { // Gửi file, ảnh
            try {
                // Model_File được tạo từ fileID truy vấn từ DataBase với fileExtension truyền vào
                Model_File file = serviceFile.addFileReceiver(data.getText()); 
                serviceFile.initFile(file, data);   // Thêm cặp key_value vào HashMap
                ar.sendAckData(file.getFileID());   // Gửi xác nhận kèm theo fileID của file vừa tạo
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        } else {    // Gửi text
            for (Model_Client c : listClient) {
                if (c.getUser().getUserID() == data.getToUserID()) {    // Tìm người dùng đích và gửi text đi   
                    c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getFromUserID(), data.getText(), data.getMessageType(), null));
                    break;
                }
            }
        }
    }
    // Tìm client đích và gửi ảnh đã mã hóa tới
    private void sendTempFileToClient(Model_Send_Message data, Model_Receive_Image dataImage) {
        for (Model_Client c : listClient) {
            if (c.getUser().getUserID() == data.getToUserID()) {
                c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getFromUserID(), data.getText(), 3, dataImage));
                break;
            }
        }
    }
    
    public List<Model_Client> getListClient() {
        return listClient; // Trả về danh sách các client
    }
}
