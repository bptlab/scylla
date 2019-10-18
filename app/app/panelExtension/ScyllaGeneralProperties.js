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

function distributionElementAccessor(parentName) {
    return function(element){
        const parent = getExtension(getBusinessObject(element), 'scylla:'+parentName);
        return parent && parent.distribution;
    }
}

const distributions = 
    [
        { 
            value: "Binomial",					
            name: "Binomial",       
            attributes: [
                {name : "probability", type : "float", range : "probability"},
                {name : "amount", type : "int", range : "positive"}
            ]
        }, 	    //new AttributeType[]{AttributeType.DOUBLE,AttributeType.INT]}),
        {  
            value: "Constant", 				    
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
            attributes: [
                {name : "entryset", type : "entryset"}
            ]
        }, 						    //new AttributeType[]{]}),
    ]

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

function distributionIdToDescriptorProperty(id) {
    return 'scylla:'+id+'Distribution';
}

function distributionDescriptorPropertyToId(element) {
    return element.$type.replace('Distribution','').replace('scylla:','');
}

function distributionById(id) {
    return distributions.find(each => each.value == id);
}

function createDistributionSelectBox(parentName){
    let getDistributionElement = distributionElementAccessor(parentName);
    return entryFactory.selectBox({
        id : 'distribution',
        //description : 'How is the duration distributed?',
        label : 'Distribution',
        modelProperty : distributionProperty,
        selectOptions : distributions,
        get : element => {
            const distribution = getDistributionElement(element);
            backend.print("Element is "+JSON.stringify(element));
            backend.print("Element is "+JSON.stringify(getBusinessObject(element)));
            backend.print("Distribution is "+JSON.stringify(distribution));
            let toReturn = {};
            toReturn[distributionProperty] = distribution ? distributionDescriptorPropertyToId(distribution) : undefined;
            return toReturn;
        },
        set: (element, value) => {
            const moddle = modeler.get('moddle'),
                modeling = modeler.get('modeling');
            let businessObject = getBusinessObject(element);
            const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
            let duration = getExtension(businessObject, 'scylla:'+parentName);
            let distributionId = value[distributionProperty];
            let distributionType = distributionById(distributionId);
            let distributionElement = moddle.create(distributionIdToDescriptorProperty(distributionId));

            if (!duration) {
                duration = moddle.create('scylla:'+parentName);
                extensionElements.get('values').push(duration);
            }

            distributionType.attributes
                .filter(each => (each.type != "entryset"))
                .forEach(each => {
                    let element = moddle.createAny('scylla:'+each.name);
                    let defaultValue;
                    switch(each.range){
                        case "positive" : defaultValue = '1'; break;
                        case "probability" : defaultValue = '0.5'; break;
                        default: ;
                    }
                    element.$body = each.default || defaultValue || '0';
                    distributionElement[each.name] = element;
                });

            duration.distribution = distributionElement;
        
            //duration.distribution = value;
            //analysisDetails.lastChecked = new Date().toISOString();
        
            modeling.updateProperties(element, {
              extensionElements
            });
        }
    });
}

function createDistributionAttributeInputs(distribution, parentName){
    let getDistributionElement = distributionElementAccessor(parentName);
    let distributionType = distributionById(distributionDescriptorPropertyToId(distribution)); 
    let prefix = distributionProperty+'.'+distributionType.value;
    if(distributionType.value == "arbitraryFiniteProbability"){
        return [createEntrysetInput(prefix, getDistributionElement)]
    } else {
        return distributionType.attributes.map(each => createNumberInput(each, prefix, getDistributionElement));
    }
}

function createNumberInput(attribute, prefix, getDistributionElement) {
    let attributeName = attribute.name;
    let propertyName = prefix+'.'+attributeName;
    let textField = entryFactory.textField({
        id : propertyName,
        label : attributeName,
        modelProperty : propertyName,
        get : element => {
            let distribution = getDistributionElement(element);
            let toReturn = {};
            toReturn[propertyName] = distribution[attributeName];
            return toReturn;
        },
        set: (element, value) => {
            let distribution = getDistributionElement(element);
            distribution[attributeName] = value && value[propertyName];
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
    return textField;
}

function createEntrysetInput(prefix, getDistributionElement) {

    let propertyName = prefix+'.entryset';
    let valueProperty = propertyName+'.value';
    let probabilityProperty = propertyName+'.probability';

    return entryFactory.table({
        id : propertyName,
        modelProperty : propertyName,
        labels: [ 'Value', 'Probability' ],
        modelProperties: [ valueProperty, probabilityProperty ],
        addLabel: 'New entry',
        show: function(element, node) {
          return true;
        },
        getElements: function(element) {
            let distribution = getDistributionElement(element);
            if(!distribution) return [];//For unknown reasons, the system tries to create the table for other elements than those it is actually created for
            let entries = distribution.entries;
            return entries.map(each => {
                let entry = new Object();
                entry[valueProperty] = ""+each.value;
                entry[probabilityProperty] = ""+each.probability;
                return entry;
            });
        },
        addElement: function(element, node) {
            const moddle = modeler.get('moddle');
            let distribution = getDistributionElement(element);
            let newEntry = moddle.createAny('scylla:'+'entry');
            newEntry.value = 0;
            newEntry.probability = 0;
            distribution.entryset.push(newEntry);
            return;
        },
        removeElement: function(element, node, idx) {
            let distribution = getDistributionElement(element);
            distribution.entryset.splice(idx,1);
            return [];
        },
        updateElement: function(element, value, node, idx) {
            let distribution = getDistributionElement(element);
            let entry = distribution.entryset[idx];
            entry.value = value[valueProperty];
            entry.probability = value[probabilityProperty];
            return [];
        },
        validate: function(element, value, node, idx) {}
    });
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

function createTimeUnitSelectBox(parentName) {
    return entryFactory.selectBox({
        id : 'timeUnit',
        //description : 'Which timeUnit?',
        label : 'Timeunit',
        //modelProperty : 'distribution',
        selectOptions : timeUnits(),
        get : element => {
            const duration = getExtension(getBusinessObject(element), 'scylla:'+parentName);
            return {undefined : (duration ? duration.timeUnit : timeUnits()[0].value)};
        },
        set: (element, value) => {
            const moddle = modeler.get('moddle'),
                modeling = modeler.get('modeling');
            let businessObject = getBusinessObject(element);
            const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
            let duration = getExtension(businessObject, 'scylla:'+parentName);

            if (!duration) {
                duration = moddle.create('scylla:'+parentName);
                extensionElements.get('values').push(duration);
            }

            duration.timeUnit = value.undefined;
        
            //duration.distribution = value;
            //analysisDetails.lastChecked = new Date().toISOString();
        
            modeling.updateProperties(element, {
              extensionElements
            });
        }
    })
}

function createResourceTable() {
    return entryFactory.table({
        id : 'resource',
        description : 'Which resource to assign',
        modelProperty : 'resources',
        labels: [ 'Resource', 'Quantity' ],
        modelProperties: [ 'resourceName', 'quantity' ],
        addLabel: 'Add assignment',
        show: function(element, node) {
          return true;
        },
        getElements: resourcesOf,
        addElement: function(element, node) {
            ensureResources(element);
            const moddle = modeler.get('moddle');
            let newAssignment = moddle.create('scylla:Resource');
            newAssignment.resourceName = 'foo';
            newAssignment.quantity = 1;
            resourcesOf(element).push(newAssignment);
        },
        removeElement: function(element, node, idx) {
            ensureResources(element);
            resourcesOf(element).splice(idx,1);
        },
        updateElement: function(element, value, node, idx) {
            let assignment = resourcesOf(element)[idx];
            assignment.resourceName = value.resourceName;
            assignment.quantity = value.quantity;
        },
        validate: function(element, value, node) {
            let validationResult = new Object();
            if(!availableResources().includes(value.resourceName))validationResult.resourceName = "Unknown resource id";
            if(!Number.isInteger(+value.quantity))validationResult.quantity = "Resource quantities must be integers";
            return (validationResult.resourceName || validationResult.quantity) && validationResult;
        }
      });
}

function availableResources(){
    let allSet = [];
    allSet.includes = (element) => true;
    return allSet;
}

function ensureResources(element) {
    const moddle = modeler.get('moddle');
    const modeling = modeler.get('modeling');
    const businessObject = getBusinessObject(element);
    const extensionElements = businessObject.extensionElements || moddle.create('bpmn:ExtensionElements');
    let resources = getExtension(businessObject, 'scylla:Resources');
    if (!resources) {
        resources = moddle.create('scylla:Resources', {resource: []});
        extensionElements.get('values').push(resources);
    }

    modeling.updateProperties(element, {
      extensionElements
    });
    return resources;
}

function resourcesOf(element) {
    const resources = getExtension(getBusinessObject(element), 'scylla:Resources');
    return resources ? resources.resource || (resources.resource = []) : [];
}

function getExtension(element, type) {
    if (!element.extensionElements) {
        return;
    }

    return element.extensionElements.values.filter(function(e) {
        return e.$instanceOf(type);
    })[0];
}

function distributionGroup(element, id, label){
    var group = {
        id: id,
        label: label,
        entries: []
    };

    group.entries.push(createDistributionSelectBox(id));

    let distribution = distributionElementAccessor(id)(element);
    if(distribution) {
        createDistributionAttributeInputs(distribution, id).forEach(each => group.entries.push(each));
    }
    
    group.entries.push(createTimeUnitSelectBox(id));
    return group
}


export default function(element) {

    if(is(element, 'bpmn:Task')){
        var durationGroup = distributionGroup(element, 'Duration', 'Duration');

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
        resourcesGroup.entries.push(createResourceTable());

        return [durationGroup, resourcesGroup];
    }

    if(is(element,'bpmn:StartEvent')){

        var arrivalRateGroup = distributionGroup(element, 'arrivalRate', 'Arrival Rate');

        return [arrivalRateGroup];
    }

    return [];
}