package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val onboardingCompleted: Boolean = false,
    
    // Confidence scores (1-10) for Section A
    val confWorkplace: Int = 5,
    val confRomantic: Int = 5,
    val confFriends: Int = 5,
    val confPublic: Int = 5,
    val confConflict: Int = 5,
    val confDigital: Int = 5,
    
    // Converted fields (Stored as simple comma-separated or json lists for database simplicity)
    val chosenPainPoints: String = "", // e.g. "small_talk,criticism"
    val chosenModes: String = "", // e.g. "BUSINESS,PROFESSIONAL"
    val communicationGoal30Days: String = "",
    val learningPace: String = "Scenario practice (role-play simulations)", // Micro/Deep/Scenario...
    val timeCommitMinutes: Int = 15,
    val practicePreference: String = "AI-guided practice", // AI/Human/Both
    
    // AI generated metrics during Phase 2 Onboarding
    val communicationArchetype: String = "The Developing Speaker",
    val primaryMode: String = "PROFESSIONAL",
    val currentConfidenceScore: Int = 50, // Calculated composite score (1-100)
    
    // Daily Intention / Plan States
    val selectedWeekIndex: Int = 1,
    val streakCounter: Int = 3,
    val communityContributionScore: Int = 42,
    val lastLoginTimestamp: Long = 0L
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sender: String, // "user" or "ai" or "agent_<id>"
    val messageText: String,
    val isCommand: Boolean = false,
    val isImage: Boolean = false,
    val imageBase64: String? = null,
    val activeMode: String = "PROFESSIONAL"
)

@Entity(tableName = "booked_sessions")
data class BookedSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val coachName: String,
    val scheduledTime: String,
    val sessionType: String,
    val price: String,
    val progressStatus: String = "Scheduled", // "Scheduled", "Completed"
    val notes: String = ""
)

@Entity(tableName = "joined_courses")
data class JoinedCourse(
    @PrimaryKey val courseId: String,
    val title: String,
    val description: String,
    val category: String, // Mode name
    val progress: Int = 0, // percentage (0-100)
    val isCreatorOwn: Boolean = false,
    val creatorTier: String = "Silver" // "Bronze", "Silver", "Gold", "Platinum"
)

@Entity(tableName = "communication_action_plans")
data class CommunicationActionPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalName: String,
    val focusArea: String,
    val strategyText: String,
    val priority: String, // "High", "Medium", "Low"
    val progressPercentage: Int = 0,
    val targetDays: Int = 30,
    val isCompleted: Boolean = false
)

@Entity(tableName = "peer_feedback_assessments")
data class PeerFeedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val peerName: String,
    val dateString: String,
    val category: String,
    val scenario: String,
    val scorePresence: Int,
    val scoreEngagement: Int,
    val scoreResonance: Int,
    val constructiveNotes: String,
    val keyTakeaways: String
)

@Entity(tableName = "firebase_communication_goals")
data class FirebaseGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalTitle: String,
    val details: String = "",
    val firebasePushKey: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "roleplay_histories")
data class RoleplayHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val scenarioName: String,
    val dateString: String,
    val summaryText: String,
    val aiScore: Int,
    val metricFillerCount: Int = 0,
    val metricAssertiveness: Int = 80,
    val timestamp: Long = System.currentTimeMillis()
)

