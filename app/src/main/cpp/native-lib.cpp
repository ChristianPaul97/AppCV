#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/dnn.hpp>
#include <opencv2/video.hpp>
#include <opencv2/features2d.hpp>
#include "android/bitmap.h"
#include <vector>
#include <string>
#include <opencv2/core/mat.hpp>
#include <opencv2/opencv.hpp>
//tensor



using namespace cv;

void bitmapToMat(JNIEnv * env, jobject bitmap, cv::Mat &dst, jboolean
needUnPremultiplyAlpha){
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, cv::COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, cv::COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
        //if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}
void matToBitmap(JNIEnv * env, cv::Mat src, jobject bitmap, jboolean needPremultiplyAlpha) {
    AndroidBitmapInfo info;
    void* pixels = 0;
    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width ==
                                                                         (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, cv::COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, cv::COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, cv::COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, cv::COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        //jclass je = env->FindClass("org/opencv/core/CvException");
        jclass je = env->FindClass("java/lang/Exception");
        //if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;

    }

}
extern "C" JNIEXPORT jstring JNICALL
Java_ec_edu_ups_appcv_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_aEscalaGrises(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    //cv::flip(src, src, 0);
    cv::Mat tmp;
    cv::cvtColor(src, tmp, cv::COLOR_BGR2GRAY);
    matToBitmap(env, tmp, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_aplicarDesenfoque(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::Mat blurred;
    cv::GaussianBlur(src, blurred, cv::Size(15, 15), 0); // tamaño del kernel

    matToBitmap(env, blurred, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_detectarContornos(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::Mat gray;
    cv::cvtColor(src, gray, cv::COLOR_BGR2GRAY);

    cv::Mat canny;
    cv::Canny(gray, canny, 100, 200); //  ajustar los valores de umbral

    std::vector<std::vector<cv::Point>> contours;
    cv::findContours(canny, contours, cv::RETR_EXTERNAL, cv::CHAIN_APPROX_SIMPLE);

    cv::drawContours(src, contours, -1, cv::Scalar(0, 255, 0), 2); //contornos en la imagen original

    matToBitmap(env, src, bitmapOut, false);
}


extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_mejorarContraste(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::Mat lab;
    cv::cvtColor(src, lab, cv::COLOR_BGR2Lab);

    std::vector<cv::Mat> labChannels;
    cv::split(lab, labChannels);

    cv::Ptr<cv::CLAHE> clahe = cv::createCLAHE();
    clahe->setClipLimit(2.0); // Puedes ajustar el límite de recorte según tus necesidades

    cv::Mat enhanced;
    clahe->apply(labChannels[0], enhanced);

    enhanced.copyTo(labChannels[0]);
    cv::merge(labChannels, lab);

    cv::Mat result;
    cv::cvtColor(lab, result, cv::COLOR_Lab2BGR);

    matToBitmap(env, result, bitmapOut, false);
}

extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_detectarMovimiento(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat prevFrame;
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::Mat gray;
    cv::cvtColor(src, gray, cv::COLOR_BGR2GRAY);

    if (prevFrame.empty()) {
        // Si no hay fotograma anterior, lo asignamos y salimos del método
        prevFrame = gray.clone();
        matToBitmap(env, src, bitmapOut, false);
        return;
    }

    cv::Mat frameDiff;
    cv::absdiff(prevFrame, gray, frameDiff); // Diferencia absoluta entre el fotograma actual y el anterior

    cv::Mat thresholded;
    cv::threshold(frameDiff, thresholded, 30, 255, cv::THRESH_BINARY); // Umbralización para obtener una imagen binaria

    cv::Mat result = cv::Mat::zeros(src.size(), CV_8UC3); // Matriz de salida inicializada en negro

    src.copyTo(result, thresholded); // Copiar los píxeles de src solo donde hay movimiento (thresholded es blanco)

    prevFrame = gray.clone(); // Actualizar el fotograma anterior

    matToBitmap(env, result, bitmapOut, false);
}


//histograma
extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_aHistogramaE(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {

    Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    Mat tmp;
    cvtColor(src, tmp, COLOR_BGR2GRAY);

    // Equalizar el histograma
    equalizeHist(tmp, tmp);

    matToBitmap(env, tmp, bitmapOut, false);
}


extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_detectarColor(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, true);

    cv::Mat hsv;
    cv::cvtColor(src, hsv, cv::COLOR_BGR2HSV);

    cv::Scalar lowerBound(0, 50, 50); // Valores mínimos del rango de color (en formato HSV)
    cv::Scalar upperBound(10, 255, 255); // Valores máximos del rango de color (en formato HSV)

    cv::Mat mask;
    cv::inRange(hsv, lowerBound, upperBound, mask);

    cv::Mat result;
    src.copyTo(result, mask);

    matToBitmap(env, result, bitmapOut, false);
}


extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_detectarCirculo(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::Mat gray;
    cv::cvtColor(src, gray, cv::COLOR_BGR2GRAY);

    cv::GaussianBlur(gray, gray, cv::Size(5, 5), 0); // Aplicar un filtro Gaussiano para suavizar la imagen

    std::vector<cv::Vec3f> circles;
    cv::HoughCircles(gray, circles, cv::HOUGH_GRADIENT, 1, gray.rows / 8, 100, 30, 10, 100); // Detección de círculos utilizando la transformada de Hough

    for (const auto& circle : circles) {
        cv::Point center(cvRound(circle[0]), cvRound(circle[1])); // Obtener el centro del círculo
        int radius = cvRound(circle[2]); // Obtener el radio del círculo

        cv::circle(src, center, radius, cv::Scalar(0, 255, 0), 2); // Dibujar el círculo en la imagen original
    }

    matToBitmap(env, src, bitmapOut, false);
}


extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_detectarBordesLaplaciano(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::Mat gray;
    cv::cvtColor(src, gray, cv::COLOR_RGBA2GRAY);

    cv::Mat laplacian;
    cv::Laplacian(gray, laplacian, CV_8U);

    cv::Mat edges;
    cv::threshold(laplacian, edges, 30, 255, cv::THRESH_BINARY);

    matToBitmap(env, edges, bitmapOut, true);
}



extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_Dectector(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmapIn,
        jobject bitmapOut) {

    Mat*  mGray = (Mat*)bitmapIn;
    Mat*  mRGBA = (Mat*)bitmapOut;

    std::vector<Point2f> corners;
    goodFeaturesToTrack(*mGray, corners, 20, 0.01, 10,Mat(),3, false, 0.04);

    for(int i=0; i<corners.size();i++){
        circle(*mRGBA, corners[i],10, Scalar(0,255,0),2);
    }

}







/*
extern "C" void NeuralNetworkInference(float* input, float* output) {
    // Cargar el modelo TFLite
    const char* modelPath = "model.tflite";
    std::unique_ptr<tflite::FlatBufferModel> model =
            tflite::FlatBufferModel::BuildFromFile(modelPath);

    // Configurar el intérprete de TensorFlow Lite
    tflite::ops::builtin::BuiltinOpResolver resolver;
    tflite::InterpreterBuilder builder(*model, resolver);
    std::unique_ptr<tflite::Interpreter> interpreter;
    builder(&interpreter);

    // Asignar tensores de entrada y salida
    interpreter->AllocateTensors();
    float* inputTensor = interpreter->typed_input_tensor<float>(0);
    float* outputTensor = interpreter->typed_output_tensor<float>(0);

    // Asignar los valores de entrada
    memcpy(inputTensor, input, inputSize * sizeof(float));

    // Ejecutar la inferencia
    interpreter->Invoke();

    // Obtener los resultados de la inferencia
    memcpy(output, outputTensor, outputSize * sizeof(float));
}
*/
/*
extern "C" JNIEXPORT void JNICALL
Java_ec_edu_ups_appcv_MainActivity_detectarRostros(
        JNIEnv* env,
        jobject ,
        jobject bitmapIn,
        jobject bitmapOut) {
    cv::Mat src;
    bitmapToMat(env, bitmapIn, src, false);

    cv::Mat gray;
    cv::cvtColor(src, gray, cv::COLOR_BGR2GRAY);

    cv::CascadeClassifier classifier;
    std::string cascadePath = std::string(__FILE__) + "/../haarcascade_frontalface_default.xml"; // Ruta al archivo XML del clasificador de rostros
    if (!classifier.load(cascadePath)) {
        return;
    }

    std::vector<cv::Rect> faces;
    classifier.detectMultiScale(gray, faces, 1.1, 2, 0, cv::Size(30, 30)); // Detección de rostros utilizando el clasificador

    for (const auto& rect : faces) {
        cv::rectangle(src, rect, cv::Scalar(0, 255, 0), 2); // Dibujar un rectángulo alrededor de cada rostro detectado
    }

    matToBitmap(env, src, bitmapOut, false);
}

*/
