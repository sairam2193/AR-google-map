package ng.dat.ar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.opengl.Matrix;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ng.dat.ar.helper.LocationHelper;
import ng.dat.ar.model.ARPoint;

/**
 * Created by ntdat on 1/13/17.
 */

public class AROverlayView extends View {

    Context context;
    private float[] rotatedProjectionMatrix = new float[16];
    private Location currentLocation;
    private List<ARPoint> arPoints;


    public AROverlayView(Context context) {
        super(context);

        this.context = context;

        //Demo points
        arPoints=getCoordinates();
    }

     public ArrayList<ARPoint> getFilteredLocations(Location currentLocation){
         ArrayList<ARPoint> locations = new ArrayList<ARPoint>();
         double latitude = currentLocation.getLatitude();
         double longitude = currentLocation.getLongitude();
         arPoints=getCoordinates();
         Iterator itr =arPoints.iterator();
         ARPoint arPoint = null;
         while(itr.hasNext()) {
             arPoint = (ARPoint)itr.next();
             double distanceValue = Math.acos((Math.sin(Math.toRadians(latitude)) * Math.sin(Math.toRadians(arPoint.getLocation().getLatitude()))) + (Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(arPoint.getLocation().getLatitude())) * Math.cos(Math.toRadians(longitude) - Math.toRadians(arPoint.getLocation().getLongitude())))) * 6371;
           if(distanceValue <2){
                 distanceValue = Math.round(distanceValue*100.0)/100.0;
                 arPoint.setName(arPoint.getName());
                 arPoint.setDistance(String.valueOf(distanceValue).concat("km"));
                 locations.add(arPoint);
             }
         }
         return locations;
     }
     public ArrayList<ARPoint> getCoordinates(){
         return new ArrayList<ARPoint>() {{
    /*add(new ARPoint("Sun Wheel", 16.0404856, 108.2262447, 0));
            add(new ARPoint("Linh Ung Pagoda", 16.1072989, 108.2343984, 0));*/
             add(new ARPoint("HDFC atm", 13.076523, 80.125017, 0));
             add(new ARPoint("ICIC atm", 13.074036, 80.127656, 0));
             add(new ARPoint("KVB bank", 13.072864, 80.125113, 0));
             add(new ARPoint("HDFC bank", 13.074802, 80.122100, 0));
             add(new ARPoint("KVB ATM", 13.069260, 80.223195, 0));
             add(new ARPoint("KOTAK ATM",12.962385,80.245438,0));
             add(new ARPoint("IOB ATM",12.967932,80.240629,0));
             add(new ARPoint("Kotak mahindra bank",12.964859,80.246869,0));
             add(new ARPoint("ICICI ATM",12.966780,80.248387,0));
             add(new ARPoint("Canara bank",12.970598,80.250724,0));
             add(new ARPoint("Deutsche bank",12.972928,80.221487,0));
             add(new ARPoint("DBS ATM",12.981356,80.231828,0));
             add(new ARPoint("ICICI bank",12.947282,80.207186,0));
             add(new ARPoint("SBI bank",12.909747,80.224398,0));
             add(new ARPoint("SBI ATM",13.025164,80.185646,0));
             add(new ARPoint("Sella Branch",13.006480,80.206096,0));
             add(new ARPoint("RBS Bank",12.985714,80.246516,0));
             add(new ARPoint("Banca sella ATM",13.060825, 80.142580,0));
             add(new ARPoint("HDFC Bank",13.068312,80.132550,0));
             add(new ARPoint("ICICI ATM",13.066271,80.137206,0));
             add(new ARPoint("Pinnadi",13.048032,80.138190,0));
             add(new ARPoint("Munadi",13.049309,80.138244,0));
             add(new ARPoint("Left",13.048696,80.137117,0));
             add(new ARPoint("Right",13.048628,80.139080,0));
             add(new ARPoint("ICICI ATM taramani",12.977558,80.243052,0));
             add(new ARPoint("Banca sella atm",12.975075,80.248162,0));
             add(new ARPoint("HSBC Atm",12.980923,80.240424,0));

             //My Location 13.074106, 80.126112

         }};
     }
    public void updateRotatedProjectionMatrix(float[] rotatedProjectionMatrix) {
        this.rotatedProjectionMatrix = rotatedProjectionMatrix;
        this.invalidate();
    }

    public void updateCurrentLocation(Location currentLocation){
        this.currentLocation = currentLocation;
        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (currentLocation == null) {
            return;
        }
        arPoints=getFilteredLocations(currentLocation);
        final int radius = 30;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(60);

        for (int i = 0; i < arPoints.size(); i ++) {
            float[] currentLocationInECEF = LocationHelper.WSG84toECEF(currentLocation);
            float[] pointInECEF = LocationHelper.WSG84toECEF(arPoints.get(i).getLocation());
            float[] pointInENU = LocationHelper.ECEFtoENU(currentLocation, currentLocationInECEF, pointInECEF);

            float[] cameraCoordinateVector = new float[4];
            Matrix.multiplyMV(cameraCoordinateVector, 0, rotatedProjectionMatrix, 0, pointInENU, 0);

            // cameraCoordinateVector[2] is z, that always less than 0 to display on right position
            // if z > 0, the point will display on the opposite
            if (cameraCoordinateVector[2] < 0) {
                float x  = (0.5f + cameraCoordinateVector[0]/cameraCoordinateVector[3]) * canvas.getWidth();
                float y = (0.5f - cameraCoordinateVector[1]/cameraCoordinateVector[3]) * canvas.getHeight();

                canvas.drawCircle(x, y, radius, paint);
                canvas.drawText(arPoints.get(i).getName(), x - (30 * arPoints.get(i).getName().length() / 2), y - 80, paint);
            }
        }
    }
}
