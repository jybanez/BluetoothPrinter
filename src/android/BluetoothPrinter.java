package com.cordova.printer.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.Base64;
import android.widget.Toast;

import com.cordova.printer.bluetooth.Utils.BitMapUtil;
import com.cordova.printer.bluetooth.Utils.BluetoothUtil;
import com.cordova.printer.bluetooth.Utils.ESCUtil;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Random;


public class BluetoothPrinter extends CordovaPlugin {
    private static final String TAG = "BluetoothPrinter";
    /* Demo 版本号*/
    private static final String VERSION = "V1.1.1";
    /*定义打印机状态*/
    private final int PRINTER_NORMAL = 0;
    private final int PRINTER_PAPERLESS = 1;
    private final int PRINTER_THP_HIGH_TEMPERATURE = 2;
    private final int PRINTER_MOTOR_HIGH_TEMPERATURE = 3;
    private final int PRINTER_IS_BUSY = 4;
    private final int PRINTER_ERROR_UNKNOWN = 5;
    /*定义状态广播*/
    private final String PRINTER_NORMAL_ACTION = "com.iposprinter.iposprinterservice.NORMAL_ACTION";
    private final String PRINTER_PAPERLESS_ACTION = "com.iposprinter.iposprinterservice.PAPERLESS_ACTION";
    private final String PRINTER_PAPEREXISTS_ACTION = "com.iposprinter.iposprinterservice.PAPEREXISTS_ACTION";
    private final String PRINTER_THP_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_HIGHTEMP_ACTION";
    private final String PRINTER_THP_NORMALTEMP_ACTION = "com.iposprinter.iposprinterservice.THP_NORMALTEMP_ACTION";
    private final String PRINTER_MOTOR_HIGHTEMP_ACTION = "com.iposprinter.iposprinterservice.MOTOR_HIGHTEMP_ACTION";
    private final String PRINTER_BUSY_ACTION = "com.iposprinter.iposprinterservice.BUSY_ACTION";
    private final String PRINTER_CURRENT_TASK_PRINT_COMPLETE_ACTION = "com.iposprinter.iposprinterservice.CURRENT_TASK_PRINT_COMPLETE_ACTION";
    /*定义消息*/
    private final int MSG_TEST = 1;
    private final int MSG_IS_NORMAL = 2;
    private final int MSG_IS_BUSY = 3;
    private final int MSG_PAPER_LESS = 4;
    private final int MSG_PAPER_EXISTS = 5;
    private final int MSG_THP_HIGH_TEMP = 6;
    private final int MSG_THP_TEMP_NORMAL = 7;
    private final int MSG_MOTOR_HIGH_TEMP = 8;
    private final int MSG_MOTOR_HIGH_TEMP_INIT_PRINTER = 9;
    private final int MSG_CURRENT_TASK_PRINT_COMPLETE = 10;
    /*循环打印类型*/
    private final int MULTI_THREAD_LOOP_PRINT = 1;
    private final int DEFAULT_LOOP_PRINT = 0;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothDevice mBluetoothPrinterDevice = null;
    private BluetoothSocket socket = null;
    /*打印机当前状态*/
    private int printerStatus = PRINTER_ERROR_UNKNOWN;
    //循环打印标志位
    private int loopPrintFlag = DEFAULT_LOOP_PRINT;
    private Context context;

    private boolean isBluetoothOpen = false;
    private Random random = new Random();

    // Print Constants
    private final byte FONT_SIZE_NORMAL = 0x00;
    private final byte FONT_SIZE_TALL   = 0x01;
    private final byte FONT_SIZE_WIDE   = 0x10;
    private final byte FONT_SIZE_LARGE  = 0x11;

    private final byte ALIGN_LEFT   = 0;
    private final byte ALIGN_CENTER = 1;
    private final byte ALIGN_RIGHT  = 2;

    public BluetoothPrinter() {
        Log.v(TAG, "CONSTRUCTOR");
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        context = this.cordova.getActivity().getApplicationContext();

        Log.v(TAG, "initialized!");
        LoadBluetoothPrinter();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
        Log.v(TAG, "EXECUTE : " + action);
        try {
            Log.d(TAG, action);
            Log.d(TAG, args.toString());
			if (action=="print") {
				Log.d(TAG,args.toString());
                   print(args);
			}
			/*
            switch (action) {
                //case "test":
                //    test(args.get(0).toString(),args.get(1).toString());
                //    break;
                case "print":
                    Log.d(TAG,args.toString());
                    print(args);
                    break;
            }
			*/
            callbackContext.success(args);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            Log.e(TAG, errMsg);
            e.printStackTrace();
            callbackContext.error(errMsg);
        }


        return true;
    }
	/*
    public void test(String test,String params){
        switch(test){
            case "printer":
                bluetoothPrinterTest();
                break;
            case "bitmap":
                printBitmapTest();
                break;
            case "raster":
                printRasterBmpTest();
                break;
            case "screen":
                printImage(params);
                break;
            case "barcode":
                printBarcodeTest();
                break;
            case "qrcode":
                printQRcodeTest();
                break;
        }
    }
	*/
    public void test(String test,JSONArray content){
		if (test=="content){
			try{
				JSONObject obj1 = new JSONObject();
				obj1.put("type","text");
				obj1.put("size","FONT_SIZE_NORMAL");
				obj1.put("alignment","ALIGN_RIGHT");
				obj1.put("content","ABC123");
				content.put(obj1);

				JSONObject obj2 = new JSONObject();
				obj2.put("type","text");
				obj2.put("alignment","ALIGN_LEFT");
				obj2.put("size","FONT_SIZE_LARGE");
				obj2.put("content","XXX222");
				content.put(obj2);

				print(content);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		/*
        switch(test){
            case "content":
                try{
                    JSONObject obj1 = new JSONObject();
                    obj1.put("type","text");
                    obj1.put("size","FONT_SIZE_NORMAL");
                    obj1.put("alignment","ALIGN_RIGHT");
                    obj1.put("content","ABC123");
                    content.put(obj1);

                    JSONObject obj2 = new JSONObject();
                    obj2.put("type","text");
                    obj2.put("alignment","ALIGN_LEFT");
                    obj2.put("size","FONT_SIZE_LARGE");
                    obj2.put("content","XXX222");
                    content.put(obj2);

                    print(content);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
		*/
    }

    private void LoadBluetoothPrinter() {
        // 1: Get BluetoothAdapter
        mBluetoothAdapter = BluetoothUtil.getBluetoothAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Please Turn on Bluetooth!", Toast.LENGTH_LONG).show();
            isBluetoothOpen = false;
            return;
        } else {
            isBluetoothOpen = true;
        }
        //2: Get bluetoothPrinter Devices
        mBluetoothPrinterDevice = BluetoothUtil.getIposPrinterDevice(mBluetoothAdapter);
        if (mBluetoothPrinterDevice == null) {
            Toast.makeText(context, "Failed to Load Bluetooth Printer!", Toast.LENGTH_LONG).show();
            return;
        }
        //3: Get conect Socket
        try {
            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Toast.makeText(context, "Bluetooth Printer Loaded!", Toast.LENGTH_LONG).show();
    }

    public void print(JSONArray content){
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,content.toString());

                /*
                byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);

                byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                byte[] align0 = ESCUtil.alignMode((byte) 0);
                byte[] align1 = ESCUtil.alignMode((byte) 1);
                byte[] align2 = ESCUtil.alignMode((byte) 2);
                */

                try {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    //output.write(ESCUtil.init_printer());
                    //output.write(ESCUtil.printAreaWidth(384));

                    //output.write(ciphertext);
                    //output.write(mac);

                    //byte[] out = output.toByteArray();

                    for(int i=0;i<content.length();i++) {
                        try {
                            JSONObject item = content.getJSONObject(i);
                            switch(item.getString("type")){
								case "image":
                                    URL url = new URL(item.getString("content"));
                                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
                                    output.write(BitMapUtil.getRasterBmpData(bitmap, 384, 0));
                                    break;
                                case "text":
                                    if (item.has("size")) {
                                        try {
                                            switch(item.getString("size")) {
                                                case "FONT_SIZE_NORMAL":
                                                    output.write(ESCUtil.fontSizeSet(FONT_SIZE_NORMAL));
                                                    break;
                                                case "FONT_SIZE_TALL":
                                                    output.write(ESCUtil.fontSizeSet(FONT_SIZE_TALL));
                                                    break;
                                                case "FONT_SIZE_WIDE":
                                                    output.write(ESCUtil.fontSizeSet(FONT_SIZE_WIDE));
                                                    break;
                                                case "FONT_SIZE_LARGE":
                                                    output.write(ESCUtil.fontSizeSet(FONT_SIZE_LARGE));
                                                    break;
                                                default:
                                                    output.write(ESCUtil.fontSizeSet(FONT_SIZE_NORMAL));
                                                    break;
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (item.has("alignment")) {
                                        try {
                                            switch(item.getString("alignment")) {
                                                case "ALIGN_LEFT":
                                                    output.write(ESCUtil.alignMode(ALIGN_LEFT));
                                                    break;
                                                case "ALIGN_CENTER":
                                                    output.write(ESCUtil.alignMode(ALIGN_CENTER));
                                                    break;
                                                case "ALIGN_RIGHT":
                                                    output.write(ESCUtil.alignMode(ALIGN_RIGHT));
                                                    break;
                                                default:
                                                    output.write(ESCUtil.alignMode(ALIGN_LEFT));
                                                    break;
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (item.has("content")) {
                                        try {
                                            output.write(item.getString("content").getBytes("UTF-8"));
                                            output.write("\n".getBytes("UTF-8"));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    output.write(ESCUtil.performPrintAndFeedPaper((byte) 50));

                    //byte[] text = "WIZAYA\n".getBytes("UTF-8");
                    byte[][] cmdBytes = {
                            ESCUtil.init_printer(),
                            //ESCUtil.alignMode((byte) 1),
                            ESCUtil.printAreaWidth(384),
                            //ESCUtil.fontSizeSet((byte) 0x00),
                            output.toByteArray(),
                            //fontSize0, text,
                            //fontSize1, text,
                            //fontSize2, text,
                            //fontSize3, text,


                            //lineH0, text,
                            //lineH1, text,
                            //lineH2, text,
                            //lineH3, text,

                            ESCUtil.performPrintAndFeedPaper((byte) 50)
                    };

                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /*
    public int getPrinterStatus() {
        byte[] statusData = new byte[3];
        if (!isBluetoothOpen) {
            printerStatus = PRINTER_ERROR_UNKNOWN;
            return printerStatus;
        }
        if ((socket == null) || (!socket.isConnected())) {
            try {
                socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
            } catch (IOException e) {
                e.printStackTrace();
                return printerStatus;
            }
        }
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            byte[] data = ESCUtil.getPrinterStatus();
            out.write(data, 0, data.length);
            int readsize = in.read(statusData);
            Log.d(TAG, "~~~ readsize:" + readsize + " statusData:" + statusData[0] + " " + statusData[1] + " " + statusData[2]);
            if ((readsize > 0) && (statusData[0] == ESCUtil.ACK && statusData[1] == 0x11)) {
                printerStatus = statusData[2];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return printerStatus;
    }

    private void printerInit() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    if ((socket == null) || (!socket.isConnected())) {
                        socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                    }
                    //Log.d(TAG,"=====printerInit======");
                    OutputStream out = socket.getOutputStream();
                    byte[] data = ESCUtil.init_printer();
                    out.write(data, 0, data.length);
                    out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void print_loop(int flag) {
        switch (flag) {
            case MULTI_THREAD_LOOP_PRINT:
                multiThreadPrintTest();
                break;
            default:
                break;
        }
    }

    public void multiThreadPrintTest() {
        switch (random.nextInt(16)) {
            case 0:
                bluetoothPrinterTest();
                break;
            case 1:
                printLeftMarginTest();
                break;
            case 2:
                printAreaTest();
                break;
            case 3:
                printCharRightSpaceTest();
                break;
            case 4:
                printAlignModeTest();
                break;
            case 5:
                printRelativePositionTest();
                break;
            case 6:
                printAbsolutePositionTest();
                break;
            case 7:
                printTabTest();
                break;
            case 8:
                printUnderlineTest();
                break;
            case 9:
                printBitmapTest();
                break;
            case 10:
                printBarcodeTest();
                break;
            case 11:
                printQRcodeTest();
                break;
            case 12:
                printRasterBmpTest();
                break;
            case 13:
                printKoubeiBill();
                break;
            case 14:
                printBaiduBill();
                break;
            case 15:
                printMeituanBill();
                break;
            case 16:
                printElemoBill();
                break;
            default:
                break;
        }
    }
    

    private void bluetoothPrinterTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    //byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] title1 = "WIZAYA\n".getBytes("UTF-8");
                    byte[] title2 = "Bluetooth Printer test\n".getBytes("UTF-8");
                    byte[] sign1 = "****************".getBytes("UTF-8");
                    byte[] fontTest0 = "ABCDEFGHIJLKMNOP\n".getBytes("UTF-8");
                    byte[] fontTest1 = "abcdefghijlkmnop\n".getBytes("UTF-8");
                    byte[] fontTest2 = "ABCDEFGHIJLKMNOP\n".getBytes("UTF-8");
                    byte[] fontTest3 = "abcdefghijlkmnop\n".getBytes("UTF-8");
                    byte[] orderSerinum = "1234567890\n".getBytes("UTF-8");
                    byte[] specialSign = "(!@#$%^&*_+{}|[]\\:\"<>?,./;')\n".getBytes("UTF-8");
                    byte[] testSign = "----------------".getBytes("UTF-8");
                    byte[] testInfo = "www.wizaya.com\n".getBytes("UTF-8");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 200);

                    byte[][] cmdBytes = {printer_init, charCode, fontSize3, lineH3, align1, title1, fontSize1, lineH2, title2, nextLine, align0,
                            lineH2, fontSize2, sign1, fontSize0, lineH0, fontTest0, fontSize1, lineH1, fontTest1, fontSize2, lineH2, fontTest2,
                            fontSize3, lineH3, fontTest3, align2, fontSize0, lineH2, orderSerinum,
                            fontSize0, lineH1, align0, specialSign,
                            fontSize2, lineH2, testSign,
                            fontSize1, lineH2, testInfo,
                            nextLine, performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    
    private void printLeftMarginTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x13);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(8);
                    byte[] leftMargin2 = ESCUtil.printLeftMargin(16);
                    byte[] leftMargin3 = ESCUtil.printLeftMargin(24);
                    byte[] leftMargin4 = ESCUtil.printLeftMargin(32);
                    byte[] leftMargin5 = ESCUtil.printLeftMargin(48);
                    byte[] leftMargin6 = ESCUtil.printLeftMargin(80);
                    byte[] leftMargin7 = ESCUtil.printLeftMargin(120);
                    byte[] text0 = "左边距0点测试".getBytes("gb2312");
                    byte[] text1 = "左边距8点测试".getBytes("gb2312");
                    byte[] text2 = "左边距16点测试".getBytes("gb2312");
                    byte[] text3 = "左边距24点测试".getBytes("gb2312");
                    byte[] text4 = "左边距32点测试".getBytes("gb2312");
                    byte[] text5 = "左边距48点测试".getBytes("gb2312");
                    byte[] text6 = "左边距80点测试".getBytes("gb2312");
                    byte[] text7 = "左边距120点测试".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, fontSize0, lineH0, align0, leftMargin0, text0, leftMargin1, text1, leftMargin2, text2,
                            leftMargin3, text3, leftMargin4, text4, leftMargin5, text5, leftMargin6, text6, leftMargin7, text7, fontSize1, lineH1,
                            leftMargin0, text0, leftMargin1, text1, leftMargin2, text2, leftMargin3, text3, leftMargin4, text4, leftMargin5, text5,
                            leftMargin6, text6, leftMargin7, text7, fontSize2, lineH2, leftMargin0, text0, leftMargin1, text1, leftMargin2, text2,
                            leftMargin3, text3, leftMargin4, text4, leftMargin5, text5, leftMargin6, text6, leftMargin7, text7, fontSize3, lineH3,
                            leftMargin0, text0, leftMargin1, text1, leftMargin2, text2, leftMargin3, text3, leftMargin4, text4, leftMargin5, text5,
                            leftMargin6, text6, leftMargin7, text7, performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printAreaTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(24);
                    byte[] leftMargin2 = ESCUtil.printLeftMargin(32);
                    byte[] printarea0 = ESCUtil.printAreaWidth(320);
                    byte[] printarea1 = ESCUtil.printAreaWidth(304);
                    byte[] printarea2 = ESCUtil.printAreaWidth(256);
                    byte[] text0 = "左边距0点,打印区域320点宽测试".getBytes("gb2312");
                    byte[] text1 = "左边距0点,打印区域304点宽测试".getBytes("gb2312");
                    byte[] text2 = "左边距0点,打印区域256点宽测试".getBytes("gb2312");
                    byte[] text3 = "左边距24点,打印区域320点宽测试".getBytes("gb2312");
                    byte[] text4 = "左边距24点,打印区域304点宽测试".getBytes("gb2312");
                    byte[] text5 = "左边距24点,打印区域256点宽测试".getBytes("gb2312");
                    byte[] text6 = "左边距32点,打印区域320点宽测试".getBytes("gb2312");
                    byte[] text7 = "左边距32点,打印区域304点宽测试".getBytes("gb2312");
                    byte[] text8 = "左边距32点,打印区域256点宽测试".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, fontSize0, lineH0, align0, leftMargin0, printarea0, text0, printarea1, text1,
                            printarea2, text2, leftMargin1, printarea0, text3, printarea1, text4, printarea2, text5, leftMargin2, printarea0, text6,
                            printarea1, text7, printarea2, text8, fontSize1, lineH1, leftMargin0, printarea0, text0, printarea1, text1,
                            printarea2, text2, leftMargin1, printarea0, text3, printarea1, text4, printarea2, text5, leftMargin2, printarea0, text6,
                            printarea1, text7, printarea2, text8, fontSize2, lineH2, leftMargin0, printarea0, text0, printarea1, text1,
                            printarea2, text2, leftMargin1, printarea0, text3, printarea1, text4, printarea2, text5, leftMargin2, printarea0, text6,
                            printarea1, text7, printarea2, text8, fontSize3, lineH3, leftMargin0, printarea0, text0, printarea1, text1,
                            printarea2, text2, leftMargin1, printarea0, text3, printarea1, text4, printarea2, text5, leftMargin2, printarea0, text6,
                            printarea1, text7, printarea2, text8, performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printCharRightSpaceTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(24);
                    byte[] leftMargin2 = ESCUtil.printLeftMargin(32);
                    byte[] printarea0 = ESCUtil.printAreaWidth(384);
                    byte[] printarea1 = ESCUtil.printAreaWidth(304);
                    byte[] printarea2 = ESCUtil.printAreaWidth(256);
                    byte[] rightSpace0 = ESCUtil.setRightSpaceChar((byte) 0);
                    byte[] rightSpace1 = ESCUtil.setRightSpaceChar((byte) 8);
                    byte[] rightSpace2 = ESCUtil.setRightSpaceChar((byte) 16);
                    byte[] rightSpace3 = ESCUtil.setRightSpaceChar((byte) 24);
                    byte[] text0 = "左边距0点,打印区域384点宽,右间距0点测试".getBytes("gb2312");
                    byte[] text1 = "左边距0点,打印区域384点宽,右间距8点测试".getBytes("gb2312");
                    byte[] text2 = "左边距24点,打印区域304点宽,右间距16点测试".getBytes("gb2312");
                    byte[] text3 = "左边距24点,打印区域256点宽,右间距24点测试".getBytes("gb2312");
                    byte[] text4 = "左边距32点,打印区域304点宽,右间距24点测试".getBytes("gb2312");
                    byte[] text5 = "左边距32点,打印区域256点宽,右间距8点测试".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, fontSize0, lineH0, align0, leftMargin0, printarea0, rightSpace0, text0,
                            printarea0, rightSpace1, text1, leftMargin1, printarea1, rightSpace2, text2, leftMargin1, printarea2, rightSpace3, text3,
                            leftMargin2, printarea1, rightSpace3, text4, leftMargin2, printarea2, rightSpace1, text5,
                            fontSize1, lineH1, align0, leftMargin0,
                            printarea0, rightSpace0, text0, printarea0, rightSpace1, text1, leftMargin1, printarea1, rightSpace2, text2, leftMargin1, printarea2,
                            rightSpace3, text3, leftMargin2, printarea1, rightSpace3, text4, leftMargin2, printarea2, rightSpace1, text5,
                            fontSize2, lineH2, align0, leftMargin0,
                            printarea0, rightSpace0, text0, printarea0, rightSpace1, text1, leftMargin1, printarea1, rightSpace2, text2, leftMargin1, printarea2,
                            rightSpace3, text3, leftMargin2, printarea1, rightSpace3, text4, leftMargin2, printarea2, rightSpace1, text5,
                            fontSize3, lineH3, align0, leftMargin0,
                            printarea0, rightSpace0, text0, printarea0, rightSpace1, text1, leftMargin1, printarea1, rightSpace2, text2, leftMargin1, printarea2,
                            rightSpace3, text3, leftMargin2, printarea1, rightSpace3, text4, leftMargin2, printarea2, rightSpace1, text5,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printAlignModeTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(24);
                    byte[] leftMargin2 = ESCUtil.printLeftMargin(32);
                    byte[] printarea0 = ESCUtil.printAreaWidth(384);
                    byte[] printarea1 = ESCUtil.printAreaWidth(304);
                    byte[] printarea2 = ESCUtil.printAreaWidth(256);
                    byte[] rightSpace0 = ESCUtil.setRightSpaceChar((byte) 0);
                    byte[] rightSpace1 = ESCUtil.setRightSpaceChar((byte) 8);
                    byte[] rightSpace2 = ESCUtil.setRightSpaceChar((byte) 16);
                    byte[] rightSpace3 = ESCUtil.setRightSpaceChar((byte) 24);
                    byte[] text0 = "左边距0点,打印区域384点宽,右间距0点,左对齐测试".getBytes("gb2312");
                    byte[] text1 = "左边距0点,打印区域384点宽,右间距8点,左对齐测试".getBytes("gb2312");
                    byte[] text2 = "左边距0点,打印区域384点宽,右间距0点,居中测试".getBytes("gb2312");
                    byte[] text3 = "左边距0点,打印区域384点宽,右间距8点,居中测试".getBytes("gb2312");
                    byte[] text4 = "左边距0点,打印区域384点宽,右间距0点,右对齐测试".getBytes("gb2312");
                    byte[] text5 = "左边距0点,打印区域384点宽,右间距8点,右对齐测试".getBytes("gb2312");
                    byte[] text6 = "左边距24点,打印区域304点宽,右间距24点,左对齐测试".getBytes("gb2312");
                    byte[] text7 = "左边距24点,打印区域304点宽,右间距24点,居中测试".getBytes("gb2312");
                    byte[] text8 = "左边距24点,打印区域304点宽,右间距24点,右对齐测试".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, fontSize0, lineH0, align0, leftMargin0, printarea0, rightSpace0, text0,
                            printarea0, rightSpace1, text1, align1, leftMargin0, printarea0, rightSpace0, text2, leftMargin0, printarea0, rightSpace1, text3,
                            align2, leftMargin0, printarea0, rightSpace0, text4, leftMargin0, printarea0, rightSpace1, text5, leftMargin1, printarea1,
                            rightSpace3, align0, text6, leftMargin1, printarea1, rightSpace3, align1, text7, leftMargin1, printarea1, rightSpace3, align2, text8,
                            fontSize1, lineH1, align0, leftMargin0, printarea0, rightSpace0, text0,
                            printarea0, rightSpace1, text1, align1, leftMargin0, printarea0, rightSpace0, text2, leftMargin0, printarea0, rightSpace1, text3,
                            align2, leftMargin0, printarea0, rightSpace0, text4, leftMargin0, printarea0, rightSpace1, text5, leftMargin1, printarea1,
                            rightSpace3, align0, text6, leftMargin1, printarea1, rightSpace3, align1, text7, leftMargin1, printarea1, rightSpace3, align2, text8,
                            fontSize2, lineH2, align0, leftMargin0, printarea0, rightSpace0, text0,
                            printarea0, rightSpace1, text1, align1, leftMargin0, printarea0, rightSpace0, text2, leftMargin0, printarea0, rightSpace1, text3,
                            align2, leftMargin0, printarea0, rightSpace0, text4, leftMargin0, printarea0, rightSpace1, text5, leftMargin1, printarea1,
                            rightSpace3, align0, text6, leftMargin1, printarea1, rightSpace3, align1, text7, leftMargin1, printarea1, rightSpace3, align2, text8,
                            fontSize3, lineH3, align0, leftMargin0, printarea0, rightSpace0, text0,
                            printarea0, rightSpace1, text1, align1, leftMargin0, printarea0, rightSpace0, text2, leftMargin0, printarea0, rightSpace1, text3,
                            align2, leftMargin0, printarea0, rightSpace0, text4, leftMargin0, printarea0, rightSpace1, text5, leftMargin1, printarea1,
                            rightSpace3, align0, text6, leftMargin1, printarea1, rightSpace3, align1, text7, leftMargin1, printarea1, rightSpace3, align2, text8,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    private void printRelativePositionTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(24);
                    byte[] printarea0 = ESCUtil.printAreaWidth(384);
                    byte[] printarea1 = ESCUtil.printAreaWidth(304);
                    byte[] rightSpace0 = ESCUtil.setRightSpaceChar((byte) 0);
                    byte[] rightSpace1 = ESCUtil.setRightSpaceChar((byte) 8);
                    byte[] rightSpace3 = ESCUtil.setRightSpaceChar((byte) 24);
                    byte[] relative1 = ESCUtil.relativePrintPosition(24);
                    byte[] relative2 = ESCUtil.relativePrintPosition(48);
                    byte[] relative3 = ESCUtil.relativePrintPosition(96);
                    byte[] text0 = "右间距0点".getBytes("gb2312");
                    byte[] text1 = "相对位置24点测试".getBytes("gb2312");
                    byte[] text2 = "右间距0点".getBytes("gb2312");
                    byte[] text3 = "相对位置48点测试".getBytes("gb2312");
                    byte[] text4 = "右间距8点".getBytes("gb2312");
                    byte[] text5 = "相对位置48测试".getBytes("gb2312");
                    byte[] text6 = "右间距24点".getBytes("gb2312");
                    byte[] text7 = "相对位置96测试".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, fontSize0, lineH0, align0, leftMargin0, printarea0, rightSpace0, text0,
                            relative1, text1, leftMargin0, text2, relative2, text3, leftMargin1, rightSpace1, text4, relative2, text5, leftMargin1,
                            printarea1, rightSpace3, text6, relative3, text7,
                            fontSize1, lineH1, align0, leftMargin0, printarea0, rightSpace0, text0,
                            relative1, text1, leftMargin0, text2, relative2, text3, leftMargin1, rightSpace1, text4, relative2, text5, leftMargin1,
                            fontSize1, lineH1, align0, leftMargin0, printarea0, rightSpace0, text0,
                            relative1, relative1, text1, relative2, leftMargin0, text2, relative2, text3, leftMargin1, rightSpace1, text4, relative2, text5, leftMargin1,
                            printarea1, rightSpace3, text6, relative3, text7,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printAbsolutePositionTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(16);
                    byte[] absolute0 = ESCUtil.absolutePrintPosition(0);
                    byte[] absolute1 = ESCUtil.absolutePrintPosition(96);
                    byte[] text0 = "绝对打印位置0点测试".getBytes("gb2312");
                    byte[] text1 = "绝对打印位置96点测试".getBytes("gb2312");
                    byte[] text2 = "绝对打印位置0点测试".getBytes("gb2312");
                    byte[] text3 = "绝对打印位置96点测试".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, fontSize0, lineH0, align0, leftMargin0, absolute0, text0, absolute1, text1,
                            leftMargin1, absolute0, text2, leftMargin1, absolute1, text3,
                            fontSize1, lineH1, align0, leftMargin0, absolute0, text0, absolute1, text1,
                            leftMargin1, absolute0, text2, leftMargin1, absolute1, text3,
                            fontSize2, lineH2, align0, leftMargin0, absolute0, text0, absolute1, text1,
                            leftMargin1, absolute0, text2, leftMargin1, absolute1, text3,
                            fontSize3, lineH3, align0, leftMargin0, absolute0, text0, absolute1, text1,
                            leftMargin1, absolute0, text2, leftMargin1, absolute1, text3,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printTabTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] tabPosition = new byte[]{4, 6, 10};
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(16);
                    byte[] absolute0 = ESCUtil.absolutePrintPosition(8);
                    byte[] absolute1 = ESCUtil.absolutePrintPosition(24);
                    byte[] tabSet = ESCUtil.set_HT_position(tabPosition);
                    byte[] Tab = ESCUtil.HTCmd();
                    byte[] text0 = "跳格".getBytes("gb2312");
                    byte[] text1 = "4个ascii字符".getBytes("gb2312");
                    byte[] text2 = "6个ascii字符".getBytes("gb2312");
                    byte[] text3 = "10个ascii字符".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, fontSize0, lineH0, leftMargin0,
                            tabSet, text0, Tab, text1, leftMargin0, text0, Tab, text2, leftMargin0, text0, Tab, text3,
                            fontSize0, lineH0, leftMargin0,
                            tabSet, text0, Tab, Tab, text1, leftMargin0, text0, text2, leftMargin0, text0, Tab, text3,
                            fontSize1, lineH1, leftMargin0,
                            tabSet, text0, Tab, text1, leftMargin0, text0, Tab, text2, leftMargin0, text0, Tab, text3,
                            fontSize2, lineH2, leftMargin0,
                            tabSet, text0, Tab, text1, leftMargin0, text0, Tab, text2, leftMargin0, text0, Tab, text3,
                            fontSize3, lineH3, leftMargin0,
                            tabSet, text0, Tab, text1, leftMargin0, text0, Tab, text2, leftMargin0, text0, Tab, text3,

                            fontSize0, lineH0, leftMargin1,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,
                            fontSize1, lineH1, leftMargin1,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,
                            fontSize2, lineH2, leftMargin1,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,
                            fontSize3, lineH3, leftMargin1,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,

                            fontSize0, lineH0, leftMargin1, absolute0,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,
                            fontSize1, lineH1, leftMargin1, absolute0,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,
                            fontSize2, lineH2, leftMargin1, absolute0,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,
                            fontSize3, lineH3, leftMargin1, absolute0,
                            tabSet, text0, Tab, text1, leftMargin1, text0, Tab, text2, leftMargin1, text0, Tab, text3,

                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printUnderlineTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] tabPosition = new byte[]{8, 12, 18};
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(16);
                    byte[] absolute0 = ESCUtil.absolutePrintPosition(8);
                    byte[] absolute1 = ESCUtil.absolutePrintPosition(24);
                    byte[] tabSet = ESCUtil.set_HT_position(tabPosition);
                    byte[] Tab = ESCUtil.HTCmd();
                    byte[] underlineWidth1 = ESCUtil.underlineWithWidthOn((byte) 1);
                    byte[] underlineWidth2 = ESCUtil.underlineWithWidthOn((byte) 2);
                    byte[] underlineEn = ESCUtil.printUnderlineModeEn(true);
                    byte[] underlineDisable = ESCUtil.printUnderlineModeEn(false);
                    byte[] relative2 = ESCUtil.relativePrintPosition(48);
                    byte[] text0 = "下划线".getBytes("gb2312");
                    byte[] text1 = "ABC".getBytes("gb2312");
                    byte[] text2 = "123".getBytes("gb2312");
                    byte[] text3 = "下划线".getBytes("gb2312");
                    byte[] text4 = "测试".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, underlineWidth1, fontSize0, lineH0, leftMargin0,
                            tabSet, text0, underlineEn, underlineEn, text1, text2, underlineDisable, text3, underlineEn, text4, underlineDisable,
                            text0, text1, underlineEn, text2, underlineDisable, text3, underlineEn, text4, underlineDisable,
                            fontSize0, lineH0, text0, Tab, relative2, underlineEn, relative2, text1, text2, underlineDisable, text3, underlineEn, relative2, text4, underlineDisable,
                            fontSize1, lineH1, text0, underlineEn, text1, Tab, text2, underlineDisable, text3, underlineEn, relative2, text4, underlineDisable,
                            fontSize1, lineH1, text0, underlineEn, text1, Tab, text2, underlineDisable, text3, underlineEn, relative2, text4, underlineDisable,
                            fontSize2, lineH1, text0, underlineEn, text1, relative2, text2, underlineDisable, text3, underlineEn, text1, text4, underlineDisable,
                            tabSet, fontSize3, lineH1, text0, Tab, underlineEn, Tab, text1, text2, underlineDisable, text4, text3, relative2, underlineEn, text1, relative2, underlineDisable, text4,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    
    private void printBarcodeTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                byte[] printer_init = ESCUtil.init_printer();
                byte[] selectChinese = ESCUtil.selectChineseMode();
                byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                byte[] align0 = ESCUtil.alignMode((byte) 0);
                byte[] align1 = ESCUtil.alignMode((byte) 1);
                byte[] align2 = ESCUtil.alignMode((byte) 2);
                byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                byte[] leftMargin1 = ESCUtil.printLeftMargin(16);
                byte[] printBarcode = ESCUtil.barcodePrint();
                String barcodeContent0 = "20171218115";
                String barcodeContent1 = "692015246102";
                String barcodeContent2 = "2017121";
                String barcodeContent3 = "0123456789";
                byte[] nexLine32 = ESCUtil.nextLines(1);
                byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                byte[][] cmdBytes = {printer_init, selectChinese, charCode, align1, leftMargin0,
                        ESCUtil.setHRIPosition(3), ESCUtil.setBarcodeHeight(5), ESCUtil.setBarcodeWidth(12),
                        printBarcode, ESCUtil.barcodeData(0, barcodeContent0), nexLine32,
                        printBarcode, ESCUtil.barcodeData(2, barcodeContent1), nexLine32,
                        printBarcode, ESCUtil.barcodeData(3, barcodeContent2), nexLine32, printBarcode, ESCUtil.barcodeData(4, barcodeContent3), nexLine32, printBarcode, ESCUtil.barcodeData(5, barcodeContent3), nexLine32,
                        printBarcode, ESCUtil.barcodeData(6, barcodeContent3), nexLine32, printBarcode, ESCUtil.barcodeData(65, barcodeContent0), nexLine32,
                        printBarcode, ESCUtil.barcodeData(67, barcodeContent1), nexLine32, printBarcode, ESCUtil.barcodeData(68, barcodeContent2), nexLine32, printBarcode, ESCUtil.barcodeData(69, barcodeContent3), nexLine32,
                        printBarcode, ESCUtil.barcodeData(70, barcodeContent3), nexLine32, printBarcode, ESCUtil.barcodeData(71, barcodeContent3), nexLine32, printBarcode, ESCUtil.barcodeData(73, barcodeContent3), nexLine32,
                        performPrint};
                try {
                    if ((socket == null) || (!socket.isConnected())) {
                        socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                    }
                    byte[] data = ESCUtil.byteMerger(cmdBytes);
                    OutputStream out = socket.getOutputStream();
                    out.write(data, 0, data.length);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printQRcodeTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                    byte[] leftMargin1 = ESCUtil.printLeftMargin(32);
                    byte[] QRcodeData = "http://www.baidu.com".getBytes("gb2312");
                    byte[] nexLine32 = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode,
                            align0, leftMargin1, ESCUtil.setQRsize(12), ESCUtil.setQRCorrectionLevel(48), ESCUtil.cacheQRData(QRcodeData),
                            align0, leftMargin0, ESCUtil.setQRsize(6), ESCUtil.setQRCorrectionLevel(49), ESCUtil.cacheQRData(QRcodeData),
                            align0, leftMargin1, ESCUtil.setQRsize(6), ESCUtil.setQRCorrectionLevel(50), ESCUtil.cacheQRData(QRcodeData),
                            align1, leftMargin0, ESCUtil.setQRsize(6), ESCUtil.setQRCorrectionLevel(51), ESCUtil.cacheQRData(QRcodeData),
                            align1, leftMargin1, ESCUtil.setQRsize(6), ESCUtil.setQRCorrectionLevel(50), ESCUtil.cacheQRData(QRcodeData),
                            align2, leftMargin0, ESCUtil.setQRsize(6), ESCUtil.setQRCorrectionLevel(49), ESCUtil.cacheQRData(QRcodeData),
                            align2, leftMargin1, ESCUtil.setQRsize(6), ESCUtil.setQRCorrectionLevel(48), ESCUtil.cacheQRData(QRcodeData),
                            //ESCUtil.printCacheQRdata(),
                            performPrint
                    };
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void printImage(String myImageData){
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                //Log.d(TAG,myImageData);
                byte[] imageAsBytes = Base64.decode(myImageData, Base64.DEFAULT);
                Bitmap bp = BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);

                byte[][] cmdBytes = {
                        ESCUtil.init_printer(),
                        ESCUtil.alignMode((byte) 1),
                        ESCUtil.printAreaWidth(384),
                        BitMapUtil.getRasterBmpData(bp, 384, 0),
                        ESCUtil.performPrintAndFeedPaper((byte) 50)
                };

                try {
                    if ((socket == null) || (!socket.isConnected())) {
                        socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                    }
                    byte[] data = ESCUtil.byteMerger(cmdBytes);
                    OutputStream out = socket.getOutputStream();
                    out.write(data, 0, data.length);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printBitmapTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                Bitmap mBitmap1 = BitmapFactory.decodeResource(cordova.getActivity().getResources(), R.mipmap.test_p);
                Bitmap mBitmap2 = BitmapFactory.decodeResource(cordova.getActivity().getResources(), R.mipmap.test);

                byte[] printer_init = ESCUtil.init_printer();
                byte[] selectChinese = ESCUtil.selectChineseMode();
                byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                byte[] align0 = ESCUtil.alignMode((byte) 0);
                byte[] align1 = ESCUtil.alignMode((byte) 1);
                byte[] align2 = ESCUtil.alignMode((byte) 2);
                byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                byte[] leftMargin1 = ESCUtil.printLeftMargin(32);
                byte[] leftMargin2 = ESCUtil.printLeftMargin(48);
                byte[] leftMargin3 = ESCUtil.printLeftMargin(80);
                byte[] area = ESCUtil.printAreaWidth(192);
                byte[] area1 = ESCUtil.printAreaWidth(384);
                byte[] nexLine = ESCUtil.nextLines(1);
                byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                byte[][] cmdBytes = {printer_init, selectChinese, charCode,
                        align0, leftMargin0, area, BitMapUtil.getBitmapPrintData(mBitmap2, 128, 0),
                        align1, leftMargin1, BitMapUtil.getBitmapPrintData(mBitmap2, 128, 1),
                        align2, leftMargin2, area1, BitMapUtil.getBitmapPrintData(mBitmap2, 128, 32),
                        align2, leftMargin0, BitMapUtil.getBitmapPrintData(mBitmap2, 128, 33),
                        align1, leftMargin2, BitMapUtil.getBitmapPrintData(mBitmap2, 128, 33),
                };
                byte[][] cmdBytes1 = {align0, leftMargin3, BitMapUtil.getBitmapPrintData(mBitmap1, 128, 0),
                        align0, leftMargin1, BitMapUtil.getBitmapPrintData(mBitmap1, 128, 1),
                        align0, leftMargin0, BitMapUtil.getBitmapPrintData(mBitmap1, 256, 0),
                        align1, leftMargin1, BitMapUtil.getBitmapPrintData(mBitmap1, 320, 1),
                        performPrint
                };
                try {
                    if ((socket == null) || (!socket.isConnected())) {
                        socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                    }
                    byte[] data = ESCUtil.byteMerger(cmdBytes);
                    OutputStream out = socket.getOutputStream();
                    out.write(data, 0, data.length);
                    byte[] data1 = ESCUtil.byteMerger(cmdBytes1);
                    out.write(data1, 0, data1.length);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printRasterBmpTest() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                Bitmap mBitmap1 = BitmapFactory.decodeResource(cordova.getActivity().getResources(), R.mipmap.test);
                Bitmap mBitmap2 = BitmapFactory.decodeResource(cordova.getActivity().getResources(), R.mipmap.test_r);
                byte[] printer_init = ESCUtil.init_printer();
                byte[] selectChinese = ESCUtil.selectChineseMode();
                byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                byte[] align0 = ESCUtil.alignMode((byte) 0);
                byte[] align1 = ESCUtil.alignMode((byte) 1);
                byte[] align2 = ESCUtil.alignMode((byte) 2);
                byte[] leftMargin0 = ESCUtil.printLeftMargin(0);
                byte[] leftMargin1 = ESCUtil.printLeftMargin(32);
                byte[] leftMargin2 = ESCUtil.printLeftMargin(48);
                byte[] leftMargin3 = ESCUtil.printLeftMargin(80);
                byte[] area = ESCUtil.printAreaWidth(192);
                byte[] area1 = ESCUtil.printAreaWidth(384);
                byte[] nexLine = ESCUtil.nextLines(1);
                byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                byte[][] cmdBytes = {printer_init, selectChinese, charCode,
                        align0, leftMargin0, area, BitMapUtil.getRasterBmpData(mBitmap1, 128, 0),
                        align0, leftMargin1, BitMapUtil.getRasterBmpData(mBitmap1, 128, 1),
                        align1, leftMargin0, area1, BitMapUtil.getRasterBmpData(mBitmap1, 128, 2),
                        align0, leftMargin1, BitMapUtil.getRasterBmpData(mBitmap1, 128, 1),
                        align2, leftMargin0, BitMapUtil.getRasterBmpData(mBitmap1, 128, 3),
                        align0, leftMargin1, BitMapUtil.getRasterBmpData(mBitmap2, 128, 0),
                        align1, leftMargin1, BitMapUtil.getRasterBmpData(mBitmap2, 128, 0),
                        performPrint
                };
                try {
                    if ((socket == null) || (!socket.isConnected())) {
                        socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                    }
                    byte[] data = ESCUtil.byteMerger(cmdBytes);
                    OutputStream out = socket.getOutputStream();
                    out.write(data, 0, data.length);
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    private void printKoubeiBill() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] text0 = "   #4口碑外卖\n".getBytes("gb2312");
                    byte[] text1 = "         冯记黄焖鸡米饭\n".getBytes("gb2312");
                    byte[] text2 = "17:20 尽快送达\n".getBytes("gb2312");
                    byte[] text3 = "--------------------------------\n".getBytes("gb2312");
                    byte[] text4 = "18610858337韦小宝创智天地广场7号楼(605室)".getBytes("gb2312");
                    byte[] text5 = "下单: 16:35\n".getBytes("gb2312");
                    byte[] text6 = "********************************\n".getBytes("gb2312");
                    byte[] text7 = "菜品          数量   单价   金额\n".getBytes("gb2312");
                    byte[] text8 = "黄焖五花肉 (大) (不辣)\n".getBytes("gb2312");
                    byte[] text9 = "               1      25      25\n".getBytes("gb2312");
                    byte[] text10 = "黄焖五花肉 (小) (不辣)\n".getBytes("gb2312");
                    byte[] text11 = "黄焖五花肉 (小) (微辣)\n".getBytes("gb2312");
                    byte[] text12 = "配送费                         2\n".getBytes("gb2312");
                    byte[] text13 = "            实付金额: 27\n\n".getBytes("gb2312");
                    byte[] text14 = "    口碑外卖\n\n\n".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, align0, fontSize3, lineH3, text0,
                            fontSize1, lineH1, text1, text6,
                            fontSize3, lineH3, text2,
                            fontSize1, lineH1, text3,
                            fontSize3, lineH3, text4,
                            fontSize1, lineH1, text3,
                            fontSize3, lineH3, text5,
                            fontSize1, lineH1, text6, text7, text3, text8, text9, text10, text9, text11, text9, text3, text12, text3,
                            fontSize2, lineH2, text13,
                            fontSize3, lineH3, text14,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printBaiduBill() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    String Baidu = "本店留存\n************************\n      百度外卖\n      [货到付款]\n" +
                            "************************\n期望送达时间：立即配送\n" +
                            "订单备注:送到西门,不要辣\n发票信息:百度外卖\n************************\n下单编号: 14187186911689\n下单时间: " +
                            "2014-12-16 16:31************************\n" +
                            "菜品名称     数量  金额\n------------------------\n" +
                            "香辣面套餐     1   40.00\n素食天线汉堡   1   38.00\n香辣面套餐     1   40.00\n" +
                            "素食天线汉堡   1   38.00\n香辣面         1   43.00\n" +
                            "素食天线       1   34.00\n" +
                            "------------------------\n" +
                            "************************\n姓名:百度测试\n" +
                            "地址:泰然工贸园\n电话:18665248965\n" +
                            "************************\n百度测试商户\n" +
                            "18665248965\n#15 百度外卖 11月09号 \n\n\n";
                    byte[] BaiduData = Baidu.getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, align0, fontSize2, lineH2, BaiduData,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printMeituanBill() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    byte[] align2 = ESCUtil.alignMode((byte) 2);
                    byte[] text0 = "  #1  美团测试\n\n".getBytes("gb2312");
                    byte[] text1 = "      粤香港式烧腊(第1联)\n\n".getBytes("gb2312");
                    byte[] text2 = "------------------------\n\n*********预订单*********\n".getBytes("gb2312");
                    byte[] text3 = "--------------------------------\n".getBytes("gb2312");
                    byte[] text4 = "  期望送达时间:[18:00]\n\n".getBytes("gb2312");
                    byte[] text5 = "备注: 别太辣\n".getBytes("gb2312");
                    byte[] text6 = "菜品          数量   小计金额\n".getBytes("gb2312");
                    byte[] text7 = "红烧肉          X1    12\n红烧肉1         X1    12\n红烧肉2         X1    12\n".getBytes("gb2312");
                    byte[] text8 = "配送费                         5\n".getBytes("gb2312");
                    byte[] text9 = "餐盒费                         1\n".getBytes("gb2312");
                    byte[] text10 = "[超时赔付] - 详见订单\n".getBytes("gb2312");
                    byte[] text11 = "可口可乐: x1\n".getBytes("gb2312");
                    byte[] text12 = "合计                18元\n".getBytes("gb2312");
                    byte[] text13 = "张* 18312345678\n地址信息\n".getBytes("gb2312");
                    byte[] text14 = "  #1  美团测试\n\n\n".getBytes("gb2312");
                    byte[] text15 = "下单时间: 01-01 12:00".getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, align0,
                            fontSize3, lineH3, text0,
                            fontSize1, lineH1, text1,
                            fontSize2, lineH2, text2, text4,
                            fontSize1, lineH1, text3, text15,
                            fontSize2, lineH2, text5,
                            fontSize1, lineH1, text6, text3, nextLine,
                            fontSize2, lineH2, text7, nextLine,
                            fontSize1, lineH1, text3,
                            fontSize1, lineH1, text8, text9, text10, text11, text3,
                            fontSize2, lineH2, text12,
                            fontSize1, lineH1, text3,
                            fontSize3, lineH3, text13,
                            fontSize1, lineH1, text3, text14,
                            fontSize3, lineH3,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private void printElemoBill() {
        ThreadPoolManager.getInstance().executeTask(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] printer_init = ESCUtil.init_printer();
                    byte[] selectChinese = ESCUtil.selectChineseMode();
                    byte[] charCode = ESCUtil.selectCharCodeSystem((byte) 0x01);
                    byte[] fontSize0 = ESCUtil.fontSizeSet((byte) 0x00);
                    byte[] fontSize1 = ESCUtil.fontSizeSet((byte) 0x01);
                    byte[] fontSize2 = ESCUtil.fontSizeSet((byte) 0x10);
                    byte[] fontSize3 = ESCUtil.fontSizeSet((byte) 0x11);
                    byte[] lineH0 = ESCUtil.setLineHeight((byte) 16);
                    byte[] lineH1 = ESCUtil.setLineHeight((byte) 26);
                    byte[] lineH2 = ESCUtil.setLineHeight((byte) 33);
                    byte[] lineH3 = ESCUtil.setLineHeight((byte) 50);
                    byte[] align0 = ESCUtil.alignMode((byte) 0);
                    byte[] align1 = ESCUtil.alignMode((byte) 1);
                    String Elemo = "****#1饿了么外卖订单****\n\n        卡萨披萨       \n\n       --已支付--      \n\n" +
                            "      预计19:00送达     \n\n[时间]:2014-12-03 16:21\n\n  不吃辣 辣一点 多加米\n\n " +
                            "[发票]这是一个发票抬头\n\n------------------------\n\n菜名          数量  " +
                            "小计\n\n--------1号篮子---------\n\n测试美食一        X4   4\n\n" +
                            "测试美食二        X6   6\n\n测试美食三        X2   2\n\n" +
                            "--------2号篮子---------\n\n" +
                            "测试1             X1   1\n\n测试2             X1   1\n\n" +
                            "测试3             X1  23\n\n(+)测试西式甜点   X1   1\n\n" +
                            "(+)测试酸辣       X1   1\n\n--------3号篮子---------\n\n" +
                            "测试菜品名字很长很长很长\n\n测试              X1   1\n\n--------其它费用--------\n\n配送费\n\n\n";
                    byte[] ElemoData = Elemo.getBytes("gb2312");
                    byte[] nextLine = ESCUtil.nextLines(1);
                    byte[] performPrint = ESCUtil.performPrintAndFeedPaper((byte) 160);

                    byte[][] cmdBytes = {printer_init, selectChinese, charCode, align0, fontSize2, lineH2, ElemoData,
                            performPrint};
                    try {
                        if ((socket == null) || (!socket.isConnected())) {
                            socket = BluetoothUtil.getSocket(mBluetoothPrinterDevice);
                        }
                        byte[] data = ESCUtil.byteMerger(cmdBytes);
                        OutputStream out = socket.getOutputStream();
                        out.write(data, 0, data.length);
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });
    }
    */
}
