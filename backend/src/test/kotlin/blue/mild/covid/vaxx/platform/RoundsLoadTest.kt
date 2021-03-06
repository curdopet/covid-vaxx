package blue.mild.covid.vaxx.platform

import blue.mild.covid.vaxx.dto.request.LoginDtoIn
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import mu.KLogging

class RoundsLoadTest(
    targetHost: String,
    credentials: LoginDtoIn,
    requestTimeoutsSeconds: Int = 60,
    private val coroutineWorkers: Int = 2,
    private val rounds: Int
) : LoadTest(targetHost, credentials, requestTimeoutsSeconds) {

    private companion object : KLogging()

    private val singleWorkerRounds = rounds / coroutineWorkers

    override suspend fun execute(): List<RequestMetric> {
        coroutineScope {
            repeat(coroutineWorkers) {
                launch {
                    repeat(singleWorkerRounds) {
                        runPatientRegistrationWithBuilder(defaultPatientRegistrationBuilder()) {
                            logger.info { "Done: $it/$rounds" }
                        }
                    }
                }
            }
        }

        return callsCollection.toList()
    }

}
