package com.example.application.service;

import com.example.application.views.main.GameContentView;
import org.apache.commons.lang3.RandomUtils;
import ru.rzn.gmyasoedov.tetris.core.FigureGenerator;
import ru.rzn.gmyasoedov.tetris.core.Tetris;
import ru.rzn.gmyasoedov.tetris.core.TetrisSettings;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class GameHolder {
    public static final int MAX_PLAYER_LIMIT = 10;

    private final String id;
    private final String ownerSessionId;
    private final long figureGeneratorSeed;
    private final long creationTimeMillis;

    private final Map<String, GameData> gameBySessionId = new ConcurrentHashMap<>();
    private final Map<String, GameContentView> viewBySessionId = new ConcurrentHashMap<>();
    private final TetrisSettings settings;

    private volatile boolean started = false;
    private int maxLevel = 1;

    public GameHolder(String id, String ownerSessionId) {
        this.id = requireNonNull(id);
        this.ownerSessionId = requireNonNull(ownerSessionId);
        this.figureGeneratorSeed = RandomUtils.nextLong();
        this.creationTimeMillis = System.currentTimeMillis();
        this.settings = new TetrisSettings().setScoreLevelDelta(25);
    }

    public String getId() {
        return id;
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }

    public synchronized void addPlayer(String sessionId, String name, String color) {
        if (gameBySessionId.size() >= MAX_PLAYER_LIMIT) {
            throw new IllegalStateException("max player limit " + MAX_PLAYER_LIMIT);
        }
        FigureGenerator generator = new FigureGenerator(figureGeneratorSeed);

        var tetris = gameBySessionId.get(requireNonNull(sessionId));
        if (tetris != null) {
            throw new IllegalStateException("player already exist");
        }
        if (playerAlreadyExist(name)) {
            throw new IllegalStateException(name + " player name already exist");
        }

        GameData gameData = new GameData(new Tetris(generator, requireNonNull(name), settings), color);
        gameBySessionId.put(sessionId, gameData);
    }

    public void addView(String sessionId, GameContentView consumer) {
        viewBySessionId.put(sessionId, consumer);
    }

    public Collection<GameContentView> getViews() {
        return viewBySessionId.values();
    }

    public Collection<Tetris> getPlayers() {
        return gameBySessionId.values().stream().map(it -> it.tetris).collect(Collectors.toList());
    }

    public synchronized void newPlayerBroadcastAll(GameHolder message) {
        viewBySessionId.values().forEach(v -> v.renderView(message));
    }

    public Tetris getGame(String sessionId) {
        GameData gameData = gameBySessionId.get(sessionId);
        return gameData != null ? gameData.tetris : null;
    }

    public String getGameColor(String sessionId) {
        GameData gameData = gameBySessionId.get(sessionId);
        return gameData != null ? gameData.color : null;
    }

    public long getCreationTimeMillis() {
        return creationTimeMillis;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        if (this.started) return;
        this.started = started;
    }

    public boolean playerAlreadyExist(String playerName) {
        return gameBySessionId.values()
                .stream()
                .anyMatch(game -> Objects.equals(game.tetris.getId(), playerName));
    }

    public TetrisSettings getSettings() {
        return settings;
    }

    public void updateCommonMaxLevel(int level) {
        if (!settings.isCommonMaxSpeed()) return;
        synchronized (this) {
            if (level > maxLevel) {
                maxLevel = level;
                gameBySessionId.values().forEach(it -> it.tetris.setLevel(level));
            }
        }
    }

    public void clear() {
        gameBySessionId.clear();
        viewBySessionId.clear();
    }

    public static class GameData {
        private final Tetris tetris;
        private final String color;

        public GameData(Tetris tetris, String color) {
            this.tetris = tetris;
            this.color = color;
        }

        public Tetris getTetris() {
            return tetris;
        }

        public String getColor() {
            return color;
        }

        @Override
        public String toString() {
            return "GameData{" +
                    "tetris=" + tetris +
                    ", color='" + color + '\'' +
                    '}';
        }
    }
}
