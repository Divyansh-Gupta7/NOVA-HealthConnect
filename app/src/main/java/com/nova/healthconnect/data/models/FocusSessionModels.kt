package com.nova.healthconnect.data.models

import com.google.gson.annotations.SerializedName

data class FocusSessionRequest(
    @SerializedName("type") val type: String = "focus",
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("distractionsLogged") val distractionsLogged: Int = 0,
    @SerializedName("completed") val completed: Boolean = true,
    @SerializedName("soundscape") val soundscape: String = "Binaural 40Hz",
    @SerializedName("notes") val notes: String = ""
)

data class FocusSessionResponse(
    val status: String = "success",
    val message: String = "Session recorded",
    val streak: Int = 3
)
