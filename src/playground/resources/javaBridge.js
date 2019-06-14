var javaCall = function(name, values = [], callback = ()=>{}, onFailure = ()=>{}){
    return window.cefQuery({
        request: name+':'+values,
        onSuccess: callback,
        onFailure: onFailure
    });
}


var javaBridger = {
    set : {},
    get: (target, key) => {
        return function(callback, ...value){
            javaCall(key, value, callback, (error_code, error_message)=>{
                alert('Failed to execute java method: '+key+' \n'+error_code+' '+error_message);
            })
        }
    }
}
var java = new Proxy(new Object(), javaBridger);

