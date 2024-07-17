package net.kankantari.ojima.ojimizing

import net.kankantari.ojima.scores.Score
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import java.io.File

abstract class Ojimizer {
    val name: String;
    val description: String;

    lateinit var frameIndexSet: List<Int>;
    lateinit var score: Score;
    lateinit var inputVideoFile: File;
    lateinit var ojimaOptions: Map<String, String>
    var bpm: Int = 0;
    var fps: Float = 0f;


    constructor(name: String, description: String) {
        this.name = name;
        this.description = description;
    }

    fun initialize(score: Score, bpm: Int, fps: Float, inputVideoFile: File, frameIndexSet: List<Int>) {
        this.score = score;
        this.bpm = bpm;
        this.fps = fps;
        this.inputVideoFile = inputVideoFile;
        this.frameIndexSet = frameIndexSet;
    }

    fun initialize(score: Score, bpm: Int, fps: Float, inputVideoFile: File) {
        this.score = score;
        this.bpm = bpm;
        this.fps = fps;
        this.inputVideoFile = inputVideoFile;

        // 動画ファイルからフレーム数取得
        val frameGrabber = FFmpegFrameGrabber(this.inputVideoFile);
        frameGrabber.start();

        this.frameIndexSet = (0..<frameGrabber.lengthInFrames - 1).toList(); // なぜか-1が必要

        frameGrabber.stop()
        frameGrabber.release()
    }

    abstract fun ojimizeIndex(): List<Int>;

    fun setOptions(options: Map<String, String>) {
        this.ojimaOptions = options;
    }

    fun ojimizeVideo(outputFile: File, bitrate: Int = -1) {
        val frameGrabber = FFmpegFrameGrabber(this.inputVideoFile);
        frameGrabber.start();

        val frameRecorder = FFmpegFrameRecorder(outputFile, frameGrabber.imageWidth, frameGrabber.imageHeight);
        frameRecorder.frameRate = this.fps.toDouble();

        frameRecorder.videoCodec = avcodec.AV_CODEC_ID_H264;

        frameRecorder.videoBitrate = if (bitrate > 0) bitrate else frameGrabber.videoBitrate; // bps

        frameRecorder.start();

        val frameNumbers = this.ojimizeIndex();

        for (frameNumber in frameNumbers) {
            frameGrabber.frameNumber = frameNumber;
            val frame: Frame? = frameGrabber.grabImage();

            if (frame != null) {
                frameRecorder.record(frame);
            }
        }

        // リソースを解放する
        frameRecorder.stop();
        frameRecorder.release();
        frameGrabber.stop();
        frameGrabber.release();
    }
}