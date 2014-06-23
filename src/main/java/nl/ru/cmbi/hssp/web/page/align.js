function demo(msg) {
	
	document.getElementById("demo").innerHTML = msg;
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
	var columnsHighlighed = new Array(headerElements.length);
	for (var i = 0; i < headerElements.length; i++) {
		columnsHighlighed[i] = false;
	}
	for (var i = 0; i < highLightedHeaderElements.length; i++) {
		
		var m = palignmentposclass.exec( highLightedHeaderElements[i].getAttribute("class") );
		if(m) {
			columnsHighlighed[ parseInt(m[1]) ] = true;
		}
	}
	
	var sequenceElements = document.getElementsByClassName("alignedseq");
	for (var j = 0; j < sequenceElements.length; j++) {
		
		var tagLess = sequenceElements[j].textContent;
		var tagged = "";
		
		for(var i = 0; i < tagLess.length; i++) {
			
			if(columnsHighlighed[i]) {
				
				// If an opening tag hasn't been placed yet before the highlighted area, place it:
				if(i==0 || !columnsHighlighed[i-1]) {
					
					tagged += "<span class='"+alignment_highlighted_classname+"'>" ;
				}
				
			// Place an end tag if a highlighted area ends here:
			} else if(i>0 && columnsHighlighed[i-1]) {
					
				tagged += "</span>" ;
			}
			
			tagged += tagLess.charAt(i);
		}
		
		// Place an end tag at the end of the sequence, if necessary.
		if(columnsHighlighed[tagLess.length-1]) {
			
			tagged += "</span>" ;
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