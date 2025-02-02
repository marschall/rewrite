plugins {
    id("org.openrewrite.build.language-library")
}

dependencies {
    api(project(":rewrite-java"))

    implementation("org.codehaus.groovy:groovy:latest.release")

    compileOnly(project(":rewrite-test"))
    compileOnly("org.slf4j:slf4j-api:1.7.+")

    api("io.micrometer:micrometer-core:1.9.+")

    api("org.jetbrains:annotations:latest.release")

    api("com.fasterxml.jackson.core:jackson-annotations")

    testImplementation(project(":rewrite-test"))
    testImplementation(project(":rewrite-java-test"))
    testRuntimeOnly("org.codehaus.groovy:groovy-all:latest.release")
    testRuntimeOnly(project(":rewrite-java-17"))
}
