package simulations.client

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class LoadTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:80")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val dynamicClientFeeder = Iterator.continually(
    Map(
      "firstName" -> s"FirstNameTest${scala.util.Random.nextInt(10000)}",
      "lastName" -> s"LastNameTest${scala.util.Random.nextInt(10000)}",
      "email" -> s"EmailTest${scala.util.Random.nextInt(50000)}@example.com",
      "phone" -> s"+55 21 ${scala.util.Random.nextInt(9000) + 1000}-${scala.util.Random.nextInt(9000) + 1000}"
    )
  )

  val authenticate = exec(
    http("Login")
      .post("/api/auth/login")
      .body(StringBody("""{"username":"admin", "password":"Admin5432"}"""))
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("jwtToken"))
  ).exitHereIfFailed

  val crudScenario = scenario("Load Test - Full CRUD")
    .exec(authenticate)
    .pause(100.milliseconds, 300.milliseconds)
    .exec(
      http("Get All Clients")
        .get("/api/clients")
        .header("Authorization", s"Bearer #{jwtToken}")
        .check(status.is(200))
    )
    .pause(100.milliseconds, 300.milliseconds)
    .feed(dynamicClientFeeder)
    .exec(
      http("Create Client")
        .post("/api/clients")
        .header("Authorization", s"Bearer #{jwtToken}")
        .body(StringBody(
          """{
            "firstName": "#{firstName}",
            "lastName": "#{lastName}",
            "email": "#{email}",
            "phone": "#{phone}"
          }"""
        )).asJson
        .check(status.in(200 to 499))
        .check(
          status.saveAs("createStatus"),
          jsonPath("$.id").optional.saveAs("clientId")
        )
    )
    .doIfOrElse(session => session("createStatus").asOption[Int].contains(201)) {
      exec(
        http("Get Client by ID")
          .get("/api/clients/#{clientId}")
          .header("Authorization", s"Bearer #{jwtToken}")
          .check(status.is(200))
      )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Update Client")
            .put("/api/clients/#{clientId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .body(StringBody(
              """{
              "id": #{clientId},
              "firstName": "#{firstName} Updated",
              "lastName": "#{lastName} Updated",
              "email": "#{email}",
              "phone": "#{phone}"
            }"""
            )).asJson
            .check(status.is(200))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Delete Client")
            .delete("/api/clients/#{clientId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .check(status.is(204))
        )
    } {
      exitHereIfFailed
    }

  val readOnlyScenario = scenario("Load Test - Read Only")
    .exec(authenticate)
    .repeat(5) {
      pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get All Clients")
            .get("/api/clients")
            .header("Authorization", s"Bearer #{jwtToken}")
            .check(status.is(200))
        )
    }

  setUp(

    readOnlyScenario.inject(
      rampUsersPerSec(5) to 100 during (2.minutes),
      constantUsersPerSec(100) during (10.minutes),
      rampUsersPerSec(100) to 5 during (2.minutes)
    ),
    crudScenario.inject(
      rampUsersPerSec(2) to 35 during (2.minutes),
      constantUsersPerSec(35) during (10.minutes),
      rampUsersPerSec(35) to 2 during (2.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(3000),
      global.responseTime.mean.lt(800),
      global.responseTime.percentile3.lt(1500),
      global.successfulRequests.percent.gt(98),
      forAll.failedRequests.count.lt(50)
    )
}
