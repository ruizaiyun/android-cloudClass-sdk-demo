package com.easefun.polyv.commonui.adapter.viewholder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.blankj.utilcode.util.ConvertUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.easefun.polyv.cloudclass.chat.send.custom.PolyvCustomEvent;
import com.easefun.polyv.commonui.R;
import com.easefun.polyv.commonui.adapter.PolyvBaseRecyclerViewAdapter;
import com.easefun.polyv.commonui.adapter.itemview.IPolyvCustomMessageBaseItemView;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvMyProgressManager;
import com.easefun.polyv.commonui.utils.glide.progress.PolyvOnProgressListener;
import com.easefun.polyv.foundationsdk.log.PolyvCommonLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author df
 * @create 2019/1/16
 * @Describe viewhodler的基类
 */
public abstract class ClickableViewHolder<M, Q extends PolyvBaseRecyclerViewAdapter> extends RecyclerView.ViewHolder {

    private static final String TAG = "ClickableViewHolder";

    public ImageView resendMessageButton;
    public FrameLayout contentContainer;
    protected View parentView;
    protected Q adapter;
    protected Context context;

    public ClickableViewHolder(View itemView, Q adapter) {
        super(itemView);
        this.parentView = itemView;
        this.contentContainer = parentView.findViewById(R.id.message_container);
        resendMessageButton = $(com.easefun.polyv.commonui.R.id.resend_message_button);
        this.adapter = adapter;
        this.context = parentView.getContext();
    }


    public View getParentView() {
        return parentView;
    }

    @SuppressWarnings("unchecked")
    public <T extends View> T $(@IdRes int id) {
        return parentView.findViewById(id);
    }

    // <editor-fold defaultstate="collapsed" desc=""抽象方法>
    //处理普通接收到的消息
    public abstract void processNormalMessage(M item, int position);

    //处理自定义消息
    public abstract  void processCustomMessage(PolyvCustomEvent item, int position);

    //创建itemview
    public abstract  <T> IPolyvCustomMessageBaseItemView createItemView(PolyvCustomEvent<T> baseCustomEvent) ;
    // </editor-fold>

    //是否需要复用container里的childview
    public int findReuseChildIndex(String type){
       int childIndex = -1;
        int count = contentContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = contentContainer.getChildAt(i);
            if(type.equals(child.getTag())){
                PolyvCommonLog.d(TAG,"findReuseChildIndex");
                if(child.getVisibility() != View.VISIBLE){
                    child.setVisibility(View.VISIBLE);
                }
                childIndex = i;
            }else {
                if(child.getVisibility() != View.GONE){
                    child.setVisibility(View.GONE);
                }
            }
        }


        return childIndex;
    }

    protected void fitChatImgWH(int width, int height, View view) {
        int maxLength = ConvertUtils.dp2px(132);
        int minLength = ConvertUtils.dp2px(50);
        //计算显示的图片大小
        float percentage = width * 1f / height;
        if (percentage == 1) {//方图
            if (width < minLength) {
                width = height = minLength;
            } else if (width > maxLength) {
                width = height = maxLength;
            }
        } else if (percentage < 1) {//竖图
            height = maxLength;
            width = (int) Math.max(minLength, height * percentage);
        } else {//横图
            width = maxLength;
            height = (int) Math.max(minLength, width / percentage);
        }
        ViewGroup.LayoutParams vlp = view.getLayoutParams();
        vlp.width = width;
        vlp.height = height;
        view.setLayoutParams(vlp);
    }


    // <editor-fold defaultstate="collapsed" desc="图片处理方法">
    protected void loadNetImg(String chatImg, final int position, final ProgressBar imgLoading, final ImageView imageView) {
        PolyvMyProgressManager.removeListener(chatImg, position);
        final PolyvOnProgressListener onProgressListener = new PolyvOnProgressListener() {
            @Override
            public void onStart(String url) {//后台回来会重新开始
                if ((int) imgLoading.getTag() != position)
                    return;
                if (imgLoading.getProgress() == 0 && imgLoading.getVisibility() != View.VISIBLE) {
                    imgLoading.setVisibility(View.VISIBLE);
                    imageView.setImageDrawable(null);
                }
            }

            @Override
            public void onProgress(String url, boolean isComplete, int percentage, long bytesRead, long totalBytes) {
                if ((int) imgLoading.getTag() != position)
                    return;
                if (isComplete) {
                    imgLoading.setVisibility(View.GONE);

                    imgLoading.setProgress(100);
                } else if (imageView.getDrawable() == null) {//onFailed之后可能触发onProgress
                    imgLoading.setVisibility(View.VISIBLE);
                    imgLoading.setProgress(percentage);
                }
            }

            @Override
            public void onFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                if ((int) imgLoading.getTag() != position)
                    return;
                imgLoading.setVisibility(View.GONE);
                imgLoading.setProgress(0);
            }
        };
        PolyvMyProgressManager.addListener(chatImg, position, onProgressListener);
        putImgUrl(chatImg, position);
        loadChatImg(chatImg, onProgressListener, imageView, position);
    }

    protected void putImgUrl(String imgUrl, int position) {
        if (adapter == null) {
            return;
        }

        List<Integer> values = adapter.getLoadImgMap().get(imgUrl);
        if (values != null) {
            boolean isExisted = false;
            for (int i = 0; i < values.size(); i++) {
                if (values.get(i) == position) {
                    isExisted = true;
                    break;
                }
            }
            if (!isExisted) {
                values.add(position);
            }
        } else {
            values = new ArrayList<>();
            values.add(position);
            adapter.getLoadImgMap().put(imgUrl, values);
        }
    }

    private void removeImgUrl(String imgUrl, int position) {
        if (adapter == null) {
            return;
        }
        List<Integer> values = adapter.getLoadImgMap().get(imgUrl);
        if (values != null) {
            for (int i = 0; i < values.size(); i++) {
                int ePosition = values.get(i);
                if (ePosition == position) {
                    values.remove(i);
                    break;
                }
            }
        }
    }

    private void loadChatImg(final String chatImg, final PolyvOnProgressListener onProgressListener, final ImageView view, final int position) {
        Glide.with(parentView.getContext())
                .load(chatImg)
                .apply(new RequestOptions().error(com.easefun.polyv.commonui.R.drawable.polyv_image_load_err))//dontAnimate，不显示gif
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        onProgressListener.onFailed(e, model, target, isFirstResource);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        PolyvMyProgressManager.removeListener(chatImg, position);
                        removeImgUrl(chatImg, position);
                        onProgressListener.onProgress(chatImg, true, 100, 0, 0);
                        return false;
                    }
                })
                .into(view);
    }
// </editor-fold>
}
