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

import static java.util.Objects.requireNonNull;

public class GameHolder {
    public static final int MAX_PLAYER_LIMIT = 10;

    private final String id;
    private final String ownerSessionId;
    private final long figureGeneratorSeed;
    private final long creationTimeMillis;

    private final Map<String, Tetris> gameBySessionId = new ConcurrentHashMap<>();
    private final Map<String, String> colorByTetrisId = new ConcurrentHashMap<>();
    private final Map<String, GameContentView> viewBySessionId = new ConcurrentHashMap<>();
    private final Collection<Tetris> gameList = new ArrayBlockingQueue<>(MAX_PLAYER_LIMIT);
    private final TetrisSettings settings;

    private boolean started = false;
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

    public Tetris getGame(String sessionId) {
        return gameBySessionId.get(sessionId);
    }

    public String getOwnerSessionId() {
        return ownerSessionId;
    }

    public synchronized void addPlayer(String sessionId, String name, String color) {
        if (gameBySessionId.size() >= MAX_PLAYER_LIMIT) {
            throw new IllegalStateException("max player limit " + MAX_PLAYER_LIMIT);
        }
        FigureGenerator generator = new FigureGenerator(figureGeneratorSeed);

        Tetris tetris = gameBySessionId.get(requireNonNull(sessionId));
        if (tetris != null) {
            throw new IllegalStateException("player already exist");
        }
        var playerNameAlreadyExist = gameBySessionId.values()
                .stream()
                .anyMatch(game -> Objects.equals(game.getId(), name));
        if (playerNameAlreadyExist) {
            throw new IllegalStateException(name + " player name already exist");
        }

        gameBySessionId.put(sessionId, new Tetris(generator, requireNonNull(name), settings));
        if (color != null) {
            colorByTetrisId.put(name, color);
        }
        gameList.add(gameBySessionId.get(sessionId));
    }

    public void addView(String sessionId, GameContentView consumer) {
        viewBySessionId.put(sessionId, consumer);
    }

    public Collection<GameContentView> getViews() {
        return viewBySessionId.values();
    }

    public Collection<Tetris> getPlayers() {
        return gameList;
    }

    public synchronized void newPlayerBroadcastAll(GameHolder message) {
        viewBySessionId.values().forEach(v -> v.renderView(message));
    }

    public String getGameColor(String tetrisId) {
        return colorByTetrisId.get(tetrisId);
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
                .anyMatch(game -> Objects.equals(game.getId(), playerName));
    }

    public TetrisSettings getSettings() {
        return settings;
    }

    public void updateCommonMaxLevel(int level) {
        if (!settings.isCommonMaxSpeed()) return;
        synchronized (this) {
            if (level > maxLevel) {
                maxLevel = level;
                gameList.forEach(tetris -> tetris.setLevel(level));
            }
        }
    }
}
