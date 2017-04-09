package org.pandawarrior.app

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Paths
import javax.xml.bind.JAXBContext


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

fun stringXmlToDatabase(headers: ArrayList<String>, fileName: String) {
    val dbName = "xml_translation"
    val tableName = "translation"
    lock(dbName, tableName) { statement, connection ->
        val createHeaderString = headers.joinToString { "${it} string" }.replace('-', '_')
        statement.executeUpdate("drop table if exists `$tableName`")
        statement.executeUpdate("create table `$tableName` (name string, translatable string, ${createHeaderString})")
        for (header in headers) {
            val file = File("$header${File.separator}$fileName")
            val jaxbContext = JAXBContext.newInstance(AStringResource::class.java)
            val jaxbUnmarchaller = jaxbContext.createUnmarshaller()
            val aResources = jaxbUnmarchaller.unmarshal(file) as AStringResource
            if (aResources.aStringList == null) {
                throw Exception("Parsing Error")
            }
            var aStringList: List<AString> = aResources.aStringList!!
            for (aString in aStringList) {
                val stmt = connection.prepareStatement("UPDATE `$tableName` SET ${header.replace('-', '_')} = ? WHERE name= ?")
                stmt.setString(1, aString.text)
                stmt.setString(2, aString.name)
                val updateCount = stmt.executeUpdate()
                if (updateCount < 1) {
                    val stmt = connection.prepareStatement("INSERT INTO `$tableName` (name, translatable, ${header.replace('-', '_')}) VALUES(?, ?, ?)")
                    stmt.setString(1, aString.text)
                    stmt.setString(2, aString.translatable)
                    stmt.setString(3, aString.text)
                    stmt.executeUpdate()
                }
            }
        }
    }
}

fun pluralXmlToDatabase(headers: ArrayList<String>, fileName: String) {
    val dbName = "xml_translation"
    val tableName = "translation"
    lock(dbName, tableName) { statement, connection ->
        //TODO
    }
}

fun arrayXmlToDatabase(headers: ArrayList<String>, fileName: String) {
    val dbName = "xml_translation"
    val tableName = "translation"
    lock(dbName, tableName) { statement, connection ->
        //TODO
    }
}

fun databaseToCSV(headers: ArrayList<String>, fileName: String) {
    val dbName = "xml_translation"
    val tableName = "translation"
    lock(dbName, tableName) { statement, connection ->
        headers.add(0, "name")
        headers.add(1, "translatable")
        val csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n")
        //initialize FileWriter object
        val fileWriter = FileWriter(fileName)
        val csvFilePrinter = CSVPrinter(fileWriter, csvFileFormat)
        try {
            csvFilePrinter.printRecord(headers)
            val rs = statement.executeQuery("select * from $tableName")
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