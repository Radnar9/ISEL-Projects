package pdm.chessroyale.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import pdm.chessroyale.R
import pdm.chessroyale.models.pieces.Piece
import pdm.chessroyale.models.pieces.Space

/**
 * Custom view that implements a chess board tile.
 * Tiles are either black or white and can they can be empty or occupied by a chess piece.
 *
 * Implementation note: This view is not to be used with the designer tool.
 * You need to adapt this view to suit your needs. ;)
 *
 * @property type           The tile's type (i.e. black or white)
 * @property tilesPerSide   The number of tiles in each side of the chess board
 *
 */
@SuppressLint("ViewConstructor")
class Tile(
    private val ctx: Context,
    private val type: Type,
    private val tilesPerSide: Int,
    initialPiece: Piece,
) : View(ctx) {

    var piece = initialPiece
        set(value) {
            field = value
            invalidate()
        }

    enum class Type { WHITE, BLACK }

    private var brush = Paint().apply {
        color = ctx.resources.getColor(
            if (type == Type.WHITE) R.color.chess_board_white else R.color.chess_board_black,
            null
        )
        style = Paint.Style.FILL_AND_STROKE
    }

    fun enableSelectedColor(){
        brush.color = ctx.resources.getColor(
            R.color.chess_board_selected,
            null
        )
        invalidate()
    }

    fun disableSelectedColor() {
        brush.color = ctx.resources.getColor(
            if (type == Type.WHITE) R.color.chess_board_white else R.color.chess_board_black,
            null
        )
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val side = Integer.min(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )
        setMeasuredDimension(side / tilesPerSide, side / tilesPerSide)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), brush)

        if (piece !is Space) {
            VectorDrawableCompat.create(ctx.resources, piece.getDrawable(), null)?.apply {
                val padding = 8
                setBounds(padding, padding, width - padding, height - padding)
                draw(canvas)
            }
        }
    }

}