package com.parthdave.mapofflinedemo.offline.tilemaps;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;
import com.parthdave.mapofflinedemo.MyApplication;
import com.parthdave.mapofflinedemo.utils.AppSettings;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class OfflineTileProvider implements TileProvider {
    private static final int BUFFER_SIZE = 16384;
    private static final String EXTERNAL_FOLDER_NAME = "map";
    private static final int TILE_HEIGHT = 256;
    private static final int TILE_WIDTH = 256;

    public Tile getTile(int x, int y, int zoom) {
        byte[] image = new byte[0];
        int digizoom = 0;
        int fullCoverZoom = AppSettings.getMapCoverageZoomLevel();
        int rx = x;
        int ry = y;
        while (true) {
            try {
                image = readTileImage(rx, ry, zoom - digizoom);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            if (image != null) {
                break;
            }
            digizoom++;
            if (fullCoverZoom > zoom - digizoom) {
                return NO_TILE;
            }
            rx >>= 1;
            ry >>= 1;
        }
        if (digizoom > 0) {
            Bitmap b = BitmapFactory.decodeByteArray(image, 0, image.length);
            int crop_width = b.getWidth() >> digizoom;
            int bitmask = (1 << digizoom) - 1;
            Bitmap bitmapResized = Bitmap.createBitmap(b, (x & bitmask) * crop_width, (y & bitmask) * crop_width, crop_width, crop_width);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapResized.compress(CompressFormat.PNG, 100, stream);
            image = stream.toByteArray();
        }
        return new Tile(TILE_WIDTH, TILE_WIDTH, image);
    }

    private byte[] readTileImage(int x, int y, int zoom) {
        OutOfMemoryError e;
        Throwable th;
        IOException e2;
        byte[] bArr = null;
        InputStream in = null;
        ByteArrayOutputStream buffer = null;
        try {
            ByteArrayOutputStream buffer2 = null;
            InputStream in2 = new FileInputStream(getTileFilename(x, y, zoom));
            try {
                buffer2 = new ByteArrayOutputStream();
            } catch (OutOfMemoryError e3) {
                e3.printStackTrace();
                e = e3;
                in = in2;
                try {
                    e.printStackTrace();
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e4) {
                        }
                    }
                    if (buffer != null) {
                        try {
                            buffer.close();
                        } catch (Exception e5) {
                        }
                    }
                    return bArr;
                } catch (Throwable th2) {
                    th = th2;
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e6) {
                        }
                    }
                    if (buffer != null) {
                        try {
                            buffer.close();
                        } catch (Exception e7) {
                        }
                    }
                    try {
                        throw th;
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th3.printStackTrace();
                th = th3;
                in = in2;
                if (in != null) {
                    in.close();
                }
                if (buffer != null) {
                    buffer.close();
                }
                try {
                    throw th;
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
    
            }
            try {
                byte[] data = new byte[BUFFER_SIZE];
                while (true) {
                    int nRead = in2.read(data, 0, BUFFER_SIZE);
                    if (nRead == -1) {
                        break;
                    }
                    buffer2.write(data, 0, nRead);
                }
                buffer2.flush();
                bArr = buffer2.toByteArray();
                if (in2 != null) {
                    try {
                        in2.close();
                    } catch (Exception e11) {
                    }
                }
                if (buffer2 != null) {
                    try {
                        buffer2.close();
                    } catch (Exception e12) {
                    }
                }
                buffer = buffer2;
                in = in2;
            } catch (OutOfMemoryError e13) {
                e13.printStackTrace();
                e = e13;
                buffer = buffer2;
                in = in2;
                e.printStackTrace();
                if (in != null) {
                    in.close();
                }
                if (buffer != null) {
                    buffer.close();
                }
                return bArr;
            } catch (IOException e14) {
                e14.printStackTrace();
                e2 = e14;
                buffer = buffer2;
                in = in2;
                if (e2 instanceof FileNotFoundException) {
                    if (in != null) {
                        in.close();
                    }
                    if (buffer != null) {
                        buffer.close();
                    }
                    return bArr;
                }
                if (in != null) {
                    in.close();
                }
                if (buffer != null) {
                    buffer.close();
                }
                return bArr;
            } catch (Throwable th4) {
                th = th4;
                buffer = buffer2;
                in = in2;
                if (in != null) {
                    in.close();
                }
                if (buffer != null) {
                    buffer.close();
                }
            }
        } catch (OutOfMemoryError e15) {
            e15.printStackTrace();
            e = e15;
            e.printStackTrace();
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return bArr;
        } catch (IOException e16) {
            e16.printStackTrace();
            e2 = e16;
            if (e2 instanceof FileNotFoundException) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                if (buffer != null) {
                    try {
                        buffer.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return bArr;
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (buffer != null) {
                try {
                    buffer.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            return bArr;
        }
        return bArr;
    }
    
    private String getTileFilename(int x, int y, int zoom) {
        String dataPath = null;
        try {
            dataPath = getDataDir(MyApplication.getAppContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataPath + "/" + zoom + '/' + x + '/' + y + ".png";
    }
    
    public static String getDataDir(Context context) throws Exception {
        String path = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir + "/map";
        System.out.println("path:"+path);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }
}
