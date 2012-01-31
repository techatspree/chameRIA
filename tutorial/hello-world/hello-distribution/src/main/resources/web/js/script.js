$(function(){

    // Intercept button click to call the service.
    $("#call").click(function() {
       $.get("http://localhost:8080/hello?name=" + $("#name").val())
            .success(function(result) {
                // Display a success message.
                $("#result").empty();
               var message = $("<div></div>").html(result).addClass("alert-message").addClass("success");
               $("#result").append(message);
           });
    });

});
