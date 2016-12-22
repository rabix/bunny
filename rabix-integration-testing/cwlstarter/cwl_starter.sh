#!/bin/bash
virtualenv env/testenv
source env/testenv/bin/activate
pip install -U pip setuptools wheel
pip install pyopenssl ndg-httpsclient pyasn1
pip install -e git+https://github.com/common-workflow-language/cwltest.git@master#egg=cwltest
cwltest --test conformance_test_v1.0.yaml --tool ${buildFileDirPath}/rabix -j 4
