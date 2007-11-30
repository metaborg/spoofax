STRS     = $(wildcard src/str/*.str) $(wildcard src/str/lib/*.str)
CTREES   = src/str/sdf2imp.ctree

STRCFLAGS = \
	-la stratego-xtc \
    -la stratego-lib \
    -la stratego-gpp \
    -la stratego-tool-doc \
    -la stratego-sglr \
    -la ${HOME}/.nix-profile/lib/libjava-front.la \
    `pkg-config --variable=strcxtcflags strategoxt`

STRINCLUDES = \
	-I $(HOME)/.nix-profile/share/sdf/xml-front \
	-I $(HOME)/.nix-profile/share/sdf/gpp \
    `pkg-config --variable=strcflags java-front`

all : sdf2imp eclipse-deps

sdf2imp : $(STRS) Makefile
	strc -i src/str/sdf2imp.str -o $@ $(STRINCLUDES) $(STRCFLAGS) -m main-sdf2imp && \
	rm sdf2imp.c sdf2imp.lo sdf2imp.o

eclipse-deps : $(CTREES)

clean :
	rm -f $(CTREES) sdf2imp

# files:

% : %.str $(STRS)

%.rtree : %.str
	parse-stratego -i $< -o $@

%.ctree : %.str
	strc -F -i $< -o $@ $(STRINCLUDES)
