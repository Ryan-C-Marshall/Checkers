import java.util.*;

public class AIPlayer extends Player {
    int playerDepth;

    int[] movesExplored;
    int[] cacheHits;
    int[] prunes;

    public AIPlayer(boolean player1, Board mainBoard, int playerDepth) {
        super(player1, mainBoard);
        this.playerDepth = playerDepth;
    }

    @Override
    public int[] getMove() {
        this.movesExplored = new int[playerDepth];
        this.cacheHits = new int[playerDepth];
        this.prunes = new int[playerDepth];
        long time = System.currentTimeMillis();

        // make a copy of the board
        FastBoard operationalBoard = board.copyBoard();

        // create cache
        Set<Long> computedStates = new HashSet<>();
        Dictionary<Long, Integer> positionScores;
        positionScores = new Hashtable<>();  // new Hashtable<>((int) Math.pow(5, playerDepth));
        // do minimax
        int[] move = minimax(
                operationalBoard,
                playerDepth,
                this.player1,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                computedStates,
                positionScores
        ).move;


        time = System.currentTimeMillis() - time;

        for (int i = 0; i < move.length; i ++) {
            System.out.print(move[i]);
            if (i != move.length - 1){
                System.out.print(" - ");
            }
        }
        System.out.println("\n");
        System.out.println(time);
        System.out.println("\n");
        for (int i = movesExplored.length - 1; i >= 0; i --) {
            System.out.println(movesExplored[i]);
        }
        System.out.println();
        for (int i = cacheHits.length - 1; i >= 0; i --) {
            System.out.println(cacheHits[i]);
        }
        System.out.println();
        for (int i = prunes.length - 1; i >= 0; i --) {
            System.out.println(prunes[i]);
        }

        return move;
    }

    // returns [score, move[0], move[1], ...]
    // TODO different move gen (updates instead of recalculates) iterative calculation (move order), variable depth, precalculation
    protected MiniMove minimax(FastBoard board, int depth, boolean maximize, int maxGuarantee, int minGuarantee,
                             Set<Long> computedStages, Dictionary<Long, Integer> positionScores) {
        int moveScore;

        LinkedList<int[]> allMoves = board.generateMoves(maximize);

        // if win
        if (allMoves.isEmpty()) {   // this player lost
            if (maximize) {
                return new MiniMove(null, -10 * (100 - board.numMovesPlayed));
            } else {
                return new MiniMove(null, 10 * (100 - board.numMovesPlayed));
            }
        } else if (depth == 0) {    // no more depth
            return new MiniMove(null, evaluate(board));
        }

        // else:
        MiniMove bestMove = new MiniMove();
        if (maximize) {
            bestMove.score = Integer.MIN_VALUE;
        } else {
            bestMove.score = Integer.MAX_VALUE;
        }

        // V2 Hashing
        // FIXME long boardCode = board.encodeBoard();

        /*
        if (depth == playerDepth) {
            for (int[] move : allMoves) {
                System.out.print(Arrays.toString(move) + ", ");
            }
            System.out.println();
        }
         */

        for (int[] move : allMoves) {
            movesExplored[depth - 1] += 1;

            // complete move
            boolean[] ogQueened = board.pieceQueened.clone();
            int ogNumQueened = board.numQueens;
            int[] takenCoords = board.makeMove(move);

            // get score

            // V2 Hashing FIXME
            /*
            if (computedStages.contains(boardCode)) {
                moveScore = positionScores.get(boardCode);
                cacheHits[depth - 1] += 1;
            } else
             {
             */
                moveScore = minimax(board, depth - 1, !maximize, maxGuarantee, minGuarantee, computedStages, positionScores).score;
            // FIXME }

            // undo move
            board.undoMove(move, takenCoords, ogQueened, ogNumQueened);

            // update best move
            if ((maximize && moveScore > bestMove.score) || (!maximize && moveScore < bestMove.score)) {
                if (depth == playerDepth) {
                    bestMove.move = move;
                }
                bestMove.score = moveScore;
            } else if (depth == playerDepth && moveScore == bestMove.score) {
                if (Math.abs((move[move.length - 1] % 10) - 3) < Math.abs((bestMove.move[bestMove.move.length - 1] % 10) - 3)) {  // prioritize moves to the middle
                    bestMove.move = move;
                }
            }

            // System.out.println("Move = " + Arrays.toString(move) + ", depth = " + depth + ", score = " + moveScore);
            // System.out.println("Maximize: " + maximize + "\nAlpha: " + maxGuarantee + "\nBeta: " + minGuarantee);

            // V3 alpha / beta pruning FIXME
            /*
            if ((maximize && moveScore > maxGuarantee)) {
                maxGuarantee = moveScore;
                if (minGuarantee < maxGuarantee) {  // min player can guarantee a lower score elsewhere, so they'll never choose this path.
                    // System.out.println("Alpha: " + maxGuarantee + ", Beta: " + minGuarantee + ", breaking.");
                    prunes[depth - 1] += 1;
                    break;
                }
            } else if (!maximize && moveScore < minGuarantee) {
                minGuarantee = moveScore;
                if (maxGuarantee > minGuarantee) { // max player can guarantee a higher score elsewhere, so they'll never choose this path.
                    // System.out.println("Alpha: " + maxGuarantee + ", Beta: " + minGuarantee + ", breaking.");
                    prunes[depth - 1] += 1;
                    break;
                }
            }

             */

            // System.out.println("Alpha: " + maxGuarantee + ", Beta: " + minGuarantee + ", NOT breaking.");
        }

        // V2 hashing add to cache FIXME
        /*
        computedStages.add(boardCode);
        positionScores.put(boardCode, bestMove.score);

         */

        return bestMove;
    }

    protected int evaluate(FastBoard board) {
        return board.numPlayerOnePieces - board.numPlayerTwoPieces;
    }

}
