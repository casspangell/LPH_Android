package org.lovepeaceharmony.androidapp.model

data class StreakMilestone(
    var current_streak: Long,
    var last_day_chanted: String,
    var longest_streak: Long
)
