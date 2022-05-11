package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.service.GameHolderService;
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

@Route("multiplayer")
public class MultiPlayerView extends AppLayout implements HasUrlParameter<String> {

    private final GameHolderService gameHolderService;
    private final MultiPlayerContentView multiPlayerContentView;
    private GameHolder gameHolder;
    private Button startGame;
    private TextField textField;
    private long lastFocusTextFieldUpdate = 0;

    public MultiPlayerView(GameHolderService gameHolderService,
                           MultiPlayerContentView multiPlayerContentView) {
        this.gameHolderService = gameHolderService;
        this.multiPlayerContentView = multiPlayerContentView;

        HorizontalLayout header = createHeader();
        addToNavbar(createTopBar(header));
        setContent(multiPlayerContentView);
        multiPlayerContentView.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER); // Put content in the middle horizontally.
        multiPlayerContentView.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.CENTER); // Put content in the middle vertically.
    }

    private HorizontalLayout createHeader() {
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
        startGame = new Button("start game");
        startGame.setVisible(false);
        layout.add(header, new HorizontalLayout(startGame));
        return layout;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        multiPlayerContentView.renderView(gameHolder);
        textField.addKeyDownListener(Key.ARROW_DOWN, e -> multiPlayerContentView.fastSpeed());
        textField.addKeyUpListener(Key.ARROW_DOWN, e -> multiPlayerContentView.normalSpeed());
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
                startGame.setVisible(true);
                startGame.addClickListener(e -> {
                    gameHolder.setStarted(true);
                    Collection<MultiPlayerContentView> views = gameHolder.getViews();
                    Collection<Tetris> players = gameHolder.getPlayers();
                    for (Tetris tetris : players) {
                        for (MultiPlayerContentView view : views) {
                            tetris.addObserver(view::renderTetrisView);
                        }
                    }
                    players.forEach(Tetris::start);
                    startGame.setVisible(false);
                    textField.focus();
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
            gameHolder.addView(sessionId, multiPlayerContentView);
            multiPlayerContentView.addKeyListiners();
        } else {
            if (gameHolder.isStarted()) {
                throw new IllegalStateException("game is already started");
            }
            List<String> colors = List
                    .of("BLACK", "RED", "ORANGE", "BLUE", "GREEN", "MAGENTA", "PINK");
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
            textField.setMinLength(3);
            textField.setAutofocus(true);
            VerticalLayout verticalLayout = new VerticalLayout(textField, select);
            Dialog dialog = new Dialog();
            dialog.add(verticalLayout);
            Button ok = new Button("Ok", e -> {
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
        gameHolder.addView(sessionId, multiPlayerContentView);
        multiPlayerContentView.addKeyListiners();
        gameHolder.newPlayerBroadcastAll(gameHolder);//render view
    }
}
