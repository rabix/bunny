#!/bin/bash
virtualenv -p $(which python2) env/testenv
source env/testenv/bin/activate
pip install pyopenssl ndg-httpsclient pyasn1
pip install cwltest
cwltest --test conformance_test_v1.0.yaml --tool ${buildFileDirPath}/rabix
