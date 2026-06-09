package com.storemanagement.util.website;

public final class SeoUtil {
    private SeoUtil() {}

    public static String slugify(String input) {
        if (input == null) return "";
        return input.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
    }
}
