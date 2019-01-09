package com.nd.schedulermockservice

import doobie._
import doobie.implicits._
import cats.effect.IO
import scala.concurrent.ExecutionContext
import com.typesafe.config.ConfigFactory

class DbService(configBase: String) {

  implicit val cs = IO.contextShift(ExecutionContext.global)

  val xa = Transactor.fromDriverManager[IO](
    ConfigFactory.load().getString(s"$configBase.db.driver"),
    ConfigFactory.load().getString(s"$configBase.db.url"),
    ConfigFactory.load().getString(s"$configBase.db.user"),
    ConfigFactory.load().getString(s"$configBase.db.password")
  )

  sql"""create table if not exists lci_report
        (
        job_id varchar(255) not null,
        campaign_id integer not null,
        report_date date not null,
        number_of_visitors integer,
        number_of_impressions integer,
        status varchar(255) not null,
        primary key(job_id)
        );
    """.update.run.transact(xa).unsafeRunSync

  def addInitialReportData(jobId: String, campaignId: Int, reportDate: String):Int = {
    val status = ConfigFactory.load().getString(s"$configBase.report-statuses.scheduled")
    sql"""insert into lci_report (
                                  job_id,
                                  campaign_id,
                                  report_date,
                                  number_of_visitors,
                                  number_of_impressions,
                                  status
                                  )
          values (
                  $jobId,
                  $campaignId,
                  $reportDate,
                  null,
                  null,
                  $status
                  );
    """
      .update
      .run
      .transact(xa)
      .unsafeRunSync
  }

  def updateWithReportResults(jobId: String, numberOfVisitors: Option[Int], numberOfImpressions: Option[Int], status: String) = {
    val number_of_visitors = numberOfVisitors match {
      case None => null
      case _ => numberOfVisitors
    }
    val number_of_impressions = numberOfImpressions match {
      case None => null
      case _ => numberOfImpressions
    }
    sql"""update lci_report
          set number_of_visitors=$number_of_visitors, number_of_impressions=$number_of_impressions, status=$status
          where job_id = $jobId"""
    .update
    .run
    .transact(xa)
    .unsafeRunSync
  }

  def getJobById(jobId: String): Option[Job] =
    sql"select job_id, status from lci_report where job_id = $jobId"
      .query[Job]
      .option
      .transact(xa)
      .unsafeRunSync
}