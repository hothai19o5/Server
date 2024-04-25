package service;

import connection.DatabaseConnection;
import java.sql.Connection;
import model.Model_Message;
import model.Model_Register;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author admin
 */
public class ServiceUser {
    // Khởi tạo đối tượng ServiceUser và lấy đối tượng Connection từ DatabaseConnection để thực hiện các truy vấn SQL.
    public ServiceUser(){
        this.con = DatabaseConnection.getInstance().getConnection();
    }
    // Phương thức register nhận đối tượng Model_Register chứa thông tin người dùng muốn đăng ký.
    public Model_Message register(Model_Register data) {
        //  Check user exit
        Model_Message message = new Model_Message();
        try {
            // Đầu tiên, nó kiểm tra xem tên người dùng đã tồn tại hay chưa bằng cách thực hiện câu lệnh SQL CHECK_USER.
            // Đoạn này bị lỗi, nếu chỉ để PreparedStatement p = con.prepareStatement(CHECK_USER), khi đó nếu không tìm thấy thì 
            // có thể nó sẽ trả về false hoặc là ném ngoại lệ, để không bị ném ngoại lêj thì ta cần đảm bảo rằng ResultSet có 
            // thể di chuyển (scrollable)
            // Lỗi này sửa lúc 23h20p - 25/4/24 - Cùng với Vũ Ngọc Lâm
            PreparedStatement p = con.prepareStatement(CHECK_USER,
                         ResultSet.TYPE_SCROLL_INSENSITIVE, // Cho phép di chuyển mà không bị ảnh hưởng bởi thay đổi cơ sở dữ liệu
                        ResultSet.CONCUR_READ_ONLY // Chỉ cho phép đọc, không thể chỉnh sửa
                            );
            p.setString(1, data.getUserName());
            ResultSet r = p.executeQuery();
            // Nếu tên người dùng đã tồn tại, message sẽ được thiết lập với action=false và thông báo "User Already Exit".
            if (r.first()) {
                message.setAction(false);
                message.setMessage("User Already Exit");
            } else {
                message.setAction(true);
            }
            r.close();
            p.close();
            
            // Nếu tên người dùng chưa tồn tại, nó sẽ thực hiện câu lệnh SQL INSERT_USER để thêm người dùng mới vào cơ sở dữ liệu.
            if (message.isAction()) {
                //  Insert User Register
                p = con.prepareStatement(INSERT_USER);
                p.setString(1, data.getUserName());
                p.setString(2, data.getPassword());
                p.execute();
                p.close();
                // Nếu thêm thành công, message sẽ được thiết lập với action=true và thông báo "Ok".
                message.setAction(true);
                message.setMessage("Ok");
            }
        } catch (SQLException e) {
            // Nếu có lỗi xảy ra trong quá trình thực hiện SQL, message sẽ được thiết lập với action=false và thông báo "Server Error".
            message.setAction(false);
            message.setMessage("Server Error");
            e.printStackTrace();
        }
        return message;
    }
    // Đây là hai câu lệnh SQL được sử dụng để thêm người dùng mới và kiểm tra tên người dùng đã tồn tại hay chưa.
    private String INSERT_USER = "INSERT INTO user (UserName, `Password`) VALUES (?,?)";
    private String CHECK_USER = "SELECT UserID FROM user WHERE UserName =? LIMIT 1";
    
    // Đối tượng Connection được sử dụng để thực hiện các câu lệnh SQL với cơ sở dữ liệu.
    private Connection con;
}
