package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.views.about.TView;
import com.vaadin.flow.component.DetachNotifier;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ru.rzn.gmyasoedov.tetris.core.Tetris;
import ru.rzn.gmyasoedov.tetris.core.TetrisState;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringComponent
@UIScope
public class MultiPlayerContentView extends VerticalLayout implements KeyNotifier, DetachNotifier {

    private volatile Map<String, TView> tetrisViewByPlayer = Collections.emptyMap();
    private Tetris tetris;

    public void addKeyListiners() {
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.toLeft();
        }, Key.ARROW_LEFT);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.toRight();
        }, Key.ARROW_RIGHT);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.rotate();
        }, Key.ARROW_UP);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.fastSpeed();
        }, Key.ARROW_DOWN);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.rotate();
        }, Key.KEY_Z);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.rotateReverse();
        }, Key.KEY_X);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.fastSpeed();
        }, Key.KEY_A);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.normalSpeed();
        }, Key.KEY_S);
        UI.getCurrent().addShortcutListener(e -> {
            if (tetris != null) tetris.pauseOrResume();
        }, Key.KEY_P);
    }

    public void renderView(GameHolder gameHolder) {
        getUI().ifPresent(ui -> ui.access(() -> {
            String sessionId = ui.getSession().getSession().getId();
            removeAll();
            if (gameHolder == null) {
                return;
            }
            tetris = gameHolder.getGame(sessionId);
            if (tetris == null) {
                throw new IllegalStateException("no game " + sessionId);
            }
            HorizontalLayout layout = new HorizontalLayout();
            Map<String, TView> viewMap = new HashMap<>();
            gameHolder.getPlayers().forEach(tetris -> {
                TView tetrisView = new TView(30, tetris.getId());
                viewMap.put(tetris.getId(), tetrisView);
                layout.add(tetrisView);
            });
            tetrisViewByPlayer = viewMap;
            add(layout);

        }));
    }

    void renderTetrisView(TetrisState state) {
        TView tView = tetrisViewByPlayer.get(state.getGameId());
        if (tView != null) {
            tView.observer(state);
        }
    }

}