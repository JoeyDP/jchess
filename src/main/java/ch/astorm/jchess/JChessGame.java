
package ch.astorm.jchess;

import ch.astorm.jchess.core.Board;
import ch.astorm.jchess.core.Color;
import ch.astorm.jchess.core.Move;
import ch.astorm.jchess.core.Moveable;
import ch.astorm.jchess.core.Position;
import ch.astorm.jchess.core.entities.Bishop;
import ch.astorm.jchess.core.entities.King;
import ch.astorm.jchess.core.entities.Knight;
import ch.astorm.jchess.core.entities.Pawn;
import ch.astorm.jchess.core.entities.Queen;
import ch.astorm.jchess.core.entities.Rook;
import ch.astorm.jchess.core.rules.RuleManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Main entry point to create a chess game.
 */
public class JChessGame {
    private RuleManager ruleManager;
    private Position position;
    private Status status;
    private Map<String, String> metadata;

    /**
     * Final status of the game.
     */
    public static enum Status {
        /**
         * Game is not finished yet.
         */
        NOT_FINISHED(true),

        /**
         * White has won.
         */
        WIN_WHITE(false),

        /**
         * Black has won.
         */
        WIN_BLACK(false),

        /**
         * Draw (not enough material to mate).
         */
        DRAW(true),

        /**
         * Stalemate.
         */
        DRAW_STALEMATE(false),

        /**
         * Repetition of the same position.
         */
        DRAW_REPETITION(false),

        /**
         * No capture nor any pawn move in the last 75 moves.
         */
        DRAW_NOCAPTURE(false);

        private boolean allowPlay;
        private Status(boolean play) { this.allowPlay = play; }

        /**
         * Returns true if the game is allowed to play.
         */
        public boolean isPlayAllowed() { return allowPlay; }
    }

    /**
     * Creates a new chess game with the specified initial position.
     *
     * @param ruleManager The rule manager.
     * @param initPosition The initial position.
     * @param status The status of the game.
     */
    public JChessGame(RuleManager ruleManager, Position initPosition, Status status) {
        this.ruleManager = ruleManager;
        this.position = initPosition;
        this.status = status;
        this.metadata = new HashMap<>();
    }

    /**
     * Creates a new {@code JChessGame} with an uninitialized {@link Position} (empty
     * board).
     */
    public static JChessGame newEmptyGame(Color colorToPlay) {
        Board board = new Board();
        RuleManager chessRules = new RuleManager();
        Position position = new Position(board, chessRules, colorToPlay);
        return new JChessGame(chessRules, position, Status.NOT_FINISHED);
    }

    /**
     * Creates a new {@code JChessGame} with the standard initial position and {@link Color#WHITE}
     * on the move.
     */
    public static JChessGame newGame() {
        JChessGame game = newEmptyGame(Color.WHITE);
        setInitialPosition(game.getPosition());
        return game;
    }

    private static void setInitialPosition(Position position) {
        Board board = position.getBoard();

        position.put(0, 0, new Rook(Color.WHITE));
        position.put(0, 1, new Knight(Color.WHITE));
        position.put(0, 2, new Bishop(Color.WHITE));
        position.put(0, 3, new Queen(Color.WHITE));
        position.put(0, 4, new King(Color.WHITE));
        position.put(0, 5, new Bishop(Color.WHITE));
        position.put(0, 6, new Knight(Color.WHITE));
        position.put(0, 7, new Rook(Color.WHITE));

        for(int i=0 ; i<board.getColumnsCount() ; ++i) {
            position.put(1, i, new Pawn(Color.WHITE));
        }

        int topRow = board.getRowsCount()-1;
        position.put(topRow, 0, new Rook(Color.BLACK));
        position.put(topRow, 1, new Knight(Color.BLACK));
        position.put(topRow, 2, new Bishop(Color.BLACK));
        position.put(topRow, 3, new Queen(Color.BLACK));
        position.put(topRow, 4, new King(Color.BLACK));
        position.put(topRow, 5, new Bishop(Color.BLACK));
        position.put(topRow, 6, new Knight(Color.BLACK));
        position.put(topRow, 7, new Rook(Color.BLACK));

        for(int i=0 ; i<board.getColumnsCount() ; ++i) {
            position.put(topRow-1, i, new Pawn(Color.BLACK));
        }
    }

    /**
     * Returns the metadata of the game.
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Returns the rule manager.
     */
    public RuleManager getRuleManager() {
        return ruleManager;
    }

    /**
     * Returns the current position.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns the current game status.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the available legal moves in the position.
     */
    public List<Move> getAvailableMoves() {
        return position.getLegalMoves();
    }

    /**
     * Returns the available legal moves for the given {@code moveable}.
     */
    public List<Move> getAvailableMoves(Moveable moveable) {
        return getAvailableMoves().stream().
                filter(m -> m.getDisplacement().getMoveable()==moveable).
                collect(Collectors.toList());
    }

    /**
     * Applies the given {@code move} to the position and updates the game status.
     *
     * @param move The move to apply.
     * @return The new game status.
     */
    public Status apply(Move move) {
        if(!status.isPlayAllowed()) { throw new IllegalStateException("status is "+status); }
        position = position.apply(move);
        status = ruleManager.getEndgameStatus(position);
        return status;
    }

    /**
     * Returns back to the previous position and resets the game status accordingly.
     *
     * @return The current position.
     */
    public Position back() {
        Position current = position;
        position = position.getPreviousPosition();
        status = ruleManager.getEndgameStatus(position);
        return current;
    }

    /**
     * Returns the {@link Color} that has the move.
     */
    public Color getColorToMove() {
        return position.getColorToMove();
    }

    /**
     * Mark the game as a draw.
     */
    public void draw() {
        status = Status.DRAW;
    }

    /**
     * Makes the specified {@code resignedColor} to resign. The opposite color
     * will be marked as the winner.
     */
    public void resign(Color resignedColor) {
        status = resignedColor==Color.WHITE ? Status.WIN_BLACK : Status.WIN_WHITE;
    }
}
