
package util.security;

public class BCryptTest {

    public static void main(String[] args) {

        String hash =
                "$2a$12$7Eb6herMKYsRqkTQLkay4.IL8ff3m69ob3wPMExKjrfKsrHzP16si";

        System.out.println(
                PasswordUtil.verify("123456", hash));

    }
}
