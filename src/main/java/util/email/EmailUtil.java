package util.email;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Random;

public class EmailUtil {

    // 1. Hàm sinh mật khẩu ngẫu nhiên bảo mật cao đầy đủ ký tự
    public static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$*";
        StringBuilder sb = new StringBuilder();
        Random rd = new Random();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // 2. Hàm gửi email mật khẩu tự động
    public static boolean sendPasswordEmail(String toEmail, String employeeName, String autoPassword) {
        // Cấu hình tài khoản gửi thư hệ thống (Sử dụng App Password của Gmail)
        final String fromEmail = "hoanghachi12082005@gmail.com";
        final String appPassword = "kzud qllx uklc bfnd"; // Thay thế bằng mật khẩu ứng dụng Gmail của bạn

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2"); // Fix lỗi giao thức TLS mới của Google

        props.put("mail.smtp.connectiontimeout", "5000"); //Thời gian chờ kết nối 5s
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, appPassword);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[Finora] Thông báo cấp lại mật khẩu cho tài khoản", "UTF-8");

            String content = "<h3>Xin chào " + employeeName + ",</h3>"
                    + "<p>Tài khoản nội bộ của bạn trên hệ thống quản lý chuỗi cửa hàng Finora đã được khởi tạo thành công bởi Admin.</p>"
                    + "<p>Mật khẩu đăng nhập tạm thời của bạn là: <strong style='color:#93000b; font-size:16px;'>" + autoPassword + "</strong></p>"
                    + "<p>Vui lòng đăng nhập bằng Email/Số điện thoại cá nhân và thực hiện <strong>đổi mật khẩu ngay lập tức</strong> tại hệ thống để đảm bảo tính an toàn bảo mật.</p>"
                    + "<br><p>Trân trọng,<br>Ban quản trị Finora.</p>";

            message.setContent(content, "text/html; charset=UTF-8");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

}
