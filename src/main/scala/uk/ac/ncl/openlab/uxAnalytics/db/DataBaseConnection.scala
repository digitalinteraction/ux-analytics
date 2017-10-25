package uk.ac.ncl.openlab.uxAnalytics.db

import java.sql.{Connection, DriverManager}

import anorm.AnormException
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import org.postgresql.util.PSQLException

import scala.util.{Failure, Try}

/**
  * Created by Tim Osadchiy on 20/10/2017.
  */

protected object DataBaseConnection {

  private val dataSource = {
    DriverManager.registerDriver(new org.postgresql.Driver)

    val config = ConfigFactory.load()
    val username = config.getString("db.username")
    val password = config.getString("db.password")
    val host = config.getString("db.host")
    val port = config.getString("db.port")
    val dbName = config.getString("db.name")

    import com.zaxxer.hikari.HikariConfig

    val hikConfig = new HikariConfig
    hikConfig.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName)
    hikConfig.setUsername(username)
    hikConfig.setPassword(password)
    hikConfig.setMaximumPoolSize(3)
    hikConfig.setMinimumIdle(1)
    new HikariDataSource(hikConfig)
  }

  def getConnection = dataSource.getConnection

  def tryWithConnection[T](block: Connection => Try[T]): Try[T] = {
    val conn = getConnection
    try {
      block(conn)
    } catch {
      case e: Throwable => {
        if (!conn.getAutoCommit())
          conn.rollback()
        e match {
          case batchException: java.sql.BatchUpdateException => Failure(batchException.getNextException.asInstanceOf[PSQLException])
          case sqlException: PSQLException => Failure(sqlException)
          case anormException: AnormException => Failure(anormException)
          case _ => throw e
        }
      }
    } finally {
      conn.close()
    }
  }

}
