package simulations.vehicle

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class BasicTest extends Simulation {

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
      .check(bodyString.saveAs("loginResponse"))
  ).exitHereIfFailed

  val scn = scenario("Basic Vehicle Test")
    .exec(authenticate)
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
    .doIfOrElse(session => session("createStatus").as[Int] == 201) {
      exec(
        http("Get All Vehicles")
          .get("/api/vehicles")
          .header("Authorization", s"Bearer #{jwtToken}")
          .check(status.in(200 to 499))
      )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get Vehicle by ID")
            .get("/api/vehicles/#{vehicleId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .check(status.in(200 to 499))
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
            .check(status.in(200 to 499))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Delete Vehicle")
            .delete("/api/vehicles/#{vehicleId}")
            .header("Authorization", s"Bearer #{jwtToken}")
            .check(status.in(200 to 499))
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
