@file:Suppress("ktlint:standard:filename")

package ai.anam.lab

/**
 * On non-Android platforms (iOS, macOS, etc.), there is no platform context.
 */
public actual abstract class PlatformContext private constructor() {
    public companion object {
        public val INSTANCE: PlatformContext = object : PlatformContext() {}
    }
}
