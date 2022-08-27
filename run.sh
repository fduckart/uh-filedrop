#!/bin/bash
mvn clean test jacoco:report -Djs-tests.skip=true -Dtest=FileSetRepositoryTest#acomment > output.txt
RC=$?
git s
if [ $RC -eq 0 ]
then
    say 'Tests passed, hooray!'
    bbedit output.txt
else
    say 'Test failed'
    bbedit output.txt
fi
