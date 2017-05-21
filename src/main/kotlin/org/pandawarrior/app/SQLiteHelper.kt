package org.pandawarrior.app

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement






/**
 * Created by pandawarrior91 on 26/03/2017.
 */

inline fun lock(dbName: String, tableName: String, body: (statement: Statement, connection: Connection) -> Unit) {
    // ...
    Class.forName("org.sqlite.JDBC")
    var connection: Connection? = null
    try {
        connection = DriverManager.getConnection("jdbc:sqlite:${dbName}")
        val statement = connection.createStatement()
        statement.setQueryTimeout(30)
        body(statement, connection)
        val rs = statement.executeQuery("select * from $tableName")

    } catch (exception: SQLException) {
        System.err.println(exception.message)
        exception.printStackTrace()
    } finally {
        try
        {
            if(connection != null)
                connection.close()
        }
        catch(e:SQLException)
        {
            System.err.println(e.message)
        }
    }
}

fun resetDatabase(dbName: String = "", tableName: String) {
    lock(dbName, tableName) { statement, connection ->
        statement.executeUpdate("drop table if exists `$tableName`")
    }
}

fun writeFromXML(dbName: String = "", tableName: String, currentHeader: String, data: List<AString>?) {
    lock(dbName, tableName) { statement, connection ->
        statement.setQueryTimeout(30)  // set timeout to 30 sec.
        data?.forEach {
            val translatable =
                    if (it.translatable == null) "${it.translatable}"
                    else "\"${it.translatable}\""
            //println("insert into $tableName (name, translatable, $currentHeader) values('${it.name}', $translatable, '${it.text}')")
            statement.executeUpdate("insert into $tableName (name, translatable, $currentHeader) values('${it.name}', $translatable, '${it.text}')")
        }
    }
}

class InvalidSourceException(override val message: String = "Invalid Source Format") : Exception()