package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ChessBoardState
import com.example.domain.ChessMove
import com.example.domain.GameStatus
import com.example.domain.Piece
import com.example.domain.PieceColor
import com.example.domain.PieceType
import com.example.domain.Square
import com.example.ui.theme.BoardThemePalette
import com.example.ui.theme.ChampionshipGold
import com.example.ui.theme.CrimsonCheckAlert
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.HintSquareOverlay
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LastMoveSquareOverlay
import com.example.ui.theme.PieceVisualStyle
import com.example.ui.theme.SelectedSquareOverlay

@Composable
fun InteractiveChessBoardWithEval(
    boardState: ChessBoardState,
    selectedSquare: Square?,
    legalMovesForSelected: List<ChessMove>,
    hintSquares: Pair<Square, Square>? = null,
    isFlipped: Boolean = false,
    showLegalMoveHighlights: Boolean = true,
    showEvalBar: Boolean = true,
    evalScoreCentipawns: Int = 0,
    evalFormattedLabel: String = "0.0",
    boardTheme: BoardThemePalette = BoardThemePalette.WARM_WOOD,
    pieceStyle: PieceVisualStyle = PieceVisualStyle.CLASSIC_STAUNTON,
    onSquareClicked: (Square) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showEvalBar) {
            EvaluationBarView(
                scoreCentipawns = evalScoreCentipawns,
                formattedScore = evalFormattedLabel,
                isFlipped = isFlipped,
                modifier = Modifier
                    .width(24.dp)
                    .aspectRatio(1f / 13.5f)
                    .padding(end = 6.dp)
                    .testTag("live_evaluation_bar")
            )
        }

        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(boardTheme.borderWood)
                .border(2.dp, ChampionshipGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                .padding(6.dp)
                .testTag("chessboard_container")
        ) {
            ChessBoardCanvas(
                boardState = boardState,
                selectedSquare = selectedSquare,
                legalMovesForSelected = legalMovesForSelected,
                hintSquares = hintSquares,
                isFlipped = isFlipped,
                showLegalMoveHighlights = showLegalMoveHighlights,
                boardTheme = boardTheme,
                pieceStyle = pieceStyle,
                onSquareClicked = onSquareClicked
            )
        }
    }
}

@Composable
fun EvaluationBarView(
    scoreCentipawns: Int,
    formattedScore: String,
    isFlipped: Boolean,
    modifier: Modifier = Modifier
) {
    // Convert centipawns (-1000..+1000) into White fill fraction (0.05..0.95)
    val clamped = scoreCentipawns.coerceIn(-1000, 1000)
    val targetWhiteFraction = (0.5f + (clamped / 2000f)).coerceIn(0.06f, 0.94f)
    val animatedWhiteFraction by animateFloatAsState(
        targetValue = targetWhiteFraction,
        animationSpec = tween(durationMillis = 280),
        label = "eval_bar_fraction"
    )

    val topFraction = if (isFlipped) animatedWhiteFraction else (1f - animatedWhiteFraction)
    val topColor = if (isFlipped) Color(0xFFF5F2EB) else Color(0xFF1E2428)
    val bottomColor = if (isFlipped) Color(0xFF1E2428) else Color(0xFFF5F2EB)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, ChampionshipGold.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
            .semantics { contentDescription = "Live Engine Evaluation Bar: $formattedScore" }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(topFraction.coerceAtLeast(0.05f))
                    .background(topColor)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight((1f - topFraction).coerceAtLeast(0.05f))
                    .background(bottomColor)
            )
        }
        Text(
            text = formattedScore,
            fontFamily = JetBrainsMonoFontFamily,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = ChampionshipGold,
            modifier = Modifier
                .align(Alignment.Center)
                .background(Color(0xCC0B1D21), RoundedCornerShape(3.dp))
                .padding(horizontal = 2.dp, vertical = 1.dp)
        )
    }
}

@Composable
private fun ChessBoardCanvas(
    boardState: ChessBoardState,
    selectedSquare: Square?,
    legalMovesForSelected: List<ChessMove>,
    hintSquares: Pair<Square, Square>?,
    isFlipped: Boolean,
    showLegalMoveHighlights: Boolean,
    boardTheme: BoardThemePalette,
    pieceStyle: PieceVisualStyle,
    onSquareClicked: (Square) -> Unit
) {
    val textMeasurer: TextMeasurer = rememberTextMeasurer()
    val lastMove = boardState.moveHistory.lastOrNull()
    val checkedKingSquare = if (boardState.status == GameStatus.CHECK || boardState.status == GameStatus.CHECKMATE) {
        boardState.findKingSquare(boardState.activeColor)
    } else {
        null
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(6.dp))
            .pointerInput(isFlipped) {
                detectTapGestures { offset ->
                    val sqWidth = size.width / 8f
                    val sqHeight = size.height / 8f
                    val col = (offset.x / sqWidth).toInt().coerceIn(0, 7)
                    val row = (offset.y / sqHeight).toInt().coerceIn(0, 7)
                    val file = if (isFlipped) 7 - col else col
                    val rank = if (isFlipped) row else 7 - row
                    onSquareClicked(Square(file, rank))
                }
            }
            .semantics {
                contentDescription = "Interactive 8 by 8 Chessboard"
            }
    ) {
        val sqW = size.width / 8f
        val sqH = size.height / 8f

        for (row in 0..7) {
            for (col in 0..7) {
                val file = if (isFlipped) 7 - col else col
                val rank = if (isFlipped) row else 7 - row
                val square = Square(file, rank)
                val topLeft = Offset(col * sqW, row * sqH)
                val sqSize = Size(sqW, sqH)

                // 1. Base light or dark square
                val baseColor = if (square.isLightSquare) boardTheme.lightSquare else boardTheme.darkSquare
                drawRect(color = baseColor, topLeft = topLeft, size = sqSize)

                // 2. Last move highlight
                if (lastMove != null && (lastMove.from == square || lastMove.to == square)) {
                    drawRect(color = LastMoveSquareOverlay, topLeft = topLeft, size = sqSize)
                }

                // 3. Engine hint highlight
                if (hintSquares != null && (hintSquares.first == square || hintSquares.second == square)) {
                    drawRect(color = HintSquareOverlay, topLeft = topLeft, size = sqSize)
                }

                // 4. Selected square highlight
                if (selectedSquare == square) {
                    drawRect(color = SelectedSquareOverlay, topLeft = topLeft, size = sqSize)
                }

                // 5. King in check or checkmate crimson glow
                if (checkedKingSquare == square) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(CrimsonCheckAlert.copy(alpha = 0.85f), CrimsonCheckAlert.copy(alpha = 0.25f)),
                            center = Offset(topLeft.x + sqW / 2f, topLeft.y + sqH / 2f),
                            radius = sqW * 0.65f
                        ),
                        topLeft = topLeft,
                        size = sqSize
                    )
                }

                // 6. Coordinate labels along edges
                if (col == 0) {
                    val rankLabel = ('1'.code + rank).toChar().toString()
                    val labelColor = if (square.isLightSquare) boardTheme.coordTextDark else boardTheme.coordTextLight
                    drawText(
                        textMeasurer = textMeasurer,
                        text = rankLabel,
                        topLeft = Offset(topLeft.x + sqW * 0.07f, topLeft.y + sqH * 0.04f),
                        style = TextStyle(
                            color = labelColor.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    )
                }
                if (row == 7) {
                    val fileLabel = ('a'.code + file).toChar().toString()
                    val labelColor = if (square.isLightSquare) boardTheme.coordTextDark else boardTheme.coordTextLight
                    drawText(
                        textMeasurer = textMeasurer,
                        text = fileLabel,
                        topLeft = Offset(topLeft.x + sqW * 0.78f, topLeft.y + sqH * 0.70f),
                        style = TextStyle(
                            color = labelColor.copy(alpha = 0.85f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = JetBrainsMonoFontFamily
                        )
                    )
                }

                // 7. Piece rendering
                val piece = boardState.pieceAt(square)
                if (piece != null) {
                    val pad = sqW * 0.06f
                    drawContext.canvas.save()
                    drawContext.canvas.translate(topLeft.x + pad, topLeft.y + pad)
                    val insetSize = Size(sqW - pad * 2f, sqH - pad * 2f)
                    val prevSize = this.size
                    // Render piece scaled to square
                    drawPieceInBounds(piece, pieceStyle, insetSize, prevSize)
                    drawContext.canvas.restore()
                }

                // 8. Legal move destination indicators
                if (showLegalMoveHighlights) {
                    val legalMoveToHere = legalMovesForSelected.firstOrNull { it.to == square }
                    if (legalMoveToHere != null) {
                        val center = Offset(topLeft.x + sqW / 2f, topLeft.y + sqH / 2f)
                        if (legalMoveToHere.capturedPiece != null || legalMoveToHere.isEnPassant) {
                            drawCircle(
                                color = EmeraldSuccess.copy(alpha = 0.78f),
                                radius = sqW * 0.41f,
                                center = center,
                                style = Stroke(width = sqW * 0.09f)
                            )
                        } else {
                            drawCircle(
                                color = Color(0x99132A30),
                                radius = sqW * 0.16f,
                                center = center
                            )
                            drawCircle(
                                color = ChampionshipGold.copy(alpha = 0.85f),
                                radius = sqW * 0.12f,
                                center = center
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPieceInBounds(
    piece: Piece,
    style: PieceVisualStyle,
    targetSize: Size,
    canvasFullSize: Size
) {
    val scaleX = targetSize.width / canvasFullSize.width
    val scaleY = targetSize.height / canvasFullSize.height
    drawContext.canvas.scale(scaleX, scaleY)
    drawStauntonPiece(piece = piece, style = style)
}

@Composable
fun PromotionPickerDialog(
    color: PieceColor,
    pieceStyle: PieceVisualStyle,
    onPieceChosen: (PieceType) -> Unit,
    onDismiss: () -> Unit
) {
    val choices = listOf(
        PieceType.QUEEN,
        PieceType.ROOK,
        PieceType.BISHOP,
        PieceType.KNIGHT
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Promote Pawn",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "Choose a piece for your promoted ${color.displayName.lowercase()} pawn:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (type in choices) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPieceChosen(type) }
                                .padding(8.dp)
                                .testTag("promote_${type.name.lowercase()}")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(6.dp)
                            ) {
                                ChessPieceIcon(
                                    piece = Piece(type, color),
                                    pieceStyle = pieceStyle,
                                    size = 44.dp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
