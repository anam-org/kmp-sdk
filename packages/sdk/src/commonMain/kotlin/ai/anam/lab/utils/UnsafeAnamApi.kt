package ai.anam.lab.utils

/**
 * This annotation marks any unsafe, yet public APIs that are exposed by this SDK. This requires the consumer to opt-in
 * and therefore understand that they shouldn't be using this in a production environment.
 */
@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPEALIAS,
)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
public annotation class UnsafeAnamApi
