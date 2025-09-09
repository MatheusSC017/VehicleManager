package simulations.vehicle

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration._

class StressTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:80")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")
    .shareConnections

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
      .body(StringBody("""{"username":"admin", "password":"Admin5432"}"""))
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("jwtToken"))
  ).exitHereIfFailed

  val stressScenario = scenario("Stress Test - Push to Limits")
    .exec(authenticate)
    .during(3.minutes) {
      feed(dynamicVehicleFeeder)
        .randomSwitch(
          60.0 -> exec(
            http("Get All Vehicles")
              .get("/api/vehicles")
              .header("Authorization", s"Bearer #{jwtToken}")
              .check(status.in(200, 400))
          ),
          30.0 -> exec(
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
              .check(status.in(201, 400))
              .check(jsonPath("$.id").optional.saveAs("vehicleId"))
          ),
          10.0 -> doIf(session => session.contains("vehicleId")) {
            exec(
              http("Get Vehicle by ID")
                .get("/api/vehicles/#{vehicleId}")
                .header("Authorization", s"Bearer #{jwtToken}")
                .check(status.in(200, 400))
            )
          }
        )
        .pause(1.seconds, 3.seconds)
    }

  setUp(
    stressScenario.inject(
      rampUsersPerSec(2) to 8 during (1.minute),
      rampUsersPerSec(8) to 15 during (1.minute),
      rampUsersPerSec(15) to 25 during (1.minute),
      constantUsersPerSec(25) during (2.minutes),
      rampUsersPerSec(25) to 2 during (2.minutes)
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(6000),
      global.responseTime.mean.lt(500),
      global.successfulRequests.percent.gt(70),
      global.responseTime.percentile3.lt(1500)
    )

}
