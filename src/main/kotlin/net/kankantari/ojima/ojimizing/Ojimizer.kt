package net.kankantari.ojima.ojimizing

import net.kankantari.ojima.scores.Score

abstract class Ojimizer {
    val name: String;
    val description: String;

    lateinit var frameIndexSet: List<Int>;
    lateinit var score: Score;
    var bpm: Int = 0;
    var fps: Float = 0f;


    constructor(name: String, description: String) {
        this.name = name;
        this.description = description;
    }

    fun initialize(score: Score, bpm: Int, fps: Float, frameIndexSet: List<Int>) {
        this.score = score;
        this.bpm = bpm;
        this.fps = fps;
        this.frameIndexSet = frameIndexSet;
    }

    abstract fun ojimizeIndex(): List<Int>;
}