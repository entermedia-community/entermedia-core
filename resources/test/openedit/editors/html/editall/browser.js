
/*
  This is an abstract browser class defining functions that work differently in
  different browsers
*/
function Browser( inParentDoc, inDocument, inKeepHead )
{
	this.maintainHeader = inKeepHead;
	this.editorDoc = inParentDoc;
    this.doc = inDocument;
    this.usingIE = navigator.appVersion.indexOf("MSIE") > 0? true : false;
    if ( this.usingIE )
    {
		this.toSourceView = IEToSourceView;
		//this.toSourceView = MozillaToSourceView;
		this.toWysiwygView = IEToWywsiwygView;
		this.grabFromSource = GrabFromSource;
		this.grabFromWysiwyg = GrabFromWysiwyg;
	}
	else
	{
		this.toSourceView = MozillaToSourceView;
		this.toWysiwygView = MozillaToWywsiwygView;
		this.grabFromSource = MozGrabFromSource;
		this.grabFromWysiwyg = MozGrabFromWysiwyg;
	}

}
function GrabFromSource()
{
	var iText;

	iText = this.doc.body.innerText;
	return iText;
}
function GrabFromWysiwyg()
{
	var iHTML;
	if ( this.maintainHeader )
	{
		iHTML = this.doc.documentElement.outerHTML;
	}
	else
	{
		iHTML = this.doc.body.innerHTML;	
	}
	//alert( iHTML );
	return iHTML;
}
function MozGrabFromSource()
{
	var html = this.doc.body.ownerDocument.createRange();
	
	html.selectNodeContents(this.doc.body);
	
	var code = html.toString();
	//alert( code );
	return code;	
}
function MozGrabFromWysiwyg()
{
	if ( this.maintainHeader )
	{
		return "<html>\n" + toHTML( this.doc.documentElement ) + "</html>";
	}
	else
	{
		return toHTML( this.doc.body );
	}
}

function toHTML( parent )
{
	var iHTML = "";
	if ( parent.hasChildNodes() )
	{
		var nodeList = parent.childNodes;
		for( var i=0;i<  nodeList.length; i++ )
		{
			var childNode = nodeList.item(i);
			var outp = new XMLSerializer().serializeToString( childNode );
			iHTML += outp;			
		}
	}
	return iHTML;
}

/*
	IE Browser
*/

function IEToSourceView(html)
{
	var raw = html;
	var root;
		root = this.doc.body;
	//look for HTML stuff we can color
	root.innerText = raw;
	
	var code = root.innerHTML;
	code = highlight( code );	
	//root.innerHTML = code;
	document.getElementById("source").value = code;
	load( false );
}

function IEToWywsiwygView(html)
{	
	//IE will make all links relative unless we write into a document
	document.getElementById("source").value = html;
	load(this.maintainHeader);
}

/*
	Mozilla browser
*/

function MozillaToSourceView(html)
{
	var raw = html;
	//alert( raw );
	//convert to escaped HTML
	var htmlnode = document.createTextNode( raw );
	
	var root;
	//if ( this.maintainHeader )
	//{
	//	root = this.doc.documentElement;
	//}
	//else
	//{
		root = this.doc.body;
	//}
	root.innerHTML = "";
	//clear( root );
	root.appendChild(  htmlnode );
	
	//convert back to visual code
	var code = toHTML( root );
	//var code = "hey";
	
	code = highlight( code );	

	//code = "<p>\n" + code + "<p>";
	
	//root.innerHTML = code;
	document.getElementById("source").value = code;
	load( false );

}

function clear( parent )
{
	var nodeList = parent.childNodes;
	for( var i=0;i<  nodeList.length; i++ )
	{
		var childNode = nodeList.item(i);
		parent.removeChild( childNode );
	}
}
function MozillaToWywsiwygView(html)
{
	document.getElementById("source").value = html;
	//this.doc.body.innerHTML = raw;
	
	//There is some bug with the toolbars with Mozilla 1.3
	load(this.maintainHeader);
}

function ClipBoard() 
{
	holdtext.innerText = copytext.innerText;
	Copied = holdtext.createTextRange();
	Copied.execCommand("Copy");
}
