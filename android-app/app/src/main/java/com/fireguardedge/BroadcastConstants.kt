package com.fireguardedge.app

object BroadcastConstants {
    // Actions
    const val ACTION_STATUS_UPDATE = "com.fireguardedge.ACTION_STATUS_UPDATE"
    const val ACTION_DETECTION = "com.fireguardedge.ACTION_DETECTION"
    const val ACTION_LOG_ADDED = "com.fireguardedge.ACTION_LOG_ADDED"

    // Extras keys
    const val EXTRA_VISION_LABEL = "extra_vision_label"
    const val EXTRA_VISION_CONF = "extra_vision_conf"
    const val EXTRA_AUDIO_LABEL = "extra_audio_label"
    const val EXTRA_AUDIO_CONF = "extra_audio_conf"
    const val EXTRA_DETECTION_LABEL = "extra_detection_label"
    const val EXTRA_DETECTION_SCORE = "extra_detection_score"
    const val EXTRA_LOG_MESSAGE = "extra_log_message"
    const val EXTRA_LOG_TIME = "extra_log_time"
}