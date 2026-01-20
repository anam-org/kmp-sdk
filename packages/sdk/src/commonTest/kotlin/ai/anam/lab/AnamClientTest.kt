package ai.anam.lab

import ai.anam.lab.fakes.FakeLogger
import ai.anam.lab.fakes.FakePlatformContext
import assertk.assertThat
import assertk.assertions.isInstanceOf
import kotlin.test.Test
import kotlinx.coroutines.runBlocking

class AnamClientTest {
    private val logger = FakeLogger()

    @Test
    fun `createSession returns error when invalid JWT session token is provided`() {
        runBlocking {
            val client = withClient()

            val invalidToken = "invalid.jwt.token"

            // Verify that with an invalid session token, the client returns an error.
            val result = client.createSession(invalidToken)
            assertThat(result).isInstanceOf(SessionResult.Error::class)
        }
    }

    private fun withClient() = AnamClient(
        options = AnamClientOptions(
            context = FakePlatformContext.create(),
            environment = Environment.Production,
            logger = logger,
        ),
    )
}
