/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';

angular.module('hopsWorksApp', [
  'ngAnimate',
  'ngCookies',
  'ngResource',
  'ngRoute',
  'ngSanitize',
  'ngTouch',
  'angular-md5',
  'angular-growl',
  'ui.bootstrap',
  'ui.select',
  'chart.js',
  'ngWebSocket',
  'ng-context-menu',
  'xeditable',
  'flow',
  'ngMaterial',
  'ngMessages',
  'as.sortable'
])
        .config(['$routeProvider', '$httpProvider', '$compileProvider', 'flowFactoryProvider',
          function ($routeProvider, $httpProvider, $compileProvider, flowFactoryProvider) {

            // Responseinterceptor for authentication
            $httpProvider.interceptors.push('AuthInterceptorService');

            // Requestinterceptor to transform some of the requests
            $httpProvider.interceptors.push('RequestInterceptorService');

            // Set the content type to be FORM type for all general post requests and override them explicit if needed
            $httpProvider.defaults.headers.post['Content-Type'] = 'application/x-www-form-urlencoded';

            flowFactoryProvider.defaults = {
              //if [400, 401, 403, 409, 415, 500, 501] error codes are sent from the server do not retry.
              permanentErrors: [400, 401, 403, 409, 415, 500, 501],
              maxChunkRetries: 1,
              chunkRetryInterval: 5000,
              simultaneousUploads: 4
            };
            flowFactoryProvider.on('catchAll', function (event) {
              console.log('catchAll', arguments);
            });

            $routeProvider
                    .when('/', {
                      templateUrl: 'views/home.html',
                      controller: 'HomeCtrl as homeCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })
                    .when('/login', {
                      templateUrl: 'views/login.html',
                      controller: 'LoginCtrl as loginCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                      $location.path('/');
                                      $location.replace();
                                      return $q.when(success);
                                    },
                                    function (err) {
                                    });
                          }]
                      }
                    })
                    .when('/register', {
                      templateUrl: 'views/register.html',
                      controller: 'RegCtrl as regCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                      $location.path('/');
                                      $location.replace();
                                      return $q.when(success);
                                    },
                                    function (err) {

                                    });
                          }]
                      }
                    })
                    .when('/recover', {
                      templateUrl: 'views/recover.html',
                      controller: 'RecoverCtrl as recoverCtrl'
                    })


                    .when('/project/:projectID', {
                      templateUrl: 'views/project.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })

                    .when('/project/:projectID/datasets', {
                      templateUrl: 'views/datasets.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })

                    .when('/project/:projectID/datasets/:datasetName', {
                      templateUrl: 'views/datasetsBrowser.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })
                    .when('/project/:projectID/cuneiform', {
                      templateUrl: 'views/cuneiform.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })
                    .when('/project/:projectID/spark', {
                      templateUrl: 'views/spark.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })
                    .when('/project/:projectID/adam', {
                      templateUrl: 'views/adam.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })
                    .when('/project/:projectID/jobs', {
                      templateUrl: 'views/jobs.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })
                    .when('/project/:projectID/newjob', {
                      templateUrl: 'views/newJob.html',
                      controller: 'ProjectCtrl as projectCtrl',
                      resolve: {
                        auth: ['$q', '$location', 'AuthService', '$cookies',
                          function ($q, $location, AuthService, $cookies) {
                            return AuthService.session().then(
                                    function (success) {
                                      $cookies.email = success.data.data.value;
                                    },
                                    function (err) {
                                      delete $cookies.email;
                                      $location.path('/login');
                                      $location.replace();
                                      return $q.reject(err);
                                    });
                          }]
                      }
                    })
                    .otherwise({
                      redirectTo: '/'
                    });

            $compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|tel|file|blob):/);
          }])

//We already have a limitTo filter built-in to angular,
//let's make a startFrom filter
        .filter('startFrom', function () {
          return function (input, start) {
            start = +start; //parse to int
            return input.slice(start);
          };
        })


        .filter('cardFilter', function () {
          return function (items, props) {
            var out = [];

            if (angular.isArray(items)) {
              items.forEach(function (item) {
                var itemMatches = false;
                var keys = Object.keys(props);
                for (var i = 0; i < keys.length; i++) {
                  var prop = keys[i];
                  var text = props[prop].toLowerCase();
                  if (item[prop].toString().toLowerCase().indexOf(text) !== -1) {
                    itemMatches = true;
                    break;
                  }
                }

                if (itemMatches) {
                  out.push(item);
                }
              });
            } else {
              // Let the output be the input untouched
              out = items;
            }


            return out;
          };
        });
