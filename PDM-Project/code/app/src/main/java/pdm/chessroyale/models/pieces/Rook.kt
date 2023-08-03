package pdm.chessroyale.models.pieces

import pdm.chessroyale.models.Color
import pdm.chessroyale.R
import pdm.chessroyale.models.TABLE_SIZE

class Rook(var pos: Position, private val color: Color) : Piece() {
    override fun getDrawable(): Int {
        return if (color == Color.WHITE) R.drawable.ic_white_rook else R.drawable.ic_black_rook
    }

    override fun getColor(): Color {
        return color
    }

    //https://www.google.com/search?q=chess+rook+movements&tbm=isch&ved=2ahUKEwifx7bnwJr0AhUKexoKHUb_BN8Q2-cCegQIABAA&oq=chess+rook+movements&gs_lcp=CgNpbWcQAzIECAAQEzIICAAQCBAeEBM6BwgjEO8DECc6BggAEAcQHjoECAAQHlDqA1jXDmCiD2gBcAB4AIABXYgB_QaSAQIxMZgBAKABAaoBC2d3cy13aXotaW1nwAEB&sclient=img&ei=9GSSYZ_9BYr2acb-k_gN&bih=625&biw=1366#imgrc=0dkgysaihg2wvM
    override fun getValidMovesPositions(): MutableList<Position> {
        val list: MutableList<Position> = mutableListOf()
        for (line in 1..TABLE_SIZE) {
            list.add(Position(line, pos.column))
        }
        for (column in 0 until TABLE_SIZE) {
            list.add(Position(pos.line, column))
        }
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