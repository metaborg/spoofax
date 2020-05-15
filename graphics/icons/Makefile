.PHONY: all build
.SILENT:

all: build

build: png2ico/png2ico
	png2ico/png2ico spoofax.ico \
	  spoofax_16.png \
	  spoofax_32.png \
	  spoofax_64.png \
	  spoofax_128.png
	echo "Created: spoofax.ico"

png2ico/png2ico:
	wget http://www.winterdrache.de/freeware/png2ico/data/png2ico-src-2002-12-08.tar.gz -O png2ico.tar.gz
	tar -xf png2ico.tar.gz
	make -C png2ico/

