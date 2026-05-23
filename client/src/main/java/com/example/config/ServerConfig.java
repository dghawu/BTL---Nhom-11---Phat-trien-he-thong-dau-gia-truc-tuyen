package com.example.config;

import java.io.*;
import java.util.Properties;

/**
 * ServerConfig — lưu và load cấu hình server (host, port).
 *
 * Khi dùng ngrok, mỗi server (API 8888 và Push 8889) sẽ được tunnel
 * qua 2 địa chỉ ngrok riêng biệt, ví dụ:
 *   API  host: 0.tcp.ap.ngrok.io  port: 12345
 *   Push host: 0.tcp.ap.ngrok.io  port: 67890
 *
 * Config được lưu vào file server.properties cạnh file .jar để
 * người dùng không cần nhập lại mỗi lần mở app.
 */
public class ServerConfig {

    // ── Default (fallback nếu chưa config) ──────────────────────────────
    public static final String DEFAULT_API_HOST  = "localhost";
    public static final int    DEFAULT_API_PORT  = 8888;
    public static final String DEFAULT_PUSH_HOST = "localhost";
    public static final int    DEFAULT_PUSH_PORT = 8889;

    // ── Keys trong file properties ────────────────────────────────────────
    private static final String KEY_API_HOST  = "api.host";
    private static final String KEY_API_PORT  = "api.port";
    private static final String KEY_PUSH_HOST = "push.host";
    private static final String KEY_PUSH_PORT = "push.port";

    // ── File lưu config ───────────────────────────────────────────────────
    private static final String CONFIG_FILE = "server.properties";

    // ── Giá trị runtime (có thể thay đổi qua dialog) ─────────────────────
    private static String apiHost  = DEFAULT_API_HOST;
    private static int    apiPort  = DEFAULT_API_PORT;
    private static String pushHost = DEFAULT_PUSH_HOST;
    private static int    pushPort = DEFAULT_PUSH_PORT;

    // ── Singleton init — gọi 1 lần khi app khởi động ─────────────────────
    static {
        load();
    }

    private ServerConfig() {}

    // ── Getters ───────────────────────────────────────────────────────────

    public static String getApiHost()  { return apiHost; }
    public static int    getApiPort()  { return apiPort; }
    public static String getPushHost() { return pushHost; }
    public static int    getPushPort() { return pushPort; }

    // ── Setters (dùng khi user nhập config mới) ───────────────────────────

    public static void set(String newApiHost, int newApiPort,
                           String newPushHost, int newPushPort) {
        apiHost  = newApiHost.trim();
        apiPort  = newApiPort;
        pushHost = newPushHost.trim();
        pushPort = newPushPort;
        save();
    }

    // ── Load từ file ──────────────────────────────────────────────────────

    private static void load() {
        File f = new File(CONFIG_FILE);
        if (!f.exists()) return;

        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(f)) {
            p.load(fis);
            apiHost  = p.getProperty(KEY_API_HOST,  DEFAULT_API_HOST);
            pushHost = p.getProperty(KEY_PUSH_HOST, DEFAULT_PUSH_HOST);
            apiPort  = parsePort(p.getProperty(KEY_API_PORT),  DEFAULT_API_PORT);
            pushPort = parsePort(p.getProperty(KEY_PUSH_PORT), DEFAULT_PUSH_PORT);
            System.out.println("[ServerConfig] Đã load: api=" + apiHost + ":" + apiPort
                    + "  push=" + pushHost + ":" + pushPort);
        } catch (IOException e) {
            System.err.println("[ServerConfig] Không đọc được config: " + e.getMessage());
        }
    }

    // ── Lưu vào file ──────────────────────────────────────────────────────

    public static void save() {
        Properties p = new Properties();
        p.setProperty(KEY_API_HOST,  apiHost);
        p.setProperty(KEY_API_PORT,  String.valueOf(apiPort));
        p.setProperty(KEY_PUSH_HOST, pushHost);
        p.setProperty(KEY_PUSH_PORT, String.valueOf(pushPort));

        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            p.store(fos, "Auction System - Server Configuration");
            System.out.println("[ServerConfig] Đã lưu config.");
        } catch (IOException e) {
            System.err.println("[ServerConfig] Không lưu được config: " + e.getMessage());
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private static int parsePort(String val, int defaultVal) {
        if (val == null) return defaultVal;
        try {
            int port = Integer.parseInt(val.trim());
            return (port > 0 && port < 65536) ? port : defaultVal;
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    public static boolean isLocalhost() {
        return "localhost".equals(apiHost) || "127.0.0.1".equals(apiHost);
    }
}
