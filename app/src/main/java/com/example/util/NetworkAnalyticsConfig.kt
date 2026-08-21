package com.example.util

/**
 * Centralized configuration constants for network analytics, packet buffering,
 * controlled UI refresh intervals, and smooth chart animation durations.
 */
object NetworkAnalyticsConfig {
  /**
   * Interval at which statistics, bandwidth, and traffic counters are aggregated (1000ms).
   */
  const val STATISTICS_AGGREGATION_INTERVAL_MS: Long = 1000L

  /**
   * Controlled dashboard and UI chart refresh interval (1500ms).
   * Prevents UI thrashing while real-time packet collection runs continuously at full speed.
   */
  const val UI_REFRESH_INTERVAL_MS: Long = 1500L

  /**
   * Smooth, professional chart animation duration (1000ms).
   * Eliminates rapid dancing or resetting of graphs during live updates.
   */
  const val CHART_ANIMATION_DURATION_MS: Int = 1000

  /**
   * Refresh interval for database history and retention metrics (3000ms).
   */
  const val HISTORY_REFRESH_INTERVAL_MS: Long = 3000L

  /**
   * Minimum relative change threshold (0.5%) below which micro-updates are filtered out
   * to guarantee rock-solid visual stability.
   */
  const val MIN_CHANGE_THRESHOLD_RATIO: Float = 0.005f
}
