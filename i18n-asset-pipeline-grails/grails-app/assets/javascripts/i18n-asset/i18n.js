;(function(factory){
    factory(window.$L);
}(function(messages){
    if(!messages){
        console.log('No messagebundle loaded, always returning message code as fallback.');
    }
    
    var emptyArray = [];
    var i18n = function(){}
    i18n.prototype.md = function(code,defaultMessage){
        return this.m(code,emptyArray,defaultMessage);
    },
    i18n.prototype.m = function(code,args,defaultMessage){
        var message = this.getMessage(code);
        if(!message){
            message = defaultMessage || this.getDefaultMessage(code);
        }
        if(args && args.length>0){
            message = this.format(message,args);
        }
        return message;
    },
    i18n.prototype.format = function(source,params){
        if ( params === undefined ) {
            return source;
        }
        for(var i=0;i<params.length;i++){
            source = source.replace( new RegExp( "\\{" + i + "\\}", "g" ), function() {
                return params[i];
            });
        }
        return source;
    },
    i18n.prototype.getDefaultMessage = function(code){
        return '[' + code + ']';
    },
    i18n.prototype.getMessage = function(code){
        return messages? messages(code): null;
    }
    window.$i18n = new i18n();    
}));