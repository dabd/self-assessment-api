/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.selfassessmentapi.connectors

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.selfassessmentapi.config.{AppContext, WSHttp}
import uk.gov.hmrc.selfassessmentapi.models.{PeriodId, SourceId}
import uk.gov.hmrc.selfassessmentapi.models.des.{Financials, SelfEmploymentPeriod}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentPeriodResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentPeriodConnector {

  private lazy val baseUrl: String = AppContext.desUrl
  private val http = WSHttp

  private implicit def httpResponse2SeResponse(fut: Future[HttpResponse]): Future[SelfEmploymentPeriodResponse] =
    fut.map(SelfEmploymentPeriodResponse(_))

  def create(nino: Nino, id: SourceId, selfEmploymentPeriod: SelfEmploymentPeriod)(implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    http.doPost(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries", selfEmploymentPeriod, hc.headers)

  def get(nino: Nino, id: SourceId, periodId: PeriodId)(implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    http.doGet(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries/$periodId")

  def getAll(nino: Nino, id: SourceId)(implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    http.doGet(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries")

  def update(nino: Nino, id: SourceId, periodId: PeriodId, update: Financials)(implicit hc: HeaderCarrier): Future[SelfEmploymentPeriodResponse] =
    http.doPut(baseUrl + s"/income-store/nino/$nino/self-employments/$id/periodic-summaries/$periodId", update)

}
