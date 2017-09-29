#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import sys
import os
import subprocess

# -- General configuration

extensions = ['javasphinx']
templates_path = ['_templates']
source_suffix = '.rst'
master_doc = 'index'
project = 'Spoofax API'
copyright = '2017, MetaBorg'
author = 'MetaBorg'
version = '2.3.0-SNAPSHOT'
release = version
language = None
exclude_patterns = ['_build', 'Thumbs.db', '.DS_Store']
pygments_style = 'sphinx'

# -- Options for HTML output

# Only import and set the ReadTheDocs theme if we're building docs locally.
on_rtd = os.environ.get('READTHEDOCS', None) == 'True'
if not on_rtd:
  import sphinx_rtd_theme
  html_theme = 'sphinx_rtd_theme'
  html_theme_path = [sphinx_rtd_theme.get_html_theme_path()]

html_static_path = ['_static']

# -- Options for javasphinx extension

javadoc_url_map = {
  'java': ('http://docs.oracle.com/javase/8/docs/api/', 'javadoc'),
  'com.google.inject' : ('http://google.github.io/guice/api-docs/latest/javadoc/', 'javadoc'),
  'org.apache.commons.vfs2' : ('https://commons.apache.org/proper/commons-vfs/apidocs/', 'javadoc')
}

def make_apidoc(app):
  sources = {
    'org.metaborg.core'              : '../org.metaborg.core/src/main/java/'
  , 'org.metaborg.spoofax.core'      : '../org.metaborg.spoofax.core/src/main/java/'
  , 'org.metaborg.meta.core'         : '../org.metaborg.meta.core/src/main/java/'
  , 'org.metaborg.spoofax.meta.core' : '../org.metaborg.spoofax.meta.core/src/main/java/'
  }
  excludes = [

  ]
  for dest, src in sources.items():
    cur_dir = os.path.abspath(os.path.dirname(__file__))
    output_path = os.path.join(cur_dir, 'apidoc', dest)
    cmd_path = 'javasphinx-apidoc'
    # Check to see if we are in a virtualenv, if we are, assemble the path manually
    if hasattr(sys, 'real_prefix'):
      cmd_path = os.path.abspath(os.path.join(sys.prefix, 'bin', 'javasphinx-apidoc'))
    cmd = [cmd_path, '-v', '-f', '-o', output_path, src]
    cmd.extend(excludes)
    try:
      subprocess.check_call(cmd)
    except subprocess.CalledProcessError as e:
      print('Failed to generate API docs: {}'.format(e))

# -- Additional Sphinx configuration

def setup(app):
  app.connect('builder-inited', make_apidoc)
