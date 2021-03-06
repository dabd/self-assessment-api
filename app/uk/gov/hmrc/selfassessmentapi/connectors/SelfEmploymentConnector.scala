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
import uk.gov.hmrc.selfassessmentapi.models.SourceId
import uk.gov.hmrc.selfassessmentapi.models.des.{Business, SelfEmploymentUpdate}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentResponse

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object SelfEmploymentConnector {

  private lazy val baseUrl: String = AppContext.desUrl
  private val http = WSHttp

  private implicit def httpResponse2SeResponse(fut: Future[HttpResponse]): Future[SelfEmploymentResponse] =
    fut.map(SelfEmploymentResponse(_))

  def create(nino: Nino, business: Business)(implicit hc: HeaderCarrier): Future[SelfEmploymentResponse] =
    http.doPost(baseUrl + s"/income-tax-self-assessment/nino/$nino/business", business, hc.headers)

  def get(nino: Nino)(implicit hc: HeaderCarrier): Future[SelfEmploymentResponse] =
    http.doGet(baseUrl + s"/registration/business-details/nino/$nino")

  def update(nino: Nino, business: SelfEmploymentUpdate, id: SourceId)(implicit hc: HeaderCarrier): Future[SelfEmploymentResponse] =
    http.doPut(baseUrl + s"/income-tax-self-assessment/nino/$nino/business/$id", business)
}

