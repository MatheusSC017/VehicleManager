package simulations.maintenance

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.concurrent.ConcurrentLinkedQueue

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class StressTest extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:80")
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val vehicleFeeder = csv("data/vehicles.csv").circular
  val infoFeeder = Iterator.continually(Map("additionalInfo" -> s"Info-${scala.util.Random.nextInt(1000)}"))

  object SharedData {
    val vehicleIds = new ConcurrentLinkedQueue[String]()
  }

  val authenticate = exec(
    http("Login")
      .post("/api/auth/login")
      .body(StringBody("""{"username":"admin", "password":"Admin5432"}"""))
      .check(status.is(200))
      .check(jsonPath("$.token").saveAs("jwtToken"))
  ).exitHereIfFailed

  val registerVehicles = scenario("Register Vehicles")
    .exec(authenticate)
    .pause(100.milliseconds, 300.milliseconds)
    .feed(vehicleFeeder)
    .exec(
      http("Create Vehicle")
        .post("/api/vehicles")
        .header("Authorization", "Bearer #{jwtToken}")
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
        .check(status.in(201))
        .check(
          status.saveAs("vehicleCreateStatus"),
          jsonPath("$.id").optional.saveAs("vehicleId")
        )
    )
    .doIf(session => session.contains("vehicleId")) {
      exec { session =>
        SharedData.vehicleIds.add(session("vehicleId").as[String])
        session
      }
    }

  val stressScenario = scenario("Maintenance CRUD Test")
    .exec(authenticate)
    .pause(100.milliseconds, 300.milliseconds)
    .feed(infoFeeder)
    .exec { session =>
      val ids = SharedData.vehicleIds.asScala.toVector
      if (ids.nonEmpty) {
        val randomId = ids(scala.util.Random.nextInt(ids.size))
        session.set("vehicleId", randomId)
      } else {
        session.markAsFailed
      }
    }
    .during(3.minutes) {
      randomSwitch(
        60.0 -> exec(
          http("Get All Maintenances")
            .get("/api/maintenances")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        ),
        30.0 -> exec(
          http("Create Maintenance")
            .post("/api/maintenances")
            .header("Authorization", "Bearer #{jwtToken}")
            .body(StringBody(
              """{
            "vehicleId": #{vehicleId},
            "additionalInfo": "#{additionalInfo}"
          }"""
            )).asJson
            .check(status.in(200 to 499))
            .check(
              status.saveAs("createStatus"),
              jsonPath("$.id").optional.saveAs("maintenanceId")
            )
        )
        .doIfOrElse(session => session("createStatus").as[Int] == 201) {
          exec(
            http("Delete Maintenance")
              .delete("/api/maintenances/#{maintenanceId}")
              .header("Authorization", "Bearer #{jwtToken}")
              .check(status.in(200 to 499))
          )
        } {
          exitHereIfFailed
        },
        10.0 -> doIf(session => session.contains("maintenanceId")) {
          exec(
            http("Get Maintenance by ID")
              .get("/api/maintenances/#{maintenanceId}")
              .header("Authorization", "Bearer #{jwtToken}")
              .check(status.in(200 to 499))
          )
        }
      )
      .pause(1.seconds, 3.seconds)
    }

  setUp(
    registerVehicles.inject(atOnceUsers(10)).andThen(
      stressScenario.inject(
        rampUsersPerSec(5) to 15 during (1.minute),
        rampUsersPerSec(15) to 25 during (1.minute),
        rampUsersPerSec(25) to 35 during (1.minute),
        constantUsersPerSec(35) during (2.minutes),
        rampUsersPerSec(35) to 5 during (2.minutes)
      )
    )
  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(6000),
      global.responseTime.mean.lt(500),
      global.successfulRequests.percent.gt(70),
      global.responseTime.percentile3.lt(1500)
    )
}
