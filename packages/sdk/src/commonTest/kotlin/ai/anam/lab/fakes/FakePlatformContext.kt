package ai.anam.lab.fakes

import ai.anam.lab.PlatformContext

internal expect object FakePlatformContext {
    fun create(): PlatformContext
}
