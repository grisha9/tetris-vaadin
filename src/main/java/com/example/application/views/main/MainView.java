package com.example.application.views.main;

import com.example.application.service.GameHolderService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.UUID;


/**
 * The main view is a top-level placeholder for other views.
 */
@Route("")
public class MainView extends AppLayout {
    private final GameHolderService gameHolderService;

    public MainView(GameHolderService gameHolderService) {
        this.gameHolderService = gameHolderService;

        HorizontalLayout header = createHeader();
        addToNavbar(createTopBar(header));
    }

    private VerticalLayout createTopBar(HorizontalLayout header) {
        VerticalLayout layout = new VerticalLayout();
        layout.getThemeList().add("dark");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        Button singleGame = new Button("single game");
        Button multiplayerGame = new Button("multiplayer game");
        Button singleMobileGame = new Button("single mobile game");
        singleGame.addClickListener(e -> {
            String id = UUID.randomUUID().toString();
            gameHolderService.createGame(id, UI.getCurrent().getSession().getSession().getId());
            UI.getCurrent().navigate("singleplayer/" + id);
        });
        multiplayerGame.addClickListener(e -> {
            String id = UUID.randomUUID().toString();
            gameHolderService.createGame(id, UI.getCurrent().getSession().getSession().getId());
            UI.getCurrent().navigate("multiplayer/" + id);
        });
        singleMobileGame.addClickListener(e -> {
            String id = UUID.randomUUID().toString();
            gameHolderService.createGame(id, UI.getCurrent().getSession().getSession().getId());
            UI.getCurrent().navigate("mobile/singleplayer/" + id);
        });
        layout.add(header, new HorizontalLayout(singleGame, multiplayerGame, singleMobileGame));
        return layout;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setId("header");
        Image logo = new Image("images/logo.png", "Tetris logo");
        logo.setId("logo");
        header.add(logo);
        header.add(new H1("Tetris"));
        return header;
    }

}


