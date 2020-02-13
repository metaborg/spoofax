# {{name}} Language Specification

{{#analysisStatix}}## Using Statix for multi-file analysis

By default the project is configured to analyze all files of your language in
isolation -- single-file analysis. It is also possible to configure the project
such that all files are analyzed together, and files can refer to each other --
multi-file analysis.

To enable multi-file analysis, do the following:
1. Uncomment the `(multifile)` option in `editor/Analysis.esv`
2. Uncomment the multi-file definition, and comment the single-file version, of
   `editor-analyze` in `trans/analysis.str`.

NB. When working in an IDE such as Eclipse, it is necessary to _restart the IDE_
after switching from single-file to multi-file analysis or vice versa. Failure to
do so will result in exceptions during analysis.

{{/analysisStatix}}