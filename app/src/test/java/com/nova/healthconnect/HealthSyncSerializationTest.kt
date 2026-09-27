package com.nova.healthconnect

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nova.healthconnect.data.models.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class HealthSyncSerializationTest {

    private val gson = Gson()

    @Test
    fun testNovaHealthSyncRequest_serializesSchemaVersion2AndRecordId() {
        val request = NovaHealthSyncRequest(
            schemaVersion = 2,
            syncTimestamp = "2026-09-27T12:00:00Z",
            windowStart = "2026-09-27T00:00:00Z",
            windowEnd = "2026-09-27T12:00:00Z",
            syncType = "manual",
            totalRecords = 3,
            deviceInfo = "Android Health Connect Test",
            metrics = NovaHealthMetrics(
                steps = listOf(
                    StepMetric(
                        count = 2500,
                        windowStart = "2026-09-27T08:00:00Z",
                        windowEnd = "2026-09-27T08:45:00Z",
                        sourceApp = "com.google.android.apps.fitness",
                        recordId = "step-rec-001"
                    )
                ),
                heartRate = listOf(
                    HeartRateMetric(
                        bpm = 72L,
                        measuredAt = "2026-09-27T08:30:00Z",
                        sourceApp = "com.samsung.health",
                        recordId = "hr-rec-002"
                    )
                ),
                bloodPressure = listOf(
                    BloodPressureMetric(
                        systolicMmHg = 120.0,
                        diastolicMmHg = 80.0,
                        measuredAt = "2026-09-27T09:00:00Z",
                        sourceApp = "com.omron.fitness",
                        recordId = "bp-rec-003"
                    )
                )
            )
        )

        val jsonString = gson.toJson(request)
        val jsonObject = gson.fromJson(jsonString, JsonObject::class.java)

        // 1. Verify schema_version = 2
        assertEquals(2, jsonObject.get("schema_version").asInt)
        assertEquals("manual", jsonObject.get("sync_type").asString)
        assertEquals("2026-09-27T12:00:00Z", jsonObject.get("sync_timestamp").asString)
        assertEquals("2026-09-27T00:00:00Z", jsonObject.get("window_start").asString)
        assertEquals("2026-09-27T12:00:00Z", jsonObject.get("window_end").asString)
        assertEquals(3, jsonObject.get("total_records").asInt)

        // 2. Verify metric records contain record_id
        val metricsObj = jsonObject.getAsJsonObject("metrics")
        assertNotNull(metricsObj)

        val stepsArray = metricsObj.getAsJsonArray("steps")
        assertEquals(1, stepsArray.size())
        val stepItem = stepsArray.get(0).asJsonObject
        assertEquals(2500, stepItem.get("count").asLong)
        assertEquals("step-rec-001", stepItem.get("record_id").asString)
        assertEquals("2026-09-27T08:00:00Z", stepItem.get("window_start").asString)

        val hrArray = metricsObj.getAsJsonArray("heart_rate")
        assertEquals(1, hrArray.size())
        val hrItem = hrArray.get(0).asJsonObject
        assertEquals(72.0, hrItem.get("bpm").asDouble, 0.001)
        assertEquals("hr-rec-002", hrItem.get("record_id").asString)

        val bpArray = metricsObj.getAsJsonArray("blood_pressure")
        assertEquals(1, bpArray.size())
        val bpItem = bpArray.get(0).asJsonObject
        assertEquals(120.0, bpItem.get("systolic_mmhg").asDouble, 0.001)
        assertEquals(80.0, bpItem.get("diastolic_mmhg").asDouble, 0.001)
        assertEquals("bp-rec-003", bpItem.get("record_id").asString)
    }

    @Test
    fun testNovaHealthSyncResponse_deserializesSuccessResponse() {
        val json = """
            {
                "success": true,
                "data": {
                    "syncId": 42,
                    "recordsProcessed": 15,
                    "recordsInserted": 10,
                    "recordsDeduplicated": 5,
                    "syncTimestamp": "2026-09-27T12:00:00Z",
                    "status": "COMPLETED"
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, NovaHealthSyncResponse::class.java)
        assertTrue(response.success)
        assertNotNull(response.data)
        assertEquals(42L, response.data?.syncId)
        assertEquals(15, response.data?.recordsProcessed)
        assertEquals(10, response.data?.recordsInserted)
        assertEquals(5, response.data?.recordsDeduplicated)
        assertEquals("COMPLETED", response.data?.status)
    }

    @Test
    fun testNovaHealthSyncResponse_deserializesErrorResponse() {
        val json = """
            {
                "success": false,
                "error": {
                    "code": "VALIDATION_ERROR",
                    "message": "syncTimestamp is required and must be a valid ISO 8601 string",
                    "field": "syncTimestamp"
                }
            }
        """.trimIndent()

        val response = gson.fromJson(json, NovaHealthSyncResponse::class.java)
        assertFalse(response.success)
        assertNotNull(response.error)
        assertEquals("VALIDATION_ERROR", response.error?.code)
        assertEquals("syncTimestamp", response.error?.field)
        assertTrue(response.error?.message?.contains("ISO 8601") == true)
    }

    @Test
    fun testIncrementalSyncSafetyMargin() {
        val lastSyncTime = Instant.parse("2026-09-27T10:30:00Z")
        val fiveMinutesBefore = lastSyncTime.minus(Duration.ofMinutes(5))

        assertEquals(Instant.parse("2026-09-27T10:25:00Z"), fiveMinutesBefore)
    }

    @Test
    fun testIncrementalSyncMidnightFallbackWhenNoPreviousSync() {
        val lastSyncTime: Instant? = null
        val now = Instant.parse("2026-09-27T14:00:00Z")
        val midnight = now.atZone(ZoneId.of("UTC"))
            .toLocalDate()
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()

        val sinceTime = lastSyncTime?.minus(Duration.ofMinutes(5))
        val windowStart = sinceTime ?: midnight

        assertEquals(Instant.parse("2026-09-27T00:00:00Z"), windowStart)
    }

    @Test
    fun testSyncExceptionsClassification() {
        val validationEx: Exception = SyncValidationException("Invalid schema_version")
        val authEx: Exception = SyncAuthException("Unauthorized token")
        val serverEx: Exception = SyncServerException("Internal server 500")

        assertTrue(validationEx is SyncValidationException)
        assertTrue(authEx is SyncAuthException)
        assertTrue(serverEx is SyncServerException)
        assertEquals("Invalid schema_version", validationEx.message)
    }
}

