package ai.anam.lab.client.domain.data

import ai.anam.lab.client.core.licenses.LicenseItem

fun interface FetchLicensesInteractor {
    suspend operator fun invoke(): List<LicenseItem>
}
