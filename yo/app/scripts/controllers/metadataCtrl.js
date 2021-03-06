/*jshint undef: false, unused: false, indent: 2*/
/*global angular: false */

'use strict';


angular.module('hopsWorksApp')
        .controller('MetadataCtrl', ['$cookies', '$modal', '$scope', '$rootScope', '$routeParams',
          '$filter', 'DataSetService', 'ModalService', 'growl', 'MetadataActionService',
          'MetadataHelperService', 'ProjectService',
          function ($cookies, $modal, $scope, $rootScope, $routeParams, $filter, DataSetService,
                  ModalService, growl, MetadataActionService, MetadataHelperService, ProjectService) {

            var self = this;
            self.metaData = {};
            self.metaDataDetail = {};
            self.currentFile = MetadataHelperService.getCurrentFile();
            self.tabs = [];
            self.meta = [];
            self.metainfo=[];
            self.visibilityInfo=[];
            self.availableTemplates = [];
            self.newTemplateName = "";
            self.extendedTemplateName = "";
            self.currentTableId = -1;
            self.currentTemplateID = -1;
            self.currentFileTemplates = [];
            self.selectedTemplate = {};
            self.editedField;
            self.toExtend = -1;
            self.currentBoard = {};
            self.toDownload;
            self.blob;
            self.templateContents = {};
            self.editingTemplate = false;
            self.projectInodeid = -1;
            self.noTemplates = false;
            
            self.attachedDetailedTemplateList=[];

            var dataSetService = DataSetService($routeParams.projectID);

            //update the current template whenever other users make changes
            var listener = $rootScope.$on("template.change", function (event, response) {
              try {
                var incomingTemplateId = JSON.parse(response.board).templateId;

                if (self.currentTemplateID === incomingTemplateId) {
                  self.currentBoard = JSON.parse(response.board);
                }
              } catch (error) {
                //console.log(error);
              }
            });

            /*
             * Rootscope events are not deregistered when the controller dies.
             * So on the controller destroy event deregister the rootscope listener manually.
             * @returns {undefined}
             */
            $scope.$on("$destroy", function () {
              listener();
            });

            //fetch all the available templates
            MetadataHelperService.fetchAvailableTemplates()
                    .then(function (response) {
                      self.availableTemplates = JSON.parse(response.board).templates;
                      angular.forEach(self.availableTemplates, function (template, key) {
                        template.showing = false;
                      });
                    });

            //get the current project to get its inodeid
            ProjectService.get({}, {'id': parseInt($routeParams.projectID)})
                    .$promise.then(
                            function (success) {
                              self.projectInodeid = success.inodeid;

                            }, function (error) {
                    });

            /**
             * submit form data when the 'save' button is clicked or when the enter key is hit
             */
            self.submitMetadata = function () {
              if (!self.metaData) {
                return;
              }
              //after the project inodeid is available proceed to store metadata
              MetadataActionService.storeMetadata($cookies['email'],
                      parseInt(self.currentFile.parentId), self.currentFile.name, self.currentTableId, self.metaData)
                      .then(function (response) {
                        //console.log("Metadata saved " + response.status);
                        MetadataHelperService.setCloseSlider("true");
                      });

              //truncate metaData object
              angular.forEach(self.metaData, function (value, key) {
                if (!angular.isArray(value)) {
                  self.metaData[key] = "";
                } else {
                  self.metaData[key] = [];
                }
              });
              //self.metaData = {};
            };
            
            
            self.createMetadata = function(tableId,metadataId){
                if (!self.metaData) {
                return;
                }
                var value= self.metaData[metadataId];                
                if(!value || value.length === 0){
                    growl.info("Metadata field cannot be empty", {title: 'Info', ttl: 3000});
                    return;
                }
                
                var tempInput={};
                tempInput[metadataId]=value;
                MetadataActionService.storeMetadata($cookies['email'],
                      parseInt(self.currentFile.parentId), self.currentFile.name, tableId, tempInput)
                      .then(function (response) {
                       self.metaData[metadataId]='';
                       growl.success("Created new metadata", {title: 'Success', ttl: 3000});
                       self.fetchMetadataForTemplate();
                      });
            };

            /* -- TEMPLATE HANDLING FUNCTIONS -- */
            /**
             * Selects/deselects a template item when the user clicks on it
             * 
             * @param {type} template
             * @returns {undefined}
             */
            self.toggleTemplate = function (template) {
              //disable toggling when a template name is being edited
              if (self.editingTemplate) {
                return;
              }
              //reset all templates showing flag
              angular.forEach(self.availableTemplates, function (temp, key) {
                if (template.id !== temp.id) {
                  temp.showing = false;
                }
              });

              //handle the clicked template accordingly
              template.showing = !template.showing;
              self.currentTemplateID = template.id;

              //if all templates are deselected hide the add new table button
              if (!template.showing) {
                self.currentTemplateID = -1;
                self.currentBoard = {};
              }
            };

            /**
             * Updates a template name
             * 
             * @param {type} template
             * @returns {undefined}
             */
            self.updateTemplateName = function (template) {
              MetadataActionService.updateTemplateName($cookies['email'], template)
                      .then(function (response) {
                        self.editingTemplate = false;
                        self.currentTemplateID = -1;
                      });
            };

            /**
             * Creates a new template from an existing one
             * 
             * @returns {undefined}
             */
            self.extendTemplate = function () {
                         
            if(self.checkTemplateAvailability(self.extendedTemplateName)){
                //don't proceed if there is no selected template to extend
                if (self.toExtend === -1) {
                  growl.info("Select a template first.", {title: 'Info', ttl: 5000});
                  return;
                }

                //store the new template name
                MetadataActionService.addNewTemplate($cookies['email'], self.extendedTemplateName)
                        .then(function (data) {
                          var tempTemplates = JSON.parse(data.board);

                          //get the id of the new template
                          var newlyCreatedID = tempTemplates.templates[tempTemplates.numberOfTemplates - 1].id;

                          //get the contents of the template to extend
                          MetadataActionService.fetchTemplate($cookies['email'], parseInt(self.toExtend))
                                  .then(function (response) {
                                    var templateToExtend = JSON.parse(response.board);

                                    //associate existing contents with the new template
                                    MetadataActionService.extendTemplate($cookies['email'], newlyCreatedID, templateToExtend)
                                            .then(function (data) {
                                              self.extendedTemplateName = "";

                                              //trigger the necessary variable change in the service
                                              MetadataHelperService.fetchAvailableTemplates()
                                                      .then(function (response) {
                                                        self.availableTemplates = JSON.parse(response.board).templates;
                                                        //console.log("AVAILABLE TEMPLATES " + JSON.stringify(self.availableTemplates));
                                                      });

                                              self.toExtend = -1;
                                              //console.log('Response from extending template: ');
                                              //console.log(data);
                                            });
                                  });
                        });
                    }
            };

            /**
             * Fetches a specific template from the database based on its id
             * 
             * @param {type} templateId
             * @returns {undefined}
             */
            self.fetchTemplate = function (templateId) {
              //if all templates are deselected hide the add new table button
              if (self.currentTemplateID === -1) {
                return;
              }

              MetadataActionService.fetchTemplate($cookies['email'], templateId)
                      .then(function (success) {
                        /*
                         * sort the objects of the retrieved template by id.
                         * Keeps the objects in a fixed position
                         */
                        var template = JSON.parse(success.board);
                        var sortedTables = sortObject($filter, 'id', template);
                        template.columns = sortedTables;

                        //update the currentBoard upon template retrieval
                        self.currentBoard = template;
                      }, function (error) {
                        console.log('fetchTemplate - error');
                        console.log(JSON.parse(error));
                      });
            };

            /**
             * Persists a template's contents (tables, fields) in the database
             * 
             * @param {type} closeSlideout
             * @returns {undefined}
             */
            self.storeTemplate = function (closeSlideout) {

              MetadataActionService.storeTemplate($cookies['email'], self.currentTemplateID, self.currentBoard)
                      .then(function (response) {
                        var template = JSON.parse(response.board);
                        var sortedTables = sortObject($filter, 'id', template);
                        template.columns = sortedTables;

                        self.currentBoard = template;
                        if (closeSlideout === 'true') {
                          MetadataHelperService.setCloseSlider("true");
                        }
                      }, function (error) {
                        console.log(error);
                      });
            };

            /**
             * Creates a new template in the database
             * 
             * @returns {undefined}
             */
            self.addNewTemplate = function () {
                
              if(self.checkTemplateAvailability(self.newTemplateName)){
                  
                MetadataActionService.addNewTemplate($cookies['email'], self.newTemplateName)
                        .then(function (data) {
                          self.newTemplateName = "";
                          //trigger a variable change (availableTemplates) in the service
                          MetadataHelperService.fetchAvailableTemplates();
                          self.availableTemplates = MetadataHelperService.getAvailableTemplates();
                        });
                  }
              };
            
            
            
            self.checkTemplateAvailability=function(templateName){
              
              var isTemplateAvailable=false;
              angular.forEach(self.availableTemplates, function (template, key) {
                        if(template.name.trim().toLowerCase() === templateName.trim().toLowerCase()){                            
                            isTemplateAvailable=true;
                        }                           
                }); 
                
               if(isTemplateAvailable){
                   growl.error("Template name "+templateName.trim()+" already exisits", {title: 'Info', ttl: 5000});
                   return false;
               }
               return true;                
            }

            /**
             * Deletes a template from the database
             * 
             * @param {type} templateId. The id of the template to be removed
             * @returns {undefined}
             */
            self.removeTemplate = function (templateId) {
              MetadataActionService.removeTemplate($cookies['email'], templateId)
                      .then(function (data) {
                        //trigger a variable change (availableTemplates) in the service
                        MetadataHelperService.fetchAvailableTemplates();
                        self.availableTemplates = MetadataHelperService.getAvailableTemplates();
                      });
            };

            /**
             * Associates a template to a file. It is a template id to file (inodeid)
             * association
             * 
             * @param {type} file
             * @returns {undefined}
             */
            self.attachTemplate = function (file) {
              var templateId = -1;

              var data = {inodePath: "", templateId: -1};
              data.inodePath = file.path;

              ModalService.attachTemplate('sm', file, templateId)
                      .then(function (success) {
                        data.templateId = success.templateId;

                        dataSetService.attachTemplate(data).then(
                                function (success) {
                                  growl.success(success.data.successMessage, {title: 'Success', ttl: 3000});
                                  self.setMetadataTemplate(file);
                                }, function (error) {
                          growl.info("Could not attach template to file " + file.name + ".",
                                  {title: 'Info', ttl: 5000});
                        });
                      });
            };

            /**
             * Removes the selected template from the selected inode. Affects the association table
             * 
             * @param {type} file
             * @returns {undefined}
             */
            self.detachTemplate = function (file) {
              var templateId = -1;

              ModalService.detachTemplate('sm', file, templateId)
                      .then(function (success) {

                        dataSetService.detachTemplate(success.fileId, success.templateId)
                                .then(function (success) {
                                  growl.success(success.data.successMessage, {title: 'Success', ttl: 3000});
                                  self.setMetadataTemplate(file);
                                });
                      });
            };

            /**
             * Fetches all templates attached to a given inode
             * 
             * @param {type} file
             * @returns {undefined}
             */
            self.fetchAttachedTemplates = function () {
              //initialize the variable before fetching the templates.
              //assume by default there are no templates
              self.noTemplates = false;

              dataSetService.fetchTemplatesForInode(self.currentFile.id)
                      .then(function (response) {
                        self.currentFileTemplates = response.data;

                        if (self.currentFileTemplates.length === 0) {
                          self.noTemplates = true;
                        }
                      });
            };

            /* -- TABLE AND FIELD HANDLING FUNCTIONS ADD/REMOVE -- */
            /**
             * Deletes a table. It is checking first if the table contains any fields
             * and proceeds according to user input
             * 
             * @param {type} column
             * @returns {undefined}
             */
            self.checkDeleteTable = function (column) {
              MetadataActionService.isTableEmpty($cookies['email'], column.id)
                      .then(function (response) {

                        if (response.board !== "EMPTY") {
                          ModalService.confirm("sm", "Delete table",
                                  "This table contains fields. Do you really want to delete it?\n\
                                This action cannot be undone.")
                                  .then(function (success) {
                                    self.deleteTable(column);
                                  }, function (cancelled) {
                                    //console.log("CANCELED " + JSON.stringify(cancelled));
                                    growl.info("Delete aborted", {title: 'Info', ttl: 5000});
                                  });

                          return;
                        }

                        self.deleteTable(column);
                      });
            };

            /**
             * Deletes a table
             * 
             * @param {type} column
             * @returns {undefined}
             */
            self.deleteTable = function (column) {
              MetadataActionService.deleteList($cookies['email'], self.currentTemplateID, column)
                      .then(function (success) {
                        self.fetchTemplate(self.currentTemplateID);
                        growl.success("Table " + column.name + " deleted successfully.",
                                {title: 'Success', ttl: 5000});
                      }, function (error) {
                        console.log(error);
                        growl.info("Could not delete table " + column.name +
                                " " + error + ".", {title: 'Info', ttl: 5000});
                      });
            };

            /**
             * Deletes a field. It is checking first if the field contains any raw data
             * and proceeds according to user input
             * 
             * @param {type} column. The table this field resides in
             * @param {type} card. The card going to be deleted
             * @returns {undefined}
             */
            self.checkDeleteField = function (column, card) {
              MetadataActionService.isFieldEmpty($cookies['email'], card.id)
                      .then(function (response) {

                        if (response.board !== "EMPTY") {
                          ModalService.confirm("sm", "Delete field",
                                  "This field contains raw data. Do you really want to delete it?\n\
                                This action cannot be undone.")
                                  .then(function (success) {
                                    self.deleteField(column, card);
                                  }, function (cancelled) {
                                    console.log("CANCELED " + JSON.stringify(cancelled));
                                    growl.info("Delete aborted", {title: 'Info', ttl: 5000});
                                  });

                          return;
                        }

                        self.deleteField(column, card);
                      });
            };

            /**
             * Deletes a table field
             * 
             * @param {type} column. The table this field resides in
             * @param {type} card. The field going to be deleted
             * @returns {undefined}
             */
            self.deleteField = function (column, card) {
              MetadataActionService.deleteCard($cookies['email'], self.currentTemplateID, column, card)
                      .then(function (success) {

                        self.fetchTemplate(self.currentTemplateID);
                        growl.success("Field " + card.title + " deleted successfully.",
                                {title: 'Success', ttl: 5000});
                      }, function (error) {
                        console.log(error);
                        growl.info("Could not delete field " + card.title +
                                " " + error + ".", {title: 'Info', ttl: 5000});
                      });
            };

            /**
             * Adds a new field to a table
             * 
             * @param {type} templateId
             * @param {type} column
             * @param {type} card
             * @returns {unresolved}
             */
            self.storeCard = function (templateId, column, card) {

              return MetadataActionService.storeCard($cookies['email'], templateId, column, card);
            };

            /**
             * Displays the modal dialog to creating a new card
             * 
             * @param {type} column
             * @returns {undefined}
             */
            self.addField = function (column) {
              $scope.currentColumn = column;

              ModalService.addNewField($scope)
                      .then(function (field) {

                        MetadataActionService.storeCard($cookies['email'], self.currentTemplateID, column, field)
                                .then(function (success) {
                                  growl.success("Field " + field.title + " saved successfully", {title: 'Success', ttl: 5000});
                                  self.fetchTemplate(self.currentTemplateID);
                                }, function (error) {
                                  console.log(error);
                                  growl.info("Could save field " + field.title + ".", {title: 'Info', ttl: 5000});
                                });
                      });
            };

            /**
             * Displays the modal dialog to creating a new table
             * 
             * @returns {undefined}
             */
            self.addNewList = function () {
              $scope.template = self.currentTemplateID;
              $modal.open({
                templateUrl: 'views/metadata/newListModal.html',
                controller: 'NewlistCtrl',
                scope: $scope
              })
                      .result.then(function (list) {

                        if (!angular.isUndefined(list)) {

                          //we need to add the new table into the mainboard object
                          self.currentBoard.columns.push(list);

                          MetadataActionService.storeTemplate($cookies['email'], self.currentTemplateID, self.currentBoard)
                                  .then(function (response) {
                                    var template = JSON.parse(response.board);
                                    var sortedTables = sortObject($filter, 'id', template);
                                    template.columns = sortedTables;

                                    self.currentBoard = template;
                                  }, function (error) {
                                    console.log(error);
                                  });
                        }
                      });
            };

            /* -- Field handling functions -- */
            /**
             * Makes a field (not)searchable by setting the attribute 'find' accordingly
             * 
             * @param {type} card
             * @returns {undefined}
             */
            self.makeSearchable = function (card) {
              card.find = !card.find;
              self.storeTemplate(false);
              //console.log("Card " + card.title + " became searchable " + card.find);
            };

            /**
             * Makes a field (not)required by setting the attribute 'required' accordingly
             * 
             * @param {type} card
             * @returns {undefined}
             */
            self.makeRequired = function (card) {
              card.required = !card.required;
              self.storeTemplate(false);
              //console.log("Card " + card.title + " became required " + card.required);
            };


            /**
             * Configuration object for the ng-sortable directive. Provides some drag n drop callbacks 
             * that help us take control over the objects dragged around 
             */
            self.fieldSortOptions = {
              /*
               * Triggered when an item is moved from one container (table) to another
               */
              itemMoved: function (event) {
                /*
                 * event.dest is the destination object. Handles object moving between different table objects,
                 * resetting their position attribute. 'value' is the field under processing
                 */
                angular.forEach(event.dest.sortableScope.$parent.column.cards, function (value, key) {
                  value.position = (key + 1);
                });

                self.storeTemplate(false);
              },
              /*
               * Triggered when a field changes position inside the same container (table). Does not apply on cards
               * that move from one table to another
               */
              orderChanged: function (event) {
                /*
                 * event.dest is the destination object. Handles object moving inside the same table,
                 * resetting their position attributes. 'value' is the field under processing
                 */
                angular.forEach(event.dest.sortableScope.$parent.column.cards, function (value, key) {
                  value.position = (key + 1);
                });

                self.storeTemplate(false);
              },
              containment: '#board'
            };

            /**
             * Allows modifying the definition of a field i.e. changing the field type
             * (text, yes/no field, dropdown), the field name and description
             * 
             * @param {type} column
             * @param {type} field
             * @returns {$q@call;defer.promise}
             */
            self.modifyField = function (column, field) {
              $scope.tableid = column.id;
              $scope.field = field;

              ModalService.modifyField($scope).then(
                      function (success) {
                        //Persist the modified card to the database
                        self.storeCard(self.currentTemplateID, column, success)
                                .then(function (response) {
                                  self.currentBoard = JSON.parse(response.board);
                                  growl.success("Field " + field.title + " modified successfully", {title: 'Success', ttl: 5000});
                                });
                      });
            };

            /**
             * Allows modifying the metadata (raw data) a file contains
             * 
             * @param {type} raw
             * @returns {undefined}
             */
            /*
            self.updateMetadata = function (metadata) {

              MetadataActionService.updateMetadata($cookies['email'], metadata, self.currentFile.parentId, self.currentFile.name)
                      .then(function (response) {
                        growl.success(response.board, {title: 'Success', ttl: 5000});

                      }, function (dialogResponse) {
                        growl.info("Could not update metadata " + metadata.data + ".", {title: 'Info', ttl: 5000});
                      });
            };*/
            
            self.updateMetadata = function (metadata){
                
                if (!self.metaDataDetail) {
                return;
                }
                
                var value= self.metaDataDetail[metadata.id];
                if(!value){
                    growl.info("Metadata field cannot be empty", {title: 'Info', ttl: 3000});
                    return;
                }
                
                metadata.data=value;
                
                MetadataActionService.updateMetadata($cookies['email'], metadata, self.currentFile.parentId, self.currentFile.name)
                      .then(function (response) {
                        growl.success("Metadata updated successfully", {title: 'Success', ttl: 2000});
                        self.setMetadataTemplate(self.currentFile);
                      }, function (dialogResponse) {
                        growl.info("Could not update metadata " + metadata.data + ".", {title: 'Info', ttl: 5000});
                      });
            };
            
            self.removeMetadata = function (metadata){
                
                if (!self.metaDataDetail) {
                return;
                }                
                
                MetadataActionService.removeMetadata($cookies['email'], metadata, self.currentFile.parentId, self.currentFile.name)
                      .then(function (response) {
                        growl.success("Metadata deleted successfully", {title: 'Success', ttl: 2000});
                        self.setMetadataTemplate(self.currentFile);
                      }, function (dialogResponse) {
                        growl.info("Could not delete metadata " + metadata.data + ".", {title: 'Info', ttl: 5000});
                      });
            };
            

            /**
             * When the user clicks on a folder/file in the file browser the self.currentFile gets updated
             * 
             * @param {type} file
             * @returns {undefined}
             */
            self.setMetadataTemplate = function (file) {
              self.meta = [];
              self.metainfo=[];
              self.visibilityInfo=[];
              var templateId = file.template;
              self.currentTemplateID = templateId;
              self.currentFile = file;
              //update the current file reference
              MetadataHelperService.setCurrentFile(file);
              self.currentFile = MetadataHelperService.getCurrentFile();
              self.noTemplates=false;
              dataSetService.fetchTemplatesForInode(self.currentFile.id)
                      .then(function (response) {
                        self.currentFileTemplates = response.data;
                        self.attachedDetailedTemplateList=[];
                        var index=0;
                        angular.forEach(self.currentFileTemplates, function (template, key) {
                                dataSetService.fetchTemplate(template.templateId, $cookies['email'])
                                .then(function (response) {  
                                    index++;
                                    self.attachedDetailedTemplateList.push({templateid: template.templateId, content: response.data.successMessage});  
                                    if(self.currentFileTemplates.length === index){                                       
                                        self.fetchMetadataForTemplate();
                                    }
                                });                             
                        });
                        if (self.currentFileTemplates.length === 0) {
                          self.noTemplates = true;
                        }

               });
              
            };

            $scope.$on('setMetadata', function(event, args) {
              self.setMetadataTemplate(args.file);
            });

            /**
             * Updates the view according to the user template selection.
             * @returns {undefined}
             */
            self.updateMetadataTabs = function () {
              dataSetService.fetchTemplate(self.selectedTemplate.templateId, $cookies['email'])
                      .then(function (response) {
                        var board = response.data.successMessage;
                        self.currentBoard = JSON.parse(board);
                        self.initializeMetadataTabs(JSON.parse(board));
                      });
            };
            
            

            /**
             * Fetches all the metadata a template holds, for a selected inode
             * 
             * @returns {undefined}
             */
            self.fetchMetadataForTemplate = function () {
              //columns are the tables in the template
              self.meta = [];
              self.metainfo=[];
              self.visibilityInfo=[];
                angular.forEach(self.attachedDetailedTemplateList, function (template, key) {     
                    var templatecontent=JSON.parse(template.content);
                    var tables = templatecontent.columns;
                    angular.forEach(tables, function (table, key) {
                      dataSetService.fetchMetadata(self.currentFile.parentId, self.currentFile.name, table.id)
                              .then(function (response) {
                                var content = response.data[0];
                                self.reconstructMetadata(table.name, table.id, content.metadataView,table.cards);
                              });
                    });
                });
            };

            /**
             * Creates the table with the retrieved metadata, so it can be displayed
             * in the file metadata presentation section
             * 
             * @param {type} tableName
             * @param {type} rawdata
             * @returns {undefined}
             */
            self.reconstructMetadata = function (tableName, tableId, rawdata, cards) {

              $scope.tableName = rawdata.table;
              self.meta.push({name: tableName, rest: rawdata}); 
              
              var cardDescription={};
              angular.forEach(rawdata, function (data, key) {
                   var key=tableId+'-'+data.tagName;
                   self.visibilityInfo[key]=false;
                   angular.forEach(cards, function (card, keycard) {
                        if(data.tagName === card.title){
                            cardDescription[data.tagName]=card.description;
                        }                        
                   });
                   
              });
              
              self.metainfo.push({name: tableName, id: tableId, rest: rawdata, inputcontent: cards, desc:cardDescription});
              

              
              angular.forEach(rawdata.metadataView, function (item, key) {
                   var key=item.id;
                   self.visibilityInfo[key]=false;
              });
              
              self.metadataView = {};

              //console.log("RECONSTRUCTED METADATA " + JSON.stringify(self.meta));
            };
            
            self.setVisibilityAddMetadata = function (key,value){
                self.visibilityInfo[key]=value;
            };

            /**
             * Creates the metadata tabs in the metadata insert page, according to the 
             * template that has been previously selected. Every table in the template
             * corresponds to a tab
             * 
             * @returns {undefined}
             */
            self.initializeMetadataTabs = function () {
              self.tabs = [];

              angular.forEach(self.currentBoard.columns, function (value, key) {
                console.log(key + ': ' + value.name);
                self.tabs.push({tableid: value.id, title: value.name, cards: value.cards});
              });

              self.currentTableId = angular.isUndefined(self.tabs[0]) ? -1 : self.tabs[0].tableid;
            };

            /**
             * Listener on tab selection changes
             * 
             * @param {type} tab
             * @returns {undefined}
             */
            self.onTabSelect = function (tab) {

              self.currentTableId = tab.tableid;
            };

            /**
             * Downloads a template on the fly
             * 
             * @param {type} template
             * @returns {undefined}
             */
            self.createDownloadURL = function (template) {
              var selectedTmptName = template.name;

              //console.log("SELECTED TEMPLATE " + JSON.stringify(template));

              //get the actual template
              MetadataActionService.fetchTemplate($cookies['email'], template.id)
                      .then(function (response) {
                        var contents = JSON.parse(response.board);
                        self.templateContents.templateName = selectedTmptName;
                        self.templateContents.templateContents = contents.columns;

                        //clear any previously created urls
                        if (!angular.isUndefined(self.blob)) {
                          (window.URL || window.webkitURL).revokeObjectURL(self.blob);
                        }

                        //construct the url that downloads the template
                        self.toDownload = JSON.stringify(self.templateContents);
                        self.blob = new Blob([self.toDownload], {type: 'text/plain'});
                        self.url = (window.URL || window.webkitURL).createObjectURL(self.blob);
                      });
            };

            /**
             * Uploads a .json template file to the file system and the database
             * 
             * @returns {undefined}
             */
            self.importTemplate = function () {
              ModalService.importTemplate('md')
                      .then(function (resp) {
                        /*
                         * doesn't really happening anything on success.
                         * it means that the upload was successful and the
                         * window closed automatically
                         */
                        growl.success("The template was uploaded successfully",
                                {title: 'Success', ttl: 15000});
                      },
                              function (closed) {
                                //trigger the necessary variable change in the service
                                MetadataHelperService.fetchAvailableTemplates()
                                        .then(function (response) {
                                          self.availableTemplates = JSON.parse(response.board).templates;
                                        });
                              });
            };
          }
        ]);
