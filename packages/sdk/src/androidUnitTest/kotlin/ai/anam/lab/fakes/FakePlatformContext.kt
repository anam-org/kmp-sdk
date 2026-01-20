package ai.anam.lab.fakes

import ai.anam.lab.PlatformContext
import android.content.ContextWrapper

internal actual object FakePlatformContext {
    actual fun create(): PlatformContext {
        // Create a simple mock Context for testing
        // Since Context is abstract, we use ContextWrapper with a null base
        // In a real scenario, this would be provided by a testing framework like Robolectric
        return object : ContextWrapper(null) {}
    }
}
