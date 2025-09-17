package simulations.sale

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.concurrent.ConcurrentLinkedQueue

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class StressTest  extends Simulation {

  val httpProtocol = http
    .baseUrl("http://localhost:80")
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

  val stressScenario = scenario("Sale CRUD Test")
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
    .during(3.minutes) {
      randomSwitch(
        60.0 -> exec(
          http("Get All Sales")
            .get("/api/sales")
            .header("Authorization", "Bearer #{jwtToken}")
            .check(status.in(200 to 499))
        ),
        30.0 -> exec(
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
          ).doIf(session => session("createStatus").asOption[Int].contains(201)) {
            exec(
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
          },
        10.0 -> doIf(session => session.contains("saleId")) {
          exec(
            http("Get Sale by ID")
              .get("/api/sales/#{saleId}")
              .header("Authorization", "Bearer #{jwtToken}")
              .check(status.in(200 to 499))
          )
        }
      )
        .pause(1.seconds, 3.seconds)
    }

  setUp(
    registerVehicles.inject(atOnceUsers(100)).andThen(
      registerClients.inject(atOnceUsers(100)).andThen(
        stressScenario.inject(
          rampUsersPerSec(10) to 20 during (1.minute),
          rampUsersPerSec(20) to 35 during (1.minute),
          rampUsersPerSec(35) to 60 during (1.minute),
          constantUsersPerSec(60) during (2.minutes),
          rampUsersPerSec(60) to 10 during (2.minutes)
        )
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
