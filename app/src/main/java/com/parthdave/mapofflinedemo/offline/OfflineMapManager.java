package com.parthdave.mapofflinedemo.offline;

import com.google.android.gms.maps.model.TileProvider;
import com.parthdave.mapofflinedemo.MyApplication;
import com.parthdave.mapofflinedemo.offline.tilemaps.OfflineTileProvider;


public class OfflineMapManager {
	public static TileProvider getOfflineTileProvider() {
		return new OfflineTileProvider();
	}
	
	public static boolean hasDownloadedTileMaps() {
		return true;
	}
}
