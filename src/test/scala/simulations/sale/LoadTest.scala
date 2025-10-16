package simulations.sale

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.concurrent.ConcurrentLinkedQueue
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class LoadTest extends Simulation {

  val config = ConfigFactory.load()
  val baseUrl = config.getString("vehicle-manager.baseUrl")
  val username = config.getString("vehicle-manager.username")
  val password = config.getString("vehicle-manager.password")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val vehicleFeeder = csv("data/vehicles.csv").circular
  val clientFeeder = csv("data/clients.csv").circular

  object SharedData {
    val vehicleIds = new ConcurrentLinkedQueue[String]()
    val clientIds = new ConcurrentLinkedQueue[String]()
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

  val registerClients = scenario("Register Clients")
    .exec(authenticate)
    .pause(100.milliseconds, 300.milliseconds)
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
        .check(status.in(201))
        .check(
          status.saveAs("clientCreateStatus"),
          jsonPath("$.id").optional.saveAs("clientId")
        )
    )
    .doIf(session => session.contains("clientId")) {
      exec { session =>
        SharedData.clientIds.add(session("clientId").as[String])
        session
      }
    }

  val crudScenario = scenario("Sale CRUD Test")
    .exec(authenticate)
    .pause(100.milliseconds, 300.milliseconds)
    .exec { session =>
      val ids = SharedData.vehicleIds.asScala.toVector
      if (ids.nonEmpty) {
        val randomId = ids(scala.util.Random.nextInt(ids.size))
        session.set("vehicleId", randomId)
      } else {
        session.markAsFailed
      }
    }
    .exec { session =>
      val ids = SharedData.clientIds.asScala.toVector
      if (ids.nonEmpty) {
        val randomId = ids(scala.util.Random.nextInt(ids.size))
        session.set("clientId", randomId)
      } else {
        session.markAsFailed
      }
    }
    .exec(
      http("Create Sale")
        .post("/api/sales")
        .header("Authorization", "Bearer #{jwtToken}")
        .body(StringBody(
          """{
            "client": #{clientId},
            "vehicle": #{vehicleId},
            "status": "SOLD"
          }"""
        )).asJson
        .check(status.in(200 to 499))
        .check(
          status.saveAs("createStatus"),
          jsonPath("$.id").optional.saveAs("saleId")
        )
    )
    .doIfOrElse(session => session.contains("createStatus") && session("createStatus").as[Int] == 201) {
      exec(
        http("Get All Sales")
          .get("/api/sales")
          .header("Authorization", "Bearer #{jwtToken}")
          .check(status.in(200 to 499))
      )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get Sale by ID")
            .get("/api/sales/#{saleId}")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get Sales by Vehicle ID")
            .get("/api/sales/vehicle/#{vehicleId}")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Update Sale")
            .put("/api/sales/#{saleId}")
            .header("Authorization", "Bearer #{jwtToken}")
            .body(StringBody(
              """{
                "client": #{clientId},
                "vehicle": #{vehicleId},
                "status": "CANCELED"
              }"""
            )).asJson
            .check(status.in(200 to 499))
        )
    } {
      exitHereIfFailed
    }

  val readOnlyScenario = scenario("Load Test - Read Only")
    .exec(authenticate)
    .pause(100.milliseconds, 300.milliseconds)
    .exec { session =>
      val ids = SharedData.vehicleIds.asScala.toVector
      if (ids.nonEmpty) {
        val randomId = ids(scala.util.Random.nextInt(ids.size))
        session.set("vehicleId", randomId)
      } else {
        session.markAsFailed
      }
    }
    .repeat(5) {
      pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get All Sales")
            .get("/api/sales")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        )
        .pause(100.milliseconds, 300.milliseconds)
        .exec(
          http("Get Sales by Vehicle ID")
            .get("/api/sales/vehicle/#{vehicleId}")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        )
    }

  setUp(
    registerVehicles.inject(atOnceUsers(100)).andThen(
      registerClients.inject(atOnceUsers(100)).andThen(
        crudScenario.inject(
          rampUsersPerSec(2) to 30 during (2.minutes),
          constantUsersPerSec(30) during (10.minutes),
          rampUsersPerSec(30) to 2 during (2.minutes)
        ),
        readOnlyScenario.inject(
          rampUsersPerSec(2) to 30 during (2.minutes),
          constantUsersPerSec(30) during (10.minutes),
          rampUsersPerSec(30) to 2 during (2.minutes)
        )
      )
    )

  ).protocols(httpProtocol)
    .assertions(
      global.responseTime.max.lt(4000),
      global.responseTime.mean.lt(1000),
      global.successfulRequests.percent.gt(95)
    )
}
