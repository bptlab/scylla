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
    const duration = getExtension(getBusinessObject(element), 'scylla:duration');
    return duration && duration.$children && duration.$children[indexOfDistribution];
}

function distributions() {
    return [
        { 
            value: "binomial",					
            name: "Binomial",       
            attributes: [
                {name : "probability", type : "float", range : "probability"},
                {name : "amount", type : "int", range : "positive"}
            ]
        }, 	    //new AttributeType[]{AttributeType.DOUBLE,AttributeType.INT]}),
        {  
            value: "constant", 				    
            name: "Constant", 		
            attributes: [
                {name : "constantValue", type : "float"}
            ]
        }, 			    //new AttributeType[]{AttributeType.DOUBLE]}),
        { 
            value: "erlang", 					    
            name: "Erlang", 		
            attributes: [
                {name : "order", type : "int", range : "positive"},
                {name : "mean", type : "float", range : "positive"}
            ]
        }, 			    //new AttributeType[]{AttributeType.INT,AttributeType.DOUBLE]}),
        { 
            value: "exponential", 				
            name: "Exponential", 	
            attributes: [
                {name : "mean", type : "float", range : "positive"}
            ]
        }, 					    //new AttributeType[]{AttributeType.DOUBLE]}),
        { 
            value: "triangular", 				    
            name: "Triangluar", 	
            attributes: [
                {name : "lower", type : "float"},
                {name : "peak", type : "float"},
                {name : "upper", type : "float"}
            ]
        }, 	    //new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE,AttributeType.DOUBLE]}),
        { 
            value: "normal", 					    
            name: "Normal", 		
            attributes: [
                {name : "mean", type : "float"},
                {name : "standardDeviation", type : "float", range : "positive"}
            ]
        },   //new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE]}),
        { 
            value: "poisson", 					
            name: "Poisson", 		
            attributes: [
                {name : "mean", type : "float", range : "positive"}
            ]
        }, 					    //new AttributeType[]{AttributeType.DOUBLE]}),
        { 
            value: "uniform", 					
            name: "Uniform", 		
            attributes: [
                {name : "lower", type : "float"},
                {name : "upper", type : "float"}
            ]
        }, 			    //new AttributeType[]{AttributeType.DOUBLE,AttributeType.DOUBLE]}),
        { 
            value: "arbitraryFiniteProbability",  
            name: "Discrete", 		
            attributes: []
        }, 						    //new AttributeType[]{]}),
    ]
}

function getInput(attribute){
    let inputString = "";
    switch(attribute.type){
        case "int" : inputString += 'type="number" step="1" '; break;
        case "float" : inputString += 'type="number" step="0.01" '; break;
        default: throw "Unknown distribution attribute type";
    }

    switch(attribute.range){
        case "positive" :
        case "non-negative" : inputString += 'min="0" '; break;

        case "probability" : inputString += 'min="0" max="1" '; break;

        case "unlimited" :
        default: ;
    }

    return inputString;
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
                // getExtension(getBusinessObject(element), 'scylla:duration').distribution = value
                const moddle = modeler.get('moddle'),
                    modeling = modeler.get('modeling');
                let businessObject = getBusinessObject(element);
                const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
                let duration = getExtension(businessObject, 'scylla:duration');
                let distributionId = value[distributionProperty];
                let distributionType = distributionById(distributionId);
                let distributionElement = moddle.createAny(distributionIdToElementName(distributionId));

                if (!duration) {
                    duration = moddle.createAny('scylla:duration', 'http://scylla', {$children: []});
                    extensionElements.get('values').push(duration);
                }

                distributionElement.$children = distributionType.attributes.map(each => {
                    let element = moddle.createAny('scylla:'+each.name);
                    let defaultValue;
                    switch(each.range){
                        case "positive" : defaultValue = 1; break;
                        case "probability" : defaultValue = 0.5; break;
                        default: ;
                    }
                    element.$body = each.default || defaultValue || '0';
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
                let attributeName = attribute.name;
                let propertyName = distributionProperty+'.'+type.value+'.'+attributeName;
                let textField = entryFactory.textField({
                    id : propertyName,
                    label : attributeName,
                    modelProperty : propertyName,
                    get : element => {
                        let distribution = getDistributionElement(element);
                        let attributeElement = distribution.$children.find(each => each.$type == 'scylla:'+attributeName);
                        let toReturn = {};
                        toReturn[propertyName] = attributeElement.$body;
                        return toReturn;
                    },
                    set: (element, value) => {
                        let distribution = getDistributionElement(element);
                        let attributeElement = distribution.$children.find(each => each.$type == 'scylla:'+attributeName);
                        attributeElement.$body = value && value[propertyName];
                    },
                    validate: function(element, values) {
                        let value = values[propertyName];
                        let error = new Object();
                        if(attribute.type == "int" || attribute.type == "float"){
                            let type = attribute.type == "int" ? "an integer" : "a number";
                            if(isNaN(value) || value == "" || (attribute.type == "int" && !Number.isInteger(+value)))error[propertyName] = "Please enter "+type;
                            else if(attribute.range == "positive" && value <= 0)error[propertyName] = "Only strictly positive values allowed";
                            else if(attribute.range == "non-negative" && value < 0)error[propertyName] = "Only non-negative values allowed";
                            else if(attribute.range == "probability" && (value < 0 || value > 1))error[propertyName] = "Only values between 0 and 1 allowed";
                        }

                        
                        return error;
                    }
                });
				textField.html = textField.html
					.replace('<div', '<div style="padding-left: 30pt"')
                    .replace('<label', '<label style="padding-left: 30pt"')
                    .replace('type="text"', getInput(attribute));
                //backend.print(textField.html);
                durationGroup.entries.push(textField);
            });
        }
        

        durationGroup.entries.push(entryFactory.selectBox({
            id : 'timeUnit',
            //description : 'Which timeUnit?',
            label : 'Timeunit',
            //modelProperty : 'distribution',
            selectOptions : timeUnits(),
            get : element => {
                const extensionElements = getExtension(getBusinessObject(element), 'scylla:duration');
                return {undefined : (extensionElements ? extensionElements.timeUnit : timeUnits()[0].value)};
            },
            set: (element, value) => {
                // getExtension(getBusinessObject(element), 'scylla:duration').distribution = value
                const moddle = modeler.get('moddle'),
                    modeling = modeler.get('modeling');
                let businessObject = getBusinessObject(element);
                const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
                let duration = getExtension(businessObject, 'scylla:duration');

                if (!duration) {
                    duration = moddle.createAny('scylla:duration', 'http://scylla', {$children: []});
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