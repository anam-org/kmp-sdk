/// Simple class that provides a number of static functions, redirecting to the shared (KMP) logger instance that's available
/// via the ObjectGraph.
class Logger {
    private static let logger = AppState.shared.logger
    private init() { }

    static func logVerbose(_ tag: String, message: @escaping () -> String) {
        logger.v(tag: tag, throwable: nil, message: message)
    }

    static func logDebug(_ tag: String, message: @escaping () -> String) {
        logger.d(tag: tag, throwable: nil, message: message)
    }

    static func logInfo(_ tag: String, message: @escaping () -> String) {
        logger.i(tag: tag, throwable: nil, message: message)
    }

    static func logWarning(_ tag: String, message: @escaping () -> String) {
        logger.w(tag: tag, throwable: nil, message: message)
    }

    static func logError(_ tag: String, message: @escaping () -> String) {
        logger.e(tag: tag, throwable: nil, message: message)
    }
}
