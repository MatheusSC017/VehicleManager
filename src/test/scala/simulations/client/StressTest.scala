package simulations.client

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class StressTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:80")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .shareConnections

  val dynamicClientFeeder = Iterator.continually(
    Map(
      "firstName" -> s"FirstNameTest${scala.util.Random.nextInt(10000)}",
      "lastName" -> s"LastNameTest${scala.util.Random.nextInt(10000)}",
      "email" -> s"EmailTest.${scala.util.Random.nextInt(1000000)}@example.com",
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

  val stressScenario = scenario("Stress Test - Push to Limits")
    .exec(authenticate)
    .during(3.minutes) {
      feed(dynamicClientFeeder)
        .randomSwitch(
          60.0 -> exec(
            http("Get All Clients")
              .get("/api/clients")
              .header("Authorization", s"Bearer #{jwtToken}")
              .check(status.in(200, 400))
          ),
          30.0 -> exec(
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
              .check(status.in(200, 400))
              .check(jsonPath("$.id").optional.saveAs("clientId"))
          ),
          10.0 -> doIf(session => session.contains("clientId")) {
            exec(
              http("Get Client by ID")
                .get("/api/clients/#{clientId}")
                .header("Authorization", s"Bearer #{jwtToken}")
                .check(status.in(200, 400))
            )
          }
        )
        .pause(1.seconds, 3.seconds)
    }

  setUp(
    stressScenario.inject(
      rampUsersPerSec(2) to 5 during (1.minute),
      rampUsersPerSec(5) to 10 during (1.minute),
      rampUsersPerSec(10) to 15 during (1.minute),
      constantUsersPerSec(15) during (2.minutes),
      rampUsersPerSec(15) to 2 during (2.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(6000),
      global.responseTime.mean.lt(500),
      global.successfulRequests.percent.gt(70),
      global.responseTime.percentile3.lt(1500)
    )
}
