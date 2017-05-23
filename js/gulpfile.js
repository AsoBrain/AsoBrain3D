const gulp = require( 'gulp' );
const babel = require( 'gulp-babel' );
const newer = require( 'gulp-newer' );

const paths = {
	source: 'src/**/*.js',
	target: 'lib'
};

gulp.task( 'build', () =>
		gulp.src( paths.source )
			.pipe( newer( paths.target ) )
			.pipe( babel() )
			.pipe( gulp.dest( paths.target ) )
);

gulp.task( 'watch', () => gulp.watch( paths.source, [ 'build' ] ) );
