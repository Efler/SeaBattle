package org.eflerrr.seabattle.server;

import org.apache.commons.lang3.tuple.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

// TODO: check unused sockets!

public class Server {
    private static final int PLAYER_READY_STATUS = 0;
    private static final int FIRST_PLAYER_STATUS = 1;
    private static final int SECOND_PLAYER_STATUS = 2;
    private static final int PLAYER_OPPONENT = 10;
    private static final int BOT_OPPONENT = 11;

    private static ServerSocket serverSocket = null;    // TODO: check if used, otherwise delete!
    private static ExecutorService service = null;


    private static class GameVsPlayerHandler implements Runnable {
        private final Socket firstClient;
        private final DataInputStream firstClientReader;
        private final DataOutputStream firstClientWriter;
        private final Socket secondClient;
        private final DataInputStream secondClientReader;
        private final DataOutputStream secondClientWriter;

        private int battle(
                Battleground firstBattleground, DataInputStream firstClientReader, DataOutputStream firstClientWriter,
                Battleground secondBattleground, DataInputStream secondClientReader, DataOutputStream secondClientWriter
        ) {
            // TODO: make battle logic!
            System.out.println("#1");
            System.out.println(firstBattleground);
            System.out.println("#2");
            System.out.println(secondBattleground);
            return 0;
        }

        private GameVsPlayerHandler(Socket first, DataInputStream firstReader, DataOutputStream firstWriter,
                                    Socket second, DataInputStream secondReader, DataOutputStream secondWriter) {
            this.firstClient = first;
            this.firstClientReader = firstReader;
            this.firstClientWriter = firstWriter;
            this.secondClient = second;
            this.secondClientReader = secondReader;
            this.secondClientWriter = secondWriter;
        }

        @Override
        public void run() {
            CopyOnWriteArrayList<Future<Pair<Battleground, Integer>>> results = new CopyOnWriteArrayList<>();
            results.add(service.submit(new PlayerHandler(firstClient, firstClientReader, firstClientWriter, 1)));
            results.add(service.submit(new PlayerHandler(secondClient, secondClientReader, secondClientWriter, 2)));
            CopyOnWriteArrayList<Pair<Battleground, Integer>> battlegrounds = new CopyOnWriteArrayList<>();
            Socket waitingClient;
            DataOutputStream waitingClientWriter = null;
            try {
                while (true) {
                    for (var res : results) {
                        if (res.isDone()) {
                            var tmpFirstRes = res.get();
                            int tmpFirstId = tmpFirstRes.getRight();
                            battlegrounds.add(tmpFirstRes);
                            waitingClient = (tmpFirstId == 1) ? firstClient : secondClient;
                            waitingClientWriter = (tmpFirstId == 1) ? firstClientWriter : secondClientWriter;
                            results.remove(res);
                            break;
                        }
                    }
                    if (!battlegrounds.isEmpty()) {
                        break;
                    }
                    Thread.sleep(200);
                }
                waitingClientWriter.writeInt(FIRST_PLAYER_STATUS);

                var awaitingRes = results.getLast();
                while (!awaitingRes.isDone()) {
                    Thread.sleep(200);
                }

                var tmpSecondRes = awaitingRes.get();
                int tmpSecondId = tmpSecondRes.getRight();
                battlegrounds.add(tmpSecondRes);
                Socket awaitingClient = (tmpSecondId == 1) ? firstClient : secondClient;
                DataOutputStream awaitingClientWriter = (tmpSecondId == 1) ? firstClientWriter : secondClientWriter;
                awaitingClientWriter.writeInt(SECOND_PLAYER_STATUS);
                waitingClientWriter.writeInt(PLAYER_READY_STATUS);
                battlegrounds.sort(Comparator.comparingInt(Pair::getRight));

                int winnerId = battle(
                        battlegrounds.getFirst().getLeft(), firstClientReader, firstClientWriter,
                        battlegrounds.getLast().getLeft(), secondClientReader, secondClientWriter);
                // TODO: run battle!
                Thread.sleep(20000);

            } catch (InterruptedException | ExecutionException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class GameVsBotHandler implements Runnable {
        private final Socket clientSocket;
        private final DataInputStream clientReader;
        private final DataOutputStream clientWriter;

        private GameVsBotHandler(Socket client, DataInputStream reader, DataOutputStream writer) {
            this.clientSocket = client;
            this.clientReader = reader;
            this.clientWriter = writer;
        }

        private int battle(
                Battleground clientBattleground, Battleground botBattleground
        ) {
            // TODO: battle logic!
            System.out.println("#PLAYER");
            System.out.println(clientBattleground);
            System.out.println("#BOT");
            System.out.println(botBattleground);
            return 0;
        }

        private Battleground generateBattleground() {
            // TODO: implement!
            var random = new Random();

            return null;
        }

        @Override
        public void run() {
            try {
                var result = service.submit(new PlayerHandler(clientSocket, clientReader, clientWriter, 1));
                while (!result.isDone()) {
                    Thread.sleep(200);
                }
                var clientBattleground = result.get().getLeft();
                var botBattleground = generateBattleground();
                clientWriter.writeInt(SECOND_PLAYER_STATUS);

                int winnerId = battle(clientBattleground, botBattleground);
                // TODO: run battle!
                Thread.sleep(20000);

            } catch (InterruptedException | ExecutionException | IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static class PlayerHandler implements Callable<Pair<Battleground, Integer>> {
        private final Socket player;
        private final DataInputStream playerReader;
        private final DataOutputStream playerWriter;
        private final int id;
        private final Battleground battleground = new Battleground();

        public PlayerHandler(Socket player, DataInputStream playerReader, DataOutputStream playerWriter, int id) {
            this.player = player;
            this.id = id;
            this.playerReader = playerReader;
            this.playerWriter = playerWriter;
        }

        private boolean checkShipShape(Collection<Pair<Integer, Integer>> coords) {
            boolean angle = false;
            int tmpX = -1;
            for (var x : coords.stream().map(Pair::getLeft).toList()) {
                if (tmpX < 0) {
                    tmpX = x;
                } else {
                    if (tmpX != x) {
                        angle = true;
                        int tmpY = -1;
                        for (var y : coords.stream().map(Pair::getRight).toList()) {
                            if (tmpY < 0) {
                                tmpY = y;
                            } else {
                                if (tmpY != y) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }

            var secondCoords = coords.stream().map(angle ? Pair::getLeft : Pair::getRight).sorted().toList();
            for (int i = 1; i < secondCoords.size(); ++i) {
                if (secondCoords.get(i) - secondCoords.get(i - 1) != 1) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Pair<Battleground, Integer> call() throws IOException {
            while (true) {
                int count = playerReader.readInt();
                var coords = new ArrayList<Pair<Integer, Integer>>();
                for (int i = 0; i < count; ++i) {
                    int x = playerReader.readInt();
                    int y = playerReader.readInt();
                    coords.add(Pair.of(x, y));
                }
                if (!checkShipShape(coords)) {
                    playerWriter.writeInt(1);   // TODO: statuses!
                } else {
                    battleground.placeShip(coords);
                    if (!battleground.isReady()) {
                        playerWriter.writeInt(0);
                    } else {
                        playerWriter.writeInt(-1);
                        return Pair.of(battleground, id);
                    }
                }
            }
        }
    }

    public static void start(int port) {
        try {
            try (ServerSocket createdSocket = new ServerSocket(port);
                 var createdService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            ) {
                serverSocket = createdSocket;
                service = createdService;
                ArrayDeque<Socket> waitingClients = new ArrayDeque<>();

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    DataInputStream clientReader = new DataInputStream(clientSocket.getInputStream());
                    DataOutputStream clientWriter = new DataOutputStream(clientSocket.getOutputStream());
                    int chosenOppId = clientReader.readInt();
                    if (chosenOppId == BOT_OPPONENT) {
                        clientWriter.writeInt(SECOND_PLAYER_STATUS);
                        service.execute(new GameVsBotHandler(clientSocket, clientReader, clientWriter));
                    } else if (chosenOppId == PLAYER_OPPONENT) {
                        var waitingClient = waitingClients.pollFirst();
                        if (waitingClient == null) {
                            clientWriter.writeInt(FIRST_PLAYER_STATUS);
                            waitingClients.add(clientSocket);
                        } else {
                            clientWriter.writeInt(SECOND_PLAYER_STATUS);
                            DataInputStream waitingClientReader = new DataInputStream(waitingClient.getInputStream());
                            DataOutputStream waitingClientWriter = new DataOutputStream(waitingClient.getOutputStream());
                            waitingClientWriter.writeInt(PLAYER_READY_STATUS);
                            service.execute(new GameVsPlayerHandler(
                                    waitingClient, waitingClientReader, waitingClientWriter,
                                    clientSocket, clientReader, clientWriter));
                        }
                    } else {
                        System.out.println("Invalid message from client!");
                        clientSocket.close();
                    }
                }
            }
        } catch (IOException ex) {
            System.out.printf("Invalid server starting! Message: %s%n", ex.getMessage());
            ex.printStackTrace();
        }
    }
}
