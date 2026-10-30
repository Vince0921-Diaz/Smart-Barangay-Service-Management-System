package util;

public final class Session {
    private static String userId;
    private static String fullName;
    private static String role;

    private Session() {}

    public static void login(String id, String name, String userRole) {
        userId = id;
        fullName = name;
        role = userRole;
    }

    public static String getUserId() { return userId; }
    public static String getFullName() { return fullName; }
    public static String getRole() { return role; }

    public static boolean isLoggedIn() { return userId != null; }

    public static void clear() {
        userId = null;
        fullName = null;
        role = null;
    }
}
