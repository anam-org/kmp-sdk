package ai.anam.lab.client.core.client

import ai.anam.lab.Environment

/**
 * Configuration for the API endpoint used across the app and SDK.
 */
data class ApiConfig(val environment: Environment = Environment.Production)
