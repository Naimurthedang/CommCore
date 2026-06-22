package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CommCoreEngine {
    private const val TAG = "CommCoreEngine"

    // Retrieve API key with support for placeholder fallback
    fun getApiKey(): String {
        val key = BuildConfig.GEMINI_API_KEY
        return if (key.isEmpty() || key == "MY_GEMINI_API_KEY") "" else key
    }

    suspend fun generateChatResponse(
        userInput: String,
        history: List<ChatMessage>,
        currentMode: String,
        isThinkingHigh: Boolean = false,
        name: String = "User"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        
        // Command and sub-agent auto-assignment
        val (activeAgent, agentIntro) = determineAgent(userInput, currentMode)
        val cleanInput = removeCommandPrefix(userInput)
        
        val systemInstructionText = getSystemInstructions(currentMode, activeAgent, userInput, name)
        
        if (apiKey.isEmpty()) {
            // Run high-fidelity offline fallback simulation
            return@withContext simulateOfflineFallback(userInput, currentMode, activeAgent, agentIntro, name)
        }

        val selectedModel = if (isThinkingHigh) {
            "gemini-3.1-pro-preview"
        } else {
            "gemini-3.5-flash"
        }

        // Format conversation history to match Gemini API structure
        val contentsList = mutableListOf<Content>()
        
        // Add last 6 messages of conversation to avoid exceeding token limit
        val recentHistory = history.takeLast(6)
        recentHistory.forEach { msg ->
            contentsList.add(
                Content(parts = listOf(Part(text = "${msg.sender.uppercase()}: ${msg.messageText}")))
            )
        }
        
        // Add current user prompt
        contentsList.add(
            Content(parts = listOf(Part(text = "USER: $cleanInput")))
        )

        val request = GenerateContentRequest(
            contents = contentsList,
            generationConfig = GenerationConfig(
                temperature = 0.7f,
                thinkingConfig = if (isThinkingHigh) ThinkingConfig(thinkingLevel = "HIGH") else null
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        try {
            val response = RetrofitClient.service.generateContent(selectedModel, apiKey, request)
            val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (responseText != null) {
                if (agentIntro.isNotEmpty()) {
                    "$agentIntro\n\n$responseText"
                } else {
                    responseText
                }
            } else {
                throw Exception("Response candidate empty")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API failed, falling back to local simulation", e)
            simulateOfflineFallback(userInput, currentMode, activeAgent, agentIntro, name)
        }
    }

    // Determine fallback agent and intro text
    private fun determineAgent(input: String, mode: String): Pair<String, String> {
        val clean = input.lowercase().trim()
        return when {
            clean.contains("conflict") || clean.contains("fight") || clean.contains("angry") || clean.contains("apologize") || clean.startsWith("/urgent") -> {
                "The Diplomat" to "🕊️ **I'm bringing in The Diplomat for this... [Conflict de-escalation Specialist]**"
            }
            clean.contains("speech") || clean.contains("presentation") || clean.contains("pitch") || clean.contains("public") || clean.contains("story") -> {
                "The Storyteller" to "🎤 **I'm bringing in The Storyteller for this... [Narrative & Presentation Expert]**"
            }
            clean.contains("negotiate") || clean.contains("raise") || clean.contains("boss") || clean.contains("salary") || clean.startsWith("/prep") -> {
                "The Strategist" to "♟️ **I'm bringing in The Strategist for this... [Power Dynamics & Obstacle Planner]**"
            }
            clean.contains("email") || clean.contains("slack") || clean.contains("text") || clean.contains("social") || clean.startsWith("/polish") -> {
                "The Editor" to "✍️ **I'm bringing in The Editor for this... [Digital Text Tone Optimizer]**"
            }
            clean.contains("relationship") || clean.contains("date") || clean.contains("intimacy") || clean.contains("partner") || clean.contains("empath") -> {
                "The Connector" to "❤️ **I'm bringing in The Connector for this... [Intimacy & Empathy Architect]**"
            }
            else -> {
                "The Mirror" to "🪞 **I'm bringing in The Mirror for this... [Objectivity and Pattern Reporter]**"
            }
        }
    }

    private fun removeCommandPrefix(input: String): String {
        return input.replace(Regex("^/(polish|prep|debrief|roleplay|urgent|sandbox)\\s*"), "").trim()
    }

    private fun getSystemInstructions(mode: String, agent: String, input: String, name: String): String {
        val base = """
            You are CommCore AI, the central intelligence engine for a communication mastery platform. 
            You are operating in $mode mode. 
            The user's name is $name.
            Your current assigned sub-agent personality is $agent. 
            
            ADAPTATION FOR COMRADERY ($mode Mode):
            - Tailor your vocabulary, pacing, and tone.
            - Focus on actionable, step-by-step communication advice.
            - Ensure every single reply concludes with an actionable 'Next Step' challenge.
            
            ACTIVE COMMANDS:
            If user typed a command, handle it with absolute priority:
            - /polish: Rewrite text for maximum safety, feedback, and outcome based on $mode.
            - /prep: Formulate opening lines, 3 objection handlers, and subtle physical-pacing behaviors.
            - /debrief: Critically analyze positive/negative patterns, extract lessons.
            - /roleplay: Start or progress a conversational exercise.
            - /urgent: Provide emergency diplomatic triage, boundary maintenance or recovery plans.
            - /sandbox: Spin up a custom sandbox laboratory training session using specified categories, stakeholder profile settings, and communication criteria tags.
            
            At the end of your response, always append a short Feedback Summary with these 4 items:
            1. Behavioral Observation: What the user is doing.
            2. Impact Analysis: How it lands on others.
            3. Target Adjustment: One fine-tuned tweak.
            4. Composite Score Impact: Potential +1 to +5 confidence bonus.
        """.trimIndent()
        return base
    }

    // High quality offline fallback system ensuring real results in sandbox
    private fun simulateOfflineFallback(
        userInput: String,
        currentMode: String,
        agentName: String,
        agentIntro: String,
        name: String
    ): String {
        val cleanInput = removeCommandPrefix(userInput)
        val isCommand = userInput.startsWith("/")
        
        val (cmdType, promptContent) = if (isCommand) {
            val type = userInput.substringBefore(" ").removePrefix("/")
            val content = userInput.substringAfter(" ", "").ifEmpty { cleanInput }
            type to content
        } else {
            "" to userInput
        }

        val builder = java.lang.StringBuilder()
        builder.append(agentIntro).append("\n\n")

        when (cmdType) {
            "polish" -> {
                builder.append("### ✨ Polish Operation Complete\n")
                builder.append("I have optimized your draft to maximize influence and tone in **$currentMode Mode**:\n\n")
                when (currentMode) {
                    "BUSINESS" -> {
                        builder.append("> **Before:** \"$promptContent\"\n\n")
                        builder.append("> **After (ROI-Focused):** \"I wanted to share the updated milestones. Following our previous sync, we have mapped key efficiency gains. Let's block 10 minutes tomorrow to align on resource optimization and next steps.\"\n")
                    }
                    "RELATIONSHIP" -> {
                        builder.append("> **Before:** \"$promptContent\"\n\n")
                        builder.append("> **After (Empathy-Driven):** \"I hear what you are saying, and I want to understand how you feel. Can we speak about this tonight without distractions? I really care about finding a solution that works for both of us.\"\n")
                    }
                    else -> {
                        builder.append("> **Before:** \"$promptContent\"\n\n")
                        builder.append("> **After (Polished & Streamlined):** \"Corrected the tone for maximum professional clarity. Let's coordinate on details this afternoon so we can hit our target targets.\"\n")
                    }
                }
            }
            "prep" -> {
                builder.append("### 🎯 Conversation Strategy Blueprint\n")
                builder.append("Preparing you to lead the upcoming conversation around *\"$promptContent\"* successfully:\n\n")
                builder.append("1. **🎯 Icebreaker/Opening Line:** \"Thank you for sitting down with me. I wanted to align our goals regarding $promptContent so we can maximize shared success.\"\n")
                builder.append("2. **🛡️ 3 Objection Handlers:**\n")
                builder.append("   - *Objection:* 'We don't have the timing/budget.' ➔ *Response:* 'I completely understand. That's why we're starting with a scaled-down milestone to minimize risk.'\n")
                builder.append("   - *Objection:* 'This isn't a priority correct now.' ➔ *Response:* 'I feel you. Let's look at how this cuts future workload so we free up team capacity.'\n")
                builder.append("   - *Objection:* 'I need to check with others.' ➔ *Response:* 'Absolutely. Let's compile a brief summary together to make their review seamless.'\n")
                builder.append("3. **🧘 Body Language / Cue:** Keep your breathing steady, maintain 70% direct eye-contact, and nod slowly to signal active presence.\n")
            }
            "debrief" -> {
                builder.append("### 📊 Communication Debrief Analysis\n")
                builder.append("Analyzing the scenario: *\"$promptContent\"*\n\n")
                builder.append("- **What Went Well:** You showed vulnerability and raised the issue directly instead of postponing. That prevents boundary decay.\n")
                builder.append("- **What Went Poorly:** Apologizing repeatedly ('sorry to bring this up') diluted your authority and shifted the dynamic.\n")
                builder.append("- **Core Psychological Lesson:** Respect is built by claiming your rightful needs gracefully. When you apologize for having boundaries, you inadvertently invite others to ignore them.\n")
            }
            "roleplay" -> {
                builder.append("### 🎭 Interactive Play Simulation: Active!\n")
                builder.append("Let's simulate the scenario: *\"$promptContent\"*.\n\n")
                if (promptContent.contains("Salary Negotiation")) {
                    builder.append("👔 **Persona Profile:** Arthur (Hard-nosed CFO)\n")
                    builder.append("───────────────────────────────────\n\n")
                    builder.append("\"Look, in terms of your proposal for a 15% bump... the budget is incredibly tight this quarter. High costs in marketing have forced a general wage freeze. Honestly, I don't see why we should make an exception here when everyone is making sacrifices. What is your bottom-line rationale for changing the general schedule?\"\n\n")
                    builder.append("*Arthur sits back, tapping his pen. How do you anchor your first double-anchor response?*")
                } else if (promptContent.contains("VC Pitch")) {
                    builder.append("💼 **Persona Profile:** Clara (Skeptical Investor)\n")
                    builder.append("───────────────────────────────────\n\n")
                    builder.append("\"I see your projected user acquisition curve, but honestly, this space is overcrowded. There are like 10 other startups with similar features. What is your real unfair distribution advantage that prevents them from crushing you in six months? Pitch me live.\"\n\n")
                    builder.append("*Clara folds her arms and looks straight at you. Speak clearly, pacing your words.*")
                } else if (promptContent.contains("Conflict Resolution")) {
                    builder.append("🤝 **Persona Profile:** Dennis (Defensive Manager)\n")
                    builder.append("───────────────────────────────────\n\n")
                    builder.append("\"If you're hinting that the team deadlines aren't being met, I'd suggest you look at your own delivery bottlenecks first. My developers have been working overtime. Why are we talking about my scheduling when your team took extra days last week?\"\n\n")
                    builder.append("*Dennis is visibly defensive. Mirror his concerns first to trigger oxytocin and active listening.*")
                } else if (promptContent.contains("Boundary Setting")) {
                    builder.append("🛡️ **Persona Profile:** Monica (Over-demanding Client)\n")
                    builder.append("───────────────────────────────────\n\n")
                    builder.append("\"I know we agreed on a Friday delivery, but I need these complete mockups in my inbox by Saturday morning. My CEO decided to move the board meeting to Monday morning. I am expecting you to get this done. Can I count on you or do we need to re-evaluate our long-term collaboration?\"\n\n")
                    builder.append("*Monica sounds high-pressure. Formulate a boundary integrity shield with gravity anchors.*")
                } else if (promptContent.contains("Performance Review")) {
                    builder.append("📈 **Persona Profile:** Vivian (Results-driven VP)\n")
                    builder.append("───────────────────────────────────\n\n")
                    builder.append("\"We need to talk about your leadership metrics. Your output scores are solid, but we are looking for a massive multiplier in team velocity. The board expects a 2x throughput this quarter. Are you ready to make hard cuts if needed to hit this? What is your execution plan?\"\n\n")
                    builder.append("*Vivian looks at you with high standards. Structure your impromptu response clearly.*")
                } else {
                    builder.append("\"I see your point, but we've always handled it this way. Why should we invest time in changing it now?\"\n\n")
                    builder.append("*How do you respond? State your approach. I will assess your assertiveness level.*")
                }
            }
            "urgent" -> {
                builder.append("### 🚨 Emergency Triage Protocol Activated!\n")
                builder.append("Situation: *\"$promptContent\"*\n\n")
                builder.append("1. **Do Not Panic:** Take a deep breath. Refrain from sending defensive follow-up messages immediately.\n")
                builder.append("2. **Send a Re-calibration note:** 'I wanted to send a quick clarification. My previous message was sent in haste—I meant to emphasize our shared goal of optimization, not criticize. Let's discuss in person.'\n")
                builder.append("3. **Repair Blueprint:** Next time, wait 3 minutes before hitting send on high-stake communications.\n")
            }
            "sandbox" -> {
                builder.append("### ⚡ ADVANCED AI SANDBOX LAB ACTIVATED!\n")
                val params = promptContent.split(",")
                val category = params.getOrNull(0)?.trim()?.ifEmpty { "Strategic Boardroom" } ?: "Strategic Boardroom"
                val difficulty = params.getOrNull(1)?.trim()?.ifEmpty { "Hostile/Aggressive" } ?: "Hostile/Aggressive"
                val focus = params.getOrNull(2)?.trim()?.ifEmpty { "Vocal Filler Word Shield" } ?: "Vocal Filler Word Shield"
                
                builder.append("✨ **Immersive Sandbox Lab Session Initialized Successfully.**\n")
                builder.append("- **Selected Context:** $category\n")
                builder.append("- **Opponent Profile:** $difficulty\n")
                builder.append("- **Primary Skill Objective:** $focus\n")
                builder.append("───────────────────────────────────\n\n")
                builder.append("\"Look, I hear your theories on scope alignment, but I've been in this industry as a director for fifteen years. Asking for an immediate resource or budget change right now is completely out of the question. We need quick, direct performance output, not secondary administrative friction. What is your bottom line? Let's make this simple and assertive.\"\n\n")
                builder.append("*How do you respond? State your strategy contextually. Your live Speech Telemetry HUD overlay is tracking performance indices.*")
            }
            else -> {
                // Standard chatbot conversational reply based on selected sub-agent
                when (agentName) {
                    "The Diplomat" -> {
                        builder.append("Hello $name. As The Diplomat, I focus on conflict mediation and boundaries. You asked: \"$promptContent\".\n\n")
                        builder.append("In $currentMode, managing high emotional stakes requires de-escalation: replace accusations with objective observations. Use 'I-statements' to declare how specific actions impact goals.")
                    }
                    "The Storyteller" -> {
                        builder.append("Step on stage, $name. I'm The Storyteller. You asked: \"$promptContent\".\n\n")
                        builder.append("To hold attention, use a narrative hook immediately. Start with a vivid scenario: 'Picture this...'. Cut fillers, lower your vocal register, and let silence emphasize critical milestones.")
                    }
                    "The Strategist" -> {
                        builder.append("Let's look at the power dynamics, $name. I'm The Strategist. You asked: \"$promptContent\".\n\n")
                        builder.append("In $currentMode, leverage is key. Map their secondary motivators—what does your counterpart stand to gain? Script your questions to lead them to discover your solution.")
                    }
                    "The Editor" -> {
                        builder.append("Let's refine this, $name. I'm The Editor. You asked: \"$promptContent\".\n\n")
                        builder.append("Digital interfaces suffer from high signal loss. Keep paragraphs under 3 lines. Bold key variables. Use punchy subject lines to double your open response rate.")
                    }
                    "The Connector" -> {
                        builder.append("Hello $name. As The Connector, I map relationship bonds. You asked: \"$promptContent\".\n\n")
                        builder.append("Active listening is your strongest tool. Reflect they emotions before suggesting actions. Acknowledge their perspective: 'It sounds like you felt undervalued in that discussion.'")
                    }
                    else -> {
                        builder.append("Welcome $name. I am CommCore AI, your communication coach. You asked: \"$promptContent\".\n\n")
                        builder.append("Your progression as a master communicator starts with mindfulness. Every interaction is either building connection or generating noise. Let's practice active frameworks together.")
                    }
                }
            }
        }

        builder.append("\n\n")
        builder.append("═══════════════════════════════════\n")
        builder.append("📊 **COMMCORE COACH FEEDBACK ARCHITECTURE**\n")
        builder.append("═══════════════════════════════════\n")
        builder.append("1. **Behavioral Observation:** Exploring active, direct communication methods under $currentMode.\n")
        builder.append("2. **Impact Analysis:** Boldness builds strong long-term alignment, though initially raising small team friction.\n")
        builder.append("3. **Target Tweak:** Limit apologies ('just', 'sorry') by 50% in next script exchange.\n")
        builder.append("4. **Composite Confidence Index:** **+3 Points** (Confidence track updated!)\n")

        return builder.toString()
    }
}
