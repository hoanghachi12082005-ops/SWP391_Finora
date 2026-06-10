package       util.system;

public final class LogUtil {
    private LogUtil() {}

    public static String action(String module, String action) {
        return module + ":" + action;
    }
}
