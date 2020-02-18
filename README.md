# Chess strong moves extractor
[Page reference](http://www.cs.put.poznan.pl/mszelag/Teaching/teaching.html)

## Requirements
- Java 13+
- Gradle 6.0.1 (wrapper included however)

## How to build executable jar?
You need to execute fat jar build task with gradle - `./gradlew shadowJar` in main project dir.
Result will lend in `build/libs` and be named like `strong-moves-extract-<version>.jar`.

You may also use build & run script `strong-moves-extract`; then you should specify `JAVA_HOME`.

## Running
To run a program one needs to specify input and output paths as well as configuration file.
Input path is the path to the `pgn` file, output path indicates output pgn file. Both paths may be relative or absolute.
By default there is one default configuration file `defaultConfig.json` that mainly contains server's connection
configuration.

Assuming that [UCI server](http://www.cs.put.poznan.pl/mszelag/Software/software.html) is up and running to
run the program with the default configuration one should specify the following command line arguments:
```
src/main/resources/pgn/pgn.pgn output.pgn -e defaultConfig.json
```
where
`src/main/resources/pgn/pgn.pgn` is the input path, `output.pgn` is the output path and `-e defaultConfig.json` is the
default configuration file.

If the server is running locally `defaultConfig.json` should be fine. To connect with the external server,
adjust the config file appropriately.

Other command line arguments are available by setting `--help` argument.
