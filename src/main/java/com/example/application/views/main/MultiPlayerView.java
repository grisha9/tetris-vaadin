package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.service.GameHolderService;
import com.example.application.service.Utils;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
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
import ru.rzn.gmyasoedov.tetris.core.TetrisSettings;

import java.util.Collection;
import java.util.List;

@Route("multiplayer")
public class MultiPlayerView extends AppLayout implements HasUrlParameter<String> {

    private final GameHolderService gameHolderService;
    private final GameContentView gameContentView;
    private final Button settingsButton = new Button(new Icon(VaadinIcon.COG));
    private final Button startGameButton = new Button("start game");
    private GameHolder gameHolder;
    private TextField textField;
    private long lastFocusTextFieldUpdate = 0;

    public MultiPlayerView(GameHolderService gameHolderService,
                           GameContentView gameContentView) {
        this.gameHolderService = gameHolderService;
        this.gameContentView = gameContentView;

        HorizontalLayout header = createHeader();
        addToNavbar(createTopBar(header));
        setContent(gameContentView);
        Utils.applyCenterComponentAlignment(gameContentView);
    }

    private HorizontalLayout createHeader() {
        System.out.println("mpv!!!");
        HorizontalLayout header = new HorizontalLayout();
        header.getThemeList().add("dark");
        header.setPadding(false);
        header.setSpacing(false);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setId("header");

        textField = new TextField();
        textField.setAutofocus(true);
        textField.setWidth(1, Unit.MM);

        Image logo = new Image("images/logo.png", "Tetris logo");
        logo.setId("logo");
        header.add(textField);
        header.add(logo);
        H1 h1 = new H1("Tetris multi player");
        h1.addClickListener(e -> UI.getCurrent().navigate("/"));
        header.add(h1);
        return header;
    }

    private VerticalLayout createTopBar(HorizontalLayout header) {
        VerticalLayout layout = new VerticalLayout();
        layout.getThemeList().add("dark");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        startGameButton.setVisible(false);
        settingsButton.setVisible(false);
        layout.add(header, new HorizontalLayout(startGameButton, settingsButton));
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        gameContentView.renderView(gameHolder);
        textField.addKeyDownListener(Key.ARROW_DOWN, e -> gameContentView.fastSpeed());
        textField.addKeyUpListener(Key.ARROW_DOWN, e -> gameContentView.normalSpeed());
        UI.getCurrent().addShortcutListener(e -> {
            long currentTimeMillis = System.currentTimeMillis();
            if (lastFocusTextFieldUpdate == 0 || Math.abs(currentTimeMillis - lastFocusTextFieldUpdate) > 3000) {
                textField.focus();
                lastFocusTextFieldUpdate = currentTimeMillis;
            }
        }, Key.ARROW_DOWN);
        if (gameHolder.isStarted()) {
            //todo rework binding observer
            /*Collection<Tetris> players = gameHolder.getPlayers();
            for (Tetris tetris : players) {
                for (MultiPlayerContentView view : views) {
                    tetris.addObserver(view::renderTetrisView);
                }
            }
            return;*/
        }
        getUI().ifPresent(ui -> ui.access(() -> {
            String sessionId = ui.getSession().getSession().getId();
            if (gameHolder.getOwnerSessionId().equals(sessionId)) {
                startGameButton.setVisible(true);
                settingsButton.setVisible(true);
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
                    settingsButton.setVisible(false);
                    textField.focus();
                });

                settingsButton.addClickListener(e -> {
                    Dialog dialog = new Dialog(new SettingsView(gameHolder.getSettings()));
                    Button ok = new Button("Ok", e1 -> dialog.close());
                    ok.addClickShortcut(Key.ENTER);
                    dialog.add(new HorizontalLayout(ok, new Button("Cancel", e2 -> dialog.close())));
                    dialog.open();
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


            TextField textField = new TextField();
            textField.setLabel("Name");
            textField.setMaxLength(9);
            textField.setMinLength(2);
            textField.setErrorMessage("min: " + textField.getMinLength() + " - max: " + textField.getMaxLength());
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
