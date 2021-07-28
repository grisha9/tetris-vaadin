package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.service.GameHolderService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

import java.util.Collection;

@Route("multiplayer")
public class MultiPlayerView extends AppLayout implements HasUrlParameter<String> {

    private final GameHolderService gameHolderService;
    private final MultiPlayerContentView multiPlayerContentView;
    private GameHolder gameHolder;//todo volatile or modify in single UI thread? (i am think single thread)
    private Button startGame;

    public MultiPlayerView(GameHolderService gameHolderService,
                           MultiPlayerContentView multiPlayerContentView) {
        this.gameHolderService = gameHolderService;
        this.multiPlayerContentView = multiPlayerContentView;

        HorizontalLayout header = createHeader();
        addToNavbar(createTopBar(header));
        setContent(multiPlayerContentView);
        multiPlayerContentView.setJustifyContentMode ( FlexComponent.JustifyContentMode.CENTER ); // Put content in the middle horizontally.
        multiPlayerContentView.setDefaultHorizontalComponentAlignment( FlexComponent.Alignment.CENTER ); // Put content in the middle vertically.
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
        startGame = new Button("start game");
        startGame.setVisible(false);
        layout.add(header, new HorizontalLayout(startGame));
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        multiPlayerContentView.renderView(gameHolder);
        getUI().ifPresent(ui -> ui.access(() -> {
            String sessionId = ui.getSession().getSession().getId();
            if (gameHolder.getOwnerSessionId().equals(sessionId)) {
                startGame.setVisible(true);
                startGame.addClickListener(e -> {
                    Collection<MultiPlayerContentView> views = gameHolder.getViews();
                    Collection<Tetris> players = gameHolder.getPlayers();
                    for (Tetris tetris : players) {
                        for (MultiPlayerContentView view : views) {
                            tetris.addObserver(view::renderTetrisView);
                        }
                    }
                    players.forEach(Tetris::start);
                    startGame.setVisible(false);
                });
            }
        }));
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
            TextField textField = new TextField();
            textField.setMaxLength(9);
            textField.setMinLength(3);
            textField.setAutofocus(true);
            HorizontalLayout layout = new HorizontalLayout(new Text("Name"), textField);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            Dialog dialog = new Dialog();
            dialog.add(layout);
            dialog.add(new HorizontalLayout(
                    new Button("Ok", e -> {
                        dialog.close();
                        addPlayer(gameHolder, sessionId, textField.getValue());
                    }),
                    new Button("Cancel", e -> {
                        dialog.close();
                        UI.getCurrent().navigate("");
                    }))
            );
            dialog.open();
        }
    }

    private void addPlayer(GameHolder gameHolder, String sessionId, String name) {
        gameHolder.addPlayer(sessionId, name);
        gameHolder.addView(sessionId, multiPlayerContentView);
        multiPlayerContentView.addKeyListiners();
        gameHolder.newPlayerBroadcastAll(gameHolder);//render view
    }
}
