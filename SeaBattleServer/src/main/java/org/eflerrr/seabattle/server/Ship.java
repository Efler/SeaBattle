package org.eflerrr.seabattle.server;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Ship {
    private final int size;
    private final ConcurrentHashMap<Pair<Integer, Integer>, Boolean> shipCells = new ConcurrentHashMap<>();


    public Ship(Collection<Pair<Integer, Integer>> cells) {
        this.size = cells.size();
        shipCells.putAll(cells.stream().collect(Collectors.toMap(key -> key, value -> true)));
    }

    public int getSize() {
        return size;
    }

    public boolean contains(Pair<Integer, Integer> cell) {
        return shipCells.containsKey(cell);
    }

    public void hit(Pair<Integer, Integer> cell) {
        if (!this.contains(cell)) {
            throw new IllegalArgumentException("Cell does not found!");
        }
        shipCells.put(cell, false);
    }

    public boolean isAlive() {
        return shipCells.containsValue(true);
    }

}
