package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.service.GameHolderService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

import java.util.Collection;

@Route("singleplayer")
public class SinglePlayerView extends AppLayout implements HasUrlParameter<String> {

    private final GameHolderService gameHolderService;
    private final MultiPlayerContentView multiPlayerContentView;
    private GameHolder gameHolder;

    public SinglePlayerView(GameHolderService gameHolderService,
                            MultiPlayerContentView multiPlayerContentView) {
        this.gameHolderService = gameHolderService;
        this.multiPlayerContentView = multiPlayerContentView;

        HorizontalLayout header = createHeader();
        addToNavbar(createTopBar(header));
        setContent(multiPlayerContentView);
        multiPlayerContentView.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        multiPlayerContentView.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.getThemeList().add("dark");
        header.setPadding(false);
        header.setSpacing(false);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setId("header");
        Image logo = new Image("images/logo.png", "Tetris logo");
        logo.setId("logo");
        header.add(logo);
        header.add(new H1("Tetris multi player"));
        return header;
    }

    private VerticalLayout createTopBar(HorizontalLayout header) {
        VerticalLayout layout = new VerticalLayout();
        layout.getThemeList().add("dark");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(header);
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        multiPlayerContentView.renderView(gameHolder);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String id) {
        GameHolder gameHolder = gameHolderService.getById(id);
        if (gameHolder == null) {
            throw new IllegalStateException("game not found");
        }
        this.gameHolder = gameHolder;
        String sessionId = VaadinSession.getCurrent().getSession().getId();
        System.out.println("setParam " + Thread.currentThread());
        System.out.println("created " + sessionId);
        Tetris game = gameHolder.getGame(sessionId);
        if (game != null) {
            gameHolder.addView(sessionId, multiPlayerContentView);
            multiPlayerContentView.addKeyListiners();
        } else {
            addPlayer(gameHolder, sessionId, "game");
            Collection<MultiPlayerContentView> views = gameHolder.getViews();
            game = gameHolder.getGame(sessionId);
            for (MultiPlayerContentView view : views) {
                game.addObserver(view::renderTetrisView);
            }
            game.start();
        }
    }

    private void addPlayer(GameHolder gameHolder, String sessionId, String name) {
        gameHolder.addPlayer(sessionId, name);
        gameHolder.addView(sessionId, multiPlayerContentView);
        multiPlayerContentView.addKeyListiners();
        gameHolder.newPlayerBroadcastAll(gameHolder);
    }
}
