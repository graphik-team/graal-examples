#!/bin/sh

cd $1
mvn clean package
cd -

rm -rf /tmp/graal-$1-example/
mkdir /tmp/graal-$1-example/
cp -r $1/data/ /tmp/graal-$1-example/
cp -r $1/src/main/java/fr/ /tmp/graal-$1-example/
cp    $1/target/graal-$1-*.jar /tmp/graal-$1-example/

current_dir=$(pwd)
cd /tmp/
zip -r $current_dir/graal-$1-example.zip ./graal-$1-example/*
cd -
