package ai.anam.lab.client.core.common

/**
 * Thrown when an API request fails due to missing or invalid authentication credentials
 * (e.g. an invalid API key). Feature ViewModels map domain-level `NotAuthorized` error
 * reasons to this exception so that the UI layer ([PaginationErrorIndicator]) can
 * distinguish auth failures from generic errors and display targeted messaging.
 */
class NotAuthorizedException : Exception("Not authorized")
