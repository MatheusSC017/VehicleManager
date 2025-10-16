package simulations.maintenance

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class BasicTest extends Simulation {

  val config = ConfigFactory.load()
  val baseUrl = config.getString("vehicle-manager.baseUrl")
  val username = config.getString("vehicle-manager.username")
  val password = config.getString("vehicle-manager.password")

  val httpProtocol = http
    .baseUrl(baseUrl)
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
      .body(StringBody(s"""{"username":"$username", "password":"$password"}"""))
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

  val scn = scenario("Maintenance CRUD Test")
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
    .exec(
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
        http("Get All Maintenances")
          .get("/api/maintenances")
          .header("Authorization", "Bearer #{jwtToken}")
          .check(status.in(200 to 499))
      )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get Maintenance by ID")
            .get("/api/maintenances/#{maintenanceId}")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get Maintenances by Vehicle ID")
            .get("/api/maintenances/vehicle/#{vehicleId}")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Delete Maintenance")
            .delete("/api/maintenances/#{maintenanceId}")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        )
    } {
      exitHereIfFailed
    }

  setUp(
    registerVehicles.inject(atOnceUsers(10)).andThen(
      scn.inject(
        constantUsersPerSec(5) during (20.seconds),
        rampUsersPerSec(5) to 20 during (30.seconds),
        constantUsersPerSec(20) during (1.minute),
        rampUsersPerSec(20) to 5 during (30.seconds)
      )
    )

  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(4000),
      global.responseTime.mean.lt(1000),
      global.successfulRequests.percent.gt(95)
    )

}
