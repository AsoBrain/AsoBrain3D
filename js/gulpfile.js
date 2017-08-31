'use strict';
const gulp = require( 'gulp' );
const babel = require( '@numdata/common-build-gulp' ).babel;

const paths = {
	source: 'src/**/*.js',
	target: 'lib'
};

gulp.task( 'build', babel( gulp, paths ) );
gulp.task( 'watch', () => gulp.watch( paths.source, [ 'watch-build' ] ) );
gulp.task( 'watch-build', babel( gulp, paths, { catchErrors: true } ) );
