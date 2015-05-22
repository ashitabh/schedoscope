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
package org.schedoscope

import java.util.concurrent.TimeUnit

import scala.annotation.implicitNotFound
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

import org.schedoscope.scheduler.RootActor

import akka.actor.ActorRef
import akka.pattern.Patterns
import akka.util.Timeout

package object scheduler {
  implicit val executionContext: ExecutionContext = RootActor.settings.system.dispatchers.lookup("akka.actor.future-call-dispatcher")

  def queryActor[T](actor: ActorRef, queryMessage: Any, timeoutDuration: FiniteDuration): T = {
    val askTimeOut = Timeout(FiniteDuration((timeoutDuration.toMillis * 1.1).toLong, TimeUnit.MILLISECONDS))
    val waitTimeOut = Timeout(FiniteDuration((timeoutDuration.toMillis * 1.2).toLong, TimeUnit.MILLISECONDS))
    val responseFuture = Patterns.ask(actor, queryMessage, askTimeOut)
    Await.result(responseFuture, waitTimeOut.duration).asInstanceOf[T]
  }

  def queryActors[T](actor: ActorRef, queryMessages: List[Any], timeoutDuration: FiniteDuration): List[T] = {
    val askTimeOut = Timeout(FiniteDuration((timeoutDuration.toMillis * 1.1).toLong, TimeUnit.MILLISECONDS))
    val waitTimeOut = Timeout(FiniteDuration((timeoutDuration.toMillis * 1.2).toLong, TimeUnit.MILLISECONDS))

    val responseFutures = queryMessages.map { m => Patterns.ask(actor, m, askTimeOut) }

    val responsesFuture = Future.sequence(responseFutures)

    Await.result(responsesFuture, waitTimeOut.duration * queryMessages.size).asInstanceOf[List[T]]
  }

  def queryActors[T](actors: List[ActorRef], queryMessage: Any, timeoutDuration: FiniteDuration): List[T] = {
    val askTimeOut = Timeout(FiniteDuration((timeoutDuration.toMillis * 1.1).toLong, TimeUnit.MILLISECONDS))
    val waitTimeOut = Timeout(FiniteDuration((timeoutDuration.toMillis * 1.2).toLong, TimeUnit.MILLISECONDS))
    val responseFutures = actors.map { a => Patterns.ask(a, queryMessage, askTimeOut) }

    val responsesFuture = Future.sequence(responseFutures)

    Await.result(responsesFuture, waitTimeOut.duration * actors.size).asInstanceOf[List[T]]
  }
}