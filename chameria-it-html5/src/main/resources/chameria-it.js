var worker;

function init() {
    testWorker();
}


function testWorker() {
    worker = new Worker("worker.js")
    
    // Watch for messages from the worker
    worker.onmessage = function(e) {
        $('#workertest').css("color", "green");
        console.log("ok");
    };
    
    worker.postMessage("do_something");
}
