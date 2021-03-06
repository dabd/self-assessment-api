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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.api.controllers.ErrorNotFound
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.play.microservice.controller.BaseController
import uk.gov.hmrc.selfassessmentapi.connectors.TaxCalculationConnector
import uk.gov.hmrc.selfassessmentapi.models.calculation.CalculationRequest
import uk.gov.hmrc.selfassessmentapi.models.Errors.Error
import uk.gov.hmrc.selfassessmentapi.models.{SourceId, SourceType}
import uk.gov.hmrc.selfassessmentapi.resources.wrappers.TaxCalculationResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TaxCalculationResource extends BaseController {

  private lazy val featureSwitch = FeatureSwitchAction(SourceType.Calculation)
  private val logger = Logger(TaxCalculationResource.getClass)
  private val connector = TaxCalculationConnector

  private val cannedEtaResponse =
    s"""
       |{
       |  "etaSeconds": 5
       |}
     """.stripMargin

  def requestCalculation(nino: Nino): Action[JsValue] = featureSwitch.asyncJsonFeatureSwitch { request =>
    validate[CalculationRequest, TaxCalculationResponse](request.body) { req =>
      connector.requestCalculation(nino, req.taxYear)
    } match {
      case Left(errorResult) => Future.successful(handleValidationErrors(errorResult))
      case Right(result) => result.map { response =>
        if (response.status == 202) Accepted(Json.parse(cannedEtaResponse))
          .withHeaders(LOCATION -> response.calcId.map(id => s"/self-assessment/ni/$nino/calculations/$id").getOrElse(""))
        else if (response.status == 400) BadRequest(Error.from(response.json))
        else unhandledResponse(response.status, logger)
      }
    }
  }

  def retrieveCalculation(nino: Nino, calcId: SourceId): Action[AnyContent] = featureSwitch.asyncFeatureSwitch {
    connector.retrieveCalculation(nino, calcId).map { response =>
      if (response.status == 200) Ok(Json.toJson(response.calculation))
      else if (response.status == 204) NoContent
      else if (response.status == 400) BadRequest(Error.from(response.json))
      else if (response.status == 404) NotFound(Json.toJson(ErrorNotFound))
      else unhandledResponse(response.status, logger)
    }
  }
}
