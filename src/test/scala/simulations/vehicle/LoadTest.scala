package simulations.vehicle


import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class LoadTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:80")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val vehicleFeeder = csv("data/vehicles.csv").circular

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
      http("Get All Vehicles")
        .get("/api/vehicles")
        .header("Authorization", s"Bearer #{jwtToken}")
        .check(status.is(200))
    )
    .pause(100.milliseconds, 300.milliseconds)
    .feed(vehicleFeeder)
    .exec(
      http("Create Vehicle")
        .post("/api/vehicles")
        .header("Authorization", s"Bearer #{jwtToken}")
        .body(StringBody(
          """{
            "vehicleType": "#{vehicleType}",
            "model": "#{model}",
            "brand": "#{brand}",
            "year": "#{year}",
            "color": "#{color}",
            "plate": "#{plate}",
            "chassi": "#{chassi}",
            "mileage": "#{mileage}",
            "price": "#{price}",
            "vehicleFuel": "#{vehicleFuel}",
            "vehicleChange": "#{vehicleChange}",
            "doors": "#{doors}",
            "motor": "#{motor}",
            "power": "#{power}"
          }"""
        )).asJson
        .check(status.in(200 to 499))
        .check(
          status.saveAs("createStatus"),
          jsonPath("$.id").optional.saveAs("vehicleId")
        )
    )
    .doIfOrElse(session => session("createStatus").asOption[Int].contains(201)) {
      exec(
        http("Get Vehicle by ID")
          .get("/api/vehicles/#{vehicleId}")
          .header("Authorization", s"Bearer #{jwtToken}")
          .check(status.is(200))
      )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Update Vehicle")
            .put("/api/vehicles/#{vehicleId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .body(StringBody(
              """{
                "id": #{vehicleId},
                "vehicleType": "#{vehicleType}",
                "model": "#{model} updated",
                "brand": "#{brand} updated",
                "year": "#{year}",
                "color": "#{color} updated",
                "plate": "#{plate}",
                "chassi": "#{chassi}",
                "mileage": "#{mileage}",
                "price": "#{price}",
                "vehicleFuel": "#{vehicleFuel}",
                "vehicleChange": "#{vehicleChange}",
                "doors": "#{doors}",
                "motor": "#{motor}",
                "power": "#{power}"
              }"""
            )).asJson
            .check(status.is(200))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Delete Vehicle")
            .delete("/api/vehicles/#{vehicleId}")
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
          http("Get All Vehicles")
            .get("/api/vehicles")
            .header("Authorization", s"Bearer #{jwtToken}")
            .check(status.is(200))
        )
    }

  setUp(

    readOnlyScenario.inject(
      rampUsersPerSec(5) to 80 during (2.minutes),
      constantUsersPerSec(80) during (10.minutes),
      rampUsersPerSec(80) to 5 during (2.minutes)
    ),
    crudScenario.inject(
      rampUsersPerSec(2) to 30 during (2.minutes),
      constantUsersPerSec(30) during (10.minutes),
      rampUsersPerSec(30) to 2 during (2.minutes)
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
