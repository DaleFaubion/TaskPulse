package com.example.taskpulse.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary

import androidx.compose.ui.unit.TextUnit

@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 70.dp,
    strokeWidth: Dp = 7.dp,
    percentageFontSize: TextUnit = 16.sp,
    labelFontSize: TextUnit = 9.sp,
    showLabel: Boolean = true
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000),
        label = "ProgressAnimation"
    )

    val gradientBrush = Brush.sweepGradient(
        colors = listOf(NeonViolet, ElectricCyan, NeonViolet)
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Track background arc
            drawArc(
                color = GlassSurfaceVariant,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                brush = gradientBrush,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = percentageFontSize,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (showLabel) {
                Text(
                    text = "Done",
                    fontSize = labelFontSize,
                    color = TextMuted
                )
            }
        }
    }
}
