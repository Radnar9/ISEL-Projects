package pdm.chessroyale.models

import android.os.Parcel
import android.os.Parcelable
import pdm.chessroyale.PuzzleDTO
import pdm.chessroyale.models.pieces.*
import pdm.chessroyale.views.Tile
import java.util.*
import kotlin.collections.HashMap

const val TABLE_SIZE = 8

class ChessBoard() : Parcelable {
    //this color will switch back and forth on every move (white,black,white,black...)
    private var currColor = Color.WHITE

    // Builds an hashMap in which the keys are 1-8 , and the values are a list with each row
    // Don't forget chessBoard[1][0] == WHITE pieces
    private var chessBoard = HashMap<Int, MutableList<Piece>>()
    private var checkMap = HashMap<Color, Boolean>()
    private var piecesMap = HashMap<Color, MutableList<Piece>>()

    private lateinit var puzzleDto: PuzzleDTO
    private lateinit var solutionList: LinkedList<String>
    private var isPlayable = true

    private lateinit var positionKingWhite: Position
    private lateinit var positionKingBlack: Position
    private val whiteCheck: MutableList<Piece> = mutableListOf()
    private val blackCheck: MutableList<Piece> = mutableListOf()
    private var gameOver = false
    private var colorCheckMate: Color? = null
    private var surrenderWinner: Color? = null

    var prevTile: Tile? = null

    constructor(puzzleDto: PuzzleDTO?) : this() {
        initBoard()
        if (puzzleDto != null) {
            this.puzzleDto = puzzleDto
            buildBoardFromPGN(puzzleDto.puzzleInfo.game.pgn)
            solutionList = LinkedList(puzzleDto.puzzleInfo.puzzle.solution)
        }
    }

    fun getChessBoard(): HashMap<Int, MutableList<Piece>> {
        return chessBoard
    }

    fun getChessBoardSolution(): List<String> {
        return solutionList
    }

    fun setPuzzleCompleted(): PuzzleDTO {
        puzzleDto.isCompleted = true
        return puzzleDto
    }

    fun setPlayable(state: Boolean) {
        isPlayable = state
    }

    fun isPlayable(): Boolean {
        return isPlayable
    }

    fun isGameOver(): Boolean {
        return gameOver
    }

    fun setGameOver() {
        gameOver = true
    }

    fun setCheckMate(color: Color) {
        colorCheckMate = color
    }

    fun isCheckMate(): Color? {
        return colorCheckMate
    }

    fun isCompleted(): Boolean {
        return puzzleDto.isCompleted
    }

    fun getCurrColor(): Color {
        return currColor
    }

    fun isKing(position: Position?): Boolean {
        if (position == null) return false
        return getPiece(position) is King
    }

    fun setSurrender(winner: Color) {
        surrenderWinner = winner
    }

    fun isSurrender(): Color? = surrenderWinner

    fun initBoard(): HashMap<Int, MutableList<Piece>> {
        piecesMap[Color.WHITE] = mutableListOf()
        piecesMap[Color.BLACK] = mutableListOf()
        checkMap[Color.WHITE] = false
        checkMap[Color.BLACK] = false
        val init = InitChessBoard(chessBoard, piecesMap)
        val kings = init.getKings()
        positionKingBlack = kings[1]
        positionKingWhite = kings[0]
        return chessBoard
    }

    private fun buildBoardFromPGN(pgn: String) {
        pgn.split(" ")
            .forEach {
                var currMove = it
                val isCheckMove = currMove.endsWith("+")
                if (isCheckMove) { //removes '+' char
                    currMove = currMove.substring(0, currMove.length - 1)
                }
                if (currMove.startsWith("O")) {
                    castling(currMove, currColor)
                } else if (currMove.contains("x")) { // piece ate another piece
                    val aux: List<String> = currMove.split("x")
                    val origin: String = aux[0]
                    val destination: String = aux[1]
                    val column: Int = destination[0] - 'a' //e.g 'e'
                    val line: Int = destination[1].digitToInt()  //e.g '5'
                    val destinationPos = Position(line, column)
                    if (currMove.length == 6) { //e.g : Qh4xe1
                        val pieces: List<Piece> = getPiecesFromChar(origin[0], currColor) //e.g 'Q'
                        val originColumn: Int = origin[1] - 'a' //e.g 'h'
                        val originLine: Int = origin[2].digitToInt()  //e.g '4'
                        run foreachLabel@{
                            pieces.forEach { piece ->
                                val position = piece.getPosition()
                                if (position.line == originLine && position.column == originColumn) {
                                    movePieceToPosition(piece, destinationPos)
                                    return@foreachLabel
                                }
                            }
                        }
                    } else {
                        if (origin.length == 1) {
                            if (origin[0].isLowerCase()) { //example : fxe5 (pawn eats another pawn)
                                val pawns: List<Piece> = getPiecesFromChar('P', currColor)
                                run foreachLabel@{
                                    pawns.forEach { pawn ->
                                        if ((pawn as Pawn).canCapture(
                                                line,
                                                column
                                            )
                                        ) {
                                            movePieceToPosition(pawn, destinationPos)
                                            return@foreachLabel
                                        }
                                    }
                                }
                            } else { //origin.get(0).isUpperCase() , example : Qxe5
                                foreach(origin[0], column, line, currColor, isCheckMove)
                            }
                        }
                    }
                } else if (currMove.length == 2) { // pawn movement
                    //movePawn
                    val column: Int = currMove[0] - 'a' //e.g : 'a' == column 0
                    val line: Int = currMove[1].digitToInt() //e.g : '4' == line 3
                    foreach('P', column, line, currColor, isCheckMove)
                } else if (currMove.length == 3) { // Bd2 or Qb4+ or e8Q
                    val column: Int
                    val line: Int
                    if (currMove[2].isDigit()) {
                        column = currMove[1] - 'a'
                        line = currMove[2].digitToInt()
                        foreach(currMove[0], column, line, currColor, isCheckMove)
                    } else { // Pawn promotion e.g : e8Q
                        column = currMove[0] - 'a'
                        line = currMove[1].digitToInt()
                        //remove pawn
                        piecesMap[currColor]!!.remove(chessBoard[line]!![column])
                        when (currMove[2]) {
                            'R' -> chessBoard[line]!![column] =
                                Rook(Position(line, column), currColor)
                            'N' -> chessBoard[line]!![column] =
                                Knight(Position(line, column), currColor)
                            'B' -> chessBoard[line]!![column] =
                                Bishop(Position(line, column), currColor)
                            'Q' -> chessBoard[line]!![column] =
                                Queen(Position(line, column), currColor)
                            'K' -> chessBoard[line]!![column] =
                                King(Position(line, column), currColor)
                            'P' -> chessBoard[line]!![column] =
                                Pawn(Position(line, column), currColor)
                        }
                        //add new piece
                        piecesMap[currColor]!!.add(chessBoard[line]!![column])
                    }
                } else if (currMove.length == 4 || currMove.length == 5) { // Nhf5 , Qh4e1
                    val pieces: List<Piece> = getPiecesFromChar(currMove[0], currColor)
                    val originCompare: Int
                    val compareByColumn: Boolean
                    if (currMove[1].isDigit()) { //currMove[1] is a line reference
                        originCompare = currMove[1].digitToInt()
                        compareByColumn = false
                    } else {  ////currMove[1] is a column reference
                        originCompare = currMove[1] - 'a'
                        compareByColumn = true
                    }
                    val destColumn: Int = currMove[2] - 'a'
                    val destLine: Int = currMove[3].digitToInt()
                    var originLine = 0
                    if (currMove.length == 5) {
                        originLine = currMove[2] - 'a'
                    }
                    run foreachLabel@{
                        pieces.forEach { piece ->
                            if ((compareByColumn && piece.getPosition().column == originCompare)
                                || (!compareByColumn && piece.getPosition().line == originCompare)
                            ) {
                                if (currMove.length == 5 && piece.getPosition().line != originLine) {
                                    return@foreachLabel
                                } else {
                                    movePieceToPosition(piece, Position(destLine, destColumn))
                                }
                            }

                        }
                    }
                } else {
                    throw IllegalArgumentException("Not implemented : $currMove")
                }
                if (isCheckMove) {
                    val kingList: List<Piece> =
                        if (currColor == Color.WHITE) getPiecesFromChar('K', Color.BLACK)
                        else getPiecesFromChar('K', Color.WHITE)
                    (kingList[0] as King).isCheck = true
                    checkMap[currColor] = true
                } else {
                    if (checkMap[currColor] == true) {
                        val king: King = getPiecesFromChar('K', currColor)[0] as King
                        if (!isKingStillOnCheck(king)) {
                            king.isCheck = false
                            checkMap[currColor] = false
                        }
                    }
                }
                switchColor()
            }
    }

    private fun castling(currMove: String, currColor: Color) {
        val isKingSide: Boolean = currMove == "O-O"
        val kingList: List<Piece> = getPiecesFromChar('K', currColor)
        val king: Piece = kingList[0] as King
        val rooks: List<Piece> = getPiecesFromChar('R', currColor)
        run foreach1@{
            rooks.forEach { rook ->
                //https://en.wikipedia.org/wiki/Castling#Requirements
                if (king.historyPositions.isEmpty() && rook.historyPositions.isEmpty()
                    && king.getPosition().line == rook.getPosition().line
                ) {
                    val line: Int = rook.getPosition().line
                    if (isKingSide && (rook.getPosition().column - king.getPosition().column) == 3) {
                        val rookColumn: Int = rook.getPosition().column
                        movePieceToPosition(king, Position(line, rookColumn - 1))
                        movePieceToPosition(rook, Position(line, rookColumn - 2))
                        return@foreach1
                    } else if (!isKingSide && (king.getPosition().column - rook.getPosition().column) == 4) {
                        val kingColumn: Int = king.getPosition().column
                        movePieceToPosition(king, Position(line, kingColumn - 2))
                        movePieceToPosition(rook, Position(line, kingColumn - 1))
                        return@foreach1
                    }
                }
            }
        }
    }

    /**
     * Useful for not duplicating code
     */
    private fun foreach(
        pieceChar: Char,
        column: Int,
        line: Int,
        currColor: Color,
        isCheckMove: Boolean
    ) {
        val pieces: List<Piece> = getPiecesFromChar(pieceChar, currColor)
        val destinationPos = Position(line, column)
        run foreachLabel@{
            pieces.forEach { piece ->
                if (isAllowedMove(piece, destinationPos) && (!isCheckMove || moveGivesCheck(
                        piece,
                        destinationPos
                    ))
                ) {
                    movePieceToPosition(piece, destinationPos)
                    return@foreachLabel
                }
            }
        }
    }

    /**
     * Returns true if the Piece can travel to destination position
     */
    private fun isAllowedMove(piece: Piece, destinationPos: Position): Boolean {
        if (piece is Pawn && getPiece(destinationPos) !is Space) {
            return pawnCanMove(piece, getPiece(destinationPos)!!)
        }
        var isAllowed = true
        if (piece !is Knight) { //can't jump over other pieces
            val piecePos: Position = piece.getPosition()
            if (piecePos.column == destinationPos.column) { // check vertically
                for (lineIdx in minOf(piecePos.line, destinationPos.line) + 1 until maxOf(
                    piecePos.line,
                    destinationPos.line
                )) {
                    if (chessBoard[lineIdx]!![piecePos.column] !is Space) {
                        isAllowed = false
                        break
                    }
                }
            } else if (piecePos.line == destinationPos.line) { // check horizontally
                for (columnIdx in minOf(piecePos.column, destinationPos.column) + 1 until maxOf(
                    piecePos.column,
                    destinationPos.column
                )) {
                    if (chessBoard[piecePos.line]!![columnIdx] !is Space) {
                        isAllowed = false
                        break
                    }
                }
            } else { // check diagonally
                val aux1 = destinationPos.column - piecePos.column
                val aux2 = destinationPos.line - piecePos.line
                var col: Int
                var line: Int
                val maxCol: Int
                val maxLine: Int
                //this next 4 ifs may be compacted to only 2 but for future visual details the 4 ifs will be needed
                if (aux1 > 0 && aux2 < 0) { //top to bottom , left to right (1)
                    col = minOf(destinationPos.column, piecePos.column) + 1
                    line = maxOf(destinationPos.line, piecePos.line) - 1
                    maxCol = maxOf(destinationPos.column, piecePos.column)
                    maxLine = minOf(destinationPos.line, piecePos.line)
                    while (col < maxCol && line > maxLine) {
                        if (chessBoard[line]!![col] !is Space) {
                            isAllowed = false
                            break
                        }
                        col++
                        line--
                    }
                } else if (aux1 < 0 && aux2 > 0) { //bottom to top , right to left (2)
                    col = maxOf(destinationPos.column, piecePos.column) - 1
                    line = minOf(destinationPos.line, piecePos.line) + 1
                    maxCol = minOf(destinationPos.column, piecePos.column)
                    maxLine = maxOf(destinationPos.line, piecePos.line)
                    while (col > maxCol && line < maxLine) {
                        if (chessBoard[line]!![col] !is Space) {
                            isAllowed = false
                            break
                        }
                        col--
                        line++
                    }
                } else if (aux1 > 0 && aux2 > 0) { //bottom to top , left to right (3)
                    col = minOf(destinationPos.column, piecePos.column) + 1
                    line = minOf(destinationPos.line, piecePos.line) + 1
                    maxCol = maxOf(destinationPos.column, piecePos.column)
                    maxLine = maxOf(destinationPos.line, piecePos.line)
                    while (col < maxCol && line < maxLine) {
                        if (chessBoard[line]!![col] !is Space) {
                            isAllowed = false
                            break
                        }
                        col++
                        line++
                    }
                } else if (aux1 < 0 && aux2 < 0) { //top to bottom, right to left (4)
                    col = maxOf(destinationPos.column, piecePos.column) - 1
                    line = maxOf(destinationPos.line, piecePos.line) - 1
                    maxCol = minOf(destinationPos.column, piecePos.column)
                    maxLine = minOf(destinationPos.line, piecePos.line)
                    while (col > maxCol && line > maxLine) {
                        if (chessBoard[line]!![col] !is Space) {
                            isAllowed = false
                            break
                        }
                        col--
                        line--
                    }
                }
            }
        }
        return piece.canMoveTo(destinationPos) && isAllowed
    }

    /**
     * Returns true if that move makes a "check" play (only modifies the chessBoard[][] temporarily)
     */
    private fun moveGivesCheck(originPiece: Piece, destPos: Position): Boolean {
        //save backup
        val originPos = originPiece.getPosition()
        val backupDestPiece = chessBoard[destPos.line]!![destPos.column]
        //make move (temporarily)
        chessBoard[originPos.line]!![originPos.column] =
            Space(Position(originPos.line, originPos.column))
        chessBoard[destPos.line]!![destPos.column] = originPiece
        originPiece.setPosition(destPos)
        // verifies if that play activates "check"
        val color = if (currColor == Color.WHITE) Color.BLACK
        else Color.WHITE
        val king: King = getPiecesFromChar('K', color)[0] as King
        val isCheck = isKingStillOnCheck(king)
        //restore backup
        chessBoard[originPos.line]!![originPos.column] = originPiece
        originPiece.setPosition(originPos)
        chessBoard[destPos.line]!![destPos.column] = backupDestPiece
        return isCheck
    }

    /**
     * Returns true if the king is on "check" after the last play
     */
    private fun isKingStillOnCheck(king: King): Boolean {
        //checks if check exists on the only king
        val pieces = if (king.getColor() == Color.WHITE) {
            piecesMap[Color.BLACK]!!
        } else {
            piecesMap[Color.WHITE]!!
        }
        pieces.forEach {
            if (isAllowedMove(it, king.pos)) { // == king is in danger (check)
                return true
            }
        }
        return false
    }

    private fun movePieceToPosition(originPiece: Piece, destPos: Position) {
        val originPos = originPiece.getPosition()
        val destPiece = getPiece(destPos)
        //affects chessboard hashmap
        if (destPiece is Space) {
            // reuse the Space instance (don't forget to update it with the new position)
            chessBoard[originPos.line]!![originPos.column] = destPiece
            destPiece.setPosition(originPos)
        } else {
            //removes the eaten piece from list
            if (currColor == Color.WHITE) {
                piecesMap[Color.BLACK]?.remove(destPiece)
                if (blackCheck.contains(destPiece)) {
                    blackCheck.remove(destPiece)
                }
            } else {
                piecesMap[Color.WHITE]?.remove(destPiece)
                if (whiteCheck.contains(destPiece)) {
                    whiteCheck.remove(destPiece)
                }
            }
            //creates a new Space on the origin destination
            chessBoard[originPos.line]!![originPos.column] =
                Space(Position(originPos.line, originPos.column))
        }
        chessBoard[destPos.line]!![destPos.column] = originPiece
        originPiece.setPosition(destPos)
    }

    /**
     * Returns all pieces with that color , example ('b',white) returns all white bishops
     */
    private fun getPiecesFromChar(pieceChar: Char, currColor: Color): MutableList<Piece> {
        val list: MutableList<Piece>? = piecesMap[currColor]
        val newList: MutableList<Piece> = mutableListOf()
        list!!.forEach {
            when (pieceChar) {
                'R' -> if (it is Rook) newList.add(it)
                'N' -> if (it is Knight) newList.add(it)
                'B' -> if (it is Bishop) newList.add(it)
                'Q' -> if (it is Queen) newList.add(it)
                'K' -> if (it is King) newList.add(it)
                'P' -> if (it is Pawn) newList.add(it)
            }
        }
        return newList
    }

    /**
     * Return true if the move is the solution
     */
    fun play(prevTile: Tile, currTile: Tile): Boolean {
        if (isExpectedMove(prevTile, currTile, solutionList.first)) {
            movePiece(prevTile, currTile)
            return true
        }
        return false
    }

    /**
     * Return true if the play is valid
     */
    fun tryToMove(prevTile: Tile, currTile: Tile): Boolean {
        val prevPiece = prevTile.piece
        val currPiece = currTile.piece
        if (prevPiece.getColor() == currPiece.getColor()) {
            return false
        }

        val canMove = isAllowedMove(prevPiece, currPiece.getPosition())
        if (canMove && prevPiece is King) {
            kingSavePos(prevPiece, currPiece.getPosition())
        }
        return if (canMove) {
            movePiece(prevTile, currTile)
            updateCheckLists(prevPiece)
            if (currPiece is King) {
                gameOver = true
            }
            true
        } else false
    }

    private fun updateCheckLists(piece: Piece) {
        if (piece !is King) {
            val kingPosition =
                if (piece.getColor() == Color.WHITE) positionKingBlack else positionKingWhite
            val canMove = isAllowedMove(piece, kingPosition)
            if (canMove) {
                if (piece.getColor() == Color.WHITE) {
                    if (!whiteCheck.contains(piece)) {
                        whiteCheck.add(piece)
                    }
                } else {
                    if (!blackCheck.contains(piece)) {
                        blackCheck.add(piece)
                    }
                }
            }
        }
        val opponentKing = getPiece(
            if (piece.getColor() == Color.WHITE) positionKingWhite
            else positionKingBlack
        )

        updateOpponentList(opponentKing!!)
        verifyAndSetIfCheckMate(piece.getColor(), opponentKing)
    }

    fun isPromotion(piece: Piece): Boolean {
        if (piece is Pawn && (piece.getPosition().line == 1 || piece.getPosition().line == 8)) {
            return true
        }
        return false
    }

    fun getPromotedPiece(toPromotePos: Position, futurePiece: Char): Piece {
        val pieceToBePromoted = chessBoard[toPromotePos.line]!![toPromotePos.column]
        val pieceColor = pieceToBePromoted.getColor()
        return when (futurePiece) {
            'K' -> Knight(toPromotePos, pieceColor)
            'Q' -> Queen(toPromotePos, pieceColor)
            'R' -> Rook(toPromotePos, pieceColor)
            'B' -> Bishop(toPromotePos, pieceColor)
            else -> pieceToBePromoted
        }
    }

    private fun verifyAndSetIfCheckMate(color: Color, opponentKing: Piece) {
        val opponentCheckList = if (color == Color.WHITE) blackCheck else whiteCheck
        // if it reaches here and the opponent has a check then it's game over
        if (opponentCheckList.isNotEmpty()) {
            gameOver = true
            colorCheckMate = if (color == Color.WHITE) Color.BLACK else Color.WHITE
            isPlayable = false
        }
        val currentPlayerCheckList = if (color == Color.WHITE) whiteCheck else blackCheck
        if (currentPlayerCheckList.size == 0) return
        val kingStuck = kingIsStuck(opponentKing)
        val anyone = anyoneCanKillCheck(currentPlayerCheckList[0])
        if ((kingStuck && currentPlayerCheckList.size > 1) || (!anyone && kingStuck)) {
            gameOver = true
            colorCheckMate = color
            isPlayable = false
        }
    }

    /**
     * Return true if all positions around king are busy
     */
    private fun kingIsStuck(king: Piece): Boolean {
        val pos = king.getPosition()
        val canMove = (isAllowedMove(king, Position(pos.line - 1, pos.column))
                || isAllowedMove(king, Position(pos.line + 1, pos.column))
                || isAllowedMove(king, Position(pos.line, pos.column + 1))
                || isAllowedMove(king, Position(pos.line, pos.column - 1))
                || isAllowedMove(king, Position(pos.line + 1, pos.column - 1))
                || isAllowedMove(king, Position(pos.line - 1, pos.column + 1))
                || isAllowedMove(king, Position(pos.line + 1, pos.column + 1))
                || isAllowedMove(king, Position(pos.line - 1, pos.column - 1)))
        return !canMove
    }

    /**
     * Return true if any opponent piece can kill the check piece
     */
    private fun anyoneCanKillCheck(piece: Piece): Boolean {
        val otherPieces =
            piecesMap[if (piece.getColor() == Color.BLACK) Color.WHITE else Color.BLACK]
        for (p in otherPieces!!) {
            if (isAllowedMove(p, piece.getPosition())) {
                return true
            }
        }
        return false
    }

    private fun kingSavePos(king: Piece, pos: Position) {
        if (king.getColor() == Color.BLACK) {
            positionKingBlack = pos
        } else {
            positionKingWhite = pos
        }
        updateOpponentList(king)
    }

    private fun updateOpponentList(king: Piece) {
        val auxList = if (king.getColor() == Color.BLACK) whiteCheck else blackCheck
        val otherPieces =
            piecesMap[if (king.getColor() == Color.BLACK) Color.WHITE else Color.BLACK]
        // Remove pieces that aren't doing check anymore
        auxList.removeAll {
            !isAllowedMove(it, king.getPosition())
        }
        // Verifies if some opponent piece can be moved to the king's place
        for (piece in otherPieces!!) {
            if (!auxList.contains(piece) && isAllowedMove(piece, king.getPosition())) {
                auxList.add(piece)
            }
        }
    }

    /**
     * Return true if pawn can move to that position
     */
    private fun pawnCanMove(pawn: Piece, currPiece: Piece): Boolean {
        val finalPos = currPiece.getPosition()
        if (currPiece is Space) {
            return pawn.canMoveTo(finalPos)
        } else {
            if (pawn.getColor() == Color.WHITE) {
                if (finalPos.line != pawn.getPosition().line + 1) {
                    return false
                }
            }
            if (pawn.getColor() == Color.BLACK) {
                if (finalPos.line != pawn.getPosition().line - 1) {
                    return false
                }
            }
            if (((currPiece.getPosition().column == pawn.getPosition().column + 1)
                        || (currPiece.getPosition().column == pawn.getPosition().column - 1))
            ) {
                return true
            }
        }
        return false
    }

    /**
     * Move one piece from one tile to other
     */
    private fun movePiece(prevTile: Tile, currTile: Tile) {
        val destination = currTile.piece.getPosition()
        val initiation = prevTile.piece.getPosition()

        movePieceToPosition(prevTile.piece, destination)
        currTile.piece = chessBoard[destination.line]!![destination.column]
        prevTile.piece = chessBoard[initiation.line]!![initiation.column]
        switchColor()
    }

    /**
     * Given two positions, swaps or eats the destiny piece
     */
    fun movePiece(prevPos: Position?, nextPos: Position?) {
        if (prevPos == null || nextPos == null) {
            return
        }
        val prevPiece = chessBoard[prevPos.line]!![prevPos.column]
        val nextPiece = chessBoard[nextPos.line]!![nextPos.column]
        if (nextPiece !is Space) {
            if (currColor == Color.WHITE) {
                piecesMap[Color.BLACK]?.remove(nextPiece)
                if (blackCheck.contains(nextPiece)) {
                    blackCheck.remove(nextPiece)
                }
            } else {
                piecesMap[Color.WHITE]?.remove(nextPiece)
                if (whiteCheck.contains(nextPiece)) {
                    whiteCheck.remove(nextPiece)
                }
            }
            //creates a new Space on the origin destination
            chessBoard[prevPos.line]!![prevPos.column] =
                Space(Position(prevPos.line, prevPos.column))
        } else {
            chessBoard[prevPos.line]!![prevPos.column] = chessBoard[nextPos.line]!![nextPos.column]
            nextPiece.setPosition(prevPos)
        }
        chessBoard[nextPos.line]!![nextPos.column] = prevPiece
        prevPiece.setPosition(nextPos)
        if (prevPiece is King) kingSavePos(prevPiece, nextPos) else updateCheckLists(prevPiece)
        switchColor()
    }

    /**
     * Return the piece with that position
     */
    private fun getPiece(position: Position): Piece? {
        return chessBoard[position.line]?.get(position.column)
    }

    private fun isExpectedMove(prevTile: Tile, currTile: Tile, expectedMove: String?): Boolean {
        //e.g solution : "e1e7"
        val originPosition = prevTile.piece.getPosition()
        if (expectedMove!![0] - 'a' == originPosition.column && expectedMove[1].digitToInt() == originPosition.line) {
            val destPosition = currTile.piece.getPosition()
            if (expectedMove[2] - 'a' == destPosition.column && expectedMove[3].digitToInt() == destPosition.line) {
                return true
            }
        }
        return false
    }

    private fun switchColor() {
        currColor = if (currColor == Color.WHITE) Color.BLACK
        else Color.WHITE
    }

    /**
     * Builds the solution on top of the current board state
     */
    fun buildSolution() {
        solutionList.forEach { play ->
            val l = play.toList()
            val futurePos = Position(l[3].digitToInt(), l[2] - 'a')
            val currPiece = chessBoard[l[1].digitToInt()]?.get(l[0] - 'a')
            movePieceToPosition(currPiece!!, futurePos)
        }
    }

    constructor(parcel: Parcel) : this() {
        parcel.readMap(chessBoard, ClassLoader.getSystemClassLoader())
        parcel.readMap(checkMap, ClassLoader.getSystemClassLoader())
        parcel.readMap(piecesMap, ClassLoader.getSystemClassLoader())
    }

    override fun describeContents() = 0
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeMap(chessBoard)
        dest.writeMap(checkMap)
        dest.writeMap(piecesMap)
    }

    fun removeFirstSolutionMove() {
        solutionList.removeFirst()
    }

    companion object CREATOR : Parcelable.Creator<ChessBoard> {
        override fun createFromParcel(parcel: Parcel): ChessBoard {
            return ChessBoard(parcel)
        }

        override fun newArray(size: Int): Array<ChessBoard?> {
            return arrayOfNulls(size)
        }
    }
}