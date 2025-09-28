#!/bin/bash
# 发布该包到本地maven 仓库

mvn install:install-file \
	-Dfile=./rjson-1.3.1-20210724.182155-1.jar \
	-DgroupId=tv.twelvetone.rjson \
	-DartifactId=rjson \
	-Dpackaging=jar \
	-Dversion=1.3.1-SNAPSHOT
