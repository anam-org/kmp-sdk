package ai.anam.lab.client.core.common

import ai.anam.lab.client.core.common.Either.Left
import ai.anam.lab.client.core.common.Either.Right

/**
 * Represents a value that can be either a [Left] (typically representing an error or failure) or a [Right]
 * (typically representing a success or valid value). This is a functional programming construct that allows
 * for explicit error handling without exceptions.
 *
 * @param T The type of the [Left] value.
 * @param U The type of the [Right] value.
 */
sealed class Either<out T, out U> {
    /**
     * Represents the left side of an [Either], typically used for errors or failure cases.
     *
     * @param value The value contained in this [Left] instance.
     */
    data class Left<T>(val value: T) : Either<T, Nothing>()

    /**
     * Represents the right side of an [Either], typically used for success or valid values.
     *
     * @param value The value contained in this [Right] instance.
     */
    data class Right<U>(val value: U) : Either<Nothing, U>()

    /**
     * Returns `true` if this [Either] is a [Left] instance, `false` otherwise.
     */
    val isLeft: Boolean
        get() = this is Left

    /**
     * Returns `true` if this [Either] is a [Right] instance, `false` otherwise.
     */
    val isRight: Boolean
        get() = this is Right
}

/**
 * Returns the [Left] value if this [Either] is a [Left], or `null` if it is a [Right].
 *
 * @return The left value if this is a [Left], otherwise `null`.
 */
fun <T, U> Either<T, U>.leftOrNull(): T? = when (this) {
    is Left -> value
    is Right -> null
}

/**
 * Returns the [Right] value if this [Either] is a [Right], or `null` if it is a [Left].
 *
 * @return The right value if this is a [Right], otherwise `null`.
 */
fun <T, U> Either<T, U>.rightOrNull(): U? = when (this) {
    is Left -> null
    is Right -> value
}

/**
 * Performs the given [action] on the [Left] value if this [Either] is a [Left], and returns this [Either]
 * unchanged. This is useful for side effects such as logging errors.
 *
 * @param action The action to perform on the left value.
 * @return This [Either] instance, unchanged.
 */
inline fun <T, U> Either<T, U>.onLeft(action: (T) -> Unit): Either<T, U> {
    if (this is Left) {
        action(value)
    }
    return this
}

/**
 * Performs the given [action] on the [Right] value if this [Either] is a [Right], and returns this
 * [Either] unchanged. This is useful for side effects such as logging success.
 *
 * @param action The action to perform on the right value.
 * @return This [Either] instance, unchanged.
 */
inline fun <T, U> Either<T, U>.onRight(action: (U) -> Unit): Either<T, U> {
    if (this is Right) {
        action(value)
    }
    return this
}

/**
 * Transforms the [Left] value of this [Either] using the given [transform] function. If this [Either] is
 * a [Right], it is returned unchanged.
 *
 * @param transform The function to apply to the left value.
 * @return A new [Either] with the transformed left value, or this [Either] if it is a [Right].
 */
inline fun <T, R, U> Either<T, U>.mapLeft(transform: (T) -> R): Either<R, U> = when (this) {
    is Left -> Left(transform(value))
    is Right -> this
}

/**
 * Transforms the [Right] value of this [Either] using the given [transform] function. If this [Either] is
 * a [Left], it is returned unchanged.
 *
 * @param transform The function to apply to the right value.
 * @return A new [Either] with the transformed right value, or this [Either] if it is a [Left].
 */
inline fun <T, U, R> Either<T, U>.mapRight(transform: (U) -> R): Either<T, R> = when (this) {
    is Left -> this
    is Right -> Right(transform(value))
}
