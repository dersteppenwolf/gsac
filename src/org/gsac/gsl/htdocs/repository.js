
var root = "${urlroot}";
var urlroot = "${urlroot}";

var icon_close = "${urlroot}/htdocs/icons/close.gif";
var icon_rightarrow = "${urlroot}/htdocs/icons/grayrightarrow.gif";
var icon_downdart ="${urlroot}/htdocs/icons/downdart.gif";
var icon_rightdart ="${urlroot}/htdocs/icons/rightdart.gif";
var icon_progress = "${urlroot}/htdocs/icons/progress.gif";
var icon_information = "${urlroot}/htdocs/icons/information.png";
var icon_blank = "${urlroot}/htdocs/icons/blank.gif";



function Util () {
    this.loadXML = function (url, callback,arg) {
        var req = false;
        if(window.XMLHttpRequest) {
            try {
                req = new XMLHttpRequest();
            } catch(e) {
                req = false;
            }
        } else if(window.ActiveXObject)  {
            try {
                req = new ActiveXObject("Msxml2.XMLHTTP");
            } catch(e) {
                try {
                    req = new ActiveXObject("Microsoft.XMLHTTP");
                } catch(e) {
                    req = false;
                }
            }
        }
        if(req) {
            req.onreadystatechange = function () { 
                if (req.readyState == 4 && req.status == 200)   {
                    callback(req,arg); 
                }
            };
            req.open("GET", url, true);
            req.send("");
        }
    }



    this.getUrlArg  = function( name, dflt ) {
        name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");
        var regexS = "[\\?&]"+name+"=([^&#]*)";
        var regex = new RegExp( regexS );
        var results = regex.exec( window.location.href );
        if( results == null || results=="" )
            return dflt;
        else
            return results[1];
    }

    this.setCursor = function(c) {
        var cursor = document.cursor;
        if(!cursor && document.getElementById) {
            cursor =  document.getElementById('cursor');
        }
        if(!cursor) {
            document.body.style.cursor = c;
        }
    }


    this.getDomObject = function(name) {
        obj = new DomObject(name);
        if(obj.obj) return obj;
        return null;
    }




    this.getKeyChar = function(event) {
        event = util.getEvent(event);
        if(event.keyCode) {
            return String.fromCharCode(event.keyCode);
        }
        if(event.which)  {
            return String.fromCharCode(event.which);
        }
        return '';
    }


    this.print = function (s, clear) {
        var obj = util.getDomObject("output");
        if(!obj) {
            alert('could not find print output\n'+  s);
            return;
        }
        if(clear) {
	     obj.obj.innerHTML  ="";
        }
        obj.obj.innerHTML  =obj.obj.innerHTML+"<br>" +s;
    }



    this.getEvent = function (event) {
        if(event) return event;
        return window.event;
    }


    this.getEventX =    function (event) {
        if (event.pageX) {
            return  event.pageX;
        }
        return  event.clientX + document.body.scrollLeft
        + document.documentElement.scrollLeft;
    }

    this.getEventY =function (event) {
        if (event.pageY) {
            return  event.pageY;
        }
        return  event.clientY + document.body.scrollTop
        + document.documentElement.scrollTop;

    }

    this.getTop = function (obj) {
        if(!obj) return 0;
        return obj.offsetTop+this.getTop(obj.offsetParent);
    }


    this.getBottom = function (obj) {
        if(!obj) return 0;
        return this.getTop(obj) + obj.offsetHeight;
    }


    this.setPosition = function(obj,x,y) {
        obj.style.top = y;
        obj.style.left = x;
    }

    this.getLeft =  function(obj) {
        if(!obj) return 0;
        return obj.offsetLeft+this.getLeft(obj.offsetParent);
    }
    this.getRight =  function(obj) {
        if(!obj) return 0;
        return obj.offsetRight+this.getRight(obj.offsetParent);
    }

    this.getStyle = function(obj) {
        if(obj.style) return obj.style;
        if (document.layers)  { 		
            return   document.layers[obj.name];
        }        
        return null;
    }

}

util = new Util();

var blockCnt=0;


function DomObject(name) {
    this.obj = null;
    // DOM level 1 browsers: IE 5+, NN 6+
    if (document.getElementById)	{    	
        this.obj = document.getElementById(name);
        if(this.obj)  {
            this.style = this.obj.style;
        }
    }
    // IE 4
    else if (document.all)	{  			
        this.obj = document.all[name];
        if(this.obj) 
            this.style = this.obj.style;
    }
    // NN 4
    else if (document.layers)  { 		
        this.obj = document.layers[name];
        this.style = document.layers[name];
    }
   if(this.obj) {
      this.id = this.obj.id;
      if(!this.id) {
	this.id = "obj"+ (blockCnt++);
      }
   } 
   
}


function noop() {
}

var objectToHide;

function hideEntryPopup() {
    hideObject(util.getDomObject("tooltipdiv"));
}


function hidePopupObject() {
    if(objectToHide!=popupObject) {
	return;
    }
    if(popupObject) {
        hideObject(popupObject);
        popupObject = null;
    }
}



function setImage(id,url) {
    img = util.getDomObject(id);
    if(img) {
        img.obj.src  = url;
    }
}



function Tooltip () {
    var STATE_INIT = 0;
    var STATE_LINK = 1;
    var STATE_TIP = 2;
    var lastMove = 0;
    var state = STATE_INIT;
    var currentID;
    var hideDelay = 1000;
    var showDelay = 1000;

    this.debug = function(msg) {
        util.print(msg);
    }
    this.keyPressed = function (event) {
        alert("key")
        tooltip.doHide();
        return;
        if(state==STATE_INIT) return;
        c =util.getKeyChar(event);
        if(c == '\r' && state == STATE_TIP) {
            tooltip.doHide();
        }
    }

    this.onMouseMove = function (event,id,linkId) {
        lastMove++;
        if(state!=STATE_INIT) return;
        event = util.getEvent(event);
        setTimeout("tooltip.showLink(" + lastMove+"," +util.getEventX(event)+","+ util.getEventY(event) +"," + "'" + id +"'"+  ",'" + linkId +"')", showDelay);
    }

    this.onMouseOut = function (event,id,linkId) {
        lastMove++;
        if(state !=STATE_LINK) return;
        setTimeout("tooltip.checkHide(" + lastMove+ ")", hideDelay);
    }


    this.onMouseOver = function(event,id,linkId) {
        event = util.getEvent(event);

        if(state ==STATE_LINK && currentID && id!=currentID) {
            this.doHide();
            currentID = null;

        }
        lastMove++;
        if(state!=STATE_INIT) return;
        setTimeout("tooltip.showLink(" + lastMove+"," +util.getEventX(event)+","+ util.getEventY(event) +"," + "'" + id +"'"+",'" + linkId +"')", showDelay);
    }


    this.checkHide  = function(timestamp) {
	if(timestamp<lastMove) return;
        this.doHide();
    }

    this.doHide  = function() {
        currentID = "";
        if(state !=STATE_LINK && state!=STATE_TIP)
            return;
        state = STATE_INIT;
        hideObject(util.getDomObject("tooltipdiv"));
    }


    this.getX = function(link,eventX) {
        if(link && link.obj.offsetLeft && link.obj.offsetWidth) {
            return eventX-15;
            return util.getLeft(link.obj);
        } else {
            return eventX+20;
        }
    }

    this.getY = function(link,eventY) {
        if(link && link.obj.offsetLeft && link.obj.offsetWidth) {
            return  link.obj.offsetHeight+util.getTop(link.obj)-2;
        } else {
            return eventY;
        }
    }


    this.onClick  = function(event,id) {
	state = STATE_TIP;
        var link = util.getDomObject(id);
        x = this.getX(link);
        y = this.getY(link);
        var obj = util.getDomObject("tooltipdiv");
        if(!obj) return;
        //        util.setPosition(obj, x,y);
        url = "${urlroot}/entry/show?entryid=" + id +"&output=metadataxml";
	util.loadXML( url, handleTooltip,obj);
    }


    this.showLink = function(moveId,x,y,id,linkId) {
        if(lastMove!=moveId) return;
	if(state!=STATE_INIT) return;
        currentID = id;
        var obj = util.getDomObject("tooltipdiv");
        if(!obj) return;
        state = STATE_LINK;
        var link = util.getDomObject(linkId);
        x = this.getX(link,x);
        y = this.getY(link,y);
        util.setPosition(obj, x,y);
        var imgEvents = " onMouseOver=\"tooltip.onMouseOver(event,'" + id +"')\" " +
        " onMouseOut=\"tooltip.onMouseOut(event,'" + id +"')\" " +
        " onMouseMove=\"tooltip.onMouseMove(event,'" + id +"')\" " +
        " onClick=\"tooltip.onClick(event,'" + id +"')\" ";
	obj.obj.innerHTML = "<div class=tooltip-link-inner><img title=\"Show tooltip\" alt=\"Show tooltip\" " + imgEvents +" src="+icon_information +"></div>";
        showObject(obj);
    }

    function handleTooltip(request, obj) {
        var xmlDoc=request.responseXML.documentElement;
        text = getChildText(xmlDoc);
        obj.obj.innerHTML = "<div class=tooltip-inner><div id=\"tooltipwrapper\" ><table cellspacing=0 cellpadding=0><tr valign=top><img width=\"16\" onmousedown=\"tooltip.doHide();\" id=\"tooltipclose\"  src=" + icon_close +"></td><td>&nbsp;</td><td>" + text+"</table></div></div>";
        checkTabs(text);
        showObject(obj);
    }

}

tooltip = new Tooltip();

document.onkeypress = tooltip.keyPressed;
var keyEvent;


function handleKeyPress(event) {
    keyEvent = event;
    c =util.getKeyChar(event);
    div = util.getDomObject("tooltipdiv");
    if(!div) return;
    hideObject(div);
}

document.onkeypress = handleKeyPress;


function checkTabs(html) {
    while(1) {
        var re = new RegExp("id=\"(tabId[^\"]+)\"");
        var m = re.exec(html);
        if(!m) {
            break;
        }
        var s =   m[1];
        if(s.indexOf("-")<0) {
            jQuery(function(){
                    jQuery('#'+ s).tabs();
                });
        }
        var idx = html.indexOf("id=\"tabId");
        if(idx<0) {
            break;
        }
        html = html.substring(idx+20);
    }
}


function indexOf(array,object) {
    for (i = 0; i <= array.length; i++) {
        if(array[i] == object) return i;
    }
    return -1;
}



function toggleBlockVisibility(id, imgid, showimg, hideimg) {
    var img = util.getDomObject(imgid);
    if(toggleVisibility(id,'block')) {
        if(img) img.obj.src = showimg;
    } else {
        if(img) img.obj.src = hideimg;
    }
}


function toggleInlineVisibility(id, imgid, showimg, hideimg) {
    var img = util.getDomObject(imgid);
    if(toggleVisibility(id,'inline')) {
        if(img) img.obj.src = showimg;
    } else {
        if(img) img.obj.src = hideimg;
    }
}






function  getChildText(node) {
    var text = '';
    for(childIdx=0;childIdx<node.childNodes.length;childIdx++) {
        text = text  + node.childNodes[childIdx].nodeValue;
    }
    return text;
	
}


function toggleVisibility(id,style) {
    if(!style) style='block';
    var obj = util.getDomObject(id);
    return toggleVisibilityOnObject(obj,style);
}


function hide(id) {
    hideElementById(id);
}

function hideElementById(id) {
    hideObject(util.getDomObject(id));
}

function setFormValue(id, value) {
    var obj = util.getDomObject(id);
    obj.obj.value   = value;
}


function setHtml(id, html) {
    var obj = util.getDomObject(id);
    obj.obj.innerHTML = html;
}

function showAjaxPopup(event,srcId,url) {
    util.loadXML(url, handleAjaxPopup,srcId);
}

function handleAjaxPopup(request, srcId) {
    var xmlDoc=request.responseXML.documentElement;
    text = getChildText(xmlDoc);
    var srcObj = util.getDomObject(srcId);
    var obj = util.getDomObject("tooltipdiv");
    obj.obj.innerHTML = "<div class=tooltip-inner><div id=\"tooltipwrapper\" ><table><tr valign=top><img width=\"16\" onmousedown=\"tooltip.doHide();\" id=\"tooltipclose\"  src=" + icon_close +"></td><td>&nbsp;</td><td>" + text+"</table></div></div>";
    checkTabs(text);
    showObject(obj);
}


function showPopup(event, srcId, popupId, alignLeft) {
    hidePopupObject();
    var popup = util.getDomObject(popupId);
    var srcObj = util.getDomObject(srcId);
    if(!popup || !srcObj) return;
    event = util.getEvent(event);
    x = util.getEventX(event);
    y = util.getEventY(event);
    if(srcObj.obj.offsetLeft && srcObj.obj.offsetWidth) {
        x = util.getLeft(srcObj.obj);
        y = srcObj.obj.offsetHeight+util.getTop(srcObj.obj) + 2;
    } 

    if(alignLeft) {
        x = util.getLeft(srcObj.obj);
        y = srcObj.obj.offsetHeight+util.getTop(srcObj.obj) + 2;
    } else {
        x+=2;
        x+=3;
    }

    popupObject = popup;
    showObject(popup);
    util.setPosition(popup, x,y);
}




function showStickyPopup(event, srcId, popupId, alignLeft) {
    var popup = util.getDomObject(popupId);
    var srcObj = util.getDomObject(srcId);
    if(!popup || !srcObj) return;
    event = util.getEvent(event);
    x = util.getEventX(event);
    y = util.getEventY(event);
    if(srcObj.obj.offsetLeft && srcObj.obj.offsetWidth) {
        x = util.getLeft(srcObj.obj);
        y = srcObj.obj.offsetHeight+util.getTop(srcObj.obj) + 2;
    } 

    if(alignLeft) {
        x = util.getLeft(srcObj.obj);
        y = srcObj.obj.offsetHeight+util.getTop(srcObj.obj) + 2;
    } else {
        x+=2;
        x+=3;
    }

    showObject(popup);
    util.setPosition(popup, x,y);
}


function show(id) {
    showObject(util.getDomObject(id));
}

function hideObject(obj) {
    if(!obj) {
        return 0;
    }

    var style = util.getStyle(obj);
    if(!style) {
        return 0;
    }
    style.visibility = "hidden";
    style.display = "none";
    return 1;
}


function hideMore(base) {
    var link = util.getDomObject("morelink_" + base);
    var div = util.getDomObject("morediv_" + base);
    hideObject(div);
    showObject(link);
}


function showMore(base) {
    var link = util.getDomObject("morelink_" + base);
    var div = util.getDomObject("morediv_" + base);
    hideObject(link);
    showObject(div);
}




function showObject(obj, display) {
    if(!obj) return 0;
    if(!display) display = "block";

    var style = util.getStyle(obj);
    if(!style) {
        alert("no style");
        return 0;
    }
    style.visibility = "visible";
    style.display = display;
    return 1;
}



function toggleVisibilityOnObject(obj, display) {
    if(!obj) return 0;
    if(obj.style.visibility == "hidden") {
        obj.style.visibility = "visible";
        obj.style.display = display;
        return 1;
    } else {
        obj.style.visibility = "hidden";
        obj.style.display = "none";
        return 0;
    }
}






function findFormElement(form, id) {
    var form = document.forms[form];
    if(form) {
        if(form[id]) return form[id];
    }
    obj = util.getDomObject(id);
    if(obj) return obj.obj;
    return null;
}


function selectDate(div,field,id,fmt) {
    var cal = new CalendarPopup(div);
    cal.showYearNavigation();
    cal.select(field,id,fmt);
}





function entryRowOver(entryId) {
    var rowId = "row_" +entryId;
    var divId = "div_" + entryId;
    var imgId = "img_" + entryId;
    row = util.getDomObject(rowId);
    if(!row) {
        return;
    }
    //    row.style.backgroundColor = "#edf5ff";
    row.style.backgroundColor = "#ffffdd";
    var img = util.getDomObject(imgId);
    if(img) {
        img.obj.src =  icon_downdart;
    }
}





function entryRowOut(entryId) {
    var rowId = "row_" +entryId;
    var divId = "div_" + entryId;
    var imgId = "img_" + entryId;
    row = util.getDomObject(rowId);
    if(!row) return;
    row.style.backgroundColor = "#fff";
    var img = util.getDomObject(imgId);
    if(img) {
        img.obj.src =  icon_blank;
    }
}



function entryRowClick(event, entryId, url) {
    var rowId = "row_" +entryId;
    var divId = "div_" + entryId;
    var imgId = "img_" + entryId;
    row = util.getDomObject(divId);
    if(!row) {
        return;
    }
    div = util.getDomObject("tooltipdiv");
    if(!div) {
        return;
    }
    var img = util.getDomObject(imgId);
    if(img) {
        img.obj.src =  icon_progress;
    }
    util.loadXML( url, entryHandleXml,entryId);

}







function entryHandleXml(request,entryId) {
    var rowId = "row_" +entryId;
    var divId = "div_" + entryId;
    var imgId = "img_" + entryId;
    var img = util.getDomObject(imgId);
    if(img) {
        img.obj.src =  icon_blank;
    }

    row = util.getDomObject(divId);
    if(!row) return;
    div = util.getDomObject("tooltipdiv");
    if(!div) return;
    var xmlDoc=request.responseXML.documentElement;
    text = getChildText(xmlDoc);
    div.style["left"]  =util.getLeft(row.obj)+"px";
    var bottom = util.getBottom(row.obj);

    div.style["top"]  = bottom+"px";
    div.obj.innerHTML = "<div class=tooltip-inner><div id=\"tooltipwrapper\" ><table><tr valign=top><img width=\"16\" onmousedown=\"hideEntryPopup();\" id=\"tooltipclose\"  src=" + icon_close +"></td><td>" + text+"</table></div></div>";
    checkTabs(text);
    showObject(div);
}





var googleEarthClickCnt =0;

function googleEarthResourceClicked(googleEarth, id, detailsUrl) {
    googleEarthClickCnt++;
    var myClick = googleEarthClickCnt;
    placemark =googleEarth.placemarks[id];
    if(!placemark) {
        return;
    }
    googleEarth.setLocation(placemark.lat,placemark.lon);
    googleEarth.placemarkClick(id);

    var cbx = util.getDomObject("googleearth.showdetails");
    if(cbx) {
        if(!cbx.obj.checked) return;
    } 
    var callback = function(request) {
        if(myClick != googleEarthClickCnt) return;
        var balloon = googleEarth.googleEarth.createHtmlStringBalloon('');
        balloon.setFeature(placemark.placemark);
        var xmlDoc=request.responseXML.documentElement;
        text = getChildText(xmlDoc);
        balloon.setContentString(text);
        googleEarth.googleEarth.setBalloon(balloon);
    }
    util.loadXML(detailsUrl, callback,"");

}