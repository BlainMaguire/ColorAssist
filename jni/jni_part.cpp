#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>
using namespace std;
using namespace cv;

extern "C" {

JNIEXPORT jchar JNICALL Java_com_blainmaguire_colorassist_ColorAssist_RGB2Hue(JNIEnv*, jobject, jchar red, jchar green, jchar blue);
JNIEXPORT void JNICALL Java_com_blainmaguire_colorassist_ColorAssist_ShowColor(JNIEnv*, jobject, jlong addrRgb, jchar red, jchar green, jchar blue, jint size, jboolean invert);
JNIEXPORT jcharArray JNICALL Java_com_blainmaguire_colorassist_ColorAssist_ColorPicker(JNIEnv* env, jclass cls, jlong addrRgb);
JNIEXPORT void JNICALL Java_com_blainmaguire_colorassist_ColorAssist_Channels(JNIEnv, jobject, jlong addrRgba, jboolean red, jboolean green, jboolean blue, jint size, jboolean invert);

Vec3b rgb2HSV(char red, char green, char blue) {

    unsigned char hue, sat, val;
    
    unsigned char rgb_max = std::max(red,std::max(green, blue));
    unsigned char rgb_min = std::min(red,std::min(green, blue));

    val = rgb_max;
    
    unsigned char chroma = rgb_max-rgb_min;    

    if (rgb_max == 0) {
        hue=0;
        sat=0;
        return Vec3b(hue,sat,val);
    }
    sat = 255*long(rgb_max-rgb_min)/val;
    if (sat == 0) {
        hue=0;
        return Vec3b(hue,sat,val);
    }
    
    /* Compute hue */
    if (rgb_max == red) {
        hue = 0 + 43*(green-blue)/(chroma);
    } else if (rgb_max == green) {
        hue = 85 + 43*(blue-red)/(chroma);
    } else  { //blue
        hue = 171 + 43*(red-green)/(chroma);
    }
    return Vec3b(hue,sat,val);
}

JNIEXPORT jchar JNICALL Java_com_blainmaguire_colorassist_ColorAssist_RGB2Hue(JNIEnv*, jobject, jchar red, jchar green, jchar blue) {
 
    return rgb2HSV(red,green,blue)[0];
    
}

JNIEXPORT void JNICALL Java_com_blainmaguire_colorassist_ColorAssist_ShowColor(JNIEnv*, jobject, jlong addrRgba, jchar red, jchar green, jchar blue, jint size, jboolean invert) {

    Mat mRgb;
    Mat RoiImg;
    Mat& mRgba = *(Mat*)addrRgba;
    if (size==1) {
        RoiImg = mRgba(Rect(mRgba.cols/8,mRgba.rows/8,(mRgba.cols*6/8),(mRgba.rows*6/8)));
    }
    else if (size==2) {
        RoiImg = mRgba(Rect(mRgba.cols/4,mRgba.rows/4,(mRgba.cols/2),(mRgba.rows/2)));
    }
    else {
        RoiImg = mRgba(Rect(mRgba.cols*3/8,mRgba.rows*3/8,(mRgba.cols/4),(mRgba.rows/4)));
    }
    cv::cvtColor(RoiImg , mRgb , CV_RGBA2RGB);

    MatIterator_<Vec3b> itCam = RoiImg.begin<Vec3b>(),
                    itCamEnd = RoiImg.end<Vec3b>();
    
    for(; itCam != itCamEnd; ++itCam) {
        
        Vec3b HSV = rgb2HSV((*itCam)[0], (*itCam)[1], (*itCam)[2]);
        
        float diff = (std::abs((*itCam)[0] - red)    +
                      std::abs((*itCam)[1] - green)  +
                      std::abs((*itCam)[2] - blue) ) / 3.0;
        
        if ((bool) invert) {
            diff = 255 - (char)diff;
        }
        else {
            diff = (char) diff;
        }
        *itCam = Vec3b(diff, diff, diff);
        
    }
    

}

JNIEXPORT jcharArray JNICALL Java_com_blainmaguire_colorassist_ColorAssist_ColorPicker(JNIEnv *env, jclass cls, jlong addrRgba) {

    Mat mRgb;
    Mat& mRgba = *(Mat*)addrRgba;
    Mat RoiImg = mRgba(Rect(mRgba.cols/2-4,mRgba.rows/2-4,9,9));
    cv::cvtColor(RoiImg , mRgb , CV_RGBA2RGB);

    MatIterator_<Vec3b> itCam = RoiImg.begin<Vec3b>(),
                    itCamEnd = RoiImg.end<Vec3b>();

    Scalar color =  mean(mRgb);
    jcharArray result = env->NewCharArray(3);
    
    rectangle(mRgba,
           Point(mRgba.cols/2-5, mRgba.rows/2-5),
           Point(mRgba.cols/2+5, mRgba.rows/2+5),
           Scalar( 0, 0, 0, 255),
           1,
           8 );
    rectangle(mRgba,
           Point(mRgba.cols/2-6, mRgba.rows/2-6),
           Point(mRgba.cols/2+6, mRgba.rows/2+6),
           Scalar(255, 255, 255, 24),
           1,
           8 );
    
    for(; itCam != itCamEnd; ++itCam) {

        *itCam = Vec3b(color.val[0],color.val[1],color.val[2]);
        
    }
    
    jchar resultColor[3];
    for (int i=0; i < 3; i++) {
        resultColor[i] = color.val[i];
    }
    env->SetCharArrayRegion(result, 0, 3, resultColor);
    return result;

}

JNIEXPORT void JNICALL Java_com_blainmaguire_colorassist_ColorAssist_Channels(JNIEnv, jobject, jlong addrRgba, jboolean red, jboolean green, jboolean blue, jint size, jboolean invert) {

    Mat mRgb;
    Mat& mRgba = *(Mat*)addrRgba;
    Mat RoiImg;
    
    if (size==1) {
        RoiImg = mRgba(Rect(mRgba.cols/8,mRgba.rows/8,(mRgba.cols*6/8),(mRgba.rows*6/8)));
    }
    else if (size==2) {
        RoiImg = mRgba(Rect(mRgba.cols/4,mRgba.rows/4,(mRgba.cols/2),(mRgba.rows/2)));
    }
    else {
        RoiImg = mRgba(Rect(mRgba.cols*3/8,mRgba.rows*3/8,(mRgba.cols/4),(mRgba.rows/4)));
    }
    cv::cvtColor(RoiImg , mRgb , CV_RGBA2RGB);

    MatIterator_<Vec3b> itCam = RoiImg.begin<Vec3b>(),
                    itCamEnd = RoiImg.end<Vec3b>();
    
    for(; itCam != itCamEnd; ++itCam) {

        int total = 0;
        int activeChannels = 0;
        
        if ((bool) red ) {
            total += (int)(*itCam)[0];
            activeChannels++;
        }
        
        if ((bool) green ) {
            total += (int)(*itCam)[1];
            activeChannels++;
        }
        
        if ((bool) blue ) {
            total += (int)(*itCam)[2];
            activeChannels++;
        }
        
        if (activeChannels > 1) {
            total /= activeChannels;
        }
        
        if (!(bool) invert) {
            total = 255 - (char)total;
        }
        else {
            total = (char) total;
        }
        
        *itCam = Vec3b(total, total, total);
        
    }

}

}
