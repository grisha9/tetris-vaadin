package com.example.application.views.about;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Text;
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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNullElse;

public class TView extends Div {

    private final int widthField;
    private final int heightField;
    private final int widthNext;
    private final int heightNext;
    private final Canvas canvasField;
    private final Canvas canvasNext;
    private final int cellSizePx;
    private final Label score;
    private final Label labelLevel;

    public TView(int cellSizePx, String name) {
        //addClassName("t-view");
        setId(UUID.randomUUID().toString());
        getStyle().set("border", (cellSizePx / 4) + "px solid");
        getStyle().set("padding", (cellSizePx / 4) + "px");
        setWidth(15 * cellSizePx, Unit.PIXELS);

        this.cellSizePx = cellSizePx;
        widthField = 10 * cellSizePx;
        heightField = 20 * cellSizePx;
        canvasField = new Canvas(widthField, heightField);
        widthNext = 4 * cellSizePx;
        heightNext = 4 * cellSizePx;
        canvasNext = new Canvas(widthNext, heightNext);

        CanvasRenderingContext2D ctx = canvasField.getContext();
        ctx.strokeRect(0, 0, widthField, heightField);
        ctx.setFont("bold " + Math.round(cellSizePx * 1.5) + "px Ubuntu Mono");

        VerticalLayout verticalLayout = new VerticalLayout();
        Label nameLabel = new Label(requireNonNullElse(name, ""));
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

    private void addTextFontSize(Label label, int cellSizePx) {
        label.getStyle().set("font-size", cellSizePx + "px");
        label.getStyle().set("font-family", "Ubuntu Mono");
    }


    public void observer(TetrisState state) {
        getUI().ifPresent(ui -> ui.access(() -> renderView(state)));
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
        drawMatrix(ctx, state.getNextFigure());
    }

    private void drawField(TetrisState state, CanvasRenderingContext2D ctx) {
        ctx.clearRect(0, 0, widthField, heightField);
        ctx.strokeRect(0, 0, widthField, heightField);
        drawMatrix(ctx, state.getField());

        ctx.setFillStyle("grey");
        if (state.getState() == Tetris.State.PAUSE) {
            ctx.fillText("pause", cellSizePx * 3, cellSizePx * 10);
        }
        if (state.getState() == Tetris.State.OVER) {
            ctx.fillText("game over", cellSizePx * 2, cellSizePx * 10);
        }
        ctx.setFillStyle("black");
    }

    private void drawMatrix(CanvasRenderingContext2D ctx, int[][] field) {
        for (int i = 0; i < field.length; i++) {
            int[] row = field[i];
            for (int j = 0; j < row.length; j++) {
                if (field[i][j] > 0) {
                    drawCell(ctx, i, j);
                }
            }
        }
    }

    private void drawCell(CanvasRenderingContext2D ctx, int i, int j) {
        ctx.fillRect(j * cellSizePx + 1, i * cellSizePx + 1, cellSizePx - 2, cellSizePx - 2);
    }

    private String formatScore(int score) {
        String scoreString = String.valueOf(score);
        return "0".repeat(Math.max(0, 6 - scoreString.length())) + scoreString;
    }
}
