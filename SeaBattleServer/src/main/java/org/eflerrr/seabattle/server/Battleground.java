package org.eflerrr.seabattle.server;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class Battleground {
    private boolean readyStatus = false;
    private final CopyOnWriteArrayList<Ship> ships = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<CopyOnWriteArrayList<Ship>> battlegroundCells;


    public Battleground() {
        battlegroundCells = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 10; ++i) {
            var row = new CopyOnWriteArrayList<Ship>();
            for (int k = 0; k < 10; ++k) {
                row.add(null);
            }
            battlegroundCells.add(row);
        }
    }

    public boolean isReady() {
        return readyStatus;
    }

    public void placeShip(Collection<Pair<Integer, Integer>> cells) {
        var ship = new Ship(cells);
        ships.add(ship);
        for (var cell: cells) {
            battlegroundCells.get(cell.getLeft()).set(cell.getRight(), ship);
        }
        if (ships.size() == 10) {
            readyStatus = true;
        }
    }

    public int fire(Pair<Integer, Integer> cell) {
        var ship = battlegroundCells.get(cell.getLeft()).get(cell.getRight());
        if (ship == null) {
            return -1;
        }
        ship.hit(cell);
        if (ship.isAlive()) {
            return 0;
        }
        return 1;
    }

    public int shipsAlive() {
        return (int) ships.stream().filter(Ship::isAlive).count();
    }

    @Override
    public String toString() {
        var buffer = new StringBuilder();
        for (var row : battlegroundCells) {
            for (var cell : row) {
                buffer.append(cell == null ? "_" : "x");
            }
            buffer.append("\n");
        }
        return buffer.toString();
    }
}
