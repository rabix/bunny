#!/bin/bash
virtualenv -p $(which python2) env/testenv
source env/testenv/bin/activate
pip install -U pip setuptools wheel
pip install pyopenssl ndg-httpsclient pyasn1
pip install cwltest
cwltest --test conformance_test_draft-2.yaml --tool ${buildFileDirPath}/rabix -j 1 --verbose
