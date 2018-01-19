package com.carter.graduation.design.music.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.carter.graduation.design.music.R;
import com.carter.graduation.design.music.info.MusicInfo;
import com.carter.graduation.design.music.utils.MusicUtils;
import com.carter.graduation.design.music.utils.UiUtil;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MusicFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MusicFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MusicFragment extends Fragment {


    private static final int SCAN_MUSIC_LIST = 1;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String TAG = "MusicFragment";
    // TODO: 2018/1/19 需要在系统文件中获取是否是第一次使用第一次使用需要弹出扫描音乐的对话框
    private boolean isFirstUse = true;
    private ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mSplMusic;
    private OnFragmentInteractionListener mListener;
    private ArrayList<MusicInfo> mMusicInfos;
    private Context mContext;
    private RecyclerView mRvSongListView;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SCAN_MUSIC_LIST:
                    mRvSongListView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    //获取传递的数据

                    MusicInfoAdapter musicInfoAdapter = new MusicInfoAdapter(mMusicInfos);
                    mRvSongListView.setAdapter(musicInfoAdapter);
                    //musicInfoAdapter.notifyDataSetChanged();
                    mProgressDialog.dismiss();
                    mSplMusic.setRefreshing(false);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);

        }
    };
    public MusicFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param musicInfos 音乐列表.
     * @param param2     Parameter 2.
     * @return A new instance of fragment MusicFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MusicFragment newInstance(ArrayList<MusicInfo> musicInfos, String param2) {
        MusicFragment fragment = new MusicFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_PARAM1, musicInfos);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 用于显示扫描音乐的dialog
     */
    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setMessage("扫描音乐ing  请等候");
        mProgressDialog.setTitle("提示");
        mProgressDialog.setIcon(R.drawable.small_icon);
        mProgressDialog.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            //mMusicInfos = getArguments().getParcelableArrayList(ARG_PARAM1);
//            Log.d(TAG, "onCreateView: "+mMusicInfos.get(0).getTitle());
            // mParam2 = getArguments().getString(ARG_PARAM2);
        }
        if (ContextCompat.checkSelfPermission(UiUtil.getContext(), Manifest.permission
                .READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest
                    .permission.READ_EXTERNAL_STORAGE}, 1);
        } else {
            //扫描本地歌曲库并显示  注意需要权限
            if (isFirstUse) {
                showProgressDialog();
                scanLocalMusic();
            }

        }
    }

    private void scanLocalMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                //这里为了让大家能看到进度条  让进程睡眠2s
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mMusicInfos = MusicUtils.scanAllMusicFiles();
                for (int i = 0; i < 5; i++) {
                    Log.d(TAG, "MusicFragment onCreate: " + mMusicInfos.get(i).getTitle());
                }
                Message obtain = Message.obtain();
                obtain.what = SCAN_MUSIC_LIST;
                mHandler.sendEmptyMessage(SCAN_MUSIC_LIST);
            }
        }).start();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_music, container, false);
        mRvSongListView = view.findViewById(R.id.rv_song_list);
        mSplMusic = (SwipeRefreshLayout) view.findViewById(R.id.spl_refresh_music);
        mSplMusic.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showProgressDialog();
                scanLocalMusic();
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showProgressDialog();
                    scanLocalMusic();

                } else {
                    showPermissionDialog();
                    //Toast.makeText(this, "本app需要此权限否则无法使用", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 向用户解释该权限的作用
     */
    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.create()
                .setIcon(R.drawable.icon);
        builder.setCancelable(false)
                .setMessage("本app需要此权限否则无法使用")
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest
                                .permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(mContext, "本app需要此权限否则无法使用,请前往系统设置开启",
                        Toast
                                .LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }

    private class MusicInfoAdapter extends RecyclerView.Adapter<MusicInfoAdapter.ViewHolder> {

        private ArrayList<MusicInfo> musicInfos;

        public MusicInfoAdapter(ArrayList<MusicInfo> musicInfos) {
            this.musicInfos = musicInfos;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (mContext == null) {
                mContext = parent.getContext();
            }
            View view = LayoutInflater.from(mContext).inflate(R.layout.list_music_info, parent, false);


            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            MusicInfo musicInfo = musicInfos.get(position);
            holder.tvTitle.setText(musicInfo.getTitle());
            holder.tvArtist.setText(musicInfo.getAlbum());
            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(UiUtil.getContext(), "正在播放" + mMusicInfos.get(position).getTitle(), Toast
                            .LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return musicInfos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView cardView;
            TextView tvTitle;
            TextView tvArtist;

            ViewHolder(View itemView) {
                super(itemView);

                cardView = (CardView) itemView;
                tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
                tvArtist = (TextView) itemView.findViewById(R.id.tv_artist);
            }
        }
    }
}