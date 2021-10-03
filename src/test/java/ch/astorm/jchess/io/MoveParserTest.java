
package ch.astorm.jchess.io;

import ch.astorm.jchess.JChessGame;
import ch.astorm.jchess.JChessGame.Status;
import ch.astorm.jchess.core.Color;
import ch.astorm.jchess.core.Position;
import ch.astorm.jchess.core.entities.Bishop;
import ch.astorm.jchess.core.entities.King;
import ch.astorm.jchess.core.entities.Pawn;
import ch.astorm.jchess.io.MoveParser.InvalidMoveException;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class MoveParserTest {

    @Test
    public void testSimpleGame() {
        JChessGame game = JChessGame.newGame();
        MoveParser parser = new MoveParser(game);
        assertEquals(Status.NOT_FINISHED, parser.doMove("f3"));
        assertEquals(Status.NOT_FINISHED, parser.doMove("e5"));
        assertEquals(Status.NOT_FINISHED, parser.doMove("g4"));
        assertEquals(Status.WIN_BLACK, parser.doMove("Qh4"));
        assertEquals(0, game.getAvailableMoves().size());
        assertThrows(IllegalStateException.class, () -> game.apply(null));
    }

    @Test
    public void testDrawnGame() {
        JChessGame game = JChessGame.newGame();
        MoveParser parser = new MoveParser(game);

        /*
        1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 {This opening is called the Ruy Lopez.}
        4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 8. c3 O-O 9. h3 Nb8 10. d4 Nbd7
        11. c4 c6 12. cxb5 axb5 13. Nc3 Bb7 14. Bg5 b4 15. Nb1 h6 16. Bh4 c5 17. dxe5
        Nxe4 18. Bxe7 Qxe7 19. exd6 Qf6 20. Nbd2 Nxd6 21. Nc4 Nxc4 22. Bxc4 Nb6
        23. Ne5 Rae8 24. Bxf7+ Rxf7 25. Nxf7 Rxe1+ 26. Qxe1 Kxf7 27. Qe3 Qg5 28. Qxg5
        hxg5 29. b3 Ke6 30. a3 Kd6 31. axb4 cxb4 32. Ra5 Nd5 33. f3 Bc8 34. Kf2 Bf5
        35. Ra7 g6 36. Ra6+ Kc5 37. Ke1 Nf4 38. g3 Nxh3 39. Kd2 Kb5 40. Rd6 Kc5 41. Ra6
        Nf2 42. g4 Bd3 43. Re6
        */
        parser.doMoves(Arrays.asList("e4","e5","Nf3","Nc6","Bb5","a6","Ba4","Nf6","O-O","Be7",
                                     "Re1","b5","Bb3","d6","c3","O-O","h3","Nb8","d4","Nbd7",
                                     "c4","c6","cxb5","axb5","Nc3","Bb7","Bg5","b4","Nb1","h6",
                                     "Bh4","c5","dxe5","Nxe4","Bxe7","Qxe7","exd6","Qf6","Nbd2","Nxd6",
                                     "Nc4","Nxc4","Bxc4","Nb6","Ne5","Rae8","Bxf7+","Rxf7","Nxf7","Rxe1+",
                                     "Qxe1","Kxf7","Qe3","Qg5","Qxg5","hxg5","b3","Ke6","a3","Kd6",
                                     "axb4","cxb4","Ra5","Nd5","f3","Bc8","Kf2","Bf5","Ra7","g6",
                                     "Ra6+","Kc5","Ke1","Nf4","g3","Nxh3","Kd2","Kb5","Rd6","Kc5",
                                     "Ra6","Nf2","g4","Bd3","Re6"));
        game.draw();
        assertEquals(Status.DRAW, game.getStatus());
    }

    @Test
    public void testInvalidMove() {
        JChessGame game = JChessGame.newGame();
        MoveParser parser = new MoveParser(game);
        assertThrows(InvalidMoveException.class, () -> parser.doMove("e5"));
        assertThrows(InvalidMoveException.class, () -> parser.doMove("exd4"));
        assertThrows(InvalidMoveException.class, () -> parser.doMove("Nxc3"));
        assertThrows(InvalidMoveException.class, () -> parser.doMove("O-O"));
        assertThrows(InvalidMoveException.class, () -> parser.doMove("O-O-O"));
    }

    @Test
    public void testAmbiguousMove() {
        JChessGame game = JChessGame.newEmptyGame(Color.WHITE);
        Position position = game.getPosition();
        position.put(0, 7, new King(Color.WHITE));
        position.put(7, 7, new King(Color.BLACK));
        position.put(0, 0, new Bishop(Color.WHITE));
        position.put(2, 2, new Bishop(Color.WHITE));

        MoveParser parser = new MoveParser(game);
        assertThrows(InvalidMoveException.class, () -> parser.doMove("Bb2"));
        assertThrows(InvalidMoveException.class, () -> parser.doMove("Bbb2"));
        parser.doMove("Bab2");
    }

    @Test
    public void testPromotionMove() {
        JChessGame game = JChessGame.newEmptyGame(Color.WHITE);
        Position position = game.getPosition();
        position.put(0, 7, new King(Color.WHITE));
        position.put(6, 0, new Pawn(Color.WHITE));
        position.put(7, 7, new King(Color.BLACK));
        position.put(6, 6, new Pawn(Color.BLACK));
        position.put(6, 7, new Pawn(Color.BLACK));

        MoveParser parser = new MoveParser(game);
        assertThrows(IllegalStateException.class, () -> parser.doMove("a8"));
        assertEquals(Status.WIN_WHITE, parser.doMove("a8=Q"));
    }

    @Test
    public void testNonPromotionMove() {
        JChessGame game = JChessGame.newGame();
        MoveParser parser = new MoveParser(game);
        assertThrows(IllegalStateException.class, () -> parser.doMove("e4=Q"));
    }

    @Test
    public void testParseError() {
        JChessGame game = JChessGame.newGame();
        MoveParser parser = new MoveParser(game);
        assertThrows(IllegalArgumentException.class, () -> parser.doMove("z3"));
        assertThrows(IllegalArgumentException.class, () -> parser.doMove("Kz3"));
        assertThrows(IllegalArgumentException.class, () -> parser.doMove("b9"));
        assertThrows(IllegalArgumentException.class, () -> parser.doMove("Kb9"));
    }
}
