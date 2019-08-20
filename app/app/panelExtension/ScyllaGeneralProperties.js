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

const distributionProperty = "duration.distribution";

function getDistributionElement(element) {
    const duration = getExtension(getBusinessObject(element), 'scylla:Duration');
    return duration && duration.$children && duration.$children[indexOfDistribution];
}

function distributions() {
    return [
        { value: "binomial",					name: "Binomial",       attributes: ["probability","amount"]}, 	    //new AttributeType[]{AttributeType.DOUBLE,AttributeType.INT]}),
        { value: "constant", 				    name: "Constant", 		attributes: ["constantValue"]}, 			    //new AttributeType[]{AttributeType.DOUBLE]}),
        { value: "erlang", 					    name: "Erlang", 		attributes: ["order","mean"]}, 			    //new AttributeType[]{AttributeType.INT,AttributeType.DOUBLE]}),
        { value: "exponential", 				name: "Exponential", 	attributes: ["mean"]}, 					    //new AttributeType[]{AttributeType.DOUBLE]}),
        { value: "triangular", 				    name: "Triangluar", 	attributes: ["lower","peak","upper"]}, 	    //new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE,AttributeType.DOUBLE]}),
        { value: "normal", 					    name: "Normal", 		attributes: ["mean","standardDeviation"]},   //new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE]}),
        { value: "poisson", 					name: "Poisson", 		attributes: ["mean"]}, 					    //new AttributeType[]{AttributeType.DOUBLE]}),
        { value: "uniform", 					name: "Uniform", 		attributes: ["lower","upper"]}, 			    //new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE]}),
        { value: "arbitraryFiniteProbability",  name: "Discrete", 		attributes: []}, 						    //new AttributeType[]{]}),
    ]
}

function distributionIdToElementName(id) {
    return 'scylla:'+id+'Distribution';
}

function distributionElementToId(element) {
    return element.$type.replace('Distribution','').replace('scylla:','');
}

function distributionById(id) {
    return distributions().find(each => each.value == id);
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

        let distributionBox = entryFactory.selectBox({
            id : 'distribution',
            //description : 'How is the duration distributed?',
            label : 'Distribution',
            modelProperty : distributionProperty,
            selectOptions : distributions(),
            get : element => {
                const distribution = getDistributionElement(element);
                let toReturn = {};
                toReturn[distributionProperty] = distribution ? distributionElementToId(distribution) : distributions()[0].value;
                return toReturn;
            },
            set: (element, value) => {
                // getExtension(getBusinessObject(element), 'scylla:Duration').distribution = value
                const moddle = modeler.get('moddle'),
                    modeling = modeler.get('modeling');
                let businessObject = getBusinessObject(element);
                const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
                let duration = getExtension(businessObject, 'scylla:Duration');
                let distributionId = value[distributionProperty];
                let distributionType = distributionById(distributionId);
                let distributionElement = moddle.createAny(distributionIdToElementName(distributionId));

                if (!duration) {
                    duration = moddle.createAny('scylla:Duration', 'http://scylla', {$children: []});
                    extensionElements.get('values').push(duration);
                }

                distributionElement.$children = distributionType.attributes.map(each => {
                    let element = moddle.createAny('scylla:'+each);
                    element.$body = '0';
                    return element;
                });

                duration.$children[indexOfDistribution] = distributionElement;
            
                //duration.distribution = value;
                //analysisDetails.lastChecked = new Date().toISOString();
            
                modeling.updateProperties(element, {
                  extensionElements
                });
            }
        });
        durationGroup.entries.push(distributionBox);

        let distribution = getDistributionElement(element);
        if(distribution) {
            let type = distributionById(distributionElementToId(distribution)); 
            type.attributes.forEach(attribute => {
                let propertyName = distributionProperty+'.'+type.value+'.'+attribute;
                let textField = entryFactory.textField({
                    id : propertyName,
                    label : attribute,
                    modelProperty : propertyName,
                    get : element => {
                        let distribution = getDistributionElement(element);
                        let attributeElement = distribution.$children.find(each => each.$type == 'scylla:'+attribute);
                        let toReturn = {};
                        toReturn[propertyName] = attributeElement.$body;
                        return toReturn;
                    },
                    set: (element, value) => {
                        let distribution = getDistributionElement(element);
                        let attributeElement = distribution.$children.find(each => each.$type == 'scylla:'+attribute);
                        attributeElement.$body = value && value[propertyName];
                    }
                });
				textField.html = textField.html
					.replace('<div', '<div style="padding-left: 30pt"')
					.replace('<label', '<label style="padding-left: 30pt"');
                durationGroup.entries.push(textField);
            });
        }
        

        durationGroup.entries.push(entryFactory.selectBox({
            id : 'timeUnit',
            //description : 'Which timeUnit?',
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