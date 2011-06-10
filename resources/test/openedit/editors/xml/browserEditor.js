var nextEditorID = 0;
var editors = new Array();

/**
 * Create a function that will alert with a message indicating that the given
 * function is abstract.
 *
 * @param name  The name of the method
 *
 * @return  A new Javascript Function object
 */
function abstractMethod( name )
{
	//This is popping up
	//return new Function( "alert( 'Abstract method " + name + " called' );" );
}

/**
 * HTMLDOMStrategy class
 *
 * This interface abstracts out the differences between browsers' HTML DOM
 * implementations.
 */
function HTMLDOMStrategy()
{
	// Configure methods
	
	this.focus = abstractMethod( "HTMLDOMStrategy.focus" );
	this.removeBgColor = abstractMethod( "HTMLDOMStrategy.removeBgColor" );
	this.setBgColor = abstractMethod( "HTMLDOMStrategy.setBgColor" );
	this.setBorder = abstractMethod( "HTMLDOMStrategy.setBorder" );
	this.setCellPadding = abstractMethod( "HTMLDOMStrategy.setCellPadding" );
	this.setCellSpacing = abstractMethod( "HTMLDOMStrategy.setCellSpacing" );
	this.setClass = abstractMethod( "HTMLDOMStrategy.setClass" );
	this.setCols = abstractMethod( "HTMLDOMStrategy.setCols" );
	this.setOnClick = abstractMethod( "HTMLDOMStrategy.setOnClick" );
	this.setRows = abstractMethod( "HTMLDOMStrategy.setRows" );
	this.setWidth = abstractMethod( "HTMLDOMStrategy.setWidth" );
}

/**
 * MozillaHTMLDOMStrategy class
 *
 * This is an implementation of HTMLDOMStrategy for Mozilla browsers.
 */
function MozillaHTMLDOMStrategy()
{
	HTMLDOMStrategy();
	
	this.focus = function( inputElem )
	{
		inputElem.focus();
	}
	
	this.removeBgColor = function( elem )
	{
		elem.removeAttribute( "bgcolor" );
	}
	
	this.setBgColor = function( elem, bgColor )
	{
		elem.setAttribute( "bgcolor", bgColor );
	}
	
	this.setBorder = function( tableElem, borderWidth )
	{
		tableElem.setAttribute( "border", borderWidth );
	}
	
	this.setCellPadding = function( tableElem, cellPadding )
	{
		tableElem.setAttribute( "cellpadding", cellPadding );
	}
	
	this.setCellSpacing = function( tableElem, cellSpacing )
	{
		tableElem.setAttribute( "cellspacing", cellSpacing );
	}
	
	this.setClass = function( elem, className )
	{
		elem.setAttribute( "class", className );
	}
	
	this.setCols = function( textAreaElem, cols )
	{
		textAreaElem.cols= cols ;
	}
	
	this.setOnClick = function( elem, code )
	{
		elem.setAttribute( "onclick", "javascript:" + code );
	}
	
	this.setRows = function( textAreaElem, rows )
	{
		textAreaElem.setAttribute( "rows", rows );
	}
	
	this.setWidth = function( elem, width )
	{
		elem.setAttribute( "width", width );
	}
}

/**
 * IEHTMLDOMStrategy class
 *
 * This is an implementation of HTMLDOMStrategy for Internet Explorer browsers.
 */
function IEHTMLDOMStrategy()
{
	HTMLDOMStrategy();
	
	this.focus = function( inputElem )
	{
		// IE doesn't have the focus() method, so do nothing
		inputElem.focus();
	}
	
	this.removeBgColor = function( elem )
	{
		// FIXME: This is hackish and just happens to work because of where we're calling this.
		elem.style.backgroundColor = "yellow";
	}
	
	this.setBgColor = function( elem, bgColor )
	{
		elem.style.backgroundColor = bgColor;
	}
	
	this.setBorder = function( tableElem, borderWidth )
	{
		tableElem.border = borderWidth;
	}
	
	this.setCellPadding = function( tableElem, cellPadding )
	{
		tableElem.cellPadding = cellPadding;
	}
	
	this.setCellSpacing = function( tableElem, cellSpacing )
	{
		tableElem.cellSpacing = cellSpacing;
	}
	
	this.setClass = function( elem, className )
	{
		elem.setAttribute( "className", className );
	}
	
	this.setCols = function( textAreaElem, cols )
	{
		textAreaElem.cols = cols;
	}
	
	this.setOnClick = function( elem, code )
	{
		elem.setAttribute( "onclick", new Function( code ) );
	}
	
	this.setRows = function( textAreaElem, rows )
	{
		textAreaElem.rows = rows;
	}
	
	this.setWidth = function( elem, width )
	{
		elem.width = width;
	}
}

// FIXME: I can't remember how to do proper inheritance in JavaScript.  It might
//        be like this:
//
//MozillaHTMLDOMStrategy.prototype = HTMLDOMStrategy;

/*
Here are some known problems with IE

currentCell.setAttribute( "class", "datacell-header" ); must be: 	currentCell.setAttribute("className","bar");
currentCell.setAttribute( "onclick", "alert('hey') ); must be currentCell.setAttribute( "onclick", new Function( "alert('hey') ) ); 
currentCell.setAttribute( "bgcolor", #ffffaa ); must be currentCell.style.setAttribute( "bgcolor", "#ffffaa" ); 

http://lists.w3.org/Archives/Public/www-dom/2000OctDec/0175.html
http://faqchest.dynhost.com/msdn/IE-HTML/script-00/script-0012/script00121321_31029.html
*/

/**
 * XMLEditor class
 */
function XMLEditor( inrenderElementID )
{
	// Fields for viewing
	
	this.renderElementID = inrenderElementID;
	this.xmlRenderArea = document.getElementById( this.renderElementID );
	
	this.xmlDoc = null;
	this.errs = "";
	this.editorID = nextEditorID++;
	this.usingIE = navigator.appVersion.indexOf("MSIE") > 0? true : false;
	this.domStrategy =
		( this.usingIE ? new IEHTMLDOMStrategy() : new MozillaHTMLDOMStrategy() );
	
	// Fields for editing
	
	this.selectedGID = null;
	this.copyGID = null;
	this.selectedForEdit = false;	
	this.selectedAttrib = false;
	this.stdBrowser = (document.getElementById) ? true : false;

	
	// Methods for viewing	
	this.displayXML = _XMLEditor_displayXML;
	this.getNode = _XMLEditor_getNode;
	this.displayDocument = _XMLEditor_displayDocument;
	this.displayElement = _XMLEditor_displayElement;
	this.displayComment = _XMLEditor_displayComment;
	this.appendChildren = _XMLEditor_appendChildren;
	this.appendText = _XMLEditor_appendText;
	this.createAttributeCell = _XMLEditor_createAttributeCell;
	
	this.isMixedContent = _XMLEditor_isMixedContent;
	this.getColor = _XMLEditor_getColor;
	this.repaint = _XMLEditor_repaint;	
	
	// Methods for editing
	
	this.cut = _XMLEditor_cut;
	this.copy = _XMLEditor_copy;
	this.pasteInto = _XMLEditor_pasteInto;
	this.deleteSelection = _XMLEditor_deleteSelection;
	this.selectRow = _XMLEditor_selectRow;
	this.selectComment = _XMLEditor_selectComment;
	this.selectText = _XMLEditor_selectText;
	this.getChildrenContentCell = _XMLEditor_getChildrenContentCell;
	this.clearSelection = _XMLEditor_clearSelection;
	this.saveText = _XMLEditor_saveText;
	this.buildEditor = _XMLEditor_buildEditor;
	this.goUp = _XMLEditor_goUp;
	this.goDown = _XMLEditor_goDown;
	this.goLeft = _XMLEditor_goLeft;
	this.goRight = _XMLEditor_goRight;
	this.fillLabelCell = _XMLEditor_fillLabelCell;

	//pop ups
	this.insertNew = _XMLEditor_insertNew;
	this.insertNewOk = _XMLEditor_insertNewOk;
    this.insertXML = _XMLEditor_insertXML;
	this.editAttributes = _XMLEditor_editAttributes;
	this.editAttributesOk = _XMLEditor_editAttributesOk;	
	this.hideNew = _XMLEditor_hideNew;
	this.hideAttrib = _XMLEditor_hideAttrib;
	
	// Initialization
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
	colours[10] = "#6060ff";
	colours[11] = "#5050ff";
	colours[12] = "#4040ff";
	colours[13] = "#3030ff";
	colours[14] = "#2020ff";
	colours[15] = "#1010ff";
	this.colours = colours;
	
	editors[this.editorID] = this;
}

function _XMLEditor_displayXML( src )
{
   	
	this.xmlDoc = new XMLDoc( src );
	this.displayDocument();
}
function _XMLEditor_getNode( gid )
{
	return this.xmlDoc.search( gid );
}


function _XMLEditor_displayDocument()
{
	if ( this.xmlDoc.rootElement==null )
	{
		return;
	}
	rootTable = document.createElement( "TABLE" );

	this.domStrategy.setCellSpacing( rootTable, 0 );
	this.domStrategy.setCellPadding( rootTable, 0 );
	this.domStrategy.setBorder( rootTable, 0 );
	this.domStrategy.setWidth( rootTable, "100%" );
	
	parentTableBody = document.createElement( "TBODY" );
	rootTable.appendChild( parentTableBody );
	
	var currentRow = document.createElement( "TR" );
	parentTableBody.appendChild( currentRow );
	
	this.displayElement( this.xmlDoc.rootElement, currentRow, 1 );


	if ( this.xmlRenderArea.childNodes.length > 0 )
	{
		this.xmlRenderArea.replaceChild( rootTable, this.xmlRenderArea.firstChild );
	}
	else
	{
		this.xmlRenderArea.appendChild( rootTable );
	}
	
}

function _XMLEditor_displayElement( el, currentRow, depth )
{
	if( el == null )
	{
		return;
	}	
	currentRow.setAttribute( "id", "row" + el.gid );
	this.domStrategy.setBgColor( currentRow, this.getColor( depth ) );
	var currentCell = document.createElement( "TD" );

	//now we want to put a header on

	currentRow.appendChild( currentCell );
	if ( el.children != null && !this.isMixedContent( el ) )
	{
		depth++;
		
		currentCell.colSpan = 3;
		//now we need to create a new table that will hold children
		var childrenTable = document.createElement( "TABLE" );
		var newBody = document.createElement( "TBODY");
		childrenTable.appendChild( newBody );
		currentCell.appendChild( childrenTable );
	
	
		//now we want to put a header on
		var newRow = document.createElement( "TR");
		newBody.appendChild( newRow );
		var headerCell = document.createElement( "TD" );
		newRow.appendChild( headerCell );
		var currentText = document.createTextNode( el.getName() + ": " );		
		headerCell.appendChild( currentText );
	
		//and attributes
		var attributeCell = this.createAttributeCell( el, depth );
	
		attributeCell.colSpan = 2;
		newRow.appendChild( attributeCell );

		//ALERT: we must add onclick commands within a table otherwise they are inherited by any parent containers

		this.domStrategy.setBgColor( newRow, this.getColor( depth ) );
		this.domStrategy.setClass( childrenTable, "normalTable" );
		this.domStrategy.setWidth( childrenTable, "97%" ); // FIXME: 100% for Mozilla?
		this.domStrategy.setBorder( childrenTable, 0 );
		this.domStrategy.setCellSpacing( childrenTable, 0 );
		this.domStrategy.setOnClick( attributeCell,
			"editors[" + this.editorID + "].selectRow('" + el.gid + "');" );
		this.domStrategy.setOnClick( headerCell,
			"editors[" + this.editorID + "].selectRow('" + el.gid + "');" );

		this.appendChildren( el, newBody, depth  );
	}
	else
	{
		//currentCell.setAttribute( "id", "th" + el.gid );
			//now we want to put a header on
                this.fillLabelCell( currentCell, el );
		
		//this.domStrategy.setBgColor( currentCell, this.getColor( depth ) );
		this.domStrategy.setOnClick( currentCell,
			"editors[" + this.editorID + "].selectRow('" + el.gid + "');" );
		this.domStrategy.setClass( currentCell, "textcell-header" );
		
		this.appendText( el, currentRow, depth );
		var attributeCell = this.createAttributeCell( el, depth );

		currentRow.appendChild( attributeCell );
	}
}

function _XMLEditor_fillLabelCell(cell, element)
{
	var currentText = document.createTextNode( element.getName() + ": " );
	cell.appendChild( currentText );
}

function _XMLEditor_createAttributeCell( node, depth )
{
	var attributeCell = document.createElement( "TD" );
	
	this.domStrategy.setBgColor( attributeCell, this.getColor( depth ) );
	this.domStrategy.setClass( attributeCell, "textcell-header" );
	
	var atts = ""; 
	for ( var a in node.getAttributes() )
	{
		atts += a + "=\"" + node.getAttributes()[a] + "\" ";
	}
	var attributes = document.createTextNode( atts ); //put attributes in here
	
	attributeCell.appendChild( attributes );
	return attributeCell;
}

function _XMLEditor_displayComment( el, parentTableBody, depth )
{
	var currentRow = document.createElement( "TR" );
	var currentCell = document.createElement( "TD" );
	var currentText = document.createTextNode( el.getText() );
	
	currentCell.appendChild( currentText );
	currentRow.appendChild( currentCell );
	parentTableBody.appendChild( currentRow );
	
	currentCell.colSpan= 3;
	this.domStrategy.setBgColor( currentCell, this.getColor( depth ) );
	this.domStrategy.setOnClick( currentRow,
		"editors[" + this.editorID + "].selectComment('" + el.gid + "');" );
	this.domStrategy.setClass( currentCell, "comment" );
}


function _XMLEditor_appendChildren( parent, body, parentDepth )
{
	for ( var i = 0; i < parent.children.length; i++ )
	{
		var node = parent.children[i];
		if ( node.getNodeType() == node.doc.ELEMENT_NODE || node.getNodeType() == node.doc.TEXT_NODE)
		{
			var nextRow = document.createElement( "TR" );
			body.appendChild( nextRow );
			this.displayElement( node, nextRow, parentDepth );
		}
	}
}


function _XMLEditor_appendText( parent, parentRow, depth )
{
	var xml = "";
	if ( parent.children != null )
	{
		for ( var i = 0; i < parent.children.length; i++ )
		{
			xml += parent.children[i].asXML();
		}
	}
	var text = trim( xml, true, true );
	
	var currentCell = document.createElement( "TD" );
	currentCell.setAttribute( "id", "te" + parent.gid );
	
	this.domStrategy.setOnClick( currentCell,
		"editors[" + this.editorID + "].selectText(event, '" + parent.gid + "')" );
	this.domStrategy.setClass( currentCell, "datacell" );
	
	var div = document.createElement( "div" );
	div.innerHTML = text;
	//var currentText = document.createTextNode( text );
	currentCell.appendChild( div );

	parentRow.appendChild( currentCell );
}

function _XMLEditor_isMixedContent( node ) 
{
	//it can only be mixed if its an Element type
	if ( node.getNodeType() != node.doc.ELEMENT_NODE )
	{
		return false;
	}

	var children = node.children;
	
	if ( children == null )
	{
		return false;
	}
	
	for ( var i = 0; i < children.length; i++ )
	{
		var child = children[i];
	
		if ( ( child.getNodeType() == child.doc.TEXT_NODE || child.getNodeType() == child.doc.CDATA_SECTION_NODE ) && ( trim( child.domNode.nodeValue, true, true ) != "" ) )
		{
			return true;
		}
	}
	
	return false;
}

function _XMLEditor_getColor( level )
{
	return this.colours[ level ];
}


// Editing functionality

function _XMLEditor_cut()
{
	this.copyGID = this.selectedGID;
	this.deleteSelection();
}

function _XMLEditor_copy()
{
	this.copyGID = this.selectedGID;
}


function _XMLEditor_goLeft()
{
	if ( this.selectedGID != null )
	{
		var node = this.getNode( this.selectedGID );
		var repaint = node.moveLeft();
		//now repaint everyhing from the node on down			
		//this.displayDocument();
		if ( repaint )
		{
			this.repaint( node.parent.parent,true );
		}
		else
		{
			this.repaint( node.parent,true );
		}
		this.selectRow( node.gid, node );
	}	
}
function _XMLEditor_goRight()
{
	if ( this.selectedGID != null )
	{
		var node = this.getNode( this.selectedGID );
		var repaint = node.moveRight();
		//now repaint everyhing from the node on down			
		//this.displayDocument();
		if ( repaint )
		{
			this.repaint( node.parent.parent,true );
		}
		else
		{
			this.repaint( node.parent,true );
		}
		
		this.selectRow( node.gid, node );
	}	
}
function _XMLEditor_goUp()
{
	if ( this.selectedGID != null )
	{
		var node = this.getNode( this.selectedGID );
		var repaint = node.moveUp();
		//now repaint everyhing from the node on down			
		//this.displayDocument();
		if ( repaint )
		{
			this.repaint( node.parent.parent,true );
		}
		else
		{
			this.repaint( node.parent,true );
		}
		
		this.selectRow( node.gid, node  );
	}	
}

function _XMLEditor_goDown()
{
	if ( this.selectedGID != null )
	{
		var node = this.getNode( this.selectedGID );
		var repaint = node.moveDown();
		//now repaint everyhing from the node on down			
		//this.displayDocument();
		if ( repaint )
		{
			this.repaint( node.parent.parent,true );
		}
		else
		{
			this.repaint( node.parent,true );
		}
		
		this.selectRow( node.gid, node  );
	}	
}

function _XMLEditor_pasteInto()
{
	if ( this.selectedGID == null )
	{
		alert("Please select a cell");
		return;	
	}
	if ( this.copyGID != null && this.selectedGID != this.copyGID )
	{
		//var td = document.getElementById( "editor" + gid );
		var node = this.xmlDoc.search( this.selectedGID );
		var copyNode = this.xmlDoc.search( this.copyGID ).copy();
		node.prependChild( copyNode );

		//now repaint everyhing from the node on down
		this.repaint( node, true );
		
		//this.selectHeader( this.selectedGID );
	}	
}

function _XMLEditor_deleteSelection()
{
	var node = this.getNode( this.selectedGID );
	if ( node.parent )
	{

		node.deleteFromParent();
	}
	else
	{
		alert( "Cannot delete the root node!" );
	}
	this.repaint( node.parent, true );	
	this.selectedGID = null;
	
}
function _XMLEditor_selectText( evt, gid )
{
	this.clearSelection();
	this.selectedGID = gid;	

	var node = this.getNode( gid );
	
	selectedCell = document.getElementById( "te" + node.gid );
	
	if ( selectedCell != null )
	{
		var td = this.buildEditor( selectedCell, node );
		selectedCell.parentNode.replaceChild( td, selectedCell );

		var textarea = document.getElementById( "xmltext" + node.gid );
		this.domStrategy.focus( textArea );

		this.selectedForEdit = true;
		this.selectedAttrib = false;
	}	
}

//if you have the node you can pass it in as well
function _XMLEditor_selectRow( gid, node )
{
	this.clearSelection();
	this.selectedGID = gid;	
	if ( node == null )
	{
		node = this.getNode( gid );
	}
	var row = document.getElementById("row" + gid );
	
	/*
	if ( this.usingIE )
	{
		row.style.backgroundColor = "yellow";
	}	
	else
	{
		row.removeAttribute( "bgcolor" );		
		row.setAttribute( "class", "selectedTable" );
	}
	*/
	this.domStrategy.removeBgColor( row );
	this.domStrategy.setClass( row, "selectedTable" );
}

function _XMLEditor_selectComment( gid )
{
	this.clearSelection();
	var node = this.getNode( gid );
	
}


function _XMLEditor_getChildrenContentCell( attributeCell )
{
	return attributeCell.parentNode.parentNode.rows[attributeCell.parentNode.rowIndex + 1].firstChild;
}

function _XMLEditor_clearSelection()
{	
	if ( this.selectedGID != null )
	{
		var node = this.getNode( this.selectedGID );
		if ( this.selectedForEdit)
		{
			this.saveText();
			this.repaint( node, true );
		}
		else
		{
			this.repaint( node, false );
		}
	}
	this.selectedGID = null;
}

function _XMLEditor_repaint( node, rebuild )
{
	//first we try by attribute	
	if ( node == null )
	{
		this.displayDocument();
		return;
	}
	var row = document.getElementById( "row" + node.gid );
	if ( row != null )
	{
		if ( rebuild )
		{
			var newRow = document.createElement( "TR");
			this.displayElement(node, newRow, node.getDepth() );
			row.parentNode.replaceChild( newRow, row );			
		}
		else
		{				
			var color = this.getColor( node.getDepth() );
			var childrenTable = row.parentNode.parentNode;
			this.domStrategy.setBgColor( row, color );
			this.domStrategy.setClass( row, "normalTable" );
		}
	}
}


function _XMLEditor_saveText()
{
	if ( this.selectedGID != null )
	{
		var textArea = document.getElementById( "xmltext" + this.selectedGID );
		var node = this.getNode( this.selectedGID );
		if ( node != null && textArea != null )
		{
			var newtext = xmlEscape( textArea.value , true );
			node.setText( newtext );
		}
	}
}



function _XMLEditor_buildEditor( clickcell, node )
{
	//make a text area within a table structure
	editorTable = document.createElement( "TABLE" );
	
	this.domStrategy.setCellSpacing( editorTable, 0 );
	this.domStrategy.setCellPadding( editorTable, 0 );
	this.domStrategy.setBorder( editorTable, 0 );
	this.domStrategy.setWidth( editorTable, "100%" );
	
	editorTable.setAttribute( "id", "editor" + node.gid );	

	editorTableBody = document.createElement( "TBODY" );
	editorTable.appendChild( editorTableBody );
	
	currentRow = editorTableBody.insertRow( 0 );
	
	textArea = document.createElement( "textarea" );
	var xml = "";
	if ( node.children != null )
	{
		for ( var i = 0; i < node.children.length; i++ )
		{
			xml += node.children[i].asXML();
		}
	}
	var someContent = trim( xml, true, true );
	
	if ( someContent.length < 150 )
	{
		this.domStrategy.setRows( textArea, 2 );
	}
	else 
	{
		this.domStrategy.setRows( textArea, someContent.length / 40 );
	}
	
	textArea.setAttribute( "id", "xmltext" + node.gid );
	textArea.setAttribute( "WRAP", "soft" );
	newText = document.createTextNode( someContent );
	textArea.appendChild( newText );
	textCell = currentRow.insertCell( 0 );
	textCell.appendChild( textArea );
	
	//Now all the links
	textCell = currentRow.insertCell( 1 );

	var td = document.createElement( "td" );
	td.setAttribute( "id", "te" + node.gid );
	this.domStrategy.setClass( textArea, "editcell" );
	this.domStrategy.setClass( td, "selectedTable" );
	td.appendChild( editorTable );

	return td;
}

function _XMLEditor_insertNew( )
{
	//show the add new dialog
	var popup = document.getElementById( "addpopup");
	this.domStrategy.setClass( popup, "floatDialog" );
	var element = document.getElementById( "new-elementName");	
	var elementXML = document.getElementById( "elementXML");	
	element.value="";
	elementXML.value="";
	this.domStrategy.focus( element );
}

function _XMLEditor_editAttributes( )
{
	if ( this.selectedGID != null )
	{
		//show the add new dialog
		var popup = document.getElementById( "attribpopup");	
	
		var node = this.getNode( this.selectedGID );
	
		var elementName = document.getElementById( "attrib-elementName");
		
		elementName.value = node.getName();
		this.domStrategy.setClass( popup, "floatDialog" );
		var listofAttributes = document.getElementById( "attriblist");		
		var trs = "<table cellspacing='0' width='350'><tbody>";
	
		for ( var a in node.getAttributes() )
		{
			trs += "<tr><td align='right'><input type='text' value='" + a + "'/></td><td><input type='text' value='" + node.getAttributes()[a] + "' /></td></tr>";
		}
		trs += "<tr><td align='right'><input type='text'/></td><td><input type='text' /></td></tr>";
		trs += "<tr><td align='right'><input type='text'/></td><td><input type='text' /></td></tr>";
		trs += "</tbody></table>";
		
		listofAttributes.innerHTML = trs; //Under IE 6.0 this gives an undefined error? I guess tables dont work here?
		
		var xml = "";
		if ( node.children != null )
		{
			for ( var i = 0; i < node.children.length; i++ )
			{
				xml += node.children[i].asXML();
			}
		}
		xml = trim( xml, true, true );
		var elementXML = document.getElementById( "elementAttribText");	
		
		elementXML.value = xml;
		
		this.domStrategy.focus( elementName );
	}	
}

function _XMLEditor_editAttributesOk( )
{
	//show the add new dialog
	var popup = document.getElementById( "attribpopup");	
	var node = this.getNode( this.selectedGID );
	
	var elementName = document.getElementById( "attrib-elementName");
	var xml = "<" + elementName.value;
	
	var listofAttributes = document.getElementById( "attriblist");		
		
	var body = listofAttributes.firstChild.firstChild;
	for ( var i=0;i<body.childNodes.length;i++ )
	{
		//trs += "<tr><td><input type='text' value='" + a + "'/></td><td><input type='text' value='" + node.attributes[a] + "' /></td></tr>";
		var name = body.childNodes[i].firstChild.firstChild.value;
		var valuep = body.childNodes[i].lastChild.firstChild.value;
		if ( name != null && name.length > 0 )
		{
			xml += " " + name + "=\"" + valuep +"\"";
		}
	}
	
	xml += ">";
	/*
	for( var i=0;i< node.domNode.childNodes.length;i++ )
	{
		xml += node.domNode.childNodes[i].xml;
		xml += "\n";
	}
	*/
	var elementXML = document.getElementById( "elementAttribText");	
	var text = elementXML.value;	
	if ( text != null )
	{
		xml += xmlEscape( text, true );
	}
	xml += "</" + elementName.value +">";
	node.setXml( xml ); 
	this.hideAttrib();
	this.repaint( node, true );
	this.selectRow( node.gid, node );
}



function _XMLEditor_insertNewOk( )
{
	//show the add new dialog
	var element = document.getElementById( "new-elementName");	
	var elementXML = document.getElementById( "elementXML");	
	
	if ( element != null && elementXML != null )
	{
            this.insertXML(element.value, elementXML.value)
	}
	this.hideNew();

}

function _XMLEditor_insertXML(elementName, elementContent, attributes)
{
    var xml = "<" + elementName + " ";
    for ( var a in attributes )
	{
    	xml += a + "='" + xmlEscape( attributes[a] ) + "' ";
    }
        
    xml += ">" + xmlEscape( elementContent, true ) + "</"+ elementName +">";
    
    var selectedNode = this.getNode( this.selectedGID );
    var parentNode;
    if ( selectedNode == null || selectedNode.parent == null)
    {
      	parentNode = this.xmlDoc.rootElement;
    }
    else
    {
		parentNode = selectedNode.parent;        	
    }
    var child = parentNode.addXmlAsChild( xml , selectedNode);
    this.repaint( parentNode , true);
    //this.selectRow( newChild.gid, newChild );
}

function _XMLEditor_hideNew( )
{
	// Hide the Add New Element dialog.
	var popup = document.getElementById( "addpopup");
	this.domStrategy.setClass( popup, "hide" );
}

function _XMLEditor_hideAttrib( )
{
	// Hide the Edit Attributes dialog.
	var popup = document.getElementById( "attribpopup");
	this.domStrategy.setClass( popup, "hide" );
}
