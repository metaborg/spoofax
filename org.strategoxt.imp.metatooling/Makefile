STRS   = $(wildcard src/str/*.str)
CTREES = $(STRS:.str=.ctree)

all : $(CTREES)
	@echo 'Built included files (requires further building in Eclipse).'

clean :
	rm -f src/str/*.ctree

%.rtree : %.str
	parse-stratego -i $< -o $@

%.ctree : %.str
	strc -F -i $< -o $@
