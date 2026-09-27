package com.nova.healthconnect.health

import java.time.Instant
import java.time.Duration

// ---------------------------------------------------------------------------
// Health data types NOVA can read from Health Connect.
// Each entry maps to exactly one Health Connect record class.
// ---------------------------------------------------------------------------
enum class NovaHealthDataType {
    // Vitals
    STEPS,
    SLEEP,
    HEART_RATE,
    RESTING_HEART_RATE,
    HEART_RATE_VARIABILITY,
    BLOOD_PRESSURE,
    BLOOD_GLUCOSE,
    OXYGEN_SATURATION,
    BODY_TEMPERATURE,
    SKIN_TEMPERATURE,
    RESPIRATORY_RATE,

    // Activity
    ACTIVE_CALORIES,
    TOTAL_CALORIES,
    DISTANCE,
    EXERCISE,

    // Body measurements
    WEIGHT,
    HEIGHT,
    BODY_FAT,
    LEAN_BODY_MASS,
    BODY_WATER_MASS,
    BONE_MASS,
    BASAL_METABOLIC_RATE,
    VO2_MAX,

    // Nutrition & hydration
    NUTRITION,
    HYDRATION,

    // Reproductive health
    MENSTRUATION_FLOW,
    MENSTRUATION_PERIOD,
    INTERMENSTRUAL_BLEEDING,
    OVULATION_TEST,
    CERVICAL_MUCUS,
    SEXUAL_ACTIVITY,
    BASAL_BODY_TEMPERATURE
}

// ---------------------------------------------------------------------------
// Per-record provenance — who wrote the record and on what device.
// Used by the NOVA backend to deduplicate and attribute data correctly.
// ---------------------------------------------------------------------------
data class NovaRecordOrigin(
    val sourceApp: String,           // e.g. "com.samsung.health"
    val recordId: String,            // Health Connect internal UUID
    val recordingMethod: String,     // "actively_recorded" | "automatically_recorded" | "manual_entry"
    val deviceManufacturer: String?,
    val deviceModel: String?,
    val lastModifiedAt: Instant?
)

// ---------------------------------------------------------------------------
// Typed data classes — one per HC record category.
// Written fresh; the field names follow NOVA conventions.
// ---------------------------------------------------------------------------

data class NovaStepEntry(
    val count: Long,
    val windowStart: Instant,
    val windowEnd: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaSleepStage(
    val stage: String,      // "awake" | "light" | "deep" | "rem" | "sleeping" | "out_of_bed" | "unknown"
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Long
)

data class NovaSleepSession(
    val sessionStart: Instant,
    val sessionEnd: Instant,
    val totalDurationMinutes: Long,
    val stages: List<NovaSleepStage>,
    val origin: NovaRecordOrigin? = null
)

data class NovaHeartRateSample(
    val bpm: Long,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaHeartRateVariabilityEntry(
    val rmssdMillis: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaRestingHeartRateEntry(
    val bpm: Long,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBloodPressureEntry(
    val systolicMmHg: Double,
    val diastolicMmHg: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBloodGlucoseEntry(
    val mmolPerLiter: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaOxygenSaturationEntry(
    val percentageSpo2: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBodyTemperatureEntry(
    val celsius: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaSkinTemperatureEntry(
    val deltaCelsius: Double,
    val baselineCelsius: Double?,
    val measurementLocation: Int,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaRespiratoryRateEntry(
    val breathsPerMinute: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaActiveCaloriesEntry(
    val kilocalories: Double,
    val windowStart: Instant,
    val windowEnd: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaTotalCaloriesEntry(
    val kilocalories: Double,
    val windowStart: Instant,
    val windowEnd: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaDistanceEntry(
    val meters: Double,
    val windowStart: Instant,
    val windowEnd: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaExerciseSession(
    val exerciseType: String,           // e.g. "running", "walking", "cycling"
    val title: String?,
    val sessionStart: Instant,
    val sessionEnd: Instant,
    val durationMinutes: Long,
    val distanceMeters: Double?,
    val stepCount: Long?,
    val origin: NovaRecordOrigin? = null
)

data class NovaWeightEntry(
    val kilograms: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaHeightEntry(
    val meters: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBodyFatEntry(
    val percentage: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaLeanBodyMassEntry(
    val kilograms: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBodyWaterMassEntry(
    val kilograms: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBoneMassEntry(
    val kilograms: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBasalMetabolicRateEntry(
    val kilocaloriesPerDay: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaVo2MaxEntry(
    val mlPerKgPerMinute: Double,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaNutritionEntry(
    val mealName: String?,
    val windowStart: Instant,
    val windowEnd: Instant,
    val energyKilocalories: Double?,
    val proteinGrams: Double?,
    val carbohydratesGrams: Double?,
    val fatGrams: Double?,
    val sugarGrams: Double?,
    val sodiumGrams: Double?,
    val fiberGrams: Double?,
    val origin: NovaRecordOrigin? = null
)

data class NovaHydrationEntry(
    val liters: Double,
    val windowStart: Instant,
    val windowEnd: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaMenstruationFlowEntry(
    val flowLevel: String, // "unknown" | "light" | "medium" | "heavy"
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaMenstruationPeriodEntry(
    val periodStart: Instant,
    val periodEnd: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaIntermenstrualBleedingEntry(
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaOvulationTestEntry(
    val result: String, // "positive" | "high" | "negative" | "inconclusive"
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaCervicalMucusEntry(
    val appearance: String, // "dry" | "sticky" | "creamy" | "watery" | "egg_white" | "unusual"
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaSexualActivityEntry(
    val protectionUsed: Boolean?,
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

data class NovaBasalBodyTemperatureEntry(
    val celsius: Double,
    val bodyLocation: String, // "unknown" | "armpit" | "finger" | "forehead" | "mouth" | "rectum" | "temporal_artery" | "toe" | "ear" | "wrist" | "vagina"
    val measuredAt: Instant,
    val origin: NovaRecordOrigin? = null
)

// ---------------------------------------------------------------------------
// Top-level container — all health data read in one sync pass.
// Lists are empty (not null) when the type has no records in the window.
// ---------------------------------------------------------------------------
data class NovaHealthData(
    val readWindowStart: Instant,
    val readWindowEnd: Instant,
    val steps: List<NovaStepEntry> = emptyList(),
    val sleep: List<NovaSleepSession> = emptyList(),
    val heartRate: List<NovaHeartRateSample> = emptyList(),
    val restingHeartRate: List<NovaRestingHeartRateEntry> = emptyList(),
    val heartRateVariability: List<NovaHeartRateVariabilityEntry> = emptyList(),
    val bloodPressure: List<NovaBloodPressureEntry> = emptyList(),
    val bloodGlucose: List<NovaBloodGlucoseEntry> = emptyList(),
    val oxygenSaturation: List<NovaOxygenSaturationEntry> = emptyList(),
    val bodyTemperature: List<NovaBodyTemperatureEntry> = emptyList(),
    val skinTemperature: List<NovaSkinTemperatureEntry> = emptyList(),
    val respiratoryRate: List<NovaRespiratoryRateEntry> = emptyList(),
    val activeCalories: List<NovaActiveCaloriesEntry> = emptyList(),
    val totalCalories: List<NovaTotalCaloriesEntry> = emptyList(),
    val distance: List<NovaDistanceEntry> = emptyList(),
    val exercise: List<NovaExerciseSession> = emptyList(),
    val weight: List<NovaWeightEntry> = emptyList(),
    val height: List<NovaHeightEntry> = emptyList(),
    val bodyFat: List<NovaBodyFatEntry> = emptyList(),
    val leanBodyMass: List<NovaLeanBodyMassEntry> = emptyList(),
    val bodyWaterMass: List<NovaBodyWaterMassEntry> = emptyList(),
    val boneMass: List<NovaBoneMassEntry> = emptyList(),
    val basalMetabolicRate: List<NovaBasalMetabolicRateEntry> = emptyList(),
    val vo2Max: List<NovaVo2MaxEntry> = emptyList(),
    val nutrition: List<NovaNutritionEntry> = emptyList(),
    val hydration: List<NovaHydrationEntry> = emptyList(),
    val menstruationFlow: List<NovaMenstruationFlowEntry> = emptyList(),
    val menstruationPeriod: List<NovaMenstruationPeriodEntry> = emptyList(),
    val intermenstrualBleeding: List<NovaIntermenstrualBleedingEntry> = emptyList(),
    val ovulationTest: List<NovaOvulationTestEntry> = emptyList(),
    val cervicalMucus: List<NovaCervicalMucusEntry> = emptyList(),
    val sexualActivity: List<NovaSexualActivityEntry> = emptyList(),
    val basalBodyTemperature: List<NovaBasalBodyTemperatureEntry> = emptyList()
) {
    /** Total number of records across all types. */
    fun totalRecords(): Int =
        steps.size + sleep.size + heartRate.size + restingHeartRate.size +
        heartRateVariability.size + bloodPressure.size + bloodGlucose.size +
        oxygenSaturation.size + bodyTemperature.size + skinTemperature.size +
        respiratoryRate.size + activeCalories.size + totalCalories.size +
        distance.size + exercise.size + weight.size + height.size +
        bodyFat.size + leanBodyMass.size + bodyWaterMass.size + boneMass.size +
        basalMetabolicRate.size + vo2Max.size + nutrition.size + hydration.size +
        menstruationFlow.size + menstruationPeriod.size + intermenstrualBleeding.size +
        ovulationTest.size + cervicalMucus.size + sexualActivity.size +
        basalBodyTemperature.size

    /** True when no records at all were returned for the query window. */
    fun isEmpty(): Boolean = totalRecords() == 0
}
