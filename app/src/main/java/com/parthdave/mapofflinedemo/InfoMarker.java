package com.parthdave.mapofflinedemo;

import org.mapsforge.core.model.LatLong;
import org.mapsforge.core.model.Point;
import org.mapsforge.map.layer.overlay.Marker;

public class InfoMarker<T> extends Marker {
	
	private Runnable action;
	private Object callbackObject;
	private MarkerCallbacks markerCallbacks;
	
	
	public interface MarkerCallbacks<T>{
		public void onMarkerClickEvent(T object);
	}
	
	public InfoMarker(LatLong latLong, org.mapsforge.core.graphics.Bitmap bitmap, MarkerCallbacks markerCallbacks,Object callbackObject) {
		super(latLong, bitmap, bitmap.getWidth()/2, bitmap.getHeight()/2);
		this.markerCallbacks = markerCallbacks;
		this.callbackObject = callbackObject;
	}
	
	
	public void setOnTabAction(Runnable action) {
		this.action = action;
	}
	
	@Override
	public boolean onTap(LatLong tapLatLong, Point layerXY, Point tapXY) {
		
		
		double centerX = layerXY.x + getHorizontalOffset();
		double centerY = layerXY.y + getVerticalOffset();
		
		double radiusX = (getBitmap().getWidth() / 2) * 1.1;
		double radiusY = (getBitmap().getHeight() / 2) * 1.1;
		
		
		double distX = Math.abs(centerX - tapXY.x);
		double distY = Math.abs(centerY - tapXY.y);
		
		
		if (distX < radiusX && distY < radiusY) {
			
			if(markerCallbacks!=null){
				markerCallbacks.onMarkerClickEvent(callbackObject);
			}
			
			if (action != null) {
				action.run();
				return true;
			}
		}
		
		
		return false;
	}
	
}