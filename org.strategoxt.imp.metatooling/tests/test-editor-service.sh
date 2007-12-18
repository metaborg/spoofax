#!/bin/bash

cd `dirname $0`

if [ ! -d editorservice ]
then mkdir editorservice
fi

cd editorservice

ES=../../src/syntax/EditorService

../../sdf2imp -i $ES.def -p $ES.tbl -m EditorService -s Module -e esv --verbose 2 2>&1
