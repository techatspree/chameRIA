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

//JsonRpc Client
$(function(){
    var jsonrpc = new JSONRpcClient("/JSONRPC");

    // Intercept button click to call the service.
    $("#callJsonRpc").click(function() {

       jsonrpc.helloService1.hello(function(result, exception){
         
    	 //Handle the exception
    	 if (exception) {
            console.log('An error occured while trying to call helloService1.hello');
         }

         // Display a success message.
         $("#result").empty();
         var message = $("<div></div>").html(result).addClass("alert-message").addClass("success (JsonRpc)");
         $("#result").append(message);

       }, $("#nameJsonRpc").val()); //args

    });

});


