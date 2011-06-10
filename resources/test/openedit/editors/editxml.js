var nextEditorID = 0;
var editors = new Array();

// XMLEditor class

/*
http://lists.w3.org/Archives/Public/www-dom/2000OctDec/0175.html
http://faqchest.dynhost.com/msdn/IE-HTML/script-00/script-0012/script00121321_31029.html

Here are some known problems with IE
currentCell.setAttribute( "class", "datacell-header" ); must be: 	currentCell.setAttribute("className","bar");
currentCell.setAttribute( "onclick", "alert('hey') ); must be currentCell.setAttribute( "onclick", new Function( "alert('hey') ) ); 
currentCell.setAttribute( "bgcolor", #ffffaa ); must be currentCell.style.setAttribute( "bgcolor", "#ffffaa" ); 

*/
function XMLEditor( renderElementID )
{
	// Fields for viewing
	
	this.renderElementID = renderElementID;
	this.xmlRenderArea = document.getElementById( this.renderElementID );
	this.xmlDoc = null;
	this.errs = "";
	this.editorID = nextEditorID++;
	
	
	// Fields for editing
	
	this.selectedGID = null;
	this.copyGID = null;
	this.selectedForEdit = false;	
	this.selectedAttrib = false;
	this.stdBrowser = (document.getElementById) ? true : false;
	this.usingIE = navigator.appVersion.indexOf("MSIE") > 0? true : false;

	
	// Methods for viewing
	
	this.err = _XMLEditor_err;
	this.displayXML = _XMLEditor_displayXML;
	this.getNode = _XMLEditor_getNode;
	this.writeErrorReport = _XMLEditor_writeErrorReport;
	this.displayDocument = _XMLEditor_displayDocument;
	this.displayElement = _XMLEditor_displayElement;
	this.displayComment = _XMLEditor_displayComment;
	this.appendChildren = _XMLEditor_appendChildren;
	this.appendText = _XMLEditor_appendText;
	this.createAttributeCell = _XMLEditor_createAttributeCell;
	
	this.isNotMixedContent = _XMLEditor_isNotMixedContent;
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

function _XMLEditor_err( str )
{
	this.errs += str;
}

function _XMLEditor_displayXML( src )
{
	this.errs = "";
	this.xmlDoc = new XMLDoc( src, null, _XMLEditor_err );
	
	if ( this.xmlDoc.hasErrors )
	{
		this.writeErrorReport();
	}
	else
	{
		this.displayDocument();
	}
}

function _XMLEditor_getNode( gid )
{
	return this.xmlDoc.search( gid );
}

//TODO fix this to write to the correct place
function _XMLEditor_writeErrorReport()
{
	alert( this.errs );
}

function _XMLEditor_displayDocument()
{
	if ( this.xmlDoc.docNode==null )
	{
		return;
	}
	rootTable = document.createElement( "TABLE" );

	if ( this.usingIE )
	{
		rootTable.cellSpacing = 0;
		rootTable.cellPadding = 0;
		rootTable.border = 0;	
		//editorTable.setAttribute( "className", "normalTable" );		
		rootTable.width = "100%";
		
	}
	else
	{
		rootTable.setAttribute( "width", "100%" );
		rootTable.setAttribute( "cellspacing", "0" );
		rootTable.setAttribute( "border", "0" );
		
	}
	parentTableBody = document.createElement( "TBODY" );
	rootTable.appendChild( parentTableBody );
	
	var currentRow = document.createElement( "TR" );
	parentTableBody.appendChild( currentRow );
	
	this.displayElement( this.xmlDoc.docNode, currentRow, 1 );


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
	var currentCell = document.createElement( "TD" );

	if ( this.usingIE )
	{
		currentRow.style.backgroundColor = this.getColor( depth  );
	}	
	else
	{
		currentRow.setAttribute( "bgcolor", this.getColor( depth ) );
	}

	//now we want to put a header on

	currentRow.appendChild( currentCell );

	if ( el.children != null && this.isNotMixedContent( el ) )
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
		var currentText = document.createTextNode( el.tagName + ": " );		
		headerCell.appendChild( currentText );
	
		//and attributes
		var attributeCell = this.createAttributeCell( el, depth );
	
		attributeCell.colSpan = 2;
		newRow.appendChild( attributeCell );

		//ALERT: we must add onclick commands within a table otherwise they are inherited by any parent containers
		if ( this.usingIE )
		{
			newRow.style.backgroundColor = this.getColor( depth  );
			childrenTable.setAttribute( "className", "normalTable" );
			//Hack because of IE
			childrenTable.width = "97%";
	
			childrenTable.border = 0;
			childrenTable.cellSpacing = 0;
			attributeCell.setAttribute( "onclick", new Function( "editors[" + this.editorID + "].selectRow('" + el.gid + "');" ) );
			headerCell.setAttribute( "onclick", new Function( "editors[" + this.editorID + "].selectRow('" + el.gid + "');" ) );
		}
		else
		{
			attributeCell.setAttribute( "onClick", "javascript:editors[" + this.editorID + "].selectRow('" + el.gid + "');" );
			headerCell.setAttribute( "onClick", "javascript:editors[" + this.editorID + "].selectRow('" + el.gid + "');" );
			childrenTable.setAttribute( "class", "normalTable" );		
			newRow.setAttribute( "bgcolor" , this.getColor( depth ) );
			childrenTable.setAttribute( "cellspacing", "0");
			childrenTable.setAttribute( "width", "100%" );
		}
		
		this.appendChildren( el, newBody, depth  );
	}
	else
	{
		//currentCell.setAttribute( "id", "th" + el.gid );
			//now we want to put a header on
                this.fillLabelCell( currentCell, el );
		
		if ( this.usingIE )
		{		
			//currentCell.style.backgroundColor = this.getColor( depth );
			currentCell.setAttribute( "onclick", new Function( "editors[" + this.editorID + "].selectRow('" + el.gid + "');" ) );
			currentCell.setAttribute( "className", "textcell-header" );
			
		}
		else
		{
			//currentCell.setAttribute( "bgcolor", this.getColor( depth ) );
			currentCell.setAttribute( "onclick", "javascript:editors[" + this.editorID + "].selectRow('" + el.gid + "');" );
			currentCell.setAttribute( "class", "textcell-header" );

		}
		this.appendText( el, currentRow, depth );
		var attributeCell = this.createAttributeCell( el, depth );

		currentRow.appendChild( attributeCell );
	}
}

function _XMLEditor_fillLabelCell(cell, element)
{
        var currentText = document.createTextNode( element.tagName + ": " );		
        cell.appendChild( currentText );
}

function _XMLEditor_createAttributeCell( node, depth )
{
		var attributeCell = document.createElement( "TD" );

		if ( this.usingIE )
		{			
			attributeCell.style.backgroundColor = this.getColor( depth  );
			attributeCell.setAttribute("className","textcell-header");
		}	
		else
		{		
			attributeCell.setAttribute( "bgcolor", this.getColor( depth  ) );
			attributeCell.setAttribute( "class", "textcell-header" );
		}		
		var atts = ""; 
		for ( var a in node.attributes )
		{
			atts += a + "=\"" + node.attributes[a] + "\" ";
		}
		var attributes = document.createTextNode( atts ); //put attributes in here
		
		attributeCell.appendChild( attributes );
		return attributeCell;
}

function _XMLEditor_displayComment( el, parentTableBody, depth )
{
	var currentRow = document.createElement( "TR" );
	var currentCell = document.createElement( "TD" );
	var currentText = document.createTextNode( el.content );
	
	currentCell.appendChild( currentText );
	currentRow.appendChild( currentCell );
	parentTableBody.appendChild( currentRow );
	
	currentCell.colSpan= 3;
	if ( this.usingIE )
	{
		currentCell.style.backgroundColor = this.getColor( depth  );
		currentRow.setAttribute( "onclick", new Function( "editors[" + this.editorID + "].selectComment('" + el.gid + "');" ) );
		currentCell.setAttribute("className","comment");
	}	
	else
	{
		currentCell.setAttribute( "bgcolor", this.getColor( depth  ) );
		currentRow.setAttribute( "onClick", "javascript:editors[" + this.editorID + "].selectComment('" + el.gid + "');" );
		currentCell.setAttribute("class","comment");
	}
}


function _XMLEditor_appendChildren( parent, body, parentDepth )
{
	for ( var i = 0; i < parent.children.length; i++ )
	{
		var node = parent.children[i];

		if ( node.nodeType == 'ELEMENT' || node.nodeType == 'COMMENT')
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

	if ( this.usingIE )
	{
		currentCell.setAttribute( "onclick", new Function( "editors[" + this.editorID + "].selectText(event, '" + parent.gid + "')" ) );
		currentCell.setAttribute( "className", "datacell" );
	}
	else
	{
		currentCell.setAttribute( "onclick", "javascript:editors[" + this.editorID + "].selectText(event, '" + parent.gid + "')" );
		currentCell.setAttribute( "class", "datacell" );

	}
	var div = document.createElement( "div" );
	div.innerHTML = text;
	//var currentText = document.createTextNode( text );
	currentCell.appendChild( div );

	parentRow.appendChild( currentCell );
}

function _XMLEditor_isNotMixedContent( node ) 
{
	var children = node.children;
	
	if ( children == null )
	{
		return true;
	}
	
	for ( var i = 0; i < children.length; i++ )
	{
		var child = children[i];
	
		if ( ( child.nodeType == 'TEXT' || child.nodeType == 'CDATA' ) && ( trim( child.content, true, true ) != "" ) )
		{
			return false;
		}
	}
	
	return true;
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
		node.addChild( copyNode, true );

		//now repaint everyhing from the node on down
		this.repaint( node , true);
		
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
		selectedCell.parentNode.replaceChild( this.buildEditor( selectedCell, node ), selectedCell );
	
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
	if ( this.usingIE )
	{
		row.style.backgroundColor = "yellow";
	}	
	else
	{
		row.removeAttribute( "bgcolor" );		
		row.setAttribute( "class", "selectedTable" );
	}	
	
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
			if ( this.usingIE )
			{
				row.style.backgroundColor = color;
				row.setAttribute( "className", "normalTable" );
			}
			else
			{
				row.setAttribute( "bgcolor" , color );
				row.setAttribute( "class", "normalTable" );		
			}
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
			var xml = "<dummy>" + textArea.value + "</dummy>";
			this.errs = "";
			var doc = new XMLDoc( xml, _XMLEditor_err, _XMLEditor_err );
			if ( doc.hasErrors )
			{
				this.writeErrorReport();
			}
			else
			{
				node.clear();
				var newChildren = doc.docNode.children;
				for ( var i = 0; i < newChildren.length; i++ )
				{
					node.addChild( newChildren[i].copy() );
				}
			}
		}
	}
}

//TODO: Make sure we save last edit when they click on "Save" button or leave page
//alert("Do you want to save?");


function _XMLEditor_buildEditor( clickcell, node )
{
	//make a text area within a table structure
	editorTable = document.createElement( "TABLE" );
	if ( this.usingIE )
	{
		editorTable.cellSpacing = 0;
		editorTable.cellPadding = 0;
		editorTable.border = 0;	
		//editorTable.setAttribute( "className", "normalTable" );		
		editorTable.width = "100%";
		
	}
	else
	{
		editorTable.setAttribute( "cellspacing", "0" );
		editorTable.setAttribute( "border", "0" );	
		editorTable.setAttribute( "width", "100%" );		
	}
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
	if ( this.usingIE )
	{
		textArea.cols =  "10";
		var rows = 10;
		if ( someContent.length > 150 )
		{
			rows = someContent.length / 50;
		}
		if ( rows < 75 )
		{
			textArea.rows = rows;
		}
		else
		{
			textArea.rows = 75;
		}
	}
	else
	{
		textArea.setAttribute( "cols", "120" );
		var rows = 10;
		if ( someContent.length > 150 )
		{
			rows = someContent.length / 50;
		}
		if ( rows < 75 )
		{
			textArea.setAttribute( "rows", 	rows );
		}
		else
		{
			textArea.setAttribute( "rows", 	75);
		}		
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
	if ( this.usingIE )
	{
		textArea.setAttribute( "className", "editcell" );
		td.setAttribute( "className","selectedTable");
	}
	else
	{	
		textArea.setAttribute( "class", "editcell" );
		td.setAttribute( "class","selectedTable");
	}
	td.appendChild( editorTable );
	return td;
}

function _XMLEditor_insertNew( )
{
	//show the add new dialog
	var popup = document.getElementById( "addpopup");	
	if ( this.usingIE )
	{
		popup.setAttribute("className","floatDialog" );	
	}
	else
	{
		popup.setAttribute("class","floatDialog" );	
	}	
	var element = document.getElementById( "new-elementName");	
	if ( !this.usingIE )		
	{
		element.focus();
	}
}

function _XMLEditor_editAttributes( )
{
	if ( this.selectedGID != null )
	{
		//show the add new dialog
		var popup = document.getElementById( "attribpopup");	
	
		var node = this.getNode( this.selectedGID );
	
		var elementName = document.getElementById( "attrib-elementName");
		
		elementName.value = node.tagName;
	
		if ( this.usingIE )
		{
			popup.setAttribute("className","floatDialog" );	
		}
		else
		{
			popup.setAttribute("class","floatDialog" );	
		}	
		var listofAttributes = document.getElementById( "attriblist");		
		var trs = "<table cellspacing='0' width='350'><tbody>";
	
		for ( var a in node.attributes )
		{
			trs += "<tr><td align='right'><input type='text' value='" + a + "'/></td><td><input type='text' value='" + node.attributes[a] + "' /></td></tr>";
		}
		trs += "<tr><td align='right'><input type='text'/></td><td><input type='text' /></td></tr>";
		trs += "<tr><td align='right'><input type='text'/></td><td><input type='text' /></td></tr>";
		trs += "</tbody></table>";
		
		listofAttributes.innerHTML = trs; //Under IE 6.0 this gives an undefined error? I guess tables dont work here?
		
		if ( !this.usingIE )		
		{
			elementName.focus();
		}
	}	
}
function _XMLEditor_editAttributesOk( )
{
	//show the add new dialog
	var popup = document.getElementById( "attribpopup");	
	var node = this.getNode( this.selectedGID );
	
	var elementName = document.getElementById( "attrib-elementName");
	node.tagName = elementName.value;
	
	var listofAttributes = document.getElementById( "attriblist");		
	
	var newAttribs = new Array();	
	
	var body = listofAttributes.firstChild.firstChild;
	for ( var i=0;i<body.childNodes.length;i++ )
	{
		//trs += "<tr><td><input type='text' value='" + a + "'/></td><td><input type='text' value='" + node.attributes[a] + "' /></td></tr>";
		var name = body.childNodes[i].firstChild.firstChild.value;
		var valuep = body.childNodes[i].lastChild.firstChild.value;
		if ( name != null && name.length > 0 )
		{
			newAttribs[name] = valuep;
		}
	}
	node.attributes = newAttribs;
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
}

function _XMLEditor_insertXML(elementName, elementContent, attributes)
{
    var xml = "<" + elementName + ">" + elementContent + "</"+ elementName +">";
    this.errs = "";
    var doc2 = new XMLDoc( xml, _XMLEditor_err, _XMLEditor_err );
    if ( doc2.hasErrors )
    {
            this.hideNew();
            this.writeErrorReport();
    }
    else
    {
            var node;
            if ( this.selectedGID == null )
            {
                    node = this.xmlDoc.docNode;
            }
            else
            {
                    node = this.getNode( this.selectedGID );
                    if ( node.parent != null )
                    {
                            //I guess we want to add this new node on the next row down
                            node = node.parent;
                    }
            }
            var newChild = doc2.docNode;
            if (attributes)
            {
                newChild.attributes = attributes;
            }
            this.xmlDoc.gidcount++;
            newChild.gid = this.xmlDoc.gidcount; 
            this.xmlDoc.allNodes[ newChild.gid ] = newChild;

            node.addChild( newChild );
            this.repaint( node , true);
            this.selectRow( newChild.gid, newChild );
            this.hideNew();
            return newChild;
    }
}

function _XMLEditor_hideNew( )
{
	//show the add new dialog
	var popup = document.getElementById( "addpopup");	
	if ( this.usingIE )
	{
		popup.setAttribute("className","hide" );	
	}
	else
	{
		popup.setAttribute("class","hide" );	
	}
}
function _XMLEditor_hideAttrib( )
{
	//show the add new dialog
	var popup = document.getElementById( "attribpopup");	
	if ( this.usingIE )
	{
		popup.setAttribute("className","hide" );	
	}
	else
	{
		popup.setAttribute("class","hide" );	
	}
}
