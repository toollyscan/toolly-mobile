#!/bin/sh

# Toolly browser-safe Gradle wrapper bootstrap.
# The reviewed Gradle 8.9 wrapper JAR is stored as base64 because GitHub's browser
# contents API cannot preserve binary bytes. Its SHA-256 is verified before use.

set -eu

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BOOTSTRAP_DIR="$APP_HOME/.gradle-bootstrap"
WRAPPER_JAR="$BOOTSTRAP_DIR/gradle-wrapper-8.9.jar"
WRAPPER_SOURCE="$APP_HOME/gradle/wrapper/gradle-wrapper.jar.base64"
WRAPPER_PROPERTIES="$BOOTSTRAP_DIR/gradle-wrapper-8.9.properties"
WRAPPER_PROPERTIES_SOURCE="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"
WRAPPER_SHA256="498495120a03b9a6ab5d155f5de3c8f0d986a449153702fb80fc80e134484f17"

mkdir -p "$BOOTSTRAP_DIR"

checksum() {
    if command -v sha256sum >/dev/null 2>&1; then
        sha256sum "$1" | awk '{print $1}'
    else
        shasum -a 256 "$1" | awk '{print $1}'
    fi
}

if [ ! -f "$WRAPPER_JAR" ] || [ "$(checksum "$WRAPPER_JAR")" != "$WRAPPER_SHA256" ]; then
    pending="$WRAPPER_JAR.pending"
    rm -f "$pending"
    if base64 --help 2>&1 | grep -q -- '-d'; then
        base64 -d "$WRAPPER_SOURCE" > "$pending"
    else
        base64 -D "$WRAPPER_SOURCE" > "$pending"
    fi
    if [ "$(checksum "$pending")" != "$WRAPPER_SHA256" ]; then
        rm -f "$pending"
        echo "Gradle wrapper integrity verification failed." >&2
        exit 1
    fi
    mv "$pending" "$WRAPPER_JAR"
fi

cp "$WRAPPER_PROPERTIES_SOURCE" "$WRAPPER_PROPERTIES"

if [ -n "${JAVA_HOME:-}" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
else
    JAVA_CMD=java
fi

exec "$JAVA_CMD" -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
