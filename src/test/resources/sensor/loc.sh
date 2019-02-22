#!/bin/bash

echo "This script is used to test the script sensor."

# It should report 2 comments, 7 lines of code and 2 functions.

function one_function {
    echo "This function is never called"
}

function another_function
{
    echo "and neither is this one."
}

another_function2() {
    echo "But"
}

another_function3 () {
    echo "we"
}

another_function4( ) {
    echo "only"
}

another_function5 ( ) {
    echo "check"
}

another_function6()
{
    echo "if"
}

another_function7 () 
{
    echo "each"
}

another_function8( ) 
{
    echo "style"
}

another_function9 ( ) 
{
    echo "counts"
}
