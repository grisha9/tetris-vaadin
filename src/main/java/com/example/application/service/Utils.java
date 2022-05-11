package com.example.application.service;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;

public abstract class Utils {
    public static final int CELL_SIZE_PIXELS = 25;
    private Utils() {
    }

    public static boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }
}
