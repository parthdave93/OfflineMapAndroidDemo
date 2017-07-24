package com.parthdave.mapofflinedemo;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.TileOverlay;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.GPXEntry;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPlace;

import org.mapsforge.core.graphics.Color;
import org.mapsforge.core.graphics.GraphicFactory;
import org.mapsforge.core.graphics.Paint;
import org.mapsforge.core.graphics.Style;
import org.mapsforge.core.model.LatLong;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.android.view.MapView;
import org.mapsforge.map.datastore.MapDataStore;
import org.mapsforge.map.layer.cache.TileCache;
import org.mapsforge.map.layer.overlay.Polyline;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.rendertheme.InternalRenderTheme;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
	// name of the map file in the external storage
	private static final String MAP_FILE = "canary_islands.map";
	private TileOverlay tileOverlay;
	private MapView mapView;
	private TileCache tileCache;
	private TileRendererLayer tileRendererLayer;
	private ArrayList<View> visiblePopups = new ArrayList<>();
	
	private String markerObject = "marker";
	
	private GraphHopper hopper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUpMap();
		
		
		getDirections();
	}
	String grassHopperOsmFile ;
	
	private void getDirections() {
		// create one GraphHopper instance
		hopper = new GraphHopper().forMobile();
		hopper.setDataReaderFile(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm");
//		grassHopperOsmFile = new File(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm").getAbsolutePath();
//		hopper.setDataReaderFile(grassHopperOsmFile);
		
		// where to store graphhopper files?
		String grassHopperFolder  = Environment.getExternalStorageDirectory()+"/grasshopper";
		if(!(new File(grassHopperFolder).exists())){
			new File(grassHopperFolder).mkdir();
		}
		hopper.setGraphHopperLocation(grassHopperFolder);
		
		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		hopper.load(grassHopperFolder);
		
		GHRequest req = new GHRequest(28.5743, -16.1657, 28.4288, -16.4060).setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);//setWeighting("fastest");
		GHResponse rsp = hopper.route(req);
		
		// first check for errors
		if(rsp.hasErrors()) {
			// handle them!
			// rsp.getErrors()
			return;
		}
		
		// use the best path, see the GHResponse class for more possibilities.
		/*PathWrapper path = rsp.getBest();
		
		// points, distance in meters and time in millis of the full path
		PointList pointList = path.getPoints();
		double distance = path.getDistance();
		long timeInMs = path.getTime();
		
		InstructionList il = path.getInstructions();
		// iterate over every turn instruction
		for(Instruction instruction : il) {
			instruction.getDistance();
		}
		
		// or get the json
		List<Map<String, Object>> iList = il.createJson();
		
		// or get the result as gpx entries:
		List<GPXEntry> list = il.createGPXList();*/
		
		Polyline polyline = createPolyline(rsp);
		
		this.mapView.getLayerManager().getLayers().add(polyline);
	}
	
	private Polyline createPolyline(GHResponse response)
	{
		GraphicFactory gf=AndroidGraphicFactory.INSTANCE;
		Paint paint=gf.createPaint();
		paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK));
		paint.setStyle(Style.STROKE);
		paint.setDashPathEffect(new float[] { 25, 15 });
		paint.setStrokeWidth(8);
		Polyline line = new Polyline(paint,AndroidGraphicFactory.INSTANCE);
		
		List<LatLong> geoPoints = line.getLatLongs();
		PointList tmp = response.getBest().getPoints();
		for (int i = 0; i < response.getBest().getPoints().getSize(); i++) {
			geoPoints.add(new LatLong(tmp.getLatitude(i), tmp.getLongitude(i)));
		}
		
		return line;
	}
	
	private void setUpMap() {
		AndroidGraphicFactory.createInstance(this.getApplication());
		this.mapView = new MapView(this);
		setContentView(this.mapView);
		this.mapView.setClickable(true);
		this.mapView.getMapScaleBar().setVisible(true);
		this.mapView.setBuiltInZoomControls(true);
		this.tileCache = AndroidUtil.createTileCache(this, "mapcache", mapView.getModel().displayModel.getTileSize(), 1f, this.mapView.getModel().frameBufferModel.getOverdrawFactor());
		
		MapDataStore mapDataStore = new MapFile(new File(Environment.getExternalStorageDirectory(), MAP_FILE));
		this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore, this.mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
		tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);
		this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
		this.mapView.setCenter(new LatLong(28.3778434,-16.5694826));
		createPositionMarker(28.3778434,-16.5694826);
		this.mapView.setZoomLevel((byte) 17);
	}
	
	@Override
	protected void onDestroy() {
		this.mapView.destroyAll();
		AndroidGraphicFactory.clearResourceMemoryCache();
		super.onDestroy();
	}
	
	private void createPositionMarker(double paramDouble1, double paramDouble2) {
		addMarkerPopup(paramDouble1, paramDouble2);
	}
	
	private void addMarkerPopup(double paramDouble1, double paramDouble2) {
		LatLong latLong = new LatLong(paramDouble1, paramDouble2);
		final View popUp = getLayoutInflater().inflate(R.layout.map_popup, mapView, false);
		popUp.findViewById(R.id.ivMarker).setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				View popupview = popUp.findViewById(R.id.llMarkerData);
				visiblePopups.add(popupview);
				popupview.setVisibility(View.VISIBLE);
			}
		});
		mapView.setOnTouchListener(new View.OnTouchListener() {
			@Override public boolean onTouch(View view, MotionEvent motionEvent) {
				Log.d("mapview","onTouch");
				for (int index = 0; index < visiblePopups.size(); index++) {
					visiblePopups.get(index).setVisibility(View.GONE);
					visiblePopups.remove(index--);
				}
				return false;
			}
		});
		MapView.LayoutParams mapParams = new MapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
																  ViewGroup.LayoutParams.WRAP_CONTENT,
																  latLong,
																  MapView.LayoutParams.Alignment.BOTTOM_CENTER);
		
		mapView.addView(popUp, mapParams);
	}
}
