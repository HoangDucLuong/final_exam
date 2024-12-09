package shop.utils;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;

public class FileUtility {
    
    // Hàm tải tệp lên và lưu vào thư mục gốc của ứng dụng
    public static String uploadFileImage(MultipartFile file, String folderName) {
        // Lấy thư mục gốc của ứng dụng
        String folderUpload = System.getProperty("user.dir") + "/" + folderName;
        String fileName = System.currentTimeMillis() + file.getOriginalFilename();
        String strpath = String.format("%s/%s", folderUpload, fileName);
        
        try {
            // Đảm bảo thư mục đích tồn tại
            Path path = Paths.get(folderUpload);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // Ghi dữ liệu tệp vào thư mục
            byte[] data = file.getBytes();
            try (FileOutputStream fout = new FileOutputStream(strpath);
                 BufferedOutputStream buf = new BufferedOutputStream(fout)) {
                buf.write(data);
                buf.flush();
            }
            
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return ""; // Xử lý lỗi nếu cần
        }
    }

    // Hàm để lấy hình ảnh ở định dạng Base64 từ thư mục gốc
    public static String getBase64EncodedImage(String folderName, String imageName) {
        // Lấy thư mục gốc của ứng dụng
        String folderUpload = System.getProperty("user.dir") + "/" + folderName;
        
        // Đọc và chuyển đổi tệp thành Base64
        try (FileInputStream fin = new FileInputStream(folderUpload + "/" + imageName)) {
            byte[] data = fin.readAllBytes();
            return java.util.Base64.getEncoder().encodeToString(data); // Trả về hình ảnh ở định dạng Base64
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // Hàm để xóa tệp trong thư mục gốc
    public static String deleteFile(String folderName, String fileName) {
        // Lấy đường dẫn của tệp cần xóa
        Path pathFile = Paths.get(System.getProperty("user.dir") + "/" + folderName + "/" + fileName);
        
        try {
            Files.delete(pathFile); // Xóa tệp
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
