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
    -la stratego-aterm \
    -la ${HOME}/.nix-profile/lib/libstratego-sdf.la \
    -la ${HOME}/.nix-profile/lib/libjava-front.la \
    `pkg-config --variable=strcxtcflags strategoxt`

SDFINCLUDES    = -Idef ${HOME}/.nix-profile/share/java-front-syntax/JavaMix.def \
                 -Idef ${HOME}/.nix-profile/share/java-front/JavaEBlockMix.def \
                 -Idef ${HOME}/.nix-profile/share/java-front/EmbeddedJavaMix.def

# I don't care much for nullary constructor warnings unless it's about lowercase ones, such as 'start'
STRCSTFU=2>&1 | grep -vE 'warning ] Nullary constructor .*[A-Z]|warning ] multiple external| \[ExtSDef\(|stratego-trm|module-name-checked|module-name-checked"' \
                 >&2

STRINCLUDES = \
	-I $(HOME)/.nix-profile/share/sdf/xml-front \
	-I $(HOME)/.nix-profile/share/sdf/gpp \
	-I $(HOME)/.nix-profile/share/sdf/stratego-regular \
	-I $(HOME)/.nix-profile/share/sdf/sdf-front/ \
	-I $(HOME)/.nix-profile/share/java-front/ \
	-I src/syntax \
    `pkg-config --variable=strcflags java-front`

all : sdf2imp java syntax

java : src-gen/sdf2imp.java

check : all \
        tests/EditorService.runtestsuite \
        tests/test-editor-descriptor.runsh \
        tests/test-stratego-java.runsh \
        tests/test-foo.runsh

sdf2imp : $(RTREES) $(SYNTAX) Makefile src/include/rtg.tbl
	strc -i src/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCFLAGS) -m main-sdf2imp $(STRCSTFU)
	rm sdf2imp.c sdf2imp.lo sdf2imp.o

src-gen/sdf2imp.java : $(RTREES) $(SYNTAX) Makefile src/include/rtg.tbl
	[ -e strategoxt.jar ] || (echo "ERROR: Missing file strategoxt.jar. Please copy/link it to this directory"; exit 1)
	java -server -Xss4m -jar strategoxt.jar -i src/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCFLAGS) -m main-sdf2imp $(STRCSTFU)

syntax : $(SYNTAX)

clean :
	rm -f src/include/sdf2imp.ctree sdf2imp $(RTREES) $(wildcard src/syntax/*.tbl) $(wildcard src/syntax/*.def) *.dep include/*.dep
	
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

