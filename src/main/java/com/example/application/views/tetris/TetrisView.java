package com.example.application.views.tetris;

import com.example.application.views.main.MainView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyDownEvent;
import com.vaadin.flow.component.KeyUpEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.pekkam.Canvas;
import org.vaadin.pekkam.CanvasRenderingContext2D;
import ru.rzn.gmyasoedov.tetris.core.Tetris;
import ru.rzn.gmyasoedov.tetris.core.TetrisState;

@Route(value = "tetris", layout = MainView.class)

@PageTitle("Tetris")
public class TetrisView extends VerticalLayout {

    private static final int CELL = 50;
    private static final int WIDTH = 50 * 10;
    private static final int HEIGHT = 50 * 20;
    private Tetris tetris;
    private Button start;
    private Button left;
    private Button right;
    private Button rotate;
    private final Canvas canvasField;
    private final Canvas canvasNext;
    private final Label scoreLabel;

    public TetrisView() {
        addClassName("hello-world-view");
        start = new Button("Start");
        left = new Button("left");
        right = new Button("right");
        rotate = new Button("rotate");

        canvasField = new Canvas(WIDTH, HEIGHT);
        canvasNext = new Canvas(CELL * 4, CELL * 4);
        scoreLabel = new Label();

        CanvasRenderingContext2D ctx = canvasField.getContext();

        //ctx.setFillStyle("green");
        ctx.strokeRect(0, 0, WIDTH, HEIGHT);

        add(start);
        add(left);
        add(right);
        add(rotate);
        add(canvasField);
        add(canvasNext);
        add(scoreLabel);

        setHorizontalComponentAlignment(Alignment.CENTER);


        start.addClickListener(e -> startTetris());
        left.addClickListener(e -> tetris.toLeft());
        right.addClickListener(e -> tetris.toRight());
        rotate.addClickListener(e -> tetris.rotate());


        UI.getCurrent().addShortcutListener(this::startTetris, Key.SPACE);
        UI.getCurrent().addShortcutListener(e -> tetris.toLeft(), Key.ARROW_LEFT);
        UI.getCurrent().addShortcutListener(e -> tetris.toRight(), Key.ARROW_RIGHT);
        UI.getCurrent().addShortcutListener(e -> tetris.rotate(), Key.ARROW_UP);
        UI.getCurrent().addShortcutListener(e -> tetris.fastSpeed(), Key.ARROW_DOWN);
        UI.getCurrent().addShortcutListener(e -> tetris.rotate(), Key.KEY_Z);
        UI.getCurrent().addShortcutListener(e -> tetris.rotateReverse(), Key.KEY_X);
        UI.getCurrent().addShortcutListener(e -> tetris.fastSpeed(), Key.KEY_A);
        UI.getCurrent().addShortcutListener(e -> tetris.normSpeed(), Key.KEY_S);

    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);

        addListener(KeyDownEvent.class, e -> {
            if (e.getKey().getKeys().equals(Key.ARROW_DOWN.getKeys())) {
                tetris.fastSpeed();
            }
        });
        addListener(KeyUpEvent.class, e -> {
            if (e.getKey().getKeys().equals(Key.ARROW_DOWN.getKeys())) {
                tetris.normSpeed();
            }
        });
    }

    private void startTetris() {
        if (tetris != null) {
            tetris.stop();
        }
        tetris = new Tetris();
        tetris.addObserver(this::observer);
        tetris.start();
    }


    private void observer(TetrisState state) {
        getUI().ifPresent(ui -> ui.access(() -> renderView(state)));
    }

    private void renderView(TetrisState state) {
        CanvasRenderingContext2D ctx = canvasField.getContext();
        //ctx.setFillStyle("blue");
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        ctx.strokeRect(0, 0, WIDTH, HEIGHT);
        int[][] field = state.getField();
        for (int i = 0; i < field.length; i++) {
            int[] row = field[i];
            for (int j = 0; j < row.length; j++) {
                if (field[i][j] > 0) {
                    drawCell(ctx, i, j);
                }
            }
        }

        ctx = canvasNext.getContext();
        ctx.clearRect(0, 0, 4 * CELL, 4 * CELL);
        field = state.getNextFigure();
        for (int i = 0; i < field.length; i++) {
            int[] row = field[i];
            for (int j = 0; j < row.length; j++) {
                if (field[i][j] > 0) {
                    drawCell(ctx, i, j);
                }
            }
        }
        scoreLabel.setText("score: " + state.getScore());
    }

    private void drawCell(CanvasRenderingContext2D ctx, int i, int j) {
        //ctx.strokeRect(j * CELL + 1, i * CELL + 1, CELL - 1, CELL - 1);
        //ctx.fillRect(j * CELL + 8, i * CELL + 8, CELL - 8 * 2, CELL - 8 * 2);

        ctx.fillRect(j * CELL + 1, i * CELL + 1, CELL - 2, CELL - 2);
        //ctx.fillRect(j * CELL + 1, i * CELL + 1, CELL - 1, CELL - 1);
    }
}
