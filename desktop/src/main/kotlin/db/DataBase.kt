package db

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.reactivestreams.client.MongoClients
import com.mongodb.reactivestreams.client.MongoDatabase
import kotlinx.coroutines.reactive.awaitFirstOrNull
import io.github.cdimascio.dotenv.dotenv


object DataBase {
    private val dotenv = dotenv {
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    private val connectionString: String = dotenv["MONGO_URI"] ?: "mongodb://localhost:27017"
    private val databaseName: String = dotenv["MONGO_DB_NAME"] ?: "test"


    private val client = MongoClients.create(
        MongoClientSettings.builder()
            .applyConnectionString(ConnectionString(connectionString))
            .build()
    )

    fun getDatabase(): MongoDatabase {
        return client.getDatabase(databaseName)
    }


    suspend fun testConnection(): Boolean {
        return try {
            println(connectionString)
            println(databaseName)
            val databases = client.listDatabaseNames().awaitFirstOrNull()
            databases != null
        } catch (e: Exception) {
            println("Error connecting to MongoDB: ${e.message}")
            false
        }
    }
}