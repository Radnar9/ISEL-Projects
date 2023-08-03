package pdm.chessroyale.models

import pdm.chessroyale.models.pieces.*

private val kings: MutableList<Position> = mutableListOf()

/**
 * Only usage is to save unnecessary code
 */
class InitChessBoard(
    private val chessBoard: HashMap<Int, MutableList<Piece>>,
    private val piecesMap: HashMap<Color, MutableList<Piece>>
) {
    // Piece: P(pawn), R(rook), N(knight), B(bishop), Q(Queen), K(King), S(space)

    init {
        val initialPieceLine = arrayOf('R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R')
        for (line in 1..TABLE_SIZE) {
            chessBoard[line] = mutableListOf()
            if (line == 2 || line == 7) {
                for (col in 0 until TABLE_SIZE) {
                    addPiece('P', Position(line, col))
                }
            } else if (line in 3..6) {
                for (col in 0 until TABLE_SIZE) {
                    addPiece('S', Position(line, col))
                }
            } else {
                initialPieceLine.forEachIndexed { col, piece ->
                    addPiece(piece, Position(line, col))
                }
            }
        }
    }

    fun getKings(): MutableList<Position> {
        return kings
    }

    private fun addPiece(piece: Char, position: Position) {
        val line = position.line
        val col = position.column
        val color = if (line < 3) Color.WHITE else Color.BLACK
        when (piece) {
            'R' -> add(line, Rook(Position(line, col), color))
            'N' -> add(line, Knight(Position(line, col), color))
            'B' -> add(line, Bishop(Position(line, col), color))
            'Q' -> add(line, Queen(Position(line, col), color))
            'K' -> {
                val pos = Position(line, col)
                add(line, King(pos, color))
                kings.add(pos)
            }
            'P' -> add(line, Pawn(Position(line, col), color))
            'S' -> add(line, Space(Position(line, col)))
        }
    }

    private fun add(line: Int, piece: Piece) {
        chessBoard[line]?.add(piece)
        piecesMap[piece.getColor()]?.add(piece)
    }
}