function setUp(scrollBot){
    document.addEventListener("wheel", function(e){
        scrollBot.notifyScroll(1000);
    },true);
}

function openFileDialog(callback,fileFilters){
    showOverlay();
    new Promise((resolve, reject)=>{
        initCancel(resolve);
        initDropInput(resolve);
        initFileInput(resolve,fileFilters);
    }).then(function(result){
        hideOverlay();
        callback.accept(result.join("|"));
    });
}

function showOverlay(){
	var body = document.body;
	var overlay = document.createElement('div');
	overlay.innerHTML = overlayNode.trim();
    body.appendChild(overlay.firstChild);
}

function hideOverlay(){
    var overlay = document.getElementById('overlay_upload');
    overlay.parentNode.removeChild(overlay);
}

function uploadFiles(files){
    var promises = [];
    for (var i = 0, f; f = files[i]; i++) {
        promises[i] = uploadFile(f);
    }
    //united.then(_ => {hideOverlay()});
    return Promise.all(promises);
}

function uploadFile(file){
    return new Promise(function(resolve,reject){
        var name = escape(file.name);
        var reader = new FileReader();
        var fileByteArray = [];
        reader.onloadend = function (evt) {
            if (reader.readyState == FileReader.DONE) {
                var dataUrl = reader.result;
                var data = dataUrl.substring(dataUrl.indexOf(',')+1);
                fileWriter.requestNewJob(name).then(function(writeJob){
                    var partitionSize = 524288;//512 KiB
                    for(var i = 0; i < data.length; i += partitionSize) {
                        var partition = data.substring(i,Math.min(i+partitionSize,data.length));
                        writeJob.writeBase64(partition);
                    }
                    writeJob.finish().then(resolve);
                    //resolve(file.name);
                });
            }
        }
        reader.readAsDataURL(file);
    });
}

function initCancel(callback){
    document.getElementById('cancelButton').onclick = function(){
        callback([]);
    }
}

function initFileInput(callback, fileFilters){
    function handleFileSelect(evt) {
        var files = evt.target.files;
        uploadFiles(files).then((result)=>{callback(result)});     
    }
    var fileInput = document.getElementById('fileInput');
    for(var i = fileFilters.length-1; i >= 0; i--){
        var extensions = fileFilters[i];
        for(var j = extensions.length-1; j >= 0; j--){
            fileInput.accept += '.'+extensions[j]+', ';
        }
    }
    fileInput.addEventListener('change', handleFileSelect, false);
    fileInput.click();
}

function initDropInput(callback){
    function handleFileSelect(evt) {
        evt.stopPropagation();
        evt.preventDefault();
    
        var files = evt.dataTransfer.files;
        uploadFiles(files).then((result)=>{callback(result)});   ;
    }
    
    function handleDragOver(evt) {
        evt.stopPropagation();
        evt.preventDefault();
        evt.dataTransfer.dropEffect = 'copy';
    }
    
    // Set up drag n drop listeners.
    var dropZone = document.getElementById('drop_zone');
    dropZone.addEventListener('dragover', handleDragOver, false);
    dropZone.addEventListener('drop', handleFileSelect, false);
}

/**function download(filename, text) {
        var element = document.createElement('a');
        element.setAttribute('href', 'data:;ISO-8859-1,' + escape(text));
        element.setAttribute('download', filename);
        element.style.display = 'none';
        document.body.appendChild(element);
        element.click();
        document.body.removeChild(element);
    } */