package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class BasicClientTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:80")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val clientFeeder = csv("data/clients.csv").circular

  val authenticate = exec(
    http("Login")
      .post("/api/auth/login")
      .body(StringBody("""{"username":"admin", "password":"Admin5432"}"""))
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("jwtToken"))
      .check(bodyString.saveAs("loginResponse"))
  )

  val scn = scenario("Basic Client Test")
    .exec(authenticate)
    .pause(100.millisecond)
    .feed(clientFeeder)
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
        .check(
          status.saveAs("createStatus"),
          jsonPath("$.id").optional.saveAs("clientId")
        )
    )
    .doIfOrElse(session => session("createStatus").as[Int] == 201) {
      exec(
        http("Get All Clients")
          .get("/api/clients")
          .header("Authorization", s"Bearer #{jwtToken}")
          .check(status.is(200))
      )
        .pause(100.millisecond)
        .exec(
          http("Get Client by ID")
            .get("/api/clients/#{clientId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .check(status.is(200))
        )
        .pause(100.millisecond)
        .exec(
          http("Update Client")
            .put("/api/clients/#{clientId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .body(StringBody(
              """{
              "id": #{clientId},
              "firstName": "#{firstName} updated",
              "lastName": "#{lastName} updated",
              "email": "#{email}",
              "phone": "#{phone}"
            }"""
            )).asJson
            .check(status.is(200))
            .check(jsonPath("$.id").saveAs("clientId"))
        )
        .pause(100.millisecond)
        .exec(
          http("Delete Client")
            .delete("/api/clients/#{clientId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .check(status.is(204))
        )
    } {
      exitHereIfFailed
    }

  setUp(
    scn.inject(
      constantUsersPerSec(10) during (30.seconds),
      rampUsersPerSec(10) to 50 during (1.minute),
      constantUsersPerSec(50) during (2.minutes),
      rampUsersPerSec(50) to 10 during (1.minute)
    )
  ).protocols(httpProtocol)
   .assertions(
     global.responseTime.max.lt(5000),
     global.responseTime.mean.lt(1000),
     global.successfulRequests.percent.gt(95)
   )
}