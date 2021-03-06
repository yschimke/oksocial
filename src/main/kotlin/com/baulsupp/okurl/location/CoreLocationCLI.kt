package com.baulsupp.okurl.location

import com.baulsupp.oksocial.output.ConsoleHandler
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.output.exec
import com.baulsupp.oksocial.output.isOSX
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.Response
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.logging.Level
import java.util.logging.Logger

const val LOCATION_APP = "location"

/**
 * https://github.com/kiliankoe/location
 */
class CoreLocationCLI(val outputHandler: OutputHandler<Response>) : LocationSource {
  private val logger = Logger.getLogger(CoreLocationCLI::class.java.name)

  override suspend fun read(): Location? {
    return if (isOSX) {
      try {
        val line = exec(LOCATION_APP, outputMode = ConsoleHandler.Companion.OutputMode.Return)

        if (line == null) {
          logger.log(Level.INFO, "failed to get location $line")
          return null
        }

        val moshi = Moshi.Builder().build()
        val adapter: JsonAdapter<Location> = moshi.adapter(Location::class.java)
        adapter.fromJson(line)
      } catch (e: TimeoutException) {
        logger.log(Level.FINE, "failed to get location", e)
        outputHandler.showError(
          "Timeout fetching location, consider populating ~/.okurl-location.json"
        )
        null
      } catch (e: Exception) {
        logger.log(Level.WARNING, "failed to get location, install from https://github.com/kiliankoe/location", e)
        null
      }
    } else {
      null
    }
  }
}
