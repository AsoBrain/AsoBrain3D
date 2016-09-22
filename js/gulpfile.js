const gulp = require( 'gulp' );
const watch = require( 'gulp-watch' );

const localDependencies = {
	'@numdata/oss': {
		src: [ '../../numdata_open/NumdataOpenSource/js/lib/**' ],
		dest: 'node_modules/@numdata/oss/lib'
	}
};

gulp.task( 'watchdep', () =>
{
	const watchOptions = { verbose: true };

	Object.keys( localDependencies ).forEach( key =>
	{
		const dep = localDependencies[ key ];
		watch( dep.src, Object.assign( { name: key }, watchOptions ) ).pipe( gulp.dest( dep.dest ) );
	} );
} );
