package ai.anam.lab.fakes

import ai.anam.lab.api.DataChannelMessagePayload
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal fun toolCallStartedMessage(
    eventUid: String = "event-789",
    sessionId: String = "session-123",
    toolCallId: String = "tc-001",
    toolName: String = "redirect",
    toolType: String = "client",
    toolSubtype: String? = null,
    arguments: JsonObject = buildJsonObject { put("url", "https://example.com") },
    timestamp: String = "2024-01-01T00:00:00Z",
    timestampUserAction: String = "2024-01-01T00:00:01Z",
    userActionCorrelationId: String = "corr-001",
    usedOutsideEngine: Boolean = true,
) = DataChannelMessagePayload.ToolCallStartedMessage(
    eventUid = eventUid,
    sessionId = sessionId,
    toolCallId = toolCallId,
    toolName = toolName,
    toolType = toolType,
    toolSubtype = toolSubtype,
    arguments = arguments,
    timestamp = timestamp,
    timestampUserAction = timestampUserAction,
    userActionCorrelationId = userActionCorrelationId,
    usedOutsideEngine = usedOutsideEngine,
)

internal fun toolCallCompletedMessage(
    eventUid: String = "event-789",
    sessionId: String = "session-123",
    toolCallId: String = "tc-001",
    toolName: String = "redirect",
    toolType: String = "server",
    toolSubtype: String? = null,
    arguments: JsonObject = buildJsonObject { put("url", "https://example.com") },
    timestamp: String = "2024-01-01T00:00:00Z",
    timestampUserAction: String = "2024-01-01T00:00:01Z",
    userActionCorrelationId: String = "corr-001",
    usedOutsideEngine: Boolean = true,
    result: JsonElement = JsonPrimitive("success"),
    documentsAccessed: List<String>? = null,
) = DataChannelMessagePayload.ToolCallCompletedMessage(
    eventUid = eventUid,
    sessionId = sessionId,
    toolCallId = toolCallId,
    toolName = toolName,
    toolType = toolType,
    toolSubtype = toolSubtype,
    arguments = arguments,
    timestamp = timestamp,
    timestampUserAction = timestampUserAction,
    userActionCorrelationId = userActionCorrelationId,
    usedOutsideEngine = usedOutsideEngine,
    result = result,
    documentsAccessed = documentsAccessed,
)

internal fun toolCallFailedMessage(
    eventUid: String = "event-789",
    sessionId: String = "session-123",
    toolCallId: String = "tc-001",
    toolName: String = "redirect",
    toolType: String = "server",
    toolSubtype: String? = null,
    arguments: JsonObject = buildJsonObject { put("url", "https://example.com") },
    timestamp: String = "2024-01-01T00:00:00Z",
    timestampUserAction: String = "2024-01-01T00:00:01Z",
    userActionCorrelationId: String = "corr-001",
    usedOutsideEngine: Boolean = true,
    errorMessage: String = "Tool execution timed out",
) = DataChannelMessagePayload.ToolCallFailedMessage(
    eventUid = eventUid,
    sessionId = sessionId,
    toolCallId = toolCallId,
    toolName = toolName,
    toolType = toolType,
    toolSubtype = toolSubtype,
    arguments = arguments,
    timestamp = timestamp,
    timestampUserAction = timestampUserAction,
    userActionCorrelationId = userActionCorrelationId,
    usedOutsideEngine = usedOutsideEngine,
    errorMessage = errorMessage,
)
