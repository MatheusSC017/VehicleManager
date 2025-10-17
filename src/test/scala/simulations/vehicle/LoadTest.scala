package simulations.vehicle


import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class LoadTest extends Simulation {

  val config = ConfigFactory.load()
  val baseUrl = config.getString("vehicle-manager.baseUrl")
  val username = config.getString("vehicle-manager.username")
  val password = config.getString("vehicle-manager.password")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val dynamicVehicleFeeder = Iterator.continually(
    Map(
      "vehicleType"   -> Seq("CAR", "MOTORCYCLE")(scala.util.Random.nextInt(2)),
      "vehicleStatus" -> Seq("AVAILABLE", "RESERVED", "SOLD", "MAINTENANCE")(scala.util.Random.nextInt(4)),
      "model"         -> s"Model${scala.util.Random.nextInt(1000)}",
      "brand"         -> Seq("Toyota", "Ford", "Honda", "Chevrolet", "BMW", "Mercedes")(scala.util.Random.nextInt(6)),
      "year"          -> (scala.util.Random.between(1990, 2025)).toString,
      "color"         -> Seq("Black", "White", "Silver", "Red", "Blue", "Gray")(scala.util.Random.nextInt(6)),
      "plate"         -> f"${('A' + scala.util.Random.nextInt(26)).toChar}${('A' + scala.util.Random.nextInt(26)).toChar}${('A' + scala.util.Random.nextInt(26)).toChar}-${scala.util.Random.nextInt(9999)}%04d",
      "chassi"        -> s"Chassi${scala.util.Random.nextInt(50000)}",
      "mileage"       -> BigDecimal(scala.util.Random.between(1000, 300000)).bigDecimal.toPlainString,
      "price"         -> BigDecimal(scala.util.Random.between(20000, 300000)).bigDecimal.toPlainString,
      "vehicleFuel"   -> Seq("GASOLINE", "ALCOHOL", "FLEX", "DIESEL", "HYBRID", "ELECTRIC")(scala.util.Random.nextInt(6)),
      "vehicleChange" -> Seq("MANUAL", "AUTOMATIC", "AUTOMATED", "CVT")(scala.util.Random.nextInt(4)),
      "doors"         -> scala.util.Random.nextInt(5).toString,
      "motor"         -> s"${scala.util.Random.between(1, 6)}.${scala.util.Random.between(0, 9)}",
      "power"         -> s"${scala.util.Random.between(60, 600)} HP"
    )
  )

  val authenticate = exec(
    http("Login")
      .post("/api/auth/login")
      .body(StringBody(s"""{"username":"$username", "password":"$password"}"""))
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
    .feed(dynamicVehicleFeeder)
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
        .check(status.in(200 to 400))
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
