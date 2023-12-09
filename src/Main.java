import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {

        int[] startingBoard =
                        {1, 0,  2,  0,  3,  0,  4,  0,      0, 0,
                        0,  5,  0,  6,  0,  7,  0,  8,      0, 0,
                        9,  0,  10, 0,  11, 0,  12, 0,      0, 0,
                        0,  0,  0,  0,  0,  0,  0,  0,      0, 0,
                        0,  0,  0,  0,  0,  0,  0,  0,      0, 0,
                        0,  13, 0,  14, 0,  15, 0,  16,     0, 0,
                        17, 0,  18, 0,  19, 0,  20, 0,      0, 0,
                        0,  21, 0,  22, 0,  23, 0,  24,     0, 0};

        int[] startingPiecePositions = new int[] {
                0, 2, 4, 6, 11, 13, 15, 17, 20, 22, 24, 26,
                51, 53, 55, 57, 60, 62, 64, 66, 71, 73, 75, 77
        };

        Board mainBoard = new Board(startingBoard, startingPiecePositions,
                new boolean[24], 12, 12, 0);
        Player player1 = new AIVersion(true, mainBoard, 6, 1);
        Player player2 = new AIVersion(false, mainBoard,10, 3);
        Player player = player1;

        mainBoard.printBoard(player.player1, new int[] {-1, -1});

        Set<Long> visitedPositions = new HashSet<>();

        while (true) {

            int[] move = player.getMove();

            mainBoard.makeMove(move);

            mainBoard.printBoard(!(player == player1), move);


            if (mainBoard.winner != 0) {
                System.out.println("Player " + mainBoard.winner + " wins!");
                break;
            } else if (visitedPositions.contains(mainBoard.encodeBoard())) {
                System.out.println("Tie by repetition.");
                break;
            } else if (player == player1) {
                player = player2;
            } else {
                player = player1;
            }
            visitedPositions.add(mainBoard.encodeBoard());
        }

    }
}