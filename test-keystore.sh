#!/bin/bash
cat app/build.gradle.kts | grep -n "signingConfigs {"
