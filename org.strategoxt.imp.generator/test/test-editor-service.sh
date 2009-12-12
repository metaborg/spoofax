#!/bin/bash -e

cd `dirname $0`

rm -rf editorservice
mkdir editorservice
cd editorservice

ES=../../src/syntax/EditorService

../../sdf2imp -i $ES.def -p $ES.tbl -m EditorService -s Module -e esv --verbose 2
../../sdf2imp -i $ES.def -m EditorService --verbose 2

cd ..
rm -rf editorservice
