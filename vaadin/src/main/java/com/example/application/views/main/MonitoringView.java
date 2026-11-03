package com.example.application.views.main;

import com.example.application.service.GameHolderService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.UUID;

import static com.example.application.service.Utils.createMainViewButton;

@Route("g-monitoring")
public class MonitoringView extends AppLayout {


    public MonitoringView(GameHolderService gameHolderService) {
        HorizontalLayout header = createHeader();
        addToNavbar(createTopBar(header));

        TextField textField = new TextField();
        textField.setValue("Games count: " + gameHolderService.getAllGames().size());

        Button clearButton = new Button("GC");
        clearButton.addClickListener(e -> gameHolderService.removeAllGames());

        setContent(new HorizontalLayout(textField, clearButton, createMainViewButton(null)));
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.getThemeList().add("dark");
        header.setPadding(false);
        header.setSpacing(false);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setId("header");

        Image logo = new Image("images/logo.png", "Tetris logo");
        logo.setId("logo");
        header.add(logo);
        header.add(new H1("Tetris single player"));
        return header;
    }

    private VerticalLayout createTopBar(HorizontalLayout header) {
        VerticalLayout layout = new VerticalLayout();
        layout.getThemeList().add("dark");
        layout.setWidthFull();
        layout.setSpacing(false);
        layout.setPadding(false);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.add(header);
        return layout;
    }
}
