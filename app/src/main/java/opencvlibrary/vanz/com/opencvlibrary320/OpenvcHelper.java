package opencvlibrary.vanz.com.opencvlibrary320;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OpenvcHelper {
    public static final String TAG = "==========>";
    private static OpenvcHelper instance = null;
    private static Context mcontext;
    private static List<Point> PointL;
    private static List<Point> PointR;

    private OpenvcHelper() {
    }

    public static OpenvcHelper getInstance(Context context) {
        mcontext = context;
        if(instance == null){
            instance = new OpenvcHelper();
        }
        return instance;
    }

    public String setImage(String uri, List<Point> objp){
        try {
            Mat img_object = Imgcodecs.imread(uri);
            MatOfPoint2f obj = new MatOfPoint2f();
            MatOfPoint2f scene = new MatOfPoint2f();
            List<Point> newObj = new ArrayList<>();
            List<Point> scenep = new ArrayList<>();
            for(Point b : objp){
                newObj.add(new Point(b.x/1.5,b.y/1.5));
            }
//            Log.i(TAG, "setImage: "+newObj);
            obj.fromList(newObj);
            scenep.add(new Point(0, 0));
            scenep.add(new Point(639, 0));
            scenep.add(new Point(639, 479));
            scenep.add(new Point(0, 479));
            scene.fromList(scenep);
            Mat h = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, Calib3d.CALIB_CB_ADAPTIVE_THRESH);

            Mat outMat = new Mat();
            Size size = new Size(640, 480);
            Imgproc.warpPerspective(img_object, outMat, h, size);
            Bitmap bmp_dst = Bitmap.createBitmap(outMat.cols(),outMat.rows(), Bitmap.Config.ARGB_8888);

            Utils.matToBitmap(outMat,bmp_dst);
            bmp_dst = BitmapRgbToBgr(bmp_dst);
            return saveBitmapFile(bmp_dst,uri);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "openVc: "+e);
            return null;
        }
    }
//颜色对换 rbga - rgba
    public Bitmap BitmapRgbToBgr(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int indx = 0;
        int a = 0, r = 0, g = 0, b = 0;
        for (int row = 0; row < height; row++) {
            indx = row * width;
            for (int col = 0; col < width; col++) {
                int pixel = pixels[indx];
                a = (pixel >> 24) & 0xff;
                r = (pixel >> 16) & 0xff;
                g = (pixel >> 8) & 0xff;
                b = pixel & 0xff;
                pixel = ((a & 0xff) << 24) | ((b & 0xff) << 16) | ((g & 0xff) << 8) | (r & 0xff);
                pixels[indx] = pixel;
                indx++;
            }
        }
        bitmap=Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);

        bitmap.setPixels(pixels,0,width,0,0,width,height);
        return bitmap;
    }

    //Bitmap对象保存味图片文件
    public String saveBitmapFile(Bitmap bitmap,String uri){
        File file=new File(uri);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            return uri;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "saveBitmapFile: "+e);
            return null;
        }
    }

}
