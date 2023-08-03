package pdm.chessroyale.models.pieces

import pdm.chessroyale.models.Color

abstract class Piece {
    var historyPositions : MutableList<Position> = mutableListOf()
    abstract fun getDrawable(): Int
    abstract fun getColor(): Color
    abstract fun getValidMovesPositions(): MutableList<Position>
    abstract fun canMoveTo(pos: Position): Boolean
    abstract fun setPosition(position: Position)
    abstract fun getPosition(): Position
}