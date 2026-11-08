package Services;

import java.util.Random;

public class OtpService {
    public static String generateOtp() {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000); // 6 digits
        return String.valueOf(otp);
    }
    
    public static void sendOtp(String email, String otp) {
        // Mock sending email to console/logs
        System.out.println("=========================================");
        System.out.println("MOCK EMAIL SENT TO: " + email);
        System.out.println("YOUR OTP CODE IS: " + otp);
        System.out.println("=========================================");
    }
}
