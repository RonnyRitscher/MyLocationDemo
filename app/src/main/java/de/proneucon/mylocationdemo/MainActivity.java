package de.proneucon.mylocationdemo;
/*
ZIEL: ...ist es den aktuellen Standort des Users ermitteln
und die informationen darstellen

- in einer ScrollView eine TextView platzieren
 */
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    //MEMBER
    private static final String TAG = MainActivity.class.getSimpleName(); //LOG_TAG

    private static final int REQUEST_PERMISSION_LOCATION = 123; // Variable für den PersissionTest

    private LocationManager locationManager;                    // macht die Netze verfügbar um auf GPS zuzugreifen
    private LocationListener locationListener;
    private String provider;                                    // Angabe des Providers
    private int providerTestTime = 0;                           // (0 nach bedarf) Angabe nach welcher Zeit die Proviver /GPS prüfung erfolgen soll in ms

    private TextView tv_anzeige;
    //-----------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //einbinden der Views
        tv_anzeige = findViewById(R.id.tv_anzeige);


        //PERMISSION-TEST
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_LOCATION );
            }else{
                doIt();
            }
        }else{
            doIt();
        }

    }


    //-----------------------------------
    // PERMISSIONTEST/ABFRAGE:
    //  Prüfen ob das anforderten Permissions funktionieren
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode== REQUEST_PERMISSION_LOCATION
                && grantResults.length>0
                && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            doIt();
        }
    }

    //-----------------------------------
    @SuppressLint("MissingPermission") // damit muss die Permission nicht noch einmal geprüft werden
    @Override
    protected void onResume() {
        // beim drehen oder neustart der Activity::::::
        super.onResume();
        //hier legen wir fest welchn Provider wir nutzen möchten
        locationManager.requestLocationUpdates(
                provider ,              // angabe Provider
                providerTestTime ,      // angabe wie oft (zeitlich) ein update gemacht werden soll
                0.0005F ,     // angabe der mindest Distance für das updaten (update nach x Metern)
                locationListener);      // angabe des Listeners
    }
    //-----------------------------------
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }
    //-----------------------------------
    // Prüfen der Permissions
    private void doIt(){

        // initialisieren des Managers:
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // alle Provider beschaffen die uns zur verfügung stehen:
        List<String> providerList = locationManager.getAllProviders();
        // Liste der Provider iterieren um zu schauen welche uns zur Verfügung steht
        for(String name : providerList){
            tv_anzeige.append("Name: "+name +"\n\t isEnabled: "+ locationManager.isProviderEnabled(name) +"\n");

            LocationProvider locationProvider = locationManager.getProvider(name);
            tv_anzeige.append("\trequires Cell: " + locationProvider.requiresCell() +"\n");
            tv_anzeige.append("\trequires Network: " + locationProvider.requiresNetwork() +"\n");
            tv_anzeige.append("\trequires Satellite: " + locationProvider.requiresSatellite() +"\n");
            tv_anzeige.append("\n");

            /*INFO: die Prüfung greift im Handy auf den letzten Stand zu * ggf im Handy GPS auf hohe Genauigkeit in den Einstellungen setzen*/
        }


        //KRITERIEN für den Besten Provider (für unsere Anforderungen) angeben
        Criteria criteria = new Criteria();

        /*KRITERIUM: GENAUIGKEIT*/
        //criteria.setAccuracy(Criteria.ACCURACY_HIGH);       //Genauigkeit hoch = 3  ->ging nicht auf dem Motorola -> illegalArgument
        //criteria.setAccuracy(Criteria.ACCURACY_COARSE);     //Genauigkeit mittel =2
        //criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);     //Genauigkeit mittel =2
        //criteria.setAccuracy(Criteria.ACCURACY_LOW);        //Genauigkeit niedrig = 1
        criteria.setAccuracy(Criteria.ACCURACY_FINE);       //Genauigkeit niedrig = 1

        /*KRITERIUM: GENAUIGKEIT Höhenunterschied */
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH); //ALTERNATIVE Genauigkeit hoch = 3  (höhenunterschied)

        /*KRITERIUM: ENERGIE-VERBRAUCH*/
        criteria.setPowerRequirement(Criteria.POWER_LOW);       //möglichst wenig Energie verbrauchen
        //criteria.setPowerRequirement(Criteria.POWER_MEDIUM);  //mittlere Energie verbrauchen
        //criteria.setPowerRequirement(Criteria.POWER_HIGH);    //darf viel Energie verbrauchen

        //BESTEN PROVIDER ERMITTELN
        provider = locationManager.getBestProvider(criteria, true); //verende nur den besten Provider mit den angegebenen Kriterien
        //Ausgeben des besten Providers
        tv_anzeige.append("Verwendet: " + provider + "\n");

        //Listener initialisieren:
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {      // wenn die Location sich ändert, dann...
                //
                tv_anzeige.append("Neuer Standort: \n");  //Neuen Standort anzeigen lassen
                if(location != null){
                    tv_anzeige.append(
                            "\tBreite: " + location.getLatitude() +
                            ", Länge: " + location.getLongitude() +
                            "\n"); //Breiten und Längengrad angeben
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {   // wenn der Status sich ändert, dann...

            }

            @Override
            public void onProviderEnabled(String provider) {        // wenn der Provider sich ändert, dann...

            }

            @Override
            public void onProviderDisabled(String provider) {       // wenn der Provider nicht erreichbar ist, dann...

            }
        };


    }


}
