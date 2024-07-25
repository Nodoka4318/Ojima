package net.kankantari.ojima.ojimizing.impl

import net.kankantari.ojima.errors.OjimaError
import net.kankantari.ojima.ojimizing.Ojimizer
import net.kankantari.ojima.scores.EnumTokenType
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.bytedeco.javacv.Frame
import org.bytedeco.javacv.OpenCVFrameConverter
import java.io.File
import org.bytedeco.opencv.global.opencv_core as cv

class FlipVerticalOjimizer : Ojimizer("上下反転", "上下反転します。") {
    data class FlipIndexElem(val index: Int, val flipped: Boolean)

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

        val flippedIndices = ojimizeFlipIndex(this)

        for (index in flippedIndices) {
            frameGrabber.frameNumber = index.index
            var frame: Frame? = frameGrabber.grabImage();

            if (index.flipped) {
                val converter = OpenCVFrameConverter.ToMat()
                val mat = converter.convert(frame)
                cv.flip(mat, mat, 0) // 0: x軸反転, 1: y軸反転, -1: xy軸反転

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

    companion object {
        fun ojimizeFlipIndex(ojimizer: Ojimizer): List<FlipIndexElem> {
            val ojimized = mutableListOf<FlipIndexElem>();

            val frameSizes = mutableListOf<Int>();
            var beatByFar = 0f;

            for (token in ojimizer.score.tokens) {
                val frameSize = ojimizer.fps * token.length / ojimizer.bpm * 60;

                beatByFar += token.length;

                val sum = frameSizes.sum() + frameSize;
                val accurateFrameSizeByFar = ojimizer.fps * beatByFar / ojimizer.bpm * 60;
                val adjustment = accurateFrameSizeByFar - sum; // 累積ズレを修正

                frameSizes.add(Math.round(frameSize + adjustment));

                if (token.type == EnumTokenType.Forward) {
                    val resizedIndices = DefaultOjimizer.resizeList(ojimizer.frameIndexSet, frameSizes.last())
                    val resizedFlipIndices = resizedIndices.map { FlipIndexElem(it, false) }

                    ojimized.addAll(resizedFlipIndices)
                } else if (token.type == EnumTokenType.Backward) {
                    val resizedIndices = DefaultOjimizer.resizeList(ojimizer.frameIndexSet, frameSizes.last())
                    val resizedFlipIndices = resizedIndices.map { FlipIndexElem(it, true) }

                    ojimized.addAll(resizedFlipIndices)
                } else if (token.type == EnumTokenType.Rest) {
                    ojimized.addAll(List(frameSizes.last()) { ojimized.last() } )
                }
            }

            return ojimized
        }
    }
}