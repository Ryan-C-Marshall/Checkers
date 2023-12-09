import java.util.*;

public class Board {
    protected int[] piecePositions;  // 10 * move[0] + move[1]
    protected boolean[] pieceQueened;  // true if queened

    protected int numQueens;
    protected int[] boardArr;  // [row 0 ..., row 1 ..., ...]

    LinkedList<int[]> possibleMoves;

    int winner = 0;

    int numMovesPlayed;
    int numPlayerOnePieces;
    int numPlayerTwoPieces;

    public Board(int[] startingPos, int[] piecePositions,
                 boolean[] startingPieceQueened, int startingP1Pieces, int startingP2Pieces, int startingNumMovesPlayed) {
        this.boardArr = startingPos;
        this.piecePositions = piecePositions;
        this.pieceQueened = startingPieceQueened;
        this.numQueens = 0;
        this.possibleMoves = new LinkedList<>();

        this.numPlayerOnePieces = startingP1Pieces;
        this.numPlayerTwoPieces = startingP2Pieces;
        this.numMovesPlayed = startingNumMovesPlayed;
    }

    public int[] makeMove(int[] move) {
        int piece = boardArr[move[0]];

        int[] takenCoords = executeMove(move, piece);

        // generate moves for not whoever just played
        this.possibleMoves = generateMoves(!(piece < 13));

        // check winner
        this.winner = checkWinner(piece < 13);

        return takenCoords;
    }

    protected int[] executeMove(int[] move, int piece) {

        int[] takenCoords;

        if (Math.abs(move[1] - move[0]) > 17) {  // takes
            takenCoords = new int[(move.length - 1) * 2];
        } else {
            takenCoords = null;
        }

        for (int i = 0; i < move.length - 1; i += 1) {
            if (Math.abs(move[i] - move[i + 1]) > 17) {
                int takePosition = (move[i] + move[i + 1])/2;

                // update variables
                if (piece < 13) {
                    numPlayerTwoPieces -= 1;
                } else {
                    numPlayerOnePieces -= 1;
                }

                // update taken coords
                assert takenCoords != null;
                takenCoords[2 * i] = takePosition;
                takenCoords[(2 * i) + 1] = boardArr[takePosition];

                piecePositions[boardArr[takePosition] - 1] = -1;

                boardArr[takePosition] = 0;
            }

            if (i != 0 && (move[i] / 10 == 0 || move[i] / 10 == 7)) {
                if (!pieceQueened[piece - 1]) {
                    pieceQueened[piece - 1] = true;
                    numQueens++;
                }
            }
        }

        if ((move[move.length - 1] / 10 == 0 || move[move.length - 1] / 10 == 7)) {
            if (!pieceQueened[piece - 1]) {
                pieceQueened[piece - 1] = true;
                numQueens++;
            }
        }

        boardArr[move[move.length - 1]] = piece;
        boardArr[move[0]] = 0;

        piecePositions[piece - 1] = move[move.length - 1];

        numMovesPlayed += 1;

        return takenCoords;
    }

    protected int checkWinner(boolean playerOne) {
        if (possibleMoves.isEmpty()) {
            if (playerOne) {
                return 1;
            } else {
                return -1;
            }
        }
        return 0;
    }

    public LinkedList<int[]> generateMoves(boolean player1, LinkedList<int[]> previousMoves, int[] ourMove, int[] theirMove) {
        int ourStartPos = ourMove[0];
        int ourEndPos = ourMove[ourMove.length - 1];
        int theirStartPos = theirMove[0];
        int theirEndPos = theirMove[theirMove.length - 1];

        boolean ourPieceAlive = true;

        LinkedList<int[]> possibleMoves = new LinkedList<>();

        // add most of the probably viable previous moves (not those that were blocked or TODO pieces that were taken)
        boolean addMove;
        for (int[] prevMove : previousMoves) {
            addMove = true;

            // only add moves that don't contain either endPos (no need to check the start of the move)
            for (int i = 1; i < prevMove.length; i ++) {
                int position = prevMove[i];
                if (position == ourEndPos || position == theirEndPos) {
                    addMove = false;
                    break;
                }
            }

            if (addMove) {
                possibleMoves.add(prevMove);
            }
        }

        // add anything that can now move to where it was

            // their move
        checkAroundSpace(possibleMoves, theirStartPos, player1,
                theirStartPos % 10 < 7, theirStartPos % 10 > 0, theirStartPos < 70, theirStartPos > 9);
            // our move
        if (ourStartPos != theirEndPos) {
            checkAroundSpace(possibleMoves, ourStartPos, player1,
                    ourStartPos % 10 < 7, ourStartPos % 10 > 0, ourStartPos < 70, ourStartPos > 9);
        }

        // add moves to places that are now vacant (same?)

            // their move
        if (Math.abs(theirMove[1] - theirStartPos) > 17) {  // takes
            for (int i = 0; i < theirMove.length - 2; i++) {
                int takenPosition = (theirMove[i] + theirMove[i + 1])/2;
                checkAroundSpace(possibleMoves, takenPosition, player1,
                        takenPosition % 10 < 7, takenPosition % 10 > 0, takenPosition < 70, takenPosition > 9);

                // is our piece dead
                if (takenPosition == ourEndPos) {
                    ourPieceAlive = false;
                }

                // did this open an opportunity to take

                // taking down and left
                if (takenPosition + 22 < 80 && (takenPosition + 22) % 10 < 8 && checkValidPiece(!player1, takenPosition + 11) &&
                        checkValidPiece(player1, takenPosition + 22)) {
                    int takingPiece = boardArr[takenPosition + 22];
                    int pieceThere = boardArr[takenPosition + 11];
                    boardArr[takenPosition + 11] = 0;

                    boolean tpQueened = pieceQueened[takingPiece - 1];

                    if ((!player1 || tpQueened)) {
                        int[] sequence = new int[]{takenPosition + 22, takenPosition};
                        possibleMoves.add(sequence);
                        generateTakes(takenPosition, player1,
                                (tpQueened || ((theirEndPos / 10) - 2 == 0)), sequence, possibleMoves);
                    }

                    boardArr[takenPosition + 11] = pieceThere;
                }
                // taking down and right
                if (takenPosition + 18 < 80 && (takenPosition + 18) % 10 > 0 && checkValidPiece(!player1, takenPosition + 9) &&
                        checkValidPiece(player1, takenPosition + 18)) {
                    int takingPiece = boardArr[takenPosition + 18];
                    int pieceThere = boardArr[takenPosition + 9];
                    boardArr[takenPosition + 9] = 0;

                    boolean tpQueened = pieceQueened[takingPiece - 1];

                    if ((!player1 || tpQueened)) {
                        int[] sequence = new int[] {takenPosition + 18, takenPosition};
                        possibleMoves.add(sequence);
                        generateTakes(takenPosition, player1,
                                (tpQueened || ((theirEndPos / 10) - 2 == 0)), sequence, possibleMoves);
                    }

                    boardArr[takenPosition + 9] = pieceThere;
                }
                // taking up and right
                if (takenPosition - 22 > 0 && (takenPosition - 22) % 10 > 0 && checkValidPiece(!player1, takenPosition - 11) &&
                        checkValidPiece(player1, takenPosition - 22)) {
                    int takingPiece = boardArr[takenPosition - 22];
                    int pieceThere = boardArr[takenPosition - 11];
                    boardArr[takenPosition - 11] = 0;

                    boolean tpQueened = pieceQueened[takingPiece - 1];

                    if ((player1 || tpQueened)) {
                        int[] sequence = new int[]{takenPosition - 22, takenPosition};
                        possibleMoves.add(sequence);
                        generateTakes(takenPosition, player1,
                                (tpQueened || ((theirEndPos / 10) + 2 == 7)), sequence, possibleMoves);
                    }

                    boardArr[takenPosition - 11] = pieceThere;
                }
                // taking up and left
                if (takenPosition - 18 > 0 && (takenPosition - 18) % 10 < 7 && checkValidPiece(!player1, takenPosition - 9) &&
                        checkValidPiece(player1, takenPosition - 18)) {
                    int takingPiece = boardArr[takenPosition - 18];
                    int pieceThere = boardArr[takenPosition - 9];
                    boardArr[takenPosition - 9] = 0;

                    boolean tpQueened = pieceQueened[takingPiece - 1];

                    if ((player1 || tpQueened)) {
                        int[] sequence = new int[]{takenPosition - 18, takenPosition};
                        possibleMoves.add(sequence);
                        generateTakes(takenPosition, player1,
                                (tpQueened || ((theirEndPos / 10) + 2 == 7)), sequence, possibleMoves);
                    }

                    boardArr[takenPosition - 9] = pieceThere;
                }
            }
        }
            // our move
        if (Math.abs(ourMove[1] - ourStartPos) > 17) {      // takes
            for (int i = 0; i < ourMove.length - 2; i ++) {
                int takenPosition = (ourMove[i] + ourMove[i + 1])/2;
                if (takenPosition != theirEndPos) {
                    checkAroundSpace(possibleMoves, takenPosition, player1,
                            takenPosition % 10 < 7, takenPosition % 10 > 0, takenPosition < 70, takenPosition > 9);
                }

                // did this open an opportunity to take TODO look into consolidating this stuff into a function please.
                if (takenPosition != theirEndPos) {
                    // taking down and left
                    if (takenPosition + 22 < 80 && (takenPosition + 22) % 10 < 8 && checkValidPiece(!player1, takenPosition + 11) &&
                            checkValidPiece(player1, takenPosition + 22)) {
                        int takingPiece = boardArr[takenPosition + 22];
                        int pieceThere = boardArr[takenPosition + 11];
                        boardArr[takenPosition + 11] = 0;

                        boolean tpQueened = pieceQueened[takingPiece - 1];

                        if ((!player1 || tpQueened)) {
                            int[] sequence = new int[]{takenPosition + 22, takenPosition};
                            possibleMoves.add(sequence);
                            generateTakes(takenPosition, player1,
                                    (tpQueened || ((theirEndPos / 10) - 2 == 0)), sequence, possibleMoves);
                        }

                        boardArr[takenPosition + 11] = pieceThere;
                    }
                    // taking down and right
                    if (takenPosition + 18 < 80 && (takenPosition + 18) % 10 > 0 && checkValidPiece(!player1, takenPosition + 9) &&
                            checkValidPiece(player1, takenPosition + 18)) {
                        int takingPiece = boardArr[takenPosition + 18];
                        int pieceThere = boardArr[takenPosition + 9];
                        boardArr[takenPosition + 9] = 0;

                        boolean tpQueened = pieceQueened[takingPiece - 1];

                        if ((!player1 || tpQueened)) {
                            int[] sequence = new int[]{takenPosition + 18, takenPosition};
                            possibleMoves.add(sequence);
                            generateTakes(takenPosition, player1,
                                    (tpQueened || ((theirEndPos / 10) - 2 == 0)), sequence, possibleMoves);
                        }

                        boardArr[takenPosition + 9] = pieceThere;
                    }
                    // taking up and right
                    if (takenPosition - 22 > 0 && (takenPosition - 22) % 10 > 0 && checkValidPiece(!player1, takenPosition - 11) &&
                            checkValidPiece(player1, takenPosition - 22)) {
                        int takingPiece = boardArr[takenPosition - 22];
                        int pieceThere = boardArr[takenPosition - 11];
                        boardArr[takenPosition - 11] = 0;

                        boolean tpQueened = pieceQueened[takingPiece - 1];

                        if ((player1 || tpQueened)) {
                            int[] sequence = new int[]{takenPosition - 22, takenPosition};
                            possibleMoves.add(sequence);
                            generateTakes(takenPosition, player1,
                                    (tpQueened || ((theirEndPos / 10) + 2 == 7)), sequence, possibleMoves);
                        }

                        boardArr[takenPosition - 11] = pieceThere;
                    }
                    // taking up and left
                    if (takenPosition - 18 > 0 && (takenPosition - 18) % 10 < 7 && checkValidPiece(!player1, takenPosition - 9) &&
                            checkValidPiece(player1, takenPosition - 18)) {
                        int takingPiece = boardArr[takenPosition - 18];
                        int pieceThere = boardArr[takenPosition - 9];
                        boardArr[takenPosition - 9] = 0;

                        boolean tpQueened = pieceQueened[takingPiece - 1];

                        if ((player1 || tpQueened)) {
                            int[] sequence = new int[]{takenPosition - 18, takenPosition};
                            possibleMoves.add(sequence);
                            generateTakes(takenPosition, player1,
                                    (tpQueened || ((theirEndPos / 10) + 2 == 7)), sequence, possibleMoves);
                        }

                        boardArr[takenPosition - 9] = pieceThere;
                    }
                }
            }
        }

        // generate moves for the recently moved piece
        if (ourPieceAlive) {
            generatePieceMoves(ourEndPos, player1, pieceQueened[boardArr[ourEndPos] - 1], possibleMoves);
        }

        // add takes that are now available
        // (hmm ... maybe use generate takes but only do it for moves that end on the four possible squares
        // (maybe add extra condition for queened, look at generate takes function))


        // taking their piece that ended there
        if (theirEndPos > 9 && theirEndPos < 70 && theirEndPos % 10 > 0 && theirEndPos % 10 < 7) {

            int pieceThere = boardArr[theirEndPos];
            boardArr[theirEndPos] = 0;

            boolean tpQueened;

            int downLeft = boardArr[theirEndPos - 11];
            int downRight = boardArr[theirEndPos - 9];
            int upLeft = boardArr[theirEndPos + 9];
            int upRight = boardArr[theirEndPos + 11];

            if (downLeft == 0 && checkValidPiece(player1, upRight)) {           // take down and left
                tpQueened = pieceQueened[upRight - 1];

                if ((!player1 || tpQueened)) {
                    int[] sequence = new int[]{theirEndPos + 11, theirEndPos - 11};
                    possibleMoves.add(sequence);
                    generateTakes(theirEndPos - 11, player1,
                            (tpQueened || ((theirEndPos / 10) - 2 == 0)), sequence, possibleMoves);
                }

            } else if (upRight == 0 && checkValidPiece(player1, downLeft)) {    // take up and right
                tpQueened = pieceQueened[downLeft - 1];

                if ((player1 || tpQueened)) {
                    int[] sequence = new int[]{theirEndPos - 11, theirEndPos + 11};
                    possibleMoves.add(sequence);
                    generateTakes(theirEndPos + 11, player1,
                            (tpQueened || ((theirEndPos / 10) + 2 == 7)), sequence, possibleMoves);
                }
            }
            if (downRight == 0 && checkValidPiece(player1, upLeft)) {           // take down and right
                tpQueened = pieceQueened[upLeft - 1];

                if ((!player1 || tpQueened)) {
                    int[] sequence = new int[]{theirEndPos + 9, theirEndPos - 9};
                    possibleMoves.add(sequence);
                    generateTakes(theirEndPos - 9, player1,
                            (tpQueened || ((theirEndPos / 10) - 2 == 0)), sequence, possibleMoves);
                }
            } else if (upLeft == 0 && checkValidPiece(player1, downRight)) {    // take up and left
                tpQueened = pieceQueened[downRight - 1];

                if ((player1 || tpQueened)) {
                    int[] sequence = new int[] {theirEndPos - 9, theirEndPos + 9};
                    possibleMoves.add(sequence);
                    generateTakes(theirEndPos + 11, player1,
                            (tpQueened || ((theirEndPos / 10) + 2 == 7)), sequence, possibleMoves);
                }
            }

            boardArr[theirEndPos] = pieceThere;

        }
        // taking their piece thanks to a space opening up because one of us moved our piece (and they didn't move to where we did)
        // this should be the same thing that i've coded like 16 times already so *please* just make a function

        return possibleMoves;
    }

    private void checkAroundSpace(LinkedList<int[]> possibleMoves, int emptySpace, boolean player1,
                                  boolean notRightCol, boolean notLeftCol, boolean notTopRow, boolean notBottomRow) {
        int pieceThere;

        if (notBottomRow && notLeftCol) {     // their, checking from down and left
            pieceThere = boardArr[emptySpace - 11];
            if ((!player1 || pieceQueened[pieceThere - 1]) && checkValidPiece(player1, pieceThere)) {
                possibleMoves.add(new int[]{emptySpace - 11, emptySpace});
            }
        }
        if (notBottomRow && notRightCol) {     // their, checking from down and right
            pieceThere = boardArr[emptySpace - 9];
            if ((!player1 || pieceQueened[pieceThere - 1]) && checkValidPiece(player1, pieceThere)) {
                possibleMoves.add(new int[]{emptySpace - 9, emptySpace});
            }
        }
        if (notTopRow && notLeftCol) {      // their, checking from up and left
            pieceThere = boardArr[emptySpace + 9];
            if ((player1 || pieceQueened[pieceThere - 1]) && checkValidPiece(player1, pieceThere)) {
                possibleMoves.add(new int[]{emptySpace + 9, emptySpace});
            }
        }
        if (notTopRow && notRightCol) {      // their, checking from up and right
            pieceThere = boardArr[emptySpace + 11];
            if ((player1 || pieceQueened[pieceThere - 1]) && checkValidPiece(player1, pieceThere)) {
                possibleMoves.add(new int[] {emptySpace + 9, emptySpace});
            }
        }
    }

    private boolean checkValidPiece(boolean player1, int pieceThere) {
        return (player1 && pieceThere != 0 && pieceThere < 13) || (!player1 && pieceThere > 13);
    }

    public LinkedList<int[]> generateMoves(boolean player1) {
        LinkedList<int[]> possibleMoves = new LinkedList<>();

        int startIndex;
        int position;
        if (player1) {
            startIndex = 0;
        } else {
            startIndex = 12;
        }

        for (int i = startIndex; i < startIndex + 12; i ++) {  // loop through the pieces
            position = piecePositions[i];
            boolean queened = pieceQueened[i];

            if (position != -1) {
                generatePieceMoves(position, player1, queened, possibleMoves);
            }
        }

        return possibleMoves;
    }

    private void generatePieceMoves(int position, boolean player1, boolean queened, LinkedList<int[]> pieceMoves) {

        int row = position / 10;
        int col = position % 10;

        int pieceThere;

        // - 11; down and left
        if ((!player1 || queened) && (row > 0 && col > 0)) {
            // in a position to move down/left
            pieceThere = boardArr[position - 11];

            if (pieceThere == 0) {  // move there
                pieceMoves.add(new int[] {position, position - 11});
            } else if (((pieceThere < 13) != player1) && row > 1 && col > 1 && boardArr[position - 22] == 0) {  // takes, add all
                boardArr[position - 11] = 0;
                int[] sequence = new int[] {position, position - 22};

                pieceMoves.add(sequence);
                generateTakes(position - 22, player1, (queened || row - 2 == 0), sequence, pieceMoves);

                boardArr[position - 11] = pieceThere;
            }
        }
        // - 9; down and right
        if ((!player1 || queened) && (row > 0 && col < 7)) {
            pieceThere = boardArr[position - 9];

            if (pieceThere == 0) {
                pieceMoves.add(new int[] {position, position - 9});
            } else if (((pieceThere < 13) != player1) && row > 1 && col < 6 && boardArr[position - 18] == 0) {
                boardArr[position - 9] = 0;
                int[] sequence = new int[] {position, position - 18};

                pieceMoves.add(sequence);
                generateTakes(position - 18, player1, (queened || row - 2 == 0), sequence, pieceMoves);

                boardArr[position - 9] = pieceThere;
            }
        }
        // + 11; up and right
        if ((player1 || queened) && row < 7 && col < 7) {
            pieceThere = boardArr[position + 11];

            if (pieceThere == 0) {
                pieceMoves.add(new int[] {position, position + 11});
            } else if (((pieceThere < 13) != player1) && row < 6 && col < 6 && boardArr[position + 22] == 0) {
                boardArr[position + 11] = 0;
                int[] sequence = new int[] {position, position + 22};

                pieceMoves.add(sequence);
                generateTakes(position + 22, player1, (queened || row + 2 == 7), sequence, pieceMoves);

                boardArr[position + 11] = pieceThere;
            }
        }
        // + 9; up and left
        if ((player1 || queened) && (row < 7 && col > 0)) {
            pieceThere = boardArr[position + 9];

            if (pieceThere == 0) {
                pieceMoves.add(new int[] {position, position + 9});
            } else if (((pieceThere < 13) != player1) && row < 6 && col > 1 && boardArr[position + 18] == 0) {
                boardArr[position + 9] = 0;
                int[] sequence = new int[] {position, position + 18};

                pieceMoves.add(sequence);
                generateTakes(position + 18, player1, (queened || row + 2 == 7), sequence, pieceMoves);

                boardArr[position + 9] = pieceThere;
            }
        }
    }

    private void generateTakes(int position, boolean player1, boolean queened, int[] currentSequence, LinkedList<int[]> possibleSequences) {

        int row = position / 10;
        int col = position % 10;

        int pieceTaken;
        int[] newSequence = new int[currentSequence.length + 1];
        System.arraycopy(currentSequence, 0, newSequence, 0, currentSequence.length);

        // down, left
        if ((!player1 || queened) && (row > 1 && col > 1) && boardArr[position - 22] == 0) {
            pieceTaken = boardArr[position - 11];
            if ((pieceTaken != 0) && (pieceTaken < 13 == !player1)) {
                int[] thisSequence = newSequence.clone();
                thisSequence[thisSequence.length - 1] = position - 22;
                possibleSequences.add(thisSequence);

                boardArr[position - 11] = 0;

                generateTakes(position - 22, player1, (queened || row - 2 == 0), thisSequence, possibleSequences);

                boardArr[position - 11] = pieceTaken;
            }
        }

        // down, right
        if ((!player1 || queened) && (row > 1 && col < 6) && boardArr[position - 18] == 0) {
            pieceTaken = boardArr[position - 9];
            if ((pieceTaken != 0) && (pieceTaken < 13 == !player1)) {
                int[] thisSequence = newSequence.clone();
                thisSequence[thisSequence.length - 1] = position - 18;
                possibleSequences.add(thisSequence);

                boardArr[position - 9] = 0;

                generateTakes(position - 18, player1, (queened || row - 2 == 0), thisSequence, possibleSequences);

                boardArr[position - 9] = pieceTaken;
            }
        }

        // up, right
        if ((player1 || queened) && (row < 6 && col < 6) && boardArr[position + 22] == 0) {
            pieceTaken = boardArr[position + 11];
            if ((pieceTaken != 0) && (pieceTaken < 13 == !player1)) {
                int[] thisSequence = newSequence.clone();
                thisSequence[thisSequence.length - 1] = position + 22;
                possibleSequences.add(thisSequence);

                boardArr[position + 11] = 0;

                generateTakes(position + 22, player1, (queened || row + 2 == 7), thisSequence, possibleSequences);

                boardArr[position + 11] = pieceTaken;
            }
        }

        // up, left
        if ((player1 || queened) && (row < 6 && col > 1) && boardArr[position + 18] == 0) {
            pieceTaken = boardArr[position + 9];
            if (pieceTaken != 0 && pieceTaken < 13 == !player1) {
                int[] thisSequence = newSequence.clone();
                thisSequence[thisSequence.length - 1] = position + 18;
                possibleSequences.add(thisSequence);

                boardArr[position + 9] = 0;

                generateTakes(position + 18, player1, (queened || row + 2 == 7), thisSequence, possibleSequences);

                boardArr[position + 9] = pieceTaken;
            }
        }

    }

    public FastBoard copyBoard() {
        return new FastBoard(this.boardArr.clone(), this.piecePositions.clone(),
                this.pieceQueened.clone(), this.numPlayerOnePieces, this.numPlayerTwoPieces, this.numMovesPlayed);
    }

    public long encodeBoard() {
        long startTime = System.nanoTime();
        long boardValue = 0;

        for (int rowNum = 0; rowNum < 8; rowNum ++) {
            int startIndex;
            if (rowNum % 2 == 0) {
                startIndex = 0;
            } else {
                startIndex = 1;
            }

            for (int colCount = 0; colCount < 4; colCount += 1) {
                int piece = boardArr[10 * rowNum + 2 * colCount + startIndex];

                if (piece != 0) {
                    if (piece < 13) {   // p1 piece
                        boardValue += Math.pow(3, (4 * rowNum + colCount));
                    } else {            // p2 piece
                        boardValue += 2 * Math.pow(3, (4 * rowNum + colCount));
                    }
                }
            }

        }

        boardValue *= 100;
        boardValue += numQueens;

        long timeElapsed = System.nanoTime() - startTime;
        // System.out.println("Traversing board. Time: " + timeElapsed + " ns.");

        return boardValue;
    }

    public long encodeBoardTwo() {
        long startTime = System.nanoTime();
        long boardValue = 0;

        int index = 0;
        for (int position : piecePositions) {
            int row = position / 10;
            int col = position % 10;

            if (row % 2 == 1) {
                col -= 1;
            }
            col = col / 2;


            long toAdd = (long) Math.pow(3, (4 * row + col));
            if (index > 11) {
                toAdd *= 2;
            }

            boardValue += toAdd;

            index ++;
        }

        boardValue *= 100;
        boardValue += numQueens;


        long timeElapsed = System.nanoTime() - startTime;
        // System.out.println("Traversing piecePositions. Time: " + timeElapsed + " ns.");
        return boardValue;
    }

    public void printBoard(boolean player1, int[] move) {
        for (int i = 0; i < 8; i ++) {
            // get row
            int rowNum;
            if (player1) {
                rowNum = 8 - i - 1;
                System.out.print((8 - i) + " ");
            } else {
                rowNum = i;
                System.out.print((i + 1) + " ");
            }

            // print
            System.out.print("|");
            for (int spot = 0; spot < 8; spot ++) {

                int piece;
                char spaceChar;
                if (player1) {
                    piece = boardArr[10 * rowNum + spot];

                    if (containsPosition(move, 10 * rowNum + spot)) {
                        spaceChar = '~';
                    } else {
                        spaceChar = ' ';
                    }
                } else {
                    piece = boardArr[10 * rowNum + 8 - spot - 1];

                    if (containsPosition(move, 10 * rowNum + 8 - spot - 1)) {
                        spaceChar = '~';
                    } else {
                        spaceChar = ' ';
                    }
                }

                System.out.print(spaceChar);

                // piece
                if (piece == 0) {
                    System.out.print(" ");
                } else if (piece < 13) {
                    if (pieceQueened[piece - 1]) {
                        System.out.print("K");
                    } else {
                        System.out.print("X");
                    }
                } else {
                    if (pieceQueened[piece - 1]) {
                        System.out.print("Q");
                    } else {
                        System.out.print("O");
                    }
                }

                // second space
                System.out.print(spaceChar);

                // separator
                System.out.print("|");
            }
            System.out.println();
        }

        if (player1) {
            System.out.println("    A   B   C   D   E   F   G   H ");
        } else {
            System.out.println("    H   G   F   E   D   C   B   A ");
        }
    }

    private boolean containsPosition(int[] move, int position) {
        for (int pos : move) {
            if (pos == position) {
                return true;
            }
        }
        return false;
    }
}
