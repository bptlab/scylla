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
    var viewer = new BpmnModeler({
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
            camunda: scyllaModdleDescriptor
        }
    });
} catch(err) {log(err)};
container.removeClass('with-diagram');
container.addClass('with-diagram');

var overlays = viewer.get('overlays');

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

function openXML(xml) {
    log("xml");
    viewer.importXML(xml, function(err) {
        if (err) {
        log('error: ' + err.message);
        console.error(err);
        } else {
        viewer.get('canvas').zoom('fit-viewport');
        log('success');
        }
    });
}

///////Buttons
$('#js-open2').click(function() {
    var url = $('#js-url').val();
    openFromUrl(url);
});

$('#js-open').click(function() {
    log("trying to load...");
    java.loadDiagram(openXML);
});



$('#js-showOverlays').click(function() {
    showOverlays();
});

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