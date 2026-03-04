package com.oceanview.resort;

import com.oceanview.network.OceanViewServer;
import javafx.application.Application;

public class AppLauncher {
    public static void main(String[] args) {
        // 1. අර ලස්සනම Web Server එක (OceanViewServer) background එකේ පණ ගැන්වීම
        new Thread(() -> {
            try {
                OceanViewServer.main(new String[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // 2. JavaFX UI (Main class) එක පණ ගැන්වීම
        Application.launch(Main.class, args);
    }
}