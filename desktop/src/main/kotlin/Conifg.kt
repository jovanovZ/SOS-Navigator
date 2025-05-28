import io.github.cdimascio.dotenv.dotenv

val dotenv = dotenv()
val BACKEND_URL: String = dotenv["BACKEND_URL"] ?: "http://localhost"