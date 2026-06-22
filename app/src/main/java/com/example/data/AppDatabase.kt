package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileDirect(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfile)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface BookedSessionDao {
    @Query("SELECT * FROM booked_sessions ORDER BY id DESC")
    fun getAllBookedSessions(): Flow<List<BookedSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(bookedSession: BookedSession)
}

@Dao
interface JoinedCourseDao {
    @Query("SELECT * FROM joined_courses")
    fun getAllJoinedCourses(): Flow<List<JoinedCourse>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun joinCourse(course: JoinedCourse)
}

@Dao
interface CommunicationActionPlanDao {
    @Query("SELECT * FROM communication_action_plans ORDER BY id DESC")
    fun getAllActionPlans(): Flow<List<CommunicationActionPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionPlan(plan: CommunicationActionPlan)

    @Update
    suspend fun updateActionPlan(plan: CommunicationActionPlan)

    @Query("DELETE FROM communication_action_plans WHERE id = :id")
    suspend fun deleteActionPlanById(id: Int)
}

@Dao
interface PeerFeedbackDao {
    @Query("SELECT * FROM peer_feedback_assessments ORDER BY id DESC")
    fun getAllPeerFeedback(): Flow<List<PeerFeedback>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeerFeedback(feedback: PeerFeedback)

    @Query("DELETE FROM peer_feedback_assessments WHERE id = :id")
    suspend fun deletePeerFeedbackById(id: Int)
}

@Dao
interface FirebaseGoalDao {
    @Query("SELECT * FROM firebase_communication_goals ORDER BY timestamp DESC")
    fun getAllFirebaseGoals(): Flow<List<FirebaseGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFirebaseGoal(goal: FirebaseGoal)

    @Query("DELETE FROM firebase_communication_goals WHERE id = :id")
    suspend fun deleteFirebaseGoalById(id: Int)
}

@Dao
interface RoleplayHistoryDao {
    @Query("SELECT * FROM roleplay_histories ORDER BY timestamp DESC")
    fun getAllRoleplayHistories(): Flow<List<RoleplayHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoleplayHistory(history: RoleplayHistory)

    @Query("DELETE FROM roleplay_histories WHERE id = :id")
    suspend fun deleteRoleplayHistoryById(id: Int)
}

@Database(entities = [UserProfile::class, ChatMessage::class, BookedSession::class, JoinedCourse::class, CommunicationActionPlan::class, PeerFeedback::class, FirebaseGoal::class, RoleplayHistory::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val userProfileDao: UserProfileDao
    abstract val chatMessageDao: ChatMessageDao
    abstract val bookedSessionDao: BookedSessionDao
    abstract val joinedCourseDao: JoinedCourseDao
    abstract val communicationActionPlanDao: CommunicationActionPlanDao
    abstract val peerFeedbackDao: PeerFeedbackDao
    abstract val firebaseGoalDao: FirebaseGoalDao
    abstract val roleplayHistoryDao: RoleplayHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "commcore_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
