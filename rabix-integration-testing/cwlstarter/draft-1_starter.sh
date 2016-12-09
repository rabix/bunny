#!/bin/bash
virtualenv env/testenv
source env/testenv/bin/activate
pip install -e git+https://github.com/common-workflow-language/cwltest.git@master#egg=cwltest
cwltest --test conformance_test_draft-1.yaml --tool /home/travis/build/rabix/bunny/rabix-backend-local/target/rabix-backend-local-1.0.0-SNAPSHOT/rabix -j 4
