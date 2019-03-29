var cordova = require('cordova'),
    exec = require('cordova/exec');
	
var BluetoothPrinter = {
    test: function (fnSuccess, fnError, test,params) {
        console.log('Test:'+test);
        exec(fnSuccess, fnError, "BluetoothPrinter", "test", [test,params]);
    },
	print:function(fnSuccess, fnError, content){
		exec(fnSuccess, fnError, "BluetoothPrinter", "print", content);
	}
    /*
    ,
    list: function (fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "list", []);
    },
    connect: function (fnSuccess, fnError, name) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "connect", [name]);
    },
    disconnect: function (fnSuccess, fnError) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "disconnect", []);
    },
    print: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "print", [str]);
    },
    printText: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printText", [str]);
    },
    printImage: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printImage", [str]);
    },
    printPOSCommand: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printPOSCommand", [str]);
    },
    printQRCode: function (fnSuccess, fnError, str) {
        exec(fnSuccess, fnError, "BluetoothPrinter", "printQRCode", [str]);
    }
    */
};

module.exports = BluetoothPrinter;