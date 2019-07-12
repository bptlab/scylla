import inherits from 'inherits';
import PropertiesActivator from 'bpmn-js-properties-panel/lib/PropertiesActivator';

import processProps from 'bpmn-js-properties-panel/lib/provider/bpmn/parts/ProcessProps';
import eventProps from 'bpmn-js-properties-panel/lib/provider/bpmn/parts/EventProps';
import linkProps from 'bpmn-js-properties-panel/lib/provider/bpmn/parts/LinkProps';
import documentationProps from 'bpmn-js-properties-panel/lib/provider/bpmn/parts/DocumentationProps';
import idProps from 'bpmn-js-properties-panel/lib/provider/bpmn/parts/IdProps';
import nameProps from 'bpmn-js-properties-panel/lib/provider/bpmn/parts/NameProps';


import xyzPluginProperties from './XyzPluginProperties';
import batchPluginProperties from './BatchPluginProperties';
import scyllaGeneralProperties from './ScyllaGeneralProperties';


export default function ScyllaPropertiesProvider(eventBus, bpmnFactory, elementRegistry, translate) {

    PropertiesActivator.call(this, eventBus);
    
    this.getTabs = function(element) {

        
        var generalTab = {
            id: 'general',
            label: 'General',
            groups: createGeneralTabGroups(element, bpmnFactory, elementRegistry, translate)
        };

        var scyllaTab = {
            id: 'scylla',
            label: 'Scylla',
            groups: createScyllaTabGroups(element, elementRegistry)
        };

        // All avaliable tabs
        return [
            generalTab,
            scyllaTab
        ];
    };
}

function createGeneralTabGroups(element, bpmnFactory, elementRegistry, translate) {

    var generalGroup = {
      id: 'general',
      label: 'General',
      entries: []
    };
    idProps(generalGroup, element, translate);
    nameProps(generalGroup, element, translate);
    processProps(generalGroup, element, translate);

    var scyllaGeneralGroups = scyllaGeneralProperties(element);
    
  
    var detailsGroup = {
      id: 'details',
      label: 'Details',
      entries: []
    };
    linkProps(detailsGroup, element, translate);
    eventProps(detailsGroup, element, bpmnFactory, elementRegistry, translate);
  
    var documentationGroup = {
      id: 'documentation',
      label: 'Documentation',
      entries: []
    };
  
    documentationProps(documentationGroup, element, bpmnFactory, translate);
  
    return [generalGroup]
      .concat(scyllaGeneralGroups)
      .concat([detailsGroup, documentationGroup]);
}

function createScyllaTabGroups(element, elementRegistry) {

    // Create a group called 'XYZ Plugin'
    var xyzPluginGroup = {
      id: 'xyzPlugin',
      label: 'XYZ Plugin',
      entries: []
    };
  
    // Add the xyz props to the xyz plugin group.
    xyzPluginProperties(xyzPluginGroup, element);

    
    var batchPluginGroup = {
      id: 'batchPlugin',
      label: 'Batch Plugin',
      entries: []
    };
    batchPluginProperties(xyzPluginGroup, element);
  
    return [
        xyzPluginGroup,
        batchPluginGroup
    ];
  }

inherits(ScyllaPropertiesProvider, PropertiesActivator);