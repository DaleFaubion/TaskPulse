package com.example.taskpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.theme.AmberGlow
import com.example.taskpulse.theme.ElectricCyan
import com.example.taskpulse.theme.EmeraldGreen
import com.example.taskpulse.theme.FlameRose
import com.example.taskpulse.theme.GlassSurfaceVariant
import com.example.taskpulse.theme.TextMuted
import com.example.taskpulse.theme.TextPrimary
import com.example.taskpulse.theme.TextSecondary

@Composable
fun ProductivityBreakdownCard(
    totalCheckIns: Int,
    productiveHours: Int,
    rechargeHours: Int,
    neutralHours: Int,
    wastedHours: Int,
    avgRating: Float,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        contentPadding = 10.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "📊", fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Time Distribution & Focus",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                if (totalCheckIns > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⭐ ${"%.1f".format(avgRating)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGlow
                        )
                        Text(
                            text = " / 5",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (totalCheckIns == 0) {
                Text(
                    text = "No hourly entries logged yet today.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            } else {
                // Segmented Progress Bar
                val prodWeight = (productiveHours.toFloat() / totalCheckIns).coerceAtLeast(0.01f)
                val rechWeight = (rechargeHours.toFloat() / totalCheckIns).coerceAtLeast(0.01f)
                val neutWeight = (neutralHours.toFloat() / totalCheckIns).coerceAtLeast(0.01f)
                val wastWeight = (wastedHours.toFloat() / totalCheckIns).coerceAtLeast(0.01f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(GlassSurfaceVariant)
                ) {
                    if (productiveHours > 0) {
                        Box(
                            modifier = Modifier
                                .weight(prodWeight)
                                .fillMaxHeight()
                                .background(EmeraldGreen)
                        )
                    }
                    if (rechargeHours > 0) {
                        Box(
                            modifier = Modifier
                                .weight(rechWeight)
                                .fillMaxHeight()
                                .background(ElectricCyan)
                        )
                    }
                    if (neutralHours > 0) {
                        Box(
                            modifier = Modifier
                                .weight(neutWeight)
                                .fillMaxHeight()
                                .background(AmberGlow)
                        )
                    }
                    if (wastedHours > 0) {
                        Box(
                            modifier = Modifier
                                .weight(wastWeight)
                                .fillMaxHeight()
                                .background(FlameRose)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Breakdown Pills
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BreakdownItem(
                        emoji = "⚡",
                        label = "Productive",
                        count = productiveHours,
                        total = totalCheckIns,
                        color = EmeraldGreen
                    )
                    BreakdownItem(
                        emoji = "☕",
                        label = "Recharge",
                        count = rechargeHours,
                        total = totalCheckIns,
                        color = ElectricCyan
                    )
                    BreakdownItem(
                        emoji = "⚖️",
                        label = "Neutral",
                        count = neutralHours,
                        total = totalCheckIns,
                        color = AmberGlow
                    )
                    BreakdownItem(
                        emoji = "🛑",
                        label = "Wasted",
                        count = wastedHours,
                        total = totalCheckIns,
                        color = FlameRose
                    )
                }
            }
        }
    }
}

@Composable
private fun BreakdownItem(
    emoji: String,
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val pct = if (total > 0) (count * 100) / total else 0
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$emoji $pct%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${count}h",
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}
