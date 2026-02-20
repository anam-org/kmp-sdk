package ai.anam.lab.client.core.permissions

/**
 * Platform abstraction for requesting permissions. Android and iOS use moko-permissions;
 * wasmJs uses browser Permissions API and getUserMedia.
 */
interface PlatformPermissionsController {
    suspend fun requestRecordAudio(): PermissionResult

    suspend fun requestCamera(): PermissionResult

    /**
     * For Android, the moko PermissionsController to bind to the Activity lifecycle.
     * Null on iOS and wasmJs.
     */
    val bindTarget: Any?
}
