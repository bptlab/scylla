var javaCall = function(name, values = [], callback = ()=>{}, onFailure = ()=>{}){
    var stringified = values
        .map(JSON.stringify)
        .map(each => each.replace(/\\/g, '\\\\'));

    if(!window.cefQuery)return;
    
    return window.cefQuery({
        request: name+'\\'+stringified.join('\\'),
        onSuccess: callback,
        onFailure: onFailure
    });
}


var javaBridger = {
    set : {},
    get: (target, key) => {
        return function(callback, ...values){
            if(!(callback instanceof Function) && callback){
                values.unshift(callback);
                callback = ()=>{};
            }
            javaCall(key, values, callback, (error_code, error_message)=>{
                alert('Failed to execute java method: '+key+' \n'+error_code+' '+error_message);
            })
        }
    }
}
var backend = new Proxy(new Object(), javaBridger);

