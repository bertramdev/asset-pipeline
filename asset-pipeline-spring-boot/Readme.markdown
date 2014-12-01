Spring Boot Asset Pipeline Gradle Adapter
-----------------------------------------

This is a preliminary test project for adding support to spring boot.

It takes files stored in `assets/javascripts` , `assets/stylesheets`, `assets/images`, `assets/**/*`, and compiles them into the root jar `assets` folder.
It supports both development mode where it compiles on the fly and production `gradle assemble`

Note: Be Sure to add to your `@ComponentScan` annotation the class path `asset.pipeline.springboot`

```groovy
package demo

@Configuration
@ComponentScan(['demo','asset.pipeline.springboot'])
@EnableAutoConfiguration
class Application {

    static void main(String[] args) {
        SpringApplication.run Application, args
    }
}

```

Example Gradle File for Spring Boot:
```groovy
buildscript {
    ext {
        springBootVersion = '1.1.9.RELEASE'
    }
    repositories {
        mavenCentral()
        mavenLocal();
    }
    dependencies {
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${springBootVersion}")
        classpath("com.bertramlabs.plugins:asset-pipeline-gradle:2.0.7")
    }
}

apply plugin: 'groovy'
apply plugin: 'eclipse'
apply plugin: 'idea'
apply plugin: 'spring-boot'
apply plugin: 'asset-pipeline'

jar {
    baseName = 'demo'
    version = '0.0.1-SNAPSHOT'
    from "${buildDir}/assetCompile"
}
sourceCompatibility = 1.7
targetCompatibility = 1.7

repositories {
    mavenCentral()
    mavenLocal()
}

assets {
    compileDir = "${buildDir}/assetCompile/assets"
}


dependencies {
    compile("com.bertramlabs.plugins:asset-pipeline-spring-boot:2.0.7")
    compile("org.springframework.boot:spring-boot-starter-thymeleaf")
    compile("org.codehaus.groovy:groovy")
    testCompile("org.springframework.boot:spring-boot-starter-test")
}

eclipse {
    classpath {
         containers.remove('org.eclipse.jdt.launching.JRE_CONTAINER')
         containers 'org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.7'
    }
}

task wrapper(type: Wrapper) {
    gradleVersion = '1.12'
}

```
