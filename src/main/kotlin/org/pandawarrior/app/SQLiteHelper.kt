package org.pandawarrior.app

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.CopyOnWriteArrayList




/**
 * Created by pandawarrior91 on 26/03/2017.
 */

inline fun lock(dbName: String, body: (statement:Statement)-> Unit) {
    // ...
    Class.forName("org.sqlite.JDBC")
    var connection: Connection? = null
    try {
        connection = DriverManager.getConnection("jdbc:sqlite:${dbName}")
        val statement = connection.createStatement()
        body(statement)
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

fun writeFromCSV(dbName: String = "", headers: Array<String>, csvInputList: CopyOnWriteArrayList<Map<String, String>>) {
    lock(dbName) { statement ->
        val createHeaderString = headers.joinToString { "${it} string" }.replace('-', '_')
        val headerString = headers.joinToString { it }.replace('-', '_')
        statement.setQueryTimeout(30)  // set timeout to 30 sec.
        statement.executeUpdate("drop table if exists `translation`")
        statement.executeUpdate("create table `translation` (${createHeaderString})")
        csvInputList.forEach {
            val values = it.values.joinToString { "\"${it}\"" }
            println("insert into person (${headerString}) values(${values})")
            statement.executeUpdate("insert into translation (${headerString}) values(${values})")
        }
    }
}

fun writeFromXML(dbName: String = "", currentHeader: String, data: List<AString>?){
    lock(dbName) { statement ->
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
