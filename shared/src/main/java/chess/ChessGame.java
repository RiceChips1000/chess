package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board = new ChessBoard();
    private TeamColor teamTurn = TeamColor.WHITE;

    public ChessGame() {

    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;
    }


/**
 * Enum identifying the 2 possible teams in a chess game
 */
    public enum TeamColor {
        WHITE,
        BLACK
    }

/**
 * Gets a valid moves for a piece at the given location
 *
 * @param startPosition the piece to get valid moves for
 * @return Set of valid moves for requested piece, or null if no piece at
 * startPosition
 */
public Collection<ChessMove> validMoves(ChessPosition startPosition) {
    ChessPiece piece = board.getPiece(startPosition);
    if(piece == null) {
        return null;
    }
    return piece.pieceMoves(board, startPosition);
}

/**
 * Makes a move in a chess game
 *
 * @param move chess move to perform
 * @throws InvalidMoveException if move is invalid
 */
public void makeMove(ChessMove move) throws InvalidMoveException {
    ChessPosition start = move.getStartPosition();
    ChessPosition end = move.getEndPosition();

    ChessPiece piece = board.getPiece(start);



    if(piece == null) {
        throw new InvalidMoveException("No piece at start position");
    }

    // Check if it's the correct team's turn
    if(piece.getTeamColor() != teamTurn) {
        throw new InvalidMoveException("Not your turn");
    }

    // Check if the move is valid
    Collection<ChessMove> valid = validMoves(start);
    if(valid == null || !valid.contains(move)) {
        throw new InvalidMoveException("Invalid move");
    }

    // Execute the move
    board.addPiece(start, null); // Remove piece from start

    // Handle pawn promotion
    if(move.getPromotionPiece() != null) {
        board.addPiece(end, new ChessPiece(piece.getTeamColor(), move.getPromotionPiece()));
    }   else {
        board.addPiece(end, piece);
    }


    teamTurn = (teamTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE; // should be switching the teams
}

/**
 * Determines if the given team is in check
 *
 * @param teamColor which team to check for check
 * @return True if the specified team is in check
 */
public boolean isInCheck(TeamColor teamColor) {
    throw new RuntimeException("Not implemented");
}

/**
 * Determines if the given team is in checkmate
 *
 * @param teamColor which team to check for checkmate
 * @return True if the specified team is in checkmate
 */
public boolean isInCheckmate(TeamColor teamColor) {
    throw new RuntimeException("Not implemented");
}

/**
 * Determines if the given team is in stalemate, which here is defined as having
 * no valid moves while not in check.
 *
 * @param teamColor which team to check for stalemate
 * @return True if the specified team is in stalemate, otherwise false
 */
public boolean isInStalemate(TeamColor teamColor) {
    throw new RuntimeException("Not implemented");
}

/**
 * Sets this game's chessboard with a given board
 *
 * @param board the new board to use
 */
public void setBoard(ChessBoard board) {
    throw new RuntimeException("Not implemented");
}

/**
 * Gets the current chessboard
 *
 * @return the chessboard
 */
public ChessBoard getBoard() {
    throw new RuntimeException("Not implemented");
}
}



