package com.github.rezaiyan.subscriptionmanager

import java.sql.DriverManager
import java.sql.SQLException

/**
 * A utility class to check PostgreSQL connection.
 * This can be used to verify if PostgreSQL is installed and accessible.
 */
object PostgreSQLConnectionChecker {

    /**
     * Checks if PostgreSQL is installed and accessible.
     * 
     * @param url The JDBC URL for PostgreSQL
     * @param username The PostgreSQL username
     * @param password The PostgreSQL password
     * @return A pair containing a boolean indicating success/failure and a message
     */
    fun checkConnection(
        url: String = "jdbc:postgresql://localhost:5432/subscriptiondb",
        username: String = "postgres",
        password: String = "postgres"
    ): Pair<Boolean, String> {
        return try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver")
            
            // Try to establish a connection
            val connection = DriverManager.getConnection(url, username, password)
            
            // Execute a simple query
            val statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT version()")
            
            // Get the PostgreSQL version
            val version = if (resultSet.next()) resultSet.getString(1) else "Unknown"
            
            // Close resources
            resultSet.close()
            statement.close()
            connection.close()
            
            Pair(true, "Successfully connected to PostgreSQL. Version: $version")
        } catch (e: ClassNotFoundException) {
            Pair(false, "PostgreSQL JDBC driver not found. PostgreSQL might not be installed or the driver is missing: ${e.message}")
        } catch (e: SQLException) {
            Pair(false, "Failed to connect to PostgreSQL: ${e.message}")
        } catch (e: Exception) {
            Pair(false, "Unexpected error: ${e.message}")
        }
    }

    /**
     * Main method to run the connection check from command line.
     */
    @JvmStatic
    fun main(args: Array<String>) {
        println("Checking PostgreSQL connection...")
        val (success, message) = checkConnection()
        println(message)
        
        if (!success) {
            println("\nPossible solutions:")
            println("1. Make sure PostgreSQL is installed and running")
            println("2. Verify the database name, username, and password in application.properties")
            println("3. Check if the PostgreSQL port (default: 5432) is accessible")
            println("4. For development, consider using the 'dev' profile which uses H2 database:")
            println("   ./gradlew bootRun --args='--spring.profiles.active=dev'")
        }
    }
}