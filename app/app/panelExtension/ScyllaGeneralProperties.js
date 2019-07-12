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

function distributions() {
    return [
        { value: "binomial",					name: "Binomial", 		    },//new String[]{"probability","amount"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.INT}),
        { value: "constant", 				    name: "Constant", 		    },//new String[]{"constantValue"}, 			new AttributeType[]{AttributeType.DOUBLE}),
        { value: "erlang", 					    name: "Erlang", 			},//new String[]{"order","mean"}, 			new AttributeType[]{AttributeType.INT,AttributeType.DOUBLE}),
        { value: "exponential", 				name: "Exponential", 		},//new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
        { value: "triangular", 				    name: "Triangluar", 		},//new String[]{"lower","peak","upper"}, 	new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE,AttributeType.DOUBLE}),
        { value: "normal", 					    name: "Normal", 			},//new String[]{"mean","standardDeviation"}, new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE}),
        { value: "poisson", 					name: "Poisson", 			},//new String[]{"mean"}, 					new AttributeType[]{AttributeType.DOUBLE}),
        { value: "uniform", 					name: "Uniform", 			},//new String[]{"lower","upper"}, 			new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE}),
        { value: "arbitraryFiniteProbability",  name: "Discrete", 		    },//new String[]{}, 						new AttributeType[]{}),
    ]
}

var entries = new Map();

function createResourceTable(group) {
    group.entries.push(entryFactory.table({
        id : 'resource',
        description : 'Which resource to assign',
        modelProperty : 'resources',
        labels: [ 'Resource', 'Quantity' ],
        modelProperties: [ 'resourceName', 'quantity' ],
        addLabel: 'Add assignment',
        show: function(element, node) {
          return true;
        },
        getElements: entriesOf,
        addElement: function(element, node) {
            entriesOf(element).push({resourceName: 'foo', quantity: 1});
            return [];
        },
        removeElement: function(element, node, idx) {
            entriesOf(element).splice(idx,1);
            return [];
        },
        updateElement: function(element, value, node, idx) {
            entriesOf(element)[idx] = value;
            return [];
        },
        validate: function(element, value, node, idx) {}
      }));
}

function entriesOf(element) {
    if(!entries.has(element))entries.set(element, []);
    return entries.get(element);
}


export default function(element) {

    if(is(element, 'bpmn:Task')){
        var durationGroup = {
            id: 'duration',
            label: 'Duration',
            entries: []
        };

        durationGroup.entries.push(entryFactory.selectBox({
            id : 'distribution',
            description : 'How is the duration distributed?',
            label : 'Distribution',
            modelProperty : 'distribution',
            selectOptions : distributions()
        }));

        var resourcesGroup = {
            id: 'resources',
            label: 'Assigned Resources',
            entries: []
        };

        // resourcesGroup.entries.push(entryFactory.selectBox({
        //     id : 'resource',
        //     description : 'Which resource to assign',
        //     labels : 'Resources',
        //     modelProperty : 'resources'
        // }));
        createResourceTable(resourcesGroup);

        return [durationGroup, resourcesGroup];
    }

    return [];
}