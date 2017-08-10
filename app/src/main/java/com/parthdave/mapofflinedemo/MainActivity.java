package com.parthdave.mapofflinedemo;

import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.Path;
import com.graphhopper.util.Parameters;
import com.graphhopper.util.PointList;

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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

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
	
	private LatLng firstPoint = new LatLng(28.5696778, -16.1596187), secondPoint = new LatLng(28.5492227, -16.1933043), centerPoint = new LatLng(28.4698336, -16.3259077);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		grassHopperFolder = getFilesDir() + "/grasshopper";
		grassHopperAlerterFolder = getFilesDir() + "/grassHopperAlerterFolder";
		if (!(new File(grassHopperFolder).exists())) {
			new File(grassHopperFolder).mkdir();
		}
		if (!(new File(grassHopperAlerterFolder).exists())) {
			new File(grassHopperAlerterFolder).mkdir();
		}
		
		new AsyncService().execute();
	}
	
	private void extractZips() {
		copyAssets();
		unpackZip(grassHopperFolder, "grasshoper.zip");
	}
	
	
	private boolean unpackZip(String path, String zipname) {
		InputStream is;
		ZipInputStream zis;
		try {
			String filename;
			is = new FileInputStream(path +"/"+ zipname);
			zis = new ZipInputStream(new BufferedInputStream(is));
			ZipEntry ze;
			byte[] buffer = new byte[1024];
			int count;
			
			while ((ze = zis.getNextEntry()) != null) {
				// zapis do souboru
				filename = ze.getName();
				
				// Need to create directories if not exists, or
				// it will generate an Exception...
				if (ze.isDirectory()) {
					File fmd = new File(path +"/"+ filename);
					fmd.mkdirs();
					continue;
				}
				
				FileOutputStream fout = new FileOutputStream(path +"/"+ filename);
				
				// cteni zipu a zapis
				while ((count = zis.read(buffer)) != -1) {
					fout.write(buffer, 0, count);
				}
				
				fout.close();
				zis.closeEntry();
			}
			
			zis.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	String grassHopperOsmFile;
	String grassHopperFolder;
	String grassHopperAlerterFolder;
	
	private void getDirections() {
		// create one GraphHopper instance
		hopper = new GraphHopper().forMobile();
		//		hopper.setDataReaderFile(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm");
		//		grassHopperOsmFile = new File(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm").getAbsolutePath();
		//		hopper.setDataReaderFile(grassHopperOsmFile);
		
		// where to store graphhopper files?
		
		
		hopper.setGraphHopperLocation(grassHopperFolder);
		
		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		hopper.load(grassHopperFolder);
		
		GHRequest req = new GHRequest(firstPoint.latitude, firstPoint.longitude, secondPoint.latitude, secondPoint.longitude).setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);//setWeighting("fastest");
		GHResponse rsp = hopper.route(req);
		
		// first check for errors
		if (rsp.hasErrors()) {
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
		hopper.close();
		//		findAlternateDiraction();
	}
	
	
	private void findAlternateDiraction() {
		// create one GraphHopper instance
		GraphHopper hopper = new GraphHopper().forMobile();
		//		hopper.setDataReaderFile(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm");
		//		grassHopperOsmFile = new File(Environment.getExternalStorageDirectory()+"/Downloads/tenerife.osm").getAbsolutePath();
		//		hopper.setDataReaderFile(grassHopperOsmFile);
		
		// where to store graphhopper files?
		if (!(new File(grassHopperAlerterFolder).exists())) {
			new File(grassHopperAlerterFolder).mkdir();
		}
		hopper.setGraphHopperLocation(grassHopperAlerterFolder);
		
		// now this can take minutes if it imports or a few seconds for loading
		// of course this is dependent on the area you import
		hopper.setCHEnabled(false);
		hopper.load(grassHopperAlerterFolder);
		
		GHRequest req = new GHRequest(firstPoint.latitude, firstPoint.longitude, secondPoint.latitude, secondPoint.longitude).setAlgorithm(Parameters.Algorithms.DIJKSTRA_BI);
		//		req.setWeighting("fastest");
		GHResponse rsp = hopper.route(req);
		
		// first check for errors
		if (rsp.hasErrors()) {
			// handle them!
			// rsp.getErrors()
			Iterator<Throwable> throwableIterator = rsp.getErrors().iterator();
			while (throwableIterator.hasNext()) {
				Throwable throwable = throwableIterator.next();
				throwable.printStackTrace();
				System.out.println("Message:" + throwable.getMessage());
			}
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
		
		Iterator<PathWrapper> pathWrapperIterator = rsp.getAll().iterator();
		
		while (pathWrapperIterator.hasNext()) {
			PathWrapper pathWrapper = pathWrapperIterator.next();
			
			System.out.println("Time:" + pathWrapper.getTime());
			System.out.println("Distance:" + pathWrapper.getDistance());
		}
		hopper.close();
		//		Polyline polyline = createPolyline(rsp);
		//
		//		this.mapView.getLayerManager().getLayers().add(polyline);
		//
		//		findAlternateDiraction();
	}
	
	
/*	private void findAlternateDiraction(){
		FlagEncoder encoder = new CarFlagEncoder();
		EncodingManager em = new EncodingManager(encoder);
		GraphBuilder gb = new GraphBuilder(em).setLocation(grassHopperFolder).setStore(true);
		GraphHopperStorage graph = gb.create();
		
		
		graph.edge(1, 2, 10, false);
		graph.edge(2, 3, 10, false);
		graph.edge(1, 3, 20, false);
		
		graph.flush();
		graph = gb.load();
		
		AlternativeRoute alternativeRoute = new AlternativeRoute(graph,new ShortestWeighting(encoder), TraversalMode.NODE_BASED);
		
		// trying to tweak the parameters here
		// alternativeRoute.setMaxPaths(5);
		// alternativeRoute.setMaxExplorationFactor(5);
		// alternativeRoute.setMaxWeightFactor(5);
		// alternativeRoute.setMaxShareFactor(1);
		// alternativeRoute.setMinPlateauFactor(0);
		
		
		List<Path> paths = alternativeRoute.calcPaths(1, 3);
		for (Path path : paths) {
			System.out.println(path.toDetailsString());
			System.out.println(path.calcNodes());
			System.out.println(path.calcEdges());
		}
	}*/
	
	private Polyline createPolyline(GHResponse response) {
		GraphicFactory gf = AndroidGraphicFactory.INSTANCE;
		Paint paint = gf.createPaint();
		paint.setColor(AndroidGraphicFactory.INSTANCE.createColor(Color.BLACK));
		paint.setStyle(Style.STROKE);
		paint.setDashPathEffect(new float[]{25, 15});
		paint.setStrokeWidth(8);
		Polyline line = new Polyline(paint, AndroidGraphicFactory.INSTANCE);
		
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
		
		MapDataStore mapDataStore = new MapFile(new File(grassHopperFolder, MAP_FILE));
		this.tileRendererLayer = new TileRendererLayer(tileCache, mapDataStore, this.mapView.getModel().mapViewPosition, AndroidGraphicFactory.INSTANCE);
		tileRendererLayer.setXmlRenderTheme(InternalRenderTheme.DEFAULT);
		this.mapView.getLayerManager().getLayers().add(tileRendererLayer);
		this.mapView.setCenter(new LatLong(firstPoint.latitude, firstPoint.longitude));
		createPositionMarker(firstPoint.latitude, firstPoint.longitude);
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
				Log.d("mapview", "onTouch");
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
	
	
	private void copyAssets() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");
		} catch (IOException e) {
			Log.e("tag", "Failed to get asset file list.", e);
		}
		if (files != null)
			for (String filename : files) {
				if(!filename.equals("grasshoper.zip"))
					continue;
				InputStream in = null;
				OutputStream out = null;
				try {
					in = assetManager.open(filename);
					File outFile = new File(grassHopperFolder, filename);
					out = new FileOutputStream(outFile);
					copyFile(in, out);
				} catch (IOException e) {
					Log.e("tag", "Failed to copy asset file: " + filename, e);
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							// NOOP
						}
					}
					if (out != null) {
						try {
							out.close();
						} catch (IOException e) {
							// NOOP
						}
					}
				}
			}
	}
	
	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
	
	
	class AsyncService  extends AsyncTask<Void,Void,Void> {
		
		@Override protected Void doInBackground(Void... voids) {
			extractZips();
			return null;
		}
		
		@Override protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			setUpMap();
			
			
			getDirections();
		}
	}
}
