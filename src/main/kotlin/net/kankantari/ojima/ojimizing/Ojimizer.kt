package net.kankantari.ojima.ojimizing

import net.kankantari.ojima.errors.OjimaError
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
    lateinit var ojimaOptions: Map<String, Any>
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

    /**
     * オプションの設定（適宜派生クラスで実装）
     * 必ずinitializeの後に実行すること
     */
    fun setOptions(options: Map<String, Any>) {
        this.ojimaOptions = options;

        // 共通オプション (startFrame, endFrame)
        if (options.containsKey("startFrame") || options.containsKey("endFrame")) {
            if (options.containsKey("startFrame") && options.containsKey("endFrame")) {
                try {
                    val startFrame = options.get("startFrame") as Int
                    val endFrame = options.get("endFrame") as Int

                    val max = frameIndexSet.last()

                    if (startFrame < 0 || startFrame > max || endFrame < 0 || endFrame > max) {
                        throw Error("Invalid frame range")
                    }

                    frameIndexSet = (startFrame..<endFrame - 1).toList(); // なぜか-1
                } catch (error: Error) {
                    error.printStackTrace()
                    throw OjimaError(
                        "Error whilst setting custom startFrame and endFrame.",
                        "フレーム開始位置または終了位置が不正です。"
                    )
                }
            } else {
                throw OjimaError(
                    "Either startFrame or endFrame is missing.",
                    "startFrameまたはendFrameオプションのいずれかが不足しています。"
                )
            }
        }
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
            } else {
                frameRecorder.stop()
                frameRecorder.release()
                frameGrabber.stop()
                frameGrabber.release()

                throw OjimaError(
                    "Frame number out of the range of video frame size.",
                    "動画編集処理に失敗しました。フレーム番号が不正です。"
                )
            }
        }

        // リソースを解放する
        frameRecorder.stop();
        frameRecorder.release();
        frameGrabber.stop();
        frameGrabber.release();
    }
}