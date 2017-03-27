#!/bin/bash
cd "`dirname $0`"
PATH="${PATH}:node_modules/.bin"
BUILD="$SHELL '$0'"

function run()
{
    echo "[$1]"

    case "$1" in
    update)
        npm update --save @numdata/oss
        npm install
        npm dedupe
        ;;

    clean)
        rm -rf lib dist
        ;;

    validate)
        eslint src || exit $?
        eslint --env mocha test || exit $?
        ;;

    test)
        mocha --recursive test || exit $?
        ;;

    patch)
        ./build.sh clean &&
        ./build.sh build &&
        ./build.sh test &&
        npm version patch &&
        npm publish || exit $?
        ;;

    *)
        gulp "$@"
        ;;
    esac
}

run "$1"
