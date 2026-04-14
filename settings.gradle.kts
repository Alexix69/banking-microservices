rootProject.name = "banking-microservices"

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

include("customers-service", "accounts-service")
