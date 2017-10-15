(function ($) {
    "use strict"; // Start of use strict

    $.ajaxSetup({
        contentType: "application/json; charset=utf-8"
    });

    // Smooth scrolling using jQuery easing
    $('#login-form').submit(function () {

        $.ajax({
            url: "/api/login",
            type: "POST",
            data: JSON.stringify({username: $('#usr').val(), password: $('#pwd').val()}),
            contentType: "application/json; charset=utf-8"
        }).then(function () {
            console.log('login OK');
            $.get("/api/user/self").then(function (user) {
                console.log(user);
            });
        }, function (err) {
            console.log("I Failed !", err);
        });

        return false;
    });


})(jQuery); // End of use strict
