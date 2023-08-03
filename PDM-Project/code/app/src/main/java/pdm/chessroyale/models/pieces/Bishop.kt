package pdm.chessroyale.models.pieces

import pdm.chessroyale.models.Color
import pdm.chessroyale.R
import pdm.chessroyale.models.TABLE_SIZE

class Bishop(var pos: Position, private val color: Color) : Piece() {

    override fun getDrawable(): Int {
        return if (color == Color.WHITE) R.drawable.ic_white_bishop else R.drawable.ic_black_bishop
    }

    override fun getColor(): Color {
        return color
    }

    override fun getValidMovesPositions(): MutableList<Position> {
        val list: MutableList<Position> = mutableListOf()
        val line = pos.line
        val column = pos.column
        //diagonal lines:
        //bottom to top diagonal line
        var auxLine = line
        var auxColumn = column
        while (auxLine > 1 && auxColumn > 0) {
            auxLine--
            auxColumn--
        }
        while (auxLine <= TABLE_SIZE && auxColumn < TABLE_SIZE) {
            if (auxLine != line && auxColumn != column)
                list.add(Position(auxLine, auxColumn))
            auxLine++
            auxColumn++
        }
        //top to bottom diagonal line
        auxLine = line
        auxColumn = column
        while (auxLine <= TABLE_SIZE && auxColumn > 0) {
            auxLine++
            auxColumn--
        }
        while (auxLine > 0 && auxColumn >= 0) {
            if (auxLine != line && auxColumn != column)
                list.add(Position(auxLine, auxColumn))
            auxLine--
            auxColumn++
        }
        return list
    }

    override fun setPosition(position: Position) {
        historyPositions.add(this.pos)
        this.pos = position
    }

    override fun canMoveTo(pos: Position): Boolean {
        val list: MutableList<Position> = getValidMovesPositions()
        list.forEach {
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