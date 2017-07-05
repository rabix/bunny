#!/bin/bash
virtualenv env/testenv
source env/testenv/bin/activate
pip install cwltest
cwltest --test conformance_test_draft-1.yaml --tool ${buildFileDirPath}/rabix -j 1 --verbose
