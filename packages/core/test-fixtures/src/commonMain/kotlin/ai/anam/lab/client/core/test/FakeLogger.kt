package ai.anam.lab.client.core.test

import ai.anam.lab.client.core.logging.Logger

class FakeLogger : Logger {
    override fun v(tag: String?, throwable: Throwable?, message: () -> String) {}
    override fun d(tag: String?, throwable: Throwable?, message: () -> String) {}
    override fun i(tag: String?, throwable: Throwable?, message: () -> String) {}
    override fun w(tag: String?, throwable: Throwable?, message: () -> String) {}
    override fun e(tag: String?, throwable: Throwable?, message: () -> String) {}
    override fun assert(tag: String?, throwable: Throwable?, message: () -> String) {}
}
