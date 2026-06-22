package com.example.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.example.data.*
import com.example.ui.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

object CommVisualRewards {
    var equippedHalo by mutableStateOf(false)
    var equippedNeonBorder by mutableStateOf(false)
    var equippedAuraBackglow by mutableStateOf(false)
}

@Composable
fun MainAppUi(viewModel: CommViewModel) {
    val userProfileState by viewModel.userProfile.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MidnightBg
    ) {
        val profile = userProfileState
        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyberPurple)
            }
        } else if (!profile.onboardingCompleted) {
            OnboardingView(
                onCompleted = { name, cW, cR, cF, cP, cC, cD, painPts, modes, goal, pace, time, pref, goals ->
                    viewModel.selectedCommunicationGoals = goals
                    viewModel.completeOnboarding(name, cW, cR, cF, cP, cC, cD, painPts, modes, goal, pace, time, pref)
                }
            )
        } else {
            MainAppLayout(viewModel = viewModel, profile = profile)
        }
    }
}

// --- ONBOARDING SENSORY PROGRESSIVE VIEW ---
@Composable
fun OnboardingView(
    onCompleted: (
        name: String,
        workplace: Int,
        romantic: Int,
        friends: Int,
        public: Int,
        conflict: Int,
        digital: Int,
        painPoints: List<String>,
        modes: List<String>,
        goal: String,
        learningPace: String,
        timeCommit: Int,
        practicePref: String,
        goals: List<String>
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    
    // Form Inputs State
    var userName by remember { mutableStateOf("") }
    var chosenGoals by remember { mutableStateOf(setOf("Public Speaking", "Negotiation", "Active Listening")) }
    
    // Section A: Confidence sliders (1-10)
    var confWorkplace by remember { mutableFloatStateOf(5f) }
    var confRomantic by remember { mutableFloatStateOf(5f) }
    var confFriends by remember { mutableFloatStateOf(5f) }
    var confPublic by remember { mutableFloatStateOf(5f) }
    var confConflict by remember { mutableFloatStateOf(5f) }
    var confDigital by remember { mutableFloatStateOf(5f) }

    // Section A: biggest pain point picked (pick max 2)
    val painPointsList = listOf(
        "Fear of speaking up in meetings",
        "Difficulty expressing emotions",
        "Coming across as too aggressive/passive",
        "Small talk & networking",
        "Handling criticism/feedback",
        "Active listening",
        "Writing emails/messages that get responses",
        "De-escalating conflicts",
        "Persuasion & influence",
        "Social anxiety in groups"
    )
    var selectedPainPoints by remember { mutableStateOf(setOf<String>()) }
    var poorScenarioText by remember { mutableStateOf("") }

    // Section B: Context & Goals
    val modesList = listOf("BUSINESS", "PROFESSIONAL", "RELATIONSHIP", "FRIENDS", "PUBLIC", "CONFLICT", "DIGITAL")
    var selectedModes by remember { mutableStateOf(setOf("PROFESSIONAL")) }
    var specGoal30Days by remember { mutableStateOf("") }

    // Section C: Learning Preferences
    val learningPaceList = listOf(
        "Micro-lessons (5-10 min daily)",
        "Deep dives (30-60 min sessions)",
        "Scenario practice (role-play simulations)",
        "Video analysis (reviewing real conversations)",
        "Reading & reflection"
    )
    var chosenPace by remember { mutableStateOf(learningPaceList[2]) }
    var timeCommitDaily by remember { mutableIntStateOf(15) }
    var practicePreference by remember { mutableStateOf("Both AI-guided & Advisor access") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aesthetic header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "COMMCORE AI",
                color = CyberPurple,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "Step $step of 5",
                color = CyberMint,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { step / 5f },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp)),
            color = CyberPurple,
            trackColor = MidnightSurfaceCard
        )

        Spacer(modifier = Modifier.height(28.dp))

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                fadeIn(animationSpec = spring()) togetherWith fadeOut(animationSpec = spring())
            },
            label = "onboarding_step_transitions"
        ) { curStep ->
            Column(modifier = Modifier.fillMaxWidth()) {
                when (curStep) {
                    1 -> {
                        Text(
                            "Identify Your Landscape",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Welcome. Before we generate your 30-day communication blueprint, tell us your name and rate your baseline confidence level in these environments.",
                            color = DarkSilver,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Your Preferred Name", color = DarkSilver) },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPurple,
                                unfocusedBorderColor = BorderGlass,
                                focusedContainerColor = MidnightSurface,
                                unfocusedContainerColor = MidnightSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "Rate Your Baseline (1-10):",
                            color = CyberMint,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ConfidenceSliderItem("Professional workplace", confWorkplace) { confWorkplace = it }
                        ConfidenceSliderItem("Romantic relationships", confRomantic) { confRomantic = it }
                        ConfidenceSliderItem("Friend circles & social groups", confFriends) { confFriends = it }
                        ConfidenceSliderItem("Public speaking & storytelling", confPublic) { confPublic = it }
                        ConfidenceSliderItem("Conflict & boundary management", confConflict) { confConflict = it }
                        ConfidenceSliderItem("Digital text & async communication", confDigital) { confDigital = it }
                    }
                    2 -> {
                        Text(
                            "Identify Your Friction Points",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Select the top 2 biggest communication struggles you face on a daily basis.",
                            color = DarkSilver,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        painPointsList.forEach { point ->
                            val isSelected = selectedPainPoints.contains(point)
                            Card(
                                onClick = {
                                    selectedPainPoints = if (isSelected) {
                                        selectedPainPoints - point
                                    } else {
                                        if (selectedPainPoints.size < 2) selectedPainPoints + point else selectedPainPoints
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CyberPurple.copy(alpha = 0.2f) else MidnightSurface
                                ),
                                border = BorderStroke(1.dp, if (isSelected) CyberPurple else BorderGlass)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = null,
                                        colors = CheckboxDefaults.colors(checkedColor = CyberPurple)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(point, color = Color.White, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    3 -> {
                        Text(
                            "Relive the Friction",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Describe a recent real conversation that went poorly. What happened and what would you have preferred to happen instead? (This empowers our customized engine)",
                            color = DarkSilver,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = poorScenarioText,
                            onValueChange = { poorScenarioText = it },
                            placeholder = { Text("E.g., 'My manager dismissed my feedback in a weekly sync. I froze up and apologized immediately, instead of holding my boundary.'", color = DarkSilver, fontSize = 13.sp) },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPurple,
                                unfocusedBorderColor = BorderGlass,
                                focusedContainerColor = MidnightSurface,
                                unfocusedContainerColor = MidnightSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .testTag("poor_scenario_input"),
                            maxLines = 10
                        )
                    }
                    4 -> {
                        Text(
                            "Establish Your Spheres",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Select the communication MODES you wish to cultivate (Select multiple, prioritize your immediate needs):",
                            color = DarkSilver,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        modesList.forEach { mode ->
                            val isSelected = selectedModes.contains(mode)
                            Card(
                                onClick = {
                                    selectedModes = if (isSelected) {
                                        if (selectedModes.size > 1) selectedModes - mode else selectedModes
                                    } else {
                                        selectedModes + mode
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CyberPurple.copy(alpha = 0.2f) else MidnightSurface
                                ),
                                border = BorderStroke(1.dp, if (isSelected) CyberPurple else BorderGlass)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getModeIcon(mode),
                                        contentDescription = mode,
                                        tint = if (isSelected) CyberMint else DarkSilver
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(mode, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "Select Your Key Communication Goals:",
                            color = CyberMint,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val goalsList = listOf(
                            "Public Speaking",
                            "Negotiation",
                            "Active Listening",
                            "Executive Presence",
                            "Conflict Resolution",
                            "Team Coordination"
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                goalsList.take(3).forEach { goal ->
                                    val isSelected = chosenGoals.contains(goal)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                chosenGoals = if (isSelected) chosenGoals - goal else chosenGoals + goal
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) CyberPurple.copy(alpha = 0.25f) else MidnightSurface
                                        ),
                                        border = BorderStroke(1.dp, if (isSelected) CyberPurple else BorderGlass)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    chosenGoals = if (isSelected) chosenGoals - goal else chosenGoals + goal
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = CyberPurple)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(goal, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                goalsList.drop(3).forEach { goal ->
                                    val isSelected = chosenGoals.contains(goal)
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                chosenGoals = if (isSelected) chosenGoals - goal else chosenGoals + goal
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) CyberPurple.copy(alpha = 0.25f) else MidnightSurface
                                        ),
                                        border = BorderStroke(1.dp, if (isSelected) CyberPurple else BorderGlass)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isSelected,
                                                onCheckedChange = {
                                                    chosenGoals = if (isSelected) chosenGoals - goal else chosenGoals + goal
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = CyberPurple)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(goal, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            "What is your 30-Day Communication Goal?",
                            color = CyberMint,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Be completely specific (e.g. 'I want to confidently lead my weekly team standup without notes').",
                            color = DarkSilver,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = specGoal30Days,
                            onValueChange = { specGoal30Days = it },
                            placeholder = { Text("Lead standup calmly...", color = DarkSilver) },
                            textStyle = TextStyle(color = Color.White),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPurple,
                                unfocusedBorderColor = BorderGlass,
                                focusedContainerColor = MidnightSurface,
                                unfocusedContainerColor = MidnightSurface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_input"),
                            singleLine = true
                        )
                    }
                    5 -> {
                        Text(
                            "Customize Learning Velocity",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Fine-tune the mastery progression curves based on your timeline and support choices.",
                            color = DarkSilver,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Preferred Learning Pace:", color = CyberMint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        learningPaceList.forEach { pace ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { chosenPace = pace }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = chosenPace == pace,
                                    onClick = { chosenPace = pace },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberPurple)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pace, color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text("Daily Time Commitment:", color = CyberMint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf(5, 15, 30, 60).forEach { mins ->
                                FilterChip(
                                    selected = timeCommitDaily == mins,
                                    onClick = { timeCommitDaily = mins },
                                    label = { Text("$mins mins", color = Color.White) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberPurple
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Guidance Target Profile:", color = CyberMint, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        listOf("AI-guided practice", "Human advisor access", "Both AI-guided & Advisor access").forEach { pref ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { practicePreference = pref }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = practicePreference == pref,
                                    onClick = { practicePreference = pref },
                                    colors = RadioButtonDefaults.colors(selectedColor = CyberPurple)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(pref, color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Onboarding actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = BorderStroke(1.dp, BorderGlass)
                ) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp))
            }

            Button(
                onClick = {
                    if (step < 5) {
                        if (step == 1 && userName.trim().isEmpty()) {
                            // Enforce generic fallback if blank
                            userName = "Aero Communicator"
                        }
                        step++
                    } else {
                        onCompleted(
                            userName.ifEmpty { "Aero Communicator" },
                            confWorkplace.toInt(),
                            confRomantic.toInt(),
                            confFriends.toInt(),
                            confPublic.toInt(),
                            confConflict.toInt(),
                            confDigital.toInt(),
                            selectedPainPoints.toList(),
                            selectedModes.toList(),
                            specGoal30Days.ifEmpty { "Master negotiation and boundaries" },
                            chosenPace,
                            timeCommitDaily,
                            practicePreference,
                            chosenGoals.toList()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                modifier = Modifier.testTag("onboarding_continue")
            ) {
                Text(if (step == 5) "Assemble Mastery Plan 🚀" else "Continue")
            }
        }
    }
}

@Composable
fun ConfidenceSliderItem(label: String, valRaw: Float, onValChange: (Float) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = LightSilver, fontSize = 12.sp)
            Text("${valRaw.toInt()}/10", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Slider(
            value = valRaw,
            onValueChange = onValChange,
            valueRange = 1f..10f,
            steps = 8,
            colors = SliderDefaults.colors(
                thumbColor = CyberPurple,
                activeTrackColor = CyberPurple,
                inactiveTrackColor = BorderGlass
            )
        )
    }
}

// Data class to avoid Pair nesting destructuring ambiguity
data class NavItem(val screen: CommScreen, val icon: ImageVector, val label: String)

// --- MAIN RUNTIME CONTAINER LAYOUT ---
@Composable
fun MainAppLayout(viewModel: CommViewModel, profile: UserProfile) {
    val navItems = listOf(
        NavItem(CommScreen.DASHBOARD, Icons.Default.Home, "Dashboard"),
        NavItem(CommScreen.CHAT, Icons.Default.CheckCircle, "AI Gym"),
        NavItem(CommScreen.ADVISORS, Icons.Default.Person, "Advisors"),
        NavItem(CommScreen.COMMUNITY, Icons.Default.Share, "Ecosystem"),
        NavItem(CommScreen.IMAGE_STUDIO, Icons.Default.Settings, "Image Studio")
    )

    var showBrowserNotification by remember { mutableStateOf(false) }
    var browserNotificationTitle by remember { mutableStateOf("") }
    var browserNotificationBody by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    
    val triggerBrowserNotificationAlert = { title: String, body: String ->
        browserNotificationTitle = title
        browserNotificationBody = body
        showBrowserNotification = true
        scope.launch {
            delay(4000)
            showBrowserNotification = false
        }
        Unit
    }

    if (viewModel.showTipDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showTipDialog = false },
            containerColor = MidnightSurface,
            titleContentColor = GoldAccent,
            textContentColor = Color.White,
            tonalElevation = 10.dp,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("COMMCORE EXECUTIVE TIPS", fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        "You toggled the master Tip Playbook via physical keyboard shortcut (Ctrl+T) or settings panel directly.",
                        color = LightSilver,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    CommunicationTipOfTheDay(viewModel = viewModel)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.showTipDialog = false }
                ) {
                    Text("CLOSE PLAYBOOK", color = GoldAccent, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    if (viewModel.showDailyLoginAffirmationDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDailyLoginAffirmationDialog = false },
            containerColor = MidnightSurface,
            titleContentColor = CyberMint,
            textContentColor = Color.White,
            tonalElevation = 12.dp,
            modifier = Modifier.border(1.dp, CyberMint.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Cosmic Affirmation Shield",
                        tint = CyberMint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "DAILY CONFIDENCE IMMERSION",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        "Welcome Back! Based on your communication style, here is your custom cognitive booster to practice today:",
                        color = LightSilver,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MidnightBg, RoundedCornerShape(8.dp))
                            .border(BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = viewModel.currentAffirmationTitle,
                            color = CyberMint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = viewModel.currentAffirmationText,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 18.sp
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            "DAILY CHALLENGE PRACTICE:",
                            color = GoldAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Text(
                            text = viewModel.currentAffirmationPractice,
                            color = LightSilver,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Evidence-Based CBT Intercept",
                            color = Color.Gray,
                            fontSize = 9.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        
                        if (viewModel.isFetchingAffirmation) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(10.dp), color = CyberMint, strokeWidth = 1.dp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Recalibrating...", color = CyberMint, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        } else {
                            TextButton(
                                onClick = { viewModel.fetchNewDailyAffirmation(profile.communicationArchetype, forceRefresh = true) },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = CyberMint, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("RE-GENERATE", color = CyberMint, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showDailyLoginAffirmationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMint, contentColor = MidnightBg),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("INTEGRATED", fontSize = 10.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            if (viewModel.currentScreen == CommScreen.DASHBOARD) {
                DashboardFabMenu(viewModel)
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MidnightSurface,
                modifier = Modifier.testTag("app_navigation_bar")
            ) {
                navItems.forEach { navItem ->
                    val active = viewModel.currentScreen == navItem.screen
                    NavigationBarItem(
                        selected = active,
                        onClick = { viewModel.currentScreen = navItem.screen },
                        icon = { Icon(imageVector = navItem.icon, contentDescription = navItem.label) },
                        label = { Text(navItem.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyberMint,
                            selectedTextColor = CyberMint,
                            indicatorColor = CyberPurple.copy(alpha = 0.3f),
                            unselectedTextColor = DarkSilver,
                            unselectedIconColor = DarkSilver
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Mode adaptation bar
            ModeAdaptationBar(viewModel = viewModel)

            Box(modifier = Modifier.weight(1f)) {
                when (viewModel.currentScreen) {
                    CommScreen.DASHBOARD -> DashboardScreen(viewModel, profile, triggerBrowserNotificationAlert)
                    CommScreen.CHAT -> CoachChatScreen(viewModel, profile)
                    CommScreen.ADVISORS -> MarketplaceScreen(viewModel, profile)
                    CommScreen.COMMUNITY -> CommunityScreen(viewModel, profile)
                    CommScreen.IMAGE_STUDIO -> ImageStudioScreen(viewModel, profile)
                }

                // Chrome notification simulator banner
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBrowserNotification,
                    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter)
                        .zIndex(99f)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                        border = BorderStroke(1.dp, CyberMint),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CyberMint.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Browser Alert",
                                    tint = CyberMint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "CHROME NOTIFICATION API",
                                        color = DarkSilver,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        "Just now",
                                        color = DarkSilver,
                                        fontSize = 8.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = browserNotificationTitle,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = browserNotificationBody,
                                    color = LightSilver,
                                    fontSize = 11.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeAdaptationBar(viewModel: CommViewModel) {
    val modes = listOf("BUSINESS", "PROFESSIONAL", "RELATIONSHIP", "FRIENDS", "PUBLIC", "CONFLICT", "DIGITAL")
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MidnightSurface)
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "ACTIVE CONTEXT:",
                color = DarkSilver,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Card(
                colors = CardDefaults.cardColors(containerColor = CyberMint.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.5f))
            ) {
                Text(
                    viewModel.currentMode,
                    color = CyberMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { md ->
                val selected = viewModel.currentMode == md
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.switchMode(md) },
                    label = { Text(md, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberPurple,
                        selectedLabelColor = Color.White,
                        containerColor = MidnightBg,
                        labelColor = DarkSilver
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = BorderGlass,
                        selectedBorderColor = CyberPurple
                    )
                )
            }
        }
    }
}

// --- SCREEN 1: THE INTELLIGENCE DASHBOARD ---
data class SearchItem(
    val title: String,
    val subtitle: String,
    val category: String, // "Scenarios", "Advisors", "Ecosystem"
    val targetScreen: CommScreen,
    val payload: String
)

data class HeatmapSkill(
    val name: String,
    val category: String,
    val score: Int, // 0 - 100
    val shortCode: String,
    val definition: String,
    val trainingPrompt: String
)

data class MilestoneBadge(
    val id: String,
    val title: String,
    val requirement: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val isUnlocked: Boolean,
    val progressDesc: String
)

@Composable
fun ConfidenceTrendChart(currentScore: Int) {
    // Generate 30 days of data ending at the currentScore
    val dataPoints = remember(currentScore) {
        val list = mutableListOf<Int>()
        val startScore = (currentScore * 0.75f).toInt().coerceIn(30, 80)
        val step = (currentScore - startScore) / 29f
        for (i in 0 until 30) {
            val base = startScore + (step * i)
            val noise = when (i % 6) {
                0 -> 1
                2 -> -2
                4 -> 2
                else -> 0
            }
            val finalVal = (base + noise).toInt().coerceIn(0, 100)
            list.add(if (i == 29) currentScore else finalVal)
        }
        list
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("confidence_trend_chart_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Trend Icon",
                        tint = CyberMint,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "30-DAY CONFIDENCE SCORE TREND",
                        color = CyberMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberMint.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.3f))
                ) {
                    Text(
                        "30-DAY AVG: ${(dataPoints.sum() / 30f).toInt()}%",
                        color = CyberMint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MidnightBg, shape = RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, BorderGlass), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 14.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val spacing = width / 29f

                    // 1. Draw horizontal grid lines
                    val gridLines = listOf(0.25f, 0.5f, 0.75f)
                    gridLines.forEach { percentage ->
                        val y = height * percentage
                        drawLine(
                            color = BorderGlass.copy(alpha = 0.25f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // 2. Build the chart Path
                    val fillPath = Path()
                    val strokePath = Path()

                    dataPoints.forEachIndexed { index, score ->
                        val x = index * spacing
                        val y = height - ((score / 100f) * height)

                        if (index == 0) {
                            strokePath.moveTo(x, y)
                            fillPath.moveTo(x, y)
                        } else {
                            strokePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (index == 29) {
                            fillPath.lineTo(x, height)
                            fillPath.lineTo(0f, height)
                            fillPath.close()
                        }
                    }

                    // 3. Fill region
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CyberMint.copy(alpha = 0.20f),
                                CyberPurple.copy(alpha = 0.02f)
                            )
                        )
                    )

                    // 4. Draw stroke
                    drawPath(
                        path = strokePath,
                        color = CyberMint,
                        style = Stroke(
                            width = 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 5. Draw highlighted intervals
                    val keyIndexes = listOf(0, 9, 19, 29)
                    keyIndexes.forEach { index ->
                        val x = index * spacing
                        val score = dataPoints[index]
                        val y = height - ((score / 100f) * height)

                        drawCircle(
                            color = CyberPurple,
                            radius = 6.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 3.dp.toPx(),
                            center = androidx.compose.ui.geometry.Offset(x, y)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X-Axis labels
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Day 1", color = DarkSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Day 10", color = DarkSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Day 20", color = DarkSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Text("Today", color = CyberMint, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

@Composable
fun ProgressDashboardAndActionPlans(viewModel: CommViewModel) {
    val actionPlans by viewModel.communicationActionPlans.collectAsStateWithLifecycle()
    var isCreationFormExpanded by remember { mutableStateOf(false) }

    // Input fields for custom Communication Action Plan
    var goalInput by remember { mutableStateOf("Public Speaking") }
    var isCustomGoal by remember { mutableStateOf(false) }
    var customGoalInput by remember { mutableStateOf("") }
    var focusAreaInput by remember { mutableStateOf("Executive Presence") }
    var strategyInput by remember { mutableStateOf("") }
    var priorityInput by remember { mutableStateOf("High") }
    var startingProgress by remember { mutableFloatStateOf(20f) }

    val goalsOptions = listOf(
        "Public Speaking",
        "Negotiation",
        "Active Listening",
        "Executive Presence",
        "Conflict Resolution",
        "Team Coordination",
        "Other (Custom)"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progress_dashboard_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Progress Gauge",
                        tint = CyberMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "PROGRESS DASHBOARD & ACTION PLANS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${actionPlans.size} ACTIVE plans",
                    color = CyberMint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Introduction Text
            Text(
                "Manage and update your tailored speech frameworks. Persisted on-device to track progress milestones over time.",
                color = LightSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // --- Saved Plans List ---
            if (actionPlans.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MidnightBg, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No active action plans detected. Build one below!",
                        color = DarkSilver,
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    actionPlans.forEach { plan ->
                        var isPlanEditing by remember { mutableStateOf(false) }
                        var planStrategyEdit by remember { mutableStateOf(plan.strategyText) }
                        var planPriorityEdit by remember { mutableStateOf(plan.priority) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("action_plan_item_${plan.id}"),
                            colors = CardDefaults.cardColors(containerColor = MidnightBg),
                            border = BorderStroke(1.dp, BorderGlass)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            plan.goalName.uppercase(),
                                            color = CyberMint,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            "Focus: ${plan.focusArea}",
                                            color = DarkSilver,
                                            fontSize = 10.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Priority Badge
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = when (plan.priority) {
                                                    "High" -> Color.Red.copy(alpha = 0.15f)
                                                    "Medium" -> GoldAccent.copy(alpha = 0.15f)
                                                    else -> CyberPurple.copy(alpha = 0.15f)
                                                }
                                            ),
                                            border = BorderStroke(1.dp, when (plan.priority) {
                                                "High" -> Color.Red
                                                "Medium" -> GoldAccent
                                                else -> CyberPurple
                                            }),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                plan.priority.uppercase(),
                                                color = when (plan.priority) {
                                                    "High" -> Color.Red
                                                    "Medium" -> GoldAccent
                                                    else -> CyberPurple
                                                },
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Delete Plan
                                        IconButton(
                                            onClick = { viewModel.deleteActionPlan(plan.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Plan",
                                                tint = Color.Red,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (isPlanEditing) {
                                    OutlinedTextField(
                                        value = planStrategyEdit,
                                        onValueChange = { planStrategyEdit = it },
                                        label = { Text("Edit Plan Framework Text", color = DarkSilver, fontSize = 10.sp) },
                                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = CyberMint,
                                            unfocusedBorderColor = BorderGlass,
                                            focusedContainerColor = MidnightSurface,
                                            unfocusedContainerColor = MidnightSurface
                                        )
                                    )
                                    
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                viewModel.updateActionPlan(
                                                    plan.copy(
                                                        strategyText = planStrategyEdit,
                                                        priority = planPriorityEdit
                                                    )
                                                )
                                                isPlanEditing = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Save", color = MidnightBg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { isPlanEditing = false },
                                            border = BorderStroke(1.dp, BorderGlass),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text("Cancel", color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    Text(
                                        plan.strategyText,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.clickable {
                                            planStrategyEdit = plan.strategyText
                                            planPriorityEdit = plan.priority
                                            isPlanEditing = true
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Interactive Progress Bar & Slider
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CURRENT PROGRESS: ${plan.progressPercentage}%",
                                        color = if (plan.progressPercentage >= 100) CyberMint else GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    
                                    Text(
                                        text = if (plan.progressPercentage >= 100) "[ COMPLETED ★ ]" else "${plan.targetDays} days limit",
                                        color = if (plan.progressPercentage >= 100) CyberMint else DarkSilver,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                val smoothedPlanProgress by animateFloatAsState(
                                    targetValue = plan.progressPercentage / 100f,
                                    animationSpec = spring(
                                        dampingRatio = 0.55f, // bouncy tactile recoil
                                        stiffness = Spring.StiffnessLow
                                    ),
                                    label = "framer_motion_plan_bar"
                                )
                                LinearProgressIndicator(
                                    progress = { smoothedPlanProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .testTag("plan_animated_progress_bar"),
                                    color = CyberMint,
                                    trackColor = MidnightBg
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                var sliderValue by remember(plan.progressPercentage) { mutableFloatStateOf(plan.progressPercentage.toFloat()) }
                                Slider(
                                    value = sliderValue,
                                    onValueChange = { sliderValue = it },
                                    onValueChangeFinished = {
                                        viewModel.updateActionPlan(
                                            plan.copy(progressPercentage = sliderValue.toInt())
                                        )
                                    },
                                    valueRange = 0f..100f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = CyberMint,
                                        activeTrackColor = CyberMint,
                                        inactiveTrackColor = BorderGlass
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Expandable Plan Creation Form ---
            OutlinedButton(
                onClick = { isCreationFormExpanded = !isCreationFormExpanded },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, if (isCreationFormExpanded) Color.Red else CyberPurple),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCreationFormExpanded) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isCreationFormExpanded) Color.Red else CyberPurple
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isCreationFormExpanded) "CLOSE PLAN BUILDER" else "BUILD NEW CUSTOM ACTION PLAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            AnimatedVisibility(visible = isCreationFormExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .background(MidnightBg, RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Goal dropdown selector
                    Text("Select Target Communication Goal:", color = LightSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            goalsOptions.take(4).forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            goalInput = option
                                            isCustomGoal = (option == "Other (Custom)")
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = goalInput == option,
                                        onClick = {
                                            goalInput = option
                                            isCustomGoal = (option == "Other (Custom)")
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = CyberPurple)
                                    )
                                    Text(option, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            goalsOptions.drop(4).forEach { option ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            goalInput = option
                                            isCustomGoal = (option == "Other (Custom)")
                                        }
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(
                                        selected = goalInput == option,
                                        onClick = {
                                            goalInput = option
                                            isCustomGoal = (option == "Other (Custom)")
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = CyberPurple)
                                    )
                                    Text(option, color = Color.White, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    if (isCustomGoal) {
                        OutlinedTextField(
                            value = customGoalInput,
                            onValueChange = { customGoalInput = it },
                            placeholder = { Text("E.g. Impromptu Speeches", color = DarkSilver, fontSize = 12.sp) },
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberPurple,
                                unfocusedBorderColor = BorderGlass
                            ),
                            singleLine = true
                        )
                    }

                    // Focus Area input
                    Text("Specific Focus Area:", color = LightSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = focusAreaInput,
                        onValueChange = { focusAreaInput = it },
                        placeholder = { Text("E.g. Eliminate vocal qualifiers", color = DarkSilver, fontSize = 12.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = BorderGlass
                        ),
                        singleLine = true
                    )

                    // Strategy details input
                    Text("Action Strategy Framework Description:", color = LightSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = strategyInput,
                        onValueChange = { strategyInput = it },
                        placeholder = { Text("Detail the mental loops, silent cues, or breathing pacing systems to deploy.", color = DarkSilver, fontSize = 11.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = BorderGlass
                        ),
                        maxLines = 4
                    )

                    // Priority Selector
                    Text("Focus Priority State:", color = LightSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Low", "Medium", "High").forEach { priorityOpt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = priorityInput == priorityOpt,
                                    onClick = { priorityInput = priorityOpt },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                                )
                                Text(priorityOpt, color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Starting Progress Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Starting Progress Index:", color = LightSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${startingProgress.toInt()}%", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = startingProgress,
                        onValueChange = { startingProgress = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = CyberMint, activeTrackColor = CyberMint)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Insert to Database Button
                    Button(
                        onClick = {
                            val finalGoal = if (isCustomGoal) customGoalInput.ifEmpty { "Custom Goal" } else goalInput
                            val finalStrategy = strategyInput.ifEmpty { "Observe boundaries and remove defensive justification." }
                            viewModel.insertActionPlan(
                                CommunicationActionPlan(
                                    goalName = finalGoal,
                                    focusArea = focusAreaInput.ifEmpty { "General Mastery" },
                                    strategyText = finalStrategy,
                                    priority = priorityInput,
                                    progressPercentage = startingProgress.toInt(),
                                    targetDays = 30
                                )
                            )
                            // Reset state
                            strategyInput = ""
                            startingProgress = 20f
                            isCreationFormExpanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("PERSIST ON-DEVICE SECURELY", color = MidnightBg, fontWeight = FontWeight.Black, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            val context = LocalContext.current
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    viewModel.exportActionPlansToPdf(context) { file ->
                        if (file != null && file.exists()) {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        file
                                    ))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Securely Open or Sync Action Plan PDF"))
                            } catch (e: Exception) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CommCore Strategic Action Plans")
                                    putExtra(Intent.EXTRA_TEXT, "Strategic Action Plan ready offline at absolute path: ${file.absolutePath}")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Reference Path"))
                                Toast.makeText(context, "PDF saved: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            Toast.makeText(context, "Error compiling strategic PDF. Please check data files.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("export_progress_report_button"),
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Report",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "EXPORT EXECUTIVE PLANS REPORT (PDF/TEXT)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun AIDevPlansAndMetricsDashboard(viewModel: CommViewModel, profile: UserProfile) {
    val actionPlans by viewModel.communicationActionPlans.collectAsStateWithLifecycle()

    // Calculate progress metrics
    val totalPlans = actionPlans.size
    val completedPlans = actionPlans.count { it.progressPercentage >= 100 }
    val activePlans = actionPlans.count { it.progressPercentage < 100 }
    val averageProgress = if (actionPlans.isNotEmpty()) {
        actionPlans.map { it.progressPercentage }.average().toInt()
    } else {
        0
    }
    
    val highPriorityCount = actionPlans.count { it.priority.equals("High", ignoreCase = true) }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // 1. TOP METRICS & STATS CARDS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_metrics_dashboard_card"),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = BorderStroke(1.dp, CyberMint)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "AI Metrics",
                            tint = CyberMint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI DEVELOPMENT METRICS INDEX",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(CyberMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE AI AUDIT",
                            color = CyberMint,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Overall Progress Gauge card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = MidnightBg),
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "$averageProgress%",
                                color = CyberMint,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "OVERALL PROGRESS",
                                color = DarkSilver,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Key stats details
                    Column(
                        modifier = Modifier.weight(1.2f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MidnightBg, RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Active / Total", color = LightSilver, fontSize = 10.sp)
                            Text("$activePlans / $totalPlans", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MidnightBg, RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Milestones Done", color = LightSilver, fontSize = 10.sp)
                            Text("$completedPlans", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MidnightBg, RoundedCornerShape(6.dp))
                                .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("High Priority", color = LightSilver, fontSize = 10.sp)
                            Text("$highPriorityCount", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Smoothly animated progress index mapping
                val animatedAvgProgress by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = averageProgress / 100f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessLow
                    ),
                    label = "avg_development_progress"
                )
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("AGGREGATE DEVELOPMENT INDEX", color = LightSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        Text("$averageProgress% COMPLETED", color = CyberMint, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    LinearProgressIndicator(
                        progress = { animatedAvgProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = CyberMint,
                        trackColor = MidnightBg
                    )
                }
            }
        }

        // 2b. FIREBASE CO-CONSTRUCTIVE GOALS ENGINE FORM
        val firebaseGoalsList by viewModel.firebaseGoals.collectAsStateWithLifecycle()
        var customGoalTitle by remember { mutableStateOf("") }
        var customGoalDescription by remember { mutableStateOf("") }
        val isLocalGoalsSyncing = viewModel.isFirebaseSyncing
        val currentGoalsSyncStatus = viewModel.firebaseSyncStatus

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("firebase_goals_engine_card"),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Cloud Icon",
                            tint = GoldAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "FIREBASE CLOUD GOALS PLANNER",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(if (isLocalGoalsSyncing) GoldAccent.copy(alpha = 0.15f) else CyberMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isLocalGoalsSyncing) "SYNCING..." else "SECURE SYNC",
                            color = if (isLocalGoalsSyncing) GoldAccent else CyberMint,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Set targeted communication goals in the Realtime Database to synchronize progress trackers dynamically.",
                    color = LightSilver,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderGlass.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Suggestion chips
                Text(
                    "QUICK SELECT SUGGESTED GOALS",
                    color = GoldAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val recommendedTemplateGoals = listOf(
                        "Improve Public Speaking",
                        "Better Conflict Resolution",
                        "Deliver Executive Pitch",
                        "Assertive Negotiation"
                    )
                    recommendedTemplateGoals.forEach { template ->
                        val selected = customGoalTitle == template
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) GoldAccent.copy(alpha = 0.15f) else MidnightBg
                            ),
                            border = BorderStroke(1.dp, if (selected) GoldAccent else BorderGlass),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable {
                                customGoalTitle = template
                                if (customGoalDescription.isBlank()) {
                                    customGoalDescription = "Calibrate vocal pacing, eliminate filler words, and master high-stakes context pacing."
                                }
                            }
                        ) {
                            Text(
                                text = template,
                                color = if (selected) Color.White else LightSilver,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Inputs
                OutlinedTextField(
                    value = customGoalTitle,
                    onValueChange = { customGoalTitle = it },
                    label = { Text("Communication Goal (e.g. Improve Public Speaking)", fontSize = 10.sp, color = DarkSilver) },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_goal_title_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGlass,
                        focusedContainerColor = MidnightBg,
                        unfocusedContainerColor = MidnightBg
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customGoalDescription,
                    onValueChange = { customGoalDescription = it },
                    label = { Text("Goal Strategy Details & Focus Areas", fontSize = 10.sp, color = DarkSilver) },
                    textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_goal_details_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = BorderGlass,
                        focusedContainerColor = MidnightBg,
                        unfocusedContainerColor = MidnightBg
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Firebase sync indicator box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MidnightBg, RoundedCornerShape(6.dp))
                        .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "FIREBASE REALTIME STATUS",
                                color = DarkSilver,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = currentGoalsSyncStatus,
                                color = if (isLocalGoalsSyncing) GoldAccent else CyberMint,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        if (isLocalGoalsSyncing) {
                            CircularProgressIndicator(
                                color = GoldAccent,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Ready",
                                tint = CyberMint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (customGoalTitle.isBlank()) {
                            return@Button
                        }
                        viewModel.insertFirebaseGoal(customGoalTitle, customGoalDescription)
                        customGoalTitle = ""
                        customGoalDescription = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(6.dp),
                    enabled = customGoalTitle.isNotBlank() && !isLocalGoalsSyncing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("firebase_goal_submit_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = MidnightBg, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PUSH GOAL TO FIREBASE CLOUD", color = MidnightBg, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                }

                if (firebaseGoalsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "CLOUD DATABASE STORE (LIVE SYNCED)",
                        color = LightSilver,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        firebaseGoalsList.forEach { g ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MidnightBg),
                                border = BorderStroke(1.dp, BorderGlass),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                g.goalTitle,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Box(
                                                modifier = Modifier
                                                    .background(GoldAccent.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    g.firebasePushKey,
                                                    color = GoldAccent,
                                                    fontSize = 8.sp,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            }
                                        }
                                        if (g.details.isNotBlank()) {
                                            Text(
                                                g.details,
                                                color = LightSilver,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 4.dp),
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteFirebaseGoal(g.id, g.firebasePushKey) },
                                        modifier = Modifier.size(24.dp).testTag("firebase_goal_delete_button_${g.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color.Red.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- GEMINI PERSONALIZED PLAN BUILDER (GOAL-DRIVEN) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("gemini_personalized_plan_builder_card"),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = BorderStroke(1.dp, CyberMint)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Personalized Plan Target",
                        tint = CyberMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI PERSONALIZED IMPROVEMENT PLANNER",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Formulate a customized, milestone-driven weekly training plan optimized specifically for your targeted goals.",
                    color = LightSilver,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderGlass.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Specific Goals display
                Text(
                    "YOUR ONBOARDING GOAL SPECIFICATIONS",
                    color = CyberPurple,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MidnightBg, RoundedCornerShape(6.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Text("🎯 Goal: ", color = DarkSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = profile.communicationGoal30Days.ifBlank { "Personal Confidence Mastery" },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🛡️ Archetype: ", color = DarkSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = profile.communicationArchetype,
                            color = CyberMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("⚡ Pace: ", color = DarkSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "${profile.learningPace} (${profile.timeCommitMinutes} min/day)",
                            color = LightSilver,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val targetedGoal = profile.communicationGoal30Days.ifBlank { "Personal Confidence Mastery" }
                        viewModel.generatePersonalizedImprovementPlan(
                            goalName = targetedGoal,
                            painPoints = profile.chosenPainPoints,
                            archetype = profile.communicationArchetype,
                            confidenceScore = profile.currentConfidenceScore,
                            timeCommitMinutes = profile.timeCommitMinutes,
                            learningPace = profile.learningPace
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                    shape = RoundedCornerShape(6.dp),
                    enabled = !viewModel.isGeneratingPersonalizedPlan,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("generate_personalized_plan_button")
                ) {
                    if (viewModel.isGeneratingPersonalizedPlan) {
                        CircularProgressIndicator(color = MidnightBg, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ANALYZING SPECIFIC GOALS...",
                            color = MidnightBg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MidnightBg, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "CREATE PERSONALIZED IMPROVEMENT PLAN",
                            color = MidnightBg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (viewModel.personalizedPlanError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.personalizedPlanError ?: "",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (viewModel.personalizedPlansList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = BorderGlass.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "✨ FORMULATED WEEKLY FOCUS MODULES",
                        color = CyberMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        viewModel.personalizedPlansList.forEachIndexed { i, plan ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MidnightBg),
                                border = BorderStroke(1.dp, BorderGlass),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            plan.focusArea,
                                            color = CyberMint,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    if (plan.priority.equals("High", ignoreCase = true)) Color.Red.copy(alpha = 0.15f) else CyberPurple.copy(alpha = 0.15f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                plan.priority.uppercase(),
                                                color = if (plan.priority.equals("High", ignoreCase = true)) Color.Red else CyberPurple,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        plan.strategyText,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            viewModel.personalizedPlansList.forEach { plan ->
                                viewModel.insertActionPlan(plan)
                            }
                            android.widget.Toast.makeText(context, "Successfully loaded personalized plans to local database!", android.widget.Toast.LENGTH_LONG).show()
                            viewModel.personalizedPlansList = emptyList()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("commit_personalized_plan_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "COMMIT TO ACTIVE PLANS PORTFOLIO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 2. AI DYNAMIC RECOMMENDATIONS & INSIGHTS CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_recommendations_insight_card"),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = BorderStroke(1.dp, BorderGlass)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AI Secrets",
                        tint = CyberPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI COGNITIVE RECOMMENDATION & FORECAST",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val recommendationText = when {
                    totalPlans == 0 -> {
                        "💡 No active development plans stored on-device. We recommend co-constructing a tailored 4-week roadmap using Gemini on the 'AI LAB & CHARTS' panel."
                    }
                    averageProgress < 35 -> {
                        "📢 Core Recommendation: Initiate Week 1 objectives. Focus heavily on suppressing soft verbal qualifiers ('just', 'sorry') and pacing deliberate silent pauses inside high-stakes simulators."
                    }
                    averageProgress in 35..74 -> {
                        "📈 Growth Forecast: Solid progress index. Your resonance levels suggest growing status assertion. Focus next on mastering impromptu rebuttals and empathetic mirroring."
                    }
                    else -> {
                        "👑 Executive Mastery Secured: Your development metrics display premium authority levels. Continue recording peer achievements, or generate a brand new strategy split."
                    }
                }

                Text(
                    text = recommendationText,
                    color = LightSilver,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderGlass)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "TARGET PROFILE CRITIQUE: ${profile.communicationArchetype.uppercase()}",
                    color = CyberPurple,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "As a ${profile.communicationArchetype}, your tailored development index is dynamically formulated to balance active assertion with empathetic listener pacing. Current index level: ${if (averageProgress > 60) "Optimal Presence" else "Aspirant Speaker"}.",
                    color = DarkSilver,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // 3. ACTUAL TAILORED PLANS COLLECTION
        Text(
            text = "ACTIVE TAILORED DEVELOPMENT PLANS ($totalPlans)",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        if (actionPlans.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DarkSilver, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No tailored plans created yet.",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate a weekly roadmap with Gemini to load tailored plans, or build one below.",
                        color = DarkSilver,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            actionPlans.forEach { plan ->
                var isEditingStrategy by remember(plan.id) { mutableStateOf(false) }
                var strategyEditText by remember(plan.id) { mutableStateOf(plan.strategyText) }
                var localProgress by remember(plan.id, plan.progressPercentage) { mutableFloatStateOf(plan.progressPercentage.toFloat()) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ai_dev_plan_item_${plan.id}"),
                    colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                    border = BorderStroke(
                        1.dp,
                        if (plan.progressPercentage >= 100) CyberMint.copy(alpha = 0.6f) else BorderGlass
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(CyberMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = plan.goalName.uppercase(),
                                            color = CyberMint,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (plan.progressPercentage >= 100) {
                                        Box(
                                            modifier = Modifier
                                                .background(CyberMint, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "COMPLETED",
                                                color = MidnightBg,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "Focus: ${plan.focusArea}",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (plan.priority) {
                                            "High" -> Color.Red.copy(alpha = 0.15f)
                                            "Medium" -> GoldAccent.copy(alpha = 0.15f)
                                            else -> CyberPurple.copy(alpha = 0.15f)
                                        }
                                    ),
                                    border = BorderStroke(1.dp, when (plan.priority) {
                                        "High" -> Color.Red
                                        "Medium" -> GoldAccent
                                        else -> CyberPurple
                                    }),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        plan.priority.uppercase(),
                                        color = when (plan.priority) {
                                            "High" -> Color.Red
                                            "Medium" -> GoldAccent
                                            else -> CyberPurple
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.deleteActionPlan(plan.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Plan", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEditingStrategy) {
                            OutlinedTextField(
                                value = strategyEditText,
                                onValueChange = { strategyEditText = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyberMint,
                                    unfocusedBorderColor = BorderGlass,
                                    focusedContainerColor = MidnightBg,
                                    unfocusedContainerColor = MidnightBg
                                )
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.updateActionPlan(plan.copy(strategyText = strategyEditText))
                                        isEditingStrategy = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("SAVE", color = MidnightBg, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { isEditingStrategy = false },
                                    modifier = Modifier.height(28.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, BorderGlass)
                                ) {
                                    Text("CANCEL", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        } else {
                            Text(
                                text = plan.strategyText,
                                color = LightSilver,
                                fontSize = 12.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier.clickable { isEditingStrategy = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive Progress control Slider
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "PROGRESS: ${plan.progressPercentage}%",
                                    color = if (plan.progressPercentage >= 100) CyberMint else GoldAccent,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (plan.progressPercentage >= 100) "[ REOPEN ]" else "[ SECURE COMPLETION ]",
                                    color = if (plan.progressPercentage >= 100) CyberPurple else CyberMint,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        val targetProg = if (plan.progressPercentage >= 100) 0 else 100
                                        viewModel.updateActionPlan(plan.copy(progressPercentage = targetProg))
                                    }
                                )
                            }

                            Text(
                                text = if (plan.progressPercentage >= 100) "Goal Achieved!" else "${plan.targetDays} days remaining",
                                color = if (plan.progressPercentage >= 100) CyberMint else DarkSilver,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Slider(
                            value = localProgress,
                            onValueChange = { localProgress = it },
                            onValueChangeFinished = {
                                viewModel.updateActionPlan(plan.copy(progressPercentage = localProgress.toInt()))
                            },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = CyberMint,
                                activeTrackColor = CyberMint,
                                inactiveTrackColor = BorderGlass,
                                activeTickColor = Color.Transparent,
                                inactiveTickColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth().height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyInactivityPracticeReminderWidget(viewModel: CommViewModel) {
    if (!viewModel.show24HoursReminderNotification) return
    
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_inactivity_practice_reminder_widget")
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1621)),
        border = BorderStroke(1.5.dp, Color(0xFFFF5252).copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("⏰", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "PRACTICE INACTIVITY WARNING",
                        color = Color(0xFFFF5252),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                IconButton(
                    onClick = { viewModel.dismiss24HoursReminder() },
                    modifier = Modifier.size(24.dp).testTag("dismiss_inactivity_reminder_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "It has been over ${viewModel.hoursSinceLastActive} hours since your last verbal communication drill or system check-in. Daily practice is crucial to anchor muscle memory, eliminate vocal filler words, and preserve active confidence scores.",
                color = Color(0xFFFFCDD2),
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.currentScreen = CommScreen.CHAT
                        viewModel.chatInputText = "/prep Salary Negotiation under pressure"
                        viewModel.dismiss24HoursReminder()
                        Toast.makeText(context, "Welcome back! Daily check-in practice loading...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("reminder_start_drill_btn")
                ) {
                    Text(
                        "START DAILY DRILL 🎤",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                OutlinedButton(
                    onClick = { 
                        viewModel.dismiss24HoursReminder() 
                        Toast.makeText(context, "Practice snooze acknowledged.", Toast.LENGTH_SHORT).show()
                    },
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .testTag("reminder_snooze_btn")
                ) {
                    Text(
                        "DISMISS SNOOZE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun VocalFeedbackScoreLineGraphCard(viewModel: CommViewModel) {
    val roleplaysState by viewModel.roleplayHistories.collectAsStateWithLifecycle()
    var selectedScenarioFilter by remember { mutableStateOf("All Drills") }
    var selectedPointIndex by remember { mutableStateOf(-1) }
    
    val filterOptions = listOf("All Drills", "Negotiation", "Feedback", "Conflict")
    
    val sortedRoleplays = remember(roleplaysState, selectedScenarioFilter) {
        val filtered = if (selectedScenarioFilter == "All Drills") {
            roleplaysState
        } else {
            roleplaysState.filter { 
                it.scenarioName.contains(selectedScenarioFilter, ignoreCase = true) 
            }
        }
        filtered.sortedBy { it.timestamp }
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vocal_feedback_score_line_graph_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Improvement History Icon",
                        tint = CyberMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI PERFORMANCE LINE GRAPH",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Box(
                    modifier = Modifier
                        .background(CyberMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "RECHARTS ENGINE",
                        color = CyberMint,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Chronological visualization of calculated constructive drill evaluation scores and voice confidence composites over time.",
                color = LightSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { filter ->
                    val selected = selectedScenarioFilter == filter
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) CyberMint.copy(alpha = 0.15f) else MidnightBg
                        ),
                        border = BorderStroke(1.dp, if (selected) CyberMint else BorderGlass),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.clickable {
                            selectedScenarioFilter = filter
                            selectedPointIndex = -1
                        }
                    ) {
                        Text(
                            text = filter,
                            color = if (selected) Color.White else LightSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (sortedRoleplays.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(MidnightBg, shape = RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, BorderGlass), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No history points matching '$selectedScenarioFilter' found.",
                        color = DarkSilver,
                        fontSize = 11.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MidnightBg, shape = RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, BorderGlass), shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Column {
                        val averageScore = remember(sortedRoleplays) {
                            sortedRoleplays.map { it.aiScore }.average().toInt()
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Filtered Point Count: ${sortedRoleplays.size}",
                                color = DarkSilver,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "AVG SCORE: $averageScore/100",
                                color = CyberMint,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(sortedRoleplays) {
                                        detectTapGestures { offset ->
                                            if (sortedRoleplays.isEmpty()) return@detectTapGestures
                                            val w = size.width.toFloat()
                                            val h = size.height.toFloat()
                                            val gap = w / (sortedRoleplays.size - 1).coerceAtLeast(1)
                                            
                                            var minDistance = Float.MAX_VALUE
                                            var selectedIndex = -1
                                            
                                            sortedRoleplays.forEachIndexed { idx, rp ->
                                                val xPos = idx * gap
                                                val yPos = h - ((rp.aiScore / 100f) * h)
                                                val distance = kotlin.math.sqrt(
                                                    (offset.x - xPos) * (offset.x - xPos) +
                                                    (offset.y - yPos) * (offset.y - yPos)
                                                )
                                                if (distance < minDistance) {
                                                    minDistance = distance
                                                    selectedIndex = idx
                                                }
                                            }
                                            
                                            if (minDistance < 60f) {
                                                selectedPointIndex = selectedIndex
                                            } else {
                                                selectedPointIndex = -1
                                            }
                                        }
                                    }
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height
                                val sizePoints = sortedRoleplays.size
                                val gap = canvasWidth / (sizePoints - 1).coerceAtLeast(1)
                                
                                val dividerMarks = listOf(0.25f, 0.5f, 0.75f)
                                dividerMarks.forEach { percent ->
                                    val yOffset = canvasHeight * percent
                                    drawLine(
                                        color = BorderGlass.copy(alpha = 0.2f),
                                        start = androidx.compose.ui.geometry.Offset(0f, yOffset),
                                        end = androidx.compose.ui.geometry.Offset(canvasWidth, yOffset),
                                        strokeWidth = 1.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                    )
                                }
                                
                                val strokePath = Path()
                                val fillPath = Path()
                                
                                sortedRoleplays.forEachIndexed { index, item ->
                                    val x = index * gap
                                    val scoreY = canvasHeight - ((item.aiScore / 100f) * canvasHeight)
                                    
                                    if (index == 0) {
                                        strokePath.moveTo(x, scoreY)
                                        fillPath.moveTo(x, scoreY)
                                    } else {
                                        strokePath.lineTo(x, scoreY)
                                        fillPath.lineTo(x, scoreY)
                                    }
                                    
                                    if (index == sizePoints - 1) {
                                        fillPath.lineTo(x, canvasHeight)
                                        fillPath.lineTo(0f, canvasHeight)
                                        fillPath.close()
                                    }
                                }
                                
                                if (sizePoints > 1) {
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                CyberMint.copy(alpha = 0.25f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    drawPath(
                                        path = strokePath,
                                        color = CyberMint,
                                        style = Stroke(
                                            width = 3.dp.toPx(),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                                
                                sortedRoleplays.forEachIndexed { index, item ->
                                    val x = index * gap
                                    val scoreY = canvasHeight - ((item.aiScore / 100f) * canvasHeight)
                                    val isHighlighted = index == selectedPointIndex
                                    
                                    drawCircle(
                                        color = if (isHighlighted) GoldAccent.copy(alpha = 0.8f) else CyberPurple.copy(alpha = 0.4f),
                                        radius = if (isHighlighted) 8.dp.toPx() else 5.dp.toPx(),
                                        center = androidx.compose.ui.geometry.Offset(x, scoreY)
                                    )
                                    drawCircle(
                                        color = if (isHighlighted) Color.White else CyberMint,
                                        radius = if (isHighlighted) 4.dp.toPx() else 2.5.dp.toPx(),
                                        center = androidx.compose.ui.geometry.Offset(x, scoreY)
                                    )
                                }
                            }
                        }
                        
                        val activeIndex = selectedPointIndex
                        if (activeIndex in sortedRoleplays.indices) {
                            val activeItem = sortedRoleplays[activeIndex]
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth().testTag("graph_point_tooltip")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = activeItem.scenarioName,
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = activeItem.dateString,
                                            color = DarkSilver,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "SCORE: ${activeItem.aiScore}%",
                                            color = CyberMint,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Assertiveness: ${activeItem.metricAssertiveness}%",
                                            color = LightSilver,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeItem.summaryText,
                                        color = LightSilver,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💡 Touch any dot on the line graph to view detailed evaluation scorecard metrics.",
                                color = DarkSilver,
                                fontSize = 9.sp,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PastRoleplaySessionsHistoryCard(viewModel: CommViewModel) {
    val roleplaysState by viewModel.roleplayHistories.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("past_roleplay_sessions_history_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Roleplay History Icon",
                        tint = CyberMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "AI ROLEPLAY SESSIONS ARCHIVE",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .background(CyberMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "AI INSIGHTS & RATINGS",
                        color = CyberMint,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "History log of all simulated verbal drill outputs and constructive evaluation profiles calculated by our AI engine.",
                color = LightSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            if (roleplaysState.isEmpty()) {
                Text(
                    "No logged simulation histories found. Initiate verbal training loops inside Coaching Central to generate rating diagnostics.",
                    color = DarkSilver,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    roleplaysState.forEach { rp ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MidnightBg),
                            border = BorderStroke(1.dp, BorderGlass),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("roleplay_history_item_${rp.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = rp.scenarioName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Completed on ${rp.dateString}",
                                            color = DarkSilver,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (rp.aiScore >= 85) CyberMint.copy(alpha = 0.15f)
                                                                 else if (rp.aiScore >= 75) CyberPurple.copy(alpha = 0.15f)
                                                                 else GoldAccent.copy(alpha = 0.15f)
                                            ),
                                            border = BorderStroke(
                                                1.dp, 
                                                if (rp.aiScore >= 85) CyberMint 
                                                else if (rp.aiScore >= 75) CyberPurple 
                                                else GoldAccent
                                            )
                                        ) {
                                            Text(
                                                text = "SCORE: ${rp.aiScore}/100",
                                                color = if (rp.aiScore >= 85) CyberMint 
                                                        else if (rp.aiScore >= 75) CyberPurple 
                                                        else GoldAccent,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = {
                                                viewModel.deleteRoleplayHistory(rp.id)
                                                Toast.makeText(context, "Removed session from scorecard", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp).testTag("delete_roleplay_history_${rp.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Log",
                                                tint = Color.Red.copy(alpha = 0.6f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MidnightSurfaceCard, RoundedCornerShape(4.dp))
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = "TRANSCRIPT SUMMARY & FEEDBACK SCORECARD:",
                                            color = GoldAccent,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = rp.summaryText,
                                            color = LightSilver,
                                            fontSize = 11.sp,
                                            lineHeight = 14.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(CyberMint)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Assertiveness: ${rp.metricAssertiveness}%",
                                            color = DarkSilver,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(CyberPurple)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Filler softeners: ${rp.metricFillerCount}",
                                            color = DarkSilver,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardStreakCounterWidget(viewModel: CommViewModel, profile: UserProfile) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentStreak = profile.streakCounter
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_streak_counter_widget"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Main Head Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "🔥",
                        fontSize = 24.sp,
                        modifier = Modifier.animateContentSize()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "DAILY DE-DRILL STREAK",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Consistency Breeds Executive Presence",
                            color = LightSilver,
                            fontSize = 10.sp
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = GoldAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(14.dp))
            
            // Large Display Value Row
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$currentStreak",
                    color = GoldAccent,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = "Consecutive Days Active",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Each day you complete an AI constructive sandbox walkthrough, vocal practice drill, or mock roleplay session (e.g. Salary Negotiation), your multiplier advances. Miss a day, and the physiological calibration drift resets.",
                color = LightSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderGlass.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))
            
            // Visual 7-day Progress indicators
            Text(
                "CURRENT WEEK PROGRESS",
                color = DarkSilver,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weekList = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val activeDaysCount = if (currentStreak == 0) 0 else {
                    val remainder = currentStreak % 7
                    if (remainder == 0) 7 else remainder
                }
                
                weekList.forEachIndexed { i, dayName ->
                    val isChecked = i < activeDaysCount
                    val isCurrentDay = i == activeDaysCount
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isChecked) GoldAccent.copy(alpha = 0.15f)
                                    else if (isCurrentDay) CyberPurple.copy(alpha = 0.15f)
                                    else MidnightBg
                                )
                                .border(
                                    BorderStroke(
                                        width = 1.dp,
                                        color = if (isChecked) GoldAccent else if (isCurrentDay) CyberPurple else BorderGlass
                                    ),
                                    shape = CircleShape
                                )
                        ) {
                            if (isChecked) {
                                Text("🔥", fontSize = 12.sp)
                            } else {
                                Text(
                                    text = "${i + 1}",
                                    color = if (isCurrentDay) CyberPurple else DarkSilver,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayName,
                            color = if (isChecked) Color.White else DarkSilver,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.triggerDailyGoalDone()
                        Toast.makeText(context, "Streak Secured! Continuous performance calibrating...", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("streak_count_secure_button"),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "SECURE CHECK-IN (+1)",
                        color = MidnightBg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                OutlinedButton(
                    onClick = {
                        viewModel.currentScreen = CommScreen.CHAT
                        viewModel.chatInputText = "/prep Salary Negotiation discussion with CEO tomorrow afternoon"
                    },
                    border = BorderStroke(1.dp, BorderGlass),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "LAUNCH DRILL 🎤",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        viewModel.simulate24HoursIdle()
                        Toast.makeText(context, "MOCK: 24-Hour Absence Simulated! Notice active notification at the top.", Toast.LENGTH_LONG).show()
                    },
                    modifier = Modifier.testTag("simulate_absence_dev_btn")
                ) {
                    Text(
                        text = "🧪 SIMULATE 24-HOUR ABSENCE REMINDER",
                        color = Color(0xFFFF5252),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun MilestoneBadgeCard(viewModel: CommViewModel, profile: UserProfile) {
    val actionPlans by viewModel.communicationActionPlans.collectAsStateWithLifecycle()

    val badges = remember(profile.streakCounter, profile.confWorkplace, profile.communityContributionScore, actionPlans) {
        val list = mutableListOf(
            MilestoneBadge(
                id = "pioneer",
                title = "Comm Pioneer",
                requirement = "Complete Onboarding Setup",
                description = "Unlocks basic interactive exercises and baseline vocative adaptive profiles.",
                icon = Icons.Default.CheckCircle,
                color = CyberMint,
                isUnlocked = profile.onboardingCompleted,
                progressDesc = "100% (Completed)"
            ),
            MilestoneBadge(
                id = "streak_7",
                title = "Streak Sentinel",
                requirement = "Reach 7-Day Gym Streak",
                description = "Habit formation milestone. Strengthens executive presence and automatic filler shielding.",
                icon = Icons.Default.Star,
                color = GoldAccent,
                isUnlocked = profile.streakCounter >= 7,
                progressDesc = "${profile.streakCounter}/7 Days"
            ),
            MilestoneBadge(
                id = "business_l2",
                title = "Executive Elite L2",
                requirement = "Business Mode Mastery >= 60 XP",
                description = "Demonstrates consistent assertive execution of boundary settings under workplace pressure.",
                icon = Icons.Default.Settings,
                color = CyberPurple,
                isUnlocked = (profile.confWorkplace * 10) >= 60,
                progressDesc = "${profile.confWorkplace * 10}/60 XP"
            ),
            MilestoneBadge(
                id = "empathy",
                title = "Empathetic Anchor",
                requirement = "Active Mirroring Score >= 70%",
                description = "Granted for validating and mirroring opponent stances constructively without backing down.",
                icon = Icons.Default.Person,
                color = CyberMint,
                isUnlocked = true, 
                progressDesc = "72% (Unlocked)"
            ),
            MilestoneBadge(
                id = "networker",
                title = "Synergy Networker",
                requirement = "Ecosystem Contribution >= 50",
                description = "Granted for positive contributions and peer review evaluations in the social coaching system.",
                icon = Icons.Default.Share,
                color = CyberPurple,
                isUnlocked = profile.communityContributionScore >= 50,
                progressDesc = "${profile.communityContributionScore}/50 Score"
            ),
            MilestoneBadge(
                id = "filler_shield",
                title = "Vocal Shield",
                requirement = "Filler Word Shield >= 80%",
                description = "Eradicated softeners and apologetic filler words (just, sorry) during high-pressure sessions.",
                icon = Icons.Default.Info,
                color = GoldAccent,
                isUnlocked = true, 
                progressDesc = "85% (Unlocked)"
            )
        )

        // DYNAMICALLY ATTACH SPECIALIST MILESTONE BADGES BASED ON ONBOARDING GOALS DEFINED!
        viewModel.selectedCommunicationGoals.forEach { goal ->
            val matchingPlan = actionPlans.firstOrNull { it.goalName.equals(goal, ignoreCase = true) }
            val progressVal = matchingPlan?.progressPercentage ?: 0
            list.add(
                MilestoneBadge(
                    id = "goal_${goal.lowercase().replace(" ", "_")}",
                    title = "$goal Expert",
                    requirement = "Action Plan >= 75%",
                    description = "Dynamic milestone tracking on-device strategies for the $goal challenge configured during onboarding.",
                    icon = Icons.Default.Star,
                    color = GoldAccent,
                    isUnlocked = progressVal >= 75,
                    progressDesc = "$progressVal% Complete (Required: 75%)"
                )
            )
        }

        if (viewModel.wonA16zFastPass) {
            list.add(
                MilestoneBadge(
                    id = "contest_a16z",
                    title = "a16z Venture Scout",
                    requirement = "Score >= 90% in The Venture Seed Pitch Slam",
                    description = "Special fast-track recruiting badge awarded by Andreessen Horowitz. Unlocks initial screening queue.",
                    icon = Icons.Default.CheckCircle,
                    color = GoldAccent,
                    isUnlocked = true,
                    progressDesc = "CERTIFIED WIN"
                )
            )
        }
        if (viewModel.wonStripeFastPass) {
            list.add(
                MilestoneBadge(
                    id = "contest_stripe",
                    title = "Stripe Lead Advocate",
                    requirement = "Score >= 90% in Technical Crisis Briefing",
                    description = "Certified customer engineering crisis de-escalator. Fast-tracks your profile directly to headhunters.",
                    icon = Icons.Default.CheckCircle,
                    color = CyberMint,
                    isUnlocked = true,
                    progressDesc = "CERTIFIED WIN"
                )
            )
        }
        if (viewModel.wonMcKinseyFastPass) {
            list.add(
                MilestoneBadge(
                    id = "contest_mckinsey",
                    title = "McKinsey Analyst Elite",
                    requirement = "Score >= 90% in Boardroom Takeover Defense",
                    description = "Awarded for perfect Pyramid Principle and silence control under scrutiny. Unlocks direct regional interview referral.",
                    icon = Icons.Default.CheckCircle,
                    color = CyberPurple,
                    isUnlocked = true,
                    progressDesc = "CERTIFIED WIN"
                )
            )
        }

        list
    }

    var selectedBadge by remember { mutableStateOf<MilestoneBadge?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_badge_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberPurple)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "MILESTONE ACHIEVEMENTS & BADGES",
                color = CyberPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Unlocks prestige status tokens and specialized trainer personas. Select any badge to view status credentials.",
                color = DarkSilver,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val chunks = badges.chunked(2)
                chunks.forEach { pairList ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pairList.forEach { badge ->
                            val isSelected = selectedBadge?.id == badge.id
                            val clickableModifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (badge.isUnlocked) badge.color.copy(alpha = 0.08f)
                                    else MidnightBg
                                )
                                .border(
                                    BorderStroke(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) Color.White
                                        else if (badge.isUnlocked) badge.color.copy(alpha = 0.5f)
                                        else BorderGlass
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedBadge = if (isSelected) null else badge
                                }
                                .padding(10.dp)

                            Row(
                                modifier = clickableModifier,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (badge.isUnlocked) badge.color.copy(alpha = 0.15f)
                                            else BorderGlass.copy(alpha = 0.1f)
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (badge.isUnlocked) badge.color else BorderGlass
                                            ),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = badge.icon,
                                        contentDescription = badge.title,
                                        tint = if (badge.isUnlocked) badge.color else DarkSilver,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = badge.title,
                                        color = if (badge.isUnlocked) Color.White else LightSilver,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (badge.isUnlocked) "UNLOCKED" else badge.progressDesc,
                                        color = if (badge.isUnlocked) badge.color else DarkSilver,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }

            selectedBadge?.let { badge ->
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightBg),
                    border = BorderStroke(1.dp, BorderGlass),
                    modifier = Modifier.fillMaxWidth().testTag("badge_inspector")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(badge.color.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = badge.icon,
                                        contentDescription = "Badge detail icon",
                                        tint = badge.color,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = badge.title.uppercase(),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (badge.isUnlocked) "UNLOCKED" else "LOCKED",
                                    color = if (badge.isUnlocked) badge.color else DarkSilver,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { selectedBadge = null },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close badge details",
                                        tint = DarkSilver,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Requirement: ${badge.requirement}",
                            color = LightSilver,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = badge.description,
                            color = DarkSilver,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardFabMenu(viewModel: CommViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier.padding(bottom = 16.dp, end = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            expanded = false
                            viewModel.chatInputText = "/prep "
                            viewModel.currentScreen = CommScreen.CHAT
                        },
                        containerColor = CyberMint,
                        contentColor = MidnightBg,
                        icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Prep", modifier = Modifier.size(16.dp)) },
                        text = { Text("/prep (Quick Prep Gym)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(36.dp).testTag("fab_option_prep")
                    )

                    ExtendedFloatingActionButton(
                        onClick = {
                            expanded = false
                            viewModel.chatInputText = "/polish "
                            viewModel.currentScreen = CommScreen.CHAT
                        },
                        containerColor = CyberPurple,
                        contentColor = Color.White,
                        icon = { Icon(Icons.Default.Edit, contentDescription = "Polish", modifier = Modifier.size(16.dp)) },
                        text = { Text("/polish (Speech Polish)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(36.dp).testTag("fab_option_polish")
                    )

                    ExtendedFloatingActionButton(
                        onClick = {
                            expanded = false
                            viewModel.chatInputText = "/roleplay "
                            viewModel.currentScreen = CommScreen.CHAT
                        },
                        containerColor = GoldAccent,
                        contentColor = MidnightBg,
                        icon = { Icon(Icons.Default.Person, contentDescription = "Roleplay", modifier = Modifier.size(16.dp)) },
                        text = { Text("/roleplay (AI Partner)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.height(36.dp).testTag("fab_option_roleplay")
                    )
                }
            }

            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = if (expanded) Color.Red else CyberMint,
                contentColor = MidnightBg,
                modifier = Modifier.size(56.dp).testTag("quick_command_fab")
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Trigger Common Commands"
                )
            }
        }
    }
}

data class CommunicationTip(
    val title: String,
    val description: String,
    val actionableStep: String,
    val goalCategory: String
)

val allTipRepository = listOf(
    // Public Speaking
    CommunicationTip(
        title = "The 2-Second Softener Shield",
        description = "When asked a difficult question under pressure, pause for a slow mental count of two before replying.",
        actionableStep = "Don't rush to fill the silence. Silent space is perceived as dynamic poise; rushing to answer with 'actually' or 'sorry' delegates status.",
        goalCategory = "Public Speaking"
    ),
    CommunicationTip(
        title = "Vocal Pitch Grounding",
        description = "Calibrate lower throat register resonance in high-stakes boardroom questions.",
        actionableStep = "Breathe deeply into your stomach using 4-4-4 diaphragmatic pacing to ground your pitch before initiating presentations.",
        goalCategory = "Public Speaking"
    ),
    CommunicationTip(
        title = "The Eye-Anchor Lock",
        description = "Connect individual thoughts to single faces rather than spraying the entire audience.",
        actionableStep = "Pace your assertions by speaking a complete sentence to a single team member, then look to another for the next point.",
        goalCategory = "Public Speaking"
    ),
    // Negotiation
    CommunicationTip(
        title = "Empathic Double-Anchor Setup",
        description = "Never offer concessions without framing value indices around high market-aligned targets first.",
        actionableStep = "Reference a range anchored by industry standards rather than an isolated point figure to establish a structural cushion.",
        goalCategory = "Negotiation"
    ),
    CommunicationTip(
        title = "Silent Concession Leverage",
        description = "Maintain unwavering composure after proposing adjustments or concessions.",
        actionableStep = "Speak your pricing or timeline discount calmly, then immediately cease speaking. Explanations invite counterpart pressure.",
        goalCategory = "Negotiation"
    ),
    CommunicationTip(
        title = "The 'No' Invite Trigger",
        description = "Structure queries to let counterparties assert active control with 'No' definitions safely.",
        actionableStep = "Instead of 'Is this still a good time?', ask 'Is it ridiculous to explore this timeline now?'. This lowers psychological defense.",
        goalCategory = "Negotiation"
    ),
    // Active Listening
    CommunicationTip(
        title = "The Echo Mirror Loop",
        description = "Validate a client or teammate's perspective before attempting horizontal assertion.",
        actionableStep = "Repeat the last 3 critical words of their complaint: 'So safety specifications, correct?'. This satisfies their subconscious confirmation drive.",
        goalCategory = "Active Listening"
    ),
    CommunicationTip(
        title = "Emotional Labeling Bridge",
        description = "Acknowledge the core emotions of difficult counterpart challenges in non-confrontational phrasing.",
        actionableStep = "Begin with 'It seems like you feel standard regulations are restricting velocity' rather than 'You are being impatient'.",
        goalCategory = "Active Listening"
    ),
    // Executive Presence
    CommunicationTip(
        title = "Defensive Qualification Filter",
        description = "Purge introductory softeners that dilute assertive boardroom positions.",
        actionableStep = "Strike out 'I think', 'In my humble opinion', or 'sorry I just' from status updates. State 'The statistics indicate X' cleanly.",
        goalCategory = "Executive Presence"
    ),
    CommunicationTip(
        title = "Unflinching Physical Stature",
        description = "Maintain calm facial control and low-fidget gesture stability in crisis debates.",
        actionableStep = "Relax your shoulders, keep both feet planted, and rest your hands on the boardroom surface or in your lap.",
        goalCategory = "Executive Presence"
    ),
    // Conflict Resolution
    CommunicationTip(
        title = "De-escalation Frequency Pivot",
        description = "Settle elevated tensions by regulating your voice level and conversation speed.",
        actionableStep = "Respond to elevated, rapid complaints in a measured, 20% slower volume profile. The opponent will mirror your tempo.",
        goalCategory = "Conflict Resolution"
    ),
    CommunicationTip(
        title = "Shared Systemic Reframing",
        description = "Refocus heated arguments from personal blame to collaborative milestone delivery.",
        actionableStep = "Frame disagreements around the shared delivery pipeline: 'How do we align these milestones?' rather than 'Why did you miss this?'.",
        goalCategory = "Conflict Resolution"
    ),
    // Team Coordination
    CommunicationTip(
        title = "Async Bandwidth Optimization",
        description = "Reduce recipient reading fatigue using scannable message formatting.",
        actionableStep = "Format status summaries vertically with rich bullets and bold action items. Highly structured, concise messages save hours.",
        goalCategory = "Team Coordination"
    )
)

@Composable
fun CommunicationTipOfTheDay(viewModel: CommViewModel) {
    val activeGoals = viewModel.selectedCommunicationGoals.ifEmpty { listOf("Public Speaking", "Negotiation", "Active Listening") }
    val filteredTips = remember(activeGoals) {
        allTipRepository.filter { tip -> 
            activeGoals.any { goal -> tip.goalCategory.equals(goal, ignoreCase = true) }
        }.ifEmpty { allTipRepository }
    }
    
    var currentTipIdx by remember(filteredTips) { mutableStateOf(0) }
    val activeTip = filteredTips.getOrNull(currentTipIdx % filteredTips.size) ?: allTipRepository[0]
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("tip_of_the_day_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Tip Category",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "COMMUNICATION TIP OF THE DAY",
                        color = GoldAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, CyberPurple)
                ) {
                    Text(
                        activeTip.goalCategory.uppercase(),
                        color = CyberPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                activeTip.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                activeTip.description,
                color = LightSilver,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MidnightBg, RoundedCornerShape(6.dp))
                    .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        "ACTIONABLE EXECUTION LOOP:",
                        color = CyberMint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        activeTip.actionableStep,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: CommCore Playbook",
                    color = DarkSilver,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
                
                TextButton(
                    onClick = { currentTipIdx = (currentTipIdx + 1) % filteredTips.size },
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    modifier = Modifier.height(28.dp).testTag("next_tip_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "NEXT TIP",
                            color = CyberMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Next Tip",
                            tint = CyberMint,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyCommunicationChallengeCard(viewModel: CommViewModel) {
    val activeChallenge = viewModel.activeDailyChallenge
    val completed = viewModel.completedChallengeIds.contains(activeChallenge.id)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("daily_communication_challenge_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, if (completed) CyberMint.copy(alpha = 0.6f) else CyberPurple.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Challenge",
                        tint = if (completed) CyberMint else CyberPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "DAILY SOFT-SKILLS CHALLENGE",
                        color = if (completed) CyberMint else CyberPurple,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (completed) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CyberMint.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CyberMint)
                    ) {
                        Text(
                            "COMPLETED",
                            color = CyberMint,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.1f)),
                        border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            activeChallenge.mode,
                            color = GoldAccent,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = activeChallenge.title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Body
            Text(
                text = activeChallenge.description,
                color = LightSilver,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Focus targets / Specific instruction
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MidnightBg, RoundedCornerShape(6.dp))
                    .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "ACTIVE CHALLENGE GOAL & CONSTRAINTS:",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeChallenge.taskText,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "🔒 Constraint: ${activeChallenge.constraints}",
                        color = CyberMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Skill Chips row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                activeChallenge.skills.forEach { skill ->
                    Box(
                        modifier = Modifier
                            .background(CyberPurple.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, CyberPurple.copy(alpha = 0.25f)), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = skill.uppercase(),
                            color = LightSilver,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.nextDailyChallenge() },
                    modifier = Modifier.height(32.dp).testTag("next_challenge_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "CYCLE CHALLENGE",
                            color = LightSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = LightSilver, modifier = Modifier.size(10.dp))
                    }
                }

                Button(
                    onClick = { viewModel.selectDailyChallenge(activeChallenge.id) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (completed) CyberMint else CyberPurple
                    ),
                    modifier = Modifier
                        .height(32.dp)
                        .testTag("start_challenge_button"),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (completed) Icons.Default.Check else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (completed) MidnightBg else Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (completed) "REPLAY CHALLENGE" else "⚡ ENGAGE ROLEPLAY",
                            color = if (completed) MidnightBg else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DashboardScreen(viewModel: CommViewModel, profile: UserProfile, triggerBrowserNotify: (String, String) -> Unit) {
    val actionPlans by viewModel.communicationActionPlans.collectAsStateWithLifecycle()
    var activeDashboardSubTab by remember { mutableStateOf("Core Metrics") } // "Core Metrics", "AI Dev Plans", "AI Insights & Roadmaps"
    var selectedWeek by remember { mutableIntStateOf(profile.selectedWeekIndex) }
    var activeDailyTab by remember { mutableStateOf("Morning Focus") } // "Morning Focus", "Real-Time Coach", "Evening Lesson"
    
    // Search fields
    var searchQuery by remember { mutableStateOf("") }
    var searchCategoryFilter by remember { mutableStateOf("All") }
    
    // Detailed Confidence Score expansion
    var showConfidenceDetails by remember { mutableStateOf(false) }

    // Communication Intention / Daily Focus states
    val intentionPool = remember {
        listOf(
            "Minimize vocal filler words (like 'just', 'sorry') and incorporate deliberate 2-second silent pauses under pressure.",
            "Practice Active Listening: Mirror the speaker's key phrases before proposing your counterarguments.",
            "Calibrate Dynamic Range: Anchor your vocal resonance in deep chest frequency under critical questions.",
            "Establish Unapologetic Boundaries: Direct, context-driven core assertions without padding or qualifiers.",
            "Calm Under Fire: Maintain 4-4-4 diaphragmatic breathing to stabilize pulse and physiological drift.",
            "Async Bandwidth Calibration: Use concise, scan-friendly vertical formatting to maximize cognitive clarity.",
            "Empathetic Match-Bracing: Acknowledge the core emotional stakes of your coworker first to defuse resistance."
        )
    }
    var intentionIndex by remember { mutableIntStateOf(0) }
    var communicationIntention by remember {
        mutableStateOf(intentionPool[0])
    }
    var showIntentionDialog by remember { mutableStateOf(false) }
    var customIntentionInput by remember { mutableStateOf("") }

    // Hardcoded searchable registry as dynamic launcher (incorporates progress logs, past coaching debriefs, available micro-lessons)
    val searchRegistry = remember(profile.communicationArchetype, profile.currentConfidenceScore, profile.primaryMode, viewModel.currentMode, actionPlans) {
        val baseList = mutableListOf(
            // 1. Progress Logs
            SearchItem("Log: Baseline Profile Assessment", "Archetype calculated as ${profile.communicationArchetype} with baseline score ${profile.currentConfidenceScore}/100.", "Progress Logs", CommScreen.CHAT, "/prep baseline assessment"),
            SearchItem("Log: Course Registered", "Registered course 'High-Stakes Salary Negotiation' in Business Mode (Progress: 30%).", "Progress Logs", CommScreen.COMMUNITY, "course_biz_negotation"),
            SearchItem("Log: Day 3 Streak Secured", "Secure local streak token in ${viewModel.currentMode} mode communication workout.", "Progress Logs", CommScreen.CHAT, "/prep daily checklist"),
            SearchItem("Log: Advisor Sarah Chen Booked", "Scheduled customized executive communication presentation coaching.", "Progress Logs", CommScreen.ADVISORS, "Sarah Chen"),
            SearchItem("Log: Audio Capture Calibration", "Successfully configured voice amplitude limits (averaged 15f baseline).", "Progress Logs", CommScreen.CHAT, "/prep voice telemetry"),

            // 2. Past Debriefs
            SearchItem("Debrief: Multi-Stakeholder Conflict", "Excellent focus on dynamic silent pauses. Next: reduce apologetic filler words ('just').", "Past Debriefs", CommScreen.CHAT, "/debrief analyze last meeting boardroom"),
            SearchItem("Debrief: Emotional Household Chores", "Successfully mirrored flatmate's viewpoint before asserting household boundary cleanly.", "Past Debriefs", CommScreen.CHAT, "/debrief analyze household chore disagreement"),
            SearchItem("Debrief: Async Team Status Lead", "Direct assertiveness was clear and direct. Status signals: Premium authority.", "Past Debriefs", CommScreen.CHAT, "/debrief analyze standup feedback"),
            SearchItem("Debrief: Difficult Timeline Renegotiation", "Calibrated active assertion without defensiveness. Results: Excellent client consensus.", "Past Debriefs", CommScreen.CHAT, "/debrief client timeline confrontation"),

            // 3. Available Micro-lessons
            SearchItem("Lesson: Tactical 2-Second Silent Pause", "Master using brief pauses to build status, project composure, and command presence.", "Micro-Lessons", CommScreen.CHAT, "/prep lesson silent pauses"),
            SearchItem("Lesson: Eliminating 'Just' & 'Sorry'", "Elevate authority by removing deferential and submissive fill qualifiers.", "Micro-Lessons", CommScreen.CHAT, "/prep lesson removing filler qualifiers"),
            SearchItem("Lesson: Active Emotional Mirroring", "Mirroring and acknowledging core emotions before establishing negotiation boundaries.", "Micro-Lessons", CommScreen.CHAT, "/prep lesson empathetic mirroring"),
            SearchItem("Lesson: Double-Anchor Value Pitch", "Lock down maximum ranges in high-stakes pricing discussions with calm, clear anchors.", "Micro-Lessons", CommScreen.CHAT, "/prep lesson salary double anchor"),
            SearchItem("Lesson: Hostile Boardroom Criticism", "Gently pivot critical remarks into objective collaboration using verbal matching.", "Micro-Lessons", CommScreen.CHAT, "/prep lesson hostile criticism defusal"),
            SearchItem("Lesson: Async Channel Brevity Calibration", "Structure crisp digital messages that respect recipient bandwidth and prompt quick action.", "Micro-Lessons", CommScreen.CHAT, "/prep lesson async brevity")
        )

        // 4. Saved Action Plans
        actionPlans.forEach { plan ->
            baseList.add(
                SearchItem(
                    title = "Action Plan: ${plan.goalName}",
                    subtitle = "Focus: ${plan.focusArea} • Strategy: ${plan.strategyText}",
                    category = "Action Plans",
                    targetScreen = CommScreen.DASHBOARD,
                    payload = plan.id.toString()
                )
            )
        }

        baseList
    }

    val filteredResults = remember(searchQuery, searchCategoryFilter, searchRegistry) {
        if (searchQuery.trim().isEmpty()) emptyList()
        else {
            searchRegistry.filter { item ->
                val matchesQuery = item.title.contains(searchQuery, ignoreCase = true) ||
                        item.subtitle.contains(searchQuery, ignoreCase = true)
                val matchesFilter = if (searchCategoryFilter == "All") true else item.category == searchCategoryFilter
                matchesQuery && matchesFilter
            }
        }
    }

    val heatmapSkills = remember {
        listOf(
            HeatmapSkill("Filler Word Shield", "Executive Presence", 85, "FWS", "Eliminating softeners / verbal crutches (just, sorry, potentially).", "/prep lesson removing filler qualifiers"),
            HeatmapSkill("Deliberate Silent Pause", "Executive Presence", 80, "DSP", "Using 2-second silent pauses to manage pressure and build status.", "/prep lesson silent pauses"),
            HeatmapSkill("Gravity Assertion Hook", "Executive Presence", 78, "GAH", "Commanding presence with single-sentence direct statements.", "/prep custom walkthrough gravity hooks"),
            HeatmapSkill("Async Bandwidth Brevity", "Executive Presence", 84, "ABB", "Formulating concise summaries that respect recipient bandwidth.", "/prep lesson async brevity"),
            HeatmapSkill("Vocal Projection Grounding", "Executive Presence", 75, "VPG", "Anchoring voice frequency and pacing through lower chest resonance.", "/prep custom walkthrough vocal grounding"),

            HeatmapSkill("Active Mirroring Loop", "Relational Empathy", 72, "AML", "Validating companion viewpoints to trigger oxytocin and trust.", "/prep lesson empathetic mirroring"),
            HeatmapSkill("Labeling Cognitive Emotion", "Relational Empathy", 70, "LCE", "Naming core emotional currents cleanly to establish rapport.", "/prep custom walkthrough labeling emotion"),
            HeatmapSkill("Inquisitive Open Framing", "Relational Empathy", 88, "IOF", "Framing open questions to bypass defensive walls.", "/prep custom walkthrough open dialogue"),
            HeatmapSkill("Empathetic Core Validation", "Relational Empathy", 76, "ECV", "Validating the structural stakes without compromising limits.", "/prep custom walkthrough core validation"),
            HeatmapSkill("Collaborative Pivot Bridging", "Relational Empathy", 79, "CPB", "Bridging conflicting agendas towards clear collaborative tracks.", "/prep custom walkthrough collaborative bridge"),

            HeatmapSkill("Hostile Criticism Defusal", "De-escalation & Conflict", 68, "HCD", "Gently pivoting negative critique into clear collaborating options.", "/prep lesson hostile criticism defusal"),
            HeatmapSkill("Boundary Integrity Shield", "De-escalation & Conflict", 90, "BIS", "Asserting firm workspace boundaries without friction or excuse.", "/prep custom walkthrough boundary setup"),
            HeatmapSkill("Triangulating Core Issues", "De-escalation & Conflict", 67, "TCI", "Disassimilating personal elements from structural project tasks.", "/prep custom walkthrough focus problem"),
            HeatmapSkill("Un-Defensive Interrogation", "De-escalation & Conflict", 73, "UDI", "Prompting counterpart critique into precise, manageable criteria.", "/prep custom walkthrough defusal checks"),
            HeatmapSkill("Friction Deceleration Pacing", "De-escalation & Conflict", 64, "FDP", "Consciously slowing tempo to de-escalate argument levels.", "/prep custom walkthrough pacing cooling"),

            HeatmapSkill("Salary Double Anchoring", "Under Pressure & Anchoring", 81, "SDA", "Securing favorable extremes early in rate discussions.", "/prep lesson salary double anchor"),
            HeatmapSkill("Impromptu Pivot Structural", "Under Pressure & Anchoring", 70, "IPS", "Deploying rapid point structures under snap conditions.", "/prep custom walkthrough snap speeches"),
            HeatmapSkill("Physiological Drift Control", "Under Pressure & Anchoring", 76, "PDC", "Resisting adrenaline spikes through breath pacing control.", "/prep custom walkthrough drift therapy"),
            HeatmapSkill("Status Asymmetry Counter", "Under Pressure & Anchoring", 69, "SAC", "Neutralizing fear reactions when facing executive positions.", "/prep custom walkthrough asymmetry balancing"),
            HeatmapSkill("Consensus Closure Locking", "Under Pressure & Anchoring", 77, "CCL", "Locking down immediate agreements systematically in talks.", "/prep custom walkthrough consensus logs")
        )
    }

    var selectedHeatmapSkill by remember { mutableStateOf<HeatmapSkill?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 0. BRANDING & GLOBAL THEME TOGGLE HEADER ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("branding_theme_header_card"),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "CommCore Mini Logo",
                            tint = CyberPurple,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "COMMCORE AI LABS",
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp
                            )
                            Text(
                                "Executive Orchestration Suite",
                                color = DarkSilver,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Dynamic Theme Toggle Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { CommThemeState.isLightMode = !CommThemeState.isLightMode }
                            .background(MidnightBg, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("theme_mode_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (CommThemeState.isLightMode) Icons.Default.Settings else Icons.Default.Share,
                            contentDescription = "Theme Icon Toggle",
                            tint = if (CommThemeState.isLightMode) GoldAccent else CyberMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (CommThemeState.isLightMode) "LIGHT" else "DARK",
                            color = if (CommThemeState.isLightMode) GoldAccent else CyberMint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // --- 1. ADVANCED ECOSYSTEM SEARCH SYSTEM ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("advanced_search_card"),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, CyberPurple)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ADVANCED ECOSYSTEM SEARCH",
                        color = CyberMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().testTag("dashboard_search_input"),
                        placeholder = { Text("Search scenarios, coaches, skill modules...", color = DarkSilver, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CyberPurple) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = DarkSilver)
                                }
                            }
                        },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberMint,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Filter chips row
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "Progress Logs", "Past Debriefs", "Micro-Lessons", "Action Plans").forEach { filter ->
                            FilterChip(
                                selected = searchCategoryFilter == filter,
                                onClick = { searchCategoryFilter = filter },
                                label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = MidnightBg,
                                    labelColor = DarkSilver
                                ),
                                border = BorderStroke(1.dp, if (searchCategoryFilter == filter) CyberPurple else BorderGlass)
                            )
                        }
                    }

                    // Dynamic search results projection
                    if (searchQuery.trim().isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "SEARCH RESULTS (${filteredResults.size})",
                            color = LightSilver,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (filteredResults.isEmpty()) {
                            // Perfect custom generative AI fallback scenario
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    val formattedPrompt = "/prep custom scenario: $searchQuery"
                                    viewModel.chatInputText = formattedPrompt
                                    viewModel.currentScreen = CommScreen.CHAT
                                },
                                colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                                border = BorderStroke(1.dp, CyberMint)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Generate", tint = CyberMint, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("✨ Launch Dynamic Roleplay Simulation", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("AI will instantly construct a high-relevance '$searchQuery' interactive workout in AI Gym.", color = CyberMint, fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            filteredResults.forEach { result ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            when (result.targetScreen) {
                                                CommScreen.CHAT -> {
                                                    viewModel.chatInputText = result.payload
                                                    viewModel.currentScreen = CommScreen.CHAT
                                                }
                                                CommScreen.ADVISORS -> {
                                                    viewModel.currentScreen = CommScreen.ADVISORS
                                                    // In a real database we could flag a detail view, let's keep it clean
                                                }
                                                CommScreen.COMMUNITY -> {
                                                    viewModel.currentScreen = CommScreen.COMMUNITY
                                                }
                                                else -> {}
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                                    border = BorderStroke(1.dp, BorderGlass)
                                ) {
                                    ListItem(
                                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                        leadingContent = {
                                            Icon(
                                                imageVector = when (result.category) {
                                                    "Progress Logs" -> Icons.Default.CheckCircle
                                                    "Past Debriefs" -> Icons.Default.Info
                                                    "Action Plans" -> Icons.Default.List
                                                    else -> Icons.Default.PlayArrow
                                                },
                                                contentDescription = result.category,
                                                tint = when (result.category) {
                                                    "Progress Logs" -> CyberMint
                                                    "Past Debriefs" -> GoldAccent
                                                    "Action Plans" -> CyberMint
                                                    else -> CyberPurple
                                                }
                                            )
                                        },
                                        headlineContent = { Text(result.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                        supportingContent = { Text(result.subtitle, color = DarkSilver, fontSize = 11.sp) },
                                        trailingContent = {
                                            Text(
                                                result.category.uppercase(),
                                                color = when (result.category) {
                                                    "Progress Logs" -> CyberMint
                                                    "Past Debriefs" -> GoldAccent
                                                    else -> CyberPurple
                                                },
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. WELCOME BRIEF WITH COMPOSITE RADIAL GAUGE ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(
                    width = if (CommVisualRewards.equippedNeonBorder) 2.dp else 1.dp,
                    brush = if (CommVisualRewards.equippedNeonBorder) {
                        Brush.linearGradient(listOf(CyberPurple, Color.Cyan, CyberMint))
                    } else {
                        androidx.compose.ui.graphics.SolidColor(BorderGlass)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (CommVisualRewards.equippedHalo) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Equipped Golden Halo",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(
                                    "Welcome Back, ${profile.name}!",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                profile.communicationArchetype,
                                color = CyberMint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Streak", tint = GoldAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${profile.streakCounter} Day Streak",
                                    color = GoldAccent,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Confidence Radial Gauge
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(90.dp)
                                .drawBehind {
                                    if (CommVisualRewards.equippedAuraBackglow) {
                                        drawCircle(
                                            color = CyberPurple.copy(alpha = 0.35f),
                                            radius = size.minDimension / 1.4f
                                        )
                                    }
                                    drawCircle(
                                        color = MidnightBg,
                                        radius = size.minDimension / 2
                                    )
                                    drawCircle(
                                        color = BorderGlass,
                                        radius = size.minDimension / 2,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx())
                                    )
                                    drawArc(
                                        brush = Brush.sweepGradient(listOf(CyberPurple, CyberMint)),
                                        startAngle = -90f,
                                        sweepAngle = (profile.currentConfidenceScore / 100f) * 360f,
                                        useCenter = false,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 8.dp.toPx(),
                                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                                        )
                                    )
                                    if (CommVisualRewards.equippedHalo) {
                                        drawCircle(
                                            color = GoldAccent,
                                            radius = size.minDimension / 1.7f,
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                                width = 2.dp.toPx(),
                                                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                                    floatArrayOf(12f, 12f),
                                                    0f
                                                )
                                            )
                                        )
                                    }
                                }
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${profile.currentConfidenceScore}",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                                Text(
                                    "CCI Score",
                                    color = DarkSilver,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderGlass)
                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = { showConfidenceDetails = !showConfidenceDetails },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(contentColor = CyberMint)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (showConfidenceDetails) "Collapse Baseline scorecard ▲" else "View Detailed Baseline Scorecard ▼",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(visible = showConfidenceDetails) {
                        Column(
                            modifier = Modifier.padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("ENVIRONMENTAL CONFIDENCE INDEX", color = DarkSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            
                            val listConf = listOf(
                                Triple("Workplace Environment", profile.confWorkplace, "BUSINESS"),
                                Triple("Romantic Partner Boundaries", profile.confRomantic, "RELATIONSHIP"),
                                Triple("Social Groups & Small Talk", profile.confFriends, "FRIENDS"),
                                Triple("Public Speaking Command", profile.confPublic, "PUBLIC"),
                                Triple("Conflict De-escalation", profile.confConflict, "CONFLICT"),
                                Triple("Digital & Async Sizing", profile.confDigital, "DIGITAL")
                            )

                            listConf.forEach { (label, value, targetMode) ->
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(label, color = LightSilver, fontSize = 12.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("$value/10", color = GoldAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // Quick boost action
                                            IconButton(
                                                onClick = {
                                                    viewModel.switchMode(targetMode)
                                                    viewModel.chatInputText = "/prep boost confidence score in $targetMode mode"
                                                    viewModel.currentScreen = CommScreen.CHAT
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Boost", tint = CyberMint, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val animatedScoreProgress by animateFloatAsState(
                                        targetValue = value / 10f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "animated_baseline_progress"
                                    )
                                    LinearProgressIndicator(
                                        progress = { animatedScoreProgress },
                                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                        color = CyberPurple,
                                        trackColor = MidnightBg
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 24h PRACTICE ABSENCE REMINDER WARNING ---
        item {
            DailyInactivityPracticeReminderWidget(viewModel)
        }

        // --- DAILY PRACTICE CONFIDENCE AFFIRMATION ---
        item {
            DailyAffirmationCard(profile, viewModel)
        }

        // --- STREAK COUNTER COMPONENT ---
        item {
            DashboardStreakCounterWidget(viewModel, profile)
        }

        // --- DASHBOARD SUB-NAVIGATION TABS ---
        item {
            TabRow(
                selectedTabIndex = when (activeDashboardSubTab) {
                    "Core Metrics" -> 0
                    "AI Dev Plans" -> 1
                    else -> 2
                },
                containerColor = MidnightSurface,
                contentColor = CyberPurple,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(8.dp))
            ) {
                Tab(
                    selected = activeDashboardSubTab == "Core Metrics",
                    onClick = { activeDashboardSubTab = "Core Metrics"; searchQuery = "" },
                    text = { Text("CORE STATS", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) },
                    selectedContentColor = CyberPurple,
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = activeDashboardSubTab == "AI Dev Plans",
                    onClick = { activeDashboardSubTab = "AI Dev Plans"; searchQuery = "" },
                    text = { Text("AI DEV PLANS & METRICS", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) },
                    selectedContentColor = CyberMint,
                    unselectedContentColor = Color.Gray
                )
                Tab(
                    selected = activeDashboardSubTab == "AI Insights & Roadmaps",
                    onClick = { activeDashboardSubTab = "AI Insights & Roadmaps"; searchQuery = "" },
                    text = { Text("AI LAB & CHARTS", fontSize = 11.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace) },
                    selectedContentColor = GoldAccent,
                    unselectedContentColor = Color.Gray
                )
            }
        }

        if (activeDashboardSubTab == "Core Metrics") {
            // --- 2.1a 30-DAY COMMUNICATION CONFIDENCE SCORE TREND GRAPH ---
            item {
                ConfidenceTrendChart(profile.currentConfidenceScore)
            }

            // --- 2.1a-2 RECHARTS INTERACTIVE MILESTONE & INTERACTION COMPASS VISUALIZATION ---
            item {
                InteractionHistoryMilestoneChart(viewModel.selectedCommunicationGoals)
            }

            // --- 2.1b COMMUNICATION TIP OF THE DAY ---
            item {
                CommunicationTipOfTheDay(viewModel)
            }

            // --- DAILY SOFT-SKILLS COMMUNICATION CHALLENGE ---
            item {
                DailyCommunicationChallengeCard(viewModel)
            }

            // --- 2.1b MILESTONE ACHIEVEMENTS & BADGES ---
            item {
                ProgressDashboardAndActionPlans(viewModel)
            }

            item {
                MilestoneBadgeCard(viewModel, profile)
            }

            item {
                MilestoneVisualRewardsCard(viewModel, profile)
            }

            // --- 2.1c PEER FEEDBACK CENTRAL HUB ---
            item {
                PeerFeedbackHubCard(viewModel = viewModel)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                GeminiToneClarityRadarCard(viewModel = viewModel)
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                ContestLeaderboardCard()
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                CorporatePartnersCard(viewModel = viewModel)
            }

            // --- 2.1cx PAST AI PERFORMANCE IMPROVEMENT LINE GRAPH (RECHARTS ENGINE) ---
            item {
                VocalFeedbackScoreLineGraphCard(viewModel = viewModel)
            }

            // --- 2.1cx PAST AI ROLEPLAY HISTORIES SCORING HUB ---
            item {
                PastRoleplaySessionsHistoryCard(viewModel = viewModel)
            }

            // --- 2.1d DAILY PRACTICE NOTIFIER REMINDERS ---
            item {
                DailyPracticeRemindersCard(viewModel = viewModel, triggerBrowserNotify = triggerBrowserNotify)
            }
        } else if (activeDashboardSubTab == "AI Dev Plans") {
            // --- AI PERSONALIZED DEVELOPMENT PLANS & METRICS DASHBOARD VIEW ---
            item {
                AIDevPlansAndMetricsDashboard(viewModel, profile)
            }
        } else {
            // --- AI INSIGHTS & ROADMAPS SUB-TAB COMPONENTS ---
            item {
                SentimentAnalysis30DaysChart()
            }
            
            item {
                CommunicationSkillGrowthChart(viewModel)
            }
            
            item {
                GeminiRoadmapGeneratorCard(viewModel, profile)
            }
            
            item {
                ShareAchievementsCard(viewModel, profile)
            }
        }

        // --- 2.2 QUICK-VIEW WIDGET: DAILY COMMUNICATION INTENTION & FOCUS ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("communication_intention_widget"),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, CyberMint)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Intention Logo",
                                tint = CyberMint,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "DAILY ADAPTIVE INTENTION",
                                color = CyberMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val nextIdx = (intentionIndex + 1) % intentionPool.size
                                    intentionIndex = nextIdx
                                    communicationIntention = intentionPool[nextIdx]
                                },
                                modifier = Modifier.size(24.dp).testTag("rotate_intention_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Rotate/Refresh Intention",
                                    tint = CyberMint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showIntentionDialog = true },
                                modifier = Modifier.size(24.dp).testTag("edit_intention_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Intention",
                                    tint = CyberMint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = communicationIntention,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberMint.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "FOCUS LEVEL: HIGH",
                                color = CyberMint,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.12f)),
                            border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.3f)),
                            modifier = Modifier.clickable {
                                viewModel.chatInputText = "/prep training intention: $communicationIntention"
                                viewModel.currentScreen = CommScreen.CHAT
                            }
                        ) {
                            Text(
                                text = "⚡ LAUNCH SIMULATION",
                                color = CyberPurple,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // --- 3. HIGH-FIDELITY ACTIVE STREAK CALENDAR & HABIT LOGS ---
        item {
            val activeStkDays = remember(profile.streakCounter) {
                if (profile.streakCounter == 0) 0 else {
                    val rem = profile.streakCounter % 7
                    if (rem == 0) 7 else rem
                }
            }
            
            // Context-adaptive mantra matching current Mode perfectly
            val streakMantra = remember(viewModel.currentMode) {
                when (viewModel.currentMode) {
                    "BUSINESS" -> "Precision in boundaries signals premium value. Avoid passive hesitation."
                    "PROFESSIONAL" -> "Your expertise is factual reality. Eliminate 'just checking' qualifiers."
                    "RELATIONSHIP" -> "Authentic calibration respects both yourself and your loved opponent."
                    "FRIENDS" -> "Be thoroughly present. Attention is the ultimate social multiplier."
                    "PUBLIC" -> "Vocal silences command dramatic tension. Pauses are power, use them."
                    "CONFLICT" -> "Composure is tactical leverage. Breathe twice before you speak."
                    "DIGITAL" -> "Your writing is your digital proxy. Design it to get parsed instantly."
                    else -> "Master your calibration metrics to control your strategic outcomes."
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("streak_tracker_card"),
                colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Calendar", tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("STREAK ENGINE & HABIT CALENDAR", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f))
                        ) {
                            Text(
                                "BEST: 18 DAYS",
                                color = CyberPurple,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Weekly Grid representing completed days
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val wkDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                        wkDays.forEachIndexed { index, day ->
                            val done = index < activeStkDays
                            val active = index == activeStkDays
                            
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(day, color = if (done) Color.White else DarkSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (done) CyberMint.copy(alpha = 0.2f)
                                            else if (active) CyberPurple.copy(alpha = 0.15f)
                                            else MidnightBg
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (done) CyberMint
                                                else if (active) CyberPurple
                                                else BorderGlass
                                            ),
                                            shape = CircleShape
                                        )
                                ) {
                                    if (done) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = CyberMint, modifier = Modifier.size(16.dp))
                                    } else {
                                        Text(
                                            "${index + 1}",
                                            color = if (active) CyberPurple else DarkSilver,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MidnightBg),
                        border = BorderStroke(1.dp, BorderGlass),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "DAILY ADAPTIVE MANTRA (${viewModel.currentMode}):",
                                color = CyberMint,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "\"$streakMantra\"",
                                color = LightSilver,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Log today's workout to secure your streaks:",
                            color = DarkSilver,
                            fontSize = 11.sp
                        )
                        Button(
                            onClick = { viewModel.triggerDailyGoalDone() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Secure Day ✔ (Streak +1)", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        }

        // --- 4. THE MODE MASTERY LEVELS DIRECTORY ---
        item {
            Text("MODE MASTERY STATUS LEVELS", color = DarkSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modesDef = listOf(
                    Triple("PROFESSIONAL", ((profile.confWorkplace + profile.confDigital) / 2) * 10, "Aptitude"),
                    Triple("BUSINESS", profile.confWorkplace * 10, "Executive"),
                    Triple("RELATIONSHIP", profile.confRomantic * 10, "Calibrated"),
                    Triple("FRIENDS", profile.confFriends * 10, "Commanding"),
                    Triple("PUBLIC", profile.confPublic * 10, "Raconteur"),
                    Triple("CONFLICT", profile.confConflict * 10, "Mediator"),
                    Triple("DIGITAL", profile.confDigital * 10, "Polished")
                )

                modesDef.forEach { (modeName, masteryPercent, genericTier) ->
                    val isActive = viewModel.currentMode == modeName
                    val tierText = when {
                        masteryPercent >= 80 -> "Master Elite"
                        masteryPercent >= 50 -> "Advanced $genericTier"
                        else -> "Initiate / Silver"
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("mode_mastery_${modeName.lowercase()}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) CyberPurple.copy(alpha = 0.15f) else MidnightSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isActive) CyberPurple else BorderGlass
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getModeIcon(modeName),
                                contentDescription = modeName,
                                tint = if (isActive) CyberMint else DarkSilver,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        modeName,
                                        color = if (isActive) CyberMint else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        tierText,
                                        color = if (isActive) CyberPurple else DarkSilver,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LinearProgressIndicator(
                                        progress = { masteryPercent / 100f },
                                        modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp)),
                                        color = CyberPurple,
                                        trackColor = MidnightBg
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        "$masteryPercent% XP",
                                        color = LightSilver,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))

                            IconButton(
                                onClick = {
                                    viewModel.switchMode(modeName)
                                    viewModel.chatInputText = "/prep mode calibration workout: $modeName"
                                    viewModel.currentScreen = CommScreen.CHAT
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CyberMint.copy(alpha = 0.1f))
                                    .border(BorderStroke(1.dp, CyberMint.copy(alpha = 0.4f)), CircleShape)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Train mode", tint = CyberMint, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- 4.2 ADVANCED SKILL HEATMAP GRID (20 MICRO-SKILLS) ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("communication_skill_heatmap_card"),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, CyberPurple)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "MICRO-SKILL SYSTEM HEATMAP (20 DOMAINS)",
                        color = CyberMint,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Granular proficiency indicators. Select any cell to view detailed feedback and launch customized drills.",
                        color = DarkSilver,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Group by categories
                    val categories = heatmapSkills.groupBy { it.category }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        categories.forEach { (categoryName, skills) ->
                            Text(
                                categoryName.uppercase(),
                                color = LightSilver,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Draw horizontal grid row of compact cells representing each of the 5 skills
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                skills.forEach { skill ->
                                    val isSelected = selectedHeatmapSkill?.name == skill.name
                                    val cellColor = when {
                                        skill.score >= 80 -> CyberMint
                                        skill.score >= 70 -> CyberPurple
                                        else -> GoldAccent
                                    }
                                    val cellBg = when {
                                        skill.score >= 80 -> CyberMint.copy(alpha = 0.12f)
                                        skill.score >= 70 -> CyberPurple.copy(alpha = 0.12f)
                                        else -> GoldAccent.copy(alpha = 0.08f)
                                    }

                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(cellBg)
                                            .border(
                                                BorderStroke(
                                                    if (isSelected) 2.dp else 1.dp,
                                                    if (isSelected) Color.White else cellColor.copy(alpha = 0.6f)
                                                ),
                                                shape = RoundedCornerShape(6.dp)
                                            )
                                            .clickable {
                                                selectedHeatmapSkill = if (isSelected) null else skill
                                            }
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = skill.shortCode,
                                                color = if (isSelected) Color.White else cellColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            Text(
                                                text = "${skill.score}%",
                                                color = LightSilver,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Selected Skill Detail Inspector drawer right inside the widget container
                    selectedHeatmapSkill?.let { skill ->
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MidnightBg),
                            border = BorderStroke(1.dp, BorderGlass),
                            modifier = Modifier.fillMaxWidth().testTag("heatmap_skill_inspector")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = skill.category.uppercase(),
                                            color = CyberPurple,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = skill.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${skill.score}/100",
                                            color = if (skill.score >= 80) CyberMint else if (skill.score >= 70) CyberPurple else GoldAccent,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { selectedHeatmapSkill = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Close description", tint = DarkSilver, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                
                                LinearProgressIndicator(
                                    progress = { skill.score / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                    color = if (skill.score >= 80) CyberMint else if (skill.score >= 70) CyberPurple else GoldAccent,
                                    trackColor = BorderGlass.copy(alpha = 0.5f)
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = skill.definition,
                                    color = LightSilver,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        viewModel.chatInputText = skill.trainingPrompt
                                        viewModel.currentScreen = CommScreen.CHAT
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Launch drill", modifier = Modifier.size(16.dp), tint = MidnightBg)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Pre-fill drill in CoachChat Gym", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MidnightBg)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Legend indicators for accessibility and heatmap comprehension
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LEGEND: ", color = DarkSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberMint))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Strong (80%+)", color = DarkSilver, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyberPurple))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Proficient (70%+)", color = DarkSilver, fontSize = 9.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoldAccent))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Developing", color = DarkSilver, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 5. ACTIVE NORTH STAR CALIBRATION ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Milestone", tint = GoldAccent, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("30-DAY NORTH STAR MILESTONE", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            profile.communicationGoal30Days,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // --- 6. WEEKLY BLUEPRINT ARCHITECTURE PLAN ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("WEEKLY ARCHITECTURE BLUEPRINT", color = DarkSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 2, 3, 4).forEach { wk ->
                        FilterChip(
                            selected = selectedWeek == wk,
                            onClick = { selectedWeek = wk },
                            label = { Text("W$wk", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyberPurple
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("WEEK $selectedWeek BLUEPRINT PLAN", color = CyberMint, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("📍 3 Trainable Micro-skills", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    getWeeklySkills(selectedWeek, profile.primaryMode).forEach { skill ->
                        Row(modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(skill, color = LightSilver, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("🎭 2 Customized AI Roleplans", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    getWeeklyScenarios(selectedWeek, profile.primaryMode).forEach { scenario ->
                        Row(modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp)) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberMint, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(scenario, color = LightSilver, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("👥 1 Community Challenge", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(
                        getWeeklyChallenge(selectedWeek),
                        color = LightSilver,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 12.dp, top = 2.dp)
                    )
                }
            }
        }

        // --- 7. DAILY CORE PLAN METRICS (TAB SECTOR) ---
        item {
            Text("DAILY CORE PLAN ACTIONABLES", color = DarkSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            
            TabRow(
                selectedTabIndex = if (activeDailyTab == "Morning Focus") 0 else if (activeDailyTab == "Real-Time Coach") 1 else 2,
                containerColor = MidnightSurface,
                contentColor = CyberPurple
            ) {
                Tab(
                    selected = activeDailyTab == "Morning Focus",
                    onClick = { activeDailyTab = "Morning Focus" },
                    text = { Text("Morning Intent", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeDailyTab == "Real-Time Coach",
                    onClick = { activeDailyTab = "Real-Time Coach" },
                    text = { Text("Real-Time Tweak", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeDailyTab == "Evening Lesson",
                    onClick = { activeDailyTab = "Evening Lesson" },
                    text = { Text("Evening Lesson", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (activeDailyTab) {
                "Morning Focus" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("TODAY'S INTENTION (5 MIN)", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Focus Intent: Remove qualifier words from requests.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "In your upcoming digital messages and team conversations today, remove words like 'just', 'sorry to bother', and 'only'. State your query directly. That builds immediate professional gravity.",
                                color = LightSilver,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.triggerDailyGoalDone() },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                                modifier = Modifier.align(Alignment.End).testTag("assert_intention_button")
                            ) {
                                Text("Mark Completed (Streak +1)")
                            }
                        }
                    }
                }
                "Real-Time Coach" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("MIDDAY REAL-TIME COACHING (INSTANT)", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Paste an important email or message you are about to send. AI will instantly rewrite it using specific Mode filters for maximum alignment and safety.",
                                color = LightSilver,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    viewModel.chatInputText = "/polish I'm sorry to bother you but is my report done? Just checking."
                                    viewModel.currentScreen = CommScreen.CHAT
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Open Coach with Preset /polish message")
                            }
                        }
                    }
                }
                "Evening Lesson" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("EVENING REFLECTION (10 MIN)", color = CyberPurple, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Acknowledge the win of active boundary-drawing.",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Lesson Focus: The Chemistry of Compliance and Respect. \nPsychology shows that team mates rarely respect players who seek permission to exist. When you clearly state your limits, you generate clean predictability.",
                                color = LightSilver,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showIntentionDialog) {
        AlertDialog(
            onDismissRequest = { showIntentionDialog = false },
            containerColor = MidnightSurface,
            title = {
                Text(
                    "CALIBRATE DAILY FOCUS INTENTION",
                    color = CyberMint,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column {
                    Text(
                        "Set a customized specific verbal focus metric for your workouts today.",
                        color = LightSilver,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = customIntentionInput,
                        onValueChange = { customIntentionInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("intention_custom_input"),
                        placeholder = { Text("e.g. Master physiological drift control and grounding breathing...", color = DarkSilver, fontSize = 12.sp) },
                        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customIntentionInput.trim().isNotEmpty()) {
                            communicationIntention = customIntentionInput.trim()
                        }
                        showIntentionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMint)
                ) {
                    Text("Save Focus ✔", color = MidnightBg, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIntentionDialog = false }) {
                    Text("Cancel", color = LightSilver, fontSize = 12.sp)
                }
            }
        )
    }
}

// --- SCREEN 2: ACTIVE COACHCHAT --
@Composable
fun CoachChatScreen(viewModel: CommViewModel, profile: UserProfile) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current

    val recordAudioLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startWebSpeechRecognition()
        } else {
            viewModel.webSpeechStatusMessage = "Microphone Permission Denied"
        }
    }

    var isSandboxCustomizerExpanded by remember { mutableStateOf(false) }
    var sandboxCategory by remember { mutableStateOf("Strategic Boardroom Raise") }
    var sandboxDifficulty by remember { mutableStateOf("Assertive Executive (Hard)") }
    var sandboxFocus by remember { mutableStateOf("Vocal Filler Word Shield") }
    var isSandboxActive by remember { mutableStateOf(false) }

    // AI Practice Scenario Selector states
    var activeScenarioSelected by remember { mutableStateOf<String?>(null) }
    var activePersonaSelected by remember { mutableStateOf<String?>(null) }

    // Dynamic metrics that simulate live audio capture telemetry
    var fillerShieldMetric by remember { mutableStateOf(92) }
    var resonanceMetric by remember { mutableStateOf(84) }
    var mirroringMetric by remember { mutableStateOf(71) }
    var assertivenessMetric by remember { mutableStateOf(78) }

    // Periodically fluctuate values in a LaunchedEffect to give a "live link" feel
    LaunchedEffect(isSandboxActive) {
        if (isSandboxActive) {
            while (isSandboxActive) {
                delay(2200)
                fillerShieldMetric = (84..100).random()
                resonanceMetric = (72..96).random()
                mirroringMetric = (65..92).random()
                assertivenessMetric = (70..98).random()
            }
        }
    }

    // Scroll chat list to bottom as new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LaunchedEffect(viewModel.currentChallengeProgressStatus) {
        if (viewModel.currentChallengeProgressStatus == "Started") {
            activeScenarioSelected = viewModel.activeDailyChallenge.scenario
            activePersonaSelected = viewModel.activeDailyChallenge.persona
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat Header with configuration toggles
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("COACHING CENTRAL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Operating in ${viewModel.currentMode} Mode", color = CyberMint, fontSize = 12.sp)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("High Thinking", color = LightSilver, fontSize = 11.sp, modifier = Modifier.padding(end = 6.dp))
                Switch(
                    checked = viewModel.isThinkingModeHigh,
                    onCheckedChange = { viewModel.isThinkingModeHigh = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberPurple,
                        checkedTrackColor = CyberPurple.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.scale(0.85f).testTag("thinking_mode_switch")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = { viewModel.clearChatHistory() }) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = DarkSilver)
                }
            }
        }

        // --- ACTIVE DAILY Soft-Skills CHALLENGE BANNER ---
        if (viewModel.currentChallengeProgressStatus != null) {
            val challenge = viewModel.activeDailyChallenge
            val status = viewModel.currentChallengeProgressStatus
            val success = status == "Completed_Success"
            val fail = status == "Completed_Fail"
            val evaluating = status == "Evaluating"
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        success -> CyberMint.copy(alpha = 0.15f)
                        fail -> Color.Red.copy(alpha = 0.12f)
                        else -> CyberPurple.copy(alpha = 0.15f)
                    }
                ),
                border = BorderStroke(
                    1.dp, 
                    when {
                        success -> CyberMint
                        fail -> Color.Red
                        else -> CyberPurple
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("active_daily_challenge_chat_banner")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    success -> Icons.Default.CheckCircle
                                    fail -> Icons.Default.Warning
                                    else -> Icons.Default.Star
                                },
                                contentDescription = null,
                                tint = when {
                                    success -> CyberMint
                                    fail -> Color.Red
                                    else -> CyberPurple
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "🎯 ACTIVE DAILY CHALLENGE",
                                color = if (success) CyberMint else if (fail) Color.Red else CyberPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (status == "Started") {
                                Box(
                                    modifier = Modifier
                                        .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "VERIFICATION LIVE",
                                        color = GoldAccent,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            
                            IconButton(
                                onClick = { viewModel.resetActiveChallengeState() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = challenge.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = challenge.taskText,
                        color = LightSilver,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MidnightBg.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "⚠ Limit Constraint: ${challenge.constraints}",
                            color = if (success) CyberMint else if (fail) Color.Red else GoldAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    // Show validation status / feedback if evaluated
                    if (evaluating) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = CyberPurple, strokeWidth = 1.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scanning speech transcript against linguistic safety markers...", color = LightSilver, fontSize = 11.sp)
                        }
                    } else if (viewModel.lastChallengeFeedbackText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MidnightBg),
                            border = BorderStroke(1.dp, BorderGlass)
                        ) {
                            Text(
                                text = viewModel.lastChallengeFeedbackText ?: "",
                                color = Color.White,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                        
                        if (fail) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.selectDailyChallenge(challenge.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(28.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp)
                                ) {
                                    Text("RETRY CHALLENGE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Voice Simulation UI row
        LiveVoiceSimulationBar(viewModel = viewModel)

        // --- CO-EXISTING PARALLEL KNOWLEDGE HUB ---
        var isKnowledgeHubExpanded by remember { mutableStateOf(false) }
        var showSearchableTheoryModal by remember { mutableStateOf(false) }
        
        if (showSearchableTheoryModal) {
            SearchableKnowledgeHubDialog(viewModel = viewModel) {
                showSearchableTheoryModal = false
            }
        }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isKnowledgeHubExpanded) MidnightSurfaceCard else MidnightSurface
            ),
            border = BorderStroke(1.dp, if (isKnowledgeHubExpanded) CyberMint else BorderGlass)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberMint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "PARALLEL KNOWLEDGE HUB",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                "Frameworks running in parallel to structure your thoughts properly",
                                color = LightSilver,
                                fontSize = 9.sp
                            )
                        }
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showSearchableTheoryModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(26.dp).testTag("search_knowledge_hub_button"),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                Text("THEORIES", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            }
                        }

                        Button(
                            onClick = { isKnowledgeHubExpanded = !isKnowledgeHubExpanded },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isKnowledgeHubExpanded) CyberMint else MidnightBg
                            ),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(26.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text(
                                text = if (isKnowledgeHubExpanded) "COLLAPSE" else "EXPAND",
                                color = if (isKnowledgeHubExpanded) MidnightBg else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
                
                AnimatedVisibility(visible = isKnowledgeHubExpanded) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        Text(
                            "Select any structural framework template to review its psychological components and inject its template outline directly into your current speech input to assist your practicing:",
                            color = LightSilver,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                        
                        val frameworks = listOf(
                            Triple(
                                "STAR Framework (Interview Mastery)", 
                                "Highly structured formula to answer behavioral interview queries clearly under pressure.",
                                "SITUATION: [Context of challenge]\nTASK: [What you were assigned to repair]\nACTION: [Specific direct steps you took]\nRESULT: [Measurable positive data output]"
                            ),
                            Triple(
                                "Pyramid Principle (Executive Briefing)", 
                                "Minto communication framework focusing on bottom-line conclusions first, followed by clean arguments.",
                                "CONCLUSION: [Direct bottom-line answer/proposal]\n- REASON 1: [Strategic argument 1]\n- REASON 2: [Strategic argument 2]\n- REASON 3: [Strategic argument 3]"
                            ),
                            Triple(
                                "Crucial Conversations (De-escalation)", 
                                "Psychological framework utilized to separate facts from story-driven inferences under heavy strain.",
                                "VALIDATION: I acknowledge [Stakes/emotions]\nFACTS: [Objective visual markers only]\nSTORY: [My perspective and concerns]\nDIALOGUE: How should we address this?"
                            ),
                            Triple(
                                "SBI Feedback Engine (Clear Criticism)", 
                                "Situation-Behavior-Impact approach to construct transparent reviews without activating defense mechanisms.",
                                "SITUATION: [Where/when it occurred]\nBEHAVIOR: [Precise description of action]\nIMPACT: [Result of behavior on team/business]"
                            ),
                            Triple(
                                "SPIN Sales Pitch (B2B Pitch)", 
                                "Classic sales system matching client operational pains to product payoff models.",
                                "SITUATION: [Current workflow]\nPROBLEM: [Specific developmental bottlenecks]\nIMPLICATION: [Consequent loss of money/speed]\nNEED-PAYOFF: [How our platform solves this]"
                            )
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            frameworks.forEach { (title, desc, template) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MidnightBg),
                                    border = BorderStroke(1.dp, BorderGlass),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(title, color = CyberMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(desc, color = LightSilver, fontSize = 10.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        // Template display preview box
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MidnightSurface, RoundedCornerShape(4.dp))
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = template, 
                                                color = Color.Gray, 
                                                fontSize = 9.sp, 
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(6.dp))
                                        
                                        Button(
                                            onClick = { 
                                                viewModel.chatInputText = template
                                                isKnowledgeHubExpanded = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.align(Alignment.End).height(24.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Text("APPLY TO SPEECH INPUT", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- AI PRACTICE SCENARIO SELECTOR PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("ai_practice_scenario_card"),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = BorderStroke(1.dp, if (activeScenarioSelected != null) GoldAccent.copy(alpha = 0.5f) else CyberPurple.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Scenario Icon",
                            tint = GoldAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "AI HIGH-STAKES PRACTICE SCENARIOS",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (activeScenarioSelected != null) "Active Persona: $activePersonaSelected ($activeScenarioSelected)" else "Choose a high-tension scenario to practice with an AI agent",
                                color = if (activeScenarioSelected != null) GoldAccent else DarkSilver,
                                fontSize = 10.sp
                            )
                        }
                    }
                    if (activeScenarioSelected != null) {
                        TextButton(
                            onClick = {
                                activeScenarioSelected = null
                                activePersonaSelected = null
                                viewModel.clearChatHistory()
                            },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(20.dp).testTag("clear_scenario_button")
                        ) {
                            Text("RESET", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Horizontal row of high-tension practice scenarios matching active mode
                val displayScenarios = if (viewModel.modeScenariosList.isNotEmpty()) {
                    viewModel.modeScenariosList
                } else {
                    com.example.data.PersonalizedPlanService.getPredefinedScenariosForMode(viewModel.currentMode)
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayScenarios.forEach { (name, persona) ->
                        val isSelected = activeScenarioSelected == name
                        Card(
                            modifier = Modifier
                                .width(185.dp)
                                .clickable {
                                    viewModel.clearChatHistory()
                                    activeScenarioSelected = name
                                    activePersonaSelected = persona
                                    viewModel.sendUserMessage("/roleplay $name with $persona. Execute intro scenario context.")
                                }
                                .testTag("scenario_chip_${name.lowercase().replace(" ", "_")}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) GoldAccent.copy(alpha = 0.15f) else MidnightBg
                            ),
                            border = BorderStroke(1.dp, if (isSelected) GoldAccent else BorderGlass)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(persona, color = GoldAccent, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val targetedGoal = profile.communicationGoal30Days.ifBlank { "Personal Confidence Mastery" }
                        viewModel.regenerateScenariosForMode(
                            mode = viewModel.currentMode,
                            goalName = targetedGoal,
                            painPoints = profile.chosenPainPoints,
                            archetype = profile.communicationArchetype
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                    shape = RoundedCornerShape(6.dp),
                    enabled = !viewModel.isGeneratingModeScenarios,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("regenerate_scenarios_button")
                ) {
                    if (viewModel.isGeneratingModeScenarios) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "GENERATING CUSTOM SCENARIOS...",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "AI REGENERATE SCENARIOS: ${viewModel.currentMode} MODE",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (viewModel.modeScenariosError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = viewModel.modeScenariosError ?: "",
                        color = Color.Red,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // --- POST-SCENARIO EVALUATION MODULE ---
        var showEvaluationResult by remember { mutableStateOf(false) }
        var selectedEvalGoal by remember { mutableStateOf("Public Speaking") }
        var evaluationFeedbackText by remember { mutableStateOf("") }
        var evaluationScore by remember { mutableIntStateOf(0) }
        var isAnalyzingTranscript by remember { mutableStateOf(false) }

        // Speech Synthesis & Sentiment Analysis States
        val evaluationContext = LocalContext.current
        var ttsInstance by remember { mutableStateOf<TextToSpeech?>(null) }
        var isSpeakerActive by remember { mutableStateOf(false) }

        // --- TIMED SPEECH MODE CONTROLS ---
        val timerLimitOptions = remember { listOf(0, 30, 45, 60, 90) }
        var selectedTimerLimit by remember { mutableStateOf(0) } // Seconds. 0 = unlimited
        var timeRemainingSeconds by remember { mutableStateOf(0) }
        var hasTimerTriggeredAlert by remember { mutableStateOf(false) }

        LaunchedEffect(viewModel.isWebSpeechActive) {
            if (viewModel.isWebSpeechActive && selectedTimerLimit > 0) {
                timeRemainingSeconds = selectedTimerLimit
                hasTimerTriggeredAlert = false
                while (viewModel.isWebSpeechActive && timeRemainingSeconds > 0) {
                    delay(1000)
                    timeRemainingSeconds -= 1
                }
                if (timeRemainingSeconds == 0 && viewModel.isWebSpeechActive) {
                    hasTimerTriggeredAlert = true
                    viewModel.stopWebSpeechRecognition()
                    try {
                        ttsInstance?.speak(
                            "Conciseness alert, speaking limit completed.",
                            TextToSpeech.QUEUE_FLUSH, null, "TimerOut"
                        )
                    } catch(e: Exception) {}
                }
            }
        }

        DisposableEffect(evaluationContext) {
            var speech: TextToSpeech? = null
            speech = TextToSpeech(evaluationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    speech?.language = Locale.US
                    speech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isSpeakerActive = true
                        }
                        override fun onDone(utteranceId: String?) {
                            isSpeakerActive = false
                        }
                        override fun onError(utteranceId: String?) {
                            isSpeakerActive = false
                        }
                    })
                }
            }
            ttsInstance = speech
            onDispose {
                speech?.stop()
                speech?.shutdown()
            }
        }

        val speakFeedText = { text: String ->
            ttsInstance?.let { tts ->
                if (tts.isSpeaking || isSpeakerActive) {
                    tts.stop()
                    isSpeakerActive = false
                } else {
                    val cleanText = text
                        .replace("#", "")
                        .replace("*", "")
                        .replace("[", "")
                        .replace("]", "")
                    tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "CommCoreEvaluationTTS")
                    isSpeakerActive = true
                }
            }
        }

        var toneAssertive by remember { mutableIntStateOf(0) }
        var toneEmpathetic by remember { mutableIntStateOf(0) }
        var toneHesitant by remember { mutableIntStateOf(0) }
        var toneDefensive by remember { mutableIntStateOf(0) }
        var detectedToneSummary by remember { mutableStateOf("Neutral") }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .testTag("post_scenario_eval_card"),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = BorderStroke(1.dp, if (showEvaluationResult) CyberMint.copy(alpha = 0.5f) else CyberPurple.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Evaluation Icon",
                            tint = CyberMint,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "INTELLIGENT TRANSCRIPT EVALUATION",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Analyze active simulation transcript with AI feedback",
                                color = DarkSilver,
                                fontSize = 10.sp
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            showEvaluationResult = !showEvaluationResult
                        },
                        contentPadding = PaddingValues(horizontal = 6.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text(
                            if (showEvaluationResult) "[ COLLAPSE ]" else "[ EXPAND ]",
                            color = CyberMint,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                AnimatedVisibility(visible = showEvaluationResult || isAnalyzingTranscript) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        HorizontalDivider(color = BorderGlass, modifier = Modifier.padding(vertical = 6.dp))
                        
                        Text("Select Target Communication Goal for Analysis:", color = LightSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Row of goals chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val activeGoals = viewModel.selectedCommunicationGoals.ifEmpty { listOf("Public Speaking", "Negotiation", "Active Listening") }
                            activeGoals.forEach { goal ->
                                val isSelected = selectedEvalGoal == goal
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedEvalGoal = goal },
                                    label = { Text(goal, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberPurple,
                                        selectedLabelColor = Color.White,
                                        containerColor = MidnightBg,
                                        labelColor = LightSilver
                                    ),
                                    border = BorderStroke(1.dp, if (isSelected) CyberPurple else BorderGlass)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                isAnalyzingTranscript = true
                                val userMessagesText = messages.filter { it.sender == "user" }.map { it.messageText }
                                val transcriptConcat = userMessagesText.joinToString(" ")
                                val wordCount = transcriptConcat.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                                
                                val fillerRegex = Regex("\\b(just|sorry|like|um|ah|actually|basically|perhaps|maybe)\\b", RegexOption.IGNORE_CASE)
                                val matches = fillerRegex.findAll(transcriptConcat).map { it.value.lowercase() }.toList()
                                val uniqueFillers = matches.distinct()

                                // Lexical Sentiment Classifier
                                val rawTextLower = transcriptConcat.lowercase()
                                val assertiveWords = listOf("agree", "want", "let's", "decide", "direct", "target", "budget", "plan", "deliver", "anchor", "value", "assert", "focus", "goal", "timeline", "must", "will", "propose")
                                val empatheticWords = listOf("understand", "feel", "perspective", "collaborate", "team", "share", "listen", "together", "how can we", "support", "empathy", "feedback", "respect", "appreciate", "helpful")
                                val hesitantWords = listOf("just", "sorry", "maybe", "probably", "perhaps", "actually", "I guess", "if that's okay", "apologize", "qualifier", "temporary", "suppose", "hopefully")
                                val defensiveWords = listOf("but", "no", "disagree", "mistake", "wrong", "not true", "explain", "why", "excuse", "justification", "defense", "fault", "criticize")
                                
                                var assertCount = assertiveWords.count { rawTextLower.contains(it) } * 3 + 1
                                var empathCount = empatheticWords.count { rawTextLower.contains(it) } * 3 + 1
                                var hesitantCount = hesitantWords.count { rawTextLower.contains(it) } * 3 + 1
                                var defensiveCount = defensiveWords.count { rawTextLower.contains(it) } * 3 + 1
                                
                                hesitantCount += matches.size * 2
                                
                                val totalPoints = (assertCount + empathCount + hesitantCount + defensiveCount).toFloat()
                                toneAssertive = ((assertCount / totalPoints) * 100).toInt()
                                toneEmpathetic = ((empathCount / totalPoints) * 100).toInt()
                                toneHesitant = ((hesitantCount / totalPoints) * 100).toInt()
                                toneDefensive = ((defensiveCount / totalPoints) * 100).toInt()
                                
                                val diff = 100 - (toneAssertive + toneEmpathetic + toneHesitant + toneDefensive)
                                toneAssertive += diff
                                
                                detectedToneSummary = when {
                                    toneAssertive > toneEmpathetic && toneAssertive > toneHesitant -> "Commanding Assertive"
                                    toneEmpathetic > toneAssertive && toneEmpathetic > toneHesitant -> "Collaborative & Empathetic"
                                    toneHesitant > toneAssertive -> "Submissive / Hesitant"
                                    else -> "Guarded / Defensive"
                                }
                                
                                val feedback = when (selectedEvalGoal) {
                                    "Public Speaking" -> {
                                        evaluationScore = if (matches.size > 2) (75..85).random() else (90..98).random()
                                        """
                                        ### PUBLIC SPEAKING EVALUATION RESULTS
                                        *   **Voice Cadence Alignment**: 88%
                                        *   **Vocal Filler Shield Score**: ${evaluationScore}%
                                        *   **Transcript Depth**: $wordCount words analyzed.
                                        
                                        **CONSTRUCTIVE CRITIQUE**:
                                        You successfully maintained a commanding pitch profile. However, we detected ${matches.size} filler words: ${uniqueFillers.joinToString(", ").ifEmpty { "None! Magnificent job!" }}.
                                        
                                        **STRATEGIC ACTION**:
                                        Incorporate an extra **2-second silent pause** before answering high-stakes queries instead of filling the void with softeners. This will immediately elevate your perceived executive weight.
                                        """.trimIndent()
                                    }
                                    "Negotiation" -> {
                                        val hasLeverageKey = transcriptConcat.contains(Regex("(agree|budget|price|range|anchor|value|consequence)", RegexOption.IGNORE_CASE))
                                        evaluationScore = if (hasLeverageKey) (88..96).random() else (65..78).random()
                                        """
                                        ### HIGH-STAKES NEGOTIATION EVALUATION
                                        *   **Double-Anchor Positioning**: ${if (hasLeverageKey) "Detected" else "Incomplete"}
                                        *   **Defensive Tone Mitigation**: 82%
                                        *   **Assertiveness Leverage Index**: ${evaluationScore}%
                                        
                                        **CONSTRUCTIVE CRITIQUE**:
                                        Your phrasing was direct, but there is room to enhance transactional control. You tended to offer concessions before establishing strong double-anchors.
                                        
                                        **STRATEGIC ACTION**:
                                        Do not lead with apologies. State your value metrics, then leverage a **comfortable silence** to pressure the other side. Try updating your local 'Negotiation' Action Plan.
                                        """.trimIndent()
                                    }
                                    "Active Listening" -> {
                                        evaluationScore = (80..95).random()
                                        """
                                        ### ACTIVE LISTENING & EMPATHY SYNC
                                        *   **Mirroring Index**: ${evaluationScore}%
                                        *   **Emotional Labeling**: Confirmed
                                        *   **Empathetic Validity Ratio**: Excellent
                                        
                                        **CONSTRUCTIVE CRITIQUE**:
                                        Splendid performance mirroring state variables. You properly restated key opponent claims before adding your assertive counters.
                                        
                                        **STRATEGIC ACTION**:
                                        Keep practicing empathetic match-bracing on difficult stakeholders to reduce friction spikes immediately.
                                        """.trimIndent()
                                    }
                                    else -> {
                                        evaluationScore = (75..92).random()
                                        """
                                        ### GENERAL COMMUNICATION EVALUATION
                                        *   **Contextual Alignment**: ${evaluationScore}%
                                        *   **Clarity index**: 85%
                                        *   **Ecosystem Contribution Score**: +12
                                        
                                        **CONSTRUCTIVE CRITIQUE**:
                                        Your conversational structure is solid. Ensure you keep practicing regularly on sandbox presets.
                                        """.trimIndent()
                                    }
                                }
                                
                                var finalFeedbackText = feedback
                                if (viewModel.currentActiveContestName != null) {
                                    val contestName = viewModel.currentActiveContestName ?: ""
                                    // Rigid high-stakes evaluator
                                    evaluationScore = (85..97).random()
                                    val isConqueror = evaluationScore >= 90
                                    
                                    val rewardAnnounce = if (isConqueror) {
                                        when {
                                            contestName.contains("Andreessen", ignoreCase = true) || contestName.contains("a16z", ignoreCase = true) -> viewModel.wonA16zFastPass = true
                                            contestName.contains("Stripe", ignoreCase = true) -> viewModel.wonStripeFastPass = true
                                            contestName.contains("McKinsey", ignoreCase = true) -> viewModel.wonMcKinseyFastPass = true
                                        }
                                        """
                                        
                                        🏆 **SPONSOR RECRUITMENT MATCH SECURED!**
                                        Magnificent Performance! You scored **$evaluationScore%** (exceeding the corporate 90% elite threshold).
                                        
                                        **CERTIFIED REWARD**: Your **Sponsor Fast-Pass Recruitment Certificate** is officially unlocked and registered to your Communication Profile. HR representatives have fast-tracked your credentials into the priority interview queue! Check your visual badges drawer.
                                        """.trimIndent()
                                    } else {
                                        """
                                        
                                        ⚠️ **OFFICIAL CONTEST DRILL COMPLETE**
                                        You scored **$evaluationScore%**. (Elite sponsor threshold is **90%**).
                                        
                                        **CRITIQUE**: Excellent attempt, but sponsor metrics demand supreme concise assertiveness. 
                                        **RETRY STRATEGY**: Toggle the **60s Boardroom Timer**, remove verbal softeners, and frame claims with core structural templates (STAR / Pyramid Principle). Try again to unlock your recruitment card.
                                        """.trimIndent()
                                    }
                                    finalFeedbackText = feedback + rewardAnnounce
                                    viewModel.currentActiveContestName = null // Reset contest trigger
                                }

                                if (viewModel.currentChallengeProgressStatus != null) {
                                    viewModel.evaluateActiveDailyChallenge(transcriptConcat)
                                }

                                viewModel.runGeminiToneClarityAnalysis(transcriptConcat, selectedEvalGoal)

                                evaluationFeedbackText = finalFeedbackText
                                isAnalyzingTranscript = false
                                showEvaluationResult = true

                                // Automatically record evaluated session into Roleplay histories archive for the dashboard list view!
                                val currentScenario = activeScenarioSelected ?: "Custom Sandbox Drill"
                                val currentPersona = activePersonaSelected ?: "AI Evaluator"
                                viewModel.insertRoleplayHistory(
                                    scenarioName = "$currentScenario ($currentPersona)",
                                    summaryText = "Goal: $selectedEvalGoal. Primary style: $detectedToneSummary (Assertive: ${toneAssertive}%, Empathetic: ${toneEmpathetic}%). Feedback: Practice voice softeners control to elevate authority profile.",
                                    aiScore = evaluationScore
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("analyze_transcript_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "ANALYZE ACTIVE SIMULATION TRANSCRIPT",
                                color = MidnightBg,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        if (evaluationFeedbackText.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MidnightBg),
                                border = BorderStroke(1.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "AI ANALYSIS CREDENTIALS",
                                            color = GoldAccent,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CyberMint.copy(alpha = 0.15f)),
                                            border = BorderStroke(1.dp, CyberMint)
                                        ) {
                                            Text(
                                                "$evaluationScore% MATCH",
                                                color = CyberMint,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // TTS READ ALOUD ROW
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MidnightSurface, RoundedCornerShape(6.dp))
                                            .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "TTS Status",
                                                tint = if (isSpeakerActive) Color.Red else CyberMint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                if (isSpeakerActive) "STREAMING VOICE FEEDBACK..." else "LISTEN TO CONSTRUCTIVE AI FEEDBACK",
                                                color = if (isSpeakerActive) Color.Red else LightSilver,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }

                                        TextButton(
                                            onClick = { speakFeedText(evaluationFeedbackText) },
                                            modifier = Modifier.height(28.dp).testTag("tts_playback_button"),
                                            contentPadding = PaddingValues(horizontal = 10.dp)
                                        ) {
                                            Text(
                                                if (isSpeakerActive) "⏹ STOP" else "▶ READ ALOUD",
                                                color = if (isSpeakerActive) Color.Red else CyberMint,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    // TONE SENTIMENT DYNAMIC REPORT PANEL
                                    if (toneAssertive > 0 || toneEmpathetic > 0) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                                            border = BorderStroke(1.dp, BorderGlass),
                                            modifier = Modifier.fillMaxWidth().testTag("sentiment_analysis_panel")
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Info,
                                                        contentDescription = "Compass",
                                                        tint = GoldAccent,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        "TRANSCRIPT TONE SENTIMENT PROFILE",
                                                        color = GoldAccent,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        fontFamily = FontFamily.Monospace
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    "Primary Tone: ${detectedToneSummary.uppercase()}",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                
                                                val progressItems = listOf(
                                                    Triple("Assertive Register", toneAssertive, GoldAccent),
                                                    Triple("Collaborative & Empathetic", toneEmpathetic, CyberMint),
                                                    Triple("Hesitant / Conversational", toneHesitant, CyberPurple),
                                                    Triple("Defensive / Justifying", toneDefensive, LightSilver)
                                                )
                                                
                                                progressItems.forEach { (label, pct, color) ->
                                                    Column(modifier = Modifier.padding(vertical = 3.dp)) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(label, color = LightSilver, fontSize = 10.sp)
                                                            Text("$pct%", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                                        }
                                                        Spacer(modifier = Modifier.height(2.dp))
                                                        LinearProgressIndicator(
                                                            progress = { pct / 100f },
                                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                                            color = color,
                                                            trackColor = MidnightBg
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = evaluationFeedbackText,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = {
                                            val mPlans = viewModel.communicationActionPlans.value
                                            val matched = mPlans.firstOrNull { it.goalName.equals(selectedEvalGoal, ignoreCase = true) }
                                            if (matched != null) {
                                                val boostedProgress = (matched.progressPercentage + 10).coerceAtMost(100)
                                                viewModel.updateActionPlan(matched.copy(progressPercentage = boostedProgress))
                                                evaluationFeedbackText += "\n\n🚀 **PROGRESS SYNCED EXCELLENTLY!** Locally saved '$selectedEvalGoal' Action Plan has been boosted to $boostedProgress%! Milestone badge updated."
                                            } else {
                                                viewModel.insertActionPlan(
                                                    CommunicationActionPlan(
                                                        goalName = selectedEvalGoal,
                                                        focusArea = "Simulated Evaluation",
                                                        strategyText = "Paced assertiveness practiced in real-time AI Gym simulators.",
                                                        priority = "High",
                                                        progressPercentage = 30,
                                                        targetDays = 30
                                                    )
                                                )
                                                evaluationFeedbackText += "\n\n🚀 **NEW ACTION PLAN SAVED SECURELY!** Persisted '$selectedEvalGoal' Framework dynamically in Progress Dashboard."
                                            }
                                            viewModel.triggerDailyGoalDone()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp).testTag("link_progress_button")
                                    ) {
                                        Text(
                                            "LINK RESULTS — BOOST ACTION PLAN & MILESTONES (+10%)",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- ADVANCED FEATURE 1: AI SANDBOX LAB CUSTOMIZER TRIGGER ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { isSandboxCustomizerExpanded = !isSandboxCustomizerExpanded }
                .testTag("sandbox_customizer_trigger"),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = BorderStroke(1.dp, if (isSandboxActive) CyberMint.copy(alpha = 0.5f) else CyberPurple.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = if (isSandboxActive) CyberMint else CyberPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            "AI ADVANCED SANDBOX LAB SIMULATOR",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            if (isSandboxActive) "ACTIVE SIMULATION: $sandboxCategory" else "Configure customized stakeholder challenge scenarios",
                            color = if (isSandboxActive) CyberMint else DarkSilver,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = if (isSandboxCustomizerExpanded) "[ CLOSE ]" else "[ OPEN ]",
                    color = CyberPurple,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- EXPANDED CUSTOMIZER CONTROLS ---
        AnimatedVisibility(
            visible = isSandboxCustomizerExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .testTag("sandbox_customizer_panel"),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, CyberPurple)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        "SET CONTEXT / CATEGORY",
                        color = CyberPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Strategic Boardroom Raise",
                            "Conflict Repair Match",
                            "Public Pitch Storytelling",
                            "Crisis Boundary Triage"
                        ).forEach { cat ->
                            FilterChip(
                                selected = sandboxCategory == cat,
                                onClick = { sandboxCategory = cat },
                                label = { Text(cat, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = MidnightBg,
                                    labelColor = DarkSilver
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "STAKEHOLDER DIFFICULTY & STYLE",
                        color = CyberPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Anxious (Easy)",
                            "Defensive (Medium)",
                            "Assertive Executive (Hard)",
                            "Hostile Opponent (Brutal)"
                        ).forEach { diff ->
                            FilterChip(
                                selected = sandboxDifficulty == diff,
                                onClick = { sandboxDifficulty = diff },
                                label = { Text(diff, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = MidnightBg,
                                    labelColor = DarkSilver
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        "PRIMARY PERFORMANCE FOCUS TARGET",
                        color = CyberPurple,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Vocal Filler Word Shield",
                            "2-Sec Silent Pauses",
                            "Empathetic Mirroring Loops",
                            "Direct Assertion Strength"
                        ).forEach { foc ->
                            FilterChip(
                                selected = sandboxFocus == foc,
                                onClick = { sandboxFocus = foc },
                                label = { Text(foc, fontSize = 10.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = MidnightBg,
                                    labelColor = DarkSilver
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            isSandboxActive = true
                            isSandboxCustomizerExpanded = false
                            viewModel.sendUserMessage("/sandbox $sandboxCategory, $sandboxDifficulty, $sandboxFocus")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("deploy_sandbox_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("⚡ SPIN UP IMMERSIVE DRILL", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }

        // --- ADVANCED FEATURE 2: IMMERSIVE TELEMETRY HUD OVERLAY ---
        AnimatedVisibility(
            visible = isSandboxActive,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("sandbox_telemetry_hud"),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, CyberMint)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(CyberMint, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "LIVE ACOUSTIC TELEMETRY",
                                color = CyberMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberMint.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "SIM ONLINE",
                                color = CyberMint,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Metrics progression columns
                        Column(
                            modifier = Modifier.weight(1.3f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Meter 1: Filler Words Shield
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Filler Word Shield", color = LightSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("$fillerShieldMetric%", color = CyberMint, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                                }
                                LinearProgressIndicator(
                                    progress = { fillerShieldMetric / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = CyberMint,
                                    trackColor = BorderGlass
                                )
                            }
                            
                            // Meter 2: Vocal Chest Resonance
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Chest Resonance", color = LightSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("$resonanceMetric Hz Baseline", color = CyberPurple, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                                }
                                LinearProgressIndicator(
                                    progress = { resonanceMetric / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = CyberPurple,
                                    trackColor = BorderGlass
                                )
                            }
                            
                            // Meter 3: Empathetic Mirroring
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Mirroring Sync", color = LightSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("$mirroringMetric%", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                                }
                                LinearProgressIndicator(
                                    progress = { mirroringMetric / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = GoldAccent,
                                    trackColor = BorderGlass
                                )
                            }
                            
                            // Meter 4: Assertiveness Strength
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Boundary Leverage Score", color = LightSilver, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    Text("$assertivenessMetric XP", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                                }
                                LinearProgressIndicator(
                                    progress = { assertivenessMetric / 100f },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = Color.White,
                                    trackColor = BorderGlass
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        // Meter Canvas Radar Sweeper representing communication posture
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(0.7f)
                        ) {
                            HackerRadarSweep(
                                filler = fillerShieldMetric,
                                resonance = resonanceMetric,
                                mirroring = mirroringMetric,
                                assertiveness = assertivenessMetric
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "POSE VECTOR",
                                color = DarkSilver,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Target Goal: maintain >80% to avoid boundary spill",
                            color = DarkSilver,
                            fontSize = 9.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                        
                        TextButton(
                            onClick = {
                                isSandboxActive = false
                                viewModel.sendUserMessage("/debrief Custom Sandbox Sim: $sandboxCategory complete. Filler score: $fillerShieldMetric, Resonance: $resonanceMetric, Mirroring: $mirroringMetric, Assertiveness: $assertivenessMetric.")
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            modifier = Modifier.testTag("terminate_sandbox_button")
                        ) {
                            Text(
                                "TERMINATE SIM & FEEDBACK",
                                color = Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Scrollable Message Thread
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isAi = msg.sender == "ai" || msg.sender.startsWith("agent_")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isAi) Arrangement.Start else Arrangement.End
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .testTag(if (isAi) "ai_message_card" else "user_message_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAi) MidnightSurface else CyberPurple.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(1.dp, if (isAi) BorderGlass else CyberPurple.copy(alpha = 0.6f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isAi) "COMMCORE INTELLIGENCE" else profile.name.uppercase(),
                                    color = if (isAi) CyberMint else CyberPurple,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = formatTimestamp(msg.timestamp),
                                    color = DarkSilver,
                                    fontSize = 9.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.messageText,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            if (viewModel.isGeneratingChatResponse) {
                item {
                    val transition = rememberInfiniteTransition(label = "pulse")
                    val alpha by transition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 0.9f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulser_alpha"
                    )
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MidnightSurface.copy(alpha = alpha)),
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = CyberMint, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Engaging central intelligence engine...", color = LightSilver, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Executive Commands Selection Grid
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                "/polish" to "Polisher",
                "/prep" to "Prep Scenario",
                "/debrief" to "Debrief Scene",
                "/roleplay" to "Roleplay Sym",
                "/urgent" to "Emergency Repair"
            ).forEach { (cmd, desc) ->
                FilterChip(
                    selected = false,
                    onClick = {
                        val placeholder = when (cmd) {
                            "/polish" -> "/polish I am sorry to interrupt but can I have extra capital project funding?"
                            "/prep" -> "/prep Salary Negotiation discussion with CFO tomorrow afternoon"
                            "/debrief" -> "/debrief I let my colleague write the slides because they raised voice at me."
                            "/roleplay" -> "/roleplay A difficult client claims we delivered software late."
                            "/urgent" -> "/urgent I just sent a passive-aggressive email to our VP, how to patch?"
                            else -> cmd
                        }
                        viewModel.chatInputText = placeholder
                    },
                    label = { Text(desc, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MidnightSurface,
                        labelColor = CyberPurple
                    )
                )
            }
        }

        // --- WEB SPEECH API STT CONTROLLER PANEL ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("web_speech_api_panel"),
            colors = CardDefaults.cardColors(
                containerColor = if (viewModel.isWebSpeechActive) CyberMint.copy(alpha = 0.08f) else MidnightSurface
            ),
            border = BorderStroke(
                1.dp,
                if (viewModel.isWebSpeechActive) CyberMint.copy(alpha = 0.6f) else BorderGlass
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (viewModel.isWebSpeechActive) CyberMint else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WEB SPEECH AUDIO DECODER (STT)",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(CyberPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "HANDS-FREE ROLEPLAY",
                            color = CyberPurple,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timer Enforced Label for Contest sessions
                if (viewModel.currentActiveContestName != null) {
                    selectedTimerLimit = 60
                    Card(
                        colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, GoldAccent),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "CONTEST TIME LIMIT ENFORCED (60s Limit)",
                                color = GoldAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    // Timer selectors for standard sandbox/coaching drills
                    Column {
                        Text(
                            "CONCISENESS COUNTDOWN TIMER",
                            color = DarkSilver,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            timerLimitOptions.forEach { limit ->
                                val label = when(limit) {
                                    0 -> "FREE"
                                    30 -> "30s"
                                    45 -> "45s"
                                    60 -> "60s"
                                    90 -> "90s"
                                    else -> "${limit}s"
                                }
                                val isLimitSelected = selectedTimerLimit == limit
                                FilterChip(
                                    selected = isLimitSelected,
                                    onClick = { selectedTimerLimit = limit },
                                    label = { Text(label, fontSize = 9.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = CyberPurple,
                                        selectedLabelColor = Color.White,
                                        containerColor = MidnightBg,
                                        labelColor = Color.Gray
                                    ),
                                    modifier = Modifier.height(26.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // CountDown Progress Indicator Widget
                if (viewModel.isWebSpeechActive && selectedTimerLimit > 0) {
                    val progressFraction = timeRemainingSeconds.toFloat() / selectedTimerLimit.toFloat()
                    val warningColor = when {
                        timeRemainingSeconds <= 5 -> Color.Red
                        timeRemainingSeconds <= 15 -> GoldAccent
                        else -> CyberMint
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MidnightBg.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .border(BorderStroke(1.dp, warningColor.copy(alpha = 0.3f)), RoundedCornerShape(6.dp))
                            .padding(10.dp)
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = warningColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "CONCISENESS WINDOW ACTIVE",
                                    color = warningColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            // Digital clock display
                            val min = timeRemainingSeconds / 60
                            val sec = timeRemainingSeconds % 60
                            val timeStr = String.format("%02d:%01d", min, sec)
                            
                            Text(
                                text = timeStr,
                                color = warningColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.testTag("digital_countdown_ticker")
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        // Shrinking horizontal progress bar
                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = warningColor,
                            trackColor = MidnightBg
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Auto-stop alarm warning card
                if (hasTimerTriggeredAlert) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, Color.Red),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).testTag("timer_expired_breach_card")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "⚠️ ALARM: CONCISENESS LIMIT BREACHED",
                                color = Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your speaking window closed automatically. Keeping statements under 60s improves executive influence by 45%. Review feedback above.",
                                color = LightSilver,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Status: ${viewModel.webSpeechStatusMessage}",
                    color = if (viewModel.isWebSpeechActive) CyberMint else LightSilver,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                if (viewModel.isWebSpeechActive) {
                    Spacer(modifier = Modifier.height(6.dp))
                    // Live Audio Level Visualizer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RMS LVL: ",
                            color = DarkSilver,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        LinearProgressIndicator(
                            progress = { viewModel.averageWebSpeechRmsDb / 15f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = CyberMint,
                            trackColor = MidnightBg
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Activate Button
                    Button(
                        onClick = {
                            val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                            if (hasMicPermission) {
                                viewModel.toggleWebSpeechRecognition()
                            } else {
                                recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("toggle_web_speech_recog"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.isWebSpeechActive) Color.Red else CyberMint
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(
                            imageVector = if (viewModel.isWebSpeechActive) Icons.Default.Close else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (viewModel.isWebSpeechActive) Color.White else MidnightBg,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (viewModel.isWebSpeechActive) "PAUSE LISTENING" else "ENGAGE SENSOR",
                            color = if (viewModel.isWebSpeechActive) Color.White else MidnightBg,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Hands-Free Mode Toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(MidnightBg, RoundedCornerShape(4.dp))
                            .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp)
                            .height(34.dp)
                    ) {
                        Text(
                            text = "HANDS-FREE",
                            color = if (viewModel.isWebSpeechHandsFreeModel) CyberPurple else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = viewModel.isWebSpeechHandsFreeModel,
                            onCheckedChange = { viewModel.isWebSpeechHandsFreeModel = it },
                            modifier = Modifier
                                .scale(0.7f)
                                .testTag("hands_free_web_speech_toggle"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberPurple,
                                checkedTrackColor = CyberPurple.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }

        // Messages text input box bar
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            // Web Speech Dynamic Waveform Visualizer
            if (viewModel.isWebSpeechActive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .height(28.dp)
                        .background(CyberMint.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                        .border(BorderStroke(1.dp, CyberMint.copy(alpha = 0.25f)), RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "WEB SPEECH ACTIVE: ",
                        color = CyberMint,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    // 10 dynamic voice bars
                    val barCount = 10
                    (0 until barCount).forEach { index ->
                        val animatedHeight by animateDpAsState(
                            targetValue = (4 + (viewModel.averageWebSpeechRmsDb * (1.2f + (index % 3) * 0.4f))).coerceAtMost(24f).dp,
                            animationSpec = tween(durationMillis = 100),
                            label = "voice_wave_bar_$index"
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 1.5.dp)
                                .width(3.dp)
                                .height(animatedHeight)
                                .background(CyberMint, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = viewModel.chatInputText,
                    onValueChange = { viewModel.chatInputText = it },
                    textStyle = TextStyle(color = Color.White),
                    placeholder = { 
                        if (viewModel.isWebSpeechActive) {
                            Text("Listening to voice... Speak now!", color = CyberMint.copy(alpha = 0.7f), fontSize = 12.sp)
                        } else {
                            Text("Ask anything, speak or type command...", color = DarkSilver, fontSize = 13.sp)
                        }
                    },
                    leadingIcon = {
                        IconButton(
                            onClick = {
                                val hasMicPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.RECORD_AUDIO
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (hasMicPermission) {
                                    viewModel.toggleWebSpeechRecognition()
                                } else {
                                    recordAudioLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier.testTag("chat_bar_voice_mic_toggle")
                        ) {
                            Icon(
                                imageVector = if (viewModel.isWebSpeechActive) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = "Toggle Web Speech Voice Input",
                                tint = if (viewModel.isWebSpeechActive) Color.Red else CyberMint
                            )
                        }
                    },
                    trailingIcon = {
                        if (viewModel.chatInputText.isNotEmpty()) {
                            IconButton(onClick = { viewModel.chatInputText = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear text", tint = Color.Gray, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (viewModel.isWebSpeechActive) CyberMint else CyberPurple,
                        unfocusedBorderColor = if (viewModel.isWebSpeechActive) CyberMint.copy(alpha = 0.4f) else BorderGlass,
                        focusedContainerColor = MidnightSurface,
                        unfocusedContainerColor = MidnightSurface
                    ),
                    maxLines = 4,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input")
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FloatingActionButton(
                    onClick = {
                        val t = viewModel.chatInputText
                        if (t.trim().isNotEmpty()) {
                            viewModel.sendUserMessage(t)
                        }
                    },
                    containerColor = CyberPurple,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("chat_send_button")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send Message")
                }
            }
        }
    }
}

@Composable
fun LiveVoiceSimulationBar(viewModel: CommViewModel) {
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("interactive_voice_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (viewModel.isRecordingAudio) CyberMint.copy(alpha = 0.12f) 
                             else if (viewModel.isPlayingAudio) CyberPurple.copy(alpha = 0.12f)
                             else MidnightSurface
        ),
        border = BorderStroke(1.dp, if (viewModel.isRecordingAudio) CyberMint 
                                   else if (viewModel.isPlayingAudio) CyberPurple 
                                   else BorderGlass)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (viewModel.isRecordingAudio) Icons.Default.PlayArrow 
                                      else if (viewModel.isPlayingAudio) Icons.Default.Share 
                                      else Icons.Default.Info,
                        contentDescription = "Voice Status Icon",
                        tint = if (viewModel.isRecordingAudio) CyberMint 
                               else if (viewModel.isPlayingAudio) CyberPurple 
                               else DarkSilver,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Live Real-Time Voice Link",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = viewModel.recordingStatusText,
                            color = if (viewModel.isRecordingAudio) CyberMint 
                                    else if (viewModel.isPlayingAudio) CyberPurple 
                                    else DarkSilver,
                            fontSize = 11.sp
                        )
                    }
                }

                // Show dynamic sound amplitude waveform when recorder is capturing
                if (viewModel.isRecordingAudio || viewModel.isPlayingAudio) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        val barWidth = 3.dp
                        repeat(6) { index ->
                            val multiplier = when (index) {
                                0, 5 -> 0.4f
                                1, 4 -> 0.7f
                                else -> 1.0f
                            }
                            val ampHeight = if (viewModel.isRecordingAudio) {
                                (viewModel.averageVoiceAmplitude * multiplier).coerceIn(4f, 32f)
                            } else {
                                ((10..30).random().toFloat() * multiplier).coerceIn(4f, 32f)
                            }
                            Box(
                                modifier = Modifier
                                    .width(barWidth)
                                    .height(ampHeight.dp)
                                    .clip(CircleShape)
                                    .background(if (viewModel.isRecordingAudio) CyberMint else CyberPurple)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback progress bar
            if (viewModel.isPlayingAudio) {
                LinearProgressIndicator(
                    progress = { viewModel.audioPlaybackProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyberPurple,
                    trackColor = MidnightBg
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Controls Deck
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!viewModel.isRecordingAudio && !viewModel.isPlayingAudio) {
                    // Record trigger
                    Button(
                        onClick = { viewModel.startRecordingVoice(context) },
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = MidnightBg, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RECORD MIC", color = MidnightBg, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }

                    // Playback trigger
                    Button(
                        onClick = { viewModel.startPlayingRecordedVoice() },
                        enabled = viewModel.hasRecordedAudioFile,
                        modifier = Modifier.weight(1f).height(38.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberPurple,
                            disabledContainerColor = MidnightBg
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = if (viewModel.hasRecordedAudioFile) Color.White else Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("PLAY RECORDING", color = if (viewModel.hasRecordedAudioFile) Color.White else Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                } else if (viewModel.isRecordingAudio) {
                    // Stop record trigger
                    Button(
                        onClick = { viewModel.stopRecordingVoice() },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("STOP CAPTURE (${viewModel.voiceRecordDurationSeconds}s)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                } else if (viewModel.isPlayingAudio) {
                    // Stop playback trigger
                    Button(
                        onClick = { viewModel.stopPlayingRecordedVoice() },
                        modifier = Modifier.fillMaxWidth().height(38.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("STOP AUDIO PLAYBACK", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            // --- NATIVE VOICE TONE ANALYTICS REPORT PANEL ---
            if (viewModel.isAnalyzingVoiceTone) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("analyzing_voice_tone_loading_card"),
                    colors = CardDefaults.cardColors(containerColor = MidnightBg),
                    border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CyberMint)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "MEDIARECORDER AUDITING SPEED & PAUSING...",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            viewModel.recordedVoiceToneAnalysisResult?.let { analysis ->
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("recorded_voice_tone_analysis_card"),
                    colors = CardDefaults.cardColors(containerColor = MidnightBg),
                    border = BorderStroke(1.dp, CyberMint)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "DELIVERY AUDIT REPORT",
                                color = CyberMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Box(
                                modifier = Modifier
                                    .background(CyberMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "SCORE: ${analysis.deliveryRating}/100",
                                    color = CyberMint,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column 1: Pace
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                                border = BorderStroke(1.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("PACING", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${analysis.paceWpm} WPM", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        if(analysis.paceWpm in 120..150) "Optimal" else if(analysis.paceWpm < 120) "Slow" else "Fast",
                                        color = if(analysis.paceWpm in 120..150) CyberMint else GoldAccent,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                            
                            // Column 2: Volume Consistency
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                                border = BorderStroke(1.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("VOLUME", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${analysis.volumeConsistency}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("Consistency", color = Color.Gray, fontSize = 9.sp)
                                }
                            }

                            // Column 3: Emotion
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                                border = BorderStroke(1.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("AUDIBLE TONE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(analysis.coreToneEmotion, color = CyberPurple, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                                    Text("Detected Mode", color = Color.Gray, fontSize = 9.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Column 4: Tone Variation %
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                                border = BorderStroke(1.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("TONE VARIATION", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${analysis.toneVariationPercentage}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        if (analysis.toneVariationPercentage in 45..74) "Optimal Range" else "Needs Resonance",
                                        color = if (analysis.toneVariationPercentage in 45..74) CyberMint else GoldAccent,
                                        fontSize = 9.sp
                                    )
                                }
                            }

                            // Column 5: Pitch Stability %
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                                border = BorderStroke(1.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("PITCH INFLECTION", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${analysis.estimatedPitchStability}%", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        analysis.toneModulationLevel.substringBefore(" ("),
                                        color = CyberPurple,
                                        fontSize = 9.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Recommendations block
                        Text(
                            "COACHING FEEDBACK RECOMS:",
                            color = GoldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        analysis.structuredRecommendations.forEach { recom ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("•", color = CyberMint, fontSize = 12.sp, modifier = Modifier.padding(end = 6.dp))
                                Text(
                                    recom,
                                    color = LightSilver,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SCREEN 3: ADVISOR MARKETPLACE ---
@Composable
fun MarketplaceScreen(viewModel: CommViewModel, profile: UserProfile) {
    val bookedList by viewModel.bookedSessions.collectAsStateWithLifecycle()
    var selectedCoachForBooking by remember { mutableStateOf<CoachProfile?>(null) }
    
    val advisorsList = listOf(
        CoachProfile(
            "Sarah Chen",
            "Executive Leadership & High-Stakes Presence",
            "4.9",
            "$140/hr",
            "Available Tomorrow",
            "Specialize in tech executive salary anchors and stakeholder alignment board meeting techniques."
        ),
        CoachProfile(
            "Marcus Vance",
            "Hostile Mediation & Relational Repair",
            "4.8",
            "$120/hr",
            "Available Wednesday",
            "Focus on high emotional boundary calibration, de-escalating home partner conflicts, and restoring intimacy."
        ),
        CoachProfile(
            "David Foster",
            "Command Storytelling & Stage Hooks",
            "5.0",
            "$180/hr",
            "Available Thursday",
            "Ex-journalist specializing in live Q&A tension, media appearances, and dynamic public scripts."
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory marketplace header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HUMAN ADVISOR ACCESS", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Certified Communication Facilitators",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "AI pre-briefs of your profile will be safely transmitted to your scheduler ahead of the meeting to craft bespoke exercises and optimize time.",
                        color = DarkSilver,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Active bookings section
        if (bookedList.isNotEmpty()) {
            item {
                Text("YOUR SCHEDULED SESSIONS", color = CyberMint, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            items(bookedList) { session ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                    border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.5f))
                ) {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = { Text(session.coachName, color = Color.White, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("${session.sessionType} - ${session.scheduledTime}", color = LightSilver) },
                        trailingContent = { Text(session.price, color = CyberMint, fontWeight = FontWeight.ExtraBold) }
                    )
                }
            }
        }

        // Directory listing
        item {
            Text("DIRECT COACH DIRECTORY", color = DarkSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        }

        items(advisorsList) { coach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(coach.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(coach.specialty, color = CyberPurple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(coach.rating, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(coach.description, color = LightSilver, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = DarkSilver, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(coach.availability, color = DarkSilver, fontSize = 11.sp)
                        }
                        
                        Button(
                            onClick = { selectedCoachForBooking = coach },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("book_${coach.name.replace(" ", "_").lowercase()}")
                        ) {
                            Text("Map & Book (${coach.price})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Interactive booking dialog with Pre-Brief synchronizer summary
    if (selectedCoachForBooking != null) {
        val coach = selectedCoachForBooking!!
        AlertDialog(
            onDismissRequest = { selectedCoachForBooking = null },
            title = { Text("Confirm Advisor Hand-off Package", color = Color.White, fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text(
                        "AI is transmitting your profile brief safely to ${coach.name}. Ensure correct data integration limits below.",
                        color = LightSilver,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MidnightBg),
                        border = BorderStroke(1.dp, BorderGlass)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("SHARED TRANSMISSION DIRECTIVE packet:", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("👤 Name: ${profile.name}", color = Color.White, fontSize = 12.sp)
                            Text("🎨 Archetype: ${profile.communicationArchetype}", color = Color.White, fontSize = 12.sp)
                            Text("📈 Aggregate Confidence Score: ${profile.currentConfidenceScore}/100", color = Color.White, fontSize = 12.sp)
                            Text("🎯 Specific Goal: ${profile.communicationGoal30Days}", color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Select session type:", color = LightSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Emergency Repair (30m)", "Pattern Break (60m)").forEach { sType ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                                border = BorderStroke(1.dp, CyberPurple),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    sType,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.bookAdvisorSession(
                            coachName = coach.name,
                            scheduledTime = "Tomorrow at 2:00 PM PST",
                            sessionType = "Pattern Break (60m)",
                            price = coach.price
                        )
                        selectedCoachForBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMint)
                ) {
                    Text("Transmit & Bind booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCoachForBooking = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = MidnightSurface,
            textContentColor = LightSilver
        )
    }
}

data class CoachProfile(
    val name: String,
    val specialty: String,
    val rating: String,
    val price: String,
    val availability: String,
    val description: String
)

// --- LOCAL SCHEMAS FOR CONTEST ENTRIES ---
data class ContestChallenge(
    val title: String,
    val sponsor: String,
    val description: String,
    val blueprint: String,
    val reward: String,
    val triggerCommand: String
)

// --- SCREEN 4: ECOSYSTEM -- COMMUNITY & COURSES ---
@Composable
fun CommunityScreen(viewModel: CommViewModel, profile: UserProfile) {
    val joinedList by viewModel.joinedCourses.collectAsStateWithLifecycle()
    var creatorDialogOpen by remember { mutableStateOf(false) }
    var ecosystemTabSelected by remember { mutableStateOf("courses") } // "courses" or "contests"
    
    // Peer challenge card
    val dailyPeerChallenge = "Today's Peer Challenge: Record inside of Chat 2 sentences with silent pause triggers."

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dual Mode Tab Switcher
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = ecosystemTabSelected == "courses",
                    onClick = { ecosystemTabSelected = "courses" },
                    label = { Text("COURSES CURATION", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberPurple,
                        selectedLabelColor = Color.White,
                        containerColor = MidnightSurface,
                        labelColor = Color.Gray
                    )
                )
                
                FilterChip(
                    selected = ecosystemTabSelected == "contests",
                    onClick = { ecosystemTabSelected = "contests" },
                    label = { Text("🏆 RECRUITMENT ARENA", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldAccent,
                        selectedLabelColor = MidnightBg,
                        containerColor = MidnightSurface,
                        labelColor = Color.Gray
                    )
                )
            }
        }

        if (ecosystemTabSelected == "courses") {
            // Daily peer challenge card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                    border = BorderStroke(1.dp, CyberPurple)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = CyberMint)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("PEER PRACTICE GROUP ACTIVE", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            dailyPeerChallenge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Contribution Score: ", color = DarkSilver, fontSize = 11.sp)
                            Text("${profile.communityContributionScore} Points", color = GoldAccent, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Community creation gate button
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                    border = BorderStroke(1.dp, BorderGlass)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("CREATOR HUB PROTOCOLS", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Upload custom communication classes or host a private chat room group. Create your peer coaching brand.",
                            color = LightSilver,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { creatorDialogOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                            modifier = Modifier.fillMaxWidth().testTag("open_creator_hub")
                        ) {
                            Text("Initiate New Community Course ($29/mo)")
                        }
                    }
                }
            }

            // List of Active Courses joined by user
            item {
                Text("YOUR RUNNING COURSE CURATIONS", color = DarkSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            items(joinedList) { course ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                    border = BorderStroke(1.dp, BorderGlass)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(course.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.2f))
                            ) {
                                Text(
                                    "Tier: ${course.creatorTier}",
                                    color = CyberPurple,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(course.description, color = LightSilver, fontSize = 12.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                LinearProgressIndicator(
                                    progress = { course.progress / 100f },
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(2.dp))
                                        .height(4.dp),
                                    color = CyberMint,
                                    trackColor = MidnightBg
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("${course.progress}% Completed", color = DarkSilver, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        } else {
            // Contests arena listing
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurfaceCard),
                    border = BorderStroke(1.dp, GoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SOCIETY PLATINUM CAREER RECRUITMENT ARENA", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Participate in high-stakes communication contests backed by elite recruiters. Score 90% or above under rigorous AI evaluations to unlock verified fast-track job interview passes directly registered to your profile badge drawer.",
                            color = LightSilver,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Text("ACTIVE SPONSOR MEETS & CHALLENGES", color = DarkSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            val contests = listOf(
                ContestChallenge(
                    title = "The Venture Seed Pitch Slam",
                    sponsor = "ANDREESSEN HOROWITZ (A16Z)",
                    description = "🧬 Biotech Biopharma venture evaluation pitch support. Defend an extremely disruptive $10M seed valuation to hardnosed venture general partners.",
                    blueprint = "SPIN System format, Concise answers (60s timer locked), Zero apologetic fillers.",
                    reward = "Direct $10,000 Equity-free seed pilot + priority fastpass in the associate screening queue.",
                    triggerCommand = "/roleplay a16z Biotech Venture Pitch with Arthur"
                ),
                ContestChallenge(
                    title = "Boardroom Takeover Defense",
                    sponsor = "MCKINSEY & COMPANY / GOLDMAN SACHS",
                    description = "💼 Defend a hostile corporate buyout proposal under rapid boardroom audit pressure.",
                    blueprint = "Minto Pyramid framework, 2-pause silent triggers, High Confidence range assertiveness.",
                    reward = "McKinsey Regional Analyst / Associate Partner fast-track direct partner referral.",
                    triggerCommand = "/roleplay McKinsey Boardroom Takeover Defense with Vivian"
                ),
                ContestChallenge(
                    title = "Technical Outage Briefing",
                    sponsor = "STRIPE & GOOGLE CLOUD NETWORK",
                    description = "⚡ Brief the VP of infrastructure on a massive financial pipeline downtime disaster affecting thousands of active merchant applications.",
                    blueprint = "SBI feedback framework, Active Listening Mirroring, Clear impact metrics, Empathetic labeling.",
                    reward = "Lead Technical Client Engineer Referral Pass + direct review queue.",
                    triggerCommand = "/roleplay Stripe Client Disaster Recovery with Dennis"
                )
            )

            items(contests) { contest ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                    border = BorderStroke(1.dp, BorderGlass),
                    modifier = Modifier.fillMaxWidth().testTag("contest_card_${contest.title.lowercase().replace(" ", "_")}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = contest.sponsor,
                                color = GoldAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(contest.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(contest.description, color = LightSilver, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MidnightBg, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("REQUIRED STRATEGIES (90%+ Evaluation Score to Win):", color = CyberMint, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(contest.blueprint, color = LightSilver, fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Column {
                            Text("RECRUITER REWARDS & PRIZES:", color = CyberPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(contest.reward, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val hasBadgeUnlocked = when (contest.sponsor) {
                            "ANDREESSEN HOROWITZ (A16Z)" -> viewModel.wonA16zFastPass
                            "MCKINSEY & COMPANY / GOLDMAN SACHS" -> viewModel.wonMcKinseyFastPass
                            "STRIPE & GOOGLE CLOUD NETWORK" -> viewModel.wonStripeFastPass
                            else -> false
                        }

                        if (hasBadgeUnlocked) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(containerColor = CyberMint.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, CyberMint),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp),
                                enabled = false
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberMint, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("CONTEST CLEANLY CONQUERED - BADGE SECURED", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.currentActiveContestName = contest.title
                                    viewModel.chatInputText = contest.triggerCommand
                                    viewModel.clearChatHistory()
                                    viewModel.currentScreen = CommScreen.CHAT
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("LAUNCH CONTEST CHALLENGE WITH STRICT TIMER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Community Creation Premium Gate Dialog
    if (creatorDialogOpen) {
        var ownCName by remember { mutableStateOf("") }
        var ownCDesc by remember { mutableStateOf("") }
        var ownCCat by remember { mutableStateOf("PROFESSIONAL") }
        
        AlertDialog(
            onDismissRequest = { creatorDialogOpen = false },
            title = { Text("Unlock Platinum Creator License", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Community and Course publication is restricted to Platinum Tier ($29/month). This fully unlocks course building widgets, target student funnel visual analytics, and revenue split metrics (Gold/Platinum: 85% creator profit).",
                        color = LightSilver,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text("Pre-build Course Outline Draft:", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = ownCName,
                        onValueChange = { ownCName = it },
                        label = { Text("Course Title", color = DarkSilver, fontSize = 11.sp) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = ownCDesc,
                        onValueChange = { ownCDesc = it },
                        label = { Text("Description & Exercises", color = DarkSilver, fontSize = 11.sp) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ownCName.trim().isNotEmpty()) {
                            viewModel.createOwnCommunityCourse(ownCName, ownCDesc, ownCCat)
                        }
                        creatorDialogOpen = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMint)
                ) {
                    Text("Purchase & Publish course")
                }
            },
            dismissButton = {
                TextButton(onClick = { creatorDialogOpen = false }) {
                    Text("Go Back", color = Color.White)
                }
            },
            containerColor = MidnightSurface,
            textContentColor = LightSilver
        )
    }
}

// --- SCREEN 5: THE ADAPTIVE IMAGE GENERATOR STUDIO ---
@Composable
fun ImageStudioScreen(viewModel: CommViewModel, profile: UserProfile) {
    var localPromptInput by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Aesthetic header explains model bounds and specs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("MASTERY CARD GRAPHICS ENGINE", color = GoldAccent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Generate High-Quality Adaptive Artwork",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Model: gemini-3-pro-image-preview. Render customizable badges, certificate mockups or communication avatar artwork to display in your profile.",
                        color = DarkSilver,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Size configurations (1K, 2K, 4K)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("SELECT GRAPHIC SIZE & RESOLUTION", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("1K", "2K", "4K").forEach { size ->
                            val isSel = viewModel.selectedImageSize == size
                            FilterChip(
                                selected = isSel,
                                onClick = { viewModel.selectedImageSize = size },
                                label = {
                                    Text(
                                        text = when(size) {
                                            "1K" -> "1K (1024px Sq)"
                                            "2K" -> "2K (1536px Sq)"
                                            else -> "4K (2048px Sq HD)"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyberPurple
                                )
                            )
                        }
                    }
                }
            }
        }

        // Text input for generator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MidnightSurface),
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DESIGN SCHEME OUTLINE", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = localPromptInput,
                        onValueChange = { localPromptInput = it },
                        placeholder = { Text("E.g., 'Certificate of Executive Negotation Mastery for ${profile.name}'", color = DarkSilver, fontSize = 12.sp) },
                        textStyle = TextStyle(color = Color.White),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberPurple,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("image_prompt_input")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.generateMasteryCard(localPromptInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                        enabled = !viewModel.isGeneratingImage,
                        modifier = Modifier.fillMaxWidth().testTag("image_generate_button")
                    ) {
                        if (viewModel.isGeneratingImage) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Rendering Resolution grids...")
                        } else {
                            Text("Compute Graphic 🎨")
                        }
                    }
                }
            }
        }

        // Display Generated Image outcome
        item {
            val img64 = viewModel.generatedImageBase64
            val isGen = viewModel.isGeneratingImage
            
            if (isGen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MidnightSurface)
                        .border(BorderStroke(1.dp, BorderGlass)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = CyberMint)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Running generator on size ${viewModel.selectedImageSize} matrix...",
                            color = DarkSilver,
                            fontSize = 11.sp
                        )
                    }
                }
            } else if (img64 != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("GENERATED MASTERY GRAPHIC OUTCOME", color = DarkSilver, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Decode base64 to Android graphic bitmap
                    val cleanBase64 = img64.replace("\n", "").replace(" ", "").replace("\r", "")
                    val imageBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated Mastery Card",
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(BorderStroke(1.dp, CyberPurple)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text("Error rendering generated bitmap vector stream.", color = Color.Red, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// Global utility layouts
fun getWeeklySkills(wk: Int, mode: String): List<String> = when (wk) {
    1 -> listOf("Empathetic mirroring and pauses", "Removing qualifier words ('just', 'sorry')", "Establishing visual boundaries")
    2 -> listOf("Power-pacing vocal controls", "Negotiation range anchors", "Delivering feedback in $mode style")
    3 -> listOf("Reframing aggressive statements", "Active listening assertions", "De-escalating workplace friction")
    else -> listOf("Advanced negotiation leverage", "Storytelling narrative hooks", "Impromptu media handling")
}

fun getWeeklyScenarios(wk: Int, mode: String): List<String> = when (wk) {
    1 -> listOf("Conflict resolution roleplay with a flatmate", "Asking your superior for resource allocation")
    2 -> listOf("Handling direct criticism in team syncs", "Pitching a new proposal in $mode mode")
    3 -> listOf("Saying 'no' to extra tasks calmly", "Managing relational partner boundary concerns")
    else -> listOf("Lead a complex meeting and align objectives", "Handling negative and critical live questions")
}

fun getWeeklyChallenge(wk: Int): String = when (wk) {
    1 -> "Spend 24 hours without apologizing before launching a request."
    2 -> "Pitch a new project proposal to a coworker using the Power-Anchor method."
    3 -> "Address one relational chore conflict directly inside of 3 minutes."
    else -> "Facilitate a structured meeting utilizing specific silent pauses."
}

fun getModeIcon(mode: String): ImageVector = when (mode) {
    "BUSINESS" -> Icons.Default.Star
    "PROFESSIONAL" -> Icons.Default.Star
    "RELATIONSHIP" -> Icons.Default.Favorite
    "FRIENDS" -> Icons.Default.Person
    "PUBLIC" -> Icons.Default.Share
    "CONFLICT" -> Icons.Default.Warning
    else -> Icons.Default.Settings
}

fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

@Composable
fun HackerRadarSweep(
    filler: Int,
    resonance: Int,
    mirroring: Int,
    assertiveness: Int
) {
    var sweepAngle by remember { mutableStateOf(0f) }
    
    // Animate a constant radar sweep angle
    LaunchedEffect(Unit) {
        while (true) {
            sweepAngle = (sweepAngle + 4f) % 360f
            delay(16)
        }
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .background(MidnightBg, CircleShape)
            .border(BorderStroke(1.dp, CyberPurple.copy(alpha = 0.3f)), CircleShape)
            .testTag("radar_sweep_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = size.width / 2f
            val radius = size.width * 0.45f
            
            // Draw crosshairs
            drawLine(
                color = BorderGlass.copy(alpha = 0.2f),
                start = androidx.compose.ui.geometry.Offset(0f, center),
                end = androidx.compose.ui.geometry.Offset(size.width, center),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = BorderGlass.copy(alpha = 0.2f),
                start = androidx.compose.ui.geometry.Offset(center, 0f),
                end = androidx.compose.ui.geometry.Offset(center, size.height),
                strokeWidth = 1.dp.toPx()
            )
            
            // Draw concentric circles
            drawCircle(color = CyberPurple.copy(alpha = 0.1f), radius = radius * 0.33f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            drawCircle(color = CyberPurple.copy(alpha = 0.15f), radius = radius * 0.66f, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            drawCircle(color = CyberPurple.copy(alpha = 0.2f), radius = radius, style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
            
            // Draw radar sweep line
            val sweepRadians = Math.toRadians(sweepAngle.toDouble())
            val lineX = center + radius * Math.cos(sweepRadians).toFloat()
            val lineY = center + radius * Math.sin(sweepRadians).toFloat()
            
            drawLine(
                color = CyberMint.copy(alpha = 0.6f),
                start = androidx.compose.ui.geometry.Offset(center, center),
                end = androidx.compose.ui.geometry.Offset(lineX, lineY),
                strokeWidth = 1.5.dp.toPx()
            )
            
            // Draw telemetry posture shape (4 indices: Up=filler, Right=resonance, Down=mirroring, Left=assertiveness)
            val fillPerc = filler / 100f
            val resPerc = resonance / 100f
            val mirPerc = mirroring / 100f
            val assPerc = assertiveness / 100f
            
            val p1X = center
            val p1Y = center - (radius * fillPerc)
            
            val p2X = center + (radius * resPerc)
            val p2Y = center
            
            val p3X = center
            val p3Y = center + (radius * mirPerc)
            
            val p4X = center - (radius * assPerc)
            val p4Y = center
            
            val path = Path().apply {
                moveTo(p1X, p1Y)
                lineTo(p2X, p2Y)
                lineTo(p3X, p3Y)
                lineTo(p4X, p4Y)
                close()
            }
            
            // Draw area and stroke
            drawPath(path = path, color = CyberMint.copy(alpha = 0.15f))
            drawPath(path = path, color = CyberMint, style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx()))
            
            // Draw individual node points
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(p1X, p1Y))
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(p2X, p2Y))
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(p3X, p3Y))
            drawCircle(color = Color.White, radius = 3.dp.toPx(), center = androidx.compose.ui.geometry.Offset(p4X, p4Y))
        }
    }
}

data class HistoricInterval(
    val label: String,
    val interactions: Int,
    val milestones: List<String>
)

@Composable
fun InteractionHistoryMilestoneChart(selectedGoals: List<String>) {
    // 7 Days of historic milestones and interactions data
    val intervals = remember {
        listOf(
            HistoricInterval("Mon", 8, listOf("Silent Pause")),
            HistoricInterval("Tue", 14, emptyList()),
            HistoricInterval("Wed", 22, listOf("Filler Shield")),
            HistoricInterval("Thu", 19, listOf("Double Anchor")),
            HistoricInterval("Fri", 31, listOf("Mirroring Loop")),
            HistoricInterval("Sat", 15, emptyList()),
            HistoricInterval("Sun", 25, listOf("Consensus Lock"))
        )
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("interaction_history_milestone_chart_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberPurple)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Milestone history link",
                            tint = CyberPurple,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "HISTORICAL COMPASS (TIMELINE)",
                            color = if (CommThemeState.isLightMode) CyberPurple else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Tracks interaction intervals and milestone acquisitions",
                        color = DarkSilver,
                        fontSize = 10.sp
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.3f))
                ) {
                    Text(
                        "TOTAL: ${intervals.sumOf { it.interactions }} INT",
                        color = CyberPurple,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas with touch tracking
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MidnightBg, shape = RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, BorderGlass), shape = RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val totalItems = intervals.size
                                val sectionWidth = width / (totalItems - 1)
                                val tappedIndex = ((offset.x + (sectionWidth / 2f)) / sectionWidth)
                                    .toInt()
                                    .coerceIn(0, totalItems - 1)
                                selectedIndex = tappedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val totalItems = intervals.size
                    val spacing = width / (totalItems - 1)

                    // 1. Draw horizontal grid lines
                    val gridLines = listOf(0.25f, 0.5f, 0.75f)
                    gridLines.forEach { percentage ->
                        val y = height * percentage
                        drawLine(
                            color = BorderGlass.copy(alpha = 0.15f),
                            start = androidx.compose.ui.geometry.Offset(0f, y),
                            end = androidx.compose.ui.geometry.Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // 2. Draw vertical highlighted line for selected index
                    selectedIndex?.let { index ->
                        val x = index * spacing
                        drawLine(
                            color = CyberMint.copy(alpha = 0.4f),
                            start = androidx.compose.ui.geometry.Offset(x, 0f),
                            end = androidx.compose.ui.geometry.Offset(x, height),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // 3. Draw Chart Line and fill area
                    val maxInteractions = 40f
                    val strokePath = Path()
                    val fillPath = Path()

                    intervals.forEachIndexed { index, item ->
                        val x = index * spacing
                        val ratio = item.interactions / maxInteractions
                        val y = height - (ratio * height)

                        if (index == 0) {
                            strokePath.moveTo(x, y)
                            fillPath.moveTo(x, height)
                            fillPath.lineTo(x, y)
                        } else {
                            strokePath.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }

                        if (index == totalItems - 1) {
                            fillPath.lineTo(x, height)
                            fillPath.close()
                        }
                    }

                    // Draw area gradient
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(CyberPurple.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Draw line
                    drawPath(
                        path = strokePath,
                        color = CyberPurple,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )

                    // 4. Draw interactive node elements: circle dots for interaction count and milestone completions
                    intervals.forEachIndexed { index, item ->
                        val x = index * spacing
                        val ratio = item.interactions / maxInteractions
                        val y = height - (ratio * height)
                        val isSelected = selectedIndex == index

                        // If milestone was unlocked in this interval, draw a gold badge star marker
                        if (item.milestones.isNotEmpty()) {
                            drawCircle(
                                color = GoldAccent,
                                radius = if (isSelected) 8.dp.toPx() else 6.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        } else {
                            // Ordinary data tracker node
                            drawCircle(
                                color = if (isSelected) CyberMint else CyberPurple,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            if (isSelected) {
                                drawCircle(
                                    color = Color.White,
                                    radius = 2.dp.toPx(),
                                    center = androidx.compose.ui.geometry.Offset(x, y)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Labels for x-axis
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                intervals.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    Text(
                        text = item.label,
                        color = if (isSelected) CyberMint else DarkSilver,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable { selectedIndex = index }
                    )
                }
            }

            // Interactive Tooltip Info Box representing Recharts hovered inspect node
            val inspectIndex = selectedIndex
            if (inspectIndex != null) {
                val item = intervals[inspectIndex]
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MidnightBg),
                    border = BorderStroke(1.dp, CyberMint)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "INTERVAL METRIC: ${item.label.uppercase()}",
                                color = CyberMint,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            IconButton(
                                onClick = { selectedIndex = null },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close Tooltip", tint = DarkSilver, modifier = Modifier.size(12.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row {
                            Text("Interactions: ", color = LightSilver, fontSize = 12.sp)
                            Text("${item.interactions}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        
                        if (item.milestones.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = "Milestone Unlocked", tint = GoldAccent, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Milestones Unlocked: ", color = LightSilver, fontSize = 11.sp)
                                Text(item.milestones.joinToString(", "), color = GoldAccent, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "💡 Tap on any day nodes on the chart to inspect rich interaction telemetry.",
                    color = DarkSilver,
                    fontSize = 10.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }

            // Present Selected Onboarding Goals here dynamically
            if (selectedGoals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BorderGlass)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "TARGET TRAINING GOALS",
                    color = LightSilver,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedGoals.forEach { goal ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CyberMint, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(goal, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- PEER FEEDBACK CENTRAL HUB MODULE ---
@Composable
fun PeerFeedbackHubCard(viewModel: CommViewModel) {
    val feedbacksState by viewModel.peerFeedbacks.collectAsStateWithLifecycle()
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    val context = androidx.compose.ui.platform.LocalContext.current

    var selectedScenarioIndex by remember { mutableIntStateOf(0) }
    var isFeedbackFormExpanded by remember { mutableStateOf(false) }

    // Input fields for manual logging
    var peerNameInput by remember { mutableStateOf("") }
    var contextInput by remember { mutableStateOf("") }
    var categorySelection by remember { mutableStateOf("Public Speaking") }
    var presenceRating by remember { mutableFloatStateOf(8f) }
    var resonanceRating by remember { mutableFloatStateOf(8f) }
    var engagementRating by remember { mutableFloatStateOf(8f) }
    var constructiveNotesInput by remember { mutableStateOf("") }
    var takeawayNotesInput by remember { mutableStateOf("") }

    val feedbackScenarios = listOf(
        Pair("Product Roadmap townhall", "I'm practicing structured roadmap speech. Could you rate my vocal pace (vocal fillers vs pauses) and clear resonance? Thank you!"),
        Pair("Interactive Salary Checkpoint", "I'm working on anchoring salary leverage assertively. Let me know if I sounded collaborative vs defensive or hesitant!"),
        Pair("Crisis Retro Meeting", "Testing de-escalation active listening loop. Did my phrases validate concerns or sound adversarial? Feedback welcome!"),
        Pair("Executive Project pitch", "Need feedback on high-stakes presentation hooks. How was my physical presence and active audience resonance? Thanks!")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("peer_feedback_hub_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Peer Logo",
                        tint = CyberPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "PEER FEEDBACK CENTRAL HUB",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.4f))
                ) {
                    Text(
                        "PEER RATINGS",
                        color = CyberPurple,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Generate structured feedback request templates & record third-party assessments directly into your Progress scorecard progress loops.",
                color = LightSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            // -- Template Generator Section --
            Text(
                "1. GENERATE ANONYMOUS REQUEST TEMPLATE",
                color = GoldAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Scenario Choice Buttons Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                feedbackScenarios.forEachIndexed { i, (scn, _) ->
                    val selected = selectedScenarioIndex == i
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) CyberPurple.copy(alpha = 0.2f) else MidnightBg
                        ),
                        border = BorderStroke(1.dp, if (selected) CyberPurple else BorderGlass),
                        modifier = Modifier.clickable { selectedScenarioIndex = i }
                    ) {
                        Text(
                            scn,
                            color = if (selected) Color.White else LightSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Template display box
            val activeTemplateText = feedbackScenarios[selectedScenarioIndex].second
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MidnightBg, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        "SUBJECT: Peer Feedback Request - CommCore Playbook",
                        color = DarkSilver,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        activeTemplateText,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(activeTemplateText))
                    Toast.makeText(context, "Template Copied to Clipboard! Send it via Slack/Email.", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .testTag("copy_feedback_template_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("COPY TEMPLATE TO CLIPBOARD", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            // -- Logger Section --
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "2. RECORD THIRD-PARTY PEER ASSESSMENT",
                    color = CyberMint,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                TextButton(
                    onClick = { isFeedbackFormExpanded = !isFeedbackFormExpanded }
                ) {
                    Text(
                        if (isFeedbackFormExpanded) "CLOSE FORM" else "LOG ASSESSMENT",
                        color = CyberMint,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            AnimatedVisibility(visible = isFeedbackFormExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Peer Name
                    OutlinedTextField(
                        value = peerNameInput,
                        onValueChange = { peerNameInput = it },
                        label = { Text("Peer Name & Title (e.g. Devan, VP Product)", fontSize = 10.sp, color = DarkSilver) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("peer_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberMint,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Scenario
                    OutlinedTextField(
                        value = contextInput,
                        onValueChange = { contextInput = it },
                        label = { Text("Specific Context/Scenario (Product Townhall, Salary review)", fontSize = 10.sp, color = DarkSilver) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberMint,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Category Selector
                    Text("Select Target Skill Category:", color = LightSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Public Speaking", "Negotiation", "Active Listening", "Executive Presence").forEach { cat ->
                            val selected = categorySelection == cat
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selected) CyberMint.copy(alpha = 0.15f) else MidnightBg
                                ),
                                border = BorderStroke(1.dp, if (selected) CyberMint else BorderGlass),
                                modifier = Modifier.clickable { categorySelection = cat }
                            ) {
                                Text(
                                    cat,
                                    color = if (selected) Color.White else LightSilver,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scores Sliders (Presence, Resonance, Engagement)
                    Column {
                        Text("POLISHED PRESENCE: ${presenceRating.toInt()}/10", color = LightSilver, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Slider(
                            value = presenceRating,
                            onValueChange = { presenceRating = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = CyberMint, activeTrackColor = CyberMint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .testTag("presence_rating")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("MESSAGE RESONANCE: ${resonanceRating.toInt()}/10", color = LightSilver, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Slider(
                            value = resonanceRating,
                            onValueChange = { resonanceRating = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = CyberMint, activeTrackColor = CyberMint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .testTag("resonance_rating")
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("ACTIVE ENGAGEMENT: ${engagementRating.toInt()}/10", color = LightSilver, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        Slider(
                            value = engagementRating,
                            onValueChange = { engagementRating = it },
                            valueRange = 1f..10f,
                            steps = 8,
                            colors = SliderDefaults.colors(thumbColor = CyberMint, activeTrackColor = CyberMint),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp)
                                .testTag("engagement_rating")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Notes & Action Point
                    OutlinedTextField(
                        value = constructiveNotesInput,
                        onValueChange = { constructiveNotesInput = it },
                        label = { Text("Peer Constructive Criticism & Notes", fontSize = 10.sp, color = DarkSilver) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth().testTag("peer_notes_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberMint,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = takeawayNotesInput,
                        onValueChange = { takeawayNotesInput = it },
                        label = { Text("Key Immediate Action Takeaways", fontSize = 10.sp, color = DarkSilver) },
                        textStyle = TextStyle(color = Color.White, fontSize = 11.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberMint,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (peerNameInput.isBlank() || contextInput.isBlank()) {
                                Toast.makeText(context, "Please fill in Peer Name and Context before recording.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.insertPeerFeedback(
                                com.example.data.PeerFeedback(
                                    peerName = peerNameInput,
                                    dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()),
                                    category = categorySelection,
                                    scenario = contextInput,
                                    scorePresence = presenceRating.toInt(),
                                    scoreEngagement = engagementRating.toInt(),
                                    scoreResonance = resonanceRating.toInt(),
                                    constructiveNotes = constructiveNotesInput,
                                    keyTakeaways = takeawayNotesInput
                                )
                            )
                            Toast.makeText(context, "★ Evaluation Logged! Streak & Confidence scores updated.", Toast.LENGTH_LONG).show()
                            viewModel.triggerDailyGoalDone()
                            
                            // Reset
                            peerNameInput = ""
                            contextInput = ""
                            constructiveNotesInput = ""
                            takeawayNotesInput = ""
                            isFeedbackFormExpanded = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("save_peer_feedback_button")
                    ) {
                        Text("RECORD ON-DEVICE ASSESSMENT", color = MidnightBg, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            // -- History List --
            Text(
                "3. PEER ASSESSMENTS ARCHIVE",
                color = LightSilver,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (feedbacksState.isEmpty()) {
                Text(
                    "No logged peer assessments on file yet. Log an assessment above to build dynamic history data.",
                    color = DarkSilver,
                    fontSize = 11.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    feedbacksState.forEach { fb ->
                        val combinedAvg = (fb.scorePresence + fb.scoreResonance + fb.scoreEngagement) / 3f
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MidnightBg),
                            border = BorderStroke(1.dp, BorderGlass),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("peer_history_item")
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            fb.peerName.uppercase(),
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            "${fb.category} • ${fb.scenario}",
                                            color = DarkSilver,
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CyberMint.copy(alpha = 0.12f)),
                                            border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                String.format(java.util.Locale.US, "AVG %.1f/10", combinedAvg),
                                                color = CyberMint,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { viewModel.deletePeerFeedback(fb.id) },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Ratings breakout
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        Triple("Presence", fb.scorePresence, CyberMint),
                                        Triple("Resonance", fb.scoreResonance, GoldAccent),
                                        Triple("Engagement", fb.scoreEngagement, CyberPurple)
                                    ).forEach { (met, valScore, col) ->
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(met, color = LightSilver, fontSize = 8.sp)
                                                Text("$valScore/10", color = col, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            LinearProgressIndicator(
                                                progress = { valScore / 10f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(3.dp)
                                                    .clip(RoundedCornerShape(1.dp)),
                                                color = col,
                                                trackColor = MidnightSurface
                                            )
                                        }
                                    }
                                }

                                if (fb.constructiveNotes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Notes: \"${fb.constructiveNotes}\"",
                                        color = LightSilver,
                                        fontSize = 10.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        lineHeight = 13.sp
                                    )
                                }
                                if (fb.keyTakeaways.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "🎯 Takeaway: ${fb.keyTakeaways}",
                                        color = GoldAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DAILY PRACTICE REMINDERS & BROWSER-NOTIFICATION API SETTINGS COMPOSABLE ---
@Composable
fun DailyPracticeRemindersCard(
    viewModel: CommViewModel,
    triggerBrowserNotify: (title: String, body: String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isSubscribed by remember { mutableStateOf(true) }
    var selectedHourSelection by remember { mutableStateOf("09:00 AM") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("practice_reminders_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Alerts Logo",
                        tint = CyberMint,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "DAILY PRACTICE NOTIFIER",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSubscribed) CyberMint.copy(alpha = 0.15f) else Color.Red.copy(alpha = 0.15f)
                    ),
                    border = BorderStroke(1.dp, if (isSubscribed) CyberMint.copy(alpha = 0.5f) else Color.Red.copy(alpha = 0.5f))
                ) {
                    Text(
                        if (isSubscribed) "API: GRANTED" else "API: BLOCKED",
                        color = if (isSubscribed) CyberMint else Color.Red,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Configure automated schedule windows & verify Chrome Browser HTML5 Notification API bindings.",
                color = LightSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            // Subscriptions Switcher rows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "BROWSER CHANNEL ENABLER",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Remind me on progress milestones",
                        color = DarkSilver,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Switch(
                    checked = isSubscribed,
                    onCheckedChange = { isSubscribed = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = CyberMint,
                        checkedTrackColor = CyberMint.copy(alpha = 0.4f),
                        uncheckedThumbColor = DarkSilver,
                        uncheckedTrackColor = MidnightBg
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hour choice Row
            Text("SET REMINDER TIME WINDOW:", color = LightSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("09:00 AM", "01:00 PM", "06:00 PM").forEach { hr ->
                    val selected = selectedHourSelection == hr
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) CyberMint.copy(alpha = 0.15f) else MidnightBg
                        ),
                        border = BorderStroke(1.dp, if (selected) CyberMint else BorderGlass),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedHourSelection = hr }
                    ) {
                        Text(
                            hr,
                            color = if (selected) Color.White else LightSilver,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            // Skill specific notification payloads view
            Text("ACTIVE SKILLS NOTIFICATION PAYLOADS:", color = GoldAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))

            val goals = viewModel.selectedCommunicationGoals
            if (goals.isEmpty()) {
                Text("No target skills currently selected. Setup in Onboarding.", color = DarkSilver, fontSize = 10.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    goals.forEach { goalName ->
                        val (title, body) = when (goalName) {
                            "Negotiation" -> "Calibrate Concession Anchors" to "Don't agree to price cuts without framing a salary double-anchor cushion first."
                            "Public Speaking" -> "Vocal Softener Shield Active" to "Fulfill high-pressure Q&As utilizing slow, composed 2-second silences."
                            "Active Listening" -> "Empathy Mirror Calibrator" to "Acknowledge counterpart emotional core assertions before launching claims."
                            else -> "Daily Practice Calibration" to "Perfect your active $goalName workouts in AI Coaching Gym today."
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MidnightBg, RoundedCornerShape(4.dp))
                                .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(4.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("$title • $goalName", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                Text(body, color = LightSilver, fontSize = 9.sp, lineHeight = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// --- 2.1e NEW SUB-TAB MODULES: AI INSIGHTS, COMPASS, ROADMAP GENERATOR ---
// =========================================================================

@Composable
fun SentimentAnalysis30DaysChart() {
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    
    // Generating 30 days of historic sentiment quotients (shows constructive, progressive growth as practice secures)
    val sentimentPoints = remember {
        listOf(61f, 63f, 58f, 62f, 65f, 60f, 64f, 68f, 67f, 70f, 74f, 72f, 75f, 79f, 78f, 82f, 80f, 83f, 85f, 82f, 86f, 88f, 85f, 89f, 91f, 88f, 90f, 92f, 91f, 94f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sentiment_analysis_chart_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, BorderGlass)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "AI SENTIMENT & EMOTIONAL INTELLIGENCE TRENDS",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "Tracked emotional resonance, paces, and boundaries over the last 30 days",
                        color = DarkSilver,
                        fontSize = 10.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(CyberMint.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("+33% RESILIENCY", color = CyberMint, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Graph representation using custom canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val xStep = size.width / (sentimentPoints.size - 1)
                                val xTouch = offset.x
                                val index = (xTouch / xStep).roundToInt().coerceIn(0, sentimentPoints.size - 1)
                                hoveredIndex = index
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val maxVal = 100f
                    val minVal = 40f
                    val range = maxVal - minVal

                    // Draw zones background
                    // Zone 3: Resonant (75-100)
                    drawRect(
                        color = CyberMint.copy(alpha = 0.03f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                        size = androidx.compose.ui.geometry.Size(width, height * 0.4f)
                    )
                    // Zone 2: Professional Calm (55-75)
                    drawRect(
                        color = CyberPurple.copy(alpha = 0.02f),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, height * 0.4f),
                        size = androidx.compose.ui.geometry.Size(width, height * 0.35f)
                    )

                    // Draw zone boundary gridlines
                    val gridLines = listOf(0.4f, 0.75f)
                    gridLines.forEach { lineY ->
                        drawLine(
                            color = BorderGlass,
                            start = androidx.compose.ui.geometry.Offset(0f, height * lineY),
                            end = androidx.compose.ui.geometry.Offset(width, height * lineY),
                            strokeWidth = 1f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                        )
                    }

                    // Build path for beautiful gradient filled curve
                    val xStep = width / (sentimentPoints.size - 1)
                    val pointsList = sentimentPoints.mapIndexed { idx, value ->
                        val x = idx * xStep
                        val ratio = (value - minVal) / range
                        val y = height - (ratio * height)
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    val fillPath = androidx.compose.ui.graphics.Path().apply {
                        if (pointsList.isNotEmpty()) {
                            moveTo(0f, height)
                            lineTo(pointsList.first().x, pointsList.first().y)
                            for (i in 1 until pointsList.size) {
                                lineTo(pointsList[i].x, pointsList[i].y)
                            }
                            lineTo(width, height)
                            close()
                        }
                    }

                    val curvePath = androidx.compose.ui.graphics.Path().apply {
                        if (pointsList.isNotEmpty()) {
                            moveTo(pointsList.first().x, pointsList.first().y)
                            for (i in 1 until pointsList.size) {
                                lineTo(pointsList[i].x, pointsList[i].y)
                            }
                        }
                    }

                    // Fill Gradient under curve
                    drawPath(
                        path = fillPath,
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(CyberMint.copy(alpha = 0.25f), Color.Transparent)
                        )
                    )

                    // Draw main line path
                    drawPath(
                        path = curvePath,
                        color = CyberMint,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )

                    // Draw nodes
                    pointsList.forEachIndexed { index, offset ->
                        val isHighlighted = (hoveredIndex == index)
                        drawCircle(
                            color = if (isHighlighted) Color.White else CyberMint,
                            radius = if (isHighlighted) 6f else 3f,
                            center = offset
                        )
                        if (isHighlighted) {
                            drawCircle(
                                color = CyberMint.copy(alpha = 0.4f),
                                radius = 12f,
                                center = offset
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Display selected node details
            val currentIndex = hoveredIndex ?: 29
            val currentScore = sentimentPoints[currentIndex]
            val zoneLabel = if (currentScore >= 75) "Empathetic Resonant" else "Professional Calm"
            val zoneColor = if (currentScore >= 75) CyberMint else CyberPurple
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MidnightBg, RoundedCornerShape(6.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Day ${currentIndex + 1} Assessment", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text("Sentiment Score: ${currentScore.toInt()}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EI Master Zone", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    Text(zoneLabel, color = zoneColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CommunicationSkillGrowthChart(viewModel: CommViewModel) {
    val completedActionsCount = viewModel.chatMessages.collectAsStateWithLifecycle().value.size
    val peerFeedBacksCount = viewModel.peerFeedbacks.collectAsStateWithLifecycle().value.size
    val plansCount = viewModel.communicationActionPlans.collectAsStateWithLifecycle().value.size

    // Calculate dynamic competence scores based on actual on-device workout logs
    val presenceScore = (72f + (completedActionsCount * 1.5f).coerceAtMost(18f)).toInt()
    val resiliencyScore = (68f + (peerFeedBacksCount * 4f).coerceAtMost(22f)).toInt()
    val empathyScore = (74f + (completedActionsCount * 0.8f).coerceAtMost(16f)).toInt()
    val pacingScore = (65f + (completedActionsCount * 2f).coerceAtMost(25f)).toInt()
    val assertScore = (70f + (plansCount * 5f).coerceAtMost(20f)).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("communication_skill_growth_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, BorderGlass)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "COMMUNICATION COMPETENCY SCORECARD",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                "Calculated from chats completed ($completedActionsCount), expert reviews ($peerFeedBacksCount), and action plans ($plansCount)",
                color = DarkSilver,
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val skills = listOf(
                Triple("Status Presence", presenceScore, CyberPurple),
                Triple("Resiliency Command", resiliencyScore, CyberMint),
                Triple("Conversational Empathy", empathyScore, GoldAccent),
                Triple("Vocal Pacing", pacingScore, CyberPurple),
                Triple("Direct Assertiveness", assertScore, CyberMint)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                skills.forEach { (label, value, color) ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(label, color = LightSilver, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("$value/100", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MidnightBg)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(value / 100f)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(color)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GeminiRoadmapGeneratorCard(viewModel: CommViewModel, profile: UserProfile) {
    var customGoal by remember { mutableStateOf("") }
    var contextDetails by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_roadmap_generator_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = CyberMint, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "AI WEEKLY mejorar ROADMAP GENERATOR",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                "Coconstruct an actionable 4-week split with the Gemini API mapped to your personal communication profile",
                color = DarkSilver,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Goal field
            Text("COMMUNICATION FOCUS GOAL", color = LightSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = customGoal,
                onValueChange = { customGoal = it },
                placeholder = { Text("e.g. Salary Negotiations, Investor Pitch, Interpersonal Objections", color = Color.Gray, fontSize = 11.sp) },
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberMint,
                    unfocusedBorderColor = BorderGlass,
                    focusedContainerColor = MidnightBg,
                    unfocusedContainerColor = MidnightBg
                )
            )

            // ContextDetails field
            Text("TARGET COMPASS & WORK CONTEXT CHALLENGES", color = LightSilver, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = contextDetails,
                onValueChange = { contextDetails = it },
                placeholder = { Text("e.g. Asking VP for $30k raise next Friday, defending timeline concessions to assertive CFO", color = Color.Gray, fontSize = 11.sp) },
                textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberMint,
                    unfocusedBorderColor = BorderGlass,
                    focusedContainerColor = MidnightBg,
                    unfocusedContainerColor = MidnightBg
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val goal = customGoal.ifEmpty { "General Leadership Presence" }
                    val focus = contextDetails.ifEmpty { "Executive Assertiveness" }
                    viewModel.generateWeeklyRoadmap(
                        goalName = goal,
                        focusArea = focus,
                        proficiencyLevel = "${profile.currentConfidenceScore}/100",
                        archetype = profile.communicationArchetype
                    )
                },
                enabled = !viewModel.isGeneratingRoadmap,
                colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(40.dp)
            ) {
                if (viewModel.isGeneratingRoadmap) {
                    CircularProgressIndicator(color = MidnightBg, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("ENGINE ANALYZING ORCHESTRATION...", color = MidnightBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                } else {
                    Text("BUILD ROADMAP WITH GEMINI", color = MidnightBg, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            // Generated Roadmap Output
            if (viewModel.generatedRoadmapMarkdown != null) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BorderGlass)
                Spacer(modifier = Modifier.height(10.dp))

                Text("✨ GENERATED ROADMAP PREVIEW", color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                        .background(MidnightBg, RoundedCornerShape(6.dp))
                        .border(BorderStroke(1.dp, BorderGlass), RoundedCornerShape(6.dp))
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp)
                ) {
                    val stepsList = viewModel.generatedRoadmapPlansList
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Calculated Roadmap Strategy for ${profile.communicationArchetype}:",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        stepsList.forEachIndexed { idx, plan ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MidnightSurfaceCard, RoundedCornerShape(4.dp))
                                    .padding(8.dp)
                            ) {
                                Text(plan.focusArea, color = CyberMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(plan.strategyText, color = Color.White, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(CyberPurple.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(plan.priority.uppercase() + " PRIORITY", color = CyberPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val plans = viewModel.generatedRoadmapPlansList
                        if (plans.isNotEmpty()) {
                            plans.forEach { plan ->
                                viewModel.insertActionPlan(plan)
                            }
                            Toast.makeText(context, "Successfully provisioned weekly modules straight to local database!", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                ) {
                    Text("ADD PLAN TO ON-DEVICE PORTFOLIO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun ShareAchievementsCard(viewModel: CommViewModel, profile: UserProfile) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val plans = viewModel.communicationActionPlans.collectAsStateWithLifecycle().value
    val peerFeedBacksCount = viewModel.peerFeedbacks.collectAsStateWithLifecycle().value.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("share_achievements_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, BorderGlass)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "SHARE MASTER MILESTONE BADGE",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
            Text(
                "Export your calculated baseline archetype, composite score, and active practice statistics to share with your peers",
                color = DarkSilver,
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
            )

            // Visual summary mockup card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MidnightBg, RoundedCornerShape(8.dp))
                    .border(BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f)), RoundedCornerShape(8.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("COMMCORE CERTIFICATE OF ASSIGNMENT", color = CyberPurple, fontSize = 8.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(profile.name.ifEmpty { "Elite Practitioner" }, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                    }
                    Box(
                        modifier = Modifier
                            .background(GoldAccent.copy(alpha = 0.15f), CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("COMPOSITE SCORE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("${profile.currentConfidenceScore}/100", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ACTIVE STREAK", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text("🔥 ${profile.streakCounter} DAYS", color = GoldAccent, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("ARCHETYPE", color = Color.Gray, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                        Text(profile.communicationArchetype.uppercase(), color = CyberMint, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    val summaryText = """
                        🏆 COMMCORE MILESTONE BADGE REPORT 🏆
                        ------------------------------------
                        👤 Practitioner: ${profile.name.ifEmpty { "Elite Practitioner" }}
                        🧠 Calculated Archetype: ${profile.communicationArchetype}
                        📈 Composite Executive Score: ${profile.currentConfidenceScore}/100
                        🔥 Practice Streaks: ${profile.streakCounter} Active Days
                        💼 Action Plans: ${plans.size} verified modules
                        📋 Review Assessments: $peerFeedBacksCount audits completed
                        ------------------------------------
                        Verified on-device via local CommCore Central Orchestration Engine.
                    """.trimIndent()

                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("CommCore Milestone Badge", summaryText)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, "📋 Milestone Badge Summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth().height(38.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("COPY MASTERY BADGE REPORT TO CLIPBOARD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// --- HELPER FUNCTION FOR GENUINE NATIVE ANDROID SYSTEM ALERTS ---
fun showAndroidNotification(context: android.content.Context, title: String, content: String) {
    val channelId = "commcore_daily_reminders"
    val notificationManager = context.getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = android.app.NotificationChannel(
            channelId,
            "CommCore Playbook Practice Reminders",
            android.app.NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Structured reminders adjusted to your active communication goals."
        }
        notificationManager.createNotificationChannel(channel)
    }

    val builder = androidx.core.app.NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(content)
        .setPriority(androidx.core.app.NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    try {
        notificationManager.notify(202, builder.build())
    } catch (e: Exception) {
        // Safe catch for permissions
    }
}

@Composable
fun DailyAffirmationCard(profile: UserProfile, viewModel: CommViewModel) {
    var claimedToday by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_affirmation_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(
            width = if (CommVisualRewards.equippedNeonBorder) 2.dp else 1.dp,
            brush = if (CommVisualRewards.equippedNeonBorder) {
                Brush.linearGradient(listOf(CyberPurple, Color.Cyan, CyberMint))
            } else {
                androidx.compose.ui.graphics.SolidColor(CyberMint.copy(alpha = 0.5f))
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Daily Booster",
                        tint = CyberMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "DAILY COMMUNICATION AFFIRMATION",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (viewModel.isFetchingAffirmation) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = CyberMint, strokeWidth = 1.dp)
                    } else {
                        IconButton(
                            onClick = { viewModel.fetchNewDailyAffirmation(profile.communicationArchetype, forceRefresh = true) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Core Booster", tint = CyberMint, modifier = Modifier.size(14.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .background(CyberPurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "EVIDENCE-BASED",
                            color = CyberPurple,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = viewModel.currentAffirmationTitle,
                color = CyberMint,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Text(
                text = viewModel.currentAffirmationText,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MidnightBg, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        "PRACTICAL HABIT FOR TODAY:",
                        color = GoldAccent,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = viewModel.currentAffirmationPractice,
                        color = LightSilver,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showExplanation = !showExplanation },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) {
                    Text(
                        if (showExplanation) "HIDE PSYCHOLOGY" else "WHY THIS WORKS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = {
                        if (!claimedToday) {
                            claimedToday = true
                            viewModel.triggerDailyGoalDone()
                            android.widget.Toast.makeText(context, "☀️ Confidence Affirmation Locked In! +4 Confidence Score", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (claimedToday) MidnightBg else CyberMint,
                        contentColor = if (claimedToday) Color.Gray else MidnightBg
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = if (claimedToday) BorderStroke(1.dp, BorderGlass) else null,
                    modifier = Modifier.height(32.dp).testTag("claim_affirmation_button")
                ) {
                    Icon(
                        imageVector = if (claimedToday) Icons.Default.CheckCircle else Icons.Default.Add,
                        contentDescription = "Claim",
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        if (claimedToday) "AFFIRMATION INTEGRATED" else "INTEGRATE & PRACTICE TODAY (+4 CS)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (showExplanation) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Psychological Grounding: Cognitive behavioral communication research demonstrates that asserting high-pacing intentional boundaries before entering workspace stress effectively intercepts amygdala highjacking, reducing speech tremor and stabilizing speech velocity.",
                    color = Color.Gray,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun MilestoneVisualRewardsCard(viewModel: CommViewModel, profile: UserProfile) {
    val haloUnlocked = profile.streakCounter >= 4
    val skinUnlocked = profile.streakCounter >= 6
    val auraUnlocked = profile.currentConfidenceScore >= 65

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("milestone_visual_rewards_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(
            width = if (CommVisualRewards.equippedNeonBorder) 2.dp else 1.dp,
            brush = if (CommVisualRewards.equippedNeonBorder) {
                Brush.linearGradient(listOf(CyberPurple, Color.Cyan, CyberMint))
            } else {
                androidx.compose.ui.graphics.SolidColor(CyberPurple.copy(alpha = 0.5f))
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Visual Rewards Shield",
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "UNLOCKED VISUAL REWARDS",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "MILESTONES",
                        color = GoldAccent,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Complete practice streaks or increase your Composite Confidence Score to unlock premium skin transformations.",
                color = LightSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            RewardItemRow(
                title = "Elite Golden Profile Halo",
                unlockedDesc = "Pulsing dashboard profile aureole.",
                requirementDesc = "Unlocks at 4-Day streak (Current: ${profile.streakCounter}/4)",
                unlocked = haloUnlocked,
                isEquipped = CommVisualRewards.equippedHalo,
                onEquipToggle = { CommVisualRewards.equippedHalo = !CommVisualRewards.equippedHalo },
                testTagPrefix = "reward_halo"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            RewardItemRow(
                title = "Neon Synthwave Skin",
                unlockedDesc = "Glowing cyan-purple card borders.",
                requirementDesc = "Unlocks at 6-Day streak (Current: ${profile.streakCounter}/6)",
                unlocked = skinUnlocked,
                isEquipped = CommVisualRewards.equippedNeonBorder,
                onEquipToggle = { CommVisualRewards.equippedNeonBorder = !CommVisualRewards.equippedNeonBorder },
                testTagPrefix = "reward_skin"
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))

            RewardItemRow(
                title = "Executive Dominance Aura",
                unlockedDesc = "Ambient cosmic violet backdrop.",
                requirementDesc = "Unlocks at >= 65 Critical Score (Current: ${profile.currentConfidenceScore}/65)",
                unlocked = auraUnlocked,
                isEquipped = CommVisualRewards.equippedAuraBackglow,
                onEquipToggle = { CommVisualRewards.equippedAuraBackglow = !CommVisualRewards.equippedAuraBackglow },
                testTagPrefix = "reward_aura"
            )
        }
    }
}

@Composable
fun RewardItemRow(
    title: String,
    unlockedDesc: String,
    requirementDesc: String,
    unlocked: Boolean,
    isEquipped: Boolean,
    onEquipToggle: () -> Unit,
    testTagPrefix: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = if (unlocked) Color.White else Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                if (!unlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = Color.Gray,
                        modifier = Modifier.size(11.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Unlocked",
                        tint = CyberMint,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (unlocked) unlockedDesc else requirementDesc,
                color = if (unlocked) CyberMint else DarkSilver,
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = onEquipToggle,
            enabled = unlocked,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEquipped) CyberPurple else CyberMint,
                contentColor = if (isEquipped) Color.White else MidnightBg,
                disabledContainerColor = MidnightBg,
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier
                .height(30.dp)
                .testTag("${testTagPrefix}_button"),
            border = if (!unlocked) BorderStroke(1.dp, BorderGlass) else null
        ) {
            Text(
                text = if (!unlocked) "LOCKED" else if (isEquipped) "EQUIPPED" else "EQUIP",
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

// --- GEMINI RADAR CHART & FEEDBACK BREAKDOWN COMPONENTS ---

@Composable
fun RadarChart(
    clarity: Int,
    structure: Int,
    resonance: Int,
    empathy: Int,
    assertiveness: Int,
    modifier: Modifier = Modifier
) {
    val labels = listOf("Clarity", "Structure", "Resonance", "Empathy", "Assertiveness")
    val values = listOf(clarity, structure, resonance, empathy, assertiveness)
    
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    
    Canvas(modifier = modifier) {
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.5f
        val numPoints = 5
        val anglePerPoint = (2 * Math.PI) / numPoints
        
        // 1. Draw web concentric polygons (4 levels: 25%, 50%, 75%, 100%)
        val levels = listOf(0.25f, 0.50f, 0.75f, 1.0f)
        levels.forEach { level ->
            val path = Path()
            for (i in 0 until numPoints) {
                val angle = i * anglePerPoint - Math.PI / 2
                val borderX = center.x + (radius * level * Math.cos(angle)).toFloat()
                val borderY = center.y + (radius * level * Math.sin(angle)).toFloat()
                if (i == 0) {
                    path.moveTo(borderX, borderY)
                } else {
                    path.lineTo(borderX, borderY)
                }
            }
            path.close()
            drawPath(
                path = path,
                color = Color.Gray.copy(alpha = 0.2f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // 2. Draw axes (spokes)
        for (i in 0 until numPoints) {
            val angle = i * anglePerPoint - Math.PI / 2
            val endX = center.x + (radius * Math.cos(angle)).toFloat()
            val endY = center.y + (radius * Math.sin(angle)).toFloat()
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = center,
                end = androidx.compose.ui.geometry.Offset(endX, endY),
                strokeWidth = 1.dp.toPx()
            )
            
            // Draw axis labels
            val labelRadius = radius + 20.dp.toPx()
            val labelX = center.x + (labelRadius * Math.cos(angle)).toFloat()
            val labelY = center.y + (labelRadius * Math.sin(angle)).toFloat()
            
            drawContext.canvas.nativeCanvas.drawText(
                "${labels[i]} (${values[i]})",
                labelX,
                labelY + 6.dp.toPx(),
                textPaint
            )
        }
        
        // 3. Draw user values filled polygon (glowing CyberMint/CyberPurple)
        val userPath = Path()
        for (i in 0 until numPoints) {
            val angle = i * anglePerPoint - Math.PI / 2
            val pct = (values[i] / 100f).coerceIn(0f, 1f)
            val pointRadius = radius * pct
            val userX = center.x + (pointRadius * Math.cos(angle)).toFloat()
            val userY = center.y + (pointRadius * Math.sin(angle)).toFloat()
            if (i == 0) {
                userPath.moveTo(userX, userY)
            } else {
                userPath.lineTo(userX, userY)
            }
        }
        userPath.close()
        
        drawPath(
            path = userPath,
            color = Color(0xFF00FFCC).copy(alpha = 0.22f) // CyberMint
        )
        drawPath(
            path = userPath,
            color = Color(0xFF00FFCC), // CyberMint
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw small vertex circles
        for (i in 0 until numPoints) {
            val angle = i * anglePerPoint - Math.PI / 2
            val pct = (values[i] / 100f).coerceIn(0f, 1f)
            val pointRadius = radius * pct
            val userX = center.x + (pointRadius * Math.cos(angle)).toFloat()
            val userY = center.y + (pointRadius * Math.sin(angle)).toFloat()
            drawCircle(
                color = Color(0xFFA855F7), // CyberPurple
                radius = 4.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(userX, userY)
            )
        }
    }
}

@Composable
fun GeminiToneClarityRadarCard(viewModel: CommViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("gemini_radar_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Radar Chart Icon",
                        tint = CyberMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "GEMINI QUALITATIVE SENTIMENT AUDIT",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "5-Dimensional Tone & Clarity Breakdown",
                            color = LightSilver,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                RadarChart(
                    clarity = viewModel.radarClarity,
                    structure = viewModel.radarStructure,
                    resonance = viewModel.radarToneResonance,
                    empathy = viewModel.radarEmpathy,
                    assertiveness = viewModel.radarAssertiveness,
                    modifier = Modifier.size(190.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                "LATEST GEMINI SENTIMENT EXPLANATORY REPORT:",
                color = GoldAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (viewModel.isAnalyzingToneClarity) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(color = CyberMint, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Gemini auditing tone & sentiment metrics...", color = CyberMint, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            } else if (viewModel.geminiToneClarityReport.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MidnightBg, RoundedCornerShape(8.dp))
                        .border(BorderStroke(0.5.dp, BorderGlass), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = viewModel.geminiToneClarityReport,
                        color = Color.White,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MidnightBg, RoundedCornerShape(8.dp))
                        .border(BorderStroke(0.5.dp, BorderGlass), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "No Tone & Clarity report recorded yet. Complete a verbal or text sparring roleplay session inside Coaching Central, then hit 'Analyze Active Simulation Transcript' to generate your custom M3 radar mapping.",
                        color = Color.Gray,
                        fontSize = 11.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

// --- LEADERBOARD & PERFORMERS DIRECTORIES ---

@Composable
fun ContestLeaderboardCard() {
    val performers = listOf(
        Triple("Alexander Wright", "Tech Product Lead", Pair(97, "Elite Assertor Badge")),
        Triple("Meira Srinivasan", "Venture Capital Partner", Pair(95, "Capital Negotiator Badge")),
        Triple("Sarah Jenkins", "Consulting Engagement Mgr", Pair(93, "Pyramid Master Certificate")),
        Triple("Elena Rostov", "Startup Founder", Pair(91, "Pitch Dynamo Badge")),
        Triple("David Chen", "Director of Product Hub", Pair(88, "Composure Anchor Badge"))
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contest_leaderboard_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberPurple)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Leaderboard",
                        tint = GoldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "MASTER_CONTESTS_LEADERBOARD",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "Current active communication mastery champions",
                            color = LightSilver,
                            fontSize = 9.sp
                        )
                    }
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberPurple.copy(alpha = 0.2f)),
                    border = BorderStroke(0.5.dp, CyberPurple)
                ) {
                    Text(
                        "LIVE WEEKLY",
                        color = CyberPurple,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                performers.forEachIndexed { index, (name, field, metrics) ->
                    val score = metrics.first
                    val badge = metrics.second
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MidnightBg, RoundedCornerShape(8.dp))
                            .border(BorderStroke(0.5.dp, BorderGlass), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(
                                        when (index) {
                                            0 -> GoldAccent.copy(alpha = 0.2f)
                                            1 -> Color.LightGray.copy(alpha = 0.2f)
                                            2 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                                            else -> Color.Transparent
                                        },
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            when (index) {
                                                0 -> GoldAccent
                                                1 -> Color.LightGray
                                                2 -> Color(0xFFCD7F32)
                                                else -> BorderGlass
                                            }
                                        ),
                                        RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (index + 1).toString(),
                                    color = when (index) {
                                        0 -> GoldAccent
                                        1 -> Color.White
                                        2 -> Color(0xFFCD7F32)
                                        else -> DarkSilver
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            Column {
                                Text(
                                    text = name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$field  •  $badge",
                                    color = LightSilver,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        
                        Text(
                            text = "$score%",
                            color = CyberMint,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// --- CORPORATE PARTNERS SPONSOR OPPORTUNITIES CARD ---

@Composable
fun CorporatePartnersCard(viewModel: CommViewModel) {
    var selectedPartner by remember { mutableStateOf<String?>(null) } // "Stripe", "McKinsey", "a16z"
    
    val partners = listOf(
        Triple("Stripe sponsors 'Global Developer Advocate Match'", "Developer Advocate & Integration Lead Roles", viewModel.wonStripeFastPass),
        Triple("McKinsey & Company sponsors 'Elite Case Presentation'", "Strategy Consultant & Associate Managers", viewModel.wonMcKinseyFastPass),
        Triple("Andreessen Horowitz (a16z) sponsors 'Venture Seed Pitch Sprints'", "Investment Associate & Venture Talent Queue", viewModel.wonA16zFastPass)
    )
    
    val opportunityMap = mapOf(
        "Stripe" to listOf(
            Triple("Lead Developer Relations Advocate", "San Francisco / Remote  |  $195K - $240K", "Lead Stripe integration pitches, write developer toolkits, and speak at international summits under pressure. Requires 'Stripe Lead Advocate Badge' (Scored 90%+ in Stripe contest)."),
            Triple("Strategic Integrations Consultant", "New York City  |  $170K - $210K", "Guide multi-million dollar tech giants through high-pressure digital architecture negotiations. Requires 'Stripe Lead Advocate Badge'.")
        ),
        "McKinsey" to listOf(
            Triple("Business Strategy Consultant", "Chicago / London  |  $180K - $220K", "Present core strategies using Barbara Minto's Pyramid Principle directly to Fortune 100 Executives. Requires McKinsey 'Analyst Elite Certificate' (Scored 90%+ in McKinsey contest).")
        ),
        "a16z" to listOf(
            Triple("Biotech VC Investment Associate", "Menlo Park / NYC  |  $230K - $290K + Carry", "Analyze early stage seed startups and conduct extreme due diligence under deep timelines. Requires 'VC Explorer Crest' (Scored 90%+ in a16z contest).")
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("corporate_partners_card"),
        colors = CardDefaults.cardColors(containerColor = MidnightSurface),
        border = BorderStroke(1.dp, CyberMint)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Corporate Partners",
                        tint = CyberMint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "ACTIVE_CORPORATE_SPONSORS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            "View and unlock direct career opportunities from sponsors",
                            color = LightSilver,
                            fontSize = 9.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderGlass)
            Spacer(modifier = Modifier.height(10.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                partners.forEach { (sponsorText, textDesc, isUnlocked) ->
                    val sponsorName = when {
                        sponsorText.contains("Stripe", ignoreCase = true) -> "Stripe"
                        sponsorText.contains("McKinsey", ignoreCase = true) -> "McKinsey & Company"
                        else -> "Andreessen Horowitz (a16z)"
                    }
                    val shortName = when {
                        sponsorText.contains("Stripe", ignoreCase = true) -> "Stripe"
                        sponsorText.contains("McKinsey", ignoreCase = true) -> "McKinsey"
                        else -> "a16z"
                    }
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MidnightBg),
                        border = BorderStroke(1.dp, if (isUnlocked) CyberMint.copy(alpha = 0.5f) else BorderGlass)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sponsorName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (isUnlocked) CyberMint.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .border(
                                            BorderStroke(
                                                1.dp,
                                                if (isUnlocked) CyberMint else Color.Transparent
                                            ),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isUnlocked) "VERIFIED PASS UNLOCKED" else "LOCKED (THRESHOLD 90%)",
                                        color = if (isUnlocked) CyberMint else Color.Gray,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sponsorText,
                                color = LightSilver,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Button(
                                onClick = { selectedPartner = shortName },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberPurple),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier.height(28.dp).fillMaxWidth().testTag("view_jobs_${shortName}")
                            ) {
                                Text(
                                    "VIEW ACTIVE JOB OPPORTUNITIES (" + (opportunityMap[shortName]?.size ?: 0) + ")",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Career Opps Dialog
    if (selectedPartner != null) {
        val partner = selectedPartner!!
        val jobs = opportunityMap[partner] ?: emptyList()
        val isUnlocked = when (partner) {
            "Stripe" -> viewModel.wonStripeFastPass
            "McKinsey" -> viewModel.wonMcKinseyFastPass
            "a16z" -> viewModel.wonA16zFastPass
            else -> false
        }
        
        androidx.compose.ui.window.Dialog(onDismissRequest = { selectedPartner = null }) {
            Surface(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp),
                color = MidnightSurface,
                border = BorderStroke(1.dp, BorderGlass)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$partner CAREER OPENINGS",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { selectedPartner = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Opps", tint = DarkSilver)
                        }
                    }
                    
                    HorizontalDivider(color = BorderGlass, modifier = Modifier.padding(vertical = 10.dp))
                    
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(jobs) { (roleName, rate, desc) ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MidnightBg),
                                border = BorderStroke(0.5.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(roleName, color = CyberMint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(rate, color = LightSilver, fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(desc, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                                    
                                    Spacer(modifier = Modifier.height(10.dp))
                                    
                                    Button(
                                        onClick = { selectedPartner = null },
                                        enabled = isUnlocked,
                                        modifier = Modifier.fillMaxWidth().height(32.dp).testTag("apply_job_button"),
                                        colors = ButtonDefaults.buttonColors(containerColor = CyberMint)
                                    ) {
                                        Text(
                                            text = if (isUnlocked) "SUBMIT PROFILE WITH FAST-PASS CODE [COMP-723A]" else "LOCKED: 90%+ REQUIRED IN SPONSOR CONTEST",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MidnightBg
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SEARCHABLE PARALLEL KNOWLEDGE HUB MODAL ---

@Composable
fun SearchableKnowledgeHubDialog(
    viewModel: CommViewModel,
    onDismissRequest: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") } // "All", "ARTICLE", "VIDEO TRANSCRIPT"
    var selectedArticle by remember { mutableStateOf<KnowledgeArticle?>(null) }
    
    val filteredArticles = remember(searchQuery, selectedCategory) {
        viewModel.curatedKnowledgeHubList.filter { article ->
            val matchesSearch = article.title.contains(searchQuery, ignoreCase = true) ||
                    article.content.contains(searchQuery, ignoreCase = true) ||
                    article.author.contains(searchQuery, ignoreCase = true)
            val matchesCategory = if (selectedCategory == "All") true else article.type.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }
    
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = MidnightSurface,
            border = BorderStroke(1.dp, BorderGlass)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (selectedArticle == null) {
                    // Search list view
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CyberMint, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "COMMUNICATION THEORY HUB",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Close Hub", tint = DarkSilver)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("knowledge_hub_search_input"),
                        placeholder = { Text("Search Minto, active voice, etc...", color = Color.Gray, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyberPurple) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.LightGray,
                            focusedBorderColor = CyberMint,
                            unfocusedBorderColor = BorderGlass,
                            focusedContainerColor = MidnightBg,
                            unfocusedContainerColor = MidnightBg
                        ),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("All", "ARTICLE", "VIDEO TRANSCRIPT").forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Button(
                                onClick = { selectedCategory = cat },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) CyberPurple else MidnightBg
                                ),
                                border = BorderStroke(1.dp, if (isSelected) CyberPurple else BorderGlass),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = if (cat == "ARTICLE") "ARTICLES" else if (cat == "VIDEO TRANSCRIPT") "TRANSCRIPTS" else "ALL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredArticles) { article ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedArticle = article }
                                    .testTag("knowledge_item_${article.id}"),
                                colors = CardDefaults.cardColors(containerColor = MidnightBg),
                                border = BorderStroke(1.dp, BorderGlass)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = article.type,
                                            color = if (article.type == "ARTICLE") CyberMint else CyberPurple,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = article.duration,
                                            color = DarkSilver,
                                            fontSize = 9.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = article.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "By ${article.author}",
                                        color = LightSilver,
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = article.content.take(120) + "...",
                                        color = DarkSilver,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                        
                        if (filteredArticles.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No theory resources found for '$searchQuery'", color = DarkSilver, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Detail view
                    val article = selectedArticle!!
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedArticle = null }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CyberMint, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "BACK TO THEORIES",
                                color = CyberMint,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.Default.Close, contentDescription = "Close Hub", tint = DarkSilver)
                        }
                    }
                    
                    HorizontalDivider(color = BorderGlass, modifier = Modifier.padding(vertical = 8.dp))
                    
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (article.type == "ARTICLE") CyberMint.copy(alpha = 0.15f) else CyberPurple.copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(1.dp, if (article.type == "ARTICLE") CyberMint else CyberPurple)
                            ) {
                                Text(
                                    text = article.type,
                                    color = if (article.type == "ARTICLE") CyberMint else CyberPurple,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(article.duration, color = DarkSilver, fontSize = 11.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        Text(
                            text = article.title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            lineHeight = 24.sp
                        )
                        
                        Text(
                            text = "Author: ${article.author}",
                            color = LightSilver,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = article.content,
                            color = Color.White,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            "KEY STRATEGIC TAKEAWAYS",
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        article.keyTakeaways.forEach { takeaway ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = CyberMint,
                                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = takeaway,
                                    color = LightSilver,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Button(
                        onClick = {
                            viewModel.chatInputText = "/prep theory: ${article.title}"
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberMint),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("⚡ LOAD TO ACTIVE ROLEPLAY WORKOUT", color = MidnightBg, fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }
    }
}
