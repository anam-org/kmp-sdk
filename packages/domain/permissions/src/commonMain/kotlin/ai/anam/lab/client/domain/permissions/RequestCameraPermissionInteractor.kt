package ai.anam.lab.client.domain.permissions

import ai.anam.lab.client.core.permissions.PermissionResult

fun interface RequestCameraPermissionInteractor {
    suspend operator fun invoke(): PermissionResult
}
