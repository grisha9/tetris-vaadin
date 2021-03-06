package com.example.application.views.tertis;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.vaadin.pekkam.Canvas;
import org.vaadin.pekkam.CanvasRenderingContext2D;
import ru.rzn.gmyasoedov.tetris.core.Tetris;
import ru.rzn.gmyasoedov.tetris.core.TetrisState;

import java.util.UUID;

public class TetrisView extends Div {

    public static final String BACKGROUND_COLOR = "#F8F8F8";
    private final int widthNext;
    private final int heightNext;
    private final Canvas canvasField;
    private final Canvas canvasNext;
    private final int cellSizePx;
    private final Label score;
    private final Label labelLevel;
    private final String color;
    private final int widthField;
    private final int heightField;
    private boolean paused = false;

    public TetrisView(int cellSizePx, String name, String color) {
        this.color = color;
        setId(UUID.randomUUID().toString());
        getStyle().set("border", (cellSizePx / 4) + "px solid");
        getStyle().set("padding", (cellSizePx / 4) + "px");
        getStyle().set("color", color);
        setWidth(15 * cellSizePx, Unit.PIXELS);

        this.cellSizePx = cellSizePx;
        this.widthField = 10 * cellSizePx;
        this.heightField = 20 * cellSizePx;
        this.canvasField = new Canvas(widthField, heightField);
        this.widthNext = 4 * cellSizePx;
        this.heightNext = 4 * cellSizePx;
        this.canvasNext = new Canvas(widthNext, heightNext);

        CanvasRenderingContext2D ctx = canvasField.getContext();
        ctx.setStrokeStyle(color);
        ctx.strokeRect(0, 0, widthField, heightField);
        ctx.setFont("bold " + Math.round(cellSizePx * 1.5) + "px Ubuntu Mono");
        drawEmptyMatrix(ctx);

        VerticalLayout verticalLayout = new VerticalLayout();
        Label nameLabel = new Label(getName(name));
        nameLabel.getStyle().set("font-weight", "bold");
        Label scoreLabel = new Label("score");
        score = new Label("");
        labelLevel = new Label("level:");
        score.setText(formatScore(0));
        addTextFontSize(nameLabel, cellSizePx);
        addTextFontSize(scoreLabel, cellSizePx);
        addTextFontSize(score, cellSizePx);
        addTextFontSize(labelLevel, cellSizePx);

        verticalLayout.getStyle().set("padding", "0");
        verticalLayout.getStyle().set("margin-top", "0");
        verticalLayout.getStyle().set("margin-bottom", "0");
        verticalLayout.getStyle().set("margin-right", "0");
        verticalLayout.getStyle().set("margin-left", (cellSizePx * 2 / 3) + "px");
        verticalLayout.add(nameLabel, scoreLabel, score, labelLevel, canvasNext);
        verticalLayout.setHorizontalComponentAlignment(FlexComponent.Alignment.START);

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(canvasField, verticalLayout);
        horizontalLayout.setVerticalComponentAlignment(FlexComponent.Alignment.CENTER, canvasField, verticalLayout);
        add(horizontalLayout);

    }

    private String getName(String name) {
        name = name == null ? "" : name;
        if (name.length() > 9) {
            name = name.substring(0, 9);
        }
        return name;
    }

    private void addTextFontSize(Label label, int cellSizePx) {
        label.getStyle().set("font-size", cellSizePx + "px");
        label.getStyle().set("font-family", "Ubuntu Mono");
    }


    public void observer(TetrisState state) {
        if (state.getState() == Tetris.State.OVER) {
            getUI().ifPresent(ui -> ui.accessSynchronously(() -> renderView(state)));
        } else {
            getUI().ifPresent(ui -> ui.access(() -> renderView(state)));
        }
    }

    private void renderView(TetrisState state) {
        //ctx.setFillStyle("blue");
        //ctx.setStrokeStyle("blue");
        drawField(state, canvasField.getContext());
        drawNextFigure(state, canvasNext.getContext());

        score.setText(formatScore(state.getScore()));
        labelLevel.setText("level: " + state.getLevel());
    }

    private void drawNextFigure(TetrisState state, CanvasRenderingContext2D ctx) {
        ctx.clearRect(0, 0, widthNext, heightNext);
        drawMatrix(ctx, state.getNextFigure(), "#FFFFFF");
    }

    private void drawField(TetrisState state, CanvasRenderingContext2D ctx) {
        if (paused) {
            ctx.clearRect(0, 0, widthField, heightField);
            ctx.strokeRect(0, 0, widthField, heightField);
            paused = state.getState() == Tetris.State.PAUSE;
        }
        drawMatrix(ctx, state.getField(), BACKGROUND_COLOR);

        ctx.setFillStyle("grey");
        if (state.getState() == Tetris.State.PAUSE) {
            ctx.fillText("pause", cellSizePx * 3, cellSizePx * 10);
            paused = true;
        }

        if (state.getState() == Tetris.State.OVER) {
            ctx.fillText("game over", cellSizePx * 2, cellSizePx * 10);
        }
    }

    private void drawMatrix(CanvasRenderingContext2D ctx, int[][] field, String backgroundColor) {
        for (int i = 0; i < field.length; i++) {
            int[] row = field[i];
            for (int j = 0; j < row.length; j++) {
                drawCell(ctx, i, j, field[i][j] > 0 ? color : backgroundColor);
            }
        }
    }

    private void drawEmptyMatrix(CanvasRenderingContext2D ctx) {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                drawCell(ctx, i, j, BACKGROUND_COLOR);
            }
        }
    }

    private void drawCell(CanvasRenderingContext2D ctx, int i, int j, String fillStyle) {
        ctx.setFillStyle(fillStyle);
        ctx.fillRect(j * cellSizePx + 1, i * cellSizePx + 1, cellSizePx - 2, cellSizePx - 2);
    }

    private String formatScore(int score) {
        String scoreString = String.valueOf(score);
        return "0".repeat(Math.max(0, 6 - scoreString.length())) + scoreString;
    }
}
