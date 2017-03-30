package org.pandawarrior.app

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.StringReader
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.CopyOnWriteArrayList
import java.io.FileReader
import java.io.Reader






/**
 * Created by pandawarrior91 on 26/03/2017.
 */

inline fun lock(dbName: String, body: (statement: Statement, connection: Connection) -> Unit) {
    // ...
    Class.forName("org.sqlite.JDBC")
    var connection: Connection? = null
    try {
        connection = DriverManager.getConnection("jdbc:sqlite:${dbName}")
        val statement = connection.createStatement()
        statement.setQueryTimeout(30)
        body(statement, connection)
        val rs = statement.executeQuery("select * from translation")
        while(rs.next())
        {
            // read the result set
            System.out.println("name = " + rs.getString("name"))
            System.out.println("value = " + rs.getInt("value"))
        }

    } catch (exception: SQLException) {
        System.err.println(exception.message)
        exception.printStackTrace()
    } finally
    {
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

fun resetDatabase(dbName: String = ""){
    lock(dbName) { statement, connection ->
        statement.executeUpdate("drop table if exists `translation`")
    }
}

fun writeFromXML(dbName: String = "", currentHeader: String, data: List<AString>?){
    lock(dbName) { statement, connection ->
        statement.setQueryTimeout(30)  // set timeout to 30 sec.
        data?.forEach {
            val translatable =
                    if (it.translatable == null) "${it.translatable}"
                    else "\"${it.translatable}\""
            println("insert into translation (name, translatable, $currentHeader) values('${it.name}', $translatable, '${it.text}')")
            statement.executeUpdate("insert into translation (name, translatable, $currentHeader) values('${it.name}', $translatable, '${it.text}')")
        }
    }
}
