package model;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Model_File_Sender {
    private File file;
    private RandomAccessFile accessFile;
    private Model_File data;
    private long fileSize;

    public Model_File_Sender() {
    }

    public Model_File_Sender(File file, Model_File data) throws IOException {
        this.file = file;
        this.accessFile = new RandomAccessFile(file, "r");
        this.data = data;
        this.fileSize = accessFile.length();
    }
    
    // Đọc dữ liệu từ file, trả về mảng dữ liệu
    public synchronized byte[] readFile(long currentLength) throws IOException {
        // Dùng synchronized để đổng bộ, 1 thời điểm chỉ cho 1 luồng đọc dữ liệu từ file
        accessFile.seek(currentLength); // Trỏ tới vị trí hiện tại để ghi thêm
        if (currentLength != fileSize) { // Nếu con trỏ file không trỏ tới cuối file
            int max = 2000; // Số byte max mỗi lần truyền
            long length = currentLength + max >= fileSize ? fileSize - currentLength : max;
            byte[] data = new byte[(int) length];
            accessFile.read(data);  // Đọc dữ liệu file vào mảng data
            return data;
        } else {
            return null;
        }
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

    public Model_File getData() {
        return data;
    }

    public void setData(Model_File data) {
        this.data = data;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    
}
