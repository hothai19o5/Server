package model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Model_File_Receive {
    private Model_Send_Message message;
    private File file;
    private RandomAccessFile accessFile;

    public Model_File_Receive() {
    }

    public Model_File_Receive(Model_Send_Message message, File file) throws IOException{
        this.message = message;
        this.file = file;
        // Tạo đối tượng RandomAccessFile với chế độ đọc ghi 
        this.accessFile = new RandomAccessFile(file, "rw");
    }
    // Ghi dữ liệu file vào trong một mảng byte
    public synchronized long writeFile(byte[] data) throws IOException {
        accessFile.seek(file.length()); // Di chuyển con trỏ file tới cuối file
        accessFile.write(data); // Ghi tiếp dữ liệu vào                                                                                                                                                                                                                                                 
        return accessFile.length();
    }
    
    public void close() throws IOException {
        accessFile.close();
    }

    public Model_Send_Message getMessage() {
        return message;
    }

    public void setMessage(Model_Send_Message message) {
        this.message = message;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public RandomAccessFile getAccessFile() {
        return accessFile;
    }

    public void setAccessFile(RandomAccessFile accessFile) {
        this.accessFile = accessFile;
    }
    
}
