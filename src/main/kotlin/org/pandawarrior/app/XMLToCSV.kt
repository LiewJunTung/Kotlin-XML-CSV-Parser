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

enum class TranslationType {
    NORMAL, PLURALS, ARRAYS
}

fun processXMLToCSV(readPath: String, writePath: String, translationType: TranslationType) {
    val list = getHeadersFromDirectory(readPath)
    arrayXmlToDatabase(list)
    val tableName = when (translationType) {
        TranslationType.NORMAL -> "arrays_translation"
        TranslationType.PLURALS -> "plurals_translation"
        TranslationType.ARRAYS -> "translation"
        else -> "translation"
    }
    databaseToCSV(list, writePath, arrayOf("name"), tableName)
}

fun getHeadersFromDirectory(path: String = "."): ArrayList<String> {
    val currentPath = Paths.get(path).toAbsolutePath().normalize().toString()
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
    val dbName = "build/xml_translation"
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

fun pluralXmlToDatabase(headers: ArrayList<String>) {
    val dbName = "build/xml_translation"
    val tableName = "plurals_translation"
    lock(dbName, tableName) { statement, connection ->
        //TODO
        val createHeaderString = headers.joinToString { "${it} string" }.replace('-', '_')
        statement.executeUpdate("drop table if exists `$tableName`")
        statement.executeUpdate("create table `$tableName` (id INTEGER PRIMARY KEY, name string, quantity string, ${createHeaderString})")
        for (header in headers) {
            var base: Int = 0
            val file = File("$header${File.separator}plurals.xml")
            val jaxbContext = JAXBContext.newInstance(APluralResource::class.java)
            val jaxbUnmarchaller = jaxbContext.createUnmarshaller()
            val aResources = jaxbUnmarchaller.unmarshal(file) as APluralResource
            var aPluralList: List<APlural> = aResources.aPluralList
            for (aPlural in aPluralList) {
                for ((index, aPluralItem) in aPlural.aPluralItems.withIndex()) {
                    base += 1
                    val stmt = connection.prepareStatement("UPDATE `$tableName` SET ${header.replace('-', '_')} = ?, quantity = ? WHERE name= ? AND id=?")
                    stmt.setString(1, aPluralItem.text)
                    stmt.setString(2, aPluralItem.quantity)
                    stmt.setString(3, aPlural.name)
                    stmt.setInt(4, base)
                    val updateCount = stmt.executeUpdate()
                    if (updateCount < 1) {
                        val stmt = connection.prepareStatement("INSERT INTO `$tableName` (name, quantity, ${header.replace('-', '_')}) VALUES(?, ?, ?)")
                        stmt.setString(1, aPlural.name)
                        stmt.setString(2, aPluralItem.quantity)
                        stmt.setString(3, aPluralItem.text)
                        stmt.executeUpdate()
                    }
                }
            }
        }
    }
}

fun arrayXmlToDatabase(headers: ArrayList<String>) {
    val dbName = "build/xml_translation"
    val tableName = "arrays_translation"
    lock(dbName, tableName) { statement, connection ->
        //TODO

        val createHeaderString = headers.joinToString { "${it} string" }.replace('-', '_')
        statement.executeUpdate("drop table if exists `$tableName`")
        statement.executeUpdate("create table `$tableName` (id INTEGER PRIMARY KEY, name string, ${createHeaderString})")
        for (header in headers) {
            var base: Int = 0
            val file = File("$header${File.separator}arrays.xml")
            val jaxbContext = JAXBContext.newInstance(AArrayResource::class.java)
            val jaxbUnmarchaller = jaxbContext.createUnmarshaller()
            val aResources = jaxbUnmarchaller.unmarshal(file) as AArrayResource
            val aArrayList: List<AArray> = aResources.aArrayList
            var tempArrayName = ""
            for (aArray in aArrayList) {
                for ((index, aArrayItem) in aArray.aArrayItem.withIndex()) {
                    base += 1
                    val stmt = connection.prepareStatement("UPDATE `$tableName` SET ${header.replace('-', '_')} = ? WHERE name= ? AND id=?")
                    stmt.setString(1, aArrayItem.text)
                    stmt.setString(2, aArray.name)
                    stmt.setInt(3, base)
                    val updateCount = stmt.executeUpdate()
                    if (updateCount < 1) {
                        val stmt = connection.prepareStatement("INSERT INTO `$tableName` (name, ${header.replace('-', '_')}) VALUES(?, ?)")
                        stmt.setString(1, aArray.name)
                        stmt.setString(2, aArrayItem.text)
                        stmt.executeUpdate()
                    }
                }
            }
        }
    }
}

fun databaseToCSV(headers: ArrayList<String>, fileName: String, columns: Array<String>, tableName: String) {
    val dbName = "build/xml_translation"
    lock(dbName, tableName) { statement, connection ->
        for ((index, value) in columns.withIndex()) {
            headers.add(index, value)
        }
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