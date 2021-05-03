package com.example.application.views.helloworld;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.example.application.views.main.MainView;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.shared.ui.Transport;
import org.vaadin.pekkam.Canvas;
import org.vaadin.pekkam.CanvasRenderingContext2D;

//@Route(value = "hello", layout = MainView.class)
//@RouteAlias(value = "", layout = MainView.class)
@PageTitle("Hello World")
public class HelloWorldView extends HorizontalLayout {

    private TextField name;
    private Button sayHello;

    public HelloWorldView() {
        addClassName("hello-world-view");
        name = new TextField("Your name");
        sayHello = new Button("Say hello");

        add(name, sayHello);



        setVerticalComponentAlignment(Alignment.END, name, sayHello);


        sayHello.addClickListener(e -> {

            /*new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                UI ui = getUI().get();
                ui.access(() -> name.setValue("zal1"));
            }).start();*/
            //Notification.show("Hello " + name.getValue());
            Notification.show("Hello " + e.getButton());
        });

        UI.getCurrent().addShortcutListener(
                () -> Notification.show("Shortcut triggered"),
                Key.SPACE);


        addClickShortcut(Key.KEY_A);
        addClickShortcut(Key.KEY_B);
        addClickShortcut(Key.KEY_C);

       /* addClickListener(e -> {
            Notification.show("Hello " + e.getButton());
        });*/
    }

}
