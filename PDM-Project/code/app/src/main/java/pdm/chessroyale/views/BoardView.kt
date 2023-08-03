package pdm.chessroyale.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.GridLayout
import pdm.chessroyale.R
import pdm.chessroyale.models.ChessBoard
import pdm.chessroyale.models.TABLE_SIZE
import pdm.chessroyale.models.pieces.Piece
import pdm.chessroyale.views.Tile.Type

typealias TileTouchListener = (tile: Tile, row: Int, column: Int) -> Unit

/**
 * Custom view that implements a chess board.
 */
@SuppressLint("ClickableViewAccessibility")
class BoardView(private val ctx: Context, attrs: AttributeSet?) : GridLayout(ctx, attrs) {

    private val side = 8
    private val brush = Paint().apply {
        ctx.resources.getColor(R.color.chess_board_black, null)
        style = Paint.Style.STROKE
        strokeWidth = 10F
    }

    var onTileClickedListener: TileTouchListener? = null

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        canvas.drawLine(0f, 0f, width.toFloat(), 0f, brush)
        canvas.drawLine(0f, height.toFloat(), width.toFloat(), height.toFloat(), brush)
        canvas.drawLine(0f, 0f, 0f, height.toFloat(), brush)
        canvas.drawLine(width.toFloat(), 0f, width.toFloat(), height.toFloat(), brush)
    }

    fun print(chessTable: HashMap<Int, MutableList<Piece>>?) {
        val mirroredMap: HashMap<Int, MutableList<Piece>> = HashMap()
        for (i in 1..TABLE_SIZE) {
            mirroredMap[i] = chessTable?.get(TABLE_SIZE - i + 1)!!
        }
        rowCount = side
        columnCount = side
        removeAllViews()
        repeat(side * side) {
            val row = it / side + 1
            val column = it % side
            val tile = Tile(
                ctx,
                if ((row + column) % 2 == 0) Type.WHITE else Type.BLACK,
                side,
                mirroredMap[row]!![column]
            )
            tile.setOnClickListener { onTileClickedListener?.invoke(tile, row, column) }
            addView(tile)
        }
    }
}