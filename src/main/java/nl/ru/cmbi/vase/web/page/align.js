var data_cell_classname = "data_table_cell",
	data_row_classname  = "data_table_row",
	data_toggle_image_classname = "data_table_column_image",
	toggle_classname = "toggled";


// Because JQuery doesn't work on all tag types/class names,
// some custom functions are required to perform JQuery-like actions.
function findElements( tagname, classname ) {
	
	var tags = document.getElementsByTagName(tagname);
	var classtags = [];
	for (var i = 0; i < tags.length; i++) {
		
		if( hasClass(tags[i], classname) ) {

			classtags.push(tags[i]);
		}
	}
	
	return classtags
}

function hasClass( element, classname ) {
	
	if(element != null && element.getAttribute("class")!=null) {

		var classes = element.getAttribute("class").split(/\s+/);

		for(var i=0; i<classes.length; i++) {

			if( classes[i] == classname ) {
				
				return true;
			}
		}
	}
	return false;
}

function addClass( element, classname ) {
	
	var classString = element.getAttribute("class");
	if(classString==null) {
		
		classString = "";
	}
	
	if( classString.indexOf(classname) == -1 ) {
		
		element.setAttribute('class', classString +" "+ classname );
	}
}

function removeClass( element, classname ) {
	
	var classString = element.getAttribute("class");
	if(classString!=null) {

		element.setAttribute('class',classString.replace(classname,"").trim() );
	}
}

function compareTableValues( string1, string2 ) {
	
	var f1 = parseFloat(string1), f2 = parseFloat( string2 );
	
	if(isNaN(f1) && isNaN(f2) ) {
		
		return string1.localeCompare(string2);
	
	} else if(isNaN(f1)) {
		
		return -1;
		
	} else if(isNaN(f2)) {
		
		return 1;
		
	} else {
		
		return f1 - f2 ;
	}
}

var upImageClass = "glyphicon-chevron-up", downImageClass = "glyphicon-chevron-down";

function toggleColumnImage(varID, up ) {

	var svgs = document.getElementsByTagName("span");
	for (var i = 0; i < svgs.length; i++) {
		
		var classString = svgs[i].getAttribute("class");
		
		if(null != classString && classString.indexOf(data_toggle_image_classname) != -1 ) {
			
			var m = pTableCellClass.exec( classString );
			
			classString = classString.replace(toggle_classname,"")
									.replace(upImageClass,"")
									.replace(downImageClass,"")
									.trim();
			
			if( m && m[1] == varID ) { // it's the one we need to make visible
								
				var imageclass = up? upImageClass : downImageClass;
				
				svgs[i].setAttribute("class",classString+" "+toggle_classname + " " + imageclass);
				
			} else { // it's one of the other's that needs to be hidden
				
				svgs[i].setAttribute("class",classString);
			}
		}
	}
	
}

function orderTableBy(varID) {
	
	/*	Here 'getElementsByClassName' only finds half of the elements in some browsers,
		so use the custom function instead.
	*/
	
	var divs = findElements("div",data_cell_classname);
	
	var dataCells = [];
	for (var i = 0; i < divs.length; i++) {
		
		var classString = divs[i].getAttribute("class");
			
		var m = pTableCellClass.exec( classString );
		if( m && m[1] == varID ) {

			dataCells.push(divs[i]);
		}
	}
	
	var body;
	var values = [];
	var positions = []
	var rows = [];
	for (var i = 0; i < dataCells.length; i++) {

		var value = dataCells[i].innerHTML;
		
		var e = dataCells[i].parentNode;
		while( null == e.getAttribute("class")
			|| e.getAttribute("class").indexOf(data_row_classname) == -1 ) {
			
			e=e.parentNode;
		}
		
		// for some reason, the associative array didn't work with rows as keys
		// so we use a list of list positions
		rows.push( e );
		values.push( value );
		positions.push( rows.length - 1 );
		
		body = e.parentNode;
		body.removeChild( e );
	}
	
	positions.sort(
		function( pos1, pos2 ) {

			return compareTableValues( values[ pos1 ], values[ pos2 ]);
		}
	);
	
	if(values[ positions[0] ] == values[0] ) {

		// means the smallest one is already on top in the browser
		
		positions.reverse();
		toggleColumnImage(varID, true ); // make it point up
	}
	else toggleColumnImage(varID, false ); // make it point down
	
	for(var i = 0; i<rows.length; i++) {
		
		body.appendChild( rows[ positions[i] ] );
	}
}

var	table_highlighted_classname = "danger",
	alignment_highlighted_classname = "label-danger";

/* 
 * For performance reasons, we don't place every aligned residue in a tag.
 * Instead, place only the header hashtags in tags. This allows control over the columns in the alignment.
 * We just make the following function copy any highlighting from the header elements.
*/
function updateSequenceHighlighting () {
	
	var headerElements = document.getElementsByClassName(alignment_columnheader_classname);
	var highLightedHeaderElements = document.getElementsByClassName(alignment_columnheader_classname+" "+"label-danger");
	
	// Make a boolean list that knows which columns (indices) haven been highlighted:
	var columnsHighlighted= [];
	for (var i = 0; i < highLightedHeaderElements.length; i++) {
		
		var m = palignmentposclass.exec( highLightedHeaderElements[i].getAttribute("class") );
		if(m) {
			
			// residue numbers: 1,2,3,4, ..
			// indices: 0,1,2,3, ..
			columnsHighlighted.push( parseInt(m[1]) - 1 ) ;
		}
	}
	
	var sequenceElements = document.getElementsByClassName("alignedseq");
	for (var j = 0; j < sequenceElements.length; j++) {
		
		var tagLess = sequenceElements[j].textContent;
		var tagged;
		if(columnsHighlighted.length>0) {

			tagged = tagLess.substring(0,columnsHighlighted[0]);
		} else {
			tagged = tagLess;
		}
		
		var i=0;
		while (i<columnsHighlighted.length) { // iterates over highlighted areas

			// Determine start and end position of highlighted area.
			var start = columnsHighlighted[i];

			while ( (i+1) < columnsHighlighted.length
				&& columnsHighlighted[i+1]==(columnsHighlighted[i]+1) ) {

				i+=1;
			}
			var end = columnsHighlighted[i] + 1;

			// Write html code for highlighted area.
			tagged += "<span class='"+"label-danger"+"'>"
				+ tagLess.substring(start, end) + "</span>";

			// Write html code for are between this highlight and the next.
			if( (i+1) < columnsHighlighted.length ) {

				tagged += tagLess.substring(end,columnsHighlighted[i+1]);
			}
			else if( end < tagLess.length ) {

				tagged += tagLess.substring(end);
			}
			
			i += 1;
		}
		
		sequenceElements[j].innerHTML = tagged;
	}
}

function findColumnElement(classname) {
	
	var columnElements = findElements( "*", alignment_columnheader_classname ) ;
	for(var i =0; i<columnElements.length ; i++) {
		
		if( hasClass( columnElements[i], classname) ) {
			
			return columnElements[i];
		}
	}
	return null;
}

function findAlignmentPosClassname(classname) {
	
	var m = palignmentposclass.exec(classname);
	if(m)
		return classname;
	else {
		
		var element = findColumnElement(classname);
		if(element==null || element.getAttribute("class")==null)
			return null;
		
		m = palignmentposclass.exec(element.getAttribute("class"));
		if(m)
			return m[0];
	}
	
	return null;
}

function updateJmol(classname) {
	
	// If the column was highlighted, highlight the residue in the jmol; otherwise
	// unhighlight it.
	var columnElement = $('.' + alignment_columnheader_classname + '.' + classname);
	var columnClass = columnElement.attr('class');
	
	if (columnElement.hasClass(alignment_highlighted_classname)) {
		var color = 'red';
	} else {
		var color = 'lightgrey';
	}

	var m = pPDBresclass.exec(columnClass);
	if(m) {
    	Jmol.script(jmolApplet0, 'select ' + m[1] + ';color ' + color + ';');
    }
}

function updateTable(classname) {

	var columnElement = $('.' + alignment_columnheader_classname + '.' + classname);
	
	if( columnElement.hasClass( alignment_highlighted_classname) ) {

		$('.' + data_row_classname + '.' + classname).addClass(table_highlighted_classname);
	} else {
		$('.' + data_row_classname + '.' + classname).removeClass(table_highlighted_classname);
	}
}

var scatter_dot_classname = "scatter-dot";

function updatePlots(classname) {
	
	// jquery doesn't work on svg elements !
	var dots = findElements( 'circle', scatter_dot_classname ) ;

	var columnElement = $('.' + alignment_columnheader_classname + '.' + classname);
	var toggled = columnElement.hasClass(alignment_highlighted_classname) ;
	
	for(var i=0; i<dots.length; i++) {
		
		if( hasClass(dots[i],classname) ) {
			
			if( toggled ) {
		
				addClass( dots[i], table_highlighted_classname );
			} else {
				removeClass( dots[i], table_highlighted_classname );
			}
		}
	}
}

function clearPlotsHighlighting() {

	// jquery doesn't work on svg elements !
	var dots = findElements( 'circle', scatter_dot_classname ) ;
	
	for(var i=0; i<dots.length; i++) {
		
		removeClass( dots[i], table_highlighted_classname );
	}
}

function toggleColumn(classname) {
	
	console.log("toggle column "+classname);
	
	// jquery doesn't find the classes with jmol syntax in it,
	// so find the alignmentpos classname first:
	var classname = findAlignmentPosClassname(classname);
	
	// Always update the column header first as the sequence highlighting and jmol
	// highlighting depend on it.
	$('.' + alignment_columnheader_classname + '.' + classname).toggleClass("label-danger");
	
	updateTable(classname);
	updatePlots(classname);
	updateJmol(classname);
    updateSequenceHighlighting();
}

function unHighlightAll() {
	
	$('.'+table_highlighted_classname).removeClass(table_highlighted_classname);
	$('.'+alignment_highlighted_classname).removeClass(alignment_highlighted_classname);
    
	clearPlotsHighlighting();
    Jmol.script(jmolApplet0, 'select *; color atoms lightgrey structure;');
    updateSequenceHighlighting();
}

function switchTabVisibility(id) {
	
	for (var i=0; i<tabids.length; i++) {
		
		var tabElement = document.getElementById(tabids[i]);
		var switchElement = document.getElementById("switch-"+tabids[i]);
		
		if( tabids[i]==id ) {
			
			tabElement.style.display = 'block';
			
			addClass( switchElement, 'active' );
			
		} else {
			
			tabElement.style.display = 'none';
			
			removeClass( switchElement, 'active' );
		}
	}
}
