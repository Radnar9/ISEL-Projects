package pdm.chessroyale.models.pieces

import pdm.chessroyale.models.Color
import pdm.chessroyale.R

class Pawn(var pos: Position, private val color: Color) : Piece() {

    override fun getDrawable(): Int {
        return if (color == Color.WHITE) R.drawable.ic_white_pawn else R.drawable.ic_black_pawn
    }

    override fun getColor(): Color {
        return color
    }

    override fun getValidMovesPositions(): MutableList<Position> {
        val list: MutableList<Position> = mutableListOf()
        if (color == Color.WHITE) {
            list.add(Position(pos.line + 1, pos.column))
            if (historyPositions.isEmpty()) {
                list.add(Position(pos.line + 2, pos.column))
            }
        } else if (color == Color.BLACK) {
            list.add(Position(pos.line - 1, pos.column))
            if (historyPositions.isEmpty()) {
                list.add(Position(pos.line - 2, pos.column))
            }
        }
        return list
    }

    override fun canMoveTo(pos: Position): Boolean {
        getValidMovesPositions().forEach {
            if (it.line == pos.line && it.column == pos.column) {
                return true
            }
        }
        return false
    }

    override fun setPosition(position: Position) {
        historyPositions.add(this.pos)
        this.pos = position
    }

    override fun getPosition(): Position {
        return pos
    }

    // Pawn is the only piece that moves differently in capturing and in normal movement
    fun canCapture(line: Int, column: Int): Boolean {
        val diagonalCaptureMove = pos.column == column + 1 || pos.column == column - 1
        if (color == Color.WHITE) {
            return pos.line + 1 == line && diagonalCaptureMove
        } else if (color == Color.BLACK) {
            return pos.line - 1 == line && diagonalCaptureMove
        }
        return false
    }
}
