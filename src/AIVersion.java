import java.util.Dictionary;
import java.util.LinkedList;
import java.util.Set;

public class AIVersion extends AIPlayer {
    int version;

    public AIVersion(boolean player1, Board mainBoard, int playerDepth, int version) {
        super(player1, mainBoard, playerDepth);
        this.version = version;
    }

    @Override
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
        long boardCode = 0;
        if (version >= 2) {
            boardCode = board.encodeBoard();
        }

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

            // V2 Hashing
            if (version >= 2 && computedStages.contains(boardCode)) {
                moveScore = positionScores.get(boardCode);
                cacheHits[depth - 1] += 1;
            } else {
                moveScore = minimax(board, depth - 1, !maximize, maxGuarantee, minGuarantee, computedStages, positionScores).score;
            }

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

            // V3 alpha / beta pruning
            if (version >= 3) {
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
            }

            // System.out.println("Alpha: " + maxGuarantee + ", Beta: " + minGuarantee + ", NOT breaking.");
        }

        // V2 hashing add to cache
        if (version >= 2) {
            computedStages.add(boardCode);
            positionScores.put(boardCode, bestMove.score);
        }


        return bestMove;
    }
}
