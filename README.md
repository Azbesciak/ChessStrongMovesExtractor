# Chess strong moves extractor
[Page reference](http://www.cs.put.poznan.pl/mszelag/Teaching/teaching.html)

## Requirements
- Java 13+
- Gradle 6.0.1 (wrapper included however)

## How to build executable jar?
You need to execute fat jar build task with gradle - `./gradlew shadowJar` in main project dir.
Result will lend in `build/libs` and be named like `strong-moves-extract-<version>.jar`.

You may also use build & run script `strong-moves-extract`; then you should specify `JAVA_HOME` and 
