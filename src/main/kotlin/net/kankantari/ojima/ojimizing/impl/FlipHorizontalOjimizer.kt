package net.kankantari.ojima.ojimizing.impl

import net.kankantari.ojima.errors.OjimaError
import net.kankantari.ojima.ojimizing.Ojimizer
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import org.bytedeco.opencv.global.opencv_core as cv
import java.io.File

class FlipHorizontalOjimizer : Ojimizer("左右反転", "左右反転します。") {
    override fun ojimizeIndex(): List<Int> {
        throw Error("Method ojimizeIndex() can't be used in the current Ojimizer.") // 使わないから例外を返す
    }

    override fun ojimizeVideo(outputFile: File, bitrate: Int) {
        val frameGrabber = FFmpegFrameGrabber(this.inputVideoFile);
        frameGrabber.start();

        val frameRecorder = FFmpegFrameRecorder(outputFile, frameGrabber.imageWidth, frameGrabber.imageHeight);
        frameRecorder.frameRate = this.fps.toDouble();

        frameRecorder.videoCodec = avcodec.AV_CODEC_ID_H264;

        frameRecorder.videoBitrate = if (bitrate > 0) bitrate else frameGrabber.videoBitrate; // bps

        frameRecorder.start();

        val flippedIndices = FlipVerticalOjimizer.ojimizeFlipIndex(this)

        for (index in flippedIndices) {
            frameGrabber.frameNumber = index.index
            var frame: Frame? = frameGrabber.grabImage();

            if (index.flipped) {
                val converter = OpenCVFrameConverter.ToMat()
                val mat = converter.convert(frame)
                cv.flip(mat, mat, 1) // 0: x軸反転, 1: y軸反転, -1: xy軸反転

                frame = converter.convert(mat)
                mat.release() // リソースの解放
            }

            if (frame != null) {
                frameRecorder.record(frame);
            } else {
                frameRecorder.stop()
                frameRecorder.release()
                frameGrabber.stop()
                frameGrabber.release()

                throw OjimaError(
                    "Frame number out of the range of video frame size.",
                    "動画編集処理に失敗しました。範囲外のフレームが指定されています。"
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