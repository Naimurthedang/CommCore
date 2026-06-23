package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CommViewModel
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.CyberMint
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.DarkSilver
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightSilver
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceCard
import kotlin.math.roundToInt

// Data model representing a multi-series telemetry day entry in the Recharts styled tracker
data class RechartsTelemetryDay(
    val dayNumber: Int,
    val confidenceScore: Float, // Series A (0-100)
    val pacingStability: Float,  // Series B (0-100, e.g. % of spoken words in optimal 120-150wpm range)
    val MilestonesGained: String? = null // Optional unlocked badge description
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RechartsMilestoneProgressTracker(viewModel: CommViewModel) {
    val context = LocalContext.current

    val roomTelemetryList by viewModel.rechartsTelemetryFlow.collectAsStateWithLifecycle()
    val dataList = remember(roomTelemetryList) {
        if (roomTelemetryList.isEmpty()) {
            listOf(
                RechartsTelemetryDay(1, 45f, 50f, "Speech Seedling"),
                RechartsTelemetryDay(3, 48f, 52f),
                RechartsTelemetryDay(5, 47f, 58f),
                RechartsTelemetryDay(7, 52f, 55f, "Active Pausing"),
                RechartsTelemetryDay(9, 56f, 62f),
                RechartsTelemetryDay(11, 54f, 60f),
                RechartsTelemetryDay(13, 61f, 67f),
                RechartsTelemetryDay(15, 65f, 64f, "Structure Anchor"),
                RechartsTelemetryDay(17, 63f, 70f),
                RechartsTelemetryDay(19, 68f, 72f),
                RechartsTelemetryDay(21, 72f, 75f),
                RechartsTelemetryDay(23, 76f, 78f, "Filler Guard"),
                RechartsTelemetryDay(25, 74f, 84f),
                RechartsTelemetryDay(27, 81f, 82f),
                RechartsTelemetryDay(29, 85f, 88f, "Elite Posture Mastery"),
                RechartsTelemetryDay(30, 89f, 91f)
            )
        } else {
            roomTelemetryList.map {
                RechartsTelemetryDay(
                    dayNumber = it.dayNumber,
                    confidenceScore = it.confidenceScore,
                    pacingStability = it.pacingStability,
                    MilestonesGained = it.milestonesGained
                )
            }
        }
    }

    // Interactive Legend Series toggles - simulating Recharts Legend interactivity
    var showConfidenceSeries by remember { mutableStateOf(true) }
    var showPacingSeries by remember { mutableStateOf(true) }
    var showMilestonesSeries by remember { mutableStateOf(true) }

    // Selected Hovered Index tracker (defaults to last element if none selected)
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    
    // Dynamic Simulator States
    var isSimulatingPractice by remember { mutableStateOf(false) }

    val daysCount = dataList.size
    val activeHoverDay = selectedIndex?.let { dataList.getOrNull(it) } ?: dataList.last()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recharts_progress_tracker_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(CyberMint, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "CO-SUITE PROGRESS METERS",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Dynamic dual-series communication fidelity analysis",
                        color = DarkSilver,
                        fontSize = 10.sp
                    )
                }
                
                // Recharts Status Badge
                Box(
                    modifier = Modifier
                        .background(CyberPurple.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .border(0.5.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        "RECHARTS CORE ENGINE",
                        color = CyberPurple,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- RECHARTS CHART DRAWING AREA (CANVAS) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MidnightBg, shape = RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, BorderGlass), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            // Touch & Slider Track Handler
                            detectTapGestures { offset ->
                                val w = size.width
                                val stepX = w / (daysCount - 1)
                                val index = (offset.x / stepX).roundToInt().coerceIn(0, daysCount - 1)
                                selectedIndex = index
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val w = size.width
                                    val stepX = w / (daysCount - 1)
                                    val index = (offset.x / stepX).roundToInt().coerceIn(0, daysCount - 1)
                                    selectedIndex = index
                                },
                                onDrag = { change, dragAmount ->
                                    val w = size.width
                                    val stepX = w / (daysCount - 1)
                                    val index = (change.position.x / stepX).roundToInt().coerceIn(0, daysCount - 1)
                                    selectedIndex = index
                                    change.consume()
                                },
                                onDragEnd = {
                                    // Keep selected state on end so user can read tooltip
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val spacingX = width / (daysCount - 1)

                    // 1. Draw horizontal grid coordinates (background grid lines)
                    val percentLines = listOf(0.25f, 0.5f, 0.75f, 1f)
                    percentLines.forEach { p ->
                        val y = height * p
                        drawLine(
                            color = BorderGlass.copy(alpha = 0.12f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f)
                        )
                    }

                    // 2. Draw vertical highlighted line for active selected timestamp index
                    selectedIndex?.let { index ->
                        val x = index * spacingX
                        drawLine(
                            color = Color.White.copy(alpha = 0.22f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // 3. Render SERIES A: Confidence Score (Area Chart styled)
                    if (showConfidenceSeries) {
                        val strokePath = Path()
                        val fillPath = Path()

                        dataList.forEachIndexed { i, entry ->
                            val x = i * spacingX
                            val y = height - ((entry.confidenceScore / 100f) * height)

                            if (i == 0) {
                                strokePath.moveTo(x, y)
                                fillPath.moveTo(x, height)
                                fillPath.lineTo(x, y)
                            } else {
                                strokePath.lineTo(x, y)
                                fillPath.lineTo(x, y)
                            }

                            if (i == daysCount - 1) {
                                fillPath.lineTo(x, height)
                                fillPath.close()
                            }
                        }

                        // Gradient fill under Confidence Area
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(CyberMint.copy(alpha = 0.20f), Color.Transparent)
                            )
                        )

                        // Smooth curve outline stroke
                        drawPath(
                            path = strokePath,
                            color = CyberMint,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // 4. Render SERIES B: Vocal Pacing Stability (Dotted/Dashed Line Chart styled)
                    if (showPacingSeries) {
                        val strokePath = Path()

                        dataList.forEachIndexed { i, entry ->
                            val x = i * spacingX
                            val y = height - ((entry.pacingStability / 100f) * height)

                            if (i == 0) {
                                strokePath.moveTo(x, y)
                            } else {
                                strokePath.lineTo(x, y)
                            }
                        }

                        // Dashed stroke representing pacing tempo
                        drawPath(
                            path = strokePath,
                            color = CyberPurple,
                            style = Stroke(
                                width = 2f.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        )
                    }

                    // 5. Draw highlighted data points & Milestones badges
                    dataList.forEachIndexed { i, entry ->
                        val x = i * spacingX
                        val isCurrentSelected = (selectedIndex == i)

                        // Series A Hub
                        if (showConfidenceSeries) {
                            val yConf = height - ((entry.confidenceScore / 100f) * height)
                            drawCircle(
                                color = if (isCurrentSelected) Color.White else CyberMint,
                                radius = if (isCurrentSelected) 5.dp.toPx() else 3.dp.toPx(),
                                center = Offset(x, yConf)
                            )
                        }

                        // Series B Hub
                        if (showPacingSeries) {
                            val yPac = height - ((entry.pacingStability / 100f) * height)
                            drawCircle(
                                color = if (isCurrentSelected) Color.White else CyberPurple,
                                radius = if (isCurrentSelected) 5.dp.toPx() else 3.dp.toPx(),
                                center = Offset(x, yPac)
                            )
                        }

                        // Milestones Star Indicators
                        if (showMilestonesSeries && entry.MilestonesGained != null) {
                            val yBase = if (showConfidenceSeries) {
                                height - ((entry.confidenceScore / 100f) * height)
                            } else {
                                height - ((entry.pacingStability / 100f) * height)
                            }
                            
                            // Golden milestone halo
                            drawCircle(
                                color = GoldAccent.copy(alpha = 0.4e-1f.plus(if (isCurrentSelected) 0.6f else 0.2f)),
                                radius = if (isCurrentSelected) 12.dp.toPx() else 8.dp.toPx(),
                                center = Offset(x, yBase)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- CHART TIMELINE LABELS (X-AXIS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Day 1", color = DarkSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Day 10", color = DarkSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Day 20", color = DarkSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Day 30", color = CyberMint, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- RECHARTS INTERACTIVE LEGEND (CLICK TO TOGGLE SERIES) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Legend A
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showConfidenceSeries = !showConfidenceSeries }
                        .testTag("legend_toggle_confidence")
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (showConfidenceSeries) CyberMint else Color.DarkGray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Confidence index",
                        color = if (showConfidenceSeries) LightSilver else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (showConfidenceSeries) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Legend B
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showPacingSeries = !showPacingSeries }
                        .testTag("legend_toggle_pacing")
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (showPacingSeries) CyberPurple else Color.DarkGray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Pacing Stability",
                        color = if (showPacingSeries) LightSilver else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (showPacingSeries) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Legend C
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showMilestonesSeries = !showMilestonesSeries }
                        .testTag("legend_toggle_milestones")
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (showMilestonesSeries) GoldAccent else Color.DarkGray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Milestones",
                        color = if (showMilestonesSeries) LightSilver else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = if (showMilestonesSeries) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- RECHARTS HOVER INSPECT COMPASS TOOLTIP CARD ---
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightBg),
                border = BorderStroke(1.dp, BorderGlass),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WEEK MONITOR: DAY ${activeHoverDay.dayNumber}",
                            color = CyberMint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Text(
                            text = if (selectedIndex == null) "PROMPT: HOVER / SLIDE CARD" else "NODE HOVERED",
                            color = Color.Gray,
                            fontSize = 8.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Communication confidence", color = Color.Gray, fontSize = 9.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(CyberMint, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "${activeHoverDay.confidenceScore.toInt()}%",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Speech flow tempo", color = Color.Gray, fontSize = 9.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(CyberPurple, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "${activeHoverDay.pacingStability.toInt()}% STABLE",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    // Locked/Unlocked Milestone badge description
                    activeHoverDay.MilestonesGained?.let { badge ->
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = BorderGlass)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(GoldAccent.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Milestone Gained",
                                tint = GoldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("MILESTONE COMPASS KEY DEPLOYED:", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(badge, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } ?: run {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "💡 Practice daily mock speech exercises below to unlock rare communication milestones here.",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- PRACTICE SIMULATOR (ADDS/DRAMATIZES DATA IN REAL-TIME) ---
            Button(
                onClick = {
                    isSimulatingPractice = true
                    Toast.makeText(context, "Analyzing synthetic vocal frame...", Toast.LENGTH_SHORT).show()
                },
                enabled = !isSimulatingPractice,
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("simulate_telemetry_practice_button")
            ) {
                if (isSimulatingPractice) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RECORDING VOICE PATTERNS...", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SIMULATE SPEAKING TRAINING EXERCISE", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            // Real-time addition generator
            if (isSimulatingPractice) {
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    
                    val lastEntry = dataList.last()
                    val nextDay = lastEntry.dayNumber + 1
                    
                    // Generate a high-quality improvement increment
                    val growthConf = (lastEntry.confidenceScore + (kotlin.random.Random.nextFloat() * 3f + 2f)).coerceIn(20f, 98f)
                    val growthPacing = (lastEntry.pacingStability + (kotlin.random.Random.nextFloat() * 9f - 3f)).coerceIn(25f, 96f)
                    
                    val optionalBadge = when (nextDay) {
                        31 -> "Sovereign Executive Voice"
                        32 -> "Dynamic Anchoring Ace"
                        else -> if (kotlin.random.Random.nextInt(3) == 0) "Empathetic Echo Badge 🌟" else null
                    }

                    viewModel.insertTelemetryPoint(
                        dayNumber = nextDay,
                        confidence = growthConf,
                        pacing = growthPacing,
                        badge = optionalBadge
                    )
                    isSimulatingPractice = false
                    Toast.makeText(context, "Fidelity assessment complete! Day $nextDay telemetry persisted offline.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
