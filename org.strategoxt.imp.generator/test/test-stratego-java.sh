#!/bin/bash -e

cd `dirname $0`

rm -rf strategojava
mkdir strategojava
cd strategojava

SJ=~/.nix-profile/share/java-front/Stratego-Java-15

../../sdf2imp -i $SJ.def -m Stratego-Java-15 -p $SJ.tbl -e str -s "Module[StrategoHost]" --verbose 2
../../sdf2imp -i $SJ.def -m Stratego-Java-15
../../sdf2imp -i editor/Stratego-Java-15.main.esv
(cd editor; ../../../sdf2imp -i Stratego-Java-15.main.esv)

cd ..
rm -rf stratego-java
