package ai.anam.lab.fakes

import ai.anam.lab.PlatformContext

internal actual object FakePlatformContext {
    actual fun create(): PlatformContext = PlatformContext.INSTANCE
}
