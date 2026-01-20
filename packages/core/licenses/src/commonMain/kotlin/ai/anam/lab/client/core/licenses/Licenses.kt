package ai.anam.lab.client.core.licenses

import kotlinx.serialization.Serializable

@Serializable
data class LicenseItem(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val spdxLicenses: List<SpdxLicense>?,
    val name: String?,
    val scm: Scm?,
)

@Serializable
data class SpdxLicense(val identifier: String, val name: String, val url: String)

@Serializable
data class Scm(val url: String)
