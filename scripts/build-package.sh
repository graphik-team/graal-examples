#!/bin/sh

cd $1
mvn -q clean
cd -
zip graal-$1-maven-example.zip $1/*
cd $1
mvn -q package
cd -

$tmpdir=$(mktemp)
mkdir $tmpdir/graal-$1-example/
cp -r $1/data/ $tmpdir/graal-$1-example/
cp -r $1/src/main/java/fr/ $tmpdir/graal-$1-example/
cp    $1/target/graal-$1-*.jar $tmpdir/graal-$1-example/

current_dir=$(pwd)
cd $tmpdir
zip -r $current_dir/graal-$1-example.zip ./graal-$1-example/*
cd -
