# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line.
SPHINXOPTS    =
SPHINXBUILD   = sphinx-build
SOURCEDIR     = source
BUILDDIR      = build
HTMLDIR       = $(BUILDDIR)/html
SIGNINGSDK     = ../sawtooth-sdk-signing/src/main/java

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile

JAVADOCSDIR := $(HTMLDIR)/java_docs
java:
	mkdir -p $(JAVADOCSDIR)/
	javadoc -d $(JAVADOCSDIR) -sourcepath $(SIGNINGSDK) sawtooth.sdk.signing

html: java
	$(SPHINXBUILD) -b html $(SOURCEDIR) $(HTMLDIR) $(SPHINXOPTS) 
# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)
