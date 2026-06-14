package util.finance;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtil {
    private MoneyUtil() {}

    public static String formatVnd(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(amount);
    }
}
