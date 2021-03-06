define(["dojo/_base/declare",
        "dijit/_Widget",
        "dojo/topic",
        "dojo/dom-attr",
        "dojo/_base/lang"
        ], function(declare,_Widget,topic,domAttr, lang){
	
	return declare("dojoui.Bind",_Widget, {
	  variable:"",
	  defaultValue:"",
	  _propertyArray:"",
	  _topic:"",
	  _topicQue:"",
	
	
	  postCreate:function() {
	    this._propertyArray = this.variable.split(".");
	    this._topic = this._propertyArray [0];
	    _topicQue = topic.subscribe(this._topic, lang.hitch(this, this.displayValue));
	  },
	
	
	  displayValue:function(obj) {
	    if (!obj) {
	      console.log('no object')
	      return;
	    }
	    var val;
	    for (var i = 1; i < this._propertyArray.length; i++) {
	      val = obj[this._propertyArray[i]];
	    }
	    if (this.domNode) {
	      if (!val) {
	        domAttr.set(this.domNode, "innerHTML", this.defaultValue);
	      }
	      else {
	    	domAttr.set(this.domNode, "innerHTML", val);
	      }
	
	    }
	  }
	});


});	
