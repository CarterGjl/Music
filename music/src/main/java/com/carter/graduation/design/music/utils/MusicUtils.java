package com.carter.graduation.design.music.utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import com.carter.graduation.design.music.R;
import com.carter.graduation.design.music.info.MusicInfo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by newthinkpad on 2018/1/19.
 * 用于检索音乐文件
 */

public class MusicUtils {

    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    public static ArrayList<MusicInfo> scanAllMusicFiles() {
        ArrayList<MusicInfo> musicInfos = new ArrayList<>();
        Cursor cursor = UiUtil.getContext().getContentResolver().query
                (MediaStore.Audio
                                .Media.EXTERNAL_CONTENT_URI, null, null, null,
                        MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        ._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .ALBUM));
                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .ALBUM_ID));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .ARTIST));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .DATA));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .DURATION));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .SIZE));

                if (size > 1024 * 80) {
                    //大于80k
                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.setId(id);
                    musicInfo.setAlbum(album);
                    musicInfo.setDuration(duration);
                    musicInfo.setSize(size);
                    musicInfo.setTitle(title);
                    musicInfo.setUrl(url);
                    musicInfo.setAlbum_id(albumId);
                    musicInfos.add(musicInfo);
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
        return musicInfos;
    }

    /**
     * 将毫秒转换为分:秒格式
     *
     * @param time 需要格式化的时间
     * @return
     */
    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    /**
     * 获取默认专辑图片
     *
     * @param context
     * @return
     */
    @SuppressLint("ResourceType")
    public static Bitmap getDefaultArtwork(Context context, boolean small) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        if (small) {  //返回小图片
            return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable
                    .play_img_default), null, opts);
        }
        return BitmapFactory.decodeStream(context.getResources().openRawResource(R.drawable.play_img_default),
                null, opts);
    }


    /**
     * 从文件当中获取专辑封面位图
     *
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            FileDescriptor fd = null;
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            options.inSampleSize = 1;
            // 只进行大小判断
            options.inJustDecodeBounds = true;
            // 调用此方法得到options得到图片大小
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            // 我们的目标是在800pixel的画面上显示
            // 所以需要调用computeSampleSize得到图片缩放的比例
            options.inSampleSize = 100;
            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    /**
     * 获取专辑封面位图对象
     *
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefalut
     * @return
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id, boolean allowdefalut, boolean small) {
        if (album_id < 0) {
            if (song_id < 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefalut) {
                return getDefaultArtwork(context, small);
            }
            return null;
        }
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(albumArtUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                //先制定原始大小
                options.inSampleSize = 1;
                //只进行大小判断
                options.inJustDecodeBounds = true;
                //调用此方法得到options得到图片的大小
                BitmapFactory.decodeStream(in, null, options);
                /** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例 **/
                /** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合 **/
                if (small) {
                    options.inSampleSize = computeSampleSize(options, 40);
                } else {
                    options.inSampleSize = computeSampleSize(options, 600);
                }
                // 我们得到了缩放比例，现在开始正式读入Bitmap数据
                options.inJustDecodeBounds = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, options);
            } catch (FileNotFoundException e) {
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefalut) {
                            return getDefaultArtwork(context, small);
                        }
                    }
                } else if (allowdefalut) {
                    bm = getDefaultArtwork(context, small);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 对图片进行合适的缩放
     *
     * @param options
     * @param target
     * @return
     */
    public static int computeSampleSize(BitmapFactory.Options options, int target) {
        int w = options.outWidth;
        int h = options.outHeight;
        int candidateW = w / target;
        int candidateH = h / target;
        int candidate = Math.max(candidateW, candidateH);
        if (candidate == 0) {
            return 1;
        }
        if (candidate > 1) {
            if ((w > target) && (w / candidate) < target) {
                candidate -= 1;
            }
        }
        if (candidate > 1) {
            if ((h > target) && (h / candidate) < target) {
                candidate -= 1;
            }
        }
        return candidate;
    }
    public static HashSet<MusicInfo> queryMusic(Context context,String key){
        HashSet<MusicInfo> musicList = new HashSet<>();
        Cursor cursor = null;
        cursor= context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,
                MediaStore.Audio.Media.DISPLAY_NAME + " LIKE '%" + key + "%'",null,MediaStore.Audio
                        .Media.DEFAULT_SORT_ORDER);
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        ._ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .ALBUM));
                int albumId = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .ALBUM_ID));
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .ARTIST));
                String url = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .DATA));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .DURATION));
                long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media
                        .SIZE));

                if (size > 1024 * 80) {
                    //大于80k
                    MusicInfo musicInfo = new MusicInfo();
                    musicInfo.setId(id);
                    musicInfo.setAlbum(album);
                    musicInfo.setDuration(duration);
                    musicInfo.setSize(size);
                    musicInfo.setTitle(title);
                    musicInfo.setUrl(url);
                    musicInfo.setAlbum_id(albumId);
                    musicList.add(musicInfo);
                }
                cursor.moveToNext();
            }
        }
        return musicList;
    }
    public static boolean isExists(String path) {
        File file = new File(path);
        return file.exists();
    }
}
