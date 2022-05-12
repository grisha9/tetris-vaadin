package com.example.application.views.main;

import com.example.application.service.GameHolder;
import com.example.application.service.Utils;
import com.example.application.views.tertis.TetrisView;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
public class GameContentView extends VerticalLayout implements KeyNotifier, DetachNotifier {

    private volatile Map<String, TetrisView> tetrisViewByPlayer = Collections.emptyMap();
    private Tetris tetris;
    private VerticalLayout softButtonLayout;
    private int cellSizePixels = Utils.CELL_SIZE_PIXELS;

    public void addKeyListeners() {
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
        UI.getCurrent().addShortcutListener(e -> {
            softButtonLayout.setVisible(!softButtonLayout.isVisible());
        }, Key.KEY_V);
    }

    public void renderView(GameHolder gameHolder) {
        renderView(gameHolder, false);
    }

    public void renderView(GameHolder gameHolder, boolean showSoftButton) {
        //todo auto sacale
        /*UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {
            int screenWidth = receiver.getScreenWidth();
            // do something with screen width
        });*/
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

            Map<String, TetrisView> viewMap = new HashMap<>();
            List<TetrisView> tetrisViews = new ArrayList<>(gameHolder.getPlayers().size());
            gameHolder.getPlayers().forEach(tetris -> {
                TetrisView tetrisView = new TetrisView(cellSizePixels, tetris.getId(),
                        gameHolder.getGameColor(tetris.getId()));
                tetrisViews.add(tetrisView);
                viewMap.put(tetris.getId(), tetrisView);
            });
            tetrisViewByPlayer = viewMap;
            add(getLayout(tetrisViews));
            if (showSoftButton) softButtonLayout.setVisible(true);
        }));
    }

    public void minusScale() {
        if ((cellSizePixels - 3) > 0) {
            cellSizePixels -= 3;
        }
    }

    public void plusScale() {
        cellSizePixels += 3;
    }

    private Component getLayout(List<TetrisView> tetrisViews) {
        VerticalLayout verticalLayout = getTetrisGameLayout(tetrisViews);
        addSoftButtons(verticalLayout, getLineSize(tetrisViews.size()));
        Utils.applyCenterComponentAlignment(verticalLayout);
        return new HorizontalLayout(verticalLayout);
    }

    private void addSoftButtons(VerticalLayout verticalLayout, int lineSize) {
        Button buttonLeft = new Button(new Icon(VaadinIcon.CHEVRON_CIRCLE_LEFT));
        Button buttonRight = new Button(new Icon(VaadinIcon.CHEVRON_CIRCLE_RIGHT));
        Button buttonDown = new Button(new Icon(VaadinIcon.CHEVRON_CIRCLE_DOWN));
        Button buttonRotate = new Button(new Icon(VaadinIcon.REFRESH));
        setButtonSize(buttonLeft, lineSize);
        setButtonSize(buttonRight, lineSize);
        setButtonSize(buttonDown, lineSize);
        setButtonSize(buttonRotate, lineSize);
        buttonLeft.addClickListener(event -> {
            if (tetris != null) tetris.toLeft();
        });
        buttonRight.addClickListener(event -> {
            if (tetris != null) tetris.toRight();
        });
        buttonDown.addClickListener(event -> {
            if (tetris != null) tetris.fastOrNormalSpeed();
        });
        buttonRotate.addClickListener(event -> {
            if (tetris != null) tetris.rotate();
        });
        HorizontalLayout firstRowButton = new HorizontalLayout(buttonLeft, buttonRight);
        HorizontalLayout secondRowButton = new HorizontalLayout(buttonDown, buttonRotate);
        softButtonLayout = new VerticalLayout(firstRowButton, secondRowButton);
        softButtonLayout.setVisible(Utils.isMobileDevice());
        Utils.applyCenterComponentAlignment(softButtonLayout);
        verticalLayout.add(softButtonLayout);
    }

    private void setButtonSize(Button button, int lineSize) {
        button.setWidth(7 * cellSizePixels * lineSize, Unit.PIXELS);
        button.setHeight(2 * cellSizePixels, Unit.PIXELS);
    }

    void renderTetrisView(TetrisState state) {
        TetrisView tetrisView = tetrisViewByPlayer.get(state.getGameId());
        if (tetrisView != null) {
            tetrisView.observer(state);
        }
    }

    private VerticalLayout getTetrisGameLayout(List<TetrisView> tetrisViews) {
        if (tetrisViews.size() < (MAX_PLAYER_LIMIT / 2)) {
            HorizontalLayout layout = new HorizontalLayout();
            tetrisViews.forEach(layout::add);
            return new VerticalLayout(layout);
        }

        HorizontalLayout layout1 = new HorizontalLayout();
        HorizontalLayout layout2 = new HorizontalLayout();
        VerticalLayout verticalLayout = new VerticalLayout(layout1, layout2);
        int lineSize = getLineSize(tetrisViews.size());
        for (int i = 0; i < tetrisViews.size(); i++) {
            if (i < lineSize) {
                layout1.add(tetrisViews.get(i));
            } else {
                layout2.add(tetrisViews.get(i));
            }
        }
        return verticalLayout;
    }

    private int getLineSize(int tetrisCount) {
        return (int) Math.ceil(tetrisCount / 2.0);
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