package ai.anam.lab.client.core.common

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import kotlin.test.Test

class EitherTest {

    @Test
    fun `isLeft returns true for Left instance`() {
        val either: Either<String, Int> = Either.Left("error")
        assertThat(either.isLeft).isTrue()
    }

    @Test
    fun `isLeft returns false for Right instance`() {
        val either: Either<String, Int> = Either.Right(42)
        assertThat(either.isLeft).isFalse()
    }

    @Test
    fun `isRight returns true for Right instance`() {
        val either: Either<String, Int> = Either.Right(42)
        assertThat(either.isRight).isTrue()
    }

    @Test
    fun `isRight returns false for Left instance`() {
        val either: Either<String, Int> = Either.Left("error")
        assertThat(either.isRight).isFalse()
    }

    @Test
    fun `leftOrNull returns value for Left instance`() {
        val either: Either<String, Int> = Either.Left("error")
        assertThat(either.leftOrNull()).isEqualTo("error")
    }

    @Test
    fun `leftOrNull returns null for Right instance`() {
        val either: Either<String, Int> = Either.Right(42)
        assertThat(either.leftOrNull()).isNull()
    }

    @Test
    fun `rightOrNull returns value for Right instance`() {
        val either: Either<String, Int> = Either.Right(42)
        assertThat(either.rightOrNull()).isEqualTo(42)
    }

    @Test
    fun `rightOrNull returns null for Left instance`() {
        val either: Either<String, Int> = Either.Left("error")
        assertThat(either.rightOrNull()).isNull()
    }

    @Test
    fun `onLeft executes action for Left instance`() {
        val either: Either<String, Int> = Either.Left("error")
        var executed = false
        var capturedValue: String? = null

        val result = either.onLeft { value ->
            executed = true
            capturedValue = value
        }

        assertThat(executed).isTrue()
        assertThat(capturedValue).isEqualTo("error")
        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `onLeft does not execute action for Right instance`() {
        val either: Either<String, Int> = Either.Right(42)
        var executed = false

        val result = either.onLeft {
            executed = true
        }

        assertThat(executed).isFalse()
        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `onRight executes action for Right instance`() {
        val either: Either<String, Int> = Either.Right(42)
        var executed = false
        var capturedValue: Int? = null

        val result = either.onRight { value ->
            executed = true
            capturedValue = value
        }

        assertThat(executed).isTrue()
        assertThat(capturedValue).isEqualTo(42)
        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `onRight does not execute action for Left instance`() {
        val either: Either<String, Int> = Either.Left("error")
        var executed = false

        val result = either.onRight {
            executed = true
        }

        assertThat(executed).isFalse()
        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `mapLeft transforms Left value`() {
        val either: Either<String, Int> = Either.Left("error")
        val result = either.mapLeft { it.uppercase() }

        assertThat(result).isEqualTo(Either.Left("ERROR"))
    }

    @Test
    fun `mapLeft returns unchanged Right instance`() {
        val either: Either<String, Int> = Either.Right(42)
        val result = either.mapLeft { it.uppercase() }

        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `mapLeft transforms Left value with different return type`() {
        val either: Either<Int, String> = Either.Left(5)
        val result = either.mapLeft { it * 2 }

        assertThat(result).isEqualTo(Either.Left(10))
    }

    @Test
    fun `mapRight transforms Right value`() {
        val either: Either<String, Int> = Either.Right(42)
        val result = either.mapRight { it * 2 }

        assertThat(result).isEqualTo(Either.Right(84))
    }

    @Test
    fun `mapRight returns unchanged Left instance`() {
        val either: Either<String, Int> = Either.Left("error")
        val result = either.mapRight { it * 2 }

        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `mapRight transforms Right value with different return type`() {
        val either: Either<String, Int> = Either.Right(42)
        val result = either.mapRight { "Value: $it" }

        assertThat(result).isEqualTo(Either.Right("Value: 42"))
    }

    @Test
    fun `onLeft can be chained`() {
        val either: Either<String, Int> = Either.Left("error")
        var count = 0

        val result = either
            .onLeft { count++ }
            .onLeft { count++ }

        assertThat(count).isEqualTo(2)
        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `onRight can be chained`() {
        val either: Either<String, Int> = Either.Right(42)
        var count = 0

        val result = either
            .onRight { count++ }
            .onRight { count++ }

        assertThat(count).isEqualTo(2)
        assertThat(result).isEqualTo(either)
    }

    @Test
    fun `mapLeft and mapRight can be chained`() {
        val either: Either<String, Int> = Either.Left("error")
        val result = either
            .mapLeft { it.uppercase() }
            .mapRight { it * 2 }

        assertThat(result).isEqualTo(Either.Left("ERROR"))
    }

    @Test
    fun `mapRight and mapLeft can be chained`() {
        val either: Either<String, Int> = Either.Right(42)
        val result = either
            .mapRight { it * 2 }
            .mapLeft { it.uppercase() }

        assertThat(result).isEqualTo(Either.Right(84))
    }
}
