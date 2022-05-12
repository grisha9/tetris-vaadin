package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.service.GameHolderService;
import com.example.application.service.Utils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

@Route("mobile/singleplayer")
public class MobileSinglePlayerView extends AppLayout implements HasUrlParameter<String> {

    private final GameHolderService gameHolderService;
    private final GameContentView gameContentView;
    private GameHolder gameHolder;
    private Button startGameButton;


    public MobileSinglePlayerView(GameHolderService gameHolderService,
                                  GameContentView gameContentView) {
        this.gameHolderService = gameHolderService;
        this.gameContentView = gameContentView;

        HorizontalLayout header = createHeader();
        addToNavbar(createTopBar(header));
        setContent(gameContentView);
        Utils.applyCenterComponentAlignment(gameContentView);
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
        header.add(new H1("Tetris single player"));
        return header;
    }

    private VerticalLayout createTopBar(HorizontalLayout header) {
        VerticalLayout layout = new VerticalLayout();
        layout.getThemeList().add("dark");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        Button plusScaleButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
            gameContentView.plusScale();
            gameContentView.renderView(gameHolder, true);
        });
        Button minusScaleButton = new Button(new Icon(VaadinIcon.MINUS_CIRCLE), e -> {
            gameContentView.minusScale();
            gameContentView.renderView(gameHolder, true);
        });
        startGameButton = new Button("start game");
        layout.add(header, new HorizontalLayout(startGameButton, minusScaleButton, plusScaleButton));
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        gameContentView.renderView(gameHolder, true);

        getUI().ifPresent(ui -> ui.access(() -> {
            String sessionId = ui.getSession().getSession().getId();
            if (gameHolder.getOwnerSessionId().equals(sessionId)) {
                startGameButton.setVisible(true);
                startGameButton.addClickListener(e -> {
                    gameHolder.setStarted(true);
                    gameHolder.getPlayers().forEach(Tetris::start);
                    startGameButton.setVisible(false);
                });
            }
        }));
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        String sessionId = VaadinSession.getCurrent().getSession().getId();
        var game = gameHolder.getGame(sessionId);
        if (game != null) {
            game.removeObserver(gameContentView::renderTetrisView);
        }
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, String id) {
        GameHolder gameHolder = gameHolderService.getById(id);
        if (gameHolder == null) {
            throw new IllegalStateException("game not found");
        }
        this.gameHolder = gameHolder;
        String sessionId = VaadinSession.getCurrent().getSession().getId();
        Tetris game = gameHolder.getGame(sessionId);
        if (game == null) {
            gameHolder.addPlayer(sessionId, "", "black");
            game = gameHolder.getGame(sessionId);
        }

        gameHolder.addView(sessionId, gameContentView);
        game.addObserver(gameContentView::renderTetrisView);
        gameContentView.addKeyListeners();
    }

}
