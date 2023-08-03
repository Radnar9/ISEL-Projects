package pdm.chessroyale.models.pieces

import pdm.chessroyale.models.Color
import pdm.chessroyale.R

class Knight(var pos: Position, private val color: Color) : Piece() {

    override fun getDrawable(): Int {
        return if (color == Color.WHITE) R.drawable.ic_white_knight else R.drawable.ic_black_knight
    }

    override fun getColor(): Color {
        return color
    }

    //https://www.google.com/search?q=knight+chess+movements&tbm=isch&ved=2ahUKEwibzKvDxJr0AhVCyRoKHS0sBxcQ2-cCegQIABAA&oq=knight+chess+movements&gs_lcp=CgNpbWcQAzIECAAQEzoHCCMQ7wMQJzoICAAQCBAeEBM6BggAEAcQHjoICAAQCBAHEB5QkAVY4glg2ApoAHAAeACAAcsBiAGDBpIBBTcuMC4xmAEAoAEBqgELZ3dzLXdpei1pbWfAAQE&sclient=img&ei=2miSYZv3CcKSa63YnLgB&bih=625&biw=1366#imgrc=oXMMVJ6UrcqfeM
    override fun getValidMovesPositions(): MutableList<Position> {
        val list: MutableList<Position> = mutableListOf()
        val line = pos.line
        val column = pos.column
        list.add(Position(line + 2, column - 1))
        list.add(Position(line - 2, column - 1))
        list.add(Position(line + 2, column + 1))
        list.add(Position(line - 2, column + 1))
        list.add(Position(line - 1, column + 2))
        list.add(Position(line - 1, column - 2))
        list.add(Position(line + 1, column + 2))
        list.add(Position(line + 1, column - 2))
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