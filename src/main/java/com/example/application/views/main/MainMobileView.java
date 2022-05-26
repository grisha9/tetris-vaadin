package com.example.application.views.main;

import com.example.application.service.GameHolderService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H6;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.UUID;


@Route("mobile")
public class MainMobileView extends AppLayout {
    private final GameHolderService gameHolderService;

    public MainMobileView(GameHolderService gameHolderService) {
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
        singleGame.addClickListener(e -> {
            String id = UUID.randomUUID().toString();
            gameHolderService.createGame(id, UI.getCurrent().getSession().getSession().getId());
            UI.getCurrent().navigate("mobile/singleplayer/" + id);
        });
        
        multiplayerGame.addClickListener(e -> {
            String id = UUID.randomUUID().toString();
            gameHolderService.createGame(id, UI.getCurrent().getSession().getSession().getId());
            UI.getCurrent().navigate("mobile/multiplayer/" + id);
        });

        layout.add(header, new HorizontalLayout(singleGame, multiplayerGame));
        return layout;
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setId("header");

        H6 h6 = new H6("Tetris (mobile)");
        h6.getStyle().set("padding-left", "1%");

        header.add(h6);
        return header;
    }

}


