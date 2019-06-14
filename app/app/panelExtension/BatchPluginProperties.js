import entryFactory from 'bpmn-js-properties-panel/lib/factory/EntryFactory';

import {
  is, getBusinessObject
} from 'bpmn-js/lib/util/ModelUtil';


function isActivity(element) {
  return is(element, 'bpmn:SubProcess') || is(element, 'bpmn:Task');
}

function isBatchActivity(element) {
  return isActivity(element) && getBusinessObject(element).get('isBatch');
}


export default function(group, element) {

  if (isActivity(element)) {
    group.entries.push(entryFactory.checkbox({
      id : 'isBatch',
      description : 'How fluffy is the start event?',
      label : 'Batch A',
      modelProperty : 'isBatch'
    }));
  }

  if(isBatchActivity(element)){
    group.entries.push(entryFactory.textField({
      id : 'maxBatchSize',
      description : 'Maximum number of cases per batch',
      label : 'Max Batch Size',
      modelProperty : 'maxBatchSize'
    }));
  }

}