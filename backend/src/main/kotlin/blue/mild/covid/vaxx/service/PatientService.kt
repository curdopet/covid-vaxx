package blue.mild.covid.vaxx.service

import blue.mild.covid.vaxx.dao.Answer
import blue.mild.covid.vaxx.dao.Patient
import blue.mild.covid.vaxx.dto.AnswerDto
import blue.mild.covid.vaxx.dto.PatientDto
import blue.mild.covid.vaxx.dto.PatientRegistrationDto
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import pw.forst.tools.katlib.toUuid
import java.util.UUID

class PatientService {
    suspend fun getAllPatients(): List<PatientDto> = newSuspendedTransaction {
        Patient
            .leftJoin(Answer, { id }, { patientId })
            .selectAll()
            .map {
                object {
                    val patient = PatientDto(
                        id = it[Patient.id].toUuid(),
                        firstName = it[Patient.firstName],
                        lastName = it[Patient.lastName],
                        personalNumber = it[Patient.personalNumber],
                        phoneNumber = it[Patient.phoneNumber],
                        email = it[Patient.email],
                        answers = emptyList() // filled in the next step
                    )
                    val questionId = it[Answer.questionId].toUuid()
                    val answerValue = it[Answer.value]
                }
            }
            .groupBy { it.patient }
            .map { (patient, answerRows) ->
                patient.copy(answers = answerRows.map { AnswerDto(it.questionId, it.answerValue) })
            }
    }

    suspend fun savePatient(patientDto: PatientRegistrationDto) = newSuspendedTransaction {
        val patientId = UUID.randomUUID().toString()

        Patient.insert {
            it[id] = patientId
            it[firstName] = patientDto.firstName
            it[lastName] = patientDto.lastName
            it[personalNumber] = patientDto.personalNumber
            it[phoneNumber] = patientDto.phoneNumber
            it[email] = patientDto.email
        }

        Answer.batchInsert(patientDto.answers) {
            this[Answer.patientId] = patientId
            this[Answer.questionId] = it.questionId.toString()
            this[Answer.value] = it.value
        }
    }
}
