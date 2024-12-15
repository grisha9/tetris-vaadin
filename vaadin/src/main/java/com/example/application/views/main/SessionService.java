package com.example.application.views.main;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.UUID;

@Component
@SessionScope
public class SessionService {
    private String uid = UUID.randomUUID().toString();

    public String getText(){
        return "session " + uid;
    }
}