package ai.anam.lab

/**
 * Handler for tool call lifecycle events. Register a handler for a specific tool name via
 * [Session.registerToolCallHandler].
 *
 * All callbacks are optional — only implement the ones you need.
 *
 * @property onStart Invoked when the tool call starts. For client-type tools, the return value is used as the tool
 * result and the call is automatically completed. Return `null` for no result.
 * @property onComplete Invoked when the tool call has completed successfully.
 * @property onFail Invoked when the tool call has failed.
 */
public class ToolCallHandler(
    public val onStart: (suspend (payload: ToolCallStartedPayload) -> String?)? = null,
    public val onComplete: (suspend (payload: ToolCallCompletedPayload) -> Unit)? = null,
    public val onFail: (suspend (payload: ToolCallFailedPayload) -> Unit)? = null,
)
