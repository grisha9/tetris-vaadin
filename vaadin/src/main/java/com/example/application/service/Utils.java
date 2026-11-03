package com.example.application.service;

import com.example.application.views.main.GameContentView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WebBrowser;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

public abstract class Utils {
    public static final int CELL_SIZE_PIXELS = 25;
    private Utils() {
    }

    public static boolean isMobileDevice() {
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        return webBrowser.isAndroid() || webBrowser.isIPhone() || webBrowser.isWindowsPhone();
    }

    public static void applyCenterComponentAlignment(VerticalLayout verticalLayout) {
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        verticalLayout.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
    }

    public static Button createMainViewButton(GameContentView view) {
        Button toMainViewButton = new Button("To choose game");
        toMainViewButton.addClickListener(e -> {
            Tetris tetris = view != null ? view.getTetris() : null;
            if (tetris != null) {
                tetris.stop();
            }
            UI.getCurrent().navigate("/");
        });
        return toMainViewButton;
    }
}
