/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('hopsWorksApp')
        .factory('DataSetService', ['$http', function ($http) {
            return function (id) {
              var services = {
                /**
                 * Get the listing of all datasets under the current project.
                 * @returns {unresolved}
                 */
                getAllDatasets: function () {
                  return $http.get('/api/project/' + id + '/dataset/');
                },
                /**
                 * Get the contents of the folder to which the path points. 
                 * The parameter is a path relative to the project root folder.
                 * @param {type} relativePath
                 * @returns {unresolved}
                 */
                getContents: function (relativePath) {
                  return $http.get('/api/project/' + id + '/dataset/' + relativePath);
                },
                download: function (fileName) {
                  return $http.get('/api/project/' + id + '/dataset/download/' + fileName, {responseType: 'arraybuffer'});
                },
                upload: function (dataSetPath) {
                  return $http.post('/api/project/' + id + '/dataset/upload/' + dataSetPath);
                },
                createDataSetDir: function (dataSet) {
                  var regReq = {
                    method: 'POST',
                    url: '/api/project/' + id + '/dataset',
                    headers: {
                      'Content-Type': 'application/json'
                    },
                    data: dataSet
                  };

                  return $http(regReq);
                },
                removeDataSetDir: function (fileName) {
                  return $http.delete('/api/project/' + id + '/dataset/' + fileName);
                },
                attachTemplate: function (fileTemplateData) {
                  var regReq = {
                    method: 'POST',
                    url: '/api/project/' + id + '/dataset/attachTemplate',
                    headers: {
                      'Content-Type': 'application/json'
                    },
                    data: fileTemplateData
                  };

                  return $http(regReq);
                }
              };
              return services;
            };
          }]);
