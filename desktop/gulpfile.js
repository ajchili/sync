var gulp = require('gulp');
var symdest = require('gulp-symdest');
var runSequence = require('run-sequence');
var zip = require('gulp-vinyl-zip');
var electron = require('gulp-awesome-electron');

const electronVersion = '1.8.4';
const src = ['**/*.*', 
		'!build/**', 
		'!media/**', 
		'!semantic/src/**', 
		'!semantic/task/**',
		'!CONTRIBUTING.md'];

gulp.task('build-macos', () => {
	return gulp.src(src)
		.pipe(electron({ version: electronVersion, platform: 'darwin' }))
		.pipe(zip.dest('build/sync-darwin.zip'));
});

gulp.task('build-win32', () => {
	return gulp.src(src)
		.pipe(electron({ version: electronVersion, platform: 'win32', arch: 'ia32' }))
		.pipe(zip.dest('build/sync-win-32.zip'));
});

gulp.task('build-win64', () => {
	return gulp.src(src)
		.pipe(electron({ version: electronVersion, platform: 'win32', arch: 'x64' }))
		.pipe(zip.dest('build/sync-win-64.zip'));
});

gulp.task('build-windows', () => {
    return runSequence(['build-win32', 'build-win64']);
});

gulp.task('build-linux32', () => {
	return gulp.src(src)
		.pipe(electron({ version: electronVersion, platform: 'linux', arch: 'ia32' }))
		.pipe(zip.dest('build/sync-linux-32.zip'));
});

gulp.task('build-linux64', () => {
	return gulp.src(src)
		.pipe(electron({ version: electronVersion, platform: 'linux', arch: 'x64' }))
		.pipe(zip.dest('build/sync-linux-64.zip'));
});

gulp.task('build-linux', () => {
		return runSequence(['build-linux32', 'build-linux64']);
});