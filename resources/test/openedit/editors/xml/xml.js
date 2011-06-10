// XML for SCRIPT - an XML parser in JavaScript.
//
// Copyright (C) 2000  Michael Houghton (mike@idle.org)
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.

// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

// ================================================================================
// define the characters which constitute whitespace, and quotes
// ================================================================================

var whitespace = "\n\r\t ";
var quotes = "\"'";



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
function xmlEscape( str )
{
	var result = "";
	var c = '';
	var charCode = 0;
	for ( var i = 0; i < str.length; i++ )
	{
		c = str.charAt( i );
		if ( c == '<' )
		{
			result += '&lt;';
		}
		else if ( c == '>' )
		{
			result += '&gt;';
		}
		else if ( c == '&' )
		{
			result += '&amp;';
		}
		else if ( c == '"' )
		{
			result += '&quot;';
		}
		else if ( c == "'" )
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



// ================================================================================
// XMLNode() is a constructor for a node of XML (text, comment, cdata, tag, etc.)
//     src:   contains the text for the tag or text entity
//   isTag:   identifies whether the text has been found within < > characters.
//     doc:   contains a reference to the XMLDoc object describing the document.
// ================================================================================
function XMLNode(src, isTag, doc)
{
 this.content;           // the content of text (also CDATA and COMMENT) nodes
 this.src;               // the source of tag nodes
 this.nodeType;          // the type of the node 
 this.doc = doc;         // a reference to the document
 //this.path = ".";			//Path that leads to this one  
 this.parent;
 
 this.doc.gidcount++;
 this.gid = this.doc.gidcount;
 this.doc.allNodes[ this.gid ] = this;
 

 this.tagName;           // the name of the tag (if a tag node)
 this.attributes = null; // an array of attributes (used as a hash table)
 this.children = null;   // an array (list) of the children of this node
 
 // configure the methods

 this.getText = _XMLNode_getText;
 this.parse = _XMLNode_parse;
 this.getAttribute = _XMLNode_getAttribute;
 this.toString = _XMLNode_toString;
 this.getElements = _XMLNode_getElements;
 this.getDepth = _XMLNode_getDepth;
 this.clear = _XMLNode_clear;
 this.asXML = _XMLNode_asXML;
 this.copy = _XMLNode_copy;
 this.addChild = _XMLNode_addChild;
 this.deleteFromParent = _XMLNode_deleteFromParent;
 this.indexOf = _XMLNode_indexOf;
 this.moveUp = _XMLNode_moveUp;
 this.moveDown = _XMLNode_moveDown;
 this.moveLeft = _XMLNode_moveLeft;
 this.moveRight = _XMLNode_moveRight;
 // identify the basic node type
 
 if(!isTag) { this.content = src; this.nodeType = 'TEXT'; }
 else         this.src = src;

}

function _XMLNode_deleteFromParent()
{
	if ( this.parent != null )
	{
		var idx = this.parent.indexOf( this );
		this.parent.children.splice( idx ,1 );				
	}
}

function _XMLNode_clear()
{
	this.children = new Array();
}

function _XMLNode_indexOf( node )
{
	for ( var i = 0; i < this.children.length; i++ )
	{
		if ( this.children[i] == node )
		{
			return i;
		}
	}	
}
function _XMLNode_moveUp()
{
	if ( this.parent != null)
	{
		var index = this.parent.indexOf( this );
		//var nodeabove = parent.children[ index - 1 ];

		var prevnode;
		var previndex;
		for ( var i = index -1; i >= 0; i-- )
		{
			if ( this.parent.children[i].nodeType == "ELEMENT" )
			{
				prevnode = this.parent.children[i];
				previndex = i;
				break;
			}
		}
		if ( prevnode != null )
		{
			this.parent.children.splice( previndex, 1, this );	
			this.parent.children.splice( index, 1, prevnode );	
		}
		else
		{
			//go up to the previous parent end of tree
			var sparent = this.parent.parent;
			if ( sparent != null )
			{
				this.parent.children.splice( index, 1);	
				sparent.addChild( this, true );
				return true;
			}
		}
	}	
	
}
function _XMLNode_moveDown()
{
	if ( this.parent != null)
	{
		var index = this.parent.indexOf( this );
		//var nodeabove = parent.children[ index - 1 ];

		var nextnode;
		var nextindex;
		for ( var i = index+1; i < this.parent.children.length; i++ )
		{
			if ( this.parent.children[i].nodeType == "ELEMENT" )
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
		}
		else
		{
			//go up to the parents next child
			var sparent = this.parent.parent;
			if ( sparent != null )
			{				
				this.parent.children.splice( index, 1);	
				sparent.addChild( this  );
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
		//var nodeabove = parent.children[ index - 1 ];

		var prevnode;
		var previndex;
		for ( var i = index -1; i >= 0; i-- )
		{
			if ( this.parent.children[i].nodeType == "ELEMENT" )
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
			prevnode.addChild( this );
		}
		else
		{
			//nada?
		}
	}	
}
function _XMLNode_moveLeft()
{
	if ( this.parent != null)
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



function _XMLNode_addChild( child, insert, afterthisnode )
{
	if ( afterthisnode != null )
	{
		var idxafter = indexOf( this.children, afterthisnode  );
		//Save the new node on top
		this.children.splice( idxafter, 1, child );	
	}
	else
	{
		if ( insert )
		{	
			//Save the new node on top
			this.children.unshift( child );
	
		}
		else
		{
			this.children[this.children.length] = child;
			
		}
	}
	child.parent = this;
}

function _XMLNode_copy()
{
	
  	var newnode =  new XMLNode("",true,this.doc);

	newnode.content =  this.content;           // the content of text (also CDATA and COMMENT) nodes
	newnode.src =  this.src;               // the source of tag nodes
	newnode.nodeType =  this.nodeType;          // the type of the node 
	//newnode.parent =  this.parent; //this should be replaced at some point
	newnode.tagName =  this.tagName;           // the name of the tag (if a tag node)
	if ( this.attributes != null )
	{
		newnode.attributes = new Array();
		for(var a in this.attributes)
		{
			  newnode.attributes[this.attributes.length] = this.attributes;
		}
	}
	if ( this.children != null )
	{
		newnode.children  = new Array();
		for(var c=0;c< this.children.length;c++ )
		{
			newnode.addChild( this.children[c].copy() );
		}
	}

	return newnode;	
}


// ================================================================================
// XMLNode.getText() - a method to get the text of a given node (recursively, if 
//                     it's an element.
// ================================================================================

function _XMLNode_getText() 
{ 
 if(this.nodeType=='ELEMENT')
 {
  var str = "";
  if(this.children==null)
   return null;
  
  for(var i=0; i < this.children.length; i++)
  {
   var t = this.children[i].getText();
   str +=  (t == null ? "" : this.children[i].getText());
  }
  return str;
 }
 
 else if((this.nodeType=='TEXT') || (this.nodeType=='CDATA'))
  return this.content;
 
 else return null;  // comment nodes get caught here
}



// ================================================================================
// XMLNode.getAttribute() - get the value of a named attribute from an element node
// ================================================================================

function _XMLNode_getAttribute(name) 
{ 
 if(this.attributes == null) return null;
 return this.attributes[name]; 
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
  if((this.children[i].nodeType=='ELEMENT') &&
      ((byName==null) || (this.children[i].tagName == byName)))
   
   elements[elements.length] = this.children[i];
 }
 return elements;
}



// ================================================================================
// XMLNode.toString() - produce a diagnostic string description of a node
// ================================================================================

function _XMLNode_toString() 
{  
 return "" + this.nodeType + ":" + 
  (this.nodeType=='TEXT' || this.nodeType=='CDATA' || this.nodeType=='COMMENT' ? 
   this.content : this.tagName); 
}



// ================================================================================
// XMLNode.parse() - parse out a non-text element (including CDATA, comments)
//                 - handles the parsing of attributes 
// ================================================================================


function _XMLNode_parse()
{

 // if it's a comment, strip off the packaging, mark it a comment node 
 // and quit

 
 if(this.src.indexOf('!--')==0)
 {
  this.nodeType = 'COMMENT';
  this.content = this.src.substring(3,this.src.length-2);
  this.src = "";
  return true; 
 }

 // if it's CDATA, do similar

 else if(this.src.indexOf('![CDATA[')==0)
 {
  this.nodeType = 'CDATA';
  this.content = this.src.substring(8,this.src.length-2);
  this.src = "";
  return true; 
 }

 // it's a closing tag - mark it as a CLOSE node for use in pass 3, and snip
 // off the first character

 else if(this.src.charAt(0)=='/')
 {
  this.nodeType = 'CLOSE';
  this.src = this.src.substring(1);
 }

 // otherwise it's an open tag (possibly an empty element)
 
 else if(this.src.indexOf('?')==0)
 {
  this.nodeType = 'PROC';
  this.src = this.src.substring(1,this.src.length-1);
 }

 else this.nodeType = 'OPEN';

 // if the last character is a /, check it's not a CLOSE tag
 
 if(this.src.charAt(this.src.length-1)=='/')
 {
  if(this.nodeType=='CLOSE') 
   return this.doc.error("singleton close tag");
  else this.nodeType = 'SINGLE';
  
  // strip off the last character
  
  this.src = this.src.substring(0,this.src.length-1);
 }

 // set up the properties as appropriate
    
 if(this.nodeType!='CLOSE') this.attributes = new Array();
 if(this.nodeType=='OPEN') this.children = new Array();
       
 // trim the whitespace off the remaining content  
       
 this.src = trim(this.src,true,true);
 
 // chuck out an error if there's nothing left
 
 if(this.src.length==0) return this.doc.error("empty tag");
 
 // scan forward until a space...
 
 var endOfName = firstWhiteChar(this.src,0);
 
 // if there is no space, this is just a name (e.g. (<tag>, <tag/> or </tag>
 
 if(endOfName==-1) { this.tagName = this.src; return true;} 

 // otherwise, we should expect attributes - but store the tag name first

 this.tagName = this.src.substring(0,endOfName);
 //this.path= this.tagName;
 // start from after the tag name
 
 var pos = endOfName; 
 
 // now we loop:

 while(pos<this.src.length) 
 {
 
  // chew up the whitespace, if any
  
  while((pos < this.src.length) && (whitespace.indexOf(this.src.charAt(pos))!=-1)) pos++; 
 
  // if there's nothing else, we have no (more) attributes - just break out

  if(pos >= this.src.length) break; 
    
  var p1 = pos;
   
  while((pos < this.src.length) && (this.src.charAt(pos)!='=')) pos++; 
   
  var msg = "attributes must have values";
  
  // parameters without values aren't allowed.
   
  if(pos >= this.src.length) return this.doc.error(msg);
     
  // extract the parameter name
   
  var paramname = trim(this.src.substring(p1,pos++),false,true);
  
  // chew up whitespace
          
  while((pos < this.src.length) && (whitespace.indexOf(this.src.charAt(pos))!=-1)) pos++;  
  
  // throw an error if we've run out of string

  if(pos >= this.src.length) return this.doc.error(msg); 
  
  msg = "attribute values must be in quotes";
   
  // check for a quote mark to identify the beginning of the attribute value 
   
  var quote = this.src.charAt(pos++);
  
  // throw an error if we didn't find one
  
  if(quotes.indexOf(quote)==-1)  return this.doc.error(msg);
  p1 = pos;
   
  while((pos < this.src.length) && (this.src.charAt(pos)!=quote)) pos++; 
  
  // throw an error if we found no closing quote
  
  if(pos >= this.src.length) return this.doc.error(msg);
  
  // extract the parameter value
  
  var paramval = trim(this.src.substring(p1,pos++),false,true);
  
  // if there's already a parameter by that name, it's an error
  
  if(this.attributes[paramname]!=null)
   return this.doc.error("cannot repeat attribute names in elements");
  
  // otherwise, store the parameter
  
  this.attributes[paramname] = xmlUnescape( paramval );
  
  // and loop
          
 }
 return true;
}


function _XMLNode_asXML()
{
	// Ick.  We really need inheritance here.
	if ( this.nodeType == 'ELEMENT' )
	{
		var xml = "<" + this.tagName;
		for ( var attrName in this.attributes )
		{
			xml += ' ' + attrName + '="' + xmlEscape( this.attributes[attrName] ) + '"';
		}
		
		if ( this.children == null || this.children.length == 0 )
		{
			xml += "/>";
		}
		else
		{
			xml += ">";
			
			// FIXME: Could add a pretty-printing option here.
			
			for ( var i = 0; i < this.children.length; i++ )
			{
				xml += this.children[i].asXML();
			}
			xml += "</" + this.tagName + ">";
		}
		return xml;
	}
	else if ( this.nodeType == 'TEXT' )
	{
		return xmlEscape( this.content );
	}
	else if ( this.nodeType == 'CDATA' )
	{
		return "<![CDATA[" + this.content + "]]>";
	}
	else if ( this.nodeType == 'COMMENT' )
	{
		return "<!--" + this.content + "-->";
	}
	else if ( this.nodeType == 'PROC' )
	{
		return "<?" + this.src + "?>";
	}
	else
	{
		// FIXME: Should I throw an exception here?
		return "";
	}
}

function _XMLNode_getDepth()
{
	var depth = 1;
	var node = this;
	while( node.parent != null )
	{
		node = node.parent;
		depth++;
	}
	return depth;
}


// ================================================================================
// XMLDoc  - a constructor for an XML document
//   source: the string containing the document
//    logFn: the (optional) function used to log the stream for debug purposes
//    errFn: the (optional) function used to log errors
// ================================================================================

function XMLDoc(source,logFn, errFn)
{
 this.source = source;        // the string source of the document
 this.docNode;                // the document node
 this.stream = new Array();   // the 'stream' is the store for markup elements
 this.tagOpens;               // stores the opening positions of tags ( < positions)
 this.tagCloses;              // stores the closing positions of tags ( > positions)
 this.hasErrors = false;      // were errors found during the parse?
 this.pass;                   // used to identify the current parse pass number
 this.allNodes = new Array();
 this.gidcount = 0;
 // set up the methods for this object
 
 this.logFn = logFn;          // the user-defined logging ...
 this.errFn = errFn;          // ... and error functions

 this.error = _XMLDoc_error; 
 this.parsePass1 = _XMLDoc_parsePass1;
 this.parsePass2 = _XMLDoc_parsePass2;
 this.parsePass3 = _XMLDoc_parsePass3;
 this.appendToStream = _XMLDoc_appendToStream;
 this.logStream = _XMLDoc_logStream;
 this.search = _XMLDoc_search;
 this.asXML = _XMLDoc_asXML;
 
 // parse the document
  
 if(this.parsePass1() && this.parsePass2())
 {
  this.logStream();
  this.parsePass3();
 }
}


// ================================================================================
// XMLDoc.error() - used to log an error in parsing or validating
// ================================================================================

function _XMLDoc_error(str)
{
 this.hasErrors=true;
 if(this.errFn) this.errFn("ERROR in pass " + this.pass + ": " + str);
 return false;
}



// ================================================================================
// XMLDoc.logStream() - used to log the markup stream produced by pass2
// ================================================================================

function _XMLDoc_logStream()
{
 if(!this.logFn) return;
 for(var i=0; i< this.stream.length; i++) this.logFn(this.stream[i].toString());
}



// ================================================================================
// XMLDoc.appendToStream() - adds a markup element to the stream
//                      str: the string source of the element
//                    isTag: identifies a tag element
// ================================================================================

function _XMLDoc_appendToStream(str, isTag)
{
 if(isTag)
 {
  var item =  new XMLNode(str,isTag,this);
  var sane = item.parse();
  if(sane) this.stream[this.stream.length] = item;
  return sane;
 }
 else
 {
  if(str.length>0)
   this.stream[this.stream.length] = new XMLNode(xmlUnescape(str),false, this);
  return true;
 }
}
function _XMLDoc_search( gid )
{
	//get it out of the hash node
	return this.allNodes[ gid ];
}

// ================================================================================
// XMLDoc.parsePass1() - scans through the source for opening and closings of tags
//                     - checks that the tags open and close in a sensible order
// ================================================================================

// pass 1 - check that the tags open and close correctly

function _XMLDoc_parsePass1()
{

 var pos = 0;
 this.pass = 1;

 // set up the arrays used to store positions of < and > characters

 this.tagOpens = new Array();
 this.tagCloses = new Array();
 
 while(true)
 {
  var closing_tag_prefix = '';
  var chpos = this.source.indexOf('<',pos);
  var open_length = 1;
  
  if(chpos ==-1)  break; 

  this.tagOpens[this.tagOpens.length] = chpos;  
  
  // if we found an opening comment sequence, we need to ignore all angle brackets
  // until we find a closing comment sequence
  
  if(chpos == this.source.indexOf('<!--',pos))  
  { 
   open_length = 4;
   closing_tag_prefix = '--'; 
  }

  // similarly, if we find an opening CDATA sequence, we need to ignore all angle
  // brackets until a close CDATA sequence is found
 
  if(chpos == this.source.indexOf('<![CDATA[',pos))  
  { 
   open_length = 9;
   closing_tag_prefix = ']]';
  }
 
  // look for the closing sequence

  chpos = this.source.indexOf(closing_tag_prefix + '>',chpos);
  if(chpos ==-1)  break; 
  
  this.tagCloses[this.tagCloses.length] = chpos + closing_tag_prefix.length;
  
  pos = chpos + closing_tag_prefix.length +1;
  
  // and loop
  
 }

 // if we don't have as many opens as closes, we have a mismatch
 // (this is actually rather too strict, as XML allows > characters in 
 // element content)

 if(this.tagCloses.length != this.tagOpens.length) 
  return this.error("tag character mismatch");
 else return true;
}



// ================================================================================
// XMLDoc.parsePass2() - takes the open and closing positions, and breaks up the
//                       source into a stream of markup elements
// ================================================================================

function _XMLDoc_parsePass2()
{
 var lastOpen = 0; var lastClose = 0;
 var err = false;
 this.pass =2;    

 for(var i=0; (i < this.tagOpens.length) && !err ; i++)
 {
  if(this.tagOpens[i] < lastClose)
   err = true;

  else 
  {
   // we're in text mode - so append the text (up to the next < ) as a text node
   
   err = !this.appendToStream(this.source.substring(lastClose,this.tagOpens[i]), false);
   lastOpen = this.tagOpens[i]+1;

   if(this.tagCloses[i] < lastOpen)
    this.error("tag nesting error");
   else
   {
    // now append all the text up to the next > as a tag node
   
    err = !this.appendToStream(this.source.substring(lastOpen,this.tagCloses[i]), true);
    lastClose = this.tagCloses[i]+1;
   }
  }
  
  // and loop
 }
 
 return !err;
}



// ================================================================================
// XMLDoc.parsePass3() - run through the stream, checking nesting, and build the 
//                       object tree
// ================================================================================

function _XMLDoc_parsePass3()
{
 var stack = new Array();
 var stackHeight = 0;
 var topNode;
 
 this.pass = 3;
 
 // loop through all the elements, using a stack to construct an object hierarchy
 
 for(var i=0; i<this.stream.length;i++)
 {
  var current = this.stream[i];


  if(current.nodeType=='COMMENT')  
  {
  
   // if the stack is empty, ignore the comment
   
   if(stackHeight==0)
     continue;
   
   // otherwise, append this as a child to the element at the top of the stack
   
   else
    topNode.addChild( current);
  }


  // if the current node is a text node:

  if((current.nodeType=='TEXT') ||  
     (current.nodeType=='CDATA'))
  {
  
   // if the stack is empty, and this text node isn't just whitespace, we have a
   // problem (we're not in a document element)
     
   if(stackHeight==0)
   {
    if(trim(current.content,true,false)=="") 
     continue;
    else
     return this.error("expected document node, found: " + stream[i].toString());
   }
   
   // otherwise, append this as a child to the element at the top of the stack
   
   else
   {
    topNode.addChild( current);
   }
  }
  
  // if we find an opening element tag
  
  if(current.nodeType=='OPEN')
  {
   
   // rename it as an element node 
  
   current.nodeType = "ELEMENT";

   // if the stack is empty, this node becomes the document node

   if(stackHeight==0)  this.docNode = current;
   
   // otherwise, append this node as a child to the element at the top of the stack
   
   else                topNode.addChild( current);

   // update the stack top

   stack[stackHeight++] = current;
   topNode = current;   
  }

  // if it's an empty element tag

  if(current.nodeType=='SINGLE')
  {
   
   // again, rewrite it as an element node
  
   current.nodeType = "ELEMENT";
  
   // if the stack is empty, check there's nothing else in the stream after this
   // node, as it's an empty document element
  
   if(stackHeight==0)
   {
   
    // this check almost certainly doesn't work ;)
   
    if(i<this.stream.length-1)
     return this.error("singleton document node not only element in document");

    else 
     this.docNode = current;
   }
   else 
    topNode.addChild( current);
  }

  // if it's a close tag, check the nesting

  if(current.nodeType=='CLOSE')
  {
   
   // if the stack is empty, it's certainly an error
  
   if(stack.length==0)
    return this.error("close tag without open: " +  current.toString());

   // otherwise, check that this node matches the one on the top of the stack

   else
   {
    if(current.tagName!=topNode.tagName)
     return this.error("expected closing " + topNode.tagName + 
                            ", found closing " + current.tagName);
    
    // if it does, pop the element off the top of the stack
    
    else topNode = stack[--stackHeight-1];    
   }
  }
 }
 
 // we've run out of markup - check the stack is now empty
 
 if(stackHeight!=0) return this.error("expected close " + topNode.tagName);
 else return true;
}


function _XMLDoc_asXML()
{
	var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	/*
	FIXME: What about the processing instructions, comments, etc. before and
	       after the document element?  this.allNodes gives all nodes in the
	       document, not all top-level nodes.
	for ( var i = 0; i < this.allNodes.length; i++ )
	{
		xml += this.allNodes[i].asXML();
	}
	*/
	xml += this.docNode.asXML();
	return xml;
}
