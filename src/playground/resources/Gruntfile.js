var path = require('path');

module.exports = function(grunt) {

  require('load-grunt-tasks')(grunt);

  /**
   * Resolve external project resource as file path
   */
  function resolvePath(project, file) {
    return path.join(path.dirname(require.resolve(project)), file);
  }


  grunt.initConfig({
    browserify: {
      options: {
        browserifyOptions: {
          debug: true
        },
        transform: [
          [ 'stringify', {extensions: ['.bpmn']} ],
          [ 'babelify', {global: true} ]
        ]
      },
      app: {
        files: {
          'app.bundled.js': [ 'app/**/*.js' ]
        }
      }
    },

    copy: {
      diagram_js: {
        files: [
          {
            src: resolvePath('diagram-js', 'assets/diagram-js.css'),
            dest: 'dist/css/diagram-js.css'
          }
        ]
      },
      diagram_js_minimap: {
        files: [
          {
            src: resolvePath('diagram-js-minimap', 'assets/diagram-js-minimap.css'),
            dest: 'dist/css/diagram-js-minimap.css'
          }
        ]
      },
      bpmn_js: {
        files: [
          {
            expand: true,
            cwd: resolvePath('bpmn-js', 'dist/assets'),
            src: ['**/*.*', '!**/*.js'],
            dest: 'dist/vendor'
          }
        ]
      },
      app: {
        files: [
          {
            expand: true,
            cwd: 'app/',
            src: ['**/*.*', '!**/*.js'],
            dest: 'dist'
          }
        ]
      }
    },

    less: {
      options: {
        dumpLineNumbers: 'comments',
        paths: [
          'node_modules'
        ]
      },

      styles: {
        files: {
          'dist/css/app.css': 'styles/app.less'
        }
      }
    }

  });

  // tasks

  grunt.registerTask('build', [ 'copy', 'less', 'browserify:app' ]);

  grunt.registerTask('default', [ 'build' ]);
};
