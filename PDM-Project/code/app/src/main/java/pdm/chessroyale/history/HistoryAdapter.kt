package pdm.chessroyale.history

import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import pdm.chessroyale.PuzzleDTO
import pdm.chessroyale.R

class HistoryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val dayView = itemView.findViewById<TextView>(R.id.day)
    private val isCompletedView = itemView.findViewById<TextView>(R.id.isCompleted)

    fun bindTo(puzzleDTO: PuzzleDTO, onItemClick: () -> Unit) {
        dayView.text = puzzleDTO.date
        isCompletedView.text = isCompletedView.context.getString(
                if (puzzleDTO.isCompleted) R.string.solved_puzzle
                else R.string.not_solved_puzzle
        )

        itemView.setOnClickListener {
            itemView.isClickable = false
            startAnimation {
                onItemClick()
                itemView.isClickable = true
            }
        }
    }

    private fun startAnimation(onAnimationEnd: () -> Unit) {
        val animation = ValueAnimator.ofArgb(
            ContextCompat.getColor(itemView.context, R.color.chess_board_black),
            ContextCompat.getColor(itemView.context, R.color.list_item_background_selected),
            ContextCompat.getColor(itemView.context, R.color.list_item_background)
        )

        animation.addUpdateListener { animator ->
            val background = itemView.background as GradientDrawable
            background.setColor(animator.animatedValue as Int)
        }

        animation.duration = 400
        animation.doOnEnd { onAnimationEnd() }

        animation.start()
    }

}

class HistoryAdapter(
    private val dataSource: List<PuzzleDTO>,
    private val onItemClick: (PuzzleDTO) -> Unit
): RecyclerView.Adapter<HistoryItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.puzzle_history_view, parent, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bindTo(dataSource[position]) {
            onItemClick(dataSource[position])
        }
    }

    override fun getItemCount() = dataSource.size
}