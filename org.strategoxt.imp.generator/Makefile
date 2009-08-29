# HACK: Assume nix installation in Makefile

STRS     = $(wildcard src/*.str) \
           $(wildcard src/sdf2imp/*.str) \
           $(wildcard src/sdf2imp/lib/*.str) \
           $(wildcard src/sdf2imp/project/*.str) \
           $(wildcard src/sdf2imp/parser/*.str) \
           $(wildcard src/sdf2imp/util/*.str) \
           $(wildcard src/sdf2imp/services/*.str)           

RTREES = $(STRS:.str=.rtree)

SDFS     = $(wildcard src/syntax/*.sdf)

SYNTAX   = src/syntax/EditorService.tbl src/syntax/EditorService.pp src/syntax/EditorService-pretty.pp.af src/syntax/EditorService.str src/syntax/Stratego-Java-EditorService.tbl

STRCFLAGS = \
	-la stratego-xtc \
    -la stratego-lib \
    -la stratego-gpp \
    -la stratego-tool-doc \
    -la stratego-sglr \
    -la stratego-rtg \
    -la stratego-sdf \
    -la stratego-aterm \
    -I ${HOME}/.nix-profile/share/java-front \
    -I ${HOME}/.nix-profile/share/gpp \
    -I ${HOME}/.nix-profile/share/xml-front \
    -I ${HOME}/.nix-profile/share/sdf-tools \
    -I ${HOME}/.nix-profile/share/stratego-front \
    -I ${HOME}/.nix-profile/share/xtc \
    `pkg-config --variable=strcxtcflags strategoxt`

JAVACFLAGS=-J-Xmx256m -J-Xms100m -J-server -J-XX:+UseParallelGC -cp strategoxt.jar -source 5 -nowarn
JAVAC=`if which ecj >/dev/null; then echo ecj; else echo javac; fi`
STRJ=`if which strj >/dev/null; then echo strj; elif [ -e strategoxt.jar ]; then echo java -server -Xss4m -jar strategoxt.jar; else echo Please install strj or put strategoxt.jar into the current directory. >&2; exit 1; fi`

SDFINCLUDES = \
    -Idef ${HOME}/.nix-profile/share/java-front-syntax/JavaMix.def \
    -Idef ${HOME}/.nix-profile/share/java-front/JavaEBlockMix.def \
    -Idef ${HOME}/.nix-profile/share/java-front/EmbeddedJavaMix.def

# I don't care much for nullary constructor warnings unless it's about lowercase ones, such as 'start'
STRCSTFU=2>&1 | grep -vE 'warning ] Nullary constructor .*[A-Z]|warning ] multiple external| \[ExtSDef\(|stratego-trm|module-name-checked|module-name-checked"' \
                 >&2

STRINCLUDES = \
	-I ${HOME}/.nix-profile/share/sdf/xml-front \
	-I ${HOME}/.nix-profile/share/sdf/gpp \
	-I ${HOME}/.nix-profile/share/sdf/stratego-regular \
	-I ${HOME}/.nix-profile/share/sdf/sdf-front \
	-I ${HOME}/.nix-profile/share/java-front \
	-I src/syntax \
    `pkg-config --variable=strcflags java-front`

all : sdf2imp java jar syntax

java : src-gen/org/strategoxt/imp/generator/sdf2imp.java

jar : lib/sdf2imp.jar

check : all \
        test/EditorService.runtestsuite \
        test/test-editor-service.runsh \
        test/test-stratego-java.runsh \
        test/test-foo.runsh

sdf2imp : $(RTREES) $(SYNTAX) Makefile src/include/rtg.tbl
	strc -i src/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCFLAGS) -m main-sdf2imp \
	     -la ${HOME}/.nix-profile/lib/libjava-front.la $(STRCSTFU)
	rm sdf2imp.c sdf2imp.lo sdf2imp.o

src-gen/org/strategoxt/imp/generator/sdf2imp.java : $(RTREES) $(SYNTAX) Makefile src/include/rtg.tbl
	$(STRJ) -clean -i src/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCFLAGS) -m main-sdf2imp \
	        -la java-front --library -p org.strategoxt.imp.generator $(STRCSTFU)

lib/sdf2imp.jar : src-gen/org/strategoxt/imp/generator/sdf2imp.java
	strj-jar -i $< -o $@ -cp src-gen

syntax : $(SYNTAX)

clean :
	rm -f src/include/sdf2imp.ctree sdf2imp $(RTREES) $(wildcard src/syntax/*.tbl) $(wildcard src/syntax/*.def) *.dep include/*.dep src-gen/org/strategoxt/imp/generator/sdf2imp.java src-gen/org/strategoxt/imp/generator/sdf2imp.class
	
# files:

% : %.str $(STRS)

%.rtree : %.str $(SYNTAX)
	@parse-stratego -i $< -o $@ $(STRINCLUDES)

src/include/sdf2imp.ctree : sdf2imp
	strc -F --library -i src/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCSTFU)

%.pp : %.def
	ppgen -i $< -o $@

%.pp.af : %.pp
	parse-pp-table -i $< -o $@

%.str : %.rtg
	rtg2sig -i $< -o $@

%.rtg : %.def
	sdf2rtg -i $< -o $@ -m `basename $*` --ignore-missing-cons

%.runtestsuite : %.testsuite
	parse-unit --no-heuristic-filters -i $< -p src/syntax/EditorService.tbl

%.runsh : %.sh
	sh $<

src/syntax/EditorService.def : src/syntax/EditorService.sdf $(SDFS)
	pack-sdf -i $< -o $@ -I src/syntax

src/syntax/EditorService.tbl : src/syntax/EditorService.def
	sdf2table -m EditorService -i $< -o $@

src/syntax/Stratego-Java-EditorService.def : src/syntax/Stratego-Java-EditorService.sdf $(SDFS)
	pack-sdf -i $< -o $@ -I src/syntax $(SDFINCLUDES)

src/syntax/Stratego-Java-EditorService.tbl : src/syntax/Stratego-Java-EditorService.def
	sdf2table -m Stratego-Java-EditorService -i $< -o $@

# We need a better solution for this
src/include/rtg.tbl : ~/.nix-profile/share/sdf/stratego-regular/rtg.tbl
	[ -e src/include ] || mkdir src/include
	baf2trm  -i $< -o $@ # cp $< $@

new-libs :
	rm -rf $(BRIDGE)/permissive-grammars/trans/make-permissive/bin
	make jar -C $(BRIDGE)/permissive-grammars/trans/make-permissive
	cp $(BRIDGE)/permissive-grammars/trans/make-permissive/bin/*.jar lib
