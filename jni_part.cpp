#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv/cv.h>
#include <vector>
#include <string.h>
#include "BlobLabeling.cpp"

using namespace std;
using namespace cv;



extern "C" {
JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba);

JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_FindFeatures(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{
    Mat& mGr  = *(Mat*)addrGray;
    Mat& mRgb = *(Mat*)addrRgba;
    vector<KeyPoint> v;

    Ptr<FeatureDetector> detector = FastFeatureDetector::create(50);
    detector->detect(mGr, v);
    for( unsigned int i = 0; i < v.size(); i++ )
    {
        const KeyPoint& kp = v[i];
        circle(mRgb, Point(kp.pt.x, kp.pt.y), 10, Scalar(255,0,0,255));
    }
}



JNIEXPORT void JNICALL Java_org_opencv_samples_tutorial2_Tutorial2Activity_Labling(JNIEnv*, jobject, jlong addrGray, jlong addrRgba)
{

	 Mat& mRgb = *(Mat*)addrRgba;
	 Mat& mGray = *(Mat*)addrGray;
	 IplImage temp = mGray ;  //Mat -> IplImage
	 IplImage* iplGray = cvCreateImage(cvGetSize(&temp), temp.depth, 1) ;
	 cvThreshold(iplGray, iplGray, 128, 255, CV_THRESH_BINARY_INV); //이진화

	 CBlobLabeling blob;
	 blob.SetParam(iplGray,100) ;
	 blob.DoLabeling() ; //레이블링
		for( int i=0; i<blob.m_nBlobs; i++)
		{
			CvPoint pt1 = cvPoint( blob.m_recBlobs[i].x,
					               blob.m_recBlobs[i].y) ;
			CvPoint pt2 = cvPoint( pt1.x + blob.m_recBlobs[i].width,
					               pt1.y + blob.m_recBlobs[i].height);
			rectangle(mRgb,pt1,pt2,Scalar(0,0,255),2,1,1) ; //검출
		}
		cvReleaseImage(&iplGray) ;
	}
}

