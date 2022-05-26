package com.example.application.views.main;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import ru.rzn.gmyasoedov.tetris.core.TetrisSettings;

public class SettingsView extends VerticalLayout {
    private final TextField scoreLevelDelta = new TextField("scoreLevelDelta");
    private final TextField speedDelta = new TextField("speedDelta");
    private final Checkbox commonMaxSpeed = new Checkbox("commonMaxSpeed");
    private final Checkbox badRowAfterTetris = new Checkbox("badRowAfterTetris");

    private final Binder<TetrisSettings> binder = new Binder<>(TetrisSettings.class);

    private final TetrisSettings settings;

    public SettingsView(TetrisSettings settings) {
        this.settings = settings;

        binder.forField(scoreLevelDelta)
                .withConverter(new StringToIntegerConverter("Must enter a number"))
                .bind(TetrisSettings::getScoreLevelDelta, TetrisSettings::setScoreLevelDelta);
        binder.forField(speedDelta)
                .withConverter(new StringToIntegerConverter("Must enter a number"))
                .bind(TetrisSettings::getSpeedDelta, TetrisSettings::setSpeedDelta);
        binder.forField(commonMaxSpeed)
                .bind(TetrisSettings::isCommonMaxSpeed, TetrisSettings::setCommonMaxSpeed);
        binder.forField(badRowAfterTetris)
                .bind(TetrisSettings::isBadRowAfterTetris, TetrisSettings::setBadRowAfterTetris);
        binder.setBean(settings);

        add(scoreLevelDelta, speedDelta, commonMaxSpeed, badRowAfterTetris);
        setSpacing(true);
    }

    public TetrisSettings getSettings() {
        return settings;
    }


}
