import $ from 'jquery';
import BpmnModeler from 'bpmn-js/lib/Modeler';
import propertiesPanelModule from 'bpmn-js-properties-panel';
//import propertiesProviderModule from 'bpmn-js-properties-panel/lib/provider/camunda';
import propertiesProviderModule  from './panelExtension';
//import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda';
import scyllaModdleDescriptor from './panelExtension/scyllaPropertiesDescriptor';

import minimapModule from 'diagram-js-minimap';

console.log = log;

var container = $('#js-drop-zone');
try{
    var modeler = new BpmnModeler({
        container: $('#js-canvas'),
        propertiesPanel: {
            parent: '#js-properties-panel'
        },  
        additionalModules: [
            propertiesPanelModule,
            propertiesProviderModule,
            minimapModule
        ],
        moddleExtensions: {
            scylla: scyllaModdleDescriptor //Saves all scylla extensions with prefix "scylla"
        }
    });
    modeler.on('element.changed', function(event) { 
        //Note: This is also called when an element is created or deleted
        //var element = event.element;
        backend.modelChanged();         
    });

} catch(err) {log(err)};

var overlays = modeler.get('overlays');
var minimap = modeler.get('minimap');

function log(str) {
    var console = $('#js-console');
    console.val(console.val() + str + '\n');
}

function openFromUrl(url) {

    log('attempting to open <' + url + '>');
    $.ajax(url, { dataType : 'text' }).done(function(xml) {
    openXML(xml);
    });
}

window.openXML = function openXML(xml) {
    log("xml");
    modeler.importXML(xml, function(err) {
        if (err) {
            log('error: ' + err.message);
            console.error(err);
        } else {
            modeler.get('canvas').zoom('fit-viewport');
            minimap.open();
            log('success');
        }
    });
}

window.clear = function() {
    modeler.clear();
}

window.save = function(path) {
    modeler.saveXML({ format: true }, function(err, xml) {
        if(!err) {
            backend.save(path, xml);
        };
    });
}

///////Buttons
$('#js-open2').click(function() {
    var url = $('#js-url').val();
    openFromUrl(url);
});

$('#js-open').click(function() {
    log("trying to load...");
    backend.loadDiagram(openXML);
});


$('#js-showOverlays').click(showOverlays);

function showOverlays() {
    overlays.add('StartEvent_1', 'note', {
        position: {
        bottom: 0,
        right: 0
        },
        scale: false,
        html: '<div class="diagram-note">I don\'t scale</div>'
    });
}


$('#export-model').click(exportModel);

function exportModel() {
    modeler.saveXML({ format: true }, function(err, xml) {
        if(!err) {backend.print(xml)};
    });
}

///// auto open ?url=diagram-url ///////////////////////
(function() {
    var str = window.location.search;
    var match = /(?:\&|\?)url=([^&]+)/.exec(str);
    if (match) {
    var url = decodeURIComponent(match[1]);
    $('#js-url').val(url);
    openFromUrl(url);
    }
})();