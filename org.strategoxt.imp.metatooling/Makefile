STRS   = $(wildcard src/str/*.str)
CTREES = $(STRS:.str=.ctree)

STRCFLAGS = \
	-la stratego-xtc \
    -la stratego-lib \
    -la stratego-gpp \
    -la stratego-tool-doc \
    -la stratego-sglr \
    -la ${HOME}/.nix-profile/lib/libjava-front.la

STRINCLUDES = \
	-I . \
	-I src/str \
	-I $(HOME)/.nix-profile/share \
	-I $(HOME)/.nix-profile/share/java-front \
    `pkg-config --variable=strcflags java-front`

all : sdf2imp eclipse-deps

sdf2imp : $(STRS)
	strc -i src/str/sdf2imp.str -o $@ $(STRCFLAGS) $(STRINCLUDES) -m main-sdf2imp && \
	rm sdf2imp.c sdf2imp.lo sdf2imp.o

eclipse-deps : $(CTREES)

clean :
	rm -f src/str/*.ctree create-ast-classes

# files:

% : %.str $(STRS)

%.rtree : %.str
	parse-stratego -i $< -o $@

%.ctree : %.str
	strc -F -i $< -o $@
