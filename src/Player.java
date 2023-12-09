public class Player {
    boolean player1;
    Board board;
    public Player(boolean player1, Board board) {
        this.player1 = player1;
        this.board = board;
    }

    public int[] getMove() {
        return new int[4];
    }
}
