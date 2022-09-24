#!/bin/bash
mvn clean test jacoco:report -Djs-tests.skip=true -D'test=PrepareControllerTest' > output.txt
#mvn clean test jacoco:report -Djs-tests.skip=true -D'test=*Test' > output.txt
RC=$?
if [ $RC -eq 0 ]
then
    say 'Tests passed, super hooray!'
    bbedit output.txt
else
    say 'Test failed'
    bbedit output.txt
fi
