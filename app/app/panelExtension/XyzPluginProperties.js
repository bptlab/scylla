import entryFactory from 'bpmn-js-properties-panel/lib/factory/EntryFactory';

import {
  is
} from 'bpmn-js/lib/util/ModelUtil';


export default function(group, element) {

  if (is(element, 'bpmn:Task')) {
    group.entries.push(entryFactory.textField({
      id : 'fluffiness',
      description : 'How fluffy is the start event?',
      label : 'Fluffiness',
      modelProperty : 'fluffiness'
    }));
  }
}