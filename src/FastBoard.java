
public class FastBoard extends Board {

    public FastBoard(int[] startingPos, int[] piecePositions,
                     boolean[] startingPieceQueened, int startingP1Pieces, int startingP2Pieces, int startingNumMovesPlayed) {
        super(startingPos, piecePositions, startingPieceQueened, startingP1Pieces, startingP2Pieces, startingNumMovesPlayed);
    }

    @Override
    public int[] makeMove(int[] move) {  // return everywhere that was taken

        int piece = boardArr[move[0]];

        int[] takenCoords = executeMove(move, piece);

        // don't generate moves here, because minimax might end and decide to eval the position.

        this.winner = checkWinner(piece < 13);
        return takenCoords;
    }

    public void undoMove(int[] ogMove, int[] takenCoords, boolean[] ogQueened, int ogNumQueened) {
        // put the piece back
        int finishingPos = ogMove[ogMove.length - 1];
        int ogPos = ogMove[0];
        int piece = boardArr[finishingPos];

        boardArr[ogPos] = piece;
        boardArr[finishingPos] = 0;

        piecePositions[piece - 1] = ogPos;

        // replace taken pieces
        boolean player1 = piece < 13;

        if (takenCoords != null) {
            for (int i = 0; i < takenCoords.length; i += 2) {
                int takenPos = takenCoords[i];
                int takenPiece = takenCoords[i + 1];

                boardArr[takenPos] = takenPiece;

                piecePositions[takenPiece - 1] = takenPos;

                if (player1) {
                    numPlayerTwoPieces += 1;
                } else {
                    numPlayerOnePieces += 1;
                }
            }
        }

        // redo queened
        pieceQueened = ogQueened;
        numQueens = ogNumQueened;
    }
}
