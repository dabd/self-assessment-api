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

package uk.gov.hmrc.selfassessmentapi.resources

import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.connectors.SelfEmploymentAnnualSummaryConnector
import uk.gov.hmrc.selfassessmentapi.models._
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.selfemployment.SelfEmploymentAnnualSummary
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.SelfEmploymentAnnualSummaryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object SelfEmploymentAnnualSummaryResource extends BaseController {
  private val logger = Logger(SelfEmploymentAnnualSummaryResource.getClass)
  private lazy val annualSummaryFeatureSwitch = FeatureSwitchAction(SourceType.SelfEmployments, "annual")
  private val connector = SelfEmploymentAnnualSummaryConnector

  def updateAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[JsValue] = annualSummaryFeatureSwitch.asyncJsonFeatureSwitch { request =>
    validate[SelfEmploymentAnnualSummary, SelfEmploymentAnnualSummaryResponse](request.body) { summary =>
      connector.update(nino, id, taxYear, des.SelfEmploymentAnnualSummary.from(summary))
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map { response =>
        if (response.status == 200) NoContent
        else if (response.status == 404) NotFound(Json.toJson(ErrorNotFound))
        else if (response.status == 400) BadRequest(Error.from(response.json))
        else unhandledResponse(response.status, logger)
      }
    }
  }

  // TODO: DES spec for this method is currently unavailable. This method should be updated once it is available.
  def retrieveAnnualSummary(nino: Nino, id: SourceId, taxYear: TaxYear): Action[AnyContent] = annualSummaryFeatureSwitch.asyncFeatureSwitch {
    connector.get(nino, id, taxYear).map { response =>
      if (response.status == 200) response.annualSummary match {
        case Some(summary) => Ok(Json.toJson(summary))
        case None => NotFound
      }
      else if (response.status == 404) NotFound(Json.toJson(ErrorNotFound))
      else unhandledResponse(response.status, logger)
    }
  }
}
