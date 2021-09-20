package org.lovepeaceharmony.android.app.utility

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import org.lovepeaceharmony.android.app.model.ChantingMilestone
import org.lovepeaceharmony.android.app.model.StreakMilestone

private const val CHANTING_MILESTONES = "chanting_milestone"
private const val DAY_CHANTED = "day_chanted"
private const val SECONDS = "seconds"

private const val CURRENT_CHANTING_STREAK = "current_chanting_streak"
private const val CURRENT_STREAK = "current_streak"
private const val LAST_DAY_CHANTED = "last_day_chanted"
private const val LONGEST_STREAK = "longest_streak"

private const val TOTAL_SECS_CHANTED = "total_secs_chanted"

private val Firebase.db
    get() = auth.currentUser?.uid?.let { database.getReference("love-peace-harmony").child(it) }


data class ChantData(
    val chantingMilestone: List<ChantingMilestone>,
    val streakMilestone: StreakMilestone,
    val totalSecondsChanted: Long
)

/**
 * To get db instance for read and write
 * val db = Firebase.database.getReference("love-peace-harmony").child(Firebase.auth.currentUser.uid)
 *
 *
 */
@ExperimentalCoroutinesApi
val Firebase.chantingMilestones
    get() = callbackFlow {
        db?.let { db ->
            db.get().addOnSuccessListener { snapShot ->
                LPHLog.d("The data is $snapShot")

                val milestones = snapShot.value?.let {
                    ((it as Map<*, *>)[CHANTING_MILESTONES] as Map<*, *>).values
                }?.filterNotNull()?.map {
                    it as Map<*, *>
                    return@map ChantingMilestone(
                        it["day_chanted"] as String, it["seconds"] as Long
                    )
                } ?: emptyList()

                trySend(DataState.Success(milestones))
            }
        } ?: trySend(DataState.Error("Must be signed in"))
        awaitClose()
    }

@ExperimentalCoroutinesApi
val Firebase.currentChantingStreak
    get() = callbackFlow {
        getData(CURRENT_CHANTING_STREAK) { state ->
            if (state is DataState.Success) {
                val streak = if (state.data.containsKey(CURRENT_STREAK)
                    && state.data.containsKey(LAST_DAY_CHANTED)
                    && state.data.containsKey(LONGEST_STREAK)
                ) {
                    StreakMilestone(
                        state.data[CURRENT_STREAK] as Long,
                        state.data[LAST_DAY_CHANTED] as String,
                        state.data[LONGEST_STREAK] as Long
                    )
                } else null
                trySend(DataState.Success(streak))
            } else trySend(DataState.Error((state as DataState.Error).msg))
        }
        awaitClose()
    }

@ExperimentalCoroutinesApi
val Firebase.totalSeconds
    get() = callbackFlow {
        db?.let { db ->
            db.get().addOnSuccessListener { snapShot ->
                LPHLog.d("The data is $snapShot")
                val map = snapShot.value as? HashMap<*, *>
                val data = map?.getOrDefault(TOTAL_SECS_CHANTED, 0L) as? Long ?: 0L
                trySend(DataState.Success(data))
            }
        } ?: trySend(DataState.Error("Must be signed in"))
        awaitClose()
    }

@ExperimentalCoroutinesApi
val Firebase.chantData
    get() = callbackFlow {
        db?.let { db ->
            db.get().addOnSuccessListener { snapShot ->
                LPHLog.d("The data is $snapShot")
                val streakMilestone = (((snapShot.value as? HashMap<*, *>)?.getOrDefault(
                    CURRENT_CHANTING_STREAK,
                    null
                ) as? HashMap<*, *>)
                    ?: hashMapOf<Any, Any>()).run {
                    StreakMilestone(
                        getOrDefault(CURRENT_STREAK, 0L) as Long,
                        getOrDefault(LAST_DAY_CHANTED, "") as String,
                        getOrDefault(LONGEST_STREAK, 0L) as Long
                    )
                }

                val milestones = snapShot.value?.let {
                    ((it as Map<*, *>)[CHANTING_MILESTONES] as Map<*, *>).values
                }?.filterNotNull()?.map {
                    it as Map<*, *>
                    return@map ChantingMilestone(it[DAY_CHANTED] as String, it[SECONDS] as Long)
                } ?: emptyList()

                val map = snapShot.value as? HashMap<*, *>
                val totalSeconds = map?.getOrDefault(TOTAL_SECS_CHANTED, 0L) as? Long ?: 0L

                val chantData = ChantData(
                    chantingMilestone = milestones,
                    streakMilestone = streakMilestone,
                    totalSecondsChanted = totalSeconds
                )
                trySend(DataState.Success(chantData))
            }
        } ?: trySend(DataState.Error("Must be signed in"))
        awaitClose()
    }

@ExperimentalCoroutinesApi
fun Firebase.updateMilestonesState(chantingMilestone: ChantingMilestone) =
    callbackFlow {
        db?.let { db ->
            db.child(CHANTING_MILESTONES).child(chantingMilestone.day_chanted)
                .setValue(chantingMilestone)
                .addOnSuccessListener { trySend(DataState.Success(it)) }
                .addOnFailureListener { trySend(DataState.Error("Something went wrong")) }
        } ?: trySend(DataState.Error("Must be signed in"))
        awaitClose()
    }

@ExperimentalCoroutinesApi
fun Firebase.updateStreakState(streakMilestone: StreakMilestone) = callbackFlow {
    db?.let { db ->
        db.child(CURRENT_CHANTING_STREAK).setValue(streakMilestone)
            .addOnSuccessListener { trySend(DataState.Success(it)) }
            .addOnFailureListener { trySend(DataState.Error("Something went wrong")) }
    } ?: trySend(DataState.Error("Must be signed in"))
    awaitClose()
}

@ExperimentalCoroutinesApi
fun Firebase.updateTotalSeconds(newTotal: Long) = callbackFlow {
    Log.d("RepoOperation", "updateTotalSeconds: $newTotal")
    db?.let { db ->
        db.updateChildren(mapOf(TOTAL_SECS_CHANTED to newTotal))
            .addOnSuccessListener { trySend(DataState.Success(it)) }
            .addOnFailureListener { trySend(DataState.Error("Something went wrong")) }
    } ?: trySend(DataState.Error("Must be signed in"))
    awaitClose()
}

private fun Firebase.getData(path: String, listener: (DataState<HashMap<*, *>>) -> Unit) {
    db?.let { db ->
        db.get().addOnSuccessListener { snapShot ->
            LPHLog.d("The data is $snapShot")
            val data =
                ((snapShot.value as? HashMap<*, *>)?.getOrDefault(path, null) as? HashMap<*, *>)
                    ?: hashMapOf<Any, Any>()
            listener.invoke(DataState.Success(data))
        }
    } ?: listener.invoke(DataState.Error("Must be signed in"))
}

@ExperimentalCoroutinesApi
fun Firebase.reset() = callbackFlow {
    db?.let { db ->
        db.removeValue().addOnSuccessListener {
            trySend(DataState.Success(Unit))
        }.addOnFailureListener { trySend(DataState.Error("")) }
    }
    awaitClose()
}