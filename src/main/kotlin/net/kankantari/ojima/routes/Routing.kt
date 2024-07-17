package net.kankantari.ojima.routes

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import net.kankantari.ojima.Config
import net.kankantari.ojima.errors.OjimaError
import net.kankantari.ojima.ojimizing.Ojimanager
import net.kankantari.ojima.routes.models.AdminRequest
import net.kankantari.ojima.routes.models.OjimaStatus
import net.kankantari.ojima.routes.models.OjimizationRequest
import net.kankantari.ojima.routes.models.Status
import net.kankantari.ojima.scores.Score
import java.io.File

fun Application.configureRouting() {
    install(ContentNegotiation) {
        gson()
    }

    routing {
        get("/ojimizers") {
            call.response.headers.append("Content-Type", "application/json; charset=UTF-8")
            call.respondText(Ojimanager.generateOjimizerModelsJson())
        }

        get("/reload") {
            call.response.headers.append("Content-Type", "application/json; charset=UTF-8")
            var status = "failed"

            try {
                val request = call.receive<AdminRequest>()

                if (request.password == Config.config.adminPassword) {
                    Config.load()
                    status = "success"
                }
            } catch(error: Error) {
                error.printStackTrace()
            } finally {
                call.respondText(Gson().toJson(Status(status)))
            }
        }

        post("/ojimize") {
            val conf = Config.config

            val multipart = call.receiveMultipart()
            var originalVideoStream: ByteArray? = null

            var id: String = ""
            var scoreStr: String = ""
            var mode: String = ""
            var bpm: Int = -1
            var fps: Float = -1f
            var options: Map<String, String> = mapOf()

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "requestId" -> {
                                id = part.value
                            }
                            "score" -> {
                                scoreStr = part.value
                            }
                            "mode" -> {
                                mode = part.value
                            }
                            "bpm" -> {
                                bpm = part.value.toInt()
                            }
                            "fps" -> {
                                fps = part.value.toFloat()
                            }
                            "options" -> {
                                val type = object : TypeToken<Map<String, String>>() {}.type
                                options = Gson().fromJson(part.value, type)
                            }

                            else -> call.application.environment.log.warn("Unknown item: ${part.name}: ${part.value}")
                        }
                    }

                    is PartData.FileItem -> {
                        if (part.name == "video") {
                            originalVideoStream = part.streamProvider().readBytes()
                        }
                    }

                    else -> Unit
                }

                part.dispose()
            }

            if (id.isBlank() || scoreStr.isBlank() || mode.isBlank() || bpm < 0 || fps < 0) {
                call.application.environment.log.warn("Invalid request payload: ${id}, ${scoreStr}, ${mode}, ${bpm}, ${fps}")

                call.response.header(
                    OjimaStatus.HEADER_NAME,
                    OjimaStatus(id, "failed", "不正なデータです。", false).json()
                )

                call.respond(HttpStatusCode.BadRequest, "不正なデータです。")
                return@post
            }

            call.application.environment.log.info("Request accepted: ${id}, ${scoreStr}, ${mode}, ${bpm}, ${fps}")
            val request = OjimizationRequest(id, scoreStr, mode, bpm, fps, options)

            try {
                val originalVideoFilePath = "${conf.processCachePath}/original"
                val processedVideoFilePath = "${conf.processCachePath}/processed"

                val score = Score(request.score)
                val ojimizer = Ojimanager.getOjimizer(request.mode)

                ojimizer.setOptions(request.options)

                if (!File(originalVideoFilePath).exists()) {
                    File(originalVideoFilePath).mkdirs()
                }

                if (!File(processedVideoFilePath).exists()) {
                    File(processedVideoFilePath).mkdirs()
                }

                val originalVideoFile = File("${originalVideoFilePath}/${id}")
                originalVideoFile.createNewFile()

                val processedVideoFile = File("${processedVideoFilePath}/${id}.mp4")

                originalVideoFile.writeBytes(originalVideoStream!!)

                ojimizer.initialize(score, bpm, fps, originalVideoFile)
                ojimizer.ojimizeVideo(processedVideoFile)

                call.response.header(
                    OjimaStatus.HEADER_NAME,
                    OjimaStatus(id, "success", "Ojimization completed successfully", true).json()
                )

                call.respondFile(processedVideoFile)

                if (conf.deleteOriginalVideo) {
                    originalVideoFile.delete()
                }

                if (conf.deleteProcessedVideo) {
                    processedVideoFile.delete()
                }
            } catch (error: OjimaError) {
                call.response.header(
                    OjimaStatus.HEADER_NAME,
                    OjimaStatus(id, "failed", error.webErrorMsg, false).json()
                )

                call.respond(HttpStatusCode.BadRequest, error.webErrorMsg)

                error.printStackTrace()
            } catch (error: Error) {
                call.response.header(
                    OjimaStatus.HEADER_NAME,
                    OjimaStatus(id, "failed", "内部エラーが発生しました。", false).json()
                )

                call.respond(HttpStatusCode.BadRequest, "内部エラーが発生しました。")

                error.printStackTrace()
            }
        }
    }
}
