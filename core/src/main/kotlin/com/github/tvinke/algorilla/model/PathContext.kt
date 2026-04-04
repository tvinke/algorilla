package com.github.tvinke.algorilla.model

/**
 * Architectural path context: how a method is reachable from the application's entry points.
 * Used as metadata for output labeling and future segmented presentation.
 * Does NOT affect confidence, demotion, or detection.
 */
public enum class PathContext {
    /** Reachable from HTTP/REST controller entry points. */
    REQUEST,

    /** Reachable only from lifecycle/init methods (@PostConstruct, InitializingBean). */
    LIFECYCLE,

    /** Reachable only from batch/scheduled entry points (Spring Batch Tasklet, @Scheduled). */
    BATCH,

    /** Reachable from multiple entry point kinds (e.g. both request and lifecycle). */
    MIXED,
}
