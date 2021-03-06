#!/usr/bin/env bash
# Adapt the two following lines -- NB: Java 13 is required
#JAVA_HOME="C:/dev/Java/JDK13"
GRADLE_EXE_PATH="./gradlew"

# -- You normally do not need to change anything beyond this point --
JAR_PATH="./build/libs" #default path where gradle puts outputs, however may be changed later as same as jar name if necessary
VERSION="1.0"
PROJECT_NAME="strong-moves-extract"
JAR_FILE_NAME="$PROJECT_NAME-$VERSION.jar"

JAVA="${JAVA_HOME}/bin/java"

export JAVA_HOME
if [[ ! -f ${JAR_PATH}/${JAR_FILE_NAME} ]]; then
    if [[ ! -f ${GRADLE_EXE_PATH} ]]; then
        echo "Please modify ${PROJECT_NAME}.sh to reflect your gradle installation (see README)" >&2
            exit -1;
    fi
    ${GRADLE_EXE_PATH} shadowJar
fi

CMD="${JAVA} -jar ${JAR_PATH}/${JAR_FILE_NAME}"
${CMD} "$@"
exit $?
