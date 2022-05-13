package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.service.GameHolderService;
import com.example.application.service.Utils;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

import java.util.Collection;
import java.util.List;

@Route("mobile/multiplayer")
public class MobileMultiPlayerView extends AppLayout implements HasUrlParameter<String> {

    private final GameHolderService gameHolderService;
    private final GameContentView gameContentView;
    private GameHolder gameHolder;
    private Button startGameButton;

    public MobileMultiPlayerView(GameHolderService gameHolderService,
                                 GameContentView gameContentView) {
        this.gameHolderService = gameHolderService;
        this.gameContentView = gameContentView;
        this.gameContentView.setMobile(true);

        addToNavbar(createHeader());
        setContent(gameContentView);
        Utils.applyCenterComponentAlignment(gameContentView);
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.getThemeList().add("dark");
        header.setPadding(true);
        header.setSpacing(true);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setId("header");

        Button plusScaleButton = new Button(new Icon(VaadinIcon.PLUS_CIRCLE), e -> {
            gameContentView.plusScale();
            gameContentView.renderView(gameHolder, true);
        });
        Button minusScaleButton = new Button(new Icon(VaadinIcon.MINUS_CIRCLE), e -> {
            gameContentView.minusScale();
            gameContentView.renderView(gameHolder, true);
        });
        startGameButton = new Button("start multiplayer game");
        startGameButton.setVisible(false);
        header.add(startGameButton, minusScaleButton, plusScaleButton);
        return header;
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
                    Collection<GameContentView> views = gameHolder.getViews();
                    Collection<Tetris> players = gameHolder.getPlayers();
                    for (Tetris tetris : players) {
                        for (GameContentView view : views) {
                            tetris.addObserver(view::renderTetrisView);
                        }
                    }
                    players.forEach(Tetris::start);
                    startGameButton.setVisible(false);
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
        Tetris game = gameHolder.getGame(sessionId);
        if (game != null) {
            gameHolder.addView(sessionId, gameContentView);
            gameContentView.addKeyListeners();
        } else {
            if (gameHolder.isStarted()) {
                throw new IllegalStateException("game is already started");
            }
            List<String> colors = List.of("BLACK", "RED", "ORANGE", "BLUE", "GREEN", "MAGENTA", "PINK");
            Select<String> select = new Select<>();
            select.setLabel("Color");
            select.setItems(colors);
            select.setEmptySelectionAllowed(false);
            select.setValue(colors.get(0));
            select.setRenderer(new ComponentRenderer<>(color -> {
                FlexLayout wrapper = new FlexLayout();
                wrapper.setAlignItems(FlexComponent.Alignment.CENTER);

                Div info = new Div();
                info.getStyle().set("background", color);
                info.getStyle().set("width", HasSize.getCssSize(100, Unit.PIXELS));
                info.getStyle().set("height", HasSize.getCssSize(10, Unit.PIXELS));

                wrapper.add(info);
                return wrapper;
            }));


            TextField textField = new TextField("min3 max9 symbols");
            textField.setLabel("Name");
            textField.setMaxLength(9);
            textField.setMinLength(3);
            textField.setAutofocus(true);
            VerticalLayout verticalLayout = new VerticalLayout(textField, select);
            Dialog dialog = new Dialog();
            dialog.add(verticalLayout);
            Button ok = new Button("Ok", e -> {
                if (textField.isInvalid()) return;
                if (gameHolder.playerAlreadyExist(textField.getValue())) {
                    Notification.show("User name already exist", 5000, Notification.Position.BOTTOM_END);
                    return;
                }
                dialog.close();
                addPlayer(gameHolder, sessionId, textField.getValue(), select.getValue());
            });
            ok.addClickShortcut(Key.ENTER);
            dialog.add(new HorizontalLayout(
                    ok,
                    new Button("Cancel", e -> {
                        dialog.close();
                        UI.getCurrent().navigate("");
                    }))
            );
            dialog.open();
        }
    }

    private void addPlayer(GameHolder gameHolder, String sessionId, String name, String color) {
        gameHolder.addPlayer(sessionId, name, color);
        gameHolder.addView(sessionId, gameContentView);
        gameContentView.addKeyListeners();
        gameHolder.newPlayerBroadcastAll(gameHolder);//render view
    }
}
