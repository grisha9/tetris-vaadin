package com.example.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.rzn.gmyasoedov.tetris.core.Tetris;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameHolderService {
    private final Map<String, GameHolder> gameById = new ConcurrentHashMap<>();

    public void createGame(String gameId, String ownerSessionId) {
        GameHolder gameHolder = gameById.putIfAbsent(gameId, new GameHolder(gameId, ownerSessionId));
        if (gameHolder != null) {
            throw new IllegalStateException("game already exist");
        }
    }

    public boolean exist(String id) {
        return gameById.containsKey(id);
    }

    public GameHolder getById(String id) {
        return gameById.get(id);
    }

    @Scheduled(fixedDelay = 100_000)
    public void removeOldGames() {
        gameById.values().stream()
                .filter(g -> g.getPlayers().stream().allMatch(t -> t.getState() == Tetris.State.GAME))
                .map(GameHolder::getId)
                .collect(Collectors.toList())
                .forEach(gameById::remove);
    }
}
