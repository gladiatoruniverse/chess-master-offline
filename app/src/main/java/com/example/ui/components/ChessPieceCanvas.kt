package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.Piece
import com.example.domain.PieceColor
import com.example.domain.PieceType
import com.example.ui.theme.PieceVisualStyle

@Composable
fun ChessPieceIcon(
    piece: Piece,
    pieceStyle: PieceVisualStyle = PieceVisualStyle.CLASSIC_STAUNTON,
    size: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(size)) {
        drawStauntonPiece(piece = piece, style = pieceStyle)
    }
}

fun DrawScope.drawStauntonPiece(
    piece: Piece,
    style: PieceVisualStyle
) {
    val isWhite = piece.color == PieceColor.WHITE
    val fill = if (isWhite) style.whiteFill else style.blackFill
    val stroke = if (isWhite) style.whiteStroke else style.blackStroke
    val accent = if (isWhite) style.whiteInnerAccent else style.blackInnerAccent

    val w = size.width
    val h = size.height
    val strokeWidth = (w * 0.055f).coerceAtLeast(1.8f)

    // Subtle drop shadow
    drawOval(
        color = Color(0x44000000),
        topLeft = Offset(w * 0.16f, h * 0.82f),
        size = Size(w * 0.68f, h * 0.11f)
    )

    when (piece.type) {
        PieceType.PAWN -> drawPawnShape(w, h, fill, stroke, accent, strokeWidth)
        PieceType.ROOK -> drawRookShape(w, h, fill, stroke, accent, strokeWidth)
        PieceType.KNIGHT -> drawKnightShape(w, h, fill, stroke, accent, strokeWidth)
        PieceType.BISHOP -> drawBishopShape(w, h, fill, stroke, accent, strokeWidth)
        PieceType.QUEEN -> drawQueenShape(w, h, fill, stroke, accent, strokeWidth)
        PieceType.KING -> drawKingShape(w, h, fill, stroke, accent, strokeWidth)
    }
}

private fun DrawScope.drawBasePedestal(
    w: Float,
    h: Float,
    fill: Color,
    stroke: Color,
    accent: Color,
    strokeWidth: Float
) {
    drawRoundRect(
        color = fill,
        topLeft = Offset(w * 0.18f, h * 0.78f),
        size = Size(w * 0.64f, h * 0.10f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = Fill
    )
    drawRoundRect(
        color = stroke,
        topLeft = Offset(w * 0.18f, h * 0.78f),
        size = Size(w * 0.64f, h * 0.10f),
        cornerRadius = CornerRadius(w * 0.04f, w * 0.04f),
        style = Stroke(width = strokeWidth)
    )
    drawLine(
        color = accent,
        start = Offset(w * 0.24f, h * 0.83f),
        end = Offset(w * 0.76f, h * 0.83f),
        strokeWidth = strokeWidth * 0.75f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawPawnShape(
    w: Float,
    h: Float,
    fill: Color,
    stroke: Color,
    accent: Color,
    strokeWidth: Float
) {
    val body = Path().apply {
        moveTo(w * 0.27f, h * 0.78f)
        cubicTo(w * 0.30f, h * 0.62f, w * 0.38f, h * 0.52f, w * 0.41f, h * 0.44f)
        lineTo(w * 0.59f, h * 0.44f)
        cubicTo(w * 0.62f, h * 0.52f, w * 0.70f, h * 0.62f, w * 0.73f, h * 0.78f)
        close()
    }
    drawPath(body, color = fill, style = Fill)
    drawPath(body, color = stroke, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

    // Collar
    drawRoundRect(
        color = fill,
        topLeft = Offset(w * 0.36f, h * 0.41f),
        size = Size(w * 0.28f, h * 0.06f),
        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
    )
    drawRoundRect(
        color = stroke,
        topLeft = Offset(w * 0.36f, h * 0.41f),
        size = Size(w * 0.28f, h * 0.06f),
        cornerRadius = CornerRadius(w * 0.03f, w * 0.03f),
        style = Stroke(width = strokeWidth * 0.85f)
    )

    // Head sphere
    drawCircle(
        color = fill,
        radius = w * 0.145f,
        center = Offset(w * 0.50f, h * 0.28f)
    )
    drawCircle(
        color = stroke,
        radius = w * 0.145f,
        center = Offset(w * 0.50f, h * 0.28f),
        style = Stroke(width = strokeWidth)
    )
    drawCircle(
        color = accent,
        radius = w * 0.045f,
        center = Offset(w * 0.45f, h * 0.24f)
    )

    drawBasePedestal(w, h, fill, stroke, accent, strokeWidth)
}

private fun DrawScope.drawRookShape(
    w: Float,
    h: Float,
    fill: Color,
    stroke: Color,
    accent: Color,
    strokeWidth: Float
) {
    val tower = Path().apply {
        moveTo(w * 0.26f, h * 0.78f)
        lineTo(w * 0.31f, h * 0.36f)
        lineTo(w * 0.24f, h * 0.36f)
        lineTo(w * 0.24f, h * 0.20f)
        lineTo(w * 0.34f, h * 0.20f)
        lineTo(w * 0.34f, h * 0.27f)
        lineTo(w * 0.44f, h * 0.27f)
        lineTo(w * 0.44f, h * 0.20f)
        lineTo(w * 0.56f, h * 0.20f)
        lineTo(w * 0.56f, h * 0.27f)
        lineTo(w * 0.66f, h * 0.27f)
        lineTo(w * 0.66f, h * 0.20f)
        lineTo(w * 0.76f, h * 0.20f)
        lineTo(w * 0.76f, h * 0.36f)
        lineTo(w * 0.69f, h * 0.36f)
        lineTo(w * 0.74f, h * 0.78f)
        close()
    }
    drawPath(tower, color = fill, style = Fill)
    drawPath(tower, color = stroke, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

    drawLine(
        color = accent,
        start = Offset(w * 0.34f, h * 0.44f),
        end = Offset(w * 0.66f, h * 0.44f),
        strokeWidth = strokeWidth * 0.8f,
        cap = StrokeCap.Round
    )

    drawBasePedestal(w, h, fill, stroke, accent, strokeWidth)
}

private fun DrawScope.drawKnightShape(
    w: Float,
    h: Float,
    fill: Color,
    stroke: Color,
    accent: Color,
    strokeWidth: Float
) {
    val horse = Path().apply {
        moveTo(w * 0.25f, h * 0.78f)
        cubicTo(w * 0.26f, h * 0.58f, w * 0.34f, h * 0.48f, w * 0.40f, h * 0.42f)
        lineTo(w * 0.24f, h * 0.40f)
        lineTo(w * 0.20f, h * 0.32f)
        lineTo(w * 0.36f, h * 0.22f)
        lineTo(w * 0.42f, h * 0.14f)
        lineTo(w * 0.50f, h * 0.20f)
        cubicTo(w * 0.68f, h * 0.22f, w * 0.76f, h * 0.38f, w * 0.73f, h * 0.78f)
        close()
    }
    drawPath(horse, color = fill, style = Fill)
    drawPath(horse, color = stroke, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

    // Eye
    drawCircle(
        color = stroke,
        radius = w * 0.028f,
        center = Offset(w * 0.36f, h * 0.28f)
    )
    // Mane curve
    val mane = Path().apply {
        moveTo(w * 0.53f, h * 0.26f)
        cubicTo(w * 0.63f, h * 0.34f, w * 0.65f, h * 0.50f, w * 0.63f, h * 0.68f)
    }
    drawPath(mane, color = accent, style = Stroke(width = strokeWidth * 0.9f, cap = StrokeCap.Round))

    drawBasePedestal(w, h, fill, stroke, accent, strokeWidth)
}

private fun DrawScope.drawBishopShape(
    w: Float,
    h: Float,
    fill: Color,
    stroke: Color,
    accent: Color,
    strokeWidth: Float
) {
    val mitre = Path().apply {
        moveTo(w * 0.26f, h * 0.78f)
        cubicTo(w * 0.30f, h * 0.58f, w * 0.34f, h * 0.50f, w * 0.38f, h * 0.46f)
        cubicTo(w * 0.26f, h * 0.36f, w * 0.32f, h * 0.20f, w * 0.50f, h * 0.17f)
        cubicTo(w * 0.68f, h * 0.20f, w * 0.74f, h * 0.36f, w * 0.62f, h * 0.46f)
        cubicTo(w * 0.66f, h * 0.50f, w * 0.70f, h * 0.58f, w * 0.74f, h * 0.78f)
        close()
    }
    drawPath(mitre, color = fill, style = Fill)
    drawPath(mitre, color = stroke, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

    // Top orb
    drawCircle(color = fill, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.13f))
    drawCircle(color = stroke, radius = w * 0.045f, center = Offset(w * 0.50f, h * 0.13f), style = Stroke(width = strokeWidth * 0.85f))

    // Mitre cross slash
    drawLine(
        color = stroke,
        start = Offset(w * 0.43f, h * 0.32f),
        end = Offset(w * 0.57f, h * 0.32f),
        strokeWidth = strokeWidth * 0.85f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = stroke,
        start = Offset(w * 0.50f, h * 0.25f),
        end = Offset(w * 0.50f, h * 0.39f),
        strokeWidth = strokeWidth * 0.85f,
        cap = StrokeCap.Round
    )

    drawBasePedestal(w, h, fill, stroke, accent, strokeWidth)
}

private fun DrawScope.drawQueenShape(
    w: Float,
    h: Float,
    fill: Color,
    stroke: Color,
    accent: Color,
    strokeWidth: Float
) {
    val crown = Path().apply {
        moveTo(w * 0.24f, h * 0.78f)
        lineTo(w * 0.16f, h * 0.28f)
        lineTo(w * 0.33f, h * 0.46f)
        lineTo(w * 0.38f, h * 0.22f)
        lineTo(w * 0.50f, h * 0.44f)
        lineTo(w * 0.62f, h * 0.22f)
        lineTo(w * 0.67f, h * 0.46f)
        lineTo(w * 0.84f, h * 0.28f)
        lineTo(w * 0.76f, h * 0.78f)
        close()
    }
    drawPath(crown, color = fill, style = Fill)
    drawPath(crown, color = stroke, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

    // Crown pearls
    val pearls = listOf(
        Offset(w * 0.16f, h * 0.24f),
        Offset(w * 0.38f, h * 0.18f),
        Offset(w * 0.62f, h * 0.18f),
        Offset(w * 0.84f, h * 0.24f)
    )
    for (pt in pearls) {
        drawCircle(color = fill, radius = w * 0.036f, center = pt)
        drawCircle(color = stroke, radius = w * 0.036f, center = pt, style = Stroke(width = strokeWidth * 0.8f))
    }

    drawLine(
        color = accent,
        start = Offset(w * 0.28f, h * 0.64f),
        end = Offset(w * 0.72f, h * 0.64f),
        strokeWidth = strokeWidth * 0.85f,
        cap = StrokeCap.Round
    )

    drawBasePedestal(w, h, fill, stroke, accent, strokeWidth)
}

private fun DrawScope.drawKingShape(
    w: Float,
    h: Float,
    fill: Color,
    stroke: Color,
    accent: Color,
    strokeWidth: Float
) {
    // Royal Cross on top
    drawLine(
        color = stroke,
        start = Offset(w * 0.50f, h * 0.07f),
        end = Offset(w * 0.50f, h * 0.23f),
        strokeWidth = strokeWidth * 1.15f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = stroke,
        start = Offset(w * 0.41f, h * 0.13f),
        end = Offset(w * 0.59f, h * 0.13f),
        strokeWidth = strokeWidth * 1.15f,
        cap = StrokeCap.Round
    )

    val body = Path().apply {
        moveTo(w * 0.24f, h * 0.78f)
        lineTo(w * 0.20f, h * 0.38f)
        cubicTo(w * 0.26f, h * 0.24f, w * 0.42f, h * 0.24f, w * 0.50f, h * 0.32f)
        cubicTo(w * 0.58f, h * 0.24f, w * 0.74f, h * 0.24f, w * 0.80f, h * 0.38f)
        lineTo(w * 0.76f, h * 0.78f)
        close()
    }
    drawPath(body, color = fill, style = Fill)
    drawPath(body, color = stroke, style = Stroke(width = strokeWidth, join = StrokeJoin.Round))

    // Jewel band
    drawLine(
        color = accent,
        start = Offset(w * 0.28f, h * 0.58f),
        end = Offset(w * 0.72f, h * 0.58f),
        strokeWidth = strokeWidth * 0.9f,
        cap = StrokeCap.Round
    )

    drawBasePedestal(w, h, fill, stroke, accent, strokeWidth)
}
