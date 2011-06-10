var whitespace = "\n\r\t ";
var quotes = "\"'";
var usingIE = navigator.appVersion.indexOf("MSIE") > 0? true : false;


// ================================================================================
// XMLNode() is a constructor for a node of XML (text, comment, cdata, tag, etc.)
//     src:   contains the text for the tag or text entity
//   isTag:   identifies whether the text has been found within < > characters.
//     doc:   contains a reference to the XMLDoc object describing the document.
// ================================================================================
function XMLNode(node, doc)
{
	this.domNode = node;  // the DOM node which corresponds to this one
	this.doc = doc;       // a reference to the XMLDoc that contains this node
	this.doc.gidcount++;
	this.gid = this.doc.gidcount;
	this.doc.allNodes[ this.gid ] = this;
	this.modified = false;
	this.children = new Array();
	this.parent;

	// configure the methods
	this.setModified = _XMLNode_setModified;
	this.isModified = _XMLNode_isModified;
	this.getName = _XMLNode_getName;
	this.getNodeType = _XMLNode_getNodeType;
	this.getText = _XMLNode_getText;
	this.getParent = _XMLNode_getParent;
	this.getAttribute = _XMLNode_getAttribute;
	this.setAttribute = _XMLNode_setAttribute;
	this.getElements = _XMLNode_getElements;
	this.getDepth = _XMLNode_getDepth;
	this.clear = _XMLNode_clear;
	this.copy = _XMLNode_copy;
	this.appendChild = _XMLNode_appendChild;
	this.prependChild = _XMLNode_prependChild;
	this.deleteFromParent = _XMLNode_deleteFromParent;
	this.indexOf = _XMLNode_indexOf;
	this.moveUp = _XMLNode_moveUp;
	this.moveDown = _XMLNode_moveDown;
	this.moveLeft = _XMLNode_moveLeft;
	this.moveRight = _XMLNode_moveRight;
	this.parse = _XMLNode_parse; // private method
	this.getAttributes = _XMLNode_getAttributes;
	this.asXML = _XMLNode_asXML;
	this.setText = _XMLNode_setText;
	this.setXml = _XMLNode_setXml;
	this.addXmlAsChild = _XMLNode_addXmlAsChild;
	this.reloadDom = _XMLNode_reloadDom; // private method
 
	this.parse();
}
function _XMLNode_isModified()
{	
	if ( this.modified )
	{
		return true;
	}
	if ( this.children != null )
	{
		for ( var i = 0; i < this.children.length; i++ )
		{
			if ( this.children[i].isModified() )
			{
				return true;
			}
		}
	}
	return false;	
}
function _XMLNode_setModified( inMod )
{
	this.modified = inMod;
}
/**
 * Set the content of this node to the given fragment of XML text.  Any elements
 * in the text will be converted into XMLNodes and added to the DOM proper.
 */
function _XMLNode_setText( newmixedxml )
{
	var oldtext = this.getText();
	try
	{
		// Make a temp node so that we can parse it and grab any children we find.
		this.setModified( true );
		var tmpdom = buildDom( "<tmp2>" + newmixedxml + "</tmp2>");
		
		//var basedoc = this.domNode.ownerDocument;
		var mixedNodes = tmpdom.firstChild.childNodes;

		var oktoClear = true;
		if ( newmixedxml.length == 0 && oldtext != null && oldtext.length > 0 )
		{
			oktoClear = confirm("Are you sure you want to remove the element?");
		}
		else if ( newmixedxml.length > 0 && mixedNodes.length == 0 )
		{
			oktoClear = confirm("Element text could not be parsed, ok to remove the element?");
		}
		if ( oktoClear )
		{
			this.clear();	
			
			// Import and add each of the children back into the node.  We must do these
			// individually because importNode() sets the parent to null, thus making
			// them append-able to a different parent.  If we try to append a node with
			// an existing parent, the DOM chokes, because the node we're trying to add
			// is now in two separate DOM trees (so who's its parent?).
			
			for ( var i = 0; i < mixedNodes.length; i++ )
			{
				var newNode = mixedNodes[i].cloneNode( true );
				this.domNode.appendChild( newNode );
			}
			this.parse();
		}
	}
	catch ( error1 )
	{
		var okUndo = confirm( "Error editing text " + error1 + " would you like to undo change?");
		if ( okUndo )
		{
			this.setText( oldtext );
		}
	}
}

/**
 * Set this node to the given fragment of XML text.  Any elements
 * in the text will be converted into XMLNodes and added to the DOM proper.
 */
function _XMLNode_setXml( newmixedxml )
{
	// Make a temp node so that we can parse it and grab any children we find.
	this.setModified( true );
	var tmpdom = buildDom( newmixedxml);
	
	var basedoc = this.domNode.ownerDocument;
	this.clear();	
	
	var newNode = tmpdom.firstChild.cloneNode( true );
	this.domNode.parentNode.replaceChild( newNode, this.domNode );
	this.domNode = newNode;
	this.parse();
}

function _XMLNode_addXmlAsChild( newmixedxml , belowNode)
{
	this.setModified( true );
	var tmpdom = buildDom( newmixedxml );
	var newNode = tmpdom.firstChild.cloneNode( true ); //removed the parent
	
	var hasChild;
	for( var i=0;i< this.domNode.childNodes.length;i++)
	{
		if ( this.domNode.childNodes[i] == belowNode.domNode )
		{
			hasChild=true;
			break;
		}
	}
	if ( hasChild )
	{
		this.domNode.insertBefore( newNode, belowNode.domNode );
	}
	else
	{
		this.domNode.appendChild( newNode );
	}
	this.children= new Array();
	this.parse(); //will create all the children again
	return this.children[ this.children.length ]; //last one	
}

/**
 * Serialize this node as XML and retrieve the result.
 *
 * @return A string of XML
 */
function _XMLNode_asXML()
{
	return this.domNode.xml;
}

/**
 * Get the attributes of this node as an array of name/value pairs.
 */
function _XMLNode_getAttributes()
{
	var attribs = new Array();
	if ( this.domNode.attributes != null )
	{
		for( var i = 0; i < this.domNode.attributes.length; i++ ) 
		{ 
	 		var attr = this.domNode.attributes.item( i ); 
	 		attribs[ attr.name ] = attr.value;
		}
	}
	return attribs;	
}

function _XMLNode_getName()
{
	return this.domNode.nodeName;	
}

function _XMLNode_getParent()
{
	return this.parent;	
}

function _XMLNode_getNodeType()
{
	return this.domNode.nodeType;	
/*
ELEMENT_NODE                = 1
ATTRIBUTE_NODE              = 2
TEXT_NODE                   = 3
CDATA_SECTION_NODE          = 4
ENTITY_REFERENCE_NODE       = 5
ENTITY_NODE                 = 6
PROCESSING_INSTRUCTION_NODE = 7
COMMENT_NODE                = 8
DOCUMENT_NODE               = 9
DOCUMENT_TYPE_NODE          = 10
DOCUMENT_FRAGMENT_NODE      = 11
NOTATION_NODE               = 12
*/
}

/**
 * Resynchronize the children of this node with the DOM children.  This is the
 * opposite operation from reloadDom().
 */
function _XMLNode_parse()
{
	for( var i=0;i<this.domNode.childNodes.length;i++)
	{
		var domNode = this.domNode.childNodes[i];
		//alert( domNode.nodeType + " had text of " + this.domNode.childNodes[i].nodeName + " " + this.doc.TEXT_NODE);
		if ( domNode.nodeType == this.doc.TEXT_NODE && trim( domNode.nodeValue, true, true ) == "" )
		{
			//alert( "true" );
		}
		else
		{
			var node = new XMLNode( domNode,this.doc );
			node.parent = this;
			this.children[this.children.length] = node;
			node.modified = false;
		}
	}  
}

function _XMLNode_setAttribute(name, value)
{
	this.setModified( true );
	this.domNode.setAttribute(name,value);
}

function _XMLNode_deleteFromParent()
{
	this.setModified( true );
	if ( this.parent != null )
	{
		this.parent.modified = true;
		var idx = this.parent.indexOf( this );

		this.domNode.parentNode.removeChild( this.domNode ); //remove myself for now
		
		this.parent.children.splice( idx ,1 );				
		//this.parent.domNode.removeChild
	}
}

/**
 * Remove all children from this node.
 */
function _XMLNode_clear()
{
	this.setModified( true );
	this.children = new Array();
	for( ;this.domNode.childNodes.length > 0 ; )
	{
		this.domNode.removeChild( this.domNode.childNodes[0] );
	}
}

/**
 * Retrieve the index of the given node in this node's children, if any.
 *
 * @param node  The child to search for
 *
 * @return  The index of the node in the list of children, or -1 if it could not
 *          be found
 */
function _XMLNode_indexOf( node )
{
	for ( var i = 0; i < this.children.length; i++ )
	{
		if ( this.children[i] == node )
		{
			return i;
		}
	}
	return -1;
}

function _XMLNode_moveUp()
{
	if ( this.parent != null)
	{
		var index = this.parent.indexOf( this );

		var prevnode;
		var previndex;
		for ( var i = index -1; i >= 0; i-- )
		{
			if ( this.parent.children[i].getNodeType() == this.doc.ELEMENT_NODE )
			{
				prevnode = this.parent.children[i];
				previndex = i;
				break;
			}
		}
		if ( prevnode != null )
		{   
			//swap spots on these nodes
			this.parent.children.splice( previndex, 1, this );	
			this.parent.children.splice( index, 1, prevnode );	
			this.parent.reloadDom();
			return false;
		}
		else
		{
			//go up to the previous parent end of tree
			var sparent = this.parent.parent;
			if ( sparent != null && sparent.getNodeType() == this.doc.ELEMENT_NODE)
			{
				this.deleteFromParent();				
				//now I will be added back in
				sparent.prependChild( this ); //adds to top of parent and cleans up the dom
				//this.reloadDom();
			}
			return true;
		}
	}		
}

function _XMLNode_moveDown()
{
	if ( this.parent != null)
	{
		var index = this.parent.indexOf( this );

		var nextnode;
		var nextindex;
		for ( var i = index+1; i < this.parent.children.length; i++ )
		{
			if ( this.parent.children[i].getNodeType() == this.doc.ELEMENT_NODE )
			{
				nextnode = this.parent.children[i];
				nextindex = i;
				break;
			}
		}
		if ( nextnode != null )
		{
			this.parent.children.splice( nextindex, 1, this );	
			this.parent.children.splice( index, 1, nextnode );	
			this.parent.reloadDom();
			return false;
		}
		else
		{
			//go up to the parents next child
			var sparent = this.parent.parent;
			if ( sparent != null && sparent.getNodeType() == this.doc.ELEMENT_NODE)
			{				
				this.deleteFromParent();
				sparent.appendChild( this );
				//sparent.reloadDom();

				return true;
			}
		}
	}
}

function _XMLNode_moveRight()
{
	if ( this.parent != null)
	{
		var index = this.parent.indexOf( this );

		var prevnode;
		var previndex;
		for ( var i = index -1; i >= 0; i-- )
		{
			if ( this.parent.children[i].getNodeType() == this.doc.ELEMENT_NODE )
			{
				prevnode = this.parent.children[i];
				previndex = i;
				break;
			}
		}
		if ( prevnode != null )
		{
			//this.parent.children.splice( index, 1 );	
			this.deleteFromParent();
			prevnode.appendChild( this );
			//prevnode.reloadDom();
			return true;
		}
		else
		{
			//nada?
		}
	}	
}

function _XMLNode_moveLeft()
{
	if ( this.parent != null && this.parent.parent != null) //make sure we are not on the document root already
	{
		var index = this.parent.indexOf( this );
		var sparent = this.parent.parent;
		if ( sparent != null )
		{
			var preindex = sparent.indexOf( this.parent );
			sparent.children.splice( preindex+1,0,this ); //inserts me next to my parent
			this.parent.children.splice( index, 1 );	//takes me out
			this.parent = sparent;  
			return true;
		}
		else
		{
			//nada?
		}
	}	
}

/**
 * Append the given child to the end of this node's children.
 *
 * @param child  The child to append
 */
function _XMLNode_appendChild( child )
{
	this.setModified( true );
	// Make the new child the last child.
	this.children[this.children.length] = child;
	child.parent = this;
	this.domNode.appendChild( child.domNode );
}

/**
 * Prepend the given child to the beginning of this node's children.
 *
 * @param child  The child to prepend
 */
function _XMLNode_prependChild( child )
{
	this.setModified( true );
	// Make the new child the first child.
	this.children.unshift( child );	
	child.parent = this;
	this.domNode.insertBefore( child.domNode, this.domNode.firstChild );
}

/**
 * Resynchronize the DOM children of this node with the XMLNode children.
 */
function _XMLNode_reloadDom()
{
	for( ;this.domNode.childNodes.length > 0 ; )
	{
		this.domNode.removeChild( this.domNode.childNodes[0] );
	}
	
	for( var i=0;i<this.children.length;i++ )
	{
		this.domNode.appendChild( this.children[i].domNode );
	}
}

/**
 * Copy this XML node (deep copy).
 *
 * @return  The new copy
 */
function _XMLNode_copy()
{
	var domNode = this.domNode.cloneNode(true); //deep copy
	
  	var newnode =  new XMLNode(domNode,this.doc);
  	
	return newnode;	
}


// ================================================================================
// XMLNode.getText() - a method to get the text of a given node (recursively, if 
//                     it's an element.
// ================================================================================

function _XMLNode_getText() 
{ 
 if(this.getNodeType()== this.doc.ELEMENT_NODE)
 {
 	//should this be XML?
 /*	var str = "";
  	if(this.children==null)
   		return null;
  
  for(var i=0; i < this.children.length; i++)
  {
   var t = this.children[i].getText();
   str +=  (t == null ? "" : this.children[i].getText());
  }
  return str;
  */
  return this.domNode.xml;
 }
 
 else if((this.getNodeType()==this.doc.TEXT_NODE) || (this.getNodeType()==this.doc.CDATA_SECTION_NODE))
  return this.domNode.nodeValue;
 
 else return null;  // comment nodes get caught here
}



// ================================================================================
// XMLNode.getAttribute() - get the value of a named attribute from an element node
// ================================================================================

function _XMLNode_getAttribute(name) 
{ 
 if(this.domNode.attributes == null) return null;
 return this.domNode.getAttribute(name); 
}



// ================================================================================
// XMLNode.getElements(byName) - get an array of element children of a node, with 
//                               an optional filter by name
// ================================================================================

function _XMLNode_getElements(byName) 
{ 
 if(this.children==null)
  return null;
 var elements = new Array();
 for(var i=0; i<this.children.length; i++)
 {
  if((this.children[i].getNodeType()== this.doc.ELEMENT_NODE) &&
      ((byName==null) || (this.children[i].tagName == byName)))
   
   elements[elements.length] = this.children[i];
 }
 return elements;
}


/**
 * Calculate the depth of this XML node within the document.
 *
 * @return  The depth (1 if it is the root, 2 if it is a first-level child, ...)
 */
function _XMLNode_getDepth()
{
	var depth = 1;
	var node = this;
	while( node.getParent() != null )
	{
		node = node.getParent();
		depth++;
	}
	return depth;
}


// ================================================================================
// XMLDoc  - a constructor for an XML document
// xml - the string containing the XML document source
// ================================================================================

function XMLDoc(xml)
{
	this.allNodes = new Array();
	this.gidcount = 0;
	this.ELEMENT_NODE                = 1
	this.ATTRIBUTE_NODE              = 2
	this.TEXT_NODE                   = 3
	this.CDATA_SECTION_NODE          = 4
	this.ENTITY_REFERENCE_NODE       = 5
	this.ENTITY_NODE                 = 6
	this.PROCESSING_INSTRUCTION_NODE = 7
	this.COMMENT_NODE                = 8
	this.DOCUMENT_NODE               = 9
	this.DOCUMENT_TYPE_NODE          = 10
	this.DOCUMENT_FRAGMENT_NODE      = 11
	this.NOTATION_NODE               = 12


	// set up the methods for this object
	
	
	this.search = _XMLDoc_search;
	this.asXML = _XMLDoc_asXML;
	this.createElement = _XMLDoc_createElement;
	
	this.domDoc = buildDom( xml );	
	this.rootElement = new XMLNode( this.domDoc,this );
}

/**
 * Find the node with the given ID in this document.
 *
 * @param gid  The ID to search for
 *
 * @return  The XMLNode with the given ID, or null if it could not be found
 */
function _XMLDoc_search( gid )
{
	//get it out of the hash node
	return this.allNodes[ gid ];
}

/**
 * Retrieve the content of this document as XML.
 */
function _XMLDoc_asXML()
{
	var xml; 

	xml = this.rootElement.domNode.xml;
	//alert( xml );
	
	return xml;
}

/**
 * Create a brand-new element that can be inserted into the tree.
 *
 * @return  The new element
 */
function _XMLDoc_createElement( name )
{
	var domNode = this.domDoc.createElement( name );
	return new XMLNode( domNode, this );
}

////////////////////////////////////////////////////////////////////////////////
// Miscellaneous functions
//

/**
 * Create a DOM document from the given XML string.
 *
 * @param src  An XML document, in a string
 *
 * @return  A W3C DOM Document object
 */
function buildDom( src )
{
	var xmlDocument;
	if ( usingIE )
	{
		
		xmlDocument = //new ActiveXObject("MSXML2.DOMDocument.3.0"); //Msxml2.DOMDocument
			new ActiveXObject( getControlPrefix() + ".XmlDom" );//
		xmlDocument.async = false;
		xmlDocument.validateOnParse = false;
		xmlDocument.resolveExternals = false;
		xmlDocument.loadXML( src );
		
	}
	else
	{
		xmlDocument  = (new DOMParser()).parseFromString(src, "text/xml");   
	}
	return xmlDocument;
}

function getControlPrefix() {
   if (getControlPrefix.prefix)
      return getControlPrefix.prefix;
   
   var prefixes = ["MSXML2", "Microsoft", "MSXML", "MSXML3"];
   var o, o2;
   for (var i = 0; i < prefixes.length; i++) {
      try {
         // try to create the objects
         o = new ActiveXObject(prefixes[i] + ".XmlHttp");
         o2 = new ActiveXObject(prefixes[i] + ".XmlDom");
         return getControlPrefix.prefix = prefixes[i];
      }
      catch (ex) {};
   }
   
   throw new Error("Could not find an installed XML parser");
}


// ================================================================================
// isEmpty() - convenience function to identify an empty string
// ================================================================================

function isEmpty(str) { return (str==null) || (str.length==0); }



// ================================================================================
// trim() - helper function to trim a string s of leading (l=true) and trailing 
//          (r=true) whitespace.
// ================================================================================

function trim(s, l,r)
{
 if(isEmpty(s)) return "";
 
 // the general focus here is on minimal method calls - hence only one substring is
 // done to complete the trim.

 var left=0; var right=0;

 if(l) { 
 	var i=0; 
 	while( (i<s.length) && (whitespace.indexOf(s.charAt(i++))!=-1)) 
 		left++; 
 }
 if(r) { var i=s.length-1; while((i>=left) && (whitespace.indexOf(s.charAt(i--))!=-1)) right++; }

 return s.substring(left, s.length - right);
}




// ================================================================================
// firstWhiteChar() - return the position of the first whitespace character in str 
//                    after position pos.
// ================================================================================

function firstWhiteChar(str,pos)
{
 if(isEmpty(str)) return -1; 
 
 while(pos < str.length)
 {
  if(whitespace.indexOf(str.charAt(pos))!=-1)
   return pos;
  else pos++; 
 }

 return str.length;
}


/**
 * This function takes a string as input and outputs a valid XML-escaped string
 * as output.
 */
function xmlEscape( str , keepangles)
{
	var result = "";
	var c = '';
	var charCode = 0;
	for ( var i = 0; i < str.length; i++ )
	{
		c = str.charAt( i );
		if ( !keepangles && c == '<' )
		{
			result += '&lt;';
		}
		else if ( !keepangles && c == '>' )
		{
			result += '&gt;';
		}
		else if ( c == '&' )
		{
			if ( str.length > i+5 )
			{
				var validxml = false;
				
				if ( str.charAt( i+1 ) == '#' )
				{
					validxml = true;
				}
				var chunk =  str.substring( i, i+5 );				
				if ( chunk == "&amp;" || chunk == "&apos" || chunk == "&quot" || chunk == "&nbsp" )
				{
					validxml = true;
				}
				chunk =  str.substring( i, i+4 );				
				if ( chunk == "&gt;" || chunk == "&lt;"  )
				{
					validxml = true;
				}
				
				
				if ( validxml )
				{//do nothing
					result += '&';										
				}
				else
				{
					result += '&amp;';
				}				
			}
			else
			{
				result += '&amp;';
			}
		}
		else if ( !keepangles && c == '"' )
		{
			result += '&quot;';
		}
		else if ( !keepangles && c == "'" )
		{
			result += '&apos;';
		}
		else
		{
			charCode = str.charCodeAt( i );
			if ( ( charCode < 32 && charCode != 9 && charCode != 10 && charCode != 13 )
			    || ( charCode > 126 ) )
			{
				result += "&#" + charCode + ";";
			}
			else
			{
				result += c;
			}
		}
	}
	return result;
}

/**
 * This function substitutes all built-in character entities out of the given
 * string and returns the normal text.  The following entities are recognized:
 *
 *     - &lt;
 *     - &gt;
 *     - &amp;
 *     - &quot;
 *     - &apos;
 *     - &#(ddd);
 *     - &#x(hhh);
 */
function xmlUnescape( str )
{
	if ( str.indexOf( '&' ) == -1 )
	{
		return str;
	} 
	var result = "";
	var index = 0;
	var c = '';
	while ( index < str.length )
	{
		c = str.charAt( index );
		if ( c == '&' )
		{
			// Handle character entities.
			
			if ( str.charAt( index + 1 ) == '#' )
			{
				var radix = 10;
				var startIndex = index + 2;
				if ( str.charAt( index + 2 ) == 'x' )
				{
					radix = 16;
					startIndex++;
				}
				var semicolonIndex = str.indexOf( ';', startIndex );
				if ( semicolonIndex >= 0 )
				{
					// FIXME: Do some error checking here.
					charIndex = parseInt( str.substring( startIndex, semicolonIndex ), radix );
					result += String.fromCharCode( charIndex );
					index = semicolonIndex + 1;
					continue;
				}
			}
			else
			{
				var semicolonIndex = str.indexOf( ';', index + 1 );
				if ( semicolonIndex >= 0 )
				{
					var charToAppend = '';
					// FIXME: Do some error checking here.
					entityName = str.substring( index + 1, semicolonIndex );
					if ( entityName == "lt" )
					{
						charToAppend = "<";
					}
					else if ( entityName == "gt" )
					{
						charToAppend = ">";
					}
					else if ( entityName == "amp" )
					{
						charToAppend = "&";
					}
					else if ( entityName == "quot" )
					{
						charToAppend = '"';
					}
					else if ( entityName == "apos" )
					{
						charToAppend = "'";
					}
					
					if ( charToAppend != "" )
					{
						result += charToAppend;
						index = semicolonIndex + 1;
						continue;
					}
				}
			}
		}
		
		result += c;
		index++;
	}
	
	return result;
}


// Add some IE compatibility methods to Mozilla's Document API.  There are no
// standards for parsing and serializing XML documents, sadly. :(

if (window.DOMParser &&
	window.XMLSerializer &&
	window.Node && Node.prototype && Node.prototype.__defineGetter__) {

	// XMLDocument did not extend the Document interface in some versions
	// of Mozilla. Extend both!
	//XMLDocument.prototype.loadXML = 
	Document.prototype.loadXML = function (s) {
		
		// parse the string to a new doc	
		var doc2 = (new DOMParser()).parseFromString(s, "text/xml");
		
		// remove all initial children
		while (this.hasChildNodes())
			this.removeChild(this.lastChild);
			
		// insert and import nodes
		for (var i = 0; i < doc2.childNodes.length; i++) {
			this.appendChild(this.importNode(doc2.childNodes[i], true));
		}
	};
	
	
	/*
	 * xml getter
	 *
	 * This serializes the DOM tree to an XML String
	 *
	 * Usage: var sXml = oNode.xml
	 *
	 */
	// XMLDocument did not extend the Document interface in some versions
	// of Mozilla. Extend both!
	/*
	XMLDocument.prototype.__defineGetter__("xml", function () {
		return (new XMLSerializer()).serializeToString(this);
	});
	*/
	Document.prototype.__defineGetter__("xml", function () {
		return (new XMLSerializer()).serializeToString(this);
	});
	Node.prototype.__defineGetter__("xml", function () {
		return (new XMLSerializer()).serializeToString(this);
	});	
}
