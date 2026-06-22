package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object PersonalizedPlanService {

    suspend fun generatePersonalizedImprovementPlan(
        goalName: String,
        painPoints: String,
        archetype: String,
        confidenceScore: Int,
        timeCommitMinutes: Int,
        learningPace: String
    ): List<CommunicationActionPlan> = withContext(Dispatchers.IO) {
        val apiKey = CommCoreEngine.getApiKey()
        if (apiKey.isEmpty()) {
            Log.d("PersonalizedPlanService", "API Key is empty, using fallback action plans based on specific goals.")
            return@withContext generateFallbackPlans(goalName, painPoints, archetype)
        }

        val systemInstructionText = """
            You are CommCore's premier Elite Communication Architect.
            Your task is to analyze the user's unique communication goal, specific focus area / pain points, and current proficiency level, and construct exactly 4 actionable development modules.
            
            Structure your output ONLY as a raw JSON array block wrapped in ```json ... ``` containing exactly 4 elements:
            [
              {
                "goalName" : "$goalName",
                "focusArea" : "Week 1: Focus Subtitle",
                "strategyText" : "Specific highly detailed technique or exercise to apply during on-device training.",
                "priority" : "High"
              },
              ...
            ]
            Provide exactly 4 entries. The JSON block must be strictly parseable using standard double quotes. Use priorities "High", "Medium", or "Low".
        """.trimIndent()

        val promptText = """
            Design an advanced personalized 4-week communication action plan matching:
            - Focus Goal: $goalName
            - Core Pain Points to Neutralize: $painPoints
            - Current Baseline Archetype: $archetype
            - Current Competence Index score: $confidenceScore/100
            - Learning Velocity: $learningPace committing $timeCommitMinutes minutes daily
        """.trimIndent()

        val contentsList = listOf(Content(parts = listOf(Part(text = promptText))))
        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                return@withContext parsePlansFromText(responseText, goalName)
            } else {
                throw Exception("Gemini service returned empty candidate content")
            }
        } catch (e: Exception) {
            Log.e("PersonalizedPlanService", "Gemini content generation failed, resorting to custom goal fallback", e)
            return@withContext generateFallbackPlans(goalName, painPoints, archetype)
        }
    }

    private fun parsePlansFromText(text: String, fallbackGoal: String): List<CommunicationActionPlan> {
        val plans = mutableListOf<CommunicationActionPlan>()
        try {
            val jsonStartIndex = text.indexOf("```json")
            if (jsonStartIndex != -1) {
                var jsonSubstring = text.substring(jsonStartIndex + 7)
                val jsonEndIndex = jsonSubstring.indexOf("```")
                if (jsonEndIndex != -1) {
                    jsonSubstring = jsonSubstring.substring(0, jsonEndIndex).trim()
                }
                val jsonArray = JSONArray(jsonSubstring)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    plans.add(
                        CommunicationActionPlan(
                            goalName = obj.optString("goalName", fallbackGoal),
                            focusArea = obj.optString("focusArea", "Week ${i + 1}: Module Calibrator"),
                            strategyText = obj.optString("strategyText", "Practice clear status updates daily without qualification modifiers."),
                            priority = obj.optString("priority", "High"),
                            progressPercentage = 0,
                            targetDays = 30,
                            isCompleted = false
                        )
                    )
                }
            }
            if (plans.isEmpty()) {
                throw Exception("Could not find any JSON array blocks in Gemini response text")
            }
        } catch (e: Exception) {
            Log.e("PersonalizedPlanService", "Failed to parse json block in response, fallback details generated", e)
            return generateFallbackPlans(fallbackGoal, "", "")
        }
        return plans
    }

    private fun generateFallbackPlans(goalName: String, painPoints: String, archetype: String): List<CommunicationActionPlan> {
        val gTitle = if (goalName.isNotBlank()) goalName else "Dynamic Leadership Alignment"
        val pPoints = if (painPoints.isNotBlank()) painPoints else "executive focus objections"
        return listOf(
            CommunicationActionPlan(
                goalName = gTitle,
                focusArea = "Week 1: Strategic Vocal Pauses",
                strategyText = "Practice introducing a structured 2-second deliberate pause immediately when presented with objections or stress prompts around $pPoints.",
                priority = "High",
                progressPercentage = 5,
                targetDays = 30,
                isCompleted = false
            ),
            CommunicationActionPlan(
                goalName = gTitle,
                focusArea = "Week 2: Qualifier Elimination Drill",
                strategyText = "Omit words like 'maybe', 'just', 'mostly', and 'sorry' during team alignment standups. Speak with declarative presence.",
                priority = "High",
                progressPercentage = 0,
                targetDays = 30,
                isCompleted = false
            ),
            CommunicationActionPlan(
                goalName = gTitle,
                focusArea = "Week 3: Empathetic Mirror Bracing",
                strategyText = "Incorporate direct partner validation and mirror core objection concerns before advancing with active counterpoint frames.",
                priority = "Medium",
                progressPercentage = 0,
                targetDays = 30,
                isCompleted = false
            ),
            CommunicationActionPlan(
                goalName = gTitle,
                focusArea = "Week 4: Anchoring Calibration",
                strategyText = "Deploy two clear status anchors during high-stakes roleplay simulators to maintain proper negotiation control.",
                priority = "High",
                progressPercentage = 0,
                targetDays = 30,
                isCompleted = false
            )
        )
    }

    fun getPredefinedScenariosForMode(mode: String): List<Pair<String, String>> {
        return when (mode.uppercase()) {
            "BUSINESS" -> listOf(
                "Pitching to Skeptical Stakeholders" to "Arthur (Critique CFO): Present a high-budget project with objective ROI validation.",
                "Negotiating a Contract Renewal" to "Monica (Vulnerable Vendor VP): Protect margins while retaining long-term supply alignments.",
                "Handling High-stakes Objections" to "Richard (Pragmatic Client Director): Defuse pushback on service deliverables with calm precision."
            )
            "PROFESSIONAL" -> listOf(
                "Asking for Promotion or Raise" to "Helen (Results-Driven Director): Anchor value metrics assertively with no qualification apologies.",
                "Giving Critical Performance Feedback" to "Sarah (Defensive Specialist): Address persistent tardiness without sparking emotional resistance.",
                "Proposing Strategic Workflow Overhaul" to "Dennis (Time-Pressed Executive): Pitch radical coordination adjustments in under 2 minutes."
            )
            "RELATIONSHIP" -> listOf(
                "Expressing Needs During Disagreement" to "Alex (Anxious Partner): Clearly communicate boundary feelings without generating blame cycles.",
                "Offering Support After a Difficult Day" to "Jordan (Exhausted Spouse): Actively validate partner frustrations using empathetic mirroring.",
                "Discussing Long-term Boundary Changes" to "Taylor (Protective Partner): Reach common alignment on critical family work share split."
            )
            "FRIENDS" -> listOf(
                "Addressing Trip Budget Imbalance" to "Nicky (Carefree Friend): Express discomfort on shared trip splitting without causing social rift.",
                "De-escalating Heated Political Clashes" to "Sam (Highly Opinionated Friend): Defuse group-chat tension and maintain friendship respect.",
                "Declining Multi-Sociailizing Requests" to "Jamie (Extroverted Buddy): Communicate personal social burnout without triggering friend neglect feelings."
            )
            "PUBLIC" -> listOf(
                "Handling Sudden Townhall Heckling" to "Marcus (Critical Attendee): Keep executive vocal pacing intact under public pressure.",
                "Filing Press Briefing Feedback" to "Victoria (Investigating Reporter): Structure impromptu responses during an unscheduled crisis leak.",
                "Delivering High-stakes Project Toast" to "Raymond (Skeptical Panelist): Deliver dynamic storytelling hook with narrative confidence."
            )
            "CONFLICT" -> listOf(
                "Resolving Hostile Boundary Disputes" to "Ben (Angry Neighbor): Establish objective property separation lines while calming friction.",
                "Addressing Overlapping Team Leadership" to "Dennis (Insecure Co-Lead): Negotiate clear department lines of control and avoid double signals.",
                "Managing Broken Delivery Promises" to "Vivian (Demanding Vice President): Pivot conversational control to solutions after operational delay."
            )
            "DIGITAL" -> listOf(
                "Diffusing Passive-aggressive Teams Reply" to "Chloe (Sarcastic Team Lead): Address underlying professional concern neutrally in text.",
                "Asynchronous Restructuring Pitch" to "Gerald (Async-Only Supervisor): Deliver dense operational reorganization request in clear prose.",
                "Reframing Hostile Client Complaints" to "Monica (Overwhelmed Client): Translate stressful client email accusations to constructive alignments."
            )
            else -> listOf(
                "Negotiating Salary Anchoring" to "Arthur (Hard-Nosed CFO): Leverage clear baseline rates to anchor standard market values.",
                "Handling Constructive Review Feedback" to "Sarah (Unreceptive Employee): Calm and align target outcomes clearly.",
                "VC Pitch Objections Challenge" to "Clara (Skeptical Investor): Navigate extreme funding scrutiny using direct factual anchors."
            )
        }
    }

    suspend fun generateCustomScenariosForMode(
        mode: String,
        goalName: String,
        painPoints: String,
        archetype: String
    ): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        val apiKey = CommCoreEngine.getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext getPredefinedScenariosForMode(mode)
        }

        val systemInstructionText = """
            You are CommCore's premier Elite Communication Architect.
            The user is working in MODE: $mode.
            Your task is to analyze the user's communication goal: "$goalName", painPoints: "$painPoints", and archetype: "$archetype" and generate exactly 3 custom high-level practice scenarios tailored specifically for $mode mode.
            
            Each scenario must be represented by a title and an active roleplay persona with brief instruction.
            Structure your output ONLY as a raw JSON array block wrapped in ```json ... ``` containing exactly 3 elements:
            [
              {
                "title": "Short title matching the challenge",
                "persona": "Name of AI persona (Characteristic title): Small roleplaying instruction."
              },
              ...
            ]
            Provide exactly 3 entries. The JSON block must be strictly parseable using standard double quotes. Ensure scenarios are actionable and relevant to common communication challenges within the $mode mode.
        """.trimIndent()

        val promptText = """
            Generate 3 customized practice scenarios for $mode Mode.
            Goal: $goalName
            Pain Points to target: $painPoints
            User Archetype: $archetype
        """.trimIndent()

        val contentsList = listOf(Content(parts = listOf(Part(text = promptText))))
        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(temperature = 0.8f),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            val response = RetrofitClient.service.generateContent("gemini-3.5-flash", apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                return@withContext parseScenariosFromText(responseText, mode)
            } else {
                throw Exception("Response candidate empty")
            }
        } catch (e: Exception) {
            Log.e("PersonalizedPlanService", "Failed to generate custom scenarios, returning predefined", e)
            return@withContext getPredefinedScenariosForMode(mode)
        }
    }

    private fun parseScenariosFromText(text: String, mode: String): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        try {
            val jsonStartIndex = text.indexOf("```json")
            if (jsonStartIndex != -1) {
                var jsonSubstring = text.substring(jsonStartIndex + 7)
                val jsonEndIndex = jsonSubstring.indexOf("```")
                if (jsonEndIndex != -1) {
                    jsonSubstring = jsonSubstring.substring(0, jsonEndIndex).trim()
                }
                val jsonArray = JSONArray(jsonSubstring)
                for (i in 0 until jsonArray.length().coerceAtMost(3)) {
                    val obj = jsonArray.getJSONObject(i)
                    val title = obj.optString("title").trim()
                    val persona = obj.optString("persona").trim()
                    if (title.isNotEmpty() && persona.isNotEmpty()) {
                        list.add(Pair(title, persona))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PersonalizedPlanService", "Failed to parse scenarios JSON", e)
        }
        if (list.size < 3) {
            return getPredefinedScenariosForMode(mode)
        }
        return list
    }
}
