$(function() {
    // Make menus drop down automatically.
    $('ul.nav li.dropdown').hover(function() {
        $('.dropdown-menu', this).fadeIn(1000);
    }, function() {
        $('.dropdown-menu', this).fadeOut('slow', 'swing');
    });
});

