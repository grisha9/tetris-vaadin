package com.example.application.service;

import com.example.application.views.main.MultiPlayerContentView;
import ru.rzn.gmyasoedov.tetris.core.FigureGenerator;
import ru.rzn.gmyasoedov.tetris.core.MultiPlayerFigureGenerator;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.requireNonNull;

public class GameHolder {
    private final String id;
    private final String ownerSessionId;
    private final FigureGenerator figureGenerator;
    private final Map<String, Tetris> gameBySessionId = new ConcurrentHashMap<>();
    private final Map<String, MultiPlayerContentView> viewBySessionId = new ConcurrentHashMap<>();

    public GameHolder(String id, String ownerSessionId) {
        this.id = requireNonNull(id);
        this.ownerSessionId = requireNonNull(ownerSessionId);
        this.figureGenerator = new MultiPlayerFigureGenerator();
    }

    public String getId() {
        return id;
    }

    public boolean playerExist(String sessionId) {
        return gameBySessionId.containsKey(sessionId);
    }

    public Tetris getGame(String sessionId) {
        return gameBySessionId.get(sessionId);
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }

    public void addPlayer(String sessionId, String name) {
        Tetris tetris = gameBySessionId
                .putIfAbsent(requireNonNull(sessionId), new Tetris(figureGenerator, requireNonNull(name)));
        if (tetris != null) {
            throw new IllegalStateException("player already exist");
        }
    }

    public void addView(String sessionId, MultiPlayerContentView consumer) {
        viewBySessionId.put(sessionId, consumer);
    }

    public Collection<MultiPlayerContentView> getViews() {
        return viewBySessionId.values();
    }

    public Collection<Tetris> getPlayers() {
        return gameBySessionId.values();
    }

    public void block() {
        if (figureGenerator instanceof MultiPlayerFigureGenerator) {
            ((MultiPlayerFigureGenerator) figureGenerator).block();
        }
    }

    public synchronized void newPlayerBroadcastAll(GameHolder message) {
        viewBySessionId.values().forEach(v -> v.renderView(message));
    }
}
