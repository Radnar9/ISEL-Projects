package pdm.chessroyale.models.pieces

import pdm.chessroyale.models.Color
import pdm.chessroyale.R

class King(var pos: Position, private val color: Color) : Piece() {
    var isCheck = false

    override fun getDrawable(): Int {
        return if (color == Color.WHITE) R.drawable.ic_white_king else R.drawable.ic_black_king
    }

    override fun getColor(): Color {
        return color
    }

    override fun getValidMovesPositions(): MutableList<Position> {
        val list: MutableList<Position> = mutableListOf()
        val line = pos.line
        val column = pos.column
        list.add(Position(line - 1, column - 1))
        list.add(Position(line - 1, column))
        list.add(Position(line - 1, column + 1))
        list.add(Position(line, column - 1))
        list.add(Position(line, column + 1))
        list.add(Position(line + 1, column - 1))
        list.add(Position(line + 1, column))
        list.add(Position(line + 1, column + 1))
        return list
    }

    override fun setPosition(position: Position) {
        historyPositions.add(this.pos)
        this.pos = position
    }

    override fun canMoveTo(pos: Position): Boolean {
        getValidMovesPositions().forEach {
            if (it.line == pos.line && it.column == pos.column) {
                return true
            }
        }
        return false
    }

    override fun getPosition(): Position {
        return pos
    }
}