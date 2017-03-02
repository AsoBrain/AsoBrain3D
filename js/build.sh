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

    build)
        mkdir -p lib &&
        babel --no-comments --out-dir lib src
        ;;

    watch)
        mkdir -p lib &&
        babel --no-comments --watch --out-dir lib src
        ;;

    watchdep)
        gulp watchdep
        ;;

    test)
        mocha --recursive test
        ;;

    patch)
        ./build.sh clean &&
        ./build.sh build &&
        ./build.sh test &&
        npm version patch &&
        npm publish || exit $?
        ;;

    *)
        echo "Unknown task"
        ;;
    esac
}

run "$1"
