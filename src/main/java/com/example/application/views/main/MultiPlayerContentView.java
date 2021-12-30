package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.views.about.TView;
import com.vaadin.flow.component.Component;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.application.service.GameHolder.MAX_PLAYER_LIMIT;

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

            Map<String, TView> viewMap = new HashMap<>();
            List<TView> tetrisViews = new ArrayList<>(gameHolder.getPlayers().size());
            gameHolder.getPlayers().forEach(tetris -> {
                TView tetrisView = new TView(30, tetris.getId(), gameHolder.getGameColor(tetris.getId()));
                tetrisViews.add(tetrisView);
                viewMap.put(tetris.getId(), tetrisView);
            });
            tetrisViewByPlayer = viewMap;
            add(getLayout(tetrisViews));
        }));
    }

    private Component getLayout(List<TView> tetrisViews) {
        if (tetrisViews.size() < (MAX_PLAYER_LIMIT / 2)) {
            HorizontalLayout layout = new HorizontalLayout();
            tetrisViews.forEach(layout::add);
            return layout;
        }

        HorizontalLayout layout1 = new HorizontalLayout();
        HorizontalLayout layout2 = new HorizontalLayout();
        VerticalLayout verticalLayout = new VerticalLayout(layout1, layout2);
        int lineSize = (int) Math.ceil(tetrisViews.size() / 2.0);
        for (int i = 0; i < tetrisViews.size(); i++) {
            if (i < lineSize) {
                layout1.add(tetrisViews.get(i));
            } else {
                layout2.add(tetrisViews.get(i));
            }
        }
        return new HorizontalLayout(verticalLayout);
    }

    void renderTetrisView(TetrisState state) {
        TView tView = tetrisViewByPlayer.get(state.getGameId());
        if (tView != null) {
            tView.observer(state);
        }
    }

    void fastSpeed() {
        if (tetris != null && tetris.getState() == Tetris.State.GAME) {
            tetris.fastSpeed();
        }
    }

    void normalSpeed() {
        if (tetris != null && tetris.getState() == Tetris.State.GAME) {
            tetris.normalSpeed();
        }
    }
}