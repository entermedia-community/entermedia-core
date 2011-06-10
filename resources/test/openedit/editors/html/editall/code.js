var savedOk = false;
var command = "";
var EDIT = "Edit";
var SOURCE = "Source";
var VIEW  = "Preview";
var currenttab = EDIT;
var localBrowser;
var fontname = "";
var fontsize = "";
var italic = false;
var underline = false;
var bold = false;
var unorderedlist = false;
var orderedlist = false;
var justify = 0; /* 1=left, 2=right, 3=center */
var cut = false;
var copy = false;
var paste = false;
var gotmousedown = false;
var firsttime = true;
var togglebuttons = new Array();
var fieldFocusPause  = false;
var blockElementNames = new Array(
	"ADDRESS",
	"BLOCKQUOTE",
	//"BODY",
	"CAPTION",
	"CENTER",
	"DD",
	"DIV",
	"DL",
	"FIELDSET",
	"FORM",
	"H1",
	"H2",
	"H3",
	"H4",
	"H5",
	"H6",
	"HR",
	"ISINDEX",
	"LI",
	"NOSCRIPT",
	"OL",
	"P",
	"PRE",
	"TBODY",
	"TD",
	"TFOOT",
	"TH",
	"THEAD",
	"TR",
	"UL" );
var blockElementNameSet = new Array();
for ( var i = 0; i < blockElementNames.length; i++ )
{
	blockElementNameSet[blockElementNames[i]] = "";
}

var formattingElementNames = new Array(
	"B",
	"BASEFONT",
	"BIG",
	"CENTER",
	"CITE",
	"CODE",
	"EM",
	"FONT",
	"I",
	"KBD",
	"NOBR",
	"Q",
	"S",
	"SAMP",
	"SMALL",
	"SPAN",
	"STRIKE",
	"STRONG",
	"SUB",
	"SUP",
	"TT",
	"U",
	"VAR" );
var formattingElementNameSet = new Array();
for ( var i = 0; i < formattingElementNames.length; i++ )
{
	formattingElementNameSet[formattingElementNames[i]] = "";
}

var formattingAttributeNames = new Array(
	"ALIGN",
	"BACKGROUND",
	"BGCOLOR",
	"BORDER",
	"CELLPADDING",
	"CELLSPACING",
	"CLASS",
	"className",
	"COLOR",
	"FACE",
	"HEIGHT",
	"HSPACE",
	"MARGINHEIGHT",
	"MARGINWIDTH",
	"NOSHADE",
	"NOWRAP",
	"SIZE",
	"STYLE",
	"style",
	"VALIGN",
	"VSPACE",
	"WIDTH" );

var paragraphStyles = new Array();
var characterStyles = new Array();

var gecko = false;
if (navigator.product == 'Gecko')
{
    gecko = true;
}

//When the focus is on a combo box we found that Gecko would jump 
//all over the place if we where setting the values in the combo
//so we added this method
function focusPause(pause)
{
	fieldFocusPause = pause;
}

function setButtons()
{
  if ( fieldFocusPause || currenttab == VIEW )
  {
	return;
  }
   var newfontname = getEditDocument().queryCommandValue("fontname");
   if (fontname != newfontname) {
     fontname = newfontname;
     var i;
     var numoptions = document.getElementById("fontname").length;
     for (i=0; i < numoptions; i++) {
       if ((document.getElementById("fontname").options[i].text) == newfontname) {
         document.getElementById("fontname").selectedIndex = i;
	 	 break;
       }
     }
     if (i == numoptions) {
       document.getElementById("fontname").options[numoptions] = new Option(newfontname, null);
       document.getElementById("fontname").selectedIndex = i;
     }
   }
   if (!gecko) { /* fontsize appears broken */
   var newfontsize = getEditDocument().queryCommandValue("fontsize");   
   if (fontsize != newfontsize) {
     fontsize = newfontsize;
     var i;
     for (i=0; i < document.getElementById("fontsize").length; i++) {
       if ((document.getElementById("fontsize").options[i].text) == newfontsize) {
         document.getElementById("fontsize").selectedIndex = i;
         break;
       }
     }
   }
   }
   var newbold = getEditDocument().queryCommandState("Bold");   
   if (bold != newbold) {
     bold = newbold;
     changeState( bold, document.getElementById("bold") );
   }
   var newitalic = getEditDocument().queryCommandState("Italic");   
   if (italic != newitalic) {
     italic = newitalic;
     changeState( italic, document.getElementById("italic") );
   }
   var newunderline = getEditDocument().queryCommandState("Underline");   
   if (underline != newunderline) {
     underline = newunderline;
     changeState( underline, document.getElementById("underline") );
   }
   
   var newunorderedlist = getEditDocument().queryCommandState("insertunorderedlist");
   if (unorderedlist != newunorderedlist) {
     unorderedlist = newunorderedlist;
     changeState( unorderedlist, document.getElementById("insertunorderedlist") );
   }
   var neworderedlist = getEditDocument().queryCommandState("insertorderedlist");
   if (orderedlist != neworderedlist) {
     orderedlist = neworderedlist;
     changeState( orderedlist, document.getElementById("insertorderedlist") );
   }

   var newjustify;
   var currjustify;
   currjustify = getEditDocument().queryCommandState("justifyleft");
   if (currjustify)
     newjustify = 1;
   currjustify = getEditDocument().queryCommandState("justifycenter");
   if (currjustify)
     newjustify = 2;
   currjustify = getEditDocument().queryCommandState("justifyright");
   if (currjustify)
     newjustify = 3;
   
   if (!newjustify)
     newjustify = 1;
   
   if (justify != newjustify) {
     justify = newjustify;
     changeState( false, document.getElementById("justifyright") );
     changeState( false, document.getElementById("justifycenter") );
     changeState( false, document.getElementById("justifyleft") );
     switch (newjustify) {
       case 1:
         changeState( true, document.getElementById("justifyleft") );
         break;
       case 2:
         changeState( true, document.getElementById("justifycenter") );
         break;
       case 3:
         changeState( true, document.getElementById("justifyright") );
         break;
     }
   }
	// Change the class dropdown if we can.
	
	//if ( !gecko )
	//{
		var nearestClassElem = findAncestorFromSelection( null, "class" );
		if ( nearestClassElem )
		{
			// In IE 5.5, Element.getAttribute( attrName ) does not work.
			
			// FIXME: Put the new style in the right place in the list, and put
			//        the text as nearestClass, not "p " + nearestClass.
			
			var nearestClass = nearestClassElem.attributes["class"].nodeValue;
			if ( isBlockElement( nearestClassElem ) )
			{
				selectComboBoxValue( "classname", "  " + nearestClass, "p " + nearestClass, "Paragraph Styles" );
			}
			else
			{
				selectComboBoxValue( "classname", "  " + nearestClass, "c " + nearestClass, "Character Styles" );
			}
		}
		else
		{
			selectComboBoxValue( "classname", "(none)", "(none)", "Style" );
		}
	//}
}

function isBlockElement( element )
{
	return ( blockElementNameSet[element.tagName] != null );
}

function isFormattingElement( element )
{
	// It's a formatting element if:
	//
	//     - it's in the list of formatting elements; or
	//     - it contains a colon (Microsoft Office junk).
	
	return ( formattingElementNameSet[element.tagName] != null ||
		element.tagName.indexOf( ":" ) >= 0 );
}

function populateClassDropdown()
{
	var classCombo = document.getElementById( "classname" );
	classCombo.length = 0;
	classCombo.options[classCombo.options.length] = new Option( "Style", "" );
	classCombo.options[classCombo.options.length] = new Option( "(none)", "(none)" );
	
	classCombo.options[classCombo.options.length] = new Option( "Paragraph Styles", "" );
	if ( paragraphStyles )
	{
		for ( var i = 0; i < paragraphStyles.length; i++ )
		{
			classCombo.options[classCombo.options.length] =
				new Option( "- " + paragraphStyles[i], "p " + paragraphStyles[i] );
		}
	}
	
	classCombo.options[classCombo.options.length] = new Option( "Character Styles", "" );
	if ( characterStyles )
	{
		for ( var i = 0; i < characterStyles.length; i++ )
		{
			classCombo.options[classCombo.options.length] =
				new Option( "- " + characterStyles[i], "c " + characterStyles[i] );
		}
	}
}

function selectComboBoxValue( comboBoxName, text, value, sectionText )
{
	var comboBox = document.getElementById( comboBoxName );
	var numoptions = comboBox.length;
	
	// sectionTitleIndex is the option index of the section title.
	var sectionTitleIndex = -1;
	
	// lastItemInSection is the option index of the last item in the section
	// whose title is sectionText.  Sections are denoted by leading " "
	// characters.
	var lastItemInSection = -1;
	
	// foundItem is true if the item was found in the combo box
	var foundItem = false;
	
	for ( var i = 0; i < numoptions; i++ )
	{
		if ( sectionTitleIndex >= 0 )
		{
			if ( comboBox.options[i].value == value )
			{
				comboBox.selectedIndex = i;
				foundItem = true;
				break;
			}
			if ( comboBox.options[i].text.substring( 0, 2 ) != "- " )
			{
				lastItemInSection = i - 1;
				break;
			}
		}
		else
		{
			if ( comboBox.options[i].text == sectionText )
			{
				sectionTitleIndex = i;
			}
		}
	}
	
	if ( !foundItem )
	{
		/*
		if ( lastItemInSection < 0 )
		{
			lastItemInSection = numoptions - 1;
		}
		
		if ( sectionTitleIndex >= 0 )
		{
			comboBox.options.splice( lastItemInSection + 1, 1, new Option( text, value ) );
		}
		else
		{
			// Do nothing for now...
			//alert( "Could not find section with title '" + sectionText + "'" );
		}
		*/
		if ( sectionText == "Paragraph Styles" )
		{
			paragraphStyles[paragraphStyles.length] = value.substring( 2 );
			populateClassDropdown();
		}
		else if ( sectionText == "Character Styles" )
		{
			characterStyles[characterStyles.length] = value.substring( 2 );
			populateClassDropdown();
		}
	}
	
	/*
	var i = 0;
	for ( ; i < numoptions; i++ )
	{
		if ( ( comboBox.options[i].value ) == value )
		{
			comboBox.selectedIndex = i;
			break;
		}
	}
	if ( addValue && i == numoptions )
	{
		comboBox.options[numoptions] = new Option( text, value );
		comboBox.selectedIndex = i;
	}
	*/
}

function UpdateTimer()
{
 	setButtons();
    setTimeout("UpdateTimer()", 200);
}

function changeState( on, thediv )
{
	if (  thediv == null )
	{
		//this shouold never happen
		return;
	}
	togglebuttons[thediv.id] = on;
	if ( on )
	{
		goDown( thediv );
	}	
	else
	{
		goUp( thediv );
	}
}

function goDown( thediv )
{
  thediv.firstChild.style.left = 1;
  thediv.firstChild.style.top = 1;
	if ( thediv.className.indexOf("imagebutton") == 0 )
	{
	    thediv.className = "imagebuttonDown";
	}
	else
	{
		thediv.className = "imagetabDown";
	}
}

function goUp( thediv )
{
  if ( togglebuttons[thediv.id] )
  {
  	if ( thediv.className.indexOf("imagebutton") == 0 )
  	{
	    thediv.className = "imagebuttonDown";
	}
	else
	{
		thediv.className = "imagetabDown";
	}
  }
  else
  {
    thediv.firstChild.style.left = 0;
    thediv.firstChild.style.top = 0;
  	if ( thediv.className.indexOf("imagebutton") == 0 )
  	{
	    thediv.className = "imagebutton";
	}
	else
	{
		thediv.className = "imagetab";
	}
    
  }
}

function goHover( thediv )
{
  if ( togglebuttons[thediv.id] )
  {
  	if ( thediv.className.indexOf("imagebutton") == 0 )
  	{
	    thediv.className = "imagebuttonDownHover";
	}
	else
	{
		thediv.className = "imagetabDownHover";
	}
  }
  else
  {
    thediv.firstChild.style.left = 0;
    thediv.firstChild.style.top = 0;
  	if ( thediv.className.indexOf("imagebutton") == 0 )
  	{
	    thediv.className = "imagebuttonHover";
	}
	else
	{
		thediv.className = "imagetabHover";
	}

  }

}

function tbmousedown()
{
	goDown( this );
	setButtons();
}

function tbmouseup()
{
	goHover( this );
}

function tbmouseout()
{
	goUp( this );
	//setButtons(); //this does not work since it causes it to not repaint
}

function tbmouseover()
{
	goHover( this );
}

  function insertNodeAtSelection(win, insertNode)
  {
  	 if ( gecko )
  	 {
      // get current selection
      var sel = win.getSelection();

      // get the first range of the selection
      // (there's almost always only one range)
      var range = sel.getRangeAt(0);

      // deselect everything
      sel.removeAllRanges();

      // remove content of current selection from document
      range.deleteContents();

      // get location of current selection
      var container = range.startContainer;
      var pos = range.startOffset;

      // make a new range for the new selection
      range=document.createRange();

      if (container.nodeType==3 && insertNode.nodeType==3) {

        // if we insert text in a textnode, do optimized insertion
        container.insertData(pos, insertNode.nodeValue);

        // put cursor after inserted text
        range.setEnd(container, pos+insertNode.length);
        range.setStart(container, pos+insertNode.length);

      } else {


        var afterNode;
        if (container.nodeType==3) {

          // when inserting into a textnode
          // we create 2 new textnodes
          // and put the insertNode in between

          var textNode = container;
          container = textNode.parentNode;
          var text = textNode.nodeValue;

          // text before the split
          var textBefore = text.substr(0,pos);
          // text after the split
          var textAfter = text.substr(pos);

          var beforeNode = document.createTextNode(textBefore);
          var afterNode = document.createTextNode(textAfter);

          // insert the 3 new nodes before the old one
          container.insertBefore(afterNode, textNode);
          container.insertBefore(insertNode, afterNode);
          container.insertBefore(beforeNode, insertNode);

          // remove the old node
          container.removeChild(textNode);

        } else {

          // else simply insert the node
          afterNode = container.childNodes[pos];
          container.insertBefore(insertNode, afterNode);
        }

        range.setEnd(afterNode, 0);
        range.setStart(afterNode, 0);
      }

      sel.addRange(range);
     }
     else
     {
     	if (document.selection) 
		{
   			var selectedRange = document.selection.createRange();
   			selectedRange.parentElement().appendChild( insertNode );
   		}
   	}
}
/*
function selectColor( color )
{
	document.getElementById("colorpalette").style.visibility="hidden";
	checkRange();

	//this is required by IE
	getEditWindow().focus();

	//FIXME: for some reason we end up the the start of the document in IE
	getEditDocument().execCommand(command,true, color);
	
	getEditWindow().focus();
	
}
*/

/**
 * Find the nearest block ancestor of the current selection.
 */
function findNearestBlockAncestorFromSelection()
{
	var selection = checkRange();
	if ( gecko )
	{
		return findNearestBlockAncestor( selection.commonAncestorContainer );
	}
	else
	{
		return findNearestBlockAncestor( selection.parentElement() );
	}
}

/**
 * Find the nearest ancestor element of the given element that is a block-level
 * element.  If the given element is itself a block element, it will be
 * returned.
 *
 * @param element  The element node at which to start searching
 *
 * @return  The nearest ancestor of the given element, or <code>null</code> if
 *          no such ancestor could be found
 */
function findNearestBlockAncestor( element )
{
	var curElement = element;
	while ( curElement != null && !isBlockElement( curElement ) )
	{
		curElement = curElement.parentNode;
	}
	return curElement;
}

/**
 * Find the closest ancestor of anything in the current selection with the
 * given element name and/or attribute name.
 *
 * @param elementName    The name of the element to look for (may be null)
 * @param attributeName  The name of the attribute to look for (may be null)
 */
function findAncestorFromSelection( elementName, attributeName )
{
	var selection = checkRange();
	if ( gecko )
	{
		return findAncestor(
			findNearestElementAncestor( selection.commonAncestorContainer ),
			elementName, attributeName );
	}
	else
	{
		return findAncestor( selection.parentElement(), elementName,
			attributeName );
	}
}

/**
 * Find the closest ancestor of the given element that has the given element
 * name and/or attribute name.  The element itself will be returned if it
 * matches the criteria.
 *
 * @param element        The element whose ancestors to traverse
 * @param elementName    The name of the element to look for (may be null)
 * @param attributeName  The name of the attribute to look for (may be null)
 */
function findAncestor( element, elementName, attributeName )
{
	//alert( "Finding ancestors of <" + element + ">" );
	var found = true;
	if ( found && elementName && element.nodeName != elementName )
	{
		found = false;
	}
	
	// In IE 5.5, Element.getAttribute( attrName ) does not work.
	
	/*
	if ( gecko )
	{
		if ( found && attributeName && ( !element.getAttribute( attributeName ) ) )
		{
			found = false;
		}
	}
	else
	{
	*/
		if ( found && attributeName && !hasAttribute( element, attributeName ) )
		{
			found = false;
		}
	/*
	}
	*/

	if ( found )
	{
		return element;
	}
	else
	{
		if ( element.parentNode && element.parentNode.nodeType == 1 ) // element
		{
			return findAncestor( element.parentNode, elementName, attributeName );
		}
		else
		{
			return null;
		}
	}
}

/**
 * Find the nearest ancestor of the given node that is an element.  If the
 * given node is an element, it will be returned.
 *
 * @return  The nearest element ancestor, or <code>null</code> if there is none
 */
function findNearestElementAncestor( node )
{
	var parent = node;
	while ( parent != null && !( parent.nodeType == 1 ) ) // element
	{
		parent = parent.parentNode;
	}
	return parent;
}

/**
 * Find the highest descendants of the current selection with the given element
 * name and/or attribute name.
 *
 * @param elementName    The name of the element to look for (may be null)
 * @param attributeName  The name of the attribute to look for (may be null)
 */
function findDescendantsFromSelection( elementName, attributeName )
{
	var selection = checkRange();
	var contents = selection.cloneContents(); // FIXME: Would extractContents() do as well?
	var elements = new Array();
	findDescendantsOfNode( contents, elementName, attributeName, elements );
	return elements;
}

/**
 * Find the highest descendants of the given node with the given element name
 * and/or attribute name.
 *
 * @param node           The node whose descendants to search
 * @param elementName    The name of the element to look for (may be null)
 * @param attributeName  The name of the attribute to look for (may be null)
 * @param nodeList       The array to which to append nodes
 */
function findDescendantsOfNode( node, elementName, attributeName, nodeList )
{
	for ( var i = 0; i < node.childNodes.length; i++ )
	{
		var child = node.childNodes.item( i );
		if ( child.nodeType == 1 ) // element
		{
			findDescendantsOfElement( child, elementName, attributeName, nodeList );
		}
	}
}

/**
 * Find the highest descendants of the given node with the given element name
 * and/or attribute name.  If the given element satisfies the criteria, it will
 * be added to the node list.
 *
 * @param element        The element to search, along with its descendants
 * @param elementName    The name of the element to look for (may be null)
 * @param attributeName  The name of the attribute to look for (may be null)
 * @param nodeList       The array to which to append nodes
 */
function findDescendantsOfElement( element, elementName, attributeName, nodeList )
{
	//alert( "Finding descendants of <" + element.nodeName + ">" );
	var found = true;
	if ( found && elementName && element.nodeName != elementName )
	{
		found = false;
	}
	
	// In IE 5.5, Element.getAttribute( attrName ) does not work.
	
	if ( gecko )
	{
		if ( found && attributeName && ( !element.getAttribute( attributeName ) ) )
		{
			found = false;
		}
	}
	else
	{
		if ( found && attributeName && !hasAttribute( element, attributeName ) )
		{
			found = false;
		}
	}
	
	if ( found )
	{
		nodeList[nodeList.length] = element;
	}
	else
	{
		findDescendantsOfNode( element, elementName, attributeName, nodeList );
	}
}

function removeAllFormattingFromDocument()
{
	var doc = getEditDocument();
	removeAllFormattingFromElement( doc.body );
	doc.body.normalize();
}

function removeAllFormattingFromElement( element )
{
	//alert("go " +element.nodeName + element.outerHTML );
	if ( isFormattingElement( element ) )
	{
		// This is a formatting element, so remove it completely.		
		flattenElement( element );
		return;
	}

	if ( element.nodeType == 1 ) // element
	{
		// Remove any formatting attributes off this element.
		for ( var i = 0; i < formattingAttributeNames.length; i++ )
		{
			if ( element.getAttribute( formattingAttributeNames[i] ) )
			{
				removeAttribute( element, formattingAttributeNames[i] );				
			}
		}
	}	

	var childIndex = 0;
	while ( childIndex < element.childNodes.length )
	{
		var child = element.childNodes[childIndex];

		if ( child.nodeType == 1 ) // element
		{

			removeAllFormattingFromElement( child );

		}			
		childIndex++;		
	}
}

/**
 * Remove the given element within its parent, and replace it by its children.
 * Note that this does <em>not</em> normalize the parent.
 */
function flattenElement( element )
{
	
	if ( element.childNodes.length == 0 )
	{
		element.parentNode.removeChild( element );
	}
	else
	{
		var fragment = element.ownerDocument.createDocumentFragment();
		while ( element.hasChildNodes() )
		{


			var child1 = element.firstChild;
			
			element.removeChild( child1 );
			
			fragment.appendChild( child1 );			


		}
		element.parentNode.replaceChild( fragment, element );
	}
}

/**
 * This function is needed because IE 5.5 and IE 6 do not properly support the
 * DOM Level 1 (Element.getAttribute() does not work).
 */
function hasAttribute( element, attrName )
{
	
	return element.attributes[attrName] && element.attributes[attrName].nodeValue;
	
}

/**
 * This function is needed because IE 5.5 and IE 6 do not properly support the
 * DOM Level 1 (Element.removeAttribute() does not work).
 */
function removeAttribute( element, attrName )
{
	if ( !gecko )
	{
		if ( attrName == "style" )
		{
			element.style.cssText = "";
		}
		else if ( attrName == "className")
		{
			element.setAttribute("className", "");
		}
		else
		{
			if ( element.attributes[attrName] )
			{
				element.attributes[attrName].nodeValue = "";
			}
		}
	}	
	else
	{
		element.removeAttribute( attrName ); // does not work in IE or Mozilla		
	}
}

function checkRange()
{
    if (getEditDocument().selection)
   	{
   	
   		if (getEditDocument().selection.type == "None") {
     		getEditDocument().selection.createRange();
   		}
   		var r = getEditDocument().selection.createRange();
   		return r;
   	}
   	else
   	{
   		return getEditWindow().getSelection().getRangeAt( 0 );
	}
}


function getOffsetTop(elm) {

  var mOffsetTop = elm.offsetTop;
  var mOffsetParent = elm.offsetParent;

  while(mOffsetParent){
    mOffsetTop += mOffsetParent.offsetTop;
    mOffsetParent = mOffsetParent.offsetParent;
  }
 
  return mOffsetTop;
}

function getOffsetLeft(elm) {

  var mOffsetLeft = elm.offsetLeft;
  var mOffsetParent = elm.offsetParent;

  while(mOffsetParent){
    mOffsetLeft += mOffsetParent.offsetLeft;
    mOffsetParent = mOffsetParent.offsetParent;
  }
 
  return mOffsetLeft;
}
function GetSelectionText(sel) {
  if (gecko)
    return sel.toString();
  else
    return sel.text;
}

function dumpProps( obj )
{
	var str = "";
	if ( obj )
	{
		for ( var prop in obj )
		{
			str += prop + " ";
		}
	}
	else
	{
		str = "(object is null)";
	}
	
	alert( str );
}
function findLink()
{
	
	var link;
	var oSel = checkRange();
	var itemcopy;
	var container;
	if ( gecko )
	{
		itemcopy  = oSel.cloneRange();
		itemcopy.collapse(false); //collapse on the end
		container = itemcopy.startContainer;

	}else
	{
		itemcopy = oSel.duplicate(); 
		itemcopy.collapse(); //to limit the selection to where the actual cursor is
		container = itemcopy.parentElement();
	}

	//first check if we are just clicked in there
	if ( container != null && container.tagName == "A" ) //this if may not be needed
	{
		link = container.href;
		if ( link != null && !gecko ) //we need to get the link out of the parent object since IE tends to resove URL's
		{
			link = extractHref( container );
		}
	}

	if ( link == null )
	{
		//now look in any parent objects
    	var parent = findAncestor( container, "A", "href" );
    	//alert( parent );
    	if ( parent != null && parent.href != window.location )
    	{
    		link = parent.href;
    		// The href attribute contains the fully evaluated path, e.g. 
    		// "http://localhost:8080/index.html"
    	    if ( link != null && !gecko ) //we need to get the link out of the parent object since IE tends to resove URL's
			{
				link = extractHref( parent );
			}

    	}
    }
	//finally, if we still dont have a link check the children for the first link
	if ( link == null )
	{
    	var children = new Array();
    	findDescendantsOfNode( container, "A", "href", children );
		if ( children.length > 0 )
		{
			//alert("children");
			link = children[0].href;
		    if ( link != null && !gecko ) //we need to get the link out of the parent object since IE tends to resove URL's
			{
				link = extractHref( children[0] );
			}
		}
	}


    if ( link == null )
    {
    	link = "";
    }

	return link;
}
function extractHref( atag )
{
	var ahtml = atag.outerHTML;
	//alert("Checking" + ahtml );

	var matcher = ahtml.match( /<[Aa][ \t\r\n]*[^>]*[hH][rR][eE][fF][ \t\r\n]*=[ \t\r\n]*('([^']+)'|"([^"]+)"|([^ \t\r\n>]+))/ );
	var link = matcher[1];
	//take off the quotes
	if ( link.indexOf("\"") == 0 || link.indexOf("'") == 0)
	{
		link = link.substring(1,link.length -1 );
	}
	return link;		
}
function tbclick()
{

  	if ( this.id == "viewedit" )
  	{
  		viewsource( false );
  		changeState( false, document.getElementById("viewedit") );
  		changeState( true, document.getElementById("viewsource") );
  		changeState( true, document.getElementById("viewsaved") );
	}
	else if ( this.id == "viewsource" )
	{
		viewsource( true );
  		changeState( true, document.getElementById("viewedit") );
  		changeState( false, document.getElementById("viewsource") );
  		changeState( true, document.getElementById("viewsaved") );
	} 
	else if ( this.id == "viewsaved" )
	{
		viewsaved();	
  		changeState( true, document.getElementById("viewedit") );
  		changeState( true, document.getElementById("viewsource") );
  		changeState( false, document.getElementById("viewsaved") );
	}
 	else if ((this.id == "forecolor") || (this.id == "backcolor")) {
	 command = this.id;
    buttonElement = document.getElementById(this.id );
  	
  	checkRange();
    //document.getElementById("colorpalette").style.left = getOffsetLeft(buttonElement);
    //document.getElementById("colorpalette").style.top = getOffsetTop(buttonElement) + buttonElement.offsetHeight;
    //document.getElementById("colorpalette").style.visibility="visible";
   } else if ( this.id == "save" ) {
   	saveAndClose();
  } else if (this.id == "createlink") {
  	
	getEditWindow().focus();
	var oSel = checkRange();
	//dumpProps( oSel );
	var link = findLink();
	var szURL = prompt("Enter a URL for " + GetSelectionText(oSel), link);
	if ( szURL != null )
	{
		getEditDocument().execCommand("CreateLink",false,szURL)	
	}
	//alert( "Link is " + findLink() );  	
  } else if ( this.id == "showImagePicker" )  {
		showImagePicker();
  } else if (this.id == "createtable") {

    rowstext = prompt("enter rows","");
    colstext = prompt("enter cols","");
    rows = parseInt(rowstext);
    cols = parseInt(colstext);
    if ((rows > 0) && (cols > 0)) {
       var ret = getEditDocument().createTextNode("\n");

      table = getEditDocument().createElement("table");
      table.appendChild( ret );
      table.setAttribute("cellpadding", "2");
      table.setAttribute("cellspacing", "2");
     // table.className = "wsptableeditor";
      tbody = getEditDocument().createElement("tbody");
      for (var i=0; i<rows; i++) {
 	    tbody.appendChild( getEditDocument().createTextNode("\n") );
        tr =getEditDocument().createElement("tr");
        for (var j=0; j<cols; j++) {
          td =getEditDocument().createElement("td");
          br =getEditDocument().createElement("br");
          td.appendChild(br);
	 	  tr.appendChild( getEditDocument().createTextNode("\n") );
          tr.appendChild(td);
        }
 	    tr.appendChild( getEditDocument().createTextNode("\n") );

        tbody.appendChild(tr);
      }
      table.appendChild(tbody);      
      getEditWindow().focus();

      insertNodeAtSelection(getEditWindow(), table);
    }
  } else if ( this.id == "removeformat" ) {
	//this was not good enough
	//getEditDocument().execCommand(this.id, false, null);
	
	var parent = getParentElementForSelection();
	if ( parent.nodeName == "BODY" )
	{
		var ok = confirm("Reformat entire document?");
		if ( ok )
		{
			//only seems to work in Mozilla
		  	removeAllFormattingFromElement( parent );
		}
	}
	else
	{
		removeAllFormattingFromElement( parent );
	}

  	
  } else {
  	 getEditWindow().focus();

    getEditDocument().execCommand(this.id, false, null);
  }
}

function Select(selectname)
{
	var cursel = document.getElementById(selectname).selectedIndex;
	/* First one is always a label */
	if (cursel != 0)
	{
		var selected = document.getElementById(selectname).options[cursel].value;
		if ( selectname == "classname" )
		{
			if ( selected == "(none)" )
			{
				clearClassesFromSelection();
			}
			else if ( selected != "" )
			{
				if ( selected.substring( 0, 2 ) == "p " )
				{
					applyParagraphClassToSelection( selected.substring( 2 ) );
				}
				else if ( selected.substring( 0, 2 ) == "c " )
				{
					applyCharacterClassToSelection( selected.substring( 2 ) );
				}
				else
				{
					applyCharacterClassToSelection( selected );
				}
			}
		}
		else
		{
			getEditDocument().execCommand(selectname, false, selected);
			document.getElementById(selectname).selectedIndex = 0;
		}
		getEditWindow().focus();
	}
}

/**
 * Clear all CSS classes from the current selection.
 */

function clearClassesFromSelection()
{
	// FIXME: This is really bad.  IE doesn't support all the nice programmatic
	//        DOM Level 2 Range methods, so we can't get the element objects
	//        and so forth inside the selection (i.e. we can't
	//        extractContents(), remove all the class attributes off the
	//        Element objects, then insertContents()).  So instead, we get the
	//        parent element and remove all the classes from there on down.
	var parent = getParentElementForSelection();
	
	clearClasses( parent );
}


function getParentElementForSelection()
{
	var selection = checkRange();
	var parent = null;
	if ( selection == null )
	{
		return null;
	}
	if ( gecko )
	{
		parent = selection.commonAncestorContainer;
	}
	else
	{
		parent = selection.parentElement();
	}
	if ( parent.nodeType != 1 )
	{		
		parent = parent.parentNode;
	}

	return parent;
}

/**
 * Clear any class attributes off the given element and all its descendants.
 */
function clearClasses( element )
{
	// FIXME: Remove empty <span> elements.
/*	
	if ( element.className )
	{
		// This works in IE 5.5 and IE 6
		element.className = "";
		
		// This only works in IE 6.
		element.removeAttribute( "class" );
	}
*/
	removeAttribute(element,"class");
	
	
	for ( var i = 0; i < element.childNodes.length; i++ )
	{
		var child = element.childNodes[i];
		if ( child.nodeType == 1 ) // element
		{
			clearClasses( child );
		}
	}
}

/**
 * Apply the given paragraph type CSS class to the current selection.
 *
 * @param className  The CSS class to apply
 */
function applyParagraphClassToSelection( className )
{
	var container = findNearestBlockAncestorFromSelection();
	if ( container == null )
	{
		alert( "Warning: The paragraph style could not be applied because no block element containing the entire selection could be found." );
	}
	else
	{
		//alert( "Found <" + container.nodeName + ">" );
		if ( gecko )
		{
			container.setAttribute( "class", className );
		}
		else
		{
			container.className = className;
		}
	}
}

/**
 * Apply the given character type CSS class to the current selection.
 *
 * @param className  The CSS class to apply
 */
function applyCharacterClassToSelection( className )
{
	// FIXME: Should find out if the selection is at the boundaries of some
	//        other element, and if so, apply the class to that element instead
	//        of always creating a new <span>.
	
	var selection = checkRange();
	
	// Well, there's a surprise.  IE doesn't support the standard DOM Level
	// 2 Range methods.
	
	if ( gecko )
	{
		var spanElem = getEditDocument().createElement( "span" );
		spanElem.className = className;
		
		// Now, we basically do a "surroundContents" except that we allow the
		// start and end points to be in different containers.
	
		var fragment = selection.extractContents();
		spanElem.appendChild( fragment.cloneNode( true ) ); // FIXME: cloning doesn't seem like it should be necessary
		selection.insertNode( spanElem );
	}
	else
	{
		// With IE we have to do this rather dodgy HTML string manipulation....
		
		selection.pasteHTML( "<span class=\"" + className + "\">" +
			selection.htmlText + "</span>" );
	}
}
/*
function dismisscolorpalette()
{
  document.getElementById("colorpalette").style.visibility="hidden";
}
*/

//http://www.scottandrew.com/printable/cbs-events_p.html
function addEvent(obj, evType, fn, useCapture){
  if (obj.addEventListener){
    obj.addEventListener(evType, fn, useCapture);
    return true;
  } else if (obj.attachEvent){
    var r = obj.attachEvent("on"+evType, fn);
    return r;
  } else {
    alert("Handler could not be attached");
  }
}

function getEditDocument() {

  if (gecko) // gecko
  	//Is this the same as document.getElementById("editbox").contentWindow.document?
    //return document.getElementById("editbox").contentDocument;
    return document.getElementById("editbox").contentWindow.document;
  else
    return frames.editbox.document;  
}
function getEditWindow() {

  if (gecko) // gecko
  {
    return document.getElementById('editbox').contentWindow;
  }
  else
  {  	
    return document.editbox;  
  }
}
function viewsaved()
{
	save();
   // document.getElementById("toolbar1").style.visibility="hidden";
   // document.getElementById("toolbar2").style.visibility="hidden";
    var iframe = document.getElementById("editbox");
	iframe.src = fieldOriginalUrl;
	currenttab = VIEW;
}
function viewsource(source){
	var html;
  if ( currenttab == VIEW )
  {
  	//dont save off old data
  }
  if ( currenttab == EDIT )
  {
	html = localBrowser.grabFromWysiwyg();
  }
  
  if ( currenttab == SOURCE )
  {
	html = localBrowser.grabFromSource();  	
  }

  if (source) {
    localBrowser.toSourceView(html);
   // document.getElementById("toolbar1").style.visibility="hidden";
   // document.getElementById("toolbar2").style.visibility="hidden";
	currenttab  = SOURCE;
  } else {
    localBrowser.toWysiwygView(html);
   // document.getElementById("toolbar1").style.visibility="visible";
   // document.getElementById("toolbar2").style.visibility="visible";
	currenttab  = EDIT;
    
  }
}
function isModified()
{
	return originalHtml != grabCurrentHtml();
}

function grabCurrentHtml()
{
		var html;
	if ( currenttab == EDIT )
	{
		html = localBrowser.grabFromWysiwyg();  	
	}
	
	if ( currenttab == SOURCE )
	{
		html = localBrowser.grabFromSource();  	
	}
	return html;
}

function save()
{
	var html = grabCurrentHtml();
	//var html = document.getElementById("source").value;
	document.contentForm.content.value = html;
	document.contentForm.submit();
}
function saveAndClose()
{
	save();	
	savedOk = true;
	window.close();
}

function dashedTables( oktodash )
{
	
	if ( oktodash )
	{
		//find all the tables and set the class on em	
	}
	else
	{
		//find any tables set to specified tables and clear it
	}

}

/*
http://www.faqts.com/knowledge_base/view.phtml/aid/7786

function dokeypress()
{
     tr = doc.selection.createRange();
     event = doc.parentWindow.event;
     if (event.keyCode == 13)
     {
          if (tr.htmlText.length == 0)
          {
               tr.moveEnd('character',1);
               tr.pasteHTML('<br>'+tr.htmlText);
               tr = doc.selection.createRange();
               tr.moveEnd('character',-1);
               tr.select();
          }
          else
          {
               tr.pasteHTML('<br>');
          };
          event.keyCode = 0;
     };
};
doc.onkeypress = dokeypress;
*/

function highlight(html) {

	html  = html.replace(/[\n\r]+/g, "<br/>\n");
	
	html = html.replace(/@/gi,"_AT_");
	html = html.replace(/#/gi,"_HASH_");

	var htmltag = /(&lt;[\w\/]+[ ]*[\w\=\"\'\.\/\;\: \)\(-]*&gt;)/gi;
	html = html.replace(htmltag,"<span class=ccp_tag>$1</span>");

	var imgtag = /<span class=ccp_tag>(&lt;IMG[ ]*[\w\=\"\'\.\/\;\: \)\(-]*&gt;)<\/span>/gi;
	html = html.replace(imgtag,"<span class=ccp_img>$1</span>");

	var formtag = /<span class=ccp_tag>(&lt;[\/]*(form|input){1}[ ]*[\w\=\"\'\.\/\;\: \)\(-]*&gt;)<\/span>/gi;
	html = html.replace(formtag,"<br><span class=ccp_form>$1</span>");

	var tabletag = /<span class=ccp_tag>(&lt;[\/]*(table|tbody|th|tr|td){1}([ ]*[\w\=\"\'\.\/\;\:\)\(-]*){0,}&gt;)<\/span>/gi;
	html = html.replace(tabletag,"<span class=ccp_table>$1</span>");

	//var Atag = /<span class=ccp_tag>(&lt;(\/a&gt;|[\W _\w\=\"\'\.\/\;\:\)\(-]&gt;){1})<\/span>/gi;
	var Atag = /<span class=ccp_tag>(&lt;\/a&gt;){1}<\/span>/gi;
	html = html.replace(Atag,"<span class=ccp_A>$1</span>");

	var Atag = /<span class=ccp_tag>(&lt;a [\W _\w\=\"\'\.\/\;\:\)\(-]+&gt;){1,}<\/span>/gi;
	html = html.replace(Atag,"<span class=ccp_A>$1</span>");

	var parameter = /=("[ \w\'\.\/\;\:\)\(-]+"|'[ \w\"\.\/\;\:\)\(-]+')/gi;
	html = html.replace(parameter,"=<span class=ccp_paramvalue>$1</span>");

	var entity = /&amp;([\w]+);/gi;
	html = html.replace(entity,"<span class=ccp_entity>&amp;$1;</span>");

	var comment = /(&lt;\!--[\W _\w\=\"\'\.\/\;\:\)\(-]*--&gt;)/gi;
	html = html.replace(comment,"<br><span class=ccp_htmlcomment>$1</span>");

	html = html.replace(/_AT_/gi,"@");
	html = html.replace(/_HASH_/gi,"#");


	return html;
}

function checkSave()
{	
	if( !savedOk  && isModified() )
	{
		var save = confirm("Click OK if you have changes you want saved.");
		if ( save )
		{
			saveAndClose();
		}
		else
		{
			releaseLock();
		}
	}
}
