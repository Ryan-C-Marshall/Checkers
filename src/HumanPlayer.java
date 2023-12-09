import java.util.Arrays;
import java.util.Scanner;

public class HumanPlayer extends Player {

    String[] letterList;
    Scanner scanner;

    public HumanPlayer(boolean player1, Board board) {
        super(player1, board);

        scanner = new Scanner(System.in);
        letterList = new String[] {"A", "B", "C", "D", "E", "F", "G", "H"};
    }

    @Override
    public int[] getMove() {  // change to remove possible moves when they don't fit until one is left

        int[] move = new int[getInput(false, "Enter the number of jumps you are making: ") + 1];

        move[0] = getInput(true, "Enter starting column (A - H): ");
        move[0] += 10 * (getInput(false, "Enter starting row (1 - 8): ") - 1);

        for (int i = 1; i < move.length; i += 1) {
            move[i] = getInput(true, "Enter next column (A - H): ");
            move[i] += 10 * (getInput(false, "Enter next row (1 - 8): ") - 1);
        }

        boolean validMove = false;
        for (int[] possibleMove : board.possibleMoves) {
            if (Arrays.equals(possibleMove, move)) {
                validMove = true;
                break;
            }
        }

        if (!validMove) {
            System.out.println("That is not a valid move. Please try again.");
            return getMove();
        }
        return move;
    }

    private int getInput(boolean letter, String prompt) {
        int retVal = 0;
        boolean goodInput = false;

        while (!goodInput) {
            System.out.print(prompt);
            String input = scanner.nextLine();

            try {
                if (letter) {
                    for (int i = 0; i < 8; i ++) {
                        if (input.toUpperCase().equals(letterList[i])) {
                            retVal = i;
                            goodInput = true;
                        }
                    }
                } else {
                    retVal = Integer.parseInt(input);
                    goodInput = true;
                }
            } catch (NumberFormatException e) {
                goodInput = false;
            }

            if (!goodInput) {
                System.out.println("Please enter a valid input.");
            }
        }
        return retVal;
    }
}
