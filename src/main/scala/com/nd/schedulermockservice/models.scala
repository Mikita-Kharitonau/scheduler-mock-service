package com.nd.schedulermockservice


case class LciRequestBody(
    jobName: String,
    campaignId: Int,
    reportDate: String
)


case class Job(
    jobId: String,
    status: String
)


case class LciReportResults(
    campaignId: Int,
    reportDate: String,
    numberOfVisitors: Option[Int],
    numberOfImpressions: Option[Int]
)