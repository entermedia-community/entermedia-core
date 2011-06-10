//this is javascript
var errs;
var editingcell;
var colours = new Array();
colours[1] = "#f0f0ff";
colours[2] = "#e0e0ff";
colours[3] = "#d0d0ff";
colours[4] = "#c0c0ff";
colours[5] = "#b0b0ff";
colours[6] = "#a0a0ff";
colours[7] = "#9090ff";
colours[8] = "#8080ff";
colours[9] = "#7070ff";

 
function err(str) { errs += str; }

function processXML() 
{
 var src = document.getElementById('MyTab').value;//document.editform.elements['in'].value;
 errs = "";
 
 var xd = new XMLDoc(src, null, err); 
 
 if(xd.hasErrors) writeErrorReport();  
 else displayDocument(xd); 
}

function newWin()
{
 var w = window.open("blank.html","output");
 return w.document;
 
}

function writeErrorReport()
{
 var disp = frames['floatframe'].document;
 disp.open();
 disp.write("<h1>Error report</h1>");
 disp.write("<pre>" + errs + "</pre>");
 disp.close();
}

function displayDocument(d)
{
 if(d.docNode==null) return;
 var disp = frames['floatframe'].document; //newWin()
 
 displayElement(d.docNode, disp,0);
 
 disp.close(); 
}


function displayElement(el, disp,depth)
{
 if(el==null) return; 
 
 if(!(el.nodeType=='ELEMENT')) return;

 // title

 disp.write('<font face="Arial, Helvetica" size="+2"><b>' + el.tagName + "</b></font>");
 
 // attributes
 
 var atts = "";
 
 for(var a in el.attributes)
  atts += "<tr><td><font face='Arial, Helvetica'><b>" + a + ": </b></font></td>" + "<td><font face='Arial, Helvetica'>" + el.attributes[a] + "</font></td></tr>";
  
 if(atts!="")
  disp.write('<table>' + atts + '</table>');
 else
  disp.write('<br />');
 
 
 // children
  
 if(el.children!=null)
 {
  var els = el.children;
 
  for(var e=0; e < els.length; e++)
  {
  
   var ch = els[e];
   if(ch.nodeType=='TEXT')
   {
    var cont = trim(ch.content,true,true);
    if(cont.length!=0)
     disp.write('<font face="Arial, Helvetica"><i>' + ch.getText() + '</i></font>');
   }
   else if (ch.nodeType=='CDATA')
   {
   
   
    disp.write("<pre>");
   
    var output = "";
   
    for(var p=0; p<ch.content.length; p++)
    {
     var cp = Ch.content.charAt(p);
     output += (cp=='<![CDATA[<]]>' ? '&lt;' : cp);
    }
    disp.write(output + "</pre>");
   }
   else
   {
    disp.write('<table width="100%" cellspacing="10"><tr><td bgcolor=" + colours[depth+1] + ">');
    displayElement(ch, disp,depth+1);
    disp.write("</td></tr></table>");
   }
  }
  
 }  
}

function wipeText() { document.edit.elements['in'].value =""; }


	 
		
		var stdBrowser = (document.getElementById) ? true : false

		function popToggle(evt,currElem,intd) {
			//this is getting called too much
			//alert( document.getElementById(intd.id).item(0) );
			alert( currElem );
			
			//var popUpWin = document.getElementById(currElem).style;
			//alert( popUpWin );
			var popUpWin = (stdBrowser) ? document.getElementById(currElem).style : eval("document." + currElem)
			if (popUpWin.visibility == "visible" || popUpWin.visibility == "show")
				popDown(currElem);
			else
				popUp(evt, currElem,intd);
		}
				
		function popUp(evt,currElem,intd) {

			var popnum;	
			<!--
			for (popnum=1;popnum<1;popnum++) {
				var elem = 'popUp' + popnum;
				var test = (stdBrowser) ? document.getElementById(elem).style : eval("document." + elem);
				if (test.visibility=="visible" || test.visibility=="show")
					popDown(elem);
			}
			-->

			
			var popUpWin = (stdBrowser) ? document.getElementById(currElem).style : eval("document." + currElem);
			
			myClickBounds = getBounds( intd );
			//alert( document.all );
			/*
			if (document.all) {
				popUpWin.pixelTop = parseInt(evt.y)-15;
				popUpWin.pixelLeft = intd.offsetLeft ;  //Math.max(2,parseInt(evt.x)+10);
			}
			
			else {
			*/
				if (stdBrowser) {
					//popUpWin.top = parseInt(evt.pageY)-15 + "px";
					//popUpWin.left = Math.max(2,parseInt(evt.pageX)+10) + "px";
					popUpWin.top = myClickBounds.y + "px";
					popUpWin.left = myClickBounds.x + "px";
				}
				else {
					popUpWin.top = parseInt(evt.pageY)-15;
					popUpWin.left = Math.max(2,parseInt(evt.pageX)+10);
				}
			//}
			//We should just create a SPAN using the DOM API
			
			document.getElementById('SiteText').value = intd.innerHTML;
			editingcell = intd;
			popUpWin.visibility = "visible";
			document.getElementById('SiteText').focus();
		}

		function popDown(currElem) {
			var popUpWin = (stdBrowser) ? document.getElementById(currElem).style : eval("document." + currElem);

			if (document.layers) { //if Netscape:
				popUpWin.visibility = "hide";
			}
			else {
				popUpWin.visibility = "hidden";
			}
			editingcell.innerHTML = document.getElementById('SiteText').value;
		}
		
		function getBounds(el){
			for (var lx=0,ly=0;el!=null;
				lx+=el.offsetLeft,ly+=el.offsetTop,el=el.offsetParent);
			return {x:lx,y:ly}
		}
