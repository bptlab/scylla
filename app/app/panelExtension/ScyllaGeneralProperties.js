import entryFactory from 'bpmn-js-properties-panel/lib/factory/EntryFactory';

import {
  is, getBusinessObject
} from 'bpmn-js/lib/util/ModelUtil';

const indexOfDistribution = 0;


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

function timeUnits() {
    return [
        {value: "NANOSECONDS",  name: "Nanoseconds"},
        {value: "MICROSECONDS", name: "Microseconds"},
        {value: "MILLISECONDS", name: "Milliseconds"},
        {value: "SECONDS",      name: "Seconds"},
        {value: "MINUTES",      name: "Minutes"},
        {value: "HOURS",        name: "Hours"},
        {value: "DAYS",         name: "Days"}
    ];
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

function getExtension(element, type) {
    if (!element.extensionElements) {
        return;
    }

    return element.extensionElements.values.filter(function(e) {
        return e.$instanceOf(type);
    })[0];
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
            //modelProperty : 'distribution',
            selectOptions : distributions(),
            get : element => {
                const extensionElements = getExtension(getBusinessObject(element), 'scylla:Duration');
                //backend.print(JSON.stringify(extensionElements));
                let toReturn = extensionElements && extensionElements.$children && extensionElements.$children[indexOfDistribution]
                    ? extensionElements.$children[indexOfDistribution].$type.replace('Distribution','').replace('scylla:','') : distributions()[0].value;
                return {undefined : toReturn};
            },
            set: (element, value) => {
                // getExtension(getBusinessObject(element), 'scylla:Duration').distribution = value
                const moddle = modeler.get('moddle'),
                    modeling = modeler.get('modeling');
                let businessObject = getBusinessObject(element);
                const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
                let duration = getExtension(businessObject, 'scylla:Duration');

                let distributionElement = moddle.createAny('scylla:'+value.undefined+'Distribution');

                if (!duration) {
                    duration = moddle.createAny('scylla:Duration', 'http://scylla', {$children: []});
                    extensionElements.get('values').push(duration);
                }

                duration.$children[indexOfDistribution] = distributionElement;
            
                //duration.distribution = value;
                //analysisDetails.lastChecked = new Date().toISOString();
            
                modeling.updateProperties(element, {
                  extensionElements
                });
            }
        }));

        durationGroup.entries.push(entryFactory.selectBox({
            id : 'timeUnit',
            description : 'Which timeUnit?',
            label : 'TimeUnit',
            //modelProperty : 'distribution',
            selectOptions : timeUnits(),
            get : element => {
                const extensionElements = getExtension(getBusinessObject(element), 'scylla:Duration');
                return {undefined : (extensionElements ? extensionElements.timeUnit : timeUnits()[0].value)};
            },
            set: (element, value) => {
                // getExtension(getBusinessObject(element), 'scylla:Duration').distribution = value
                const moddle = modeler.get('moddle'),
                    modeling = modeler.get('modeling');
                let businessObject = getBusinessObject(element);
                const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
                let duration = getExtension(businessObject, 'scylla:Duration');

                if (!duration) {
                    duration = moddle.createAny('scylla:Duration', 'http://scylla', {$children: []});
                    extensionElements.get('values').push(duration);
                }

                duration.timeUnit = value.undefined;
            
                //duration.distribution = value;
                //analysisDetails.lastChecked = new Date().toISOString();
            
                modeling.updateProperties(element, {
                  extensionElements
                });
            }
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