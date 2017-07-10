#!/bin/bash
virtualenv -p $(which python2) env/testenv
source env/testenv/bin/activate
pip install -U pip setuptools wheel
pip install pyopenssl ndg-httpsclient pyasn1
pip install typing==3.5.2.2
pip install cwltest
cwltest --test conformance_test_v1.0.yaml --tool ${buildFileDirPath}/rabix -j 1
