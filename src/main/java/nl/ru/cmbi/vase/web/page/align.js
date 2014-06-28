function demo(msg) {
	
	document.getElementById("demo").innerHTML = msg;
}

var data_cell_classname = "data_table_cell",
	data_row_classname  = "data_table_row",
	data_toggle_image_classname = "data_table_column_image",
	toggle_classname = "toggled";

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

function toggleColumnImage(varID, up ) {

	var svgs = document.getElementsByTagName("svg");
	for (var i = 0; i < svgs.length; i++) {
		
		var classString = svgs[i].getAttribute("class");
		
		if(null != classString && classString.indexOf(data_toggle_image_classname) != -1 ) {
			
			var m = pTableCellClass.exec( classString );
			
			classString = classString.replace(toggle_classname,"").trim();
			
			if( m && m[1] == varID ) { // it's the one we need to make visible
				
				// We need to flip the 'path' element
				for(var g=0;g<svgs[i].childNodes.length;g++) {
					
					if(svgs[i].childNodes[g].tagName!="g") continue;
					
					for(var p=0;p<svgs[i].childNodes[g].childNodes.length; p++) {
						
						if(svgs[i].childNodes[g].childNodes[p].tagName!="path") continue;
						
						var pathElement = svgs[i].childNodes[g].childNodes[p];
						
						if(up)
							
							pathElement.setAttribute("transform","scale(1,1)");
							
						else // down
							
							pathElement.setAttribute("transform","scale(1,-1)");
					}
				}
				
				svgs[i].setAttribute("class",classString+" "+toggle_classname);
				
			} else { // it's one of the other's that needs to be hidden
				
				svgs[i].setAttribute("class",classString);
			}
		}
	}
	
}

function orderTableBy(varID) {
	
	/*	Here 'getElementsByClassName' only finds half of the elements in some browsers,
		so use 'getElementsByTagName' instead.
	*/
	
	var divs = document.getElementsByTagName("div");
	var dataCells = [];
	for (var i = 0; i < divs.length; i++) {
		
		var classString = divs[i].getAttribute("class");
		
		if(null != classString && classString.indexOf(data_row_classname) == -1 ) {
			
			var m = pTableCellClass.exec( classString );
			if( m && m[1] == varID ) {

				dataCells.push(divs[i]);
			}
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

var	alignment_highlighted_classname = "alignment_highlighted";

function isColumnHighlighted(classname) {
	
	var elements = document.getElementsByClassName(classname+" "+alignment_columnheader_classname+" "+alignment_highlighted_classname);
	
	return (elements.length > 0);
}

/* 
 * For performance reasons, we don't place every aligned residue in a tag.
 * Instead, place only the header hashtags in tags. This allows control over the columns in the alignment.
 * We just make the following function copy any highlighting from the header elements.
*/
function updateSequenceHighlighting () {
	
	var headerElements = document.getElementsByClassName(alignment_columnheader_classname);
	var highLightedHeaderElements = document.getElementsByClassName(alignment_columnheader_classname+" "+alignment_highlighted_classname);
	
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
			tagged += "<span class='"+alignment_highlighted_classname+"'>"
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

// svg elements give problems when using the 'className' field, so use 'getattribute' and 'setAttribute' instead.

function highlightColumn(classname) {

	var elements = document.getElementsByClassName(classname);
	
	var m;
	
    for (var i = 0; i < elements.length; i++) {
    	
    	if( elements[i].getAttribute("class").indexOf(alignment_highlighted_classname) == -1 ) {
    		
    		elements[i].setAttribute('class', elements[i].getAttribute("class") +" "+ alignment_highlighted_classname );
    		
    		if(!m) {
    			var m = pPDBresclass.exec(elements[i].getAttribute("class"));
    		}
    	}
    }
	
    // highlight the residue in jmol too:
    if(m) {
    	Jmol.script(jmolApplet0, 'select ' + m[1] + ';color red;');
    }
}

function unHighlightAll() {
	
	var elements = document.getElementsByClassName(alignment_highlighted_classname);
	
	// 'elements' is continuously updated, thus markings must be removed in reverse order
	
    for (i = elements.length-1; i>=0; i--) {

		elements[i].setAttribute('class', elements[i].getAttribute("class")
				.replace(alignment_highlighted_classname,"").trim() );
	}
	
    
    Jmol.script(jmolApplet0, 'select *; color atoms lightgrey structure;');

    updateSequenceHighlighting();
}

function unHighlightColumn(classname) {
	
	var elements = document.getElementsByClassName(classname);
	
	var m;
	
    for (var i = 0; i < elements.length; i++) {

		elements[i].setAttribute('class', elements[i].getAttribute("class")
				.replace(alignment_highlighted_classname,"").trim() );
		
		if(!m) {
			var m = pPDBresclass.exec(elements[i].getAttribute("class"));
		}
	}
	
    // unhighlight the residue in jmol too:
    if(m) {
    	
    	Jmol.script(jmolApplet0, 'select ' + m[1] + ';color lightgrey;');
    }
}

function toggleColumn(classname) {
	
	if(isColumnHighlighted(classname)) {
    	
    	unHighlightColumn(classname);
    	
    } else {
    	
    	highlightColumn(classname);
    }
	
    updateSequenceHighlighting();
}

function switchTabVisibility(id) {
	
	for (var i=0; i<tabids.length; i++) {
		
		var tabElement = document.getElementById(tabids[i]);
		
		if( tabids[i]==id ) {
			
			tabElement.style.display = 'block';
		} else {
			tabElement.style.display = 'none';
		}
	}
}