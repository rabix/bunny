#!/bin/bash
virtualenv env/testenv
source env/testenv/bin/activate
pip install -e git+https://github.com/common-workflow-language/cwltest.git@master#egg=cwltest
echo cwltest --test conformance_test_draft-2.yaml --tool ${buildFileDirPath}rabix -j 4
cwltest --test conformance_test_draft-2.yaml --tool ${buildFileDirPath}rabix -j 4
