package pdm.chessroyale.models.pieces

import pdm.chessroyale.models.Color

class Space(var pos: Position) : Piece() {

    override fun getDrawable(): Int {
        return 0
    }

    override fun getColor(): Color {
        return Color.NONE
    }

    override fun getValidMovesPositions(): MutableList<Position> {
        return mutableListOf()
    }

    override fun setPosition(position: Position) {
        this.pos = position
    }

    override fun canMoveTo(pos: Position): Boolean {
        return false
    }

    override fun getPosition(): Position {
        return pos
    }
}