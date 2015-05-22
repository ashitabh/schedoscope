/**
 * Copyright 2015 Otto (GmbH & Co KG)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.schedoscope.dsl

import scala.collection.mutable.HashMap
import org.schedoscope.Settings
import org.schedoscope.scheduler.driver.FileSystemDriver

abstract class Transformation {
  var view: Option[View] = None

  def configureWith(c: Map[String, Any]) = {
    configuration ++= c
    this
  }

  val configuration = HashMap[String, Any]()

  def versionDigest() = Version.digest(resourceHashes)

  def resources() = List[String]()

  def resourceHashes = Version.resourceHashes(resources())

  def forView(v: View) = {
    view = Some(v)
    this
  }

  var description = this.toString

  def getView() = if (view.isDefined) view.get.urlPath else "no-view"

  def name: String
}

abstract class ExternalTransformation extends Transformation

case class NoOp() extends Transformation {
  override def name = "noop"
}

object Transformation {
  def replaceParameters(query: String, parameters: Map[String, Any]): String = {
    if (parameters.isEmpty)
      query
    else {
      val (key, value) = parameters.head
      val replacedStatement = query.replaceAll(java.util.regex.Pattern.quote("${" + key + "}"), value.toString().replaceAll("\\$", "|"))
      replaceParameters(replacedStatement, parameters.tail)
    }
  }
}
