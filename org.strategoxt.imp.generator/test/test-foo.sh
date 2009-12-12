#!/bin/bash -e

cd `dirname $0`

rm -rf foo
mkdir foo
cd foo

../../sdf2imp -m Foo -e foo --verbose 2

cd ..
rm -rf foo
