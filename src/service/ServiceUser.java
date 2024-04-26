package service;

import connection.DatabaseConnection;
import java.sql.Connection;
import model.Model_Message;
import model.Model_Register;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Model_User_Account;

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
            /*  Đoạn này bị lỗi, nếu chỉ để PreparedStatement p = con.prepareStatement(CHECK_USER), khi đó nếu không tìm thấy thì 
            có thể nó sẽ trả về false hoặc là ném ngoại lệ, để không bị ném ngoại lêj thì ta cần đảm bảo rằng ResultSet có 
            thể di chuyển (scrollable).                     Lỗi này sửa lúc 23h20p - 25/4/24 - Cùng với Vũ Ngọc Lâm*/
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
                con.setAutoCommit(false);
                /*  Tại sao sử dụng setAutoCommit(false)?
                -Giao dịch (Transactions): Trong một giao dịch, bạn muốn thực hiện nhiều thao tác như một đơn vị công việc duy nhất. 
                Bằng cách tắt tự động cam kết, bạn có thể đảm bảo tất cả các thao tác trong giao dịch đều thành công trước khi cam kết, hoặc hủy bỏ tất cả nếu có lỗi.
                -Đảm bảo tính toàn vẹn dữ liệu: Khi thực hiện một loạt thao tác, bạn muốn tránh tình trạng dữ liệu không nhất quán. 
                Với setAutoCommit(false), bạn có thể kiểm soát cách thức thực hiện các thay đổi và chỉ cam kết khi mọi thứ đúng theo kế hoạch.
                -Khôi phục khi gặp lỗi: Trong trường hợp gặp lỗi, bạn có thể sử dụng rollback() để khôi phục cơ sở dữ liệu về trạng thái trước khi thực hiện các thay đổi.*/
                p = con.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS);
                /*PreparedStatement.RETURN_GENERATED_KEYS là một cờ (flag) chỉ định rằng sau khi thực thi câu lệnh, các khóa chính tự động tạo ra (nếu có) sẽ được trả về.
                Với tùy chọn này, khi gọi execute(), bạn có thể lấy các khóa chính tự động tạo ra bằng cách sử dụng phương thức getGeneratedKeys().*/
                p.setString(1, data.getUserName());
                p.setString(2, data.getPassword());
                p.execute();
                r = p.getGeneratedKeys();
                r.first();
                int userID = r.getInt(1);
                r.close();
                p.close();
                // Create user account
                p = con.prepareStatement(INSERT_USER_ACCOUNT);
                p.setInt(1, userID);
                p.setString(2, data.getUserName());
                p.execute();
                con.commit();
                con.setAutoCommit(true);
                // Nếu thêm thành công, message sẽ được thiết lập với action=true và thông báo "Ok".
                message.setAction(true);
                message.setMessage("Ok");
                message.setData(new Model_User_Account(data.getUserName(), userID, "", "", true));
            }
        } catch (SQLException e) {
            // Nếu có lỗi xảy ra trong quá trình thực hiện SQL, message sẽ được thiết lập với action=false và thông báo "Server Error".
            message.setAction(false);
            message.setMessage("Server Error");
            e.printStackTrace();
            try {
                if(con.getAutoCommit() == false){
                    con.rollback();
                    con.setAutoCommit(true);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        return message;
    }
    
    public List<Model_User_Account> getUser(int exitUser) throws SQLException{
        List<Model_User_Account> list = new ArrayList<>();
        PreparedStatement p = con.prepareStatement(SELECT_USER_ACCOUNT);
        p.setInt(1, exitUser);
        ResultSet r = p.executeQuery();
        while(r.next()){
            int UserID = r.getInt(1);
            String UserName = r.getString(2);
            String Gender = r.getString(3);
            String ImageString = r.getString(4);
            list.add(new Model_User_Account(UserName, UserID, Gender, ImageString, true));
        }
        r.close();
        p.close();
        return list;
    }
    // Đây là hai câu lệnh SQL được sử dụng để thêm người dùng mới và kiểm tra tên người dùng đã tồn tại hay chưa.
    private String INSERT_USER = "INSERT INTO user (UserName, `Password`) VALUES (?,?)";
    private String INSERT_USER_ACCOUNT = "INSERT INTO user_account (UserID, UserName) VALUES (?,?)";
    private String CHECK_USER = "SELECT UserID FROM user WHERE UserName =? LIMIT 1";
    private String SELECT_USER_ACCOUNT = "SELECT UserID, UserName, Gender, ImageString FROM user_account WHERE user_account.`Status` = '1' and UserID<>?";
    
    // Đối tượng Connection được sử dụng để thực hiện các câu lệnh SQL với cơ sở dữ liệu.
    private Connection con;
}
