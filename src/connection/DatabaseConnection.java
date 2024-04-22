package connection;

import java.sql.Connection;
import java.sql.SQLException;

/*
    Thiết kế theo mô hình Singleton để quản lý kết nối tới một cơ sở dữ liệu MySQL.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    // Mô hình Singleton đảm bảo chỉ có một instance (bản sao) duy nhất của một lớp trong suốt vòng đời của ứng dụng.
    // Phương thức getInstance() được sử dụng để lấy instance duy nhất của DatabaseConnection. 
    // Nếu instance chưa được tạo, nó sẽ tạo một instance mới và trả về instance này. Nếu đã có instance, nó sẽ trả về instance hiện tại.
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    // Constructor của lớp DatabaseConnection được đánh dấu là private, điều này ngăn chặn việc tạo instance từ bên ngoài lớp.
    // Điều này đảm bảo rằng chỉ có thể tạo instance thông qua phương thức getInstance(), giúp duy trì mô hình Singleton.
    private DatabaseConnection() {
    }
    // Phương thức này chịu trách nhiệm thiết lập kết nối với cơ sở dữ liệu MySQL.
    // Nó sử dụng các thông số như server, port, database, userName, và password để tạo một chuỗi kết nối (Connection String).
    // Sau đó, nó sử dụng DriverManager.getConnection() để tạo kết nối và lưu vào biến connection.
    public void connectToDatabase() throws SQLException {
        String server = "localhost";
        String port = "3306";
        String database = "chatapp";
        String userName = "HoThai";
        String password = "1234";
        
        connection = java.sql.DriverManager.getConnection("jdbc:mysql://" + server + ":" + port + "/" + database , userName, password);
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

}
