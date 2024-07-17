package net.kankantari.ojima.routes.models

data class OjimizationRequest(
    val requestId: String,
    val score: String, val mode: String,
    val bpm: Int,
    val fps: Float,
    val options: Map<String, String>
);