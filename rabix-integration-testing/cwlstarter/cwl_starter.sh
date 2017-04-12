#!/bin/bash
virtualenv -p $(which python2) env/testenv
source env/testenv/bin/activate

pip install pyopenssl ndg-httpsclient pyasn1
git clone https://github.com/common-workflow-language/cwltest.git
python ./cwltest/setup.py install
cwltest --test conformance_test_v1.0.yaml --tool ${buildFileDirPath}/rabix -j 1 --verbose
