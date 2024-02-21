var container = require('rhea');
container.on('message', function (context) {
    console.log(context.message.body);
    context.connection.close();
});
container.once('sendable', function (context) {
    context.sender.send({body:'Hello World!'});
});
var connection = container.connect({'port':5672});
connection.open_receiver('examples');
connection.open_sender('examples');
