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
            contentType: "appication/json; charset=utf-8",
            headers: {
                "X_CSRF_TOKEN": Cookies.get("CSRF_TOKEN")
            }
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

    $('#self-btn').click(function () {

        $.get("/api/user/self").then(function (user) {
            console.log(user);
        });
        return false;
    });


    $('#logout-btn').click(function () {

        $.get("/api/logout").then(function (user) {
            console.log(user);
        });
        return false;
    });

    $('#all-btn').click(function () {

        $.get("/api/admin/user/all").then(function (all) {
            console.log(all);
        });
        return false;
    });


})(jQuery); // End of use strict

function register(token) {
    console.log(token);
    $.ajax({
        url: "/api/register",
        type: "POST",
        data: JSON.stringify({email: $('#register-usr').val(), password: $('#register-pwd').val()}),
        contentType: "application/json",
        headers: {
            "X_CSRF_TOKEN": Cookies.get("CSRF_TOKEN"),
            "G_CAPTCHA": token
        }
    }).then(function () {
        console.log('login OK');
    }, function (err) {
        console.log("I Failed !", err);
    });

}