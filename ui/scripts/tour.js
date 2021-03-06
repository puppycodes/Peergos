function arrow(from, to) {
    // initialize pointy
    to.pointy({
        pointer      : from,
        // additional class name added to the pointer & the arrow (canvas)
        // to add a z-index of 1
        defaultClass : 'zindex',
        // "pointy-active" class is used to keep the last updated pointer on top
        // this is the default value
        activeClass  : 'pointy-active',
        // arrow base width (in pixels)
        arrowWidth   : 30
    });
}

// String -> String
function drawArrow(sourceClass, targetClass) {
    arrow($("."+sourceClass), $("."+targetClass));
}

function tourStep(text, targetClass) {
    $(".tour-text").text(text);
    if (targetClass.length > 0)
	drawArrow("pointer", targetClass);
}

var tourElements = ["", "tour-home", "tour-upload", "tour-mkdir", "", "", "tour-path", "", "tour-view", "tour-social", "tour-logout"];
var tourText = ["Welcome to Peergos! Let us show you around.", "Click here to go to your home directory", "Click here to upload a file, or drag and drop one into the window.", "Click here to make a new directory", "Double click/double tap a file to download it", "Double click/double tap a directory to navigate into it", "You can use the path bar to jump to other directories", "Right click/long press an item to see options like delete/rename", "Click here to switch between grid and list view", "Click here for social options. You can send a follow request, see your followers and see the files shared with you.", "Click here to logout"];

function showTour(index) {
    console.log("tour "+index);
    $(".pointy").hide();
    $(".pointer").show();
    tourStep(tourText[index], tourElements[index]);
}

function tourNext() {
    var current = tourText.indexOf($(".tour-text").text());
    if (current == tourElements.length-1)
	endTour();
    else
	showTour(current+1);
}

function endTour() {
    $(".pointer").hide();
    $(".pointy").remove();
}

function startTour() {
    showTour(0);
}
