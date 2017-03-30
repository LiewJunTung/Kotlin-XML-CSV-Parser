package org.pandawarrior.app

import java.io.File
import java.io.StringReader
import java.nio.file.Paths
import javax.xml.bind.JAXBContext
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import org.apache.commons.csv.CSVFormat
import java.io.IOException


/**
 * Created by jtlie on 3/31/2017.
 */

fun getHeadersFromDirectory(): ArrayList<String> {
    val currentPath = Paths.get(".").toAbsolutePath().normalize().toString()
    val folder = File(currentPath)
    val lists = ArrayList<String>()
    for (file in folder.listFiles()) {
        if (file.isDirectory && file.name.contains("value")) {
            lists.add(file.name)
        }
    }
    return lists
}

fun xmlToDatabase(dbName: String, headers: ArrayList<String>, fileName: String) {
    lock(dbName) { statement, connection ->
        val createHeaderString = headers.joinToString { "${it} string" }.replace('-', '_')
        statement.executeUpdate("drop table if exists `translation`")
        statement.executeUpdate("create table `translation` (name string, translatable string, ${createHeaderString})")
        for (header in headers) {
            val file = File("$header${File.separator}$fileName")
            val jaxbContext = JAXBContext.newInstance(AResounce::class.java)
            val jaxbUnmarchaller = jaxbContext.createUnmarshaller()
            val aResources = jaxbUnmarchaller.unmarshal(file) as AResounce
            if (aResources.aStringList == null) {
                throw Exception("Parsing Error")
            }
            var aStringList: List<AString> = aResources.aStringList!!
            for (aString in aStringList) {
                val stmt = connection.prepareStatement("UPDATE `translation` SET ${header.replace('-', '_')} = ? WHERE name= ?")
                stmt.setString(1, aString.text)
                stmt.setString(2, aString.name)
                val updateCount = stmt.executeUpdate()
                if (updateCount < 1) {
                    val stmt = connection.prepareStatement("INSERT INTO `translation` (name, translatable, ${header.replace('-', '_')}) VALUES(?, ?, ?)")
                    stmt.setString(1, aString.text)
                    stmt.setString(2, aString.translatable)
                    stmt.setString(3, aString.text)
                    stmt.executeUpdate()
                }
            }
        }
    }
}

fun databaseToCSV(dbName: String, headers: ArrayList<String>, fileName: String) {
    lock(dbName) { statement, connection ->
        headers.add(0, "name")
        headers.add(1, "translatable")
        val csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n")
        //initialize FileWriter object
        val fileWriter = FileWriter(fileName)
        val csvFilePrinter = CSVPrinter(fileWriter, csvFileFormat)
        try {
            csvFilePrinter.printRecord(headers)
            val rs = statement.executeQuery("select * from translation")
            while (rs.next()) {
                val row = ArrayList<String>()
                // row.add(rs.getString(rs.findColumn("name")))
                // row.add(rs.getString(rs.findColumn("translatable")))

                for (header in headers) {
                    val string = rs.getString(rs.findColumn(header.replace('-', '_')))
                    row.add(string)
                }
                csvFilePrinter.printRecord(row)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fileWriter.flush()
                fileWriter.close()
                csvFilePrinter.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}