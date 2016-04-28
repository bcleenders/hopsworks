'use strict';

/**
 * @ngdoc function
 * @name hopsWorksApp.controller:WorkflowexecutionCtrl
 * @description
 * # WorkflowexecutionCtrl
 * Controller of the hopsWorksApp
 */
angular.module('hopsWorksApp')
  .controller('WorkflowExecutionCtrl',[ '$routeParams', '$location', 'growl','WorkflowExecutionService',
      function ($routeParams, $location, growl, WorkflowExecutionService) {
          var self = this;
          self.executions = [];
          var projectId = $routeParams.projectID;
          var workflowId = $routeParams.workflowID;

          var index = function(){
              WorkflowExecutionService.index(projectId, workflowId).then(function(success){
                  console.log(success);
                  self.executions = success.data;
              },function (error) {
                  growl.error(error.data.errorMsg, {title: 'Error', ttl: 5000})
              })
          }
          if(workflowId) index();

          self.create = function(id){
              var wId = workflowId;
              if(id) wId = id;
              WorkflowExecutionService.create(projectId, wId).then(function(success){
                  console.log(success);
                  growl.success("Execution started", {title: 'Success', ttl: 10000});
              },function (error) {
                  growl.error(error.data.errorMsg, {title: 'Error', ttl: 5000})
              })
          }

          self.goToShow = function (id) {
              $location.path('project/' + projectId + '/workflows/' + workflowId + '/executions/' + id);
          }
          self.getWorkflowId = function(){
              return workflowId;
          }
      }]);
