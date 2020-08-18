//
// Created by msi on 2020-06-07.
//

#include "main.h"



#include <opencv2/opencv.hpp>


using namespace cv;



//extern "C"{
//
//JNIEXPORT void JNICALL
//Java_com_techtown_ainglish_OpenCVtest_ConvertRGBtoGray(
//        JNIEnv *env,
//jobject  instance,
//        jlong matAddrInput,
//jlong matAddrResult){
//
//
//Mat &matInput = *(Mat *)matAddrInput;
//Mat &matResult = *(Mat *)matAddrResult;
//
//cvtColor(matInput, matResult, COLOR_RGBA2GRAY);
//
//
//}
//}

extern "C"
JNIEXPORT void JNICALL
Java_com_techtown_ainglish_OpenCVtestSecond_imageprocessing(JNIEnv *env,
                                                                           jobject instance,
                                                                           jlong inputImage,
                                                                           jlong outputImage,

                                                                           jint th1,

                                                                           jint th2) {



    Mat &img_input = *(Mat *) inputImage;

    Mat &img_output = *(Mat *) outputImage;



    cvtColor( img_input, img_output, COLOR_RGB2GRAY);


    blur( img_output, img_output, Size(5,5) );

    Canny( img_output, img_output, th1, th2);


}