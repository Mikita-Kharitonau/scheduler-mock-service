package com.nd.schedulermockservice

import cats.effect.IO
import io.finch._
import io.finch.circe._
import io.finch.catsEffect._
import io.circe.generic.auto._
import com.roundeights.hasher.Implicits._
import com.typesafe.config.ConfigFactory
import decorators.withInternalServerError

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.language.postfixOps


class Endpoints(configBase: String) {

  val rootUrl = "api" :: "v1"

  val dbService = new DbService(configBase)

  def addReport: Endpoint[IO, Job] = post(rootUrl :: "report" :: "lci" :: jsonBody[LciRequestBody]) {
    body: LciRequestBody =>
      withInternalServerError[Job] {
        val jobId = (body.jobName + body.campaignId + body.reportDate).sha1.hex
        dbService.addInitialReportData(jobId, body.campaignId, body.reportDate)
        val status = ConfigFactory.load().getString(s"$configBase.report-statuses.scheduled")
        val f: Future[Any] = Future {runReportAndWriteResults(jobId, body)}
        Ok(Job(jobId, status))
      }
  }

  def getJob: Endpoint[IO, Option[Job]] = get(rootUrl :: "job" :: path[String]) {
    jobId: String =>
      withInternalServerError[Option[Job]] {
        val job = dbService.getJobById(jobId)
        job match {
          case None => NoContent
          case _ => Ok(job)
        }
      }
  }

  def runReportAndWriteResults(jobId: String, requestBody: LciRequestBody): Unit = {
    val runReport: Future[LciReportResults] = Future {
      val delay = ConfigFactory.load().getString(s"$configBase.time-to-run-report").toInt
      Thread.sleep(delay)
      LciReportResults(
        requestBody.campaignId,
        requestBody.reportDate,
        Option(requestBody.campaignId + 1),
        Option(requestBody.campaignId + 2))
    }
    runReport.onComplete {
      case Success(res) => {
        val status_finished = ConfigFactory.load().getString(s"$configBase.report-statuses.finished")
        dbService.updateWithReportResults(jobId, res.numberOfVisitors, res.numberOfImpressions, status_finished)
      }
      case Failure(err) => {
        println(err.getMessage)
      }
    }
  }
}
