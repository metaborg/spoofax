#!/bin/bash -e

cd `dirname $0`

rm -rf editorservice
mdkir editorservice
cd editorservice

ES=../../src/syntax/EditorService

../../sdf2imp -i $ES.def -p $ES.tbl -m EditorService -s Module -e esv --verbose 2
../../sdf2imp -i $ES.def --verbose 2
