package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import connection.DatabaseConnection;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import model.Model_File;
import java.sql.ResultSet;
import javax.imageio.ImageIO;
import model.Model_File_Receive;
import model.Model_Package_Sender;
import model.Model_Receive_Image;
import model.Model_Send_Message;
import swing.BlurHash.BlurHash;

public class ServiceFile {

    public ServiceFile() {
        this.con = DatabaseConnection.getInstance().getConnection();
        this.fileReceivers = new HashMap<>();
    }

    public Model_File addFileReceiver(String fileExtension) throws SQLException {
        Model_File data;
        // Chuẩn bị đối tượng PreparedStatement để thực thi câu lệnh INSERT và trả về khóa tự động
        PreparedStatement p = con.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS);
        p.setString(1, fileExtension);  // Thay fileExtension vào trong câu lệnh SQL
        p.execute();    // Thực thi câu lệnh
        ResultSet r = p.getGeneratedKeys(); // ResultSet chứa dữ liệu truy vấn được
        r.first();  // Di chuyển con trỏ ResultSet tới bản ghi đầu tiên
        int fileID = r.getInt(1);   // Lấy giá trị từ khóa tự động sinh của cột đầu tiên
        data = new Model_File(fileID, fileExtension);
        r.close(); 
        p.close();
        return data;
    }
    // Cập nhật Status, BlurHash trong TABLE files 
    public void updateBlurHashDone(int fileID, String blurhash) throws SQLException {
        PreparedStatement p = con.prepareStatement(UPDATE_BLUR_HASH_DONE);
        p.setString(1, blurhash);
        p.setInt(2, fileID);
        p.execute();
        p.close();
    }
    // Update Status tại bản ghi có fileID = fileID truyền vào
    public void updateDone(int fileID) throws SQLException {
        PreparedStatement p = con.prepareStatement(UPDATE_DONE);
        p.setInt(1, fileID);
        p.execute();
        p.close();
    }
    // Thêm một cặp khóa giá trị vào HashMap
    public void initFile(Model_File file, Model_Send_Message message) throws IOException {
        fileReceivers.put(file.getFileID(), new Model_File_Receive(message, toFileObject(file)));
    }
    
    public void receiveFile(Model_Package_Sender dataPackage) throws IOException {
        if (!dataPackage.isFinish()) {  // Nếu chưa truyền xong thì truyền tiếp
            fileReceivers.get(dataPackage.getFileID()).writeFile(dataPackage.getData());
        } else {    // Nếu truyền xong rồi thì đóng RandomAccessFile
            fileReceivers.get(dataPackage.getFileID()).close();
        }
    }

    public Model_Send_Message closeFile(Model_Receive_Image dataImage) throws IOException, SQLException {
        Model_File_Receive file = fileReceivers.get(dataImage.getFileID());
        if (file.getMessage().getMessageType() == 3) {  // Ảnh
            file.getMessage().setText(""); 
            String blurhash = convertFileToBlurHash(file.getFile(), dataImage); // chuyển đổi ảnh sang ảnh mờ
            updateBlurHashDone(dataImage.getFileID(), blurhash);    // Cập nhật Status trong files
        } else {
            updateDone(dataImage.getFileID());  // Cập nhật Status trong files
        }
        fileReceivers.remove(dataImage.getFileID());    // Xóa khỏi Map vì đã gửi xong
        //  Trả về message để gửi tới client đích sau khi server nhận được ảnh từ client nguồn
        return file.getMessage();
    }
    // Chuyển ảnh sang BlurHash
    private String convertFileToBlurHash(File file, Model_Receive_Image dataImage) throws IOException {
        // BufferedImage là một lớp trong Java được sử dụng để lưu trữ hình ảnh dưới dạng bộ nhớ đệm (buffered), nghĩa là hình ảnh được lưu trữ trong bộ nhớ RAM.
        // Cần sử dụng BufferedImage để sử dụng BlurHash
        BufferedImage img = ImageIO.read(file);
        // getAutoSize() là phương thức chuyển đổi kích thước của ảnh, chuyển từ kích thước ảnh gốc tới kích thước mới bị giới hạn
        Dimension size = getAutoSize(new Dimension(img.getWidth(), img.getHeight()), new Dimension(200, 200));
        //  Chuyển ảnh sang kích cỡ mới
        BufferedImage newImage = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
        // TYPE_INT_ARGB để xác định loại hình ảnh được tạo, 32 bit, bao gồm cả bit A ( độ trong suốt )
        Graphics2D g2 = newImage.createGraphics();
        g2.drawImage(img, 0, 0, size.width, size.height, null);
        String blurhash = BlurHash.encode(newImage);    // Làm mờ ảnh bằng BlurHash
        dataImage.setWidth(size.width);
        dataImage.setHeight(size.height);
        dataImage.setImage(blurhash);   // Set ảnh mới là ảnh mờ
        return blurhash;
    }
    // getAutoSize() là phương thức chuyển đổi kích thước của ảnh, chuyển từ kích thước ảnh gốc tới kích thước mới bị giới hạn
    private Dimension getAutoSize(Dimension fromSize, Dimension toSize) {
        int w = toSize.width;
        int h = toSize.height;
        int iw = fromSize.width;
        int ih = fromSize.height;
        double xScale = (double) w / iw;
        double yScale = (double) h / ih;
        double scale = Math.min(xScale, yScale);
        int width = (int) (scale * iw);
        int height = (int) (scale * ih);
        return new Dimension(width, height);
    }
    // Chuyển từ Model_File sang một File từ fileID và fileExtension của Model_File
    private File toFileObject(Model_File file) {
        return new File(PATH_FILE + file.getFileID() + file.getFileExtension());
    }

    //  SQL
    private final String PATH_FILE = "server_data/";    // Đích để lưu các file gửi từ client tới server
    private final String INSERT = "INSERT INTO files (FileExtension) VALUES (?)";   // Lưu phần mở rộng file vào database
    // Update giá trị cột BlurHash và thiết lập Status = 1 tại bản ghi có FileID bằng giá trị nhập vào, chỉ lấy 1 bản ghi
    private final String UPDATE_BLUR_HASH_DONE = "UPDATE files SET BlurHash=?, `Status`='1' WHERE FileID=? LIMIT 1"; 
    // Update giá trị Status = 1 tại bản ghi có FileID bằng giá trị truyền vào, chỉ lấy 1 bản ghi
    private final String UPDATE_DONE = "UPDATE files SET `Status`='1' WHERE FileID=? LIMIT 1";
    //  Instance
    private final Connection con;   
    private final Map<Integer, Model_File_Receive> fileReceivers;
}

