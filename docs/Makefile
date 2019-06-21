# Minimal makefile for Sphinx documentation
#

# You can set these variables from the command line.
SPHINXOPTS    =
SPHINXBUILD   = sphinx-build
SOURCEDIR     = source
BUILDDIR      = build
HTMLDIR       = $(BUILDDIR)/html
SIGNINGSDK    = ../sawtooth-sdk-signing/src/main/java
PROCESSOR     = ../sawtooth-sdk-transaction-processor/src/main/java
PROTOS        = ../sawtooth-sdk-protos

# Put it first so that "make" without argument is like "make help".
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

.PHONY: help Makefile

JAVADOCSDIR := $(HTMLDIR)/java_docs
java:
	mkdir -p $(JAVADOCSDIR)/signing
	javadoc -d $(JAVADOCSDIR)/signing -sourcepath $(SIGNINGSDK) sawtooth.sdk.signing
	mkdir -p $(JAVADOCSDIR)/processor
	javadoc -d $(JAVADOCSDIR)/processor -sourcepath $(PROCESSOR) sawtooth.sdk.processor
	mkdir -p $(JAVADOCSDIR)/protos
	cd $(PROTOS) \
		&& mvn javadoc:javadoc \
		&& cp -rf target/site/apidocs/* ../docs/$(JAVADOCSDIR)/protos

html: java
	$(SPHINXBUILD) -b html $(SOURCEDIR) $(HTMLDIR) $(SPHINXOPTS)
# Catch-all target: route all unknown targets to Sphinx using the new
# "make mode" option.  $(O) is meant as a shortcut for $(SPHINXOPTS).
%: Makefile
	@$(SPHINXBUILD) -M $@ "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)
