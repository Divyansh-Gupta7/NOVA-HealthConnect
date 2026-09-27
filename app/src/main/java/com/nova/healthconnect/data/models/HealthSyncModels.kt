package com.nova.healthconnect.data.models

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------------------
// Rich health payload sent to the NOVA backend.
// Each list contains all records of that type from the sync window.
// Empty list means the type was not available / not granted — never null.
// ---------------------------------------------------------------------------

data class NovaHealthSyncRequest(
    @SerializedName("schema_version") val schemaVersion: Int = 2,
    @SerializedName("sync_timestamp") val syncTimestamp: String,
    @SerializedName("window_start") val windowStart: String,
    @SerializedName("window_end") val windowEnd: String,
    @SerializedName("device_info") val deviceInfo: String = "Android Health Connect",
    @SerializedName("sync_type") val syncType: String = "manual", // "manual" | "interval" | "scheduled"
    @SerializedName("total_records") val totalRecords: Int,
    @SerializedName("metrics") val metrics: NovaHealthMetrics
)

data class NovaHealthMetrics(
    // Vitals
    @SerializedName("steps") val steps: List<StepMetric> = emptyList(),
    @SerializedName("sleep") val sleep: List<SleepMetric> = emptyList(),
    @SerializedName("heart_rate") val heartRate: List<HeartRateMetric> = emptyList(),
    @SerializedName("resting_heart_rate") val restingHeartRate: List<SingleValueMetric> = emptyList(),
    @SerializedName("heart_rate_variability") val heartRateVariability: List<SingleValueMetric> = emptyList(),
    @SerializedName("blood_pressure") val bloodPressure: List<BloodPressureMetric> = emptyList(),
    @SerializedName("blood_glucose") val bloodGlucose: List<SingleValueMetric> = emptyList(),
    @SerializedName("oxygen_saturation") val oxygenSaturation: List<SingleValueMetric> = emptyList(),
    @SerializedName("body_temperature") val bodyTemperature: List<SingleValueMetric> = emptyList(),
    @SerializedName("skin_temperature") val skinTemperature: List<SkinTemperatureMetric> = emptyList(),
    @SerializedName("respiratory_rate") val respiratoryRate: List<SingleValueMetric> = emptyList(),
    // Activity
    @SerializedName("active_calories") val activeCalories: List<WindowedValueMetric> = emptyList(),
    @SerializedName("total_calories") val totalCalories: List<WindowedValueMetric> = emptyList(),
    @SerializedName("distance") val distance: List<WindowedValueMetric> = emptyList(),
    @SerializedName("exercise") val exercise: List<ExerciseMetric> = emptyList(),
    // Body measurements
    @SerializedName("weight") val weight: List<SingleValueMetric> = emptyList(),
    @SerializedName("height") val height: List<SingleValueMetric> = emptyList(),
    @SerializedName("body_fat") val bodyFat: List<SingleValueMetric> = emptyList(),
    @SerializedName("lean_body_mass") val leanBodyMass: List<SingleValueMetric> = emptyList(),
    @SerializedName("bone_mass") val boneMass: List<SingleValueMetric> = emptyList(),
    @SerializedName("basal_metabolic_rate") val basalMetabolicRate: List<SingleValueMetric> = emptyList(),
    @SerializedName("vo2_max") val vo2Max: List<SingleValueMetric> = emptyList(),
    // Nutrition & hydration
    @SerializedName("nutrition") val nutrition: List<NutritionMetric> = emptyList(),
    @SerializedName("hydration") val hydration: List<WindowedValueMetric> = emptyList(),
    // Reproductive health
    @SerializedName("menstruation_flow") val menstruationFlow: List<LabeledMetric> = emptyList(),
    @SerializedName("menstruation_period") val menstruationPeriod: List<WindowMetric> = emptyList(),
    @SerializedName("intermenstrual_bleeding") val intermenstrualBleeding: List<TimestampMetric> = emptyList(),
    @SerializedName("ovulation_test") val ovulationTest: List<LabeledMetric> = emptyList(),
    @SerializedName("cervical_mucus") val cervicalMucus: List<LabeledMetric> = emptyList(),
    @SerializedName("sexual_activity") val sexualActivity: List<SexualActivityMetric> = emptyList(),
    @SerializedName("basal_body_temperature") val basalBodyTemperature: List<BasalBodyTempMetric> = emptyList()
)

// ---------------------------------------------------------------------------
// Individual metric shapes
// ---------------------------------------------------------------------------

data class StepMetric(
    @SerializedName("count") val count: Long,
    @SerializedName("window_start") val windowStart: String,
    @SerializedName("window_end") val windowEnd: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class SleepStageMetric(
    @SerializedName("stage") val stage: String,
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String,
    @SerializedName("duration_minutes") val durationMinutes: Long
)

data class SleepMetric(
    @SerializedName("session_start") val sessionStart: String,
    @SerializedName("session_end") val sessionEnd: String,
    @SerializedName("total_duration_minutes") val totalDurationMinutes: Long,
    @SerializedName("stages") val stages: List<SleepStageMetric> = emptyList(),
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class HeartRateMetric(
    @SerializedName("bpm") val bpm: Long,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class BloodPressureMetric(
    @SerializedName("systolic_mmhg") val systolicMmHg: Double,
    @SerializedName("diastolic_mmhg") val diastolicMmHg: Double,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class SkinTemperatureMetric(
    @SerializedName("delta_celsius") val deltaCelsius: Double,
    @SerializedName("baseline_celsius") val baselineCelsius: Double?,
    @SerializedName("location") val location: Int,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class ExerciseMetric(
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String?,
    @SerializedName("session_start") val sessionStart: String,
    @SerializedName("session_end") val sessionEnd: String,
    @SerializedName("duration_minutes") val durationMinutes: Long,
    @SerializedName("distance_meters") val distanceMeters: Double?,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class NutritionMetric(
    @SerializedName("meal_name") val mealName: String?,
    @SerializedName("window_start") val windowStart: String,
    @SerializedName("window_end") val windowEnd: String,
    @SerializedName("energy_kcal") val energyKcal: Double?,
    @SerializedName("protein_g") val proteinG: Double?,
    @SerializedName("carbs_g") val carbsG: Double?,
    @SerializedName("fat_g") val fatG: Double?,
    @SerializedName("sugar_g") val sugarG: Double?,
    @SerializedName("sodium_g") val sodiumG: Double?,
    @SerializedName("fiber_g") val fiberG: Double?,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

// Simple reusable shapes
data class SingleValueMetric(
    @SerializedName("value") val value: Double,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class WindowedValueMetric(
    @SerializedName("value") val value: Double,
    @SerializedName("window_start") val windowStart: String,
    @SerializedName("window_end") val windowEnd: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class LabeledMetric(
    @SerializedName("label") val label: String,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class TimestampMetric(
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class WindowMetric(
    @SerializedName("start") val start: String,
    @SerializedName("end") val end: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class SexualActivityMetric(
    @SerializedName("protection_used") val protectionUsed: Boolean?,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

data class BasalBodyTempMetric(
    @SerializedName("celsius") val celsius: Double,
    @SerializedName("location") val location: String,
    @SerializedName("measured_at") val measuredAt: String,
    @SerializedName("source_app") val sourceApp: String? = null,
    @SerializedName("record_id") val recordId: String? = null
)

// ---------------------------------------------------------------------------
// Backend Response Models (POST /api/health-connect/sync)
// ---------------------------------------------------------------------------

data class NovaHealthSyncResponse(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("data") val data: SyncResultData? = null,
    @SerializedName("error") val error: ApiErrorData? = null
)

data class SyncResultData(
    @SerializedName("syncId") val syncId: Long = 0,
    @SerializedName("recordsProcessed") val recordsProcessed: Int = 0,
    @SerializedName("recordsInserted") val recordsInserted: Int = 0,
    @SerializedName("recordsDeduplicated") val recordsDeduplicated: Int = 0,
    @SerializedName("syncTimestamp") val syncTimestamp: String = "",
    @SerializedName("status") val status: String = ""
)

data class ApiErrorData(
    @SerializedName("code") val code: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("field") val field: String? = null
)

// ---------------------------------------------------------------------------
// Sync Classification Exceptions
// ---------------------------------------------------------------------------

class SyncValidationException(message: String) : Exception(message)
class SyncAuthException(message: String) : Exception(message)
class SyncServerException(message: String) : Exception(message)

// ---------------------------------------------------------------------------
// Legacy model — kept for backward compat with dashboard calculations
// ---------------------------------------------------------------------------
data class HealthMetricsPayload(
    @SerializedName("steps") val steps: Long = 0,
    @SerializedName("sleepDurationHours") val sleepDurationHours: Double = 0.0,
    @SerializedName("sleepStageDeepHours") val sleepStageDeepHours: Double = 0.0,
    @SerializedName("restingHeartRate") val restingHeartRate: Double = 0.0,
    @SerializedName("avgHeartRate") val avgHeartRate: Double = 0.0,
    @SerializedName("activeCaloriesBurned") val activeCaloriesBurned: Double = 0.0,
    @SerializedName("exerciseSessionMinutes") val exerciseSessionMinutes: Int = 0
)

data class HealthSyncRequest(
    @SerializedName("syncTimestamp") val syncTimestamp: String,
    @SerializedName("date") val date: String,
    @SerializedName("deviceInfo") val deviceInfo: String = "Android Health Connect",
    @SerializedName("metrics") val metrics: HealthMetricsPayload
)

data class HealthSyncResponse(
    val status: String = "success",
    val message: String = "Health data synced successfully",
    val recordsProcessed: Int = 0,
    val syncedAt: String = ""
)

