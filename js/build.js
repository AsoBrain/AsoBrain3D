'use strict';
const build = require( '@numdata/common-build-gulp' ).build;
build( {
	source: 'src/**/*.js',
	target: 'lib'
} );
