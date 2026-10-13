package com.example.taskpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.data.model.HourlyCheckIn
import com.example.taskpulse.data.model.TimeClassification
import com.example.taskpulse.theme.AmberGlow
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.NeonViolet
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary

@Composable
fun HourlyCheckInRow(
    checkIn: HourlyCheckIn,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (classColor, classBg) = when (checkIn.classification) {
        TimeClassification.PRODUCTIVE -> Pair(EmeraldGreen, EmeraldGreen.copy(alpha = 0.2f))
        TimeClassification.RECHARGE -> Pair(ElectricCyan, ElectricCyan.copy(alpha = 0.2f))
        TimeClassification.NEUTRAL -> Pair(AmberGlow, AmberGlow.copy(alpha = 0.2f))
        TimeClassification.WASTED -> Pair(FlameRose, FlameRose.copy(alpha = 0.2f))
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        contentPadding = 8.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Time Badge & Classification Emoji Box
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(classBg)
            ) {
                Text(
                    text = checkIn.classification.emoji,
                    fontSize = 17.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Time Slot & Classification Badge & Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dayLabel = com.example.taskpulse.util.DateUtils.formatDisplayDate(checkIn.dateString)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GlassSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📅 $dayLabel",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = checkIn.timeSlot,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(classBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = checkIn.classification.displayName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = classColor
                        )
                    }

                    if (checkIn.categoryTag.isNotBlank()) {
                        Text(
                            text = "• ${checkIn.categoryTag}",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Summary Text
                Text(
                    text = checkIn.summary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Productivity Stars Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (i in 1..5) {
                        Text(
                            text = if (i <= checkIn.productivityRating) "★" else "☆",
                            fontSize = 13.sp,
                            color = if (i <= checkIn.productivityRating) AmberGlow else TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${checkIn.productivityRating}/5 Productivity",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Delete Button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Text(
                    text = "✕",
                    color = TextMuted,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
