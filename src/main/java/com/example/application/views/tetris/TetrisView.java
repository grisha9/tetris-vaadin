package com.example.application.views.tetris;

import com.example.application.views.about.TView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyNotifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import ru.rzn.gmyasoedov.tetris.core.FigureGenerator;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//@Route(value = "tetris", layout = MainView.class)
@PageTitle("Tetris")
public class TetrisView extends Div implements KeyNotifier{

    private Tetris tetris;
    private Button start;
    private Button left;
    private Button right;
    private Button rotate;

    public TetrisView() {
        addClassName("tetris-view");
        List<TView> grisha1 = IntStream.range(0, 2).mapToObj(i -> new TView(20, "Grisha")).collect(Collectors.toList());
        HorizontalLayout layout = new HorizontalLayout();
        grisha1.forEach(layout::add);

        List<TView> grisha2 = IntStream.range(0, 0).mapToObj(i -> new TView(30, "Grisha")).collect(Collectors.toList());
        HorizontalLayout layout2 = new HorizontalLayout();
        grisha2.forEach(layout2::add);

        List<TView> grisha3 = IntStream.range(0, 0).mapToObj(i -> new TView(30, "Grisha")).collect(Collectors.toList());
        HorizontalLayout layout3 = new HorizontalLayout();
        grisha3.forEach(layout3::add);

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.add(layout, layout2, layout3);
        add(verticalLayout);

        ArrayList<TView> grisha = new ArrayList<>();
        grisha.addAll(grisha1);
        grisha.addAll(grisha2);
        grisha.addAll(grisha3);

        UI.getCurrent().addShortcutListener(e -> startTetris(grisha), Key.SPACE);
        UI.getCurrent().addShortcutListener(e -> tetris.toLeft(), Key.ARROW_LEFT);
        UI.getCurrent().addShortcutListener(e -> tetris.toRight(), Key.ARROW_RIGHT);
        UI.getCurrent().addShortcutListener(e -> tetris.rotate(), Key.ARROW_UP);
        //UI.getCurrent().addShortcutListener(e -> tetris.fastSpeed(), Key.ARROW_DOWN);
        UI.getCurrent().addShortcutListener(e -> tetris.rotate(), Key.KEY_Z);
        UI.getCurrent().addShortcutListener(e -> tetris.rotateReverse(), Key.KEY_X);
        UI.getCurrent().addShortcutListener(e -> tetris.fastSpeed(), Key.KEY_A);
        UI.getCurrent().addShortcutListener(e -> tetris.normalSpeed(), Key.KEY_S);
        UI.getCurrent().addShortcutListener(e -> tetris.pauseOrResume(), Key.KEY_P);
        addKeyDownListener(Key.ARROW_DOWN, e -> Notification.show("dd"));
        addKeyUpListener(Key.ARROW_DOWN, e -> Notification.show("uuu"));

    }

    /*public TetrisView() {
        addClassName("tetris-view");
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

        add(start, left);
        add(left);
        add(right);
        add(rotate);
        add(canvasField);
        add(canvasNext);
        add(scoreLabel);

        setVerticalComponentAlignment(Alignment.CENTER);


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

    }*/

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        /*addListener(KeyDownEvent.class, e -> {
            Notification.show("bbbbbb");
            if (e.getKey().getKeys().equals(Key.ARROW_DOWN.getKeys())) {
                tetris.fastSpeed();
            }
        });
        addListener(KeyUpEvent.class, e -> {
            Notification.show("zzzz");
            if (e.getKey().getKeys().equals(Key.ARROW_DOWN.getKeys())) {
                tetris.normalSpeed();
            }
        });*/
    }

    private void startTetris(Collection<TView> grisha) {
        if (tetris != null) {
            tetris.stop();
        }
        tetris = new Tetris(new FigureGenerator(),"5");
        tetris.addObserver(s -> grisha.forEach(g -> g.observer(s)));
        tetris.start();

    }


   /* private void observer(TetrisState state) {
        getUI().ifPresent(ui -> ui.access(() -> renderView(state)));
    }

    private void renderView(TetrisState state) {
        CanvasRenderingContext2D ctx = canvasField.getContext();
        //ctx.setFillStyle("blue");
        //ctx.setStrokeStyle("blue");
        ctx.clearRect(0, 0, WIDTH, HEIGHT);
        ctx.strokeRect(0, 0, WIDTH, HEIGHT);
        int[][] field = state.getField();
        for (int i = 0; i < field.length; i++) {
            int[] row = field[i];
            for (int j = 0; j < row.length; j++) {
                if (field[i][j] > 0) {
                    ctx.fillRect(j * CELL + 1, i * CELL + 1, CELL - 2, CELL - 2);
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
    }*/
}
